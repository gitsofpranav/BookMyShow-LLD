import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BookingService {

    private static final int LOCK_TIMEOUT_SECONDS = 300;

    private final Map<String, Booking> bookings;
    private final PaymentService paymentService;

    public BookingService(PaymentService paymentService) {
        this.paymentService = paymentService;
        this.bookings = new ConcurrentHashMap<>();
    }

    // 1. Create booking and process payment
    public Booking createBooking(
            User user,
            Show show,
            List<ShowSeat> selectedSeats,
            PaymentMethod paymentMethod) {

        validateRequest(user, show, selectedSeats);

        String bookingId = UUID.randomUUID().toString();

        List<ShowSeat> lockedSeats = new ArrayList<>();

        // 2. Lock all selected seats
        for (ShowSeat showSeat : selectedSeats) {

            if (showSeat.getShow() != show) {
                releaseLockedSeats(user, lockedSeats);

                throw new IllegalArgumentException(
                    "Selected seat does not belong to this show"
                );
            }

            boolean locked = showSeat.lockSeat(
                    user,
                    LOCK_TIMEOUT_SECONDS
            );

            if (!locked) {
                releaseLockedSeats(user, lockedSeats);

                throw new IllegalStateException(
                    "One or more seats are already unavailable"
                );
            }

            lockedSeats.add(showSeat);
        }

        // 3. Create PENDING booking
        Booking booking = new Booking(
                bookingId,
                user,
                show,
                new ArrayList<>(lockedSeats)
        );

        bookings.put(bookingId, booking);

        // 4. Process payment
        Payment payment = paymentService.processPayment(
                booking,
                paymentMethod
        );
        // 5. Handle payment result
        switch (payment.getStatus()) {

            case SUCCESS:
                confirmBooking(booking);
                break;

            case FAILED:
                failBooking(booking);
                break;

            case PENDING:
                // Keep seats locked until payment recovery.
                break;
        }

        return booking;
    }

    // 6. Confirm booking after successful payment
    private synchronized void confirmBooking(Booking booking) {

        List<ShowSeat> bookedSeats = new ArrayList<>();

        for (ShowSeat showSeat : booking.getSeats()) {

            boolean booked = showSeat.book(
                    booking.getUser(),
                    booking.getBookingId()
            );

            if (!booked) {

                // Roll back already-booked seats
                for (ShowSeat bookedSeat : bookedSeats) {
                    bookedSeat.markAvailableAfterCancellation(
                            booking.getBookingId()
                    );
                }

                // Release remaining locks
                releaseLockedSeats(
                        booking.getUser(),
                        booking.getSeats()
                );

                booking.failBooking();

                throw new IllegalStateException(
                    "Unable to confirm all seats"
                );
            }

            bookedSeats.add(showSeat);
        }

        booking.confirmBooking();
    }

    // 7. Handle failed payment
    private void failBooking(Booking booking) {

        releaseLockedSeats(
                booking.getUser(),
                booking.getSeats()
        );

        booking.failBooking();
    }

    // 8. Release locked seats
    private void releaseLockedSeats(
            User user,
            List<ShowSeat> seats) {

        for (ShowSeat seat : seats) {
            seat.release(user);
        }
    }

    // 9. Validate booking request
    private void validateRequest(
            User user,
            Show show,
            List<ShowSeat> seats) {

        if (user == null || show == null) {
            throw new IllegalArgumentException(
                "User and show are required"
            );
        }

        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one seat must be selected"
            );
        }

        Set<String> seatIds = new HashSet<>();

        for (ShowSeat seat : seats) {

            if (seat == null ||
                    !seatIds.add(seat.getShowSeatId())) {

                throw new IllegalArgumentException(
                    "Invalid or duplicate seat selection"
                );
            }
        }
    }

    // 10. Retrieve booking
    public Booking getBooking(String bookingId) {

        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            throw new IllegalArgumentException(
                "Booking not found"
            );
        }

        return booking;
    }

    // 11. Handle payment status after recovery
    public synchronized void handleRecoveredPayment(
            String bookingId,
            PaymentStatus paymentStatus) {

        Booking booking = getBooking(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }

        switch (paymentStatus) {

            case SUCCESS:
                confirmBooking(booking);
                break;

            case FAILED:
                failBooking(booking);
                break;

            case PENDING:
                // Wait for next reconciliation.
                break;
        }
    }

    // 12. Cancel booking
    public boolean cancelBooking(String bookingId) {

        Booking booking = bookings.get(bookingId);

        if (booking == null ||
                booking.getStatus() != BookingStatus.CONFIRMED) {
            return false;
        }

        Payment payment = paymentService.getSuccessfulPayment(bookingId);

        if (payment == null) {
            return false;
        }

        boolean refundAccepted = paymentService.initiateRefund(payment);

        if (!refundAccepted) {
            return false;
        }

        for (ShowSeat seat : booking.getSeats()) {
            seat.markAvailableAfterCancellation(booking.getBookingId());
        }

        booking.cancelBooking();

        return true;
    }
}