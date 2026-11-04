import java.util.List;

public class PaymentRecoveryService {

    private PaymentRepository paymentRepository;

    public PaymentRecoveryService() {
    }

    public PaymentRecoveryService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void recoverPendingPayments() {

        List<Payment> pendingPayments =
            paymentRepository.findPendingPayments();

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