package com.safestep.platform.commerce.interfaces.rest.transform;

import com.safestep.platform.commerce.domain.model.aggregates.*;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.interfaces.rest.resources.CartItemResource;
import com.safestep.platform.commerce.interfaces.rest.resources.CouponResource;
import com.safestep.platform.commerce.interfaces.rest.resources.RedeemedCouponResource;
import com.safestep.platform.commerce.interfaces.rest.resources.OrderItemResource;
import com.safestep.platform.commerce.interfaces.rest.resources.OrderResource;
import com.safestep.platform.commerce.interfaces.rest.resources.ProductResource;

public final class CommerceResourceAssembler {
    private CommerceResourceAssembler() {
    }

    public static ProductResource toResource(Product p) {
        return new ProductResource(p.getExternalId(), p.getName(), p.getCategory(), p.getType(), p.getPrice(),
                p.getOldPrice(), p.getRating(), p.getStock(), p.getImageUrl(), p.getTags(), p.getDescription());
    }

    public static Product toProduct(ProductResource r) {
        return new Product(null, r.id(), r.name(), r.category(), r.type(), r.price(), r.oldPrice(), r.rating(),
                r.stock(), r.imageUrl(), r.description(), r.tags());
    }

    public static CouponResource toResource(Coupon c) {
        return new CouponResource(c.getExternalId(), c.getTitle(), c.getCostCoins(), c.getType().name(),
                c.getDiscountPercentage(), c.getMinPurchaseAmount());
    }

    public static Coupon toCoupon(CouponResource r) {
        return new Coupon(null, r.id(), r.title(), r.costCoins(), CouponType.from(r.type()), r.discountPercentage(),
                r.minPurchaseAmount());
    }

    public static RedeemedCouponResource toResource(RedeemedCoupon c) {
        return new RedeemedCouponResource(c.getExternalId(), c.getCouponId(), c.getTitle(), c.getType().name(),
                c.getDiscountPercentage(), c.getMinPurchaseAmount(), c.getRedeemedAt(), c.getUsedAt(),
                c.getStatus().name());
    }

    public static CartItemResource toResource(CartItem i) {
        return new CartItemResource(i.getExternalId(), i.getUsername(), i.getProductId(), i.getQuantity(),
                i.getAddedAt());
    }

    public static OrderResource toResource(Order o) {
        return new OrderResource(o.getExternalId(), o.getUsername(), o.total(), o.getStatus().name(),
                o.getPaymentProvider(), o.getPaymentStatus().name(), o.getStripeCheckoutSessionId(),
                o.getStripePaymentIntentId(),
                o.getItems().stream()
                        .map(i -> new OrderItemResource(i.productId(), i.productName(), i.unitPrice(), i.quantity()))
                        .toList(),
                o.getCreatedAt(), o.finalTotal(), o.getAppliedDiscountPercentage(), o.getRedeemedCouponExternalId());
    }
}
