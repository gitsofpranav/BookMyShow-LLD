import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> payments;
    private final Map<String, String> idempotencyIndex;

    public InMemoryPaymentRepository() {
        this.payments = new ConcurrentHashMap<>();
        this.idempotencyIndex = new ConcurrentHashMap<>();
    }

    @Override
    public void save(Payment payment) {

        String paymentId = payment.getPaymentId();
        String idempotencyKey = payment.getIdempotencyKey();

        if (idempotencyIndex.containsKey(idempotencyKey)) {
            throw new IllegalStateException(
                "Payment already exists for this idempotency key"
            );
        }

        Payment existing = payments.putIfAbsent(paymentId, payment);

        if (existing != null) {
            throw new IllegalStateException("Payment ID already exists");
        }

        idempotencyIndex.put(idempotencyKey, paymentId);
    }

    @Override
    public void update(Payment payment) {

        if (!payments.containsKey(payment.getPaymentId())) {
            throw new IllegalArgumentException("Payment not found");
        }

        payments.put(payment.getPaymentId(), payment);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String key) {

        String paymentId = idempotencyIndex.get(key);

        if (paymentId == null) {
            return Optional.empty();
        }

        return findById(paymentId);
    }

    @Override
    public Optional<Payment> findSuccessfulPaymentByBookingId(
            String bookingId) {

        return payments.values()
                .stream()
                .filter(payment ->
                    payment.getBooking().getBookingId().equals(bookingId)
                )
                .filter(payment ->
                    payment.getStatus() == PaymentStatus.SUCCESS
                )
                .findFirst();
    }

    @Override
    public List<Payment> findPendingPayments() {

        return payments.values()
                .stream()
                .filter(payment ->
                    payment.getStatus() == PaymentStatus.PENDING
                )
                .toList();
    }
}