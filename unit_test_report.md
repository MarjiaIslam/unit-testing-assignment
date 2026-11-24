# Unit Testing Report: Parking Slot Booking

## 0) Team Members

| Student ID | Name |
|---|---|
| 0112230958 | Marjia Islam |

---

## A) Test Case List

**Suite:** 5 JUnit 5 test classes, 82 test methods, 132 test executions.

### WalletTest (16 tests)

| Test ID | Class.Method | Why this test? | Verdict | Comments / Observations |
|---|---|---|---|---|
| W1 | Wallet.Wallet() | Default constructor must start at 0 | Pass | |
| W2 | Wallet.Wallet(double) | Initial balance must be stored as given | Pass | Negative initial balances are also accepted; the spec does not forbid them |
| W3 | Wallet.addFunds | Positive amount increases balance by exactly that amount | Pass | |
| W4 | Wallet.addFunds | Boundary just above 0 (0.01) is accepted | Pass | Kills `> 0` → `>= 0` / `> 1`-style boundary mutants |
| W5 | Wallet.addFunds | 0 is not positive → `InvalidAmountException`, balance unchanged | Pass | |
| W6 | Wallet.addFunds | Negative amount → `InvalidAmountException`, balance unchanged | Pass | |
| W7 | Wallet.deductFunds | Valid deduction reduces balance by exactly that amount | Pass | |
| W8 | Wallet.deductFunds | Boundary: deducting the whole balance is allowed (`>=`) | Pass | Kills the `>=` → `>` mutant |
| W9 | Wallet.deductFunds | Deducting more than balance → `InsufficientFundsException`, balance unchanged | Pass | |
| W10 | Wallet.deductFunds | 0 → `InvalidAmountException` | Pass | |
| W11 | Wallet.deductFunds | Negative → `InvalidAmountException` | Pass | |
| W12 | Wallet.transferFunds | Sender decreases and receiver increases by the same amount | Pass | |
| W13 | Wallet.transferFunds | Boundary: transferring the whole balance is allowed | Pass | |
| W14 | Wallet.transferFunds | Over-balance → `InsufficientFundsException`, neither wallet changes | Pass | Confirms no partial transfer |
| W15 | Wallet.transferFunds | 0 → `InvalidAmountException`, neither wallet changes | Pass | |
| W16 | Wallet.transferFunds | Negative → `InvalidAmountException`, neither wallet changes | Pass | |

### VehicleTest (8 tests)

| Test ID | Class.Method | Why this test? | Verdict | Comments / Observations |
|---|---|---|---|---|
| V1 | Vehicle.Vehicle(int, VehicleType, Wallet) | Id and type are stored | Pass | |
| V2 | Vehicle.getWallet | The supplied wallet is used as-is (same instance, not a copy) | Pass | |
| V3 | Vehicle.getBalance | Balance comes from the supplied wallet | Pass | |
| V4 | Vehicle.Vehicle(int, VehicleType, double) | A wallet is created with the given balance | Pass | |
| V5 | Vehicle.Vehicle(int, VehicleType, double) | Two vehicles never share a wallet | Pass | |
| V6 | Vehicle.getBalance | Later wallet changes show up in `getBalance()` | Pass | Balance is read live, not cached |
| V7 | Vehicle.getVehicleType | Every `VehicleType` value is stored and returned (parameterized, 6 cases) | Pass | |
| V8 | Vehicle.toString | Exact format `Vehicle{vehicleId=7, vehicleType=CAR, walletBalance=50.0}` | Pass | Kills "return empty string" mutant |

### ParkingSlotTest (22 tests)

| Test ID | Class.Method | Why this test? | Verdict | Comments / Observations |
|---|---|---|---|---|
| PS1 | ParkingSlot.ParkingSlot | New slot has id, type, is active, and has an empty wallet | Pass | |
| PS2 | ParkingSlot.getBookings | New slot has an empty, non-null booking list | Pass | |
| PS3 | ParkingSlot.getBalance | Balance comes from the slot's wallet | Pass | |
| PS4 | ParkingSlot.getWallet | The same wallet is returned on every call | Pass | |
| PS5 | ParkingSlot.deactivate | Slot becomes inactive | Pass | |
| PS6 | ParkingSlot.activate | Inactive slot becomes active again | Pass | |
| PS7 | ParkingSlot.isCompatible | An inactive slot is never compatible | Pass | |
| PS8 | ParkingSlot.isCompatible | A re-activated slot is compatible again | Pass | |
| PS9 | ParkingSlot.isCompatible | Full compatibility matrix, all 6 vehicle × 4 slot types (parameterized, 24 cases) | Pass | TRUCK is not in the matrix and is rejected everywhere; the `MICROCAR` case has no `break` but falls through to `return false`, so behaviour is still correct |
| PS10 | ParkingSlot.isCompatible | A compatible type is still rejected if the window is booked (parameterized, 12 cases) | Pass | Makes sure each branch really calls `isAvailable` |
| PS11 | ParkingSlot.isCompatible | A compatible type with a non-overlapping booking is accepted | Pass | |
| PS12 | ParkingSlot.isAvailable | No bookings → available | Pass | |
| PS13 | ParkingSlot.isAvailable | Identical window → blocked | Pass | |
| PS14 | ParkingSlot.isAvailable | Requested window inside an existing booking → blocked | Pass | |
| PS15 | ParkingSlot.isAvailable | Existing booking inside the requested window → blocked | Pass | |
| PS16 | ParkingSlot.isAvailable | Overlaps the start of an existing booking → blocked | Pass | |
| PS17 | ParkingSlot.isAvailable | Overlaps the end of an existing booking → blocked | Pass | |
| PS18 | ParkingSlot.isAvailable | Boundary: starts exactly when existing ends → available | Pass | Spec uses strict `end > start`, so back-to-back is allowed |
| PS19 | ParkingSlot.isAvailable | Boundary: ends exactly when existing starts → available | Pass | |
| PS20 | ParkingSlot.isAvailable | Completely before an existing booking → available | Pass | |
| PS21 | ParkingSlot.isAvailable | Completely after an existing booking → available | Pass | |
| PS22 | ParkingSlot.isAvailable | Every stored booking is checked, not just the first | Pass | |

