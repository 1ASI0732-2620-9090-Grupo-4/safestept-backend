package com.safestep.platform.commerce.infrastructure.persistence.jpa.seed;

import com.safestep.platform.commerce.domain.model.aggregates.*;
import com.safestep.platform.commerce.domain.model.entities.*;
import com.safestep.platform.commerce.domain.model.valueobjects.*;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.repositories.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Component
public class CommerceSeedEventHandler {
    private final ObjectMapper mapper;
    private final ProductRepository products;
    private final ShoppingCartRepository carts;
    private final OrderRepository orders;
    private final CategoryRepository categories;
    private final EmergencyKitRepository kits;
    private final CouponRepository coupons;
    private final ShippingAddressRepository addresses;
    private final PaymentMethodRepository payments;
    private final ProductRecommendationRepository recommendations;

    public CommerceSeedEventHandler(ObjectMapper m, ProductRepository p, ShoppingCartRepository c, OrderRepository o,
            CategoryRepository ca, EmergencyKitRepository k, CouponRepository co,
            ShippingAddressRepository a, PaymentMethodRepository pm,
            ProductRecommendationRepository r) {
        mapper = m;
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

    private BigDecimal money(JsonNode n, String field) {
        return n.path(field).isNumber() ? n.path(field).decimalValue() : BigDecimal.ZERO;
    }

    private String owner(JsonNode n) {
        var v = n.path("userId").asText();
        return "usr-001".equals(v) || "1".equals(v) ? "backend.check" : v;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() throws Exception {
        try (var in = new ClassPathResource("safestep-seed.json").getInputStream()) {
            var root = mapper.readTree(in);
            for (var n : root.path("products")) {
                var id = n.path("id").asText();
                if (!products.existsByExternalId(id)) {
                    var tags = new ArrayList<String>();
                    n.path("tags").forEach(t -> tags.add(t.asText()));
                    products.save(new Product(null, id, n.path("name").asText(), n.path("category").asText(),
                            n.path("type").asText(), money(n, "price"),
                            n.path("oldPrice").isNumber() ? money(n, "oldPrice") : null, n.path("rating").asDouble(),
                            n.path("stock").asInt(), n.path("imageUrl").asText(), n.path("description").asText(),
                            tags));
                }
            }
            for (var n : root.path("categories")) {
                var id = n.path("id").asText();
                if (!categories.existsByExternalId(id))
                    categories.save(new Category(null, id, n.path("name").asText(), n.path("productCount").asInt()));
            }
            for (var n : root.path("emergencyKits")) {
                var id = n.path("id").asText();
                if (!kits.existsByExternalId(id))
                    kits.save(new EmergencyKit(null, id, n.path("name").asText(), n.path("description").asText(),
                            n.path("level").asText(), money(n, "individualPrice"), money(n, "kitPrice"),
                            money(n, "savings"), n.path("savingsPercentage").asInt(), n.path("imageUrl").asText(),
                            n.path("isPopular").asBoolean()));
            }
            for (var n : root.path("coupons")) {
                var id = n.path("id").asText();
                if (!coupons.existsByExternalId(id))
                    coupons.save(new Coupon(null, id, n.path("title").asText(), n.path("costCoins").asInt(),
                            n.path("discount").asText()));
            }
            for (var n : root.path("shippingAddresses")) {
                var id = n.path("id").asText();
                if (!addresses.existsByExternalId(id))
                    addresses.save(new ShippingAddress(null, id, owner(n), n.path("label").asText(),
                            n.path("recipientName").asText(), n.path("phone").asText(), n.path("country").asText(),
                            n.path("region").asText(), n.path("city").asText(), n.path("district").asText(),
                            n.path("postalCode").asText(), n.path("street").asText(), n.path("streetNumber").asText(),
                            n.path("reference").asText(), n.path("isDefault").asBoolean()));
            }
            for (var n : root.path("paymentMethods")) {
                var id = n.path("id").asText();
                if (!payments.existsByExternalId(id))
                    payments.save(new PaymentMethod(null, id, n.path("type").asText(), n.path("label").asText(),
                            n.path("description").asText(), money(n, "processingFee"),
                            n.path("isAvailable").asBoolean()));
            }
            for (var n : root.path("personalizedRecommendations")) {
                var id = n.path("id").asText();
                if (!recommendations.existsByExternalId(id))
                    recommendations.save(new ProductRecommendation(null, id, owner(n), n.path("productId").asText(),
                            n.path("reason").asText(), n.path("relatedSimulationId").asText(),
                            n.path("priority").asText(), n.path("isDismissed").asBoolean()));
            }
            for (var n : root.path("cartItems")) {
                var id = n.path("id").asText();
                if (!carts.existsByExternalId(id))
                    carts.save(new CartItem(null, id, owner(n), n.path("productId").asText(),
                            Math.max(1, n.path("quantity").asInt()), Instant.parse(n.path("addedAt").asText())));
            }
            for (var n : root.path("orders")) {
                var id = n.path("id").asText();
                if (orders.existsByExternalId(id))
                    continue;
                var items = new ArrayList<OrderItem>();
                for (var item : n.path("items")) {
                    products.findByExternalId(item.asText())
                            .ifPresent(p -> items.add(new OrderItem(p.getExternalId(), p.getName(), p.getPrice(), 1)));
                }
                if (!items.isEmpty())
                    orders.save(new Order(null, id, owner(n), items, OrderStatus.from(n.path("status").asText()),
                            LocalDate.parse(n.path("createdAt").asText().substring(0, 10))));
            }
        }
    }
}
