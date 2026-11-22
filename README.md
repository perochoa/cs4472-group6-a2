# CS4472 ATM - Assignment Repository

Course project for CS4472B Software Specification, Testing, and Quality
Assurance (Western University). An ATM system backed by a small bank server,
used as the system under test for the course assignments.

See [TESTING.md](TESTING.md) for the required test/fix workflow, pull requests,
and continuous integration. Your team implements the CI workflow specified
there; writing it is your first commit to your own copy of this repository.

Build: Java 11, Maven (`mvn -B test`).

The demo database: the application reads `bank.db` (SQLite), which is intentionally
not committed to the repository. Recreate it any time from the seed script:

```bash
sqlite3 bank.db < bank-seed.sql
```

(The test suite does not need the database; collaborators are stubbed with Mockito.)

---
## Assignment instructions

CS 4472B Software Specification, Testing, and Quality Assurance

GenAI was used to augment learning outcomes.

Setup your CI within the first 3-4 days.

All members should install the GitHub app and a direct messaging app on their phones to keep up
with their teammates and with pull-request approvals, you should aim to accept progress
pull-requests within minutes of their creation, not days!!!

The timeline is intentionally compressed to show that software development is not a "laid back"
activity.

### Assignment 2: White-Box Testing and Fault Localization

Due: Friday, December 4, 2026

#### Course repository

The ATM system under test lives in the public course repository:

<https://github.com/grimasta/WesternCS4472-ATM-Assignment>

