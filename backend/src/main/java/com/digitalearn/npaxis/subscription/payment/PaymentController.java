package com.digitalearn.npaxis.subscription.payment;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.subscription.CheckoutSessionRequest;
import com.digitalearn.npaxis.subscription.CheckoutSessionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;
import static com.digitalearn.npaxis.utils.APIConstants.CREATE_CHECKOUT_SESSION_API;
import static com.digitalearn.npaxis.utils.APIConstants.PAYMENTS_API;

@RestController
@RequestMapping(BASE_API + "/" + PAYMENTS_API)
@RequiredArgsConstructor
public class PaymentController {

    // Injecting the Interface, not the concrete Stripe implementation
    private final PaymentGatewayService paymentGatewayService;

    @PostMapping(CREATE_CHECKOUT_SESSION_API)
    @PreAuthorize("hasRole('PRECEPTOR') and #request.preceptorId == principal.userId")
    public ResponseEntity<GenericApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @Valid @RequestBody CheckoutSessionRequest request) {

        CheckoutSessionResponse response = paymentGatewayService.createCheckoutSession(request);
        return ResponseHandler.generateResponse(response, "Checkout session created successfully", true, HttpStatus.OK);
    }
}