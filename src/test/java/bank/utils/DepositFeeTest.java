package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Deposit Fee Tests (#4): DU-Path coverage on interestPercentage
 *
 * DP-01 to DP-08 test all DU-Paths
 *
 * Expected values come from the README A2 withdrawal figures
 */
class DepositFeeTest {
    private static final double DELTA = 1e-6;

    private final FeesCalculator calculator = new FeesCalculator();

    @ParameterizedTest(name = "{0}: {1}")
    @DisplayName("Deposit Interest Fee Test: DU-Path Testing")
    @CsvSource({
            // id, structural element covered, inputs..., expected fee
            "DP-01, 'n42 n70', 101.00, 1001.00, true, 1.01",
            "DP-02, 'n44 n70', 101.00, 999.99, true, 0.505",
            "DP-03, 'n48 n70', 100.00, 5001.00, true, 0.50",
            "DP-04, 'n50 n70', 100.00, 4999.99, true, 0.00",
            "DP-05, 'n56 n70', 501.00, 5001.00, false, 5.01",
            "DP-06, 'n58 n70', 501.00, 4999.99, false, 2.505",
            "DP-07, 'n62 n70', 500.00, 10001.00, false, 2.50",
            "DP-08, 'n64 n70', 500.00, 10000.00, false, 0.00",
    })
    void depositInterestFee(String id, String covers, double amount, double accountBalance,
                            boolean student, double expected) {
        assertEquals(expected,
                calculator.calculateDepositInterest(amount, accountBalance, student), DELTA);
    }
}
