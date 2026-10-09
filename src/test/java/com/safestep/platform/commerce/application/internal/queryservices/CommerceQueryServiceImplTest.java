package com.safestep.platform.commerce.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.aggregates.RedeemedCoupon;
import com.safestep.platform.commerce.domain.model.aggregates.ShippingAddress;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.model.queries.GetCartByUsernameQuery;
import com.safestep.platform.commerce.domain.model.queries.GetCategoriesQuery;
import com.safestep.platform.commerce.domain.model.queries.GetCouponsQuery;
import com.safestep.platform.commerce.domain.model.queries.GetEmergencyKitsQuery;
import com.safestep.platform.commerce.domain.model.queries.GetOrdersByUsernameQuery;
import com.safestep.platform.commerce.domain.model.queries.GetPaymentMethodsQuery;
import com.safestep.platform.commerce.domain.model.queries.GetProductByIdQuery;
import com.safestep.platform.commerce.domain.model.queries.GetProductsQuery;
import com.safestep.platform.commerce.domain.model.queries.GetRecommendationsQuery;
import com.safestep.platform.commerce.domain.model.queries.GetRedeemedCouponsByUsernameQuery;
import com.safestep.platform.commerce.domain.model.queries.GetShippingAddressesQuery;
import com.safestep.platform.commerce.domain.model.valueobjects.Category;
import com.safestep.platform.commerce.domain.model.valueobjects.EmergencyKit;
import com.safestep.platform.commerce.domain.model.valueobjects.PaymentMethod;
import com.safestep.platform.commerce.domain.model.valueobjects.ProductRecommendation;
import com.safestep.platform.commerce.domain.repositories.CategoryRepository;
import com.safestep.platform.commerce.domain.repositories.CouponRepository;
import com.safestep.platform.commerce.domain.repositories.EmergencyKitRepository;
import com.safestep.platform.commerce.domain.repositories.OrderRepository;
import com.safestep.platform.commerce.domain.repositories.PaymentMethodRepository;
import com.safestep.platform.commerce.domain.repositories.ProductRecommendationRepository;
import com.safestep.platform.commerce.domain.repositories.ProductRepository;
import com.safestep.platform.commerce.domain.repositories.RedeemedCouponRepository;
import com.safestep.platform.commerce.domain.repositories.ShippingAddressRepository;
import com.safestep.platform.commerce.domain.repositories.ShoppingCartRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommerceQueryServiceImplTest {

    private ProductRepository products;
    private ShoppingCartRepository carts;
    private OrderRepository orders;
    private CategoryRepository categories;
    private EmergencyKitRepository kits;
    private CouponRepository coupons;
    private RedeemedCouponRepository redeemedCoupons;
    private ShippingAddressRepository addresses;
    private PaymentMethodRepository payments;
    private ProductRecommendationRepository recommendations;
    private CommerceQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        carts = mock(ShoppingCartRepository.class);
        orders = mock(OrderRepository.class);
        categories = mock(CategoryRepository.class);
        kits = mock(EmergencyKitRepository.class);
        coupons = mock(CouponRepository.class);
        redeemedCoupons = mock(RedeemedCouponRepository.class);
        addresses = mock(ShippingAddressRepository.class);
        payments = mock(PaymentMethodRepository.class);
        recommendations = mock(ProductRecommendationRepository.class);
        service = new CommerceQueryServiceImpl(products, carts, orders, categories, kits, coupons, redeemedCoupons,
                addresses, payments, recommendations);
    }

    @Test
    @DisplayName("catalog queries should return what the repositories hold (AAA)")
    void catalogQueries_DelegateToRepositories() {
        // Arrange
        var productList = List.of(mock(Product.class));
        var categoryList = List.of(mock(Category.class));
        var kitList = List.of(mock(EmergencyKit.class));
        var couponList = List.of(mock(Coupon.class));
        var paymentList = List.of(mock(PaymentMethod.class));
        var product = mock(Product.class);
        when(products.findAll()).thenReturn(productList);
        when(products.findByExternalId("kit-1")).thenReturn(Optional.of(product));
        when(categories.findAll()).thenReturn(categoryList);
        when(kits.findAll()).thenReturn(kitList);
        when(coupons.findAll()).thenReturn(couponList);
        when(payments.findAll()).thenReturn(paymentList);

        // Act + Assert
        assertSame(productList, service.handle(new GetProductsQuery()));
        assertSame(product, service.handle(new GetProductByIdQuery("kit-1")).orElseThrow());
        assertSame(categoryList, service.handle(new GetCategoriesQuery()));
        assertSame(kitList, service.handle(new GetEmergencyKitsQuery()));
        assertSame(couponList, service.handle(new GetCouponsQuery()));
        assertSame(paymentList, service.handle(new GetPaymentMethodsQuery()));
    }

    @Test
    @DisplayName("user scoped queries should filter by username (AAA)")
    void userQueries_FilterByUsername() {
        // Arrange
        var cart = List.of(mock(CartItem.class));
        var orderList = List.of(mock(Order.class));
        var addressList = List.of(mock(ShippingAddress.class));
        var redeemed = List.of(mock(RedeemedCoupon.class));
        when(carts.findByUsername("ana")).thenReturn(cart);
        when(orders.findByUsername("ana")).thenReturn(orderList);
        when(addresses.findByUsername("ana")).thenReturn(addressList);
        when(redeemedCoupons.findByUsername("ana")).thenReturn(redeemed);

        // Act + Assert
        assertSame(cart, service.handle(new GetCartByUsernameQuery("ana")));
        assertSame(orderList, service.handle(new GetOrdersByUsernameQuery("ana")));
        assertSame(addressList, service.handle(new GetShippingAddressesQuery("ana")));
        assertSame(redeemed, service.handle(new GetRedeemedCouponsByUsernameQuery("ana")));
    }

    @Test
    @DisplayName("recommendations should fall back to the global list when the user has none (AAA)")
    void recommendations_FallBackToGlobalList() {
        // Arrange
        var personal = List.of(mock(ProductRecommendation.class));
        var global = List.of(mock(ProductRecommendation.class), mock(ProductRecommendation.class));
        when(recommendations.findByUsername("ana")).thenReturn(personal);
        when(recommendations.findByUsername("new")).thenReturn(List.of());
        when(recommendations.findAll()).thenReturn(global);

        // Act
        var forAna = service.handle(new GetRecommendationsQuery("ana"));
        var forNew = service.handle(new GetRecommendationsQuery("new"));

        // Assert
        assertSame(personal, forAna);
        assertEquals(2, forNew.size());
        assertTrue(forNew == global);
    }
}
