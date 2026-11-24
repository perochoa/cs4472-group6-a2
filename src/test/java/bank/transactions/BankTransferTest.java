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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	private static final double FEE = 0.5;

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

	@Test
	@DisplayName("BN-10: newToAmount n47 -> n50, from account written with 2000 - 100 - 0.5")
	void fromAccountWritten() throws Exception {
		transfer(2000.0, 500.0, FEE);
		assertEquals(1899.5, balanceWritten(FROM), DELTA);
	}

	@Test
	@DisplayName("BN-11: newFromAmount n46 -> n51, to account written with 500 + 100")
	void toAccountWritten() throws Exception {
		transfer(2000.0, 500.0, FEE);
		assertEquals(600.0, balanceWritten(TO), DELTA);
	}

	@Test
	@DisplayName("BN-12: newFromAmount n46 -> n57 and fees n44 -> n45, returned from balance")
	void fromBalanceReturned() throws Exception {
		TransactionResult result = transfer(2000.0, 500.0, FEE);
		assertTrue(result.isSuccessful());
		assertEquals(1899.5, result.getAccountBalances()[0], DELTA);
	}

	@Test
	@DisplayName("BN-13: newToAmount n47 -> n58 and fees n44 -> n47, returned to balance")
	void toBalanceReturned() throws Exception {
		TransactionResult result = transfer(2000.0, 500.0, FEE);
		assertTrue(result.isSuccessful());
		assertEquals(600.0, result.getAccountBalances()[1], DELTA);
	}

	@Test
	@DisplayName("BN-14: fees n44 -> n59, returned fee")
	void feeReturned() throws Exception {
		TransactionResult result = transfer(2000.0, 500.0, FEE);
		assertEquals(FEE, result.getFees(), DELTA);
	}

	@Test
	@DisplayName("BN-15: newFromAmount n46 -> (n48, n61), insufficient funds")
	void insufficientFunds() throws Exception {
		// not a student, $100 or more, from < $1,000, to < $1,000 -> 0.1% of $100 = 0.1
		TransactionResult result = transfer(50.0, 500.0, 0.1);
		assertFalse(result.isSuccessful());
		verify(db, never()).setBalance(eq(USER), eq(FROM), anyDouble());
		verify(db, never()).setBalance(eq(USER), eq(TO), anyDouble());
	}
}
