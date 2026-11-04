# BookMyShow - Low-Level Design (LLD)

A robust, thread-safe Low-Level Design (LLD) implementation of a movie ticket booking platform like **BookMyShow** in Java.

This project focuses on real-world engineering concerns:
- **Concurrent seat locking** with timeout expiration to prevent double-booking.
- **Idempotent payment processing** across multiple payment gateways (Strategy Pattern).
- **Payment recovery & reconciliation** for asynchronous timeouts and network failures.
- **Transactional seat rollback & cancellation handling**.

---

## 🏗️ Architecture & Core Components

```
                +---------------------+
                |    BookingService   |<-----------------------+
                +----------+----------+                        |
                           |                                   |
         +-----------------+-----------------+                 |
         |                                   |                 |
         v                                   v                 |
+-----------------+                 +------------------+       |
|    ShowSeat     |                 |  PaymentService  |       |
| (Lock & Expiry) |                 +--------+---------+       |
+-----------------+                          |                 |
                                    +--------+--------+        |
                                    |                 |        |
                                    v                 v        |
                         +--------------------+ +-----------+  |
                         | PaymentRepository  | |  Strategy |  |
                         | (InMemory / DB)    | | (UPI/Card)|  |
                         +--------------------+ +-----------+  |
                                    ^                          |
                                    |                          |
                       +------------+-------------+            |
                       |  PaymentRecoveryService  |------------+
                       +--------------------------+
```

---

## 🔑 Key Features

### 1. Concurrent Seat Locking & Timeout Mechanism
- When a user chooses seats, they are placed in a **`LOCKED`** state with a configurable timeout (default: 300 seconds).
- Thread-safety is guaranteed via synchronized methods in `ShowSeat`.
- If a lock expires before booking completion, `releaseExpiredLock()` frees the seat for other users.
- Upon successful payment, seats transition to **`BOOKED`** and are associated with `bookedByBookingId`.

### 2. Idempotent Payment Processing
- Uses the **Strategy Pattern** for extensible payment channels:
  - `CardPayment`
  - `UpiPayment`
  - `WalletPayment`
- Payments are tracked by a unique `idempotencyKey` (bound to `bookingId`) to avoid duplicate debits.
- Payment state is persisted to the repository before invoking external payment gateways.

### 3. Payment Recovery & Reconciliation
- Handles edge cases where a payment gateway deducts money, but network timeouts prevent immediate status response.
- `PaymentRecoveryService` scans for pending transactions via `paymentService.findPendingPayments()`.
- Reconciles status with the gateway (`checkPaymentStatus`).
- On recovered `SUCCESS`, notifies `BookingService.handleRecoveredPayment(...)` to confirm seats.
- On recovered `FAILED`, unlocks and releases seats back to `AVAILABLE`.

### 4. Booking Cancellation & Refund
- Confirmed bookings can be cancelled via `BookingService.cancelBooking(bookingId)`.
- Validates successful payment, marks refund status (`PaymentStatus.REFUNDED`), and resets booked seats back to `AVAILABLE`.

---

## 📂 Project Structure

```
BookMyShow/
├── src/
│   ├── Booking.java                    # Booking domain entity & state
│   ├── BookingService.java             # Core booking, locking & orchestration logic
│   ├── BookingStatus.java              # PENDING, CONFIRMED, CANCELLED, FAILED
│   ├── CardPayment.java                # Card payment strategy implementation
│   ├── InMemoryPaymentRepository.java  # Thread-safe concurrent payment repository
│   ├── Movie.java                      # Movie metadata
│   ├── Payment.java                    # Payment entity tracking status & transaction ID
│   ├── PaymentMethod.java              # UPI, CARD, WALLET enum
│   ├── PaymentRecoveryService.java     # Background payment reconciliation engine
│   ├── PaymentRepository.java          # Repository interface for payment persistence
│   ├── PaymentResult.java              # Standardized gateway response wrapper
│   ├── PaymentService.java             # Payment orchestration, strategy resolution & refund
│   ├── PaymentStatus.java              # PENDING, SUCCESS, FAILED, REFUNDED
│   ├── PaymentStrategy.java            # Strategy interface for payment gateways
│   ├── Screen.java                     # Screen representation with seats
│   ├── Seat.java                       # Physical seat representation
│   ├── SeatStatus.java                 # AVAILABLE, LOCKED, BOOKED
│   ├── SeatType.java                   # REGULAR, PREMIUM, VIP
│   ├── Show.java                       # Show entity linking movie, screen, and timings
│   ├── ShowSeat.java                   # Show-specific seat with dynamic lock & price
│   ├── Theater.java                    # Theater domain model
│   ├── UpiPayment.java                 # UPI payment strategy implementation
│   ├── User.java                       # User entity
│   └── WalletPayment.java              # Wallet payment strategy implementation
├── .gitignore
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or **JDK 21+**

### Compile
To compile all Java source files:
```powershell
javac -d bin src/*.java
```

---

## 💡 Design Patterns Used

| Pattern | Usage |
|---|---|
| **Strategy Pattern** | `PaymentStrategy`, `CardPayment`, `UpiPayment`, `WalletPayment` allow plug-and-play payment providers. |
| **Repository Pattern** | `PaymentRepository` decouples business logic from data storage. |
| **State Pattern / Enums** | `BookingStatus`, `SeatStatus`, and `PaymentStatus` drive strict lifecycle state machines. |
| **Pessimistic / Time-bound Locking** | Managed within `ShowSeat` using synchronized concurrency guards and lock expiry tracking. |
