import java.math.BigDecimal;

public class WalletPayment implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(
            BigDecimal amount,
            String idempotencyKey) {

        System.out.println("Processing Wallet payment: " + amount);

        return new PaymentResult(
            true,
            false,
            "WALLET_TXN_101"
        );
    }

    @Override
    public PaymentResult checkPaymentStatus(String key) {
        return new PaymentResult(true, false, "WALLET_TXN_101");
    }
}