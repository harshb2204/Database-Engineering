# Two-Phase Locking (2PL)

Two-Phase Locking is a concurrency control method in databases that handles acquiring and releasing locks in two distinct phases to ensure serializability.

## The Two Phases

1.  **Phase 1: Acquire (Growing Phase)**
    *   The transaction acquires all the locks it needs (e.g., shared or exclusive locks).
    *   *Rule:* The transaction can acquire locks but cannot release any locks.
2.  **Phase 2: Release (Shrinking Phase)**
    *   The transaction releases its locks (typically upon commit or rollback).
    *   *Rule:* Once a transaction releases its first lock, it cannot acquire any new locks.

## Practical Example: The Double Booking Problem

### The Problem
Imagine a cinema booking system. Two users attempt to book the exact same seat (e.g., Seat 13) at the exact same millisecond.

*   **Without Locking:** Both transactions concurrently check the database (`SELECT`) and see that the seat is available. They both execute an `UPDATE` to claim the seat for their respective users and commit. The transaction that commits last overwrites the first one. Both users think they successfully booked and paid for the same seat, resulting in a **double booking**.

### The Solution with Two-Phase Locking
We can solve this by using Two-Phase Locking to obtain an **Exclusive Lock** on the row before updating it.

**Transaction 1 (Hussein books Seat 14):**
1.  **Acquire Lock (Phase 1):** Transaction 1 executes a `SELECT ... FOR UPDATE`.
    ```sql
    BEGIN TRANSACTION;
    SELECT * FROM seats WHERE id = 14 FOR UPDATE;
    ```
    The `FOR UPDATE` clause instructs the database to obtain an exclusive lock on the row for Seat 14.
2.  **Update:** Transaction 1 safely updates the row since it holds the lock.
    ```sql
    UPDATE seats SET is_booked = 1, name = 'Hussein' WHERE id = 14;
    ```
3.  **Commit & Release (Phase 2):** Transaction 1 commits the changes. The moment the transaction commits (or rolls back), it enters the release (shrinking) phase, instantly freeing the exclusive lock.
    ```sql
    COMMIT;
    ```

**Transaction 2 (Edmund concurrently books Seat 14):**
1.  Transaction 2 attempts to check the same seat using `SELECT ... FOR UPDATE`.
2.  **Blocked:** Because Transaction 1 already holds an exclusive lock on that specific row, Transaction 2 is blocked and forced to wait. It cannot obtain the lock.
3.  **Unblocked:** Once Transaction 1 commits and releases its lock, Transaction 2 is unblocked.
4.  **Rejected:** It reads the newly committed data, sees that the seat is now booked (`is_booked = 1`), and the application can safely throw an error rejecting the second booking attempt.
