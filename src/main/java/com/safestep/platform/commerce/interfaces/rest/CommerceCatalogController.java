package com.safestep.platform.commerce.interfaces.rest;

import com.safestep.platform.commerce.application.commandservices.CommerceCommandService;
import com.safestep.platform.commerce.application.queryservices.CommerceQueryService;
import com.safestep.platform.commerce.domain.model.commands.CreateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateProductCommand;
import com.safestep.platform.commerce.domain.model.commands.DeleteCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.DeleteProductCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateProductCommand;
import com.safestep.platform.commerce.domain.model.queries.*;
import com.safestep.platform.commerce.interfaces.rest.resources.CouponResource;
import com.safestep.platform.commerce.interfaces.rest.resources.ProductResource;
import com.safestep.platform.commerce.interfaces.rest.transform.CommerceResourceAssembler;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.safestep.platform.shared.application.services.CurrentUserService;
import com.safestep.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.safestep.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/commerce", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Commerce Catalog", description = "Products, categories, kits and commerce recommendation endpoints")
public class CommerceCatalogController {

    private final CommerceQueryService commerceQueryService;
    private final CommerceCommandService commerceCommandService;
    private final CurrentUserService currentUserService;

    public CommerceCatalogController(CommerceQueryService commerceQueryService,
            CommerceCommandService commerceCommandService, CurrentUserService currentUserService) {
        this.commerceQueryService = commerceQueryService;
        this.commerceCommandService = commerceCommandService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/products")
    @Operation(summary = "Get all store products")
    public ResponseEntity<?> getProducts() {
        return ResponseEntity
                .ok(commerceQueryService.handle(new GetProductsQuery()).stream()
                        .map(CommerceResourceAssembler::toResource).toList());
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get store product by identifier")
    public ResponseEntity<?> getProductById(@PathVariable String productId) {
        return commerceQueryService.handle(new GetProductByIdQuery(productId))
                .map(CommerceResourceAssembler::toResource)
                .<ResponseEntity<?>> map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/products")
    @Operation(summary = "Create store product")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductResource payload) {
        var result = commerceCommandService
                .handle(new CreateProductCommand(CommerceResourceAssembler.toProduct(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @PutMapping("/products/{productId}")
    @Operation(summary = "Update store product")
    public ResponseEntity<?> updateProduct(@PathVariable String productId,
            @Valid @RequestBody ProductResource payload) {
        var result = commerceCommandService
                .handle(new UpdateProductCommand(productId, CommerceResourceAssembler.toProduct(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.OK);
    }

    @DeleteMapping({ "/products/{productId}", "/products/" })
    @Operation(summary = "Delete store product")
    public ResponseEntity<?> deleteProduct(@PathVariable(required = false) String productId) {
        return noContentFromResult(commerceCommandService.handle(new DeleteProductCommand(productId == null ? "" : productId)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get product categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(commerceQueryService.handle(new GetCategoriesQuery()));
    }

    @GetMapping("/kits")
    @Operation(summary = "Get emergency kits")
    public ResponseEntity<?> getEmergencyKits() {
        return ResponseEntity.ok(commerceQueryService.handle(new GetEmergencyKitsQuery()));
    }

    @GetMapping("/coupons")
    @Operation(summary = "Get redeemable coupons")
    public ResponseEntity<?> getCoupons() {
        return ResponseEntity.ok(commerceQueryService.handle(new GetCouponsQuery()).stream()
                .map(CommerceResourceAssembler::toResource).toList());
    }

    @PostMapping("/coupons")
    @Operation(summary = "Create redeemable coupon")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CouponResource payload) {
        var result = commerceCommandService
                .handle(new CreateCouponCommand(CommerceResourceAssembler.toCoupon(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @PutMapping("/coupons/{couponId}")
    @Operation(summary = "Update redeemable coupon")
    public ResponseEntity<?> updateCoupon(@PathVariable String couponId, @Valid @RequestBody CouponResource payload) {
        var result = commerceCommandService
                .handle(new UpdateCouponCommand(couponId, CommerceResourceAssembler.toCoupon(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, CommerceResourceAssembler::toResource,
                HttpStatus.OK);
    }

    @DeleteMapping({ "/coupons/{couponId}", "/coupons/" })
    @Operation(summary = "Delete redeemable coupon")
    public ResponseEntity<?> deleteCoupon(@PathVariable(required = false) String couponId) {
        return noContentFromResult(commerceCommandService.handle(new DeleteCouponCommand(couponId == null ? "" : couponId)));
    }

    @GetMapping("/recommendations/me")
    @Operation(summary = "Get current user personalized product recommendations")
    public ResponseEntity<?> getMyRecommendations() {
        return ResponseEntity.ok(commerceQueryService.handle(new GetRecommendationsQuery(currentUserService.username())));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get product reviews")
    public ResponseEntity<?> getReviews() {
        return ResponseEntity.ok(List.of());
    }

    private ResponseEntity<?> noContentFromResult(Result<String, ApplicationError> result) {
        return switch (result) {
            case Result.Success<String, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<String, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
