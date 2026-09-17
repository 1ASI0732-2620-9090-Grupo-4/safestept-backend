package com.safestep.platform.commerce.application.commandservices;

import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
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
import com.safestep.platform.commerce.domain.model.commands.UpdateCartItemCommand;
import com.safestep.platform.commerce.domain.model.commands.RedeemCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateProductCommand;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeCheckoutSession;
import com.safestep.platform.shared.application.result.*;

public interface CommerceCommandService {
    Result<CartItem, ApplicationError> handle(AddCartItemCommand command);

    Result<CartItem, ApplicationError> handle(UpdateCartItemCommand command);

    void deleteCartItem(String username, String itemId);

    Result<Order, ApplicationError> handle(CreateOrderCommand command);

    Result<StripeCheckoutSession, ApplicationError> handle(CreateStripeCheckoutSessionCommand command);

    Result<Order, ApplicationError> handle(ConfirmStripePaymentCommand command);

    Result<Order, ApplicationError> handle(CancelStripePaymentCommand command);

    Result<String, ApplicationError> handle(CaptureStripeWebhookCommand command);

    Result<Product, ApplicationError> handle(CreateProductCommand command);

    Result<Product, ApplicationError> handle(UpdateProductCommand command);

    Result<String, ApplicationError> handle(DeleteProductCommand command);

    Result<Coupon, ApplicationError> handle(CreateCouponCommand command);

    Result<Coupon, ApplicationError> handle(UpdateCouponCommand command);

    Result<String, ApplicationError> handle(DeleteCouponCommand command);

    Result<RedeemedCoupon, ApplicationError> handle(RedeemCouponCommand command);
}
