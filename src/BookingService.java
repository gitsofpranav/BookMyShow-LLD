import java.util.*;
import java.math.BigDecimal;

public class BookingService {

    private Map<String, Booking> bookings;

    private PaymentService paymentService;

    public BookingService(PaymentService paymentService) {
        this.bookings = new HashMap<>();
        this.paymentService = paymentService;
    }

    public Booking createBooking(
            User user,
            Show show,
            List<ShowSeat> seats,
            PaymentStrategy strategy) {

        // 1. Validate input
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one seat must be selected"
            );
        }

        // 2. Validate show and selected seats
        for (ShowSeat seat : seats) {

            if (!seat.getShow().getshowId()
                    .equals(show.getshowId())) {
                throw new IllegalArgumentException(
                    "Seat does not belong to this show"
                );
            }
        }

        // 3. Lock all seats
        List<ShowSeat> lockedSeats = new ArrayList<>();

        for (ShowSeat seat : seats) {

            if (!seat.lockSeat(user, 300)) {

                // Release previously locked seats
                for (ShowSeat locked : lockedSeats) {
                    locked.release(user);
                }

                return null;
            }

            lockedSeats.add(seat);
        }

        // 4. Create booking
        Booking booking = new Booking(
            UUID.randomUUID().toString(),
            user,
            show,
            seats
        );

        bookings.put(booking.getBookingId(), booking);

        // 5. Process payment
        Payment payment = paymentService.processPayment(
            booking,
            strategy
        );

        // 6. Handle payment result
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            boolean allBooked = true;

            for (ShowSeat seat : lockedSeats) {

                if (!seat.book(user, booking.getBookingId())) {
                    allBooked = false;
                    break;
                }
            }

            if (allBooked) {
                booking.confirmBooking();
            } else {
                booking.failBooking();

                // Compensation: refund successful payment.
                // Release any remaining locks owned by this user.
            }

        } else if (payment.getStatus() == PaymentStatus.FAILED) {

            booking.failBooking();

            for (ShowSeat seat : lockedSeats) {
                seat.release(user);
            }

        }

        return booking;
    }

    public boolean cancelBooking(String bookingId) {

    Booking booking = bookings.get(bookingId);

    if (booking == null ||
        booking.getStatus() != BookingStatus.CONFIRMED) {
        return false;
    }

    // Check cancellation policy

    Payment payment = paymentService
            .getSuccessfulPayment(bookingId);

    if (payment == null) {
        return false;
    }

    boolean refundAccepted =
            paymentService.initiateRefund(payment);

    if (!refundAccepted) {
        return false;
    }

    // Release seats after refund request is accepted
    for (ShowSeat seat : booking.getSeats()) {
        seat.markAvailableAfterCancellation(booking.getBookingId());
    }

    booking.cancelBooking();

    return true;
}

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }



}