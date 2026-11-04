import java.math.BigDecimal;

public interface PaymentStrategy {

    PaymentResult processPayment(
        BigDecimal amount,
        String idempotencyKey
    );

    PaymentResult checkPaymentStatus(
        String idempotencyKey
    );
}