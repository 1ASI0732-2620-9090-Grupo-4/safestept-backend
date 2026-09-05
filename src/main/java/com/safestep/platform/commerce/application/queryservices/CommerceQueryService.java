package com.safestep.platform.commerce.application.queryservices;

import com.safestep.platform.commerce.domain.model.aggregates.*;
import com.safestep.platform.commerce.domain.model.queries.*;
import com.safestep.platform.commerce.domain.model.valueobjects.*;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import java.util.*;

public interface CommerceQueryService {
    List<Product> handle(GetProductsQuery query);
    Optional<Product> handle(GetProductByIdQuery query);
    List<Category> handle(GetCategoriesQuery query);
    List<EmergencyKit> handle(GetEmergencyKitsQuery query);
    List<Coupon> handle(GetCouponsQuery query);
    List<ProductRecommendation> handle(GetRecommendationsQuery query);
    List<CartItem> handle(GetCartByUsernameQuery query);
    List<Order> handle(GetOrdersByUsernameQuery query);
    List<ShippingAddress> handle(GetShippingAddressesQuery query);
    List<PaymentMethod> handle(GetPaymentMethodsQuery query);
}
