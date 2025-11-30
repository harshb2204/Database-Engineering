# What is a Transaction?

## Transaction

- A collection of queries
- One unit of work
- E.g. Account deposit (SELECT, UPDATE, UPDATE)

A transaction groups multiple database operations together, ensuring they execute as a single atomic unit. This means either all operations succeed together, or if any fails, the entire transaction is rolled back to maintain data consistency.

## Transaction Lifespan

- **Transaction BEGIN**
- **Transaction COMMIT**
- **Transaction ROLLBACK**
- **Transaction unexpected ending = ROLLBACK (e.g. crash)**

A transaction starts with BEGIN, executes its operations, and ends with either COMMIT (success) or ROLLBACK (failure or explicit cancellation). Any unexpected termination, such as a system crash, automatically triggers a rollback to prevent partial data changes.

## Nature of Transactions

- Usually Transactions are used to change and modify data
- However, it is perfectly normal to have a read only transaction
- Example, you want to generate a report and you want to get consistent snapshot based at the time of transaction
- Consisten Snapshot means ->The transaction sees the database exactly as it was when the transaction started, even if other transactions are making changes afterward.

## Transaction Example: Money Transfer

**Scenario:** Send $100 From Account 1 to Account 2

**Initial State:**
- Account 1: $900
- Account 2: $600

**Transaction Flow:**

```
BEGIN TX1
  SELECT BALANCE FROM ACCOUNT WHERE ID = 1
  (Check: BALANCE > 100)
  UPDATE ACCOUNT SET BALANCE = BALANCE - 100 WHERE ID = 1
  UPDATE ACCOUNT SET BALANCE = BALANCE + 100 WHERE ID = 2
COMMIT TX1
```

**Result:**
- Account 1: $800
- Account 2: $700

This example demonstrates how a transaction ensures atomicity: both account updates must succeed together. If either UPDATE fails, the entire transaction rolls back, maintaining the original balances and preventing partial transfers.
