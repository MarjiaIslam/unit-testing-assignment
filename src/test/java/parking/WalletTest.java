package parking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private static final double DELTA = 1e-9;

    @Test
    void defaultConstructor_startsWithZeroBalance() {
        Wallet w = new Wallet();
        assertEquals(0.0, w.getBalance(), DELTA);
    }

    @Test
    void constructorWithBalance_setsInitialBalance() {
        Wallet w = new Wallet(125.5);
        assertEquals(125.5, w.getBalance(), DELTA);
    }

    @Test
    void addFunds_positiveAmount_increasesBalance() {
        Wallet w = new Wallet(100.0);
        w.addFunds(50.0);
        assertEquals(150.0, w.getBalance(), DELTA);
    }

    @Test
    void addFunds_smallPositiveAmount_isAccepted() {
        Wallet w = new Wallet();
        w.addFunds(0.01);
        assertEquals(0.01, w.getBalance(), DELTA);
    }

    @Test
    void addFunds_zeroAmount_throwsInvalidAmount() {
        Wallet w = new Wallet(100.0);
        assertThrows(InvalidAmountException.class, () -> w.addFunds(0.0));
        assertEquals(100.0, w.getBalance(), DELTA);
    }

    @Test
    void addFunds_negativeAmount_throwsInvalidAmount() {
        Wallet w = new Wallet(100.0);
        assertThrows(InvalidAmountException.class, () -> w.addFunds(-10.0));
        assertEquals(100.0, w.getBalance(), DELTA);
    }

    @Test
    void deductFunds_validAmount_decreasesBalance() {
        Wallet w = new Wallet(100.0);
        w.deductFunds(30.0);
        assertEquals(70.0, w.getBalance(), DELTA);
    }

    @Test
    void deductFunds_exactBalance_leavesZero() {
        Wallet w = new Wallet(40.0);
        w.deductFunds(40.0);
        assertEquals(0.0, w.getBalance(), DELTA);
    }

    @Test
    void deductFunds_moreThanBalance_throwsInsufficientFunds() {
        Wallet w = new Wallet(40.0);
        assertThrows(InsufficientFundsException.class, () -> w.deductFunds(40.01));
        assertEquals(40.0, w.getBalance(), DELTA);
    }

    @Test
    void deductFunds_zeroAmount_throwsInvalidAmount() {
        Wallet w = new Wallet(100.0);
        assertThrows(InvalidAmountException.class, () -> w.deductFunds(0.0));
        assertEquals(100.0, w.getBalance(), DELTA);
    }

    @Test
    void deductFunds_negativeAmount_throwsInvalidAmount() {
        Wallet w = new Wallet(100.0);
        assertThrows(InvalidAmountException.class, () -> w.deductFunds(-5.0));
        assertEquals(100.0, w.getBalance(), DELTA);
    }

    @Test
    void transferFunds_validAmount_movesMoneyBetweenWallets() {
        Wallet from = new Wallet(100.0);
        Wallet to = new Wallet(20.0);
        from.transferFunds(to, 30.0);
        assertEquals(70.0, from.getBalance(), DELTA);
        assertEquals(50.0, to.getBalance(), DELTA);
    }

    @Test
    void transferFunds_exactBalance_emptiesSender() {
        Wallet from = new Wallet(60.0);
        Wallet to = new Wallet();
        from.transferFunds(to, 60.0);
        assertEquals(0.0, from.getBalance(), DELTA);
        assertEquals(60.0, to.getBalance(), DELTA);
    }

    @Test
    void transferFunds_moreThanBalance_throwsAndLeavesBothUnchanged() {
        Wallet from = new Wallet(60.0);
        Wallet to = new Wallet(5.0);
        assertThrows(InsufficientFundsException.class, () -> from.transferFunds(to, 60.01));
        assertEquals(60.0, from.getBalance(), DELTA);
        assertEquals(5.0, to.getBalance(), DELTA);
    }

    @Test
    void transferFunds_zeroAmount_throwsInvalidAmount() {
        Wallet from = new Wallet(60.0);
        Wallet to = new Wallet(5.0);
        assertThrows(InvalidAmountException.class, () -> from.transferFunds(to, 0.0));
        assertEquals(60.0, from.getBalance(), DELTA);
        assertEquals(5.0, to.getBalance(), DELTA);
    }

    @Test
    void transferFunds_negativeAmount_throwsInvalidAmount() {
        Wallet from = new Wallet(60.0);
        Wallet to = new Wallet(5.0);
        assertThrows(InvalidAmountException.class, () -> from.transferFunds(to, -1.0));
        assertEquals(60.0, from.getBalance(), DELTA);
        assertEquals(5.0, to.getBalance(), DELTA);
    }
}
