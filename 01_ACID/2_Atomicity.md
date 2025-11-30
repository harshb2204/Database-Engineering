## Atomicity

- All queries in a transaction must succeed.
- If one query fails, every prior successful query in the transaction should roll back.
- If the database crashes before the transaction commits, all successful queries in that transaction must roll back.

Atomicity guarantees that a group of operations behaves like a single indivisible action: either everything is persisted, or none of it is.

## Why Atomicity Matters

- After a failure or restart, one account might be debited while the other is never credited.
- This leaves the system in an inconsistent state and effectively loses money.
- An atomic transaction rolls back all queries if any query fails, keeping balances correct.
- The database engine should automatically clean up partial work after it restarts.

Atomicity protects the system from partial updates and ensures that after any failure, the database returns to a consistent state as if the failed transaction never happened.

## Failed Transfer Example

**Goal:** Send $100 from Account 1 to Account 2.

1. `BEGIN TX1`
2. `UPDATE account SET balance = balance - 100 WHERE id = 1` (Account 1 becomes $800)
3. System crashes before the credit step (`UPDATE account SET balance = balance + 100 WHERE id = 2`)
4. Transaction never reaches `COMMIT`

**Result without atomicity:**  
Account 1 shows $800 while Account 2 still shows $600. Money appears to vanish because only one half of the transfer succeeded.

**Atomic behavior:**  
On restart, the database detects the uncommitted transaction and rolls the debit back, restoring Account 1 to $900 and leaving Account 2 at $600. The transfer can then be safely retried.

