import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findByIdempotencyKey(String key);

    void save(Payment payment);

    void update(Payment payment);
}