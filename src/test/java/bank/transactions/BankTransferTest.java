package bank.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bank.db.DBHandler;
import bank.transactions.utils.AccountType;
import bank.transactions.utils.TransactionData;
import bank.transactions.utils.TransactionResult;
import bank.transactions.utils.TransactionType;
import bank.utils.FeesCalculator;

/**
 * Bonus (#6): DU-paths in BankTransfer#perform (lines 19-65) for fees (def n44),
 * newFromAmount (def n46) and newToAmount (def n47). No variable is redefined, so every
 * DU-path is definition-clear.
 *
 * FeesCalculator and DBHandler are stubbed. The stubbed fee is the A2 figure for the inputs
 * (not a student, $100 or more, from >= $1,000, to < $1,000 -> 0.5% of $100 = 0.5), so faults
 * in FeesCalculator can't show up here.
 *
 * Oracle: the A2 transfer fee is charged on the amount transferred to the account the money is
 * coming from, so "from" loses amount + fee and "to" gains the amount (bonus assumption 4).
 */
@ExtendWith(MockitoExtension.class)
class BankTransferTest {
	private static final double DELTA = 1e-6;

	private static final String CARD = "4000000000000";
	private static final String USER = "user";
	private static final AccountType FROM = AccountType.Chequing;
	private static final AccountType TO = AccountType.Savings;
	private static final double AMOUNT = 100.0;

	@Mock
	private DBHandler db;

	@Mock
	private FeesCalculator fees;

	private TransactionResult transfer(double fromBalance, double toBalance, double fee)
			throws Exception {
		doReturn(USER).when(db).getCardOwner(CARD);
		doReturn(fromBalance).when(db).getBalance(USER, FROM);
		doReturn(toBalance).when(db).getBalance(USER, TO);
		doReturn(false).when(db).isStudent(USER);
		doReturn(fee).when(fees).calculateTransferFee(AMOUNT, fromBalance, toBalance, false);

		TransactionData data = new TransactionData(CARD, null, TransactionType.Transfer,
				new AccountType[] {FROM, TO}, AMOUNT);
		return new BankTransfer(fees, db).perform(data);
	}

	private double balanceWritten(AccountType account) throws Exception {
		ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
		verify(db).setBalance(eq(USER), eq(account), captor.capture());
		return captor.getValue();
	}

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("BankTransfer.perform: DU-paths to the setBalance uses")
	@CsvSource({
		// id, DU-path covered, account, from balance, to balance, fee, expected balance written
		// from loses amount + fee: 2000 - 100 - 0.5
		"BN-10, 'T1 newToAmount n47 -> n50', Chequing, 2000.0, 500.0, 0.5, 1899.5",
		// to gains the amount: 500 + 100
		"BN-11, 'A3 newFromAmount n46 -> n51', Savings, 2000.0, 500.0, 0.5, 600.0",
	})
	void accountWritten(String id, String covers, AccountType account, double fromBalance,
			double toBalance, double fee, double expected) throws Exception {
		transfer(fromBalance, toBalance, fee);
		assertEquals(expected, balanceWritten(account), DELTA);
	}

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("BankTransfer.perform: DU-paths to the returned balances")
	@CsvSource({
		// id, DU-path covered, from balance, to balance, fee, index (0 = from, 1 = to), expected
		// from loses amount + fee: 2000 - 100 - 0.5
		"BN-12, 'A4 newFromAmount n46 -> n57, F1 fees n44 -> n45', 2000.0, 500.0, 0.5, 0, 1899.5",
		// to gains the amount: 500 + 100
		"BN-13, 'T2 newToAmount n47 -> n58, F2 fees n44 -> n47', 2000.0, 500.0, 0.5, 1, 600.0",
	})
	void balanceReturned(String id, String covers, double fromBalance, double toBalance,
			double fee, int index, double expected) throws Exception {
		TransactionResult result = transfer(fromBalance, toBalance, fee);
		assertTrue(result.isSuccessful());
		assertEquals(expected, result.getAccountBalances()[index], DELTA);
	}

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("BankTransfer.perform: DU-path to the returned fee")
	@CsvSource({
		// id, DU-path covered, from balance, to balance, fee, expected fee returned
		"BN-14, 'F3 fees n44 -> n59', 2000.0, 500.0, 0.5, 0.5",
	})
	void feeReturned(String id, String covers, double fromBalance, double toBalance,
			double fee, double expected) throws Exception {
		TransactionResult result = transfer(fromBalance, toBalance, fee);
		assertEquals(expected, result.getFees(), DELTA);
	}

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("BankTransfer.perform: DU-path to the false p-use of n48")
	@CsvSource({
		// id, DU-path covered, from balance, to balance, fee
		// not a student, $100 or more, from < $1,000, to < $1,000 -> 0.1% of $100 = 0.1
		"BN-15, 'A2 newFromAmount n46 -> (n48, n61)', 50.0, 500.0, 0.1",
	})
	void insufficientFunds(String id, String covers, double fromBalance, double toBalance,
			double fee) throws Exception {
		TransactionResult result = transfer(fromBalance, toBalance, fee);
		assertFalse(result.isSuccessful());
		verify(db, never()).setBalance(eq(USER), eq(FROM), anyDouble());
		verify(db, never()).setBalance(eq(USER), eq(TO), anyDouble());
	}
}