### BookingTest (8 tests)

| Test ID | Class.Method | Why this test? | Verdict | Comments / Observations |
|---|---|---|---|---|
| B1 | Booking.Booking | All constructor fields are stored | Pass | |
| B2 | Booking.Booking | A new booking is `ACTIVE` | Pass | |
| B3 | Booking.Booking | Creating a booking moves no money | Pass | Money movement belongs to `ParkingSystem` |
| B4 | Booking.completeBooking | Status → `COMPLETED` | Pass | |
| B5 | Booking.cancelBooking | Status → `CANCELLED` | Pass | |
| B6 | Booking.completeBooking | Completing leaves id, amount and times unchanged | Pass | |
| B7 | Booking.toString | Output contains id, vehicle, times, amount and status | Pass | |
| B8 | Booking.toString | Output reflects the current status after a change | Pass | |

### ParkingSystemTest (28 tests)

| Test ID | Class.Method | Why this test? | Verdict | Comments / Observations |
|---|---|---|---|---|
| SYS1 | ParkingSystem.getInstance | Singleton always returns the same instance | Pass | |
| SYS2 | ParkingSystem.resetForTesting | Clears lists, restores rate 10.0, replaces the system wallet | Pass | |
| SYS3 | ParkingSystem.addVehicle / addParkingSlot | Entities are registered | Pass | |
| SYS4 | ParkingSystem setters / getters | `set*` replaces lists, rate and wallet; `getBalance` reads the system wallet | Pass | |
| SYS5 | ParkingSystem.getAvailableParkingSlots | Returns only compatible, active, free slots, in order | Pass | Mix of incompatible, inactive and booked slots |
| SYS6 | ParkingSystem.getAvailableParkingSlots | No registered slots → empty list | Pass | |
| SYS7 | ParkingSystem.book | End before start → `IllegalBookingTimeException`, nothing stored or charged | Pass | |
| SYS8 | ParkingSystem.book | Boundary: end == start → `IllegalBookingTimeException` | Pass | |
| SYS9 | ParkingSystem.book | Inactive slot → `IllegalArgumentException`, nothing stored or charged | Pass | |
| SYS10 | ParkingSystem.book | Incompatible slot type → `IllegalArgumentException` | Pass | |
| SYS11 | ParkingSystem.book | Overlapping window → `IllegalArgumentException` | Pass | |
| SYS12 | ParkingSystem.book | Valid booking: correct fields, `ACTIVE`, stored in system and slot lists, 100% charged vehicle → system | Pass | |
| SYS13 | ParkingSystem.book | Price = hours × 10 × vehicle rate × slot multiplier, for every compatible pair (parameterized, 12 cases) | Pass | TRUCK's rate (3.0) cannot be tested because TRUCK fits no slot |
| SYS14 | ParkingSystem.book | Uses the configured hourly rate, not a hard-coded 10 | Pass | |
| SYS15 | ParkingSystem.book | Booking ids are sequential (1, 2, 3) | Pass | |
| SYS16 | ParkingSystem.book | 90-minute booking is charged for 1.5 h | **Fail (D1)** | Charged 10.0 instead of 15.0 |
| SYS17 | ParkingSystem.book | 30-minute booking succeeds and is charged 0.5 h | **Fail (D2)** | Throws `InvalidAmountException` |
| SYS18 | ParkingSystem.book | Vehicle that cannot pay → `InsufficientFundsException`, balances unchanged | Pass | |
| SYS19 | ParkingSystem.book | A booking whose payment failed must not be stored | **Fail (D3)** | Orphan booking left in the system list |
| SYS20 | ParkingSystem.completeBooking | `COMPLETED`; 80% system → slot; system keeps 20% | Pass | |
| SYS21 | ParkingSystem.cancelBooking | `CANCELLED`; 90% system → vehicle; system keeps 10% | Pass | |
| SYS22 | ParkingSystem.completeBooking | Completing twice pays the slot only once | **Fail (D4)** | Slot paid 80% again |
| SYS23 | ParkingSystem.cancelBooking | A `COMPLETED` booking cannot be cancelled or refunded | **Fail (D5)** | Status flips to `CANCELLED` |
| SYS24 | ParkingSystem.completeBooking | A `CANCELLED` booking cannot be completed or paid out | **Fail (D6)** | Status flips to `COMPLETED` |
| SYS25 | ParkingSystem.cancelBooking | Cancelling twice refunds only once | **Fail (D7)** | Vehicle refunded 90% again |
| SYS26 | ParkingSystem.completeBooking | If the 80% payout fails, the booking stays `ACTIVE` | **Fail (D8)** | Left `COMPLETED` though the slot was not paid |
| SYS27 | ParkingSystem.cancelBooking | If the 90% refund fails, the booking stays `ACTIVE` | **Fail (D9)** | Left `CANCELLED` though no refund was paid |
| SYS28 | ParkingSystem.cancelBooking | A cancelled booking releases the slot for that window | **Fail (D10)** | Slot stays blocked |

