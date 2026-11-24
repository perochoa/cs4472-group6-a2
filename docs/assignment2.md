# Assignment 2: White-Box Testing and Fault Localization

CS4472B, group 6. Target: `bank.utils.FeesCalculator`.

| Operation | Technique | Issue | Owner |
|---|---|---|---|
| Withdrawal | Slice on `FinalUse(fee)` + statement coverage | #3 | Tony Wang (@twang849) |
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

**Slicing criterion:** `FinalUse(fee)`, the statement `return fee;` at line 30 (`n30`) of
`calculateWithdrawalFee`. This is a static backward slice: it holds every statement that can
change the value of `fee` when it is returned.

**The slice:** `S(fee, n30) = { n6, n10, n12, n13, n15, n18, n19, n20, n21, n23, n28, n30 }`

| Node | Statement | Why it is in the slice |
|---|---|---|
| n6 | method entry: parameters `amount`, `accountBalance`, `student`, `dayOfWeek` | Data: defines `amount` (used at n28) and the variables read by the predicates n10, n12, n18, n20 |
| n10 | `if (student)` | Control: decides whether n12 or n18 runs |
| n12 | `if ((dayOfWeek == Calendar.SATURDAY) \|\| (dayOfWeek == Calendar.SUNDAY))` | Control: decides whether n13 or n15 runs |
| n13 | `feePercentage = 0.0;` | Data: definition of `feePercentage` that reaches n28 |
| n15 | `feePercentage = 0.001;` | Data: definition of `feePercentage` that reaches n28 |
| n18 | `if (accountBalance < 1000.00)` | Control: decides whether n19 or n20 runs |
| n19 | `feePercentage = 0.002;` | Data: definition of `feePercentage` that reaches n28 |
| n20 | `else if (accountBalance < 10000.00)` | Control: decides whether n21 or n23 runs |
| n21 | `feePercentage = 0.001;` | Data: definition of `feePercentage` that reaches n28 |
| n23 | `feePercentage = 0.0;` | Data: definition of `feePercentage` that reaches n28 |
| n28 | `double fee = amount * feePercentage;` | Data: the only definition of `fee`, which n30 uses |
| n30 | `return fee;` | The slicing criterion |

**Are the `if` predicates in the slice? Yes.** The four predicates (n10, n12, n18, n20) do not
assign `fee` or `feePercentage`, but they choose which of the five assignments to `feePercentage`
runs. Each assignment is control dependent on its predicate, so the predicates are in the slice.

**Not in the slice:** line 8 (`double feePercentage;`) declares the variable but gives it no value,
so it cannot change `fee`. Every other line that is not listed is a comment, a blank line, a
closing brace or an `else` keyword, not a statement. So every executable statement in the method
except line 8 is in the slice.

**Process.** We started at n30 and walked backwards, adding the reaching definitions of every
variable used (data dependence) and the predicate that controls every statement (control
dependence), until no new statement was added:

| Step | Statement in the slice | Uses | Added | Reason |
|---|---|---|---|---|
| 1 | n30 `return fee` | `fee` | n28 | Only definition of `fee` (data) |
| 2 | n28 `fee = amount * feePercentage` | `amount`, `feePercentage` | n6, n13, n15, n19, n21, n23 | Parameter `amount`; all five definitions of `feePercentage` reach n28 (data) |
| 3 | n13, n15 | — | n12 | Weekend predicate controls them (control) |
| 4 | n19 | — | n18 | `accountBalance < 1000` controls it (control) |
| 5 | n21, n23 | — | n20 | `accountBalance < 10000` controls them (control) |
| 6 | n12, n18, n20 | `dayOfWeek`, `accountBalance` | n10 | `if (student)` controls n12 and n18; n18 controls n20 (control). Variables come from n6 (data) |
| 7 | n10 | `student` | — | n6 is already in the slice; nothing new, so stop |

**Assumptions (withdrawal).**
- The README says to ignore the `round` call; there is no `round` call in the method. Line 28
  returns `amount * feePercentage` with no rounding (Shared assumption 2).
- A balance of exactly $10,000 is a gap in the specification (Shared assumption 3). No test uses
  it, and statement coverage of the slice does not need it.
- `dayOfWeek` follows `java.util.Calendar`: 1 = Sunday, 2 = Monday, 7 = Saturday (Shared assumption 4).
- Every test withdraws `amount = 200`, so each non-zero fee is exact and different
  (0.1% → 0.2, 0.2% → 0.4). A wrong percentage gives a different result, well above `DELTA = 1e-6`.
- Balances are chosen in the middle of each range (500, 5,000, 20,000) so that each test reaches
  exactly one assignment, with no doubt about which range it is in.

**How the test cases cover every statement in the slice.** Each run executes exactly one of the five
`feePercentage` assignments, so at least five tests are needed. WD-01 to WD-05 run one each. Every
test also runs n6, n10, n28 and n30, and together they run every predicate.

