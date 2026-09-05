package com.safestep.platform.commerce.application.internal.queryservices;

import com.safestep.platform.commerce.application.queryservices.CommerceQueryService;
import com.safestep.platform.commerce.domain.model.aggregates.*;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.model.queries.*;
import com.safestep.platform.commerce.domain.model.valueobjects.*;
import com.safestep.platform.commerce.domain.repositories.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CommerceQueryServiceImpl implements CommerceQueryService {
    private final ProductRepository products;
    private final ShoppingCartRepository carts;
    private final OrderRepository orders;
    private final CategoryRepository categories;
    private final EmergencyKitRepository kits;
    private final CouponRepository coupons;
    private final ShippingAddressRepository addresses;
    private final PaymentMethodRepository payments;
    private final ProductRecommendationRepository recommendations;

    public CommerceQueryServiceImpl(ProductRepository p, ShoppingCartRepository c, OrderRepository o,
            CategoryRepository ca, EmergencyKitRepository k, CouponRepository co,
            ShippingAddressRepository a, PaymentMethodRepository pm,
            ProductRecommendationRepository r) {
        products = p;
        carts = c;
        orders = o;
        categories = ca;
        kits = k;
        coupons = co;
        addresses = a;
        payments = pm;
        recommendations = r;
    }

    @Override
    public List<Product> handle(GetProductsQuery query) {
        return products.findAll();
    }

    @Override
    public Optional<Product> handle(GetProductByIdQuery query) {
        return products.findByExternalId(query.id());
    }

    @Override
    public List<Category> handle(GetCategoriesQuery query) {
        return categories.findAll();
    }

    @Override
    public List<EmergencyKit> handle(GetEmergencyKitsQuery query) {
        return kits.findAll();
    }

    @Override
    public List<Coupon> handle(GetCouponsQuery query) {
        return coupons.findAll();
    }

    @Override
    public List<ProductRecommendation> handle(GetRecommendationsQuery query) {
        var list = recommendations.findByUsername(query.username());
        return list.isEmpty() ? recommendations.findAll() : list;
    }

    @Override
    public List<CartItem> handle(GetCartByUsernameQuery query) {
        return carts.findByUsername(query.username());
    }

    @Override
    public List<Order> handle(GetOrdersByUsernameQuery query) {
        return orders.findByUsername(query.username());
    }

    @Override
    public List<ShippingAddress> handle(GetShippingAddressesQuery query) {
        return addresses.findByUsername(query.username());
    }

    @Override
    public List<PaymentMethod> handle(GetPaymentMethodsQuery query) {
        return payments.findAll();
    }
}
