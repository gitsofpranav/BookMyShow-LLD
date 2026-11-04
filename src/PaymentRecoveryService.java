import java.util.List;

public class PaymentRecoveryService {

    private final PaymentService paymentService;

    public PaymentRecoveryService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void recoverPendingPayments() {

        List<Payment> pendingPayments =
                paymentService.findPendingPayments();

        for (Payment payment : pendingPayments) {

            PaymentStatus status =
                    paymentService.reconcilePayment(payment);

            switch (status) {

                case SUCCESS:
                    System.out.println(
                        "Payment recovered successfully: "
                        + payment.getPaymentId()
                    );
                    break;

                case FAILED:
                    System.out.println(
                        "Payment failed: "
                        + payment.getPaymentId()
                    );
                    break;

                case PENDING:
                    System.out.println(
                        "Payment still pending: "
                        + payment.getPaymentId()
                    );
                    break;

                default:
                    break;
            }
        }
    }
}
