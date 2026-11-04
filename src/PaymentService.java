import java.util.UUID;
import java.util.Optional;

public class PaymentService {

    private PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(
            Booking booking,
            PaymentStrategy strategy) {

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
                key
        );

        // Step 3: Persist before gateway call
        paymentRepository.save(payment);

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
}