Every team works in its own copy of this repository (see [Submission via GitHub](#submission-via-github) below).

#### Group Effort

You will be working in groups of up to 4 people. Work should be split equitably between members of
the group. Everyone should have a good grasp of the techniques used to carry out this assignment, so
try to avoid workload splits where some individuals perform the programming part and others the
write-up. Make an effort to contribute equally (as much as possible) to all parts of the assignment.

#### Deadline

The deadline date is Friday, December 4, 2026, as posted on westernu.brightspace.com. Your graded
pull request must be opened by midnight on the deadline date. The deadline applies to the opening
time of the final pull request. There is no late penalty for this assignment.

If for any reason beyond your control submission is impossible, please contact me as soon as
possible.

#### Introduction

In this assignment you will apply three white-box techniques from the lectures. Your tests are the
deliverable: a test that fails against the faulty code and localizes the fault is a successful
test.

The focal points of your assignment will be again the withdrawal, the deposit, and the transfer
operations. Each of these operations has its own business logic for calculating banking fees for
the given operation. The business logic specification for calculating the fees for each of the
given operations is provided below.

#### Black box vs white box: what changes in this assignment

In Assignment 1 you designed tests without using any knowledge of the implementation: every test
case had to be justified from the specification alone. That is black-box testing, and the
discipline that made it honest was traceability: each test cited the specification clause it came
from. A test whose oracle you could only have invented by reading the code was not a black-box
test, no matter where the code lived.

In this assignment the direction reverses. You will design tests from the structure of the code
itself: its control-flow graph, its definitions and uses of variables, its data dependencies. That
is white-box testing. The discipline that makes it honest is structural justification: each test
cites the basis path, DU-path, or slice element it was built to cover. A test that does not map to
the structure is not a white-box test, even if it passes.

Same system, opposite source of truth. If you can say, for every test you submit, whether its
parent is a spec clause or a code structure, you understand the difference.

#### The business logic: fee requirements

The business logic specification for calculating the fees for each of the given operations is
provided below. These figures are the oracle for the expected fee in your tests: a test's expected
value comes from this specification, not from the code. Read this section before you write any
test.

> **Note:** these are the Assignment 2 figures. Several thresholds and percentages differ from
> Assignment 1; use the figures below, not the ones from Assignment 1.

##### Withdrawal

- If the client is a student and
  - If the transaction is performed during the weekend, then there is no fee.
  - Otherwise, the fee is 0.1% of the amount withdrawn.
- If the client is not a student and
  - If the balance of the account is less than $1,000, then the fee is 0.2% of the amount withdrawn.
  - If, however, the balance is $1,000 or more, but less than $10,000, the fee is 0.1% of the amount withdrawn.
  - If the balance is more than $10,000, then there is no fee.

##### Deposit

- If the client is a student and
  - If the amount deposited is more than $100 and
    - If the balance of the account is more than $1,000, then the fee is 1% of the amount deposited.
    - Otherwise, the fee is 0.5% of the amount deposited.
  - If, however, the amount deposited is $100 or less and
    - If the balance of the account is more than $5,000, then the fee is 0.5% of the amount deposited.
    - Otherwise, there is no fee.
- If the client is not a student and
  - If the amount deposited is more than $500 and
    - If the balance of the account is more than $5,000, then the fee is 1% of the amount deposited.
    - Otherwise, the fee is 0.5% of the amount deposited.
  - If, however, the amount deposited is $500 or less and
    - If the balance of the account is more than $10,000, then the fee is 0.5% of the amount deposited.
    - Otherwise, there is no fee.

##### Transfer

- If the client is a student and
  - If the amount transferred is less than $100 and
    - If the balance of the account the money is coming from is less than $1,000 and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 0.1% of the amount transferred.
      - Otherwise, the fee is 0.05% of the amount transferred.
    - If, however, the balance of the account the money is coming from is $1,000 or more and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 0.5% of the amount transferred.
      - Otherwise, the fee is 0.25% of the amount transferred.
  - If, however, the amount transferred is $100 or more, and
    - If the balance of the account the money is coming from is less than $1,000 and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 0.05% of the amount transferred.
      - Otherwise, the fee is 0.025% of the amount transferred.
    - If, however, the balance of the account the money is coming from is $1,000 or more and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 0.25% of the amount transferred.
      - Otherwise, the fee is 0.125% of the amount transferred.
- If the client is not a student and
  - If the amount transferred is less than $100 and
    - If the balance of the account the money is coming from is less than $1,000 and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 0.2% of the amount transferred.
      - Otherwise, the fee is 0.1% of the amount transferred.
    - If, however, the balance of the account the money is coming from is $1,000 or more and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 1% of the amount transferred.
      - Otherwise, the fee is 0.5% of the amount transferred.
  - If, however, the amount transferred is $100 or more, and
    - If the balance of the account the money is coming from is less than $1,000 and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 0.1% of the amount transferred.
      - Otherwise, the fee is 0.05% of the amount transferred.
    - If, however, the balance of the account the money is coming from is $1,000 or more and
      - If the balance of the account the money is going to is less than $1,000, then the fee is 0.5% of the amount transferred.
      - Otherwise, the fee is 0.25% of the amount transferred.

#### The Testing Targets

You will perform white-box testing on the following class:

- **`bank.utils.FeesCalculator`** - the focus here is to check for the correct calculation of fees
  while performing withdrawal, deposit, and transfer.
  - For withdrawal: compute the slice based on the criterion `FinalUse(fee)` (that is, the
    statement `return fee`), and develop test cases that perform statement coverage on the
    resulting set of statements in the computed slice. For your analysis, you can ignore the call
    to the `round` function.
  - For deposit: compute the DU-paths for the variable `interestPercentage` in the statement
    `int interest = Math.round(amount * interestPercentage)`. For each such path create the test
    cases that perform statement coverage on the resulting set of statements in each such DU-path.
  - For transfer: compute and apply the test cases that result from basis path testing.

> **Note:** the deposit statement as written in the original assignment reads
> `int interest = Math.round(amount * interestPercentage)`. In the code provided, that statement
> reads `double interest = amount * interestPercentage`, with no rounding call. The variable
> `interestPercentage` and its uses are the same either way, so compute the DU-paths from the code
> as it is.

**Bonus.** There may be genuine faults outside this target. Any additional fault you localize with
a failing white-box test (with the same structural justification) earns bonus marks.

#### Test isolation

The target is a pure calculation class. Each of its three methods takes all of its inputs as
parameters, so call the method directly with the values your analysis demands; there is nothing to
stub, and Mockito is not needed for this assignment.

#### What to Run

For the testing target (`bank.utils.FeesCalculator`), run the different test cases you have
generated under the different analysis techniques as JUnit tests. You will have one test bucket
for withdrawal, one for deposit, and one for transfer.

Run everything through the repository's CI as described in [TESTING.md](TESTING.md). Locally:
`mvn -B test` runs the suite, `mvn -B checkstyle:check` runs the linter. The JaCoCo coverage
report (posted on each PR) is your evidence for the coverage claims in your write-up; quote its
numbers.

Every test you submit must execute the `FeesCalculator` method for its operation
(`calculateWithdrawalFee`, `calculateDepositInterest`, or `calculateTransferFee`). A test that
never calls its target does not count toward the coverage your write-up claims.

#### Submission via GitHub

Your graded submission for this assignment is a pull request in your team's own repository, not an
archived file. The full workflow is specified in [TESTING.md](TESTING.md) in the course repository;
read it first. The steps below are the ones specific to Assignment 2.

1. **Set up your team's repository.** Work on a fresh copy: your Assignment 1 repository is being
   graded and must stay unchanged, so do not add Assignment 2 work to it. Clone the course
   repository, then create your own repository for your team (private is fine; add grimasta and
   Felixzhangmingzhe as collaborators so it can be graded) and push the code there. Create a `dev`
   branch; `main` stays protected for the whole term.
2. **Write the CI workflow yourselves.** Your first commit to your repository is
   `.github/workflows/ci.yml`, which you write from scratch. It is not provided. A GitHub Actions
   workflow is a YAML file describing when something runs (triggers) and what runs (jobs). Each job
   runs on a fresh virtual machine (`runs-on: ubuntu-latest`) and is a sequence of steps; the two
   building blocks you need are `actions/checkout` (gives the job your code) and
   `actions/setup-java` (installs Java 11, Temurin distribution), after which a step can run any
   shell command such as `mvn -B checkstyle:check` or `mvn -B test`. Your workflow must implement
   the behaviour specified in TESTING.md: a lint job that always blocks merging, and a tests job
   that reads the PR's label and runs the suite in the mode the label demands. For this assignment
   every pull request carries the `phase:tests` label, so the suite runs with failures posted as a
   comment and not blocking the merge. The required behaviour is the specification; the
   implementation is yours. Start from the GitHub Actions documentation.
3. **Configure branch protection.** On your repository, protect `main` and `dev`: require pull
   requests before merging, require team size - 1 approving reviews (every teammate except the
   author), and dismiss stale approvals when new commits are pushed. The same level of protection
   applies to both branches.
4. **Do the work on short-lived branches off `dev`, and open progress pull requests into `dev`.**
   Push regularly; every pull request runs CI in informational mode. Review progress pull requests
   quickly; aim to accept them within minutes of their creation, not days.
5. **Open the graded pull request.** Open a PR from `dev` into `main`, apply the `phase:tests`
   label, and fill in the PR description using the repository's pull request template. There is no
   fix phase in this assignment; the single `phase:tests` pull request is the whole submission. CI
   will run Checkstyle (must pass), the test suite (failures are posted as a comment and do not
   block merging in this phase; your tests are expected to fail), and a coverage report
   (informational).
6. **Review and merge.** Every teammate except the author approves, then merge. An unmerged PR is
   not a submission. Your graded pull request must be opened by midnight on Friday, December 4,
   2026; the deadline applies to the opening time.
7. **Tell me where it is.** Submit on Brightspace the URL of your team's repository and a link to
   your merged pull request. Without this, your work cannot be found or graded.

#### What to Deliver

Everything you are graded on lives in your merged `phase:tests` pull request:

- **The final report**, as a Markdown document in the pull request (`docs/assignment2.md`). This is
  required. For each operation, it gives the analysis artifact (slice for withdrawal; DU-path table
  for deposit; control-flow graph and basis paths for transfer), the assumptions you made, and the
  test-case tables. In the first section comment on the process and the assumptions you used to
  calculate the test cases, using the analysis technique for the corresponding case. In the second
  section give the tables with the test cases.
- **The test results.** A table in the same document indicating the test case and whether the test
  passed or failed, reflected in the CI run on the PR.
- **Fault localization.** For every test case that failed, state which fault in the code you
  believe it exposes: name the class and method (and, where possible, the specific condition or
  statement) and give a one-sentence justification linking the observed failure to that fault.
  Where several failing test cases expose the same fault, group them under one entry. Use the
  fault-localization table in the pull request template for this.
- **The JUnit code** with which you ran your tests, committed in the PR under `src/test/java`.

Keep a copy of your failure log (failure ID, which test case exposed it, and a rough record of time
spent testing before each failure was found and time spent diagnosing it). You will need this log
in Assignment 3.

#### Important Note

As this is a testing course and we are trying to learn how to write good tests (meaning tests that
fail and thus locate a fault in the code), there will be some faults in the code. You are not
required to fix the faults, as this would alter the results of your tests.
