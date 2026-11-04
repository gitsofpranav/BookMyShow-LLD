import java.util.List;

public class PaymentRecoveryService {

    private PaymentRepository paymentRepository;

    public void recoverPendingPayments() {

        List<Payment> pendingPayments =
            paymentRepository.FIndingPendingPayments();

        for (Payment payment : pendingPayments) {

            PaymentResult result =
                payment.getStrategy().checkPaymentStatus(
                    payment.getIdempotencyKey()
                );

            if (result.isSuccessful()) {

                payment.markSuccess(result.getTransactionId());

                paymentRepository.update(payment);

                // Confirm booking using a transactional
                // booking confirmation operation.

            } else if (result.isFailed()) {

                payment.markFailed();

                paymentRepository.update(payment);

                // Release seats if still owned by this booking.
            }
        }
    }
}