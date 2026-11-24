package atm.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Bonus (#6): basis path testing of CashValidator#validateWithdrawal (lines 20-32).
 *
 * Decisions: n24 (amount % 50 != 0) and n26 (rest % 20 != 0), so V(G) = 3.
 * B1 = n20 n21 n23 n24(F) n31
 * B2 = n20 n21 n23 n24(T) n25 n26(F) n31
 * B3 = n20 n21 n23 n24(T) n25 n26(T) n27 n31
 * Each path's input domain is split by the spec into a valid and an invalid part where both exist.
 *
 * Oracle: A1 ATM session rules. A valid withdrawal amount can be dispensed in $20 and $50 bills.
 */
class CashValidatorTest {

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("CashValidator.validateWithdrawal: basis paths")
	@CsvSource({
		// id, basis path covered, amount, expected valid
		// 2 x $50 -> valid
		"BN-01, 'B1 n24(F)', 100, true",
		// no bill is dispensed for $0 -> not valid
		"BN-02, 'B1 n24(F)', 0, false",
		// $50 + $20 -> valid
		"BN-03, 'B2 n24(T) n26(F)', 70, true",
		// $30 cannot be made from $20 and $50 bills -> not valid
		"BN-04, 'B3 n24(T) n26(T) n27', 30, false",
		// 4 x $20 -> valid
		"BN-05, 'B3 n24(T) n26(T) n27', 80, true",
	})
	void validateWithdrawal(String id, String covers, int amount, boolean expected) {
		assertEquals(expected, CashValidator.validateWithdrawal(amount));
	}
}
