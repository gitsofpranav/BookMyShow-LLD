import java.math.BigDecimal;

public class UpiPayment implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(
            BigDecimal amount,
            String idempotencyKey) {

        System.out.println("Processing UPI payment: " + amount);

        // Integrate UPI payment gateway here

        return new PaymentResult(
            true,
            false,
            "UPI_TXN_101"
        );
    }

    @Override
    public PaymentResult checkPaymentStatus(String key) {
        // Check gateway transaction status
        return new PaymentResult(true, false, "UPI_TXN_101");
    }
}