package com.safestep.platform.commerce.interfaces.rest;

import com.safestep.platform.commerce.application.commandservices.CommerceCommandService;
import com.safestep.platform.commerce.application.queryservices.CommerceQueryService;
import com.safestep.platform.commerce.domain.model.commands.AddCartItemCommand;
import com.safestep.platform.commerce.domain.model.commands.CaptureStripeWebhookCommand;
import com.safestep.platform.commerce.domain.model.commands.CancelStripePaymentCommand;
import com.safestep.platform.commerce.domain.model.commands.ConfirmStripePaymentCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateOrderCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateStripeCheckoutSessionCommand;
import com.safestep.platform.commerce.domain.model.commands.RedeemCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCartItemCommand;
import com.safestep.platform.commerce.domain.model.queries.*;
import com.safestep.platform.commerce.interfaces.rest.resources.AddCartItemResource;
import com.safestep.platform.commerce.interfaces.rest.resources.CreateOrderResource;
import com.safestep.platform.commerce.interfaces.rest.resources.StripeCheckoutSessionResource;
import com.safestep.platform.commerce.interfaces.rest.resources.StripeWebhookResponseResource;
import com.safestep.platform.commerce.interfaces.rest.resources.UpdateCartItemResource;
import com.safestep.platform.commerce.interfaces.rest.transform.CommerceResourceAssembler;
import com.safestep.platform.shared.application.services.CurrentUserService;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.safestep.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.safestep.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/commerce", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Commerce Operations", description = "Cart, order, address and payment endpoints")
public class CommerceOperationsController {

    private final CommerceQueryService commerceQueryService;
    private final CommerceCommandService commerceCommandService;
    private final CurrentUserService currentUserService;

    public CommerceOperationsController(CommerceQueryService commerceQueryService,
            CommerceCommandService commerceCommandService, CurrentUserService currentUserService) {
        this.commerceQueryService = commerceQueryService;
        this.commerceCommandService = commerceCommandService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/cart/me")
    @Operation(summary = "Get current user cart")
    public ResponseEntity<?> getMyCart() {
        return ResponseEntity.ok(commerceQueryService.handle(new GetCartByUsernameQuery(currentUserService.username()))
                .stream().map(CommerceResourceAssembler::toResource).toList());
    }

    @PostMapping("/cart/items")
    @Operation(summary = "Add item to current user cart")
    public ResponseEntity<?> addCartItem(@Valid @RequestBody AddCartItemResource payload) {
        var result = commerceCommandService
                .handle(new AddCartItemCommand(currentUserService.username(), payload.productId(), payload.quantity()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @PutMapping("/cart/items/{itemId}")
    @Operation(summary = "Update current user cart item")
    public ResponseEntity<?> updateCartItem(@PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemResource payload) {
        var result = commerceCommandService
                .handle(new UpdateCartItemCommand(currentUserService.username(), itemId, payload.quantity()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.OK);
    }

    @DeleteMapping("/cart/items/{itemId}")
    @Operation(summary = "Delete current user cart item")
    public ResponseEntity<?> deleteCartItem(@PathVariable String itemId) {
        commerceCommandService.deleteCartItem(currentUserService.username(), itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders/me")
    @Operation(summary = "Get current user orders")
    public ResponseEntity<?> getMyOrders() {
        return ResponseEntity.ok(commerceQueryService
                .handle(new GetOrdersByUsernameQuery(currentUserService.username())).stream()
                .map(CommerceResourceAssembler::toResource).toList());
    }

    @PostMapping("/orders")
    @Operation(summary = "Create current user order")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderResource payload) {
        var result = commerceCommandService.handle(new CreateOrderCommand(currentUserService.username(),
                payload.status(), payload.redeemedCouponExternalId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @PostMapping("/coupons/{couponId}/redeem")
    @Operation(summary = "Redeem a coupon using current user's SafeCoins")
    public ResponseEntity<?> redeemCoupon(@PathVariable String couponId) {
        var result = commerceCommandService.handle(new RedeemCouponCommand(currentUserService.username(), couponId));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @GetMapping("/coupons/redeemed/me")
    @Operation(summary = "Get current user's redeemed coupons")
    public ResponseEntity<?> getMyRedeemedCoupons() {
        return ResponseEntity.ok(commerceQueryService
                .handle(new GetRedeemedCouponsByUsernameQuery(currentUserService.username())).stream()
                .map(CommerceResourceAssembler::toResource).toList());
    }

    @GetMapping("/shipping-addresses/me")
    @Operation(summary = "Get current user shipping addresses")
    public ResponseEntity<?> getMyShippingAddresses() {
        return ResponseEntity.ok(commerceQueryService.handle(new GetShippingAddressesQuery(currentUserService.username())));
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Get available payment methods")
    public ResponseEntity<?> getPaymentMethods() {
        return ResponseEntity.ok(commerceQueryService.handle(new GetPaymentMethodsQuery()));
    }

    @PostMapping("/orders/{orderId}/payments/stripe-checkout")
    @Operation(summary = "Create Stripe Checkout session for an order")
    public ResponseEntity<?> createStripeCheckoutSession(@PathVariable String orderId) {
        var result = commerceCommandService
                .handle(new CreateStripeCheckoutSessionCommand(currentUserService.username(), orderId));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                session -> new StripeCheckoutSessionResource(session.sessionUrl(), session.sessionId()), HttpStatus.OK);
    }

    @PostMapping("/orders/{orderId}/payments/stripe-confirm")
    @Operation(summary = "Confirm Stripe Checkout payment for an order")
    public ResponseEntity<?> confirmStripePayment(@PathVariable String orderId, @RequestParam("sessionId") String sessionId) {
        var result = commerceCommandService
                .handle(new ConfirmStripePaymentCommand(currentUserService.username(), orderId, sessionId));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.OK);
    }

    @PostMapping("/orders/{orderId}/payments/stripe-cancel")
    @Operation(summary = "Cancel Stripe Checkout payment for an order")
    public ResponseEntity<?> cancelStripePayment(@PathVariable String orderId,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        var result = commerceCommandService
                .handle(new CancelStripePaymentCommand(currentUserService.username(), orderId, sessionId));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.OK);
    }

    @PostMapping(value = "/payments/stripe/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Capture Stripe payment webhook")
    public ResponseEntity<?> captureStripeWebhook(@RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        var result = commerceCommandService.handle(new CaptureStripeWebhookCommand(payload, signature));
        return switch (result) {
            case Result.Success<String, ApplicationError> success when "Event ignored".equals(success.value()) ->
                    ResponseEntity.noContent().build();
            case Result.Success<String, ApplicationError> success ->
                    ResponseEntity.ok(new StripeWebhookResponseResource(success.value()));
            case Result.Failure<String, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
