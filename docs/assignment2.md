# Assignment 2: White-Box Testing and Fault Localization

CS4472B, group 6. Target: `bank.utils.FeesCalculator`.

| Operation | Technique | Issue | Owner |
|---|---|---|---|
| Withdrawal | Slice on `FinalUse(fee)` + statement coverage | #3 | TODO |
| Deposit | DU-paths for `interestPercentage` + statement coverage | #4 | TODO |
| Transfer | Basis path testing | #5 | TODO |
| Bonus | Any of the three, outside `FeesCalculator` | #6 | TODO |

> **How to use this file.** Fill in only the sections for your issue and replace their `TODO`
> markers. Don't change another operation's section or the shared conventions; raise changes to
> those on #7 instead. Delete this note in the graded PR.

---

## Conventions

These apply to every section and every test class. Questions and changes go on #7.

### Case IDs
- Prefixes: `WD` (withdrawal), `DP` (deposit), `TR` (transfer), `BN` (bonus).
- Two-digit numbers: `WD-01`, `WD-02`, ... The same ID is used in the test-case table, in the
  `@CsvSource` row, in the results table and in the fault-localization table.

### Floating-point tolerance
- One tolerance for every assertion: `DELTA = 1e-6`.
- Declare it in each test class as `private static final double DELTA = 1e-6;`.

### Control-flow graphs
- Number CFG nodes by **source line** in `src/main/java/bank/utils/FeesCalculator.java` as it is
  on `dev`. A node is written `n<line>` (for example `n30`).
