# Why Avoid SQL OFFSET for Paging?

When building web applications with APIs that support pagination (e.g., requesting page 10 of news articles where each page has 10 records), it is common to use `OFFSET` and `LIMIT`. However, using `OFFSET` can cause significant performance and consistency issues.



## How `OFFSET` Works

`OFFSET` is designed to fetch and physically drop the first *x* number of rows. 

If a user requests page 10 (assuming 10 items per page), the query translates to `OFFSET 100 LIMIT 10`. The database executes this by:
1. Fetching the first 110 rows.
2. Dropping the first 100 rows.
3. Returning the remaining 10 rows to the user.

## Problems with `OFFSET`

### 1. Performance Degradation
As the offset increases, the database is forced to do more work. Fetching and discarding a large number of rows makes the operation extremely expensive.
- **Low Offset:** `OFFSET 0 LIMIT 10` is fast (e.g., 0.2ms) because it only reads 10 rows.
- **High Offset:** `OFFSET 100000 LIMIT 10` is slow (e.g., 79ms or even seconds on a cold start) because the database has to pull 100,010 rows from the index, just to discard 100,000 of them and return 10.
- **Resource Exhaustion:** This requires reading vast amounts of rows into memory, wasting I/O and CPU cycles. In databases like SQL Server, this could even trigger lock escalation, severely impacting concurrency.

### 2. Inconsistent Results (Duplicate Records)
`OFFSET` can accidentally return duplicate records or skip records entirely if the underlying data changes while the user is paging.
- **Scenario:** A user requests page 11 (`OFFSET 110`). Meanwhile, someone inserts a new record into the table. The offset of 110 now points to a shifted set of data. The user might see a row they already read on page 10 because the new insertion pushed the older rows down the result set.

## The Solution: Keyset / Cursor Pagination

To fix performance and consistency issues, **avoid `OFFSET` entirely and simulate paging using an indexed column (like an auto-incrementing `ID` or a timestamp).**

Instead of asking the database to skip rows, ask it to fetch rows that come *after* (or *before*) the last row you saw.

### How it works
1. **Initial Query:** Ask for the first 10 rows.
   ```sql
   SELECT id, title FROM news ORDER BY id DESC LIMIT 10;
   ```
2. **Client tracks the last seen ID:** The API returns the data, and the client remembers the `ID` of the very last record it received.
3. **Next Page Query:** The client asks for the next 10 rows, passing the last seen `ID`.
   ```sql
   SELECT id, title FROM news 
   WHERE id < [last_seen_id] 
   ORDER BY id DESC 
   LIMIT 10;
   ```

### Why is this better?
- **O(1) Performance:** The database uses the index to jump directly to the `last_seen_id`. It only ever reads exactly 10 rows from the index. Whether you are on page 1 or page 10,000, the query takes roughly the same amount of time.
- **Consistency:** Because you are filtering based on a specific anchor point (`id < last_seen_id`), new records inserted at the top of the table will not shift your paging window. You will not see duplicate records.
