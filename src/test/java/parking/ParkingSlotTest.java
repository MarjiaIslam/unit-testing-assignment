package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSlotTest {

    private static final double DELTA = 1e-9;
    private static final LocalDateTime T10 = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime T12 = T10.plusHours(2);

    private ParkingSlot regular;
    private Vehicle car;

    @BeforeEach
    void setUp() {
        regular = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        car = new Vehicle(1, VehicleType.CAR, 1000.0);
    }

    private void addBooking(ParkingSlot slot, LocalDateTime start, LocalDateTime end) {
        slot.getBookings().add(new Booking(slot.getBookings().size() + 1, car, slot, start, end, 0.0));
    }

    @Test
    void constructor_setsIdTypeActiveAndEmptyWallet() {
        ParkingSlot s = new ParkingSlot("L7", ParkingSlotType.LARGE);
        assertEquals("L7", s.getSlotId());
        assertEquals(ParkingSlotType.LARGE, s.getSlotType());
        assertTrue(s.isActive());
        assertNotNull(s.getWallet());
        assertEquals(0.0, s.getBalance(), DELTA);
    }

    @Test
    void constructor_startsWithNoBookings() {
        assertNotNull(regular.getBookings());
        assertTrue(regular.getBookings().isEmpty());
    }

    @Test
    void getBalance_reflectsWalletChanges() {
        regular.getWallet().addFunds(12.5);
        assertEquals(12.5, regular.getBalance(), DELTA);
    }

    @Test
    void getWallet_returnsSameInstanceEachCall() {
        assertSame(regular.getWallet(), regular.getWallet());
    }

    @Test
    void deactivate_makesSlotInactive() {
        regular.deactivate();
        assertFalse(regular.isActive());
    }

    @Test
    void activate_reactivatesSlot() {
        regular.deactivate();
        regular.activate();
        assertTrue(regular.isActive());
    }

    @Test
    void isCompatible_inactiveSlot_returnsFalse() {
        regular.deactivate();
        assertFalse(regular.isCompatible(VehicleType.CAR, T10, T12));
    }

    @Test
    void isCompatible_reactivatedSlot_returnsTrue() {
        regular.deactivate();
        regular.activate();
        assertTrue(regular.isCompatible(VehicleType.CAR, T10, T12));
    }

    @ParameterizedTest(name = "{0} on {1} -> {2}")
    @CsvSource({
            "MOTORCYCLE, COMPACT,     true",
            "MOTORCYCLE, REGULAR,     true",
            "MOTORCYCLE, LARGE,       true",
            "MOTORCYCLE, HANDICAPPED, false",
            "CAR,        COMPACT,     false",
            "CAR,        REGULAR,     true",
            "CAR,        LARGE,       true",
            "CAR,        HANDICAPPED, false",
            "BUS,        COMPACT,     false",
            "BUS,        REGULAR,     false",
            "BUS,        LARGE,       true",
            "BUS,        HANDICAPPED, false",
            "BICYCLE,    COMPACT,     true",
            "BICYCLE,    REGULAR,     true",
            "BICYCLE,    LARGE,       true",
            "BICYCLE,    HANDICAPPED, true",
            "MICROCAR,   COMPACT,     true",
            "MICROCAR,   REGULAR,     true",
            "MICROCAR,   LARGE,       false",
            "MICROCAR,   HANDICAPPED, false",
            "TRUCK,      COMPACT,     false",
            "TRUCK,      REGULAR,     false",
            "TRUCK,      LARGE,       false",
            "TRUCK,      HANDICAPPED, false"
    })
    void isCompatible_followsCompatibilityMatrix(VehicleType vehicle, ParkingSlotType slotType, boolean expected) {
        ParkingSlot s = new ParkingSlot("S", slotType);
        assertEquals(expected, s.isCompatible(vehicle, T10, T12));
    }

    @ParameterizedTest(name = "{0} on booked {1} -> false")
    @CsvSource({
            "MOTORCYCLE, COMPACT",
            "MOTORCYCLE, REGULAR",
            "MOTORCYCLE, LARGE",
            "CAR,        REGULAR",
            "CAR,        LARGE",
            "BUS,        LARGE",
            "BICYCLE,    COMPACT",
            "BICYCLE,    REGULAR",
            "BICYCLE,    LARGE",
            "BICYCLE,    HANDICAPPED",
            "MICROCAR,   COMPACT",
            "MICROCAR,   REGULAR"
    })
    void isCompatible_compatibleTypeButOverlappingBooking_returnsFalse(VehicleType vehicle, ParkingSlotType slotType) {
        ParkingSlot s = new ParkingSlot("S", slotType);
        addBooking(s, T10, T12);
        assertFalse(s.isCompatible(vehicle, T10.plusMinutes(30), T12.plusHours(1)));
    }

    @Test
    void isCompatible_compatibleTypeAndNonOverlappingBooking_returnsTrue() {
        addBooking(regular, T10, T12);
        assertTrue(regular.isCompatible(VehicleType.CAR, T12, T12.plusHours(1)));
    }

    @Test
    void isAvailable_noBookings_returnsTrue() {
        assertTrue(regular.isAvailable(T10, T12));
    }

    @Test
    void isAvailable_identicalWindow_returnsFalse() {
        addBooking(regular, T10, T12);
        assertFalse(regular.isAvailable(T10, T12));
    }

    @Test
    void isAvailable_requestedInsideExisting_returnsFalse() {
        addBooking(regular, T10, T12);
        assertFalse(regular.isAvailable(T10.plusMinutes(30), T10.plusMinutes(90)));
    }

    @Test
    void isAvailable_existingInsideRequested_returnsFalse() {
        addBooking(regular, T10.plusMinutes(30), T10.plusMinutes(90));
        assertFalse(regular.isAvailable(T10, T12));
    }

    @Test
    void isAvailable_overlapsStartOfExisting_returnsFalse() {
        addBooking(regular, T10, T12);
        assertFalse(regular.isAvailable(T10.minusHours(1), T10.plusMinutes(1)));
    }

    @Test
    void isAvailable_overlapsEndOfExisting_returnsFalse() {
        addBooking(regular, T10, T12);
        assertFalse(regular.isAvailable(T12.minusMinutes(1), T12.plusHours(1)));
    }

    @Test
    void isAvailable_startsExactlyWhenExistingEnds_returnsTrue() {
        addBooking(regular, T10, T12);
        assertTrue(regular.isAvailable(T12, T12.plusHours(1)));
    }

    @Test
    void isAvailable_endsExactlyWhenExistingStarts_returnsTrue() {
        addBooking(regular, T10, T12);
        assertTrue(regular.isAvailable(T10.minusHours(1), T10));
    }

    @Test
    void isAvailable_completelyBeforeExisting_returnsTrue() {
        addBooking(regular, T10, T12);
        assertTrue(regular.isAvailable(T10.minusHours(3), T10.minusHours(2)));
    }

    @Test
    void isAvailable_completelyAfterExisting_returnsTrue() {
        addBooking(regular, T10, T12);
        assertTrue(regular.isAvailable(T12.plusHours(2), T12.plusHours(3)));
    }

    @Test
    void isAvailable_checksEveryStoredBooking() {
        addBooking(regular, T10.minusHours(5), T10.minusHours(4)); // does not overlap
        addBooking(regular, T10, T12);                             // overlaps
        assertFalse(regular.isAvailable(T10.plusMinutes(15), T10.plusMinutes(45)));
    }
}
