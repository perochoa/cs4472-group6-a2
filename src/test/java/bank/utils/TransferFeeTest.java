package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Basis path testing for FeesCalculator#calculateTransferFee.
 * V(G) = 16, so there are 16 basis paths and one test per path.
 * CFG nodes are numbered by source line (n79 = line 79). See docs/assignment2.md section 1.3.
 * Expected fees come from the README A2 "Transfer" figures, not from the code.
 */

class TransferFeeTest {
	private static final double DELTA = 1e-6;

	private final FeesCalculator calculator = new FeesCalculator();

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("Transfer: basis path testing")
	@CsvSource({
		// id, basis path covered, amount, fromBalance, toBalance, student, expected fee
		"TR-01, 'P1 n77-n79T-n80T-n81T-n82T-n83-n142-n144',     50,  500,  500, true,  0.05",
		"TR-02, 'P2 n77-n79T-n80T-n81T-n82F-n85-n142-n144',     50,  500, 2000, true,  0.025",
		"TR-03, 'P3 n77-n79T-n80T-n81F-n88T-n89-n142-n144',     50, 2000,  500, true,  0.25",
		"TR-04, 'P4 n77-n79T-n80T-n81F-n88F-n91-n142-n144',     50, 2000, 2000, true,  0.125",
		"TR-05, 'P5 n77-n79T-n80F-n95T-n96T-n97-n142-n144',    200,  500,  500, true,  0.1",
		"TR-06, 'P6 n77-n79T-n80F-n95T-n96F-n99-n142-n144',    200,  500, 2000, true,  0.05",
		"TR-07, 'P7 n77-n79T-n80F-n95F-n102T-n103-n142-n144',  200, 2000,  500, true,  0.5",
		"TR-08, 'P8 n77-n79T-n80F-n95F-n102F-n105-n142-n144',  200, 2000, 2000, true,  0.25",
		"TR-09, 'P9 n77-n79F-n110T-n111T-n112T-n113-n142-n144',  50,  500,  500, false, 0.1",
		"TR-10, 'P10 n77-n79F-n110T-n111T-n112F-n115-n142-n144', 50,  500, 2000, false, 0.05",
		"TR-11, 'P11 n77-n79F-n110T-n111F-n118T-n119-n142-n144', 50, 2000,  500, false, 0.5",
		"TR-12, 'P12 n77-n79F-n110T-n111F-n118F-n121-n142-n144', 50, 2000, 2000, false, 0.25",
		"TR-13, 'P13 n77-n79F-n110F-n125T-n126T-n127-n142-n144', 200,  500,  500, false, 0.2",
		"TR-14, 'P14 n77-n79F-n110F-n125T-n126F-n129-n142-n144', 200,  500, 2000, false, 0.1",
		"TR-15, 'P15 n77-n79F-n110F-n125F-n132T-n133-n142-n144', 200, 2000,  500, false, 1.0",
		"TR-16, 'P16 n77-n79F-n110F-n125F-n132F-n135-n142-n144', 200, 2000, 2000, false, 0.5",
	})
	void transferFee(String id, String covers, double amount, double fromAccountBalance,
			double toAccountBalance, boolean student, double expected) {
		assertEquals(expected,
				calculator.calculateTransferFee(amount, fromAccountBalance, toAccountBalance, student),
				DELTA, id + " covers " + covers);
	}
}
