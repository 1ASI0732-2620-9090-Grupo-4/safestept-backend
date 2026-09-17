package com.safestep.platform.commerce.application.internal.commandservices;

import com.safestep.platform.commerce.application.commandservices.CommerceCommandService;
import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeCheckoutClient;
import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeWebhookVerifier;
import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.aggregates.RedeemedCoupon;
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
import com.safestep.platform.commerce.domain.model.commands.RedeemCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCartItemCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateProductCommand;
import com.safestep.platform.commerce.domain.model.entities.*;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeCheckoutSession;
import com.safestep.platform.commerce.domain.repositories.*;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade;
import com.safestep.platform.shared.application.result.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class CommerceCommandServiceImpl implements CommerceCommandService {
    private final ProductRepository products;
    private final CouponRepository coupons;
    private final RedeemedCouponRepository redeemedCoupons;
    private final ShoppingCartRepository carts;
    private final OrderRepository orders;
    private final StripeCheckoutClient stripeCheckoutClient;
    private final StripeWebhookVerifier stripeWebhookVerifier;
    private final GamificationContextFacade gamification;

    public CommerceCommandServiceImpl(ProductRepository p, ShoppingCartRepository c, OrderRepository o,
            StripeCheckoutClient stripeCheckoutClient, StripeWebhookVerifier stripeWebhookVerifier) {
        this(p, null, null, c, o, stripeCheckoutClient, stripeWebhookVerifier, null);
    }

    public CommerceCommandServiceImpl(ProductRepository p, CouponRepository couponRepository, ShoppingCartRepository c,
            OrderRepository o, StripeCheckoutClient stripeCheckoutClient, StripeWebhookVerifier stripeWebhookVerifier) {
        this(p, couponRepository, null, c, o, stripeCheckoutClient, stripeWebhookVerifier, null);
    }

    @Autowired
    public CommerceCommandServiceImpl(ProductRepository p, CouponRepository couponRepository,
            RedeemedCouponRepository redeemedCoupons, ShoppingCartRepository c, OrderRepository o,
            StripeCheckoutClient stripeCheckoutClient, StripeWebhookVerifier stripeWebhookVerifier,
            GamificationContextFacade gamification) {
        products = p;
        coupons = couponRepository;
        this.redeemedCoupons = redeemedCoupons;
        carts = c;
        orders = o;
        this.stripeCheckoutClient = stripeCheckoutClient;
        this.stripeWebhookVerifier = stripeWebhookVerifier;
        this.gamification = gamification;
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

        RedeemedCoupon redeemed = null;
        if (c.redeemedCouponExternalId() != null && !c.redeemedCouponExternalId().isBlank()) {
            var found = redeemedCoupons.findByExternalId(c.redeemedCouponExternalId());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("redeemed coupon", c.redeemedCouponExternalId()));
            redeemed = found.get();
            if (!redeemed.getUsername().equals(c.username()))
                return Result.failure(ApplicationError.businessRuleViolation("coupon ownership",
                        "Redeemed coupon does not belong to current user"));
            if (redeemed.getStatus() != RedemptionStatus.AVAILABLE)
                return Result.failure(
                        ApplicationError.businessRuleViolation("coupon", "Redeemed coupon is not available"));
            var subtotal = items.stream().map(OrderItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (redeemed.getType() == CouponType.PERCENTAGE_OFF_MIN_PURCHASE
                    && subtotal.compareTo(redeemed.getMinPurchaseAmount()) < 0)
                return Result.failure(ApplicationError.businessRuleViolation("coupon",
                        "Order total does not reach the coupon's minimum purchase amount"));
        }

        var order = new Order(null, "ord-" + UUID.randomUUID(), c.username(), items, OrderStatus.from(c.status()),
                LocalDate.now(), null, PaymentStatus.NONE, null, null, null,
                redeemed == null ? null : redeemed.getDiscountPercentage(),
                redeemed == null ? null : redeemed.getExternalId());
        orders.save(order);
        if (redeemed != null) {
            redeemed.markUsed(Instant.now());
            redeemedCoupons.save(redeemed);
        }
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
        if (order.get().getStatus() != OrderStatus.PAID) {
            order.get().markStripePaymentFailed();
            releaseRedeemedCoupon(order.get());
        }
        return Result.success(orders.save(order.get()));
    }

    private void releaseRedeemedCoupon(Order order) {
        if (order.getRedeemedCouponExternalId() == null)
            return;
        redeemedCoupons.findByExternalId(order.getRedeemedCouponExternalId()).ifPresent(rc -> {
            rc.release();
            redeemedCoupons.save(rc);
        });
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
            releaseRedeemedCoupon(order.get());
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
        var discountError = validateCouponDiscount(coupon);
        if (discountError != null)
            return Result.failure(discountError);
        if (coupons.existsByExternalId(coupon.getExternalId()))
            return Result.failure(ApplicationError.conflict("coupon", "Coupon id already exists"));
        return Result.success(coupons.save(coupon));
    }

    private ApplicationError validateCouponDiscount(Coupon coupon) {
        if (coupon.getDiscountPercentage() < 1 || coupon.getDiscountPercentage() > 100)
            return ApplicationError.validationError("coupon", "Discount percentage must be between 1 and 100");
        if (coupon.getType() == com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType.PERCENTAGE_OFF_MIN_PURCHASE
                && (coupon.getMinPurchaseAmount() == null || coupon.getMinPurchaseAmount().signum() <= 0))
            return ApplicationError.validationError("coupon",
                    "Minimum purchase amount is required and must be positive for this coupon type");
        return null;
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
        var discountError = validateCouponDiscount(c.coupon());
        if (discountError != null)
            return Result.failure(discountError);
        var coupon = new Coupon(found.get().getId(), c.couponId(), c.coupon().getTitle(), c.coupon().getCostCoins(),
                c.coupon().getType(), c.coupon().getDiscountPercentage(), c.coupon().getMinPurchaseAmount());
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

    @Transactional
    @Override
    public Result<RedeemedCoupon, ApplicationError> handle(RedeemCouponCommand c) {
        var coupon = coupons.findByExternalId(c.couponId());
        if (coupon.isEmpty())
            return Result.failure(ApplicationError.notFound("coupon", c.couponId()));
        var spent = gamification.spendCoins(c.username(), coupon.get().getCostCoins(), coupon.get().getExternalId(),
                coupon.get().getTitle());
        if (!spent)
            return Result.failure(ApplicationError.businessRuleViolation("insufficient-coins",
                    "Not enough SafeCoins to redeem this coupon"));
        var redeemed = new RedeemedCoupon(null, "rdc-" + UUID.randomUUID(), c.username(), coupon.get().getExternalId(),
                coupon.get().getTitle(), coupon.get().getType(), coupon.get().getDiscountPercentage(),
                coupon.get().getMinPurchaseAmount(), Instant.now(), null, RedemptionStatus.AVAILABLE);
        return Result.success(redeemedCoupons.save(redeemed));
    }
}
