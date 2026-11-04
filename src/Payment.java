import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private String paymentId;
    private Booking booking;

    private BigDecimal amount;
    private PaymentStatus status;

    private String transactionId;
    private String idempotencyKey;

    private LocalDateTime paymentTime;

    public Payment(
            String paymentId,
            Booking booking,
            BigDecimal amount,
            String idempotencyKey) {

        this.paymentId = paymentId;
        this.booking = booking;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;

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
}