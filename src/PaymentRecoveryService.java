import java.util.List;

public class PaymentRecoveryService {

    private final PaymentService paymentService;
    private BookingService bookingService;

    public PaymentRecoveryService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public PaymentRecoveryService(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    public void setBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public void recoverPendingPayments() {

        List<Payment> pendingPayments =
                paymentService.findPendingPayments();

        for (Payment payment : pendingPayments) {

            PaymentStatus status =
                    paymentService.reconcilePayment(payment);

            if (bookingService != null && payment.getBooking() != null) {
                bookingService.handleRecoveredPayment(
                        payment.getBooking().getBookingId(),
                        status
                );
            }

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
