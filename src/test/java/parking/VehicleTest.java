package parking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    private static final double DELTA = 1e-9;

    @Test
    void constructorWithWallet_storesIdAndType() {
        Vehicle v = new Vehicle(42, VehicleType.TRUCK, new Wallet(10.0));
        assertEquals(42, v.getVehicleId());
        assertEquals(VehicleType.TRUCK, v.getVehicleType());
    }

    @Test
    void constructorWithWallet_usesSameWalletInstance() {
        Wallet w = new Wallet(10.0);
        Vehicle v = new Vehicle(1, VehicleType.CAR, w);
        assertSame(w, v.getWallet());
    }

    @Test
    void constructorWithWallet_balanceReflectsWallet() {
        Vehicle v = new Vehicle(1, VehicleType.CAR, new Wallet(77.25));
        assertEquals(77.25, v.getBalance(), DELTA);
    }

    @Test
    void constructorWithBalance_createsWalletWithThatBalance() {
        Vehicle v = new Vehicle(5, VehicleType.BUS, 300.0);
        assertEquals(5, v.getVehicleId());
        assertEquals(VehicleType.BUS, v.getVehicleType());
        assertNotNull(v.getWallet());
        assertEquals(300.0, v.getWallet().getBalance(), DELTA);
        assertEquals(300.0, v.getBalance(), DELTA);
    }

    @Test
    void constructorWithBalance_eachVehicleGetsOwnWallet() {
        Vehicle a = new Vehicle(1, VehicleType.CAR, 50.0);
        Vehicle b = new Vehicle(2, VehicleType.CAR, 50.0);
        assertNotSame(a.getWallet(), b.getWallet());
        a.getWallet().addFunds(25.0);
        assertEquals(75.0, a.getBalance(), DELTA);
        assertEquals(50.0, b.getBalance(), DELTA);
    }

    @Test
    void getBalance_reflectsLaterWalletChanges() {
        Vehicle v = new Vehicle(3, VehicleType.MOTORCYCLE, 100.0);
        v.getWallet().addFunds(20.0);
        assertEquals(120.0, v.getBalance(), DELTA);
        v.getWallet().deductFunds(70.0);
        assertEquals(50.0, v.getBalance(), DELTA);
    }

    @ParameterizedTest
    @EnumSource(VehicleType.class)
    void getVehicleType_worksForEveryType(VehicleType type) {
        Vehicle v = new Vehicle(9, type, 0.0);
        assertEquals(type, v.getVehicleType());
    }

    @Test
    void toString_containsIdTypeAndBalance() {
        Vehicle v = new Vehicle(7, VehicleType.CAR, 50.0);
        assertEquals("Vehicle{vehicleId=7, vehicleType=CAR, walletBalance=50.0}", v.toString());
    }
}