- The source is not changed in this assignment, so the line numbers stay fixed.
- Graphs are drawn in Mermaid (` ```mermaid ` blocks) so they render on GitHub.

Line reference:

| Method | Lines | Target statement |
|---|---|---|
| `calculateWithdrawalFee` | 6–31 | `return fee;` (line 30) |
| `calculateDepositInterest` | 33–73 | `double interest = amount * interestPercentage;` (line 70) |
| `calculateTransferFee` | 75–145 | whole method (basis paths) |

### Inputs and oracles
- Expected values come from the **A2 fee figures in the README**, never from what the code returns.
  The A2 figures differ from A1's.
- Every test-case row cites the README clause its expected value comes from.
- `dayOfWeek` uses `java.util.Calendar` constants: 1 = Sunday, 2 = Monday, ..., 7 = Saturday.
- Expected values are fees in dollars, computed as `amount × percentage` (for example 0.2% of $200 = `0.4`).

### Test classes
- JUnit 5, under `src/test/java` in the package of the class under test.
- One class per issue: `bank.utils.WithdrawalFeeTest`, `bank.utils.DepositInterestTest`,
  `bank.utils.TransferFeeTest`. Bonus classes go in the package of the class they test.
- `@ParameterizedTest` + `@CsvSource`, with the case ID as the first column.
- Every row states the structural element it covers, and every test calls the target method.
- Checkstyle must pass: `mvn -B checkstyle:check`.
- Leave the starter `FeesCalculatorTest` alone; #7 decides what to do with it.

Template (replace the placeholders):

```java
package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class XxxFeeTest {
	private static final double DELTA = 1e-6;

	private final FeesCalculator calculator = new FeesCalculator();

	@ParameterizedTest(name = "{0}: {1}")
	@DisplayName("Xxx: <technique>")
	@CsvSource({
		// id, structural element covered, inputs..., expected fee
		"XX-01, '<slice statements / DU-path / basis path>', ..., ...",
	})
	void xxxFee(String id, String covers, /* inputs */ double expected) {
		assertEquals(expected, calculator.calculateXxx(/* inputs */), DELTA);
	}
}
```

### Faults and failure log
- Fault localization: one row per faulty line or condition. Put tests in the same row only if they
  reach the same faulty line.
- Failure log: one failure ID per fault, so it lines up with the fault-localization rows.
- If one test exposes more than one fault, list it on each of those rows.
- Faults found in the same run can share the same times.

### Pull requests
- Short-lived branch off `dev`, PR into `dev`, `phase:tests` label, 3 approvals.
- Quote the JaCoCo numbers from your PR's coverage comment in your section.
- Don't fix faults in the source.

---

## Shared assumptions

1. **Tolerance.** All expected values are compared with `DELTA = 1e-6`, well below one cent and
   far below the gap between any two fee percentages in the specification.
2. **No rounding.** The README tells us to ignore the `round` call for withdrawal and notes
   `Math.round` for deposit. Neither call is in the code: withdrawal returns `amount * feePercentage`
   (line 28) and deposit computes `amount * interestPercentage` (line 70). We analyse the code as it is.
3. **Withdrawal balance of exactly $10,000.** The specification gives a fee for balances below
   $10,000 and for balances above $10,000, but not for exactly $10,000. This is a gap in the
   specification, so no test uses exactly $10,000 as its oracle. Statement coverage of the slice
   does not need that value.
4. **Day of week.** `dayOfWeek` follows `java.util.Calendar`. Saturday (7) and Sunday (1) are the weekend.
5. **Fees are positive amounts in dollars.** Inputs use positive amounts and balances unless a
   section states otherwise.

TODO (#3–#5 owners): add any other assumption your analysis needs here if it affects more than one
operation. Otherwise put it in your own section.

---

## Section 1: Process and assumptions

### 1.1 Withdrawal: slice on `FinalUse(fee)` (#3)

TODO (#3):
- **Slicing criterion:** `FinalUse(fee)` at line 30.
- **The slice:** the statements by line number, each with why it is in the slice (data or control
  dependence on `fee`). Say whether the `if` predicates are included and why.
- **Process:** how the slice was computed.
- **Assumptions** specific to withdrawal.
- **How the test cases cover every statement in the slice.**

### 1.2 Deposit: DU-paths for `interestPercentage` (#4)

TODO (#4):
- **Definitions and use:** every definition of `interestPercentage` (by line) and its use at line 70.
- **DU-path table:**

| DU-path | Def (line) | Use (line) | Path (nodes) | Definition-clear? |
|---|---|---|---|---|
| | | | | |

- **Process:** how the paths were found, and how each path's statements are covered.
- **Assumptions** specific to deposit.

### 1.3 Transfer: basis path testing (#5)

TODO (#5):
- **CFG** in Mermaid, nodes numbered by source line.
- **Cyclomatic complexity** computed three ways (E − N + 2, predicates + 1, regions), with a check
  that they agree.
- **Baseline path** and why it was chosen.
- **Basis set:** each path as a node sequence, derived by flipping one decision at a time.
- **Assumptions** specific to transfer.

### 1.4 Bonus: faults outside `FeesCalculator` (#6)

TODO (#6, or delete this section if #6 is dropped on Nov 30):
- **Class and method** under test, and why it was picked.
- **Technique** and analysis artifact (slice, DU-path table or CFG + basis paths).
- **Oracle source:** the A2 fee figures or the A1 ATM session rules, stated for each test.
- **Test doubles** used, if any (stub `FeesCalculator` so its faults don't show up here as new ones).
- **Assumptions.**

---

## Section 2: Test cases

Columns are the same for every operation. "Covers" names the slice statements, DU-path or basis
path the case was built for. "Spec clause" is the README bullet the expected value comes from.

### 2.1 Withdrawal (#3)

| ID | Covers | `amount` | `accountBalance` | `student` | `dayOfWeek` | Expected fee | Spec clause |
|---|---|---|---|---|---|---|---|
| WD-01 | | | | | | | |

### 2.2 Deposit (#4)

| ID | Covers | `amount` | `accountBalance` | `student` | Expected interest | Spec clause |
|---|---|---|---|---|---|---|
| DP-01 | | | | | | |

### 2.3 Transfer (#5)

| ID | Covers | `amount` | `fromAccountBalance` | `toAccountBalance` | `student` | Expected fee | Spec clause |
|---|---|---|---|---|---|---|---|
| TR-01 | | | | | | | |

### 2.4 Bonus (#6)

| ID | Class#method | Covers | Inputs | Expected | Oracle source |
|---|---|---|---|---|---|
| BN-01 | | | | | |

---

## Test results

Must match the JUnit report on the graded PR's CI run.

| ID | Test class | Result (pass/fail) |
|---|---|---|
| | | |

## Coverage

From the JaCoCo comment on the graded PR's final CI run (#7).

| Class | Line coverage | Branch coverage |
|---|---|---|
| `bank.utils.FeesCalculator` | TODO | TODO |

Per-PR numbers: each owner quotes the JaCoCo numbers from their own PR in their section 1 subsection.

## Fault localization

One row per faulty line or condition (see [Faults and failure log](#faults-and-failure-log)).
Only group tests that reach the same faulty line. Same columns as the PR template.

| Failing test(s) | Suspected fault (file, method, line) | One-line justification |
|---|---|---|
| | | |

## Failure log

Kept for Assignment 3. One failure ID per fault-localization row. Add a row as soon as a failure is
found, not at the end.

| Failure ID | Exposing test(s) | Method | Time testing before found | Time diagnosing | Found by |
|---|---|---|---|---|---|
| F-01 | | | | | |
