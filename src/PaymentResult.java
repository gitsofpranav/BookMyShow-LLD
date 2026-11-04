public class PaymentResult {

    private boolean successful;
    private boolean failed;
    private String transactionId;

    public PaymentResult(
            boolean successful,
            boolean failed,
            String transactionId) {

        this.successful = successful;
        this.failed = failed;
        this.transactionId = transactionId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getTransactionId() {
        return transactionId;
    }
}