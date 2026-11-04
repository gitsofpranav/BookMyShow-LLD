import java.util.List;
import java.util.UUID;
import java.util.Optional;

public class PaymentService {

    private PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(
            Booking booking,
            PaymentMethod method) {

        String key = booking.getBookingId();

        // Step 1: Check existing payment
        Optional<Payment> existing =
                paymentRepository.findByIdempotencyKey(key);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Step 2: Create payment record
        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                booking,
                booking.getTotalAmount(),
                key,
                method
        );

        // Step 3: Persist before gateway call
        paymentRepository.save(payment);

        PaymentStrategy strategy = getStrategy(method);

        // Step 4: Process payment
        PaymentResult result = strategy.processPayment(
                payment.getAmount(),
                key
        );

        // Step 5: Update payment status
        if (result.isSuccessful()) {

            payment.markSuccess(result.getTransactionId());

        } else if (result.isFailed()) {

            payment.markFailed();

        }

        paymentRepository.update(payment);

        return payment;
    }

    public Payment processPayment(
            Booking booking,
            PaymentStrategy strategy) {

        PaymentMethod method = PaymentMethod.CARD;
        if (strategy instanceof UpiPayment) {
            method = PaymentMethod.UPI;
        } else if (strategy instanceof WalletPayment) {
            method = PaymentMethod.WALLET;
        }

        return processPayment(booking, method);
    }

    public Payment getSuccessfulPayment(String bookingId) {
        return paymentRepository.findSuccessfulPaymentByBookingId(bookingId).orElse(null);
    }

    public boolean initiateRefund(Payment payment) {
        if (payment == null || payment.getStatus() != PaymentStatus.SUCCESS) {
            return false;
        }

        payment.markRefunded();
        paymentRepository.update(payment);
        return true;
    }

    public List<Payment> findPendingPayments() {
        return paymentRepository.findPendingPayments();
    }

    public PaymentStatus reconcilePayment(Payment payment) {

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment.getStatus();
        }

        try {
            PaymentStrategy strategy =
                    getStrategy(payment.getMethod());

            PaymentResult result = strategy.checkPaymentStatus(
                    payment.getIdempotencyKey()
            );

            payment.updatePayment(
                    result.getStatus(),
                    result.getTransactionId()
            );

            paymentRepository.update(payment);

            return payment.getStatus();

        } catch (Exception exception) {

            // Gateway status is still unknown.
            // Keep payment PENDING for next recovery attempt.
            return PaymentStatus.PENDING;
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        switch (method) {
            case CARD:
                return new CardPayment();
            case UPI:
                return new UpiPayment();
            case WALLET:
                return new WalletPayment();
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
    }

}