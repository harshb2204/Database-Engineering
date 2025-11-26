## Isolation

- Can my inflight transaction see changes made by other transactions?
- Read phenomena
- Isolation Levels

Isolation determines how concurrently running transactions interact. Stronger isolation reduces anomalies but may lower concurrency, while weaker isolation allows more anomalies but improves throughput.

## Isolation — Read Phenomena

- Dirty reads
- Non-repeatable reads
- Phantom reads
- Lost updates

These anomalies describe the inconsistent behaviors that can occur when transactions read or write overlapping data without sufficient isolation guarantees.

## Dirty Reads Example

**Initial SALES table**

| PID | QNT | PRICE |
| --- | --- | ----- |
| Product 1 | 10 | $5 |
| Product 2 | 20 | $4 |

**Timeline**

1. `TX1` begins and reads the table (`SELECT pid, qnt*price FROM sales`).  
   - Product 1 contributes 50, Product 2 contributes 80 (total $130).
2. Before `TX1` aggregates the total, `TX2` begins and executes `UPDATE sales SET qnt = qnt + 5 WHERE pid = 1`.  
   - Temporary table for Product 1 now shows quantity 15:

   | PID | QNT | PRICE |
   | --- | --- | ----- |
   | Product 1 | 15 | $5 |
   | Product 2 | 20 | $4 |

3. `TX1` re-reads the rows (`SELECT SUM(qnt*price) FROM sales`) and sees $155 because it included the uncommitted +5 units from `TX2`.
4. `TX2` rolls back before committing. The table returns to the committed state with quantity 10 for Product 1:

   | PID | QNT | PRICE |
   | --- | --- | ----- |
   | Product 1 | 10 | $5 |
   | Product 2 | 20 | $4 |

5. `TX1` commits its $155 calculation even though the true committed inventory value is $130. This is a dirty read because `TX1` observed a value that never became durable.

Dirty reads occur when one transaction consumes another transaction’s uncommitted changes.

## Non-repeatable Read Example

**Initial SALES table (committed state)**

| PID | QNT | PRICE |
| --- | --- | ----- |
| Product 1 | 10 | $5 |
| Product 2 | 20 | $4 |

**Timeline**

1. `TX1` begins and reads both rows: Product 1 contributes 50, Product 2 contributes 80 (total $130). It keeps the transaction open while it performs other work.
2. `TX2` begins, executes `UPDATE sales SET qnt = qnt + 5 WHERE pid = 1`, and **commits**. The committed state is now:

   | PID | QNT | PRICE |
   | --- | --- | ----- |
   | Product 1 | 15 | $5 |
   | Product 2 | 20 | $4 |

3. `TX1` re-reads the rows with `SELECT SUM(qnt*price) FROM sales` but inside the same transaction it now observes $155 instead of the $130 it saw earlier.
4. `TX1` eventually commits, but its result set is inconsistent within the same transaction because the data changed mid-flight.

Non-repeatable reads happen when a transaction revisits a row and finds a different committed value due to another transaction modifying and committing that row.
Fixing this is not easy, because now you have to keep a version of the product one at the original state. Thats what postgres does (any updates creates a new version of the row and never changes the same value) while mysql and oracle changes the final value but keeps another table called undo. The undo stack keeps an actual writing to disk, a place where all the prev values (keeps the delta changes). In postgres it will read the original version but mysql will go and read the undo log which is expensive. 

## Phantom Read Example

**Initial SALES table (committed state)**

| PID | QNT | PRICE |
| --- | --- | ----- |
| Product 1 | 10 | $5 |
| Product 2 | 20 | $4 |

**Timeline**

1. `TX1` begins and runs `SELECT pid, qnt*price FROM sales WHERE price >= 4`.  
   - It sees Product 1 ($50) and Product 2 ($80) for a total of $130.
2. `TX2` begins, inserts a brand-new row, and commits:

   `INSERT INTO sales VALUES ('Product 3', 10, 1);`

   The committed table is now:

   | PID | QNT | PRICE |
   | --- | --- | ----- |
   | Product 1 | 10 | $5 |
   | Product 2 | 20 | $4 |
   | Product 3 | 10 | $1 |

3. `TX1` re-runs its range query (`SELECT SUM(qnt*price) FROM sales WHERE price >= 4`) and still expects $130, but depending on the predicate it may now include the new Product 3 row (if the range allows it) and return $140. Even though Product 3 was committed, it “appeared” during the life of `TX1`.
4. `TX1` commits with an inconsistent view of how many rows satisfied the predicate when it began.

Phantom reads occur when a transaction re-executes a range query and finds rows that were inserted (or deleted) by another committed transaction after it first ran.

## Lost Update Example

**Initial SALES table (committed state)**

