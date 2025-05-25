package ma.emsi.charitywebapp.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StripeService {
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public String createPayment(double amount, String currency) {
        Stripe.apiKey = stripeApiKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount((long) (amount * 100))
            .setCurrency(currency)
            .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getClientSecret();
        } catch (StripeException e) {
            throw new RuntimeException("Erreur lors de la création du paiement", e);
        }
    }

    public Session createPaymentSession(Double montant, String email, Long donId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        SessionCreateParams params = SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setCustomerEmail(email)
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl("http://localhost:8080/payment-success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl("http://localhost:8080/campaigns")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount((long) (montant * 100))
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Donation #" + donId)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .putMetadata("donId", donId.toString())
            .build();

        return Session.create(params);
    }
}