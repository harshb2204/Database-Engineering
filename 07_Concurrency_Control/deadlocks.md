# Deadlocks in Databases

## What is a Deadlock?
A deadlock occurs when two or more processes (or clients) are competing for one or more resources, and each process is waiting for another to release a lock on a given resource. Because both processes are waiting on each other, they enter an infinite waiting state where neither can proceed or release their respective locks.

## How Databases Handle Deadlocks
Most modern databases have mechanisms to catch deadlocks when they occur. The database engine actively monitors for these circular waiting states. 

When a deadlock is detected:
* The database intervenes to break the deadlock.
* It selects one of the transactions as the "victim" and rolls it back (fails the transaction). Often, the transaction that entered the deadlock state last is the one chosen to be rolled back.
* The rollback releases the locks held by the victim transaction, allowing the other transaction to proceed.

## Practical Example of a Deadlock (PostgreSQL)

### Setup
* A table named `Test` with a single primary key field. Because it's a primary key, it only accepts unique values.
* Two concurrent database clients, each initiating a transaction (`BEGIN`).

### The Deadlock Scenario
1. **Transaction 1 (T1)**: Executes `INSERT INTO test VALUES (20);`. 
   * *Status*: Succeeds. T1 obtains an exclusive lock on the value `20` but has not committed yet.
2. **Transaction 2 (T2)**: Executes `INSERT INTO test VALUES (21);`. 
   * *Status*: Succeeds. T2 obtains an exclusive lock on the value `21` but has not committed yet.
3. **Transaction 2 (T2)**: Attempts to execute `INSERT INTO test VALUES (20);`. 
   * *Status*: **Blocks**. T2 is now waiting for T1 to either commit or roll back because T1 holds an uncommitted exclusive lock on `20`.
4. **Transaction 1 (T1)**: Attempts to execute `INSERT INTO test VALUES (21);`. 
   * *Status*: **Blocks**. T1 is now waiting for T2 to release its exclusive lock on `21`.

*Result*: Both transactions are now waiting for each other. PostgreSQL detects the deadlock (typically within a second) and automatically rolls back one of the transactions, unblocking the other.

## Regular Blocking vs. Deadlock

It is important to distinguish a deadlock from standard blocking, which is a normal database operation:

* **Scenario**: T1 inserts `20` (uncommitted). T2 then attempts to insert `20`.
* **Outcome**: T2 blocks and waits for T1 to finish. This is **not** a deadlock because T1 is not waiting on T2.
* If T1 **rolls back**, T2 immediately unblocks and successfully inserts `20`.
* If T1 **commits**, T2 unblocks but fails with a "duplicate key" error because the primary key `20` now officially exists.
