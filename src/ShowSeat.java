import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowSeat {

    private String showSeatId;
    private Show show;
    private Seat seat;
    private SeatStatus status;
    private User lockedBy;
    private LocalDateTime lockExpiry;
    private BigDecimal price;
    private String bookedByBookingId;

    public ShowSeat(
            String showSeatId,
            Show show,
            Seat seat,
            BigDecimal price) {

        this.showSeatId = showSeatId;
        this.show = show;
        this.seat = seat;
        this.price = price;

        this.status = SeatStatus.AVAILABLE;
    }

    // Lock seat temporarily
    public synchronized boolean lockSeat(
            User user,
            int timeoutSeconds) {

        if (status == SeatStatus.LOCKED &&
                !LocalDateTime.now().isBefore(lockExpiry)) {

            releaseExpiredLock();
        }

        if (status != SeatStatus.AVAILABLE) {
            return false;
        }

        status = SeatStatus.LOCKED;
        lockedBy = user;
        lockExpiry = LocalDateTime.now()
                .plusSeconds(timeoutSeconds);

        return true;
    }

    // Release temporary lock
    public synchronized boolean release(User user) {

        if (status != SeatStatus.LOCKED ||
                lockedBy == null ||
                !lockedBy.getUserId().equals(user.getUserId())) {
            return false;
        }

        status = SeatStatus.AVAILABLE;
        lockedBy = null;
        lockExpiry = null;

        return true;
    }

    // Confirm seat booking
    public synchronized boolean book(
            User user,
            String bookingId) {

        if (status != SeatStatus.LOCKED ||
                lockedBy == null ||
                !lockedBy.getUserId().equals(user.getUserId())) {
            return false;
        }

        if (!LocalDateTime.now().isBefore(lockExpiry)) {
            releaseExpiredLock();
            return false;
        }

        status = SeatStatus.BOOKED;
        bookedByBookingId = bookingId;

        lockedBy = null;
        lockExpiry = null;

        return true;
    }

    public synchronized boolean book(User user) {
        return book(user, null);
    }

    // Release expired lock
    private void releaseExpiredLock() {

        status = SeatStatus.AVAILABLE;
        lockedBy = null;
        lockExpiry = null;
    }

    // Release booked seat after successful cancellation/refund initiation
    public synchronized boolean markAvailableAfterCancellation(
            String bookingId) {

        if (status != SeatStatus.BOOKED ||
                bookedByBookingId == null ||
                !bookedByBookingId.equals(bookingId)) {
            return false;
        }

        status = SeatStatus.AVAILABLE;
        bookedByBookingId = null;

        return true;
    }

    public synchronized boolean markAvailableAfterCancellation(Booking booking) {
        return booking != null && markAvailableAfterCancellation(booking.getBookingId());
    }

    public String getShowSeatId() {
        return showSeatId;
    }

    public Show getShow() {
        return show;
    }

    public Seat getSeat() {
        return seat;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getBookedByBookingId() {
        return bookedByBookingId;
    }
}