| Node | WD-01 | WD-02 | WD-03 | WD-04 | WD-05 | WD-06 |
|---|---|---|---|---|---|---|
| n6 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| n10 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| n12 | ✓ | ✓ | | | | ✓ |
| n13 | ✓ | | | | | ✓ |
| n15 | | ✓ | | | | |
| n18 | | | ✓ | ✓ | ✓ | |
| n19 | | | ✓ | | | |
| n20 | | | | ✓ | ✓ | |
| n21 | | | | ✓ | | |
| n23 | | | | | ✓ | |
| n28 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| n30 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

All 12 slice statements are covered by WD-01 to WD-05 (100% statement coverage of the slice).
WD-06 is an extra case: it runs the same statements as WD-01 but makes the Sunday side of the `||`
on n12 true, so both weekend days in the specification are checked.

**Results.** All six withdrawal tests pass (CI run on PR #10, JUnit test report:
`WithdrawalFeeTest.withdrawalFee(...)[1]` to `[6]` all passed). The JUnit report names the
parameterized cases by position, not by ID: `[n]` is WD-0n (for example, `[1]` is WD-01). The code matches the A2 withdrawal figures for every slice statement, so no
fault was found in `calculateWithdrawalFee` and there are no withdrawal rows in the
fault-localization table or the failure log.

**JaCoCo (CI run on PR #10, `jacoco-report` artifact).** The PR comment shows only the project
total (4.88%), so these numbers come from the `jacoco.xml` in the run's `jacoco-report` artifact:

| Scope | Line coverage | Branch coverage |
|---|---|---|
| `calculateWithdrawalFee` | 11/11 (100%) | 10/10 (100%) |
| `bank.utils.FeesCalculator` (whole class) | 12/62 (19.35%) | 10/54 (18.52%) |

Every line and branch of `calculateWithdrawalFee` runs. The class totals are low only because
the deposit and transfer methods have no tests yet in this PR.

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
| WD-01 | n6, n10, n12, n13, n28, n30 | 200 | 500 | true | 7 (Sat) | 0.0 | Student → weekend → "there is no fee" |
| WD-02 | n6, n10, n12, n15, n28, n30 | 200 | 500 | true | 2 (Mon) | 0.2 | Student → otherwise → "0.1% of the amount withdrawn" |
| WD-03 | n6, n10, n18, n19, n28, n30 | 200 | 500 | false | 2 (Mon) | 0.4 | Not a student → balance less than $1,000 → "0.2% of the amount withdrawn" |
| WD-04 | n6, n10, n18, n20, n21, n28, n30 | 200 | 5000 | false | 2 (Mon) | 0.2 | Not a student → $1,000 or more but less than $10,000 → "0.1% of the amount withdrawn" |
| WD-05 | n6, n10, n18, n20, n23, n28, n30 | 200 | 20000 | false | 2 (Mon) | 0.0 | Not a student → balance more than $10,000 → "there is no fee" |
| WD-06 | n6, n10, n12, n13, n28, n30 (extra: Sunday side of `\|\|` on n12) | 200 | 500 | true | 1 (Sun) | 0.0 | Student → weekend → "there is no fee" |

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

In the CI JUnit report, the withdrawal cases appear as `WithdrawalFeeTest.withdrawalFee(...)[n]`,
where `[n]` is WD-0n (`[1]` = WD-01 through `[6]` = WD-06).

| ID | Test class | Result (pass/fail) |
|---|---|---|
| WD-01 | `bank.utils.WithdrawalFeeTest` | pass |
| WD-02 | `bank.utils.WithdrawalFeeTest` | pass |
| WD-03 | `bank.utils.WithdrawalFeeTest` | pass |
| WD-04 | `bank.utils.WithdrawalFeeTest` | pass |
| WD-05 | `bank.utils.WithdrawalFeeTest` | pass |
| WD-06 | `bank.utils.WithdrawalFeeTest` | pass |

## Coverage

From the JaCoCo comment on the graded PR's final CI run (#7).

| Class | Line coverage | Branch coverage |
|---|---|---|
| `bank.utils.FeesCalculator` | TODO | TODO |

Per-PR numbers: each owner quotes the JaCoCo numbers from their own PR in their section 1 subsection.

## Fault localization

One row per fault. Group every failing test that exposes the same fault in one row. Same columns
as the PR template.

| Failing test(s) | Suspected fault (file, method, line) | One-line justification |
|---|---|---|
| | | |

## Failure log

Kept for Assignment 3. Add a row as soon as a failure is found, not at the end.

| Failure ID | Exposing test(s) | Method | Time testing before found | Time diagnosing | Found by |
|---|---|---|---|---|---|
| F-01 | | | | | |
