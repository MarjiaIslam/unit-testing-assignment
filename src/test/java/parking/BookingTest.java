package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private static final double DELTA = 1e-9;
    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime END = START.plusHours(3);

    private Vehicle vehicle;
    private ParkingSlot slot;
    private Booking booking;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle(11, VehicleType.CAR, 100.0);
        slot = new ParkingSlot("R9", ParkingSlotType.REGULAR);
        booking = new Booking(3, vehicle, slot, START, END, 30.0);
    }

    @Test
    void constructor_storesAllFields() {
        assertEquals(3, booking.getBookingId());
        assertSame(vehicle, booking.getVehicle());
        assertSame(slot, booking.getParkingSlot());
        assertEquals(START, booking.getStartTime());
        assertEquals(END, booking.getEndTime());
        assertEquals(30.0, booking.getAmount(), DELTA);
    }

    @Test
    void constructor_newBookingIsActive() {
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus());
    }

    @Test
    void constructor_doesNotTouchWallets() {
        assertEquals(100.0, vehicle.getBalance(), DELTA);
        assertEquals(0.0, slot.getBalance(), DELTA);
    }

    @Test
    void completeBooking_setsStatusCompleted() {
        booking.completeBooking();
        assertEquals(BookingStatus.COMPLETED, booking.getBookingStatus());
    }

    @Test
    void cancelBooking_setsStatusCancelled() {
        booking.cancelBooking();
        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
    }

    @Test
    void completeBooking_doesNotChangeOtherFields() {
        booking.completeBooking();
        assertEquals(3, booking.getBookingId());
        assertEquals(30.0, booking.getAmount(), DELTA);
        assertEquals(START, booking.getStartTime());
        assertEquals(END, booking.getEndTime());
    }

    @Test
    void toString_containsKeyFields() {
        String s = booking.toString();
        assertTrue(s.startsWith("Booking{bookingId=3, "), s);
        assertTrue(s.contains("vehicle=" + vehicle), s);
        assertTrue(s.contains("startTime=" + START), s);
        assertTrue(s.contains("endTime=" + END), s);
        assertTrue(s.contains("amount=30.0"), s);
        assertTrue(s.endsWith("bookingStatus=ACTIVE}"), s);
    }

    @Test
    void toString_reflectsStatusChange() {
        booking.cancelBooking();
        assertTrue(booking.toString().endsWith("bookingStatus=CANCELLED}"));
    }
}
