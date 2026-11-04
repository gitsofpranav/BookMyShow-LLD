import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    void save(Payment payment);

    void update(Payment payment);

    Optional<Payment> findById(String paymentId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findSuccessfulPaymentByBookingId(String bookingId);

    List<Payment> findPendingPayments();
}