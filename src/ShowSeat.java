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
        this.lockedBy = null;
        this.lockExpiry = null;
    }

    public synchronized boolean lockSeat(
            User user, int timeoutSeconds) {

        // Check expired lock
        if (status == SeatStatus.LOCKED &&
                LocalDateTime.now().isAfter(lockExpiry)) {

            release(lockedBy);
        }

        // Check availability
        if (status != SeatStatus.AVAILABLE) {
            return false;
        }

        status = SeatStatus.LOCKED;
        lockedBy = user;

        lockExpiry = LocalDateTime.now()
                .plusSeconds(timeoutSeconds);

        return true;
    }

    public synchronized boolean release(User user) {

        if (status != SeatStatus.LOCKED ||
                !lockedBy.getUserId().equals(user.getUserId())) {
            return false;
        }

        status = SeatStatus.AVAILABLE;
        lockedBy = null;
        lockExpiry = null;

        return true;
    }

    public synchronized boolean book(User user) {

        if (status != SeatStatus.LOCKED ||
                !lockedBy.getUserId().equals(user.getUserId())) {
            return false;
        }

        if (LocalDateTime.now().isAfter(lockExpiry)) {
            release(user);
            return false;
        }

        status = SeatStatus.BOOKED;
        lockedBy = null;
        lockExpiry = null;

        return true;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
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

    public User getLockedBy() {
        return lockedBy;
    }

    public LocalDateTime getLockExpiry() {
        return lockExpiry;
    }

    public BigDecimal getPrice() {
        return price;
    }
}