**Result against current code:** 72 methods pass, 10 methods fail. Every failure corresponds to a listed defect.

---

## B) Defects List

| Defect ID | Class.Method | Description (expected vs actual) | 
|---|---|---|
| **D1** | ParkingSystem.book | **Expected:** price uses the real duration, so a 90-min CAR/REGULAR booking costs 1.5 × 10 = **15.0**. **Actual:** charged **10.0**. `Duration.between(...).toHours()` truncates partial hours, so every non-whole-hour booking is undercharged. | 
| **D2** | ParkingSystem.book | **Expected:** a valid 30-min booking succeeds for **5.0**. **Actual:** throws **`InvalidAmountException`**. Truncation gives 0 h → price 0.0 → `transferFunds(..., 0.0)` rejects it. No booking under 1 hour is possible. *(Same line as D1, different visible failure.)* | 
| **D3** | ParkingSystem.book | **Expected:** when the vehicle cannot pay, nothing is stored. **Actual:** `InsufficientFundsException` is thrown, but the unpaid `ACTIVE` booking **stays in `system.getBookings()`**: `bookings.add()` runs before `transferFunds()`, while the slot list is updated after. The system and slot lists then disagree. | 
| **D4** | ParkingSystem.completeBooking | **Expected:** a booking is settled once; a second `completeBooking` on a `COMPLETED` booking does nothing or is rejected (slot stays at 40.0). **Actual:** slot is paid 80% again (**80.0**), using other bookings' escrow money. No status check.
| **D5** | ParkingSystem.cancelBooking | **Expected:** a `COMPLETED` booking cannot be cancelled (status stays `COMPLETED`, no refund). **Actual:** status becomes **`CANCELLED`** and, per the code, a 90% refund is paid, so 170% of the fare is paid out for one booking.
| **D6** | ParkingSystem.completeBooking | **Expected:** a `CANCELLED` booking cannot be completed (status stays `CANCELLED`, slot unpaid). **Actual:** status becomes **`COMPLETED`** and, per the code, the slot is paid 80% after the vehicle was already refunded 90%. *(Same root cause as D4.)* | 
| **D7** | ParkingSystem.cancelBooking | **Expected:** cancelling twice refunds once (vehicle 145.0). **Actual:** refunded 90% again (**190.0**), paid from other bookings' escrow. *(Same root cause as D5.)* | 
| **D8** | ParkingSystem.completeBooking | **Expected:** if the 80% payout fails (`InsufficientFundsException`), the booking stays `ACTIVE`. **Actual:** booking is left **`COMPLETED`** though the slot received nothing, because `booking.completeBooking()` runs before the transfer. | 
| **D9** | ParkingSystem.cancelBooking | **Expected:** if the 90% refund fails, the booking stays `ACTIVE`. **Actual:** booking is left **`CANCELLED`** though no refund was paid, because `booking.cancelBooking()` runs before the transfer. |
| **D10** | ParkingSystem.cancelBooking | **Expected:** after cancellation the slot is free for that window. **Actual:** `slot.isAvailable(...)` returns **false**; the cancelled booking is never removed from `slot.getBookings()`, and `isAvailable` counts bookings of any status. *Interpretation note: the spec literally says "no **stored** booking overlaps", so this is a defect under the usual meaning of "cancel", not the literal wording.* | 

---

## C) Mutant Analysis

### Overall result

| Metric | Value |
|---|---|
| **Mutation score** | **98% (98 / 100 killed)** |
| Line coverage | 99% (179 / 180) |
| Test strength | 99% (98 / 99 covered mutants killed) |

| Class | Line coverage | Mutation coverage |
|---|---|---|
| Booking | 100% (28/28) | 100% (8/8) |
| ParkingSlot | 100% (36/36) | 100% (37/37) |
| ParkingSystem | 99% (70/71) | 97% (34/35) |
| Vehicle | 100% (18/18) | 100% (5/5) |
| Wallet | 100% (27/27) | 93% (14/15) |


---

## D) Individual Contribution

| Member | Contribution |
|---|---|
| Marjia Islam (0112230958) | Designed and wrote all five test classes; ran each test and recorded defects D1–D10; configured and ran PIT; wrote the report |
