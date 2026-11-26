package atm.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import atm.exceptions.InvalidPinFormatException;

/**
 * Bonus (#6): basis path testing of FormatChecker#checkPinFormat (lines 32-42).
 *
 * Decisions: n33 (pin.length == 4), n34 (i < 4), n35a (pin[i] < '0'), n35b (pin[i] >= '9'),
 * so V(G) = 5. Baseline P1 is the all-digits path through the loop.
 * P1 = n32 n33(T) n34(T) n35a(F) n35b(F) n34 ... n34(F) x
 * P2 = n32 n33(F) n40 x
 * P3 = n32 n33(T) n34(F) x                  (infeasible: i = 0 < 4 on the first test)
 * P4 = n32 n33(T) n34(T) n35a(T) n36 x
 * P5 = n32 n33(T) n34(T) n35a(F) n35b(T) n36 x
 * P6 = n32 n33(T) n34(T) n35a(F) n35b(F) n34(T) n35a(T) n36 x   (replaces P3: one loop
 *      iteration, then a throw. It is the only path with the back edge and no normal exit.)
 *
 * Oracle: A1 ATM session rules. A valid PIN is 5 digits; anything else throws
 * InvalidPinFormatException.
 */
class FormatCheckerTest {

	private final FormatChecker checker = new FormatChecker();

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("FormatChecker.checkPinFormat: basis paths")
	@CsvSource({
		// id, basis path covered, pin, expected valid
		// 4 digits -> not valid
		"BN-06, 'P1 n33(T) n34(T) n35a(F) n35b(F) n34(F)', 1234, false",
		// 5 digits -> valid
		"BN-07, 'P2 n33(F) n40', 12345, true",
		// '/' is not a digit (and 4 characters) -> not valid
		"BN-08, 'P4 n33(T) n34(T) n35a(T) n36', /123, false",
		// 4 characters -> not valid ('9' is the boundary of n35b)
		"BN-09, 'P5 n33(T) n34(T) n35a(F) n35b(T) n36', 9123, false",
		// '/' in the second position (and 4 characters) -> not valid
		"BN-16, 'P6 n33(T) n34(T) n35a(F) n35b(F) n34(T) n35a(T) n36', 1/23, false",
	})
	void checkPinFormat(String id, String covers, String pin, boolean expected) {
		char[] chars = pin.toCharArray();
		if (expected) {
			assertDoesNotThrow(() -> checker.checkPinFormat(chars));
		} else {
			assertThrows(InvalidPinFormatException.class, () -> checker.checkPinFormat(chars));
		}
	}
}
