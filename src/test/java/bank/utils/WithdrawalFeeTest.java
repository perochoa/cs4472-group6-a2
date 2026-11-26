package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Withdrawal fee tests (#3): statement coverage of the backward slice on FinalUse(fee).
 *
 * Slicing criterion: {@code return fee;} at line 30 of FeesCalculator.java.
 * Slice = { n6, n10, n12, n13, n15, n18, n19, n20, n21, n23, n28, n30 }.
 * WD-01 to WD-05 together execute every statement in the slice. WD-06 is an extra case for the
 * Sunday part of the weekend condition on n12.
 *
 * Expected values come from the README A2 withdrawal figures
 * dayOfWeek uses java.util.Calendar: 1 = Sunday, 2 = Monday, 7 = Saturday
 */
class WithdrawalFeeTest {
	private static final double DELTA = 1e-6;

	private final FeesCalculator calculator = new FeesCalculator();

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("Withdrawal: slice on FinalUse(fee) + statement coverage")
	@CsvSource({
		// id, slice statements covered, amount, accountBalance, student, dayOfWeek, expected fee
		// Rule: student, weekend -> no fee
		"WD-01, 'n6 n10 n12 n13 n28 n30', 200, 500, true, 7, 0.0",
		// Rule: student, otherwise -> 0.1% of 200
		"WD-02, 'n6 n10 n12 n15 n28 n30', 200, 500, true, 2, 0.2",
		// Rule: not a student, balance < $1,000 -> 0.2% of 200
		"WD-03, 'n6 n10 n18 n19 n28 n30', 200, 500, false, 2, 0.4",
		// Rule: not a student, $1,000 <= balance < $10,000 -> 0.1% of 200
		"WD-04, 'n6 n10 n18 n20 n21 n28 n30', 200, 5000, false, 2, 0.2",
		// Rule: not a student, balance > $10,000 -> no fee
		"WD-05, 'n6 n10 n18 n20 n23 n28 n30', 200, 20000, false, 2, 0.0",
		// Rule: student, weekend -> no fee (extra: Sunday side of the || on n12)
		"WD-06, 'n6 n10 n12 n13 n28 n30 (Sunday)', 200, 500, true, 1, 0.0",
	})
	void withdrawalFee(String id, String covers, double amount, double accountBalance,
			boolean student, int dayOfWeek, double expected) {
		assertEquals(expected,
				calculator.calculateWithdrawalFee(amount, accountBalance, student, dayOfWeek), DELTA);
	}
}
