import java.math.BigDecimal;

public class CardPayment implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(
            BigDecimal amount,
            String idempotencyKey) {

        System.out.println("Processing Card payment: " + amount);

        return new PaymentResult(
            true,
            false,
            "CARD_TXN_101"
        );
    }

    @Override
    public PaymentResult checkPaymentStatus(String key) {
        return new PaymentResult(true, false, "CARD_TXN_101");
    }
}