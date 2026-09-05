package com.safestep.platform.commerce.application.internal.commandservices;

import com.safestep.platform.commerce.application.commandservices.CommerceCommandService;
import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeCheckoutClient;
import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeWebhookVerifier;
import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.commands.AddCartItemCommand;
import com.safestep.platform.commerce.domain.model.commands.CaptureStripeWebhookCommand;
import com.safestep.platform.commerce.domain.model.commands.CancelStripePaymentCommand;
import com.safestep.platform.commerce.domain.model.commands.ConfirmStripePaymentCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateOrderCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateProductCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateStripeCheckoutSessionCommand;
import com.safestep.platform.commerce.domain.model.commands.DeleteCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.DeleteProductCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCartItemCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateProductCommand;
import com.safestep.platform.commerce.domain.model.entities.*;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeCheckoutSession;
import com.safestep.platform.commerce.domain.repositories.*;
import com.safestep.platform.shared.application.result.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class CommerceCommandServiceImpl implements CommerceCommandService {
    private final ProductRepository products;
    private final CouponRepository coupons;
    private final ShoppingCartRepository carts;
    private final OrderRepository orders;
    private final StripeCheckoutClient stripeCheckoutClient;
    private final StripeWebhookVerifier stripeWebhookVerifier;

    public CommerceCommandServiceImpl(ProductRepository p, ShoppingCartRepository c, OrderRepository o,
            StripeCheckoutClient stripeCheckoutClient, StripeWebhookVerifier stripeWebhookVerifier) {
        this(p, null, c, o, stripeCheckoutClient, stripeWebhookVerifier);
    }

    @Autowired
    public CommerceCommandServiceImpl(ProductRepository p, CouponRepository couponRepository, ShoppingCartRepository c, OrderRepository o,
            StripeCheckoutClient stripeCheckoutClient, StripeWebhookVerifier stripeWebhookVerifier) {
        products = p;
        coupons = couponRepository;
        carts = c;
        orders = o;
        this.stripeCheckoutClient = stripeCheckoutClient;
        this.stripeWebhookVerifier = stripeWebhookVerifier;
    }

    @Override
    public Result<CartItem, ApplicationError> handle(AddCartItemCommand c) {
        var p = products.findByExternalId(c.productId());
        if (p.isEmpty())
            return Result.failure(ApplicationError.notFound("product", c.productId()));
        if (c.quantity() < 1 || c.quantity() > p.get().getStock())
            return Result.failure(ApplicationError.businessRuleViolation("cart quantity",
                    "Quantity must be between one and available stock"));
        return Result.success(carts.save(new CartItem(null, "cart-" + UUID.randomUUID(), c.username(), c.productId(),
                c.quantity(), Instant.now())));
    }

    @Override
    public Result<CartItem, ApplicationError> handle(UpdateCartItemCommand c) {
        var found = carts.findByExternalIdAndUsername(c.itemId(), c.username());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("cart item", c.itemId()));
        var p = products.findByExternalId(found.get().getProductId());
        if (p.isEmpty() || c.quantity() < 1 || c.quantity() > p.get().getStock())
            return Result.failure(
                    ApplicationError.businessRuleViolation("cart quantity", "Quantity exceeds available stock"));
        found.get().changeQuantity(c.quantity());
        return Result.success(carts.save(found.get()));
    }

    @Override
    public void deleteCartItem(String u, String id) {
        carts.findByExternalIdAndUsername(id, u).ifPresent(carts::delete);
    }

    @Transactional
    @Override
    public Result<Order, ApplicationError> handle(CreateOrderCommand c) {
        var cart = carts.findByUsername(c.username());
        if (cart.isEmpty())
            return Result.failure(ApplicationError.businessRuleViolation("order", "Cart is empty"));
        var items = new ArrayList<OrderItem>();
        for (var item : cart) {
            var p = products.findByExternalId(item.getProductId());
            if (p.isEmpty())
                return Result.failure(ApplicationError.notFound("product", item.getProductId()));
            try {
                p.get().reduceStock(item.getQuantity());
            } catch (IllegalArgumentException e) {
                return Result.failure(ApplicationError.businessRuleViolation("stock", e.getMessage()));
            }
            products.save(p.get());
            items.add(
                    new OrderItem(p.get().getExternalId(), p.get().getName(), p.get().getPrice(), item.getQuantity()));
        }
        var order = new Order(null, "ord-" + UUID.randomUUID(), c.username(), items, OrderStatus.from(c.status()),
                LocalDate.now());
        orders.save(order);
        carts.deleteByUsername(c.username());
        return Result.success(order);
    }

    @Transactional
    @Override
    public Result<StripeCheckoutSession, ApplicationError> handle(CreateStripeCheckoutSessionCommand c) {
        var order = orders.findByExternalId(c.orderExternalId());
        if (order.isEmpty())
            return Result.failure(ApplicationError.notFound("order", c.orderExternalId()));
        if (!order.get().getUsername().equals(c.username()))
            return Result.failure(ApplicationError.businessRuleViolation("order ownership",
                    "Order does not belong to current user"));
        if (!order.get().canStartStripeCheckout())
            return Result.failure(ApplicationError.businessRuleViolation("order payment",
                    "Order cannot start a Stripe checkout session"));
        var stripeResult = stripeCheckoutClient.createCheckoutSession(order.get());
        if (stripeResult.isFailure())
            return stripeResult;
        var session = stripeResult.toOptional().orElseThrow();
        order.get().startStripeCheckout(session.sessionId());
        orders.save(order.get());
        return Result.success(session);
    }

    @Transactional
    @Override
    public Result<Order, ApplicationError> handle(ConfirmStripePaymentCommand c) {
        var order = orders.findByExternalId(c.orderExternalId());
        if (order.isEmpty())
            return Result.failure(ApplicationError.notFound("order", c.orderExternalId()));
        if (!order.get().getUsername().equals(c.username()))
            return Result.failure(ApplicationError.businessRuleViolation("order ownership",
                    "Order does not belong to current user"));
        if (!c.stripeSessionId().equals(order.get().getStripeCheckoutSessionId()))
            return Result.failure(ApplicationError.businessRuleViolation("stripe session",
                    "Stripe session does not belong to this order"));

        var sessionResult = stripeCheckoutClient.retrieveCheckoutSession(c.stripeSessionId());
        if (sessionResult.isFailure())
            return sessionResult.map(session -> order.get());
        var session = sessionResult.toOptional().orElseThrow();
        if (!session.isPaid())
            return Result.failure(ApplicationError.businessRuleViolation("stripe payment",
                    "Stripe session payment is not paid"));

        if (order.get().getStatus() != OrderStatus.PAID)
            order.get().markStripePaymentPaid(session.paymentIntentId(), Instant.now());
        return Result.success(orders.save(order.get()));
    }

    @Transactional
    @Override
    public Result<Order, ApplicationError> handle(CancelStripePaymentCommand c) {
        var order = orders.findByExternalId(c.orderExternalId());
        if (order.isEmpty())
            return Result.failure(ApplicationError.notFound("order", c.orderExternalId()));
        if (!order.get().getUsername().equals(c.username()))
            return Result.failure(ApplicationError.businessRuleViolation("order ownership",
                    "Order does not belong to current user"));
        if (c.stripeSessionId() != null && !c.stripeSessionId().isBlank()
                && !c.stripeSessionId().equals(order.get().getStripeCheckoutSessionId()))
            return Result.failure(ApplicationError.businessRuleViolation("stripe session",
                    "Stripe session does not belong to this order"));
        if (order.get().getStatus() != OrderStatus.PAID)
            order.get().markStripePaymentFailed();
        return Result.success(orders.save(order.get()));
    }

    @Transactional
    @Override
    public Result<String, ApplicationError> handle(CaptureStripeWebhookCommand c) {
        var eventResult = stripeWebhookVerifier.verify(c.payload(), c.signature());
        if (eventResult.isFailure())
            return eventResult.map(event -> "");
        var event = eventResult.toOptional().orElseThrow();
        if (!event.isCheckoutSessionCompleted() && !event.isCheckoutSessionExpired())
            return Result.success("Event ignored");
        var order = orders.findByStripeCheckoutSessionId(event.sessionId());
        if (order.isEmpty())
            return Result.failure(ApplicationError.notFound("order", "stripe session " + event.sessionId()));
        if (event.isCheckoutSessionCompleted()) {
            if (order.get().getStatus() != OrderStatus.PAID)
                order.get().markStripePaymentPaid(event.paymentIntentId(), Instant.now());
        } else {
            order.get().markStripePaymentFailed();
        }
        orders.save(order.get());
        return Result.success("Webhook captured");
    }

    @Override
    public Result<Product, ApplicationError> handle(CreateProductCommand c) {
        try {
            var product = c.product();
            if (product.getExternalId() == null || product.getExternalId().isBlank())
                return Result.failure(ApplicationError.validationError("product", "Product id is required"));
            if (products.existsByExternalId(product.getExternalId()))
                return Result.failure(ApplicationError.conflict("product", "Product id already exists"));
            return Result.success(products.save(product));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("product", e.getMessage()));
        }
    }

    @Override
    public Result<Product, ApplicationError> handle(UpdateProductCommand c) {
        try {
            if (c.productId() == null || c.productId().isBlank())
                return Result.failure(ApplicationError.validationError("product", "Product id is required"));
            var found = products.findByExternalId(c.productId());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("product", c.productId()));
            var product = new Product(found.get().getId(), c.productId(), c.product().getName(),
                    c.product().getCategory(), c.product().getType(), c.product().getPrice(),
                    c.product().getOldPrice(), c.product().getRating(), c.product().getStock(),
                    c.product().getImageUrl(), c.product().getDescription(), c.product().getTags());
            return Result.success(products.save(product));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("product", e.getMessage()));
        }
    }

    @Override
    public Result<String, ApplicationError> handle(DeleteProductCommand c) {
        var found = products.findByExternalId(c.productId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("product", c.productId()));
        products.delete(found.get());
        return Result.success("Product deleted");
    }

    @Override
    public Result<Coupon, ApplicationError> handle(CreateCouponCommand c) {
        var coupon = c.coupon();
        if (coupon.getExternalId() == null || coupon.getExternalId().isBlank())
            return Result.failure(ApplicationError.validationError("coupon", "Coupon id is required"));
        if (coupon.getCostCoins() < 0)
            return Result.failure(ApplicationError.validationError("coupon", "Cost coins cannot be negative"));
        if (coupons.existsByExternalId(coupon.getExternalId()))
            return Result.failure(ApplicationError.conflict("coupon", "Coupon id already exists"));
        return Result.success(coupons.save(coupon));
    }

    @Override
    public Result<Coupon, ApplicationError> handle(UpdateCouponCommand c) {
        if (c.couponId() == null || c.couponId().isBlank())
            return Result.failure(ApplicationError.validationError("coupon", "Coupon id is required"));
        var found = coupons.findByExternalId(c.couponId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("coupon", c.couponId()));
        if (c.coupon().getCostCoins() < 0)
            return Result.failure(ApplicationError.validationError("coupon", "Cost coins cannot be negative"));
        var coupon = new Coupon(found.get().getId(), c.couponId(), c.coupon().getTitle(),
                c.coupon().getCostCoins(), c.coupon().getDiscount());
        return Result.success(coupons.save(coupon));
    }

    @Override
    public Result<String, ApplicationError> handle(DeleteCouponCommand c) {
        var found = coupons.findByExternalId(c.couponId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("coupon", c.couponId()));
        coupons.delete(found.get());
        return Result.success("Coupon deleted");
    }
}
