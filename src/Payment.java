import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private String paymentId;
    private Booking booking;

    private BigDecimal amount;
    private PaymentStatus status;

    private String transactionId;
    private String idempotencyKey;
    private PaymentStrategy strategy;

    private LocalDateTime paymentTime;

    public Payment(
            String paymentId,
            Booking booking,
            BigDecimal amount,
            String idempotencyKey) {

        this(paymentId, booking, amount, idempotencyKey, null);
    }

    public Payment(
            String paymentId,
            Booking booking,
            BigDecimal amount,
            String idempotencyKey,
            PaymentStrategy strategy) {

        this.paymentId = paymentId;
        this.booking = booking;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.strategy = strategy;

        this.status = PaymentStatus.PENDING;
    }

    public void markSuccess(String transactionId) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
        this.paymentTime = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void updatePayment(PaymentStatus status, String transactionId) {
        if (status == PaymentStatus.SUCCESS) {
            markSuccess(transactionId);
        } else if (status == PaymentStatus.FAILED) {
            this.transactionId = transactionId;
            markFailed();
        } else if (status == PaymentStatus.REFUNDED) {
            markRefunded();
        }
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Booking getBooking() {
        return booking;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentStrategy getStrategy() {
        return strategy;
    }

    public PaymentStrategy getMethod() {
        return strategy;
    }
}