| PID | QNT | PRICE |
| --- | --- | ----- |
| Product 1 | 10 | $5 |
| Product 2 | 20 | $4 |

**Timeline**

1. `TX1` begins and runs `UPDATE sales SET qnt = qnt + 10 WHERE pid = 1`. It does **not** commit yet, so Product 1 is temporarily 20 inside `TX1`.
2. Before `TX1` commits, `TX2` begins, reads Product 1 as 10 (from the last committed state), and executes `UPDATE sales SET qnt = qnt + 5 WHERE pid = 1`. `TX2` commits, making the committed value 15.
3. `TX1` finally commits, but its update overwrites the committed 15 with 20 (because it still thinks it was applying +10 to the original 10). The net effect of `TX2`’s +5 is gone.
4. When `TX1` reads totals, it sees $155 instead of the $180 that both updates together should have produced.

Lost updates occur when concurrent transactions overwrite each other’s changes because they each based their update on an outdated snapshot.

## Isolation Levels for Inflight Transactions

- **Read uncommitted** – No isolation; any change from other transactions is visible immediately, even if it is not committed yet.
- **Read committed** – Each query inside a transaction sees only data that other transactions have committed.
- **Repeatable read** – Once a row is read, the transaction guarantees that row remains unchanged for the duration of the transaction.
- **Snapshot** – Every query in the transaction sees the database exactly as it looked when the transaction began, similar to taking a snapshot.
- **Serializable** – Transactions behave as if they were executed one after another with no overlap; this prevents all read anomalies but at the cost of concurrency.
- **Each DBMS implements isolation levels differently**, so exact guarantees and performance characteristics can vary between systems.

![](/diagrams/isolationlevels.png)

## Database Implementation of Isolation

- Each DBMS implements isolation levels differently in practice.
- **Pessimistic control** relies on row, table, or page locks to prevent lost updates.
- **Optimistic control** avoids locks, tracks whether data changed, and aborts the transaction if conflicts are detected at commit time.
- Repeatable read often “locks” the rows it touches; PostgreSQL implements repeatable read via snapshots, which is why it avoids phantom reads at that level.
- Serializable isolation is frequently built on optimistic concurrency control, though it can also be emulated pessimistically with `SELECT ... FOR UPDATE`.

## Example: Read Committed vs Repeatable Read (Postgres/MySQL)

```sql
CREATE TABLE test (id integer);
INSERT INTO test VALUES (2);
```

1. `t1` sets isolation level **READ COMMITTED** and begins.
2. `t2` sets isolation level **REPEATABLE READ** and begins.
3. `t1`: `SELECT * FROM test;` → sees `2`.
4. `t2`: `INSERT INTO test VALUES (4);` (not yet committed).
5. `t1`: `SELECT * FROM test;` → still sees only `2` because `t2` has not committed.
6. `t1`: `INSERT INTO test VALUES (5);`
7. `t2`: `SELECT * FROM test;`
   
8. `t1`: `COMMIT;`
9. `t2`: `SELECT * FROM test;`
   
10. `t2`: `COMMIT;`
a) 2 | 2 | 2-4 | 2-4

b) 2 | 2 | 2-4 | 2-4-5

c) 2 | 2-4 | 2-4 | 2-4

 
> **Answer A or B (depending on the platform)** — `2 | 2 | 2-4 | 2-4`.  
> This answer is correct in Postgres and MySQL because REPEATABLE READ isolation is implemented as a snapshot isolation, so phantom reads are not possible. That is why the `5` committed by `t1` won’t be read by `t2`. Meanwhile `2 | 2 | 2-4 | 2-4-5` is correct on platforms that do allow phantom reads under repeatable read (i.e., anything except Postgres and MySQL). Repeatable read only guarantees that once you read a row, that row remains unchanged; it does not universally protect against phantom inserts unless the engine implements snapshot semantics like Postgres/MySQL.

---

**Scenario:** Jeff executed the following update statement on a table with 100 million rows:

```
UPDATE TABLE STUDENTS SET GRADE = 100;
```

He realized he forgot the `WHERE` clause (only student ID 50 should be updated), so he immediately ran:

```
ROLLBACK;
```

Then he re-ran:

```
UPDATE TABLE STUDENTS SET GRADE = 100 WHERE STUDENT_ID = 50;
```

**What is the state of the table after the last statement?**

All the 100 million students will have a grade of 100. Assuming `autocommit = ON`, the first statement ran in its own transaction and committed immediately. Rolling back afterward does nothing because there is no active transaction to undo—the damage is already permanent. The final `UPDATE ... WHERE student_id = 50` simply rewrites the row for student 50 (which already has grade 100). This example highlights why critical bulk updates must run inside explicit transactions when using autocommit.
