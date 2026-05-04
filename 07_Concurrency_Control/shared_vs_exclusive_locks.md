# Shared vs Exclusive Locks

In database systems, concurrency control mechanisms often utilize locks to maintain data consistency and isolation during simultaneous transactions. Two of the most fundamental types of locks are **Shared Locks** and **Exclusive Locks**.

## Exclusive Lock (Write Lock)
An exclusive lock is acquired when a transaction needs to modify (update, delete, or insert) a piece of data (like a row or a column).
* **Purpose**: To guarantee that no other transaction can read or write the data while it is being modified. This prevents other connections from reading an intermediate or "dirty" value.
* **Behavior**: Once an exclusive lock is acquired on a value, if any other connection attempts to read or edit that value, they will encounter an error or have to wait until the lock is released.
* **Analogy**: You are editing a document and you lock the file entirely so no one can even open it to view the older version until you are done.

## Shared Lock (Read Lock)
A shared lock is acquired when a transaction wants to read a piece of data and ensure that the data does not change during the read operation.
* **Purpose**: To guarantee a consistent view of the data, especially during long-running read operations like reporting or complex queries. It ensures the value remains unchanged for the duration of the transaction.
* **Behavior**: If a shared lock is acquired on a value, any attempts by other transactions to edit (acquire an exclusive lock) that value will fail or be blocked. However, multiple transactions *can* acquire multiple shared locks on the same data concurrently.
* **Analogy**: A document is put in a "read-only" display case. Many people can look at it at the same time, but no one is allowed to take it out to modify it.

## The Relationship
Shared and exclusive locks are highly interlinked and have strict rules regarding their coexistence on the same piece of data:
1. **To obtain an Exclusive Lock:** There must be **zero** Shared Locks or Exclusive Locks currently held on that data.
2. **To obtain a Shared Lock:** There must be **zero** Exclusive Locks currently held on that data. (However, any number of Shared Locks can exist).

If a value has 7 shared locks on it from different reporting transactions, no transaction can acquire an exclusive lock to update that value until all 7 shared locks are released. Conversely, if there is an exclusive lock, nobody can read it (no shared locks can be obtained).

## Example Scenario

1. **Alice** starts a transaction to deposit $200 into her account. She attempts to acquire an **Exclusive Lock** on her balance. Since no other locks are present, it succeeds. She updates the balance and commits, releasing the lock.
2. **Alice** then starts a long-running reporting job (read operation). She acquires a **Shared Lock** on her account balance so it doesn't change while she reads.
3. Meanwhile, **Bob** starts his own reporting job on his account, successfully acquiring a **Shared Lock** on his account.
4. **Charlie** comes along and wants to transfer $300 to Bob's account. This requires modifying Bob's account, so Charlie attempts to obtain an **Exclusive Lock** on Bob's account.
5. **Charlie's attempt fails** because Bob currently holds a Shared Lock on his own account. The system prevents Charlie from editing the balance while Bob is actively reading it.
6. Once Bob's reporting job finishes, he releases the Shared Lock.
7. Charlie can now successfully acquire the **Exclusive Lock** and complete the $300 transfer.

## Advantages and Disadvantages

### Advantages
* **Ensures Data Consistency**: Prevents dirty reads and lost updates. Crucial for critical systems like banking where accurate balances are paramount.
* **Reliable Configurations**: In central configuration management, exclusive locks ensure that while configurations are being updated, clients cannot read stale or partially updated configurations. They wait until the update is finished to read the "latest and greatest" state.

### Disadvantages
* **Concurrency Suffers**: The primary drawback is reduced concurrency. Transactions will frequently block or fail because locks are held by others.
* **Availability Issues**: During heavy read periods (e.g., midnight batch reporting jobs in banks acquiring many shared locks), normal write transactions (like a user trying to transfer money) may be blocked, leading to a degraded user experience.