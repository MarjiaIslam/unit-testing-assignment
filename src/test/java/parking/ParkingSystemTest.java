package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSystemTest {

    private static final double DELTA = 1e-9;
    private static final LocalDateTime T10 = LocalDateTime.of(2026, 1, 1, 10, 0);

    private ParkingSystem system;

    @BeforeEach
    void setUp() {
        system = ParkingSystem.getInstance();
        system.resetForTesting();
    }

    @Test
    void getInstance_alwaysReturnsSameInstance() {
        assertNotNull(system);
        assertSame(system, ParkingSystem.getInstance());
    }

    @Test
    void resetForTesting_clearsEverythingAndRestoresDefaults() {
        Wallet oldWallet = system.getSYSTEM_WALLET();
        system.addVehicle(new Vehicle(1, VehicleType.CAR, 10.0));
        system.addParkingSlot(new ParkingSlot("R1", ParkingSlotType.REGULAR));
        system.getBookings().add(new Booking(1, null, null, T10, T10.plusHours(1), 1.0));
        system.setPARKING_RATE_PER_HOUR(99.0);
        system.getSYSTEM_WALLET().addFunds(50.0);

        system.resetForTesting();

        assertTrue(system.getVehicles().isEmpty());
        assertTrue(system.getParkingSlots().isEmpty());
        assertTrue(system.getBookings().isEmpty());
        assertEquals(10.0, system.getPARKING_RATE_PER_HOUR(), DELTA);
        assertNotSame(oldWallet, system.getSYSTEM_WALLET());
        assertEquals(0.0, system.getBalance(), DELTA);
    }

    @Test
    void addVehicleAndAddParkingSlot_registerEntities() {
        Vehicle v = new Vehicle(1, VehicleType.CAR, 10.0);
        ParkingSlot s = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        system.addVehicle(v);
        system.addParkingSlot(s);
        assertEquals(1, system.getVehicles().size());
        assertSame(v, system.getVehicles().get(0));
        assertEquals(1, system.getParkingSlots().size());
        assertSame(s, system.getParkingSlots().get(0));
    }

    @Test
    void setters_replaceBackingCollectionsRateAndWallet() {
        java.util.List<Vehicle> vehicles = new java.util.ArrayList<>();
        java.util.List<ParkingSlot> slots = new java.util.ArrayList<>();
        java.util.List<Booking> bookings = new java.util.ArrayList<>();
        Wallet wallet = new Wallet(42.0);

        system.setVehicles(vehicles);
        system.setParkingSlots(slots);
        system.setBookings(bookings);
        system.setPARKING_RATE_PER_HOUR(12.5);
        system.setSYSTEM_WALLET(wallet);

        assertSame(vehicles, system.getVehicles());
        assertSame(slots, system.getParkingSlots());
        assertSame(bookings, system.getBookings());
        assertEquals(12.5, system.getPARKING_RATE_PER_HOUR(), DELTA);
        assertSame(wallet, system.getSYSTEM_WALLET());
        assertEquals(42.0, system.getBalance(), DELTA);
    }

    @Test
    void getAvailableParkingSlots_returnsOnlyCompatibleActiveFreeSlots() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot compact = new ParkingSlot("C1", ParkingSlotType.COMPACT);   // incompatible
        ParkingSlot regular = new ParkingSlot("R1", ParkingSlotType.REGULAR);   // ok
        ParkingSlot large = new ParkingSlot("L1", ParkingSlotType.LARGE);       // ok
        ParkingSlot inactive = new ParkingSlot("R2", ParkingSlotType.REGULAR);  // inactive
        ParkingSlot booked = new ParkingSlot("R3", ParkingSlotType.REGULAR);    // overlapping booking
        inactive.deactivate();
        booked.getBookings().add(new Booking(99, car, booked, T10, T10.plusHours(2), 0.0));
        for (ParkingSlot s : new ParkingSlot[]{compact, regular, large, inactive, booked}) {
            system.addParkingSlot(s);
        }

        java.util.List<ParkingSlot> result = system.getAvailableParkingSlots(car, T10, T10.plusHours(1));

        assertEquals(java.util.List.of(regular, large), result);
    }

    @Test
    void getAvailableParkingSlots_noRegisteredSlots_returnsEmptyList() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        assertTrue(system.getAvailableParkingSlots(car, T10, T10.plusHours(1)).isEmpty());
    }

    @Test
    void book_endBeforeStart_throwsIllegalBookingTime() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        assertThrows(IllegalBookingTimeException.class,
                () -> system.book(car, slot, T10, T10.minusHours(1)));
        assertTrue(system.getBookings().isEmpty());
        assertEquals(1000.0, car.getBalance(), DELTA);
    }

    @Test
    void book_endEqualsStart_throwsIllegalBookingTime() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        assertThrows(IllegalBookingTimeException.class,
                () -> system.book(car, slot, T10, T10));
        assertTrue(system.getBookings().isEmpty());
        assertEquals(1000.0, car.getBalance(), DELTA);
    }

    @Test
    void book_inactiveSlot_throwsIllegalArgument() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        slot.deactivate();
        assertThrows(IllegalArgumentException.class,
                () -> system.book(car, slot, T10, T10.plusHours(2)));
        assertTrue(system.getBookings().isEmpty());
        assertTrue(slot.getBookings().isEmpty());
        assertEquals(1000.0, car.getBalance(), DELTA);
    }

    @Test
    void book_incompatibleSlotType_throwsIllegalArgument() {
        Vehicle bus = new Vehicle(2, VehicleType.BUS, 1000.0);
        ParkingSlot compact = new ParkingSlot("C1", ParkingSlotType.COMPACT);
        assertThrows(IllegalArgumentException.class,
                () -> system.book(bus, compact, T10, T10.plusHours(2)));
        assertTrue(system.getBookings().isEmpty());
        assertTrue(compact.getBookings().isEmpty());
        assertEquals(1000.0, bus.getBalance(), DELTA);
    }

    @Test
    void book_overlappingWindow_throwsIllegalArgument() {
        Vehicle first = new Vehicle(1, VehicleType.CAR, 1000.0);
        Vehicle second = new Vehicle(2, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        system.book(first, slot, T10, T10.plusHours(2));

        assertThrows(IllegalArgumentException.class,
                () -> system.book(second, slot, T10.plusHours(1), T10.plusHours(3)));
        assertEquals(1, system.getBookings().size());
        assertEquals(1, slot.getBookings().size());
        assertEquals(1000.0, second.getBalance(), DELTA);
    }

    @Test
    void book_validRequest_createsActiveBookingStoresItAndChargesVehicle() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        Booking b = system.book(car, slot, T10, T10.plusHours(2));

        assertEquals(1, b.getBookingId());
        assertSame(car, b.getVehicle());
        assertSame(slot, b.getParkingSlot());
        assertEquals(T10, b.getStartTime());
        assertEquals(T10.plusHours(2), b.getEndTime());
        assertEquals(20.0, b.getAmount(), DELTA);
        assertEquals(BookingStatus.ACTIVE, b.getBookingStatus());
        assertEquals(java.util.List.of(b), system.getBookings());
        assertEquals(java.util.List.of(b), slot.getBookings());
        assertEquals(80.0, car.getBalance(), DELTA);
        assertEquals(20.0, system.getBalance(), DELTA);
        assertEquals(0.0, slot.getBalance(), DELTA);
    }

    @ParameterizedTest(name = "{0} on {1} for 2h -> {2}")
    @CsvSource({
            "MOTORCYCLE, COMPACT,      8.0",
            "MOTORCYCLE, REGULAR,     10.0",
            "MOTORCYCLE, LARGE,       15.0",
            "CAR,        REGULAR,     20.0",
            "CAR,        LARGE,       30.0",
            "BUS,        LARGE,       60.0",
            "BICYCLE,    COMPACT,      3.2",
            "BICYCLE,    REGULAR,      4.0",
            "BICYCLE,    LARGE,        6.0",
            "BICYCLE,    HANDICAPPED,  4.8",
            "MICROCAR,   COMPACT,     24.0",
            "MICROCAR,   REGULAR,     30.0"
    })
    void book_priceFollowsVehicleRateAndSlotMultiplier(VehicleType type, ParkingSlotType slotType, double expected) {
        Vehicle v = new Vehicle(1, type, 1000.0);
        ParkingSlot slot = new ParkingSlot("S", slotType);

        Booking b = system.book(v, slot, T10, T10.plusHours(2));

        assertEquals(expected, b.getAmount(), DELTA);
        assertEquals(1000.0 - expected, v.getBalance(), DELTA);
        assertEquals(expected, system.getBalance(), DELTA);
    }

    @Test
    void book_usesConfiguredHourlyRate() {
        system.setPARKING_RATE_PER_HOUR(4.0);
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        Booking b = system.book(car, slot, T10, T10.plusHours(3));

        assertEquals(12.0, b.getAmount(), DELTA);
        assertEquals(88.0, car.getBalance(), DELTA);
    }

    @Test
    void book_assignsSequentialBookingIds() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        Booking b1 = system.book(car, slot, T10, T10.plusHours(1));
        Booking b2 = system.book(car, slot, T10.plusHours(1), T10.plusHours(2));
        Booking b3 = system.book(car, slot, T10.plusHours(2), T10.plusHours(3));

        assertEquals(1, b1.getBookingId());
        assertEquals(2, b2.getBookingId());
        assertEquals(3, b3.getBookingId());
    }

    @Test
    void book_partialHour_isChargedForActualDuration() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        Booking b = system.book(car, slot, T10, T10.plusMinutes(90));

        assertEquals(15.0, b.getAmount(), DELTA);
        assertEquals(85.0, car.getBalance(), DELTA);
    }

    @Test
    void book_underOneHour_succeedsAndIsCharged() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        Booking b = assertDoesNotThrow(() -> system.book(car, slot, T10, T10.plusMinutes(30)));

        assertEquals(5.0, b.getAmount(), DELTA);
        assertEquals(95.0, car.getBalance(), DELTA);
    }

    @Test
    void book_insufficientFunds_throwsInsufficientFunds() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 5.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        assertThrows(InsufficientFundsException.class,
                () -> system.book(car, slot, T10, T10.plusHours(2)));
        assertEquals(5.0, car.getBalance(), DELTA);
        assertEquals(0.0, system.getBalance(), DELTA);
    }

    @Test
    void book_insufficientFunds_doesNotStoreBooking() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 5.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        assertThrows(InsufficientFundsException.class,
                () -> system.book(car, slot, T10, T10.plusHours(2)));
        assertTrue(slot.getBookings().isEmpty(), "slot must not keep an unpaid booking");
        assertTrue(system.getBookings().isEmpty(), "system must not keep an unpaid booking");
    }

    @Test
    void completeBooking_marksCompletedAndPays80PercentToSlot() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5)); // 50.0

        system.completeBooking(b);

        assertEquals(BookingStatus.COMPLETED, b.getBookingStatus());
        assertEquals(40.0, slot.getBalance(), DELTA);   // 80%
        assertEquals(10.0, system.getBalance(), DELTA); // 20% retained
        assertEquals(50.0, car.getBalance(), DELTA);    // vehicle untouched
    }

    @Test
    void cancelBooking_marksCancelledAndRefunds90PercentToVehicle() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5)); // 50.0

        system.cancelBooking(b);

        assertEquals(BookingStatus.CANCELLED, b.getBookingStatus());
        assertEquals(95.0, car.getBalance(), DELTA);   // 50 + 45 refund
        assertEquals(5.0, system.getBalance(), DELTA); // 10% retained
        assertEquals(0.0, slot.getBalance(), DELTA);   // slot gets nothing
    }

    @Test
    void completeBooking_calledTwice_paysSlotOnlyOnce() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 200.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5));                      // 50.0
        system.book(car, slot, T10.plusHours(5), T10.plusHours(10));                    // 50.0 (keeps escrow funded)
        system.completeBooking(b);                                                      // slot 40, system 60

        try {
            system.completeBooking(b);
        } catch (RuntimeException ignored) {
            // rejecting the second completion is acceptable
        }

        assertEquals(BookingStatus.COMPLETED, b.getBookingStatus());
        assertEquals(40.0, slot.getBalance(), DELTA);
        assertEquals(60.0, system.getBalance(), DELTA);
    }

    @Test
    void cancelBooking_afterCompletion_doesNotRefundVehicle() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 200.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5));                      // 50.0
        system.book(car, slot, T10.plusHours(5), T10.plusHours(10));                    // 50.0 (keeps escrow funded)
        system.completeBooking(b);                                                      // slot 40, system 60

        try {
            system.cancelBooking(b);
        } catch (RuntimeException ignored) {
            // rejecting the cancellation is acceptable
        }

        assertEquals(BookingStatus.COMPLETED, b.getBookingStatus());
        assertEquals(100.0, car.getBalance(), DELTA);
        assertEquals(60.0, system.getBalance(), DELTA);
    }

    @Test
    void completeBooking_afterCancellation_doesNotPaySlot() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 200.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5));                      // 50.0
        system.book(car, slot, T10.plusHours(5), T10.plusHours(10));                    // 50.0 (keeps escrow funded)
        system.cancelBooking(b);                                                        // car 145, system 55

        try {
            system.completeBooking(b);
        } catch (RuntimeException ignored) {
            // rejecting the completion is acceptable
        }

        assertEquals(BookingStatus.CANCELLED, b.getBookingStatus());
        assertEquals(0.0, slot.getBalance(), DELTA);
        assertEquals(55.0, system.getBalance(), DELTA);
    }

    @Test
    void cancelBooking_calledTwice_refundsOnlyOnce() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 200.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5));                      // 50.0
        system.book(car, slot, T10.plusHours(5), T10.plusHours(10));                    // 50.0 (keeps escrow funded)
        system.cancelBooking(b);                                                        // car 145, system 55

        try {
            system.cancelBooking(b);
        } catch (RuntimeException ignored) {
            // rejecting the second cancellation is acceptable
        }

        assertEquals(BookingStatus.CANCELLED, b.getBookingStatus());
        assertEquals(145.0, car.getBalance(), DELTA);
        assertEquals(55.0, system.getBalance(), DELTA);
    }

    @Test
    void completeBooking_whenSettlementFails_keepsBookingActive() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5)); // 50.0
        system.setSYSTEM_WALLET(new Wallet());                     // escrow can no longer pay out

        assertThrows(InsufficientFundsException.class, () -> system.completeBooking(b));

        assertEquals(BookingStatus.ACTIVE, b.getBookingStatus());
        assertEquals(0.0, slot.getBalance(), DELTA);
    }

    @Test
    void cancelBooking_whenRefundFails_keepsBookingActive() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking b = system.book(car, slot, T10, T10.plusHours(5)); // 50.0, car left with 50.0
        system.setSYSTEM_WALLET(new Wallet());                     // escrow can no longer refund

        assertThrows(InsufficientFundsException.class, () -> system.cancelBooking(b));

        assertEquals(BookingStatus.ACTIVE, b.getBookingStatus());
        assertEquals(50.0, car.getBalance(), DELTA);
    }

    @Test
    void cancelBooking_releasesSlotForThatWindow() {
        Vehicle first = new Vehicle(1, VehicleType.CAR, 100.0);
        Vehicle second = new Vehicle(2, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        system.addParkingSlot(slot);
        Booking b = system.book(first, slot, T10, T10.plusHours(2));

        system.cancelBooking(b);

        assertTrue(slot.isAvailable(T10, T10.plusHours(2)), "cancelled booking must not block the slot");
        assertEquals(java.util.List.of(slot), system.getAvailableParkingSlots(second, T10, T10.plusHours(2)));
        assertDoesNotThrow(() -> system.book(second, slot, T10, T10.plusHours(2)));
    }
}
