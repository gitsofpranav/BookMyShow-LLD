import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking {

    private String bookingId;
    private User user;
    private Show show;
    private List<ShowSeat> seats;

    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Booking(
            String bookingId,
            User user,
            Show show,
            List<ShowSeat> seats) {

        this.bookingId = bookingId;
        this.user = user;
        this.show = show;
        this.seats = new ArrayList<>(seats);

        this.totalAmount = calculateTotalAmount();
        this.status = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    private BigDecimal calculateTotalAmount() {

        BigDecimal amount = BigDecimal.ZERO;

        for (ShowSeat seat : seats) {
            amount = amount.add(seat.getPrice());
        }

        return amount;
    }

    public void confirmBooking() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancelBooking() {
        this.status = BookingStatus.CANCELLED;
    }

    public void failBooking() {
        this.status = BookingStatus.FAILED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public User getUser() {
        return user;
    }

    public Show getShow() {
        return show;
    }

    public List<ShowSeat> getSeats() {
        return new ArrayList<>(seats);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}