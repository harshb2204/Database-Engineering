# Multiple Indexes in a Query

When a query contains multiple conditions in the `WHERE` clause, the database can sometimes leverage multiple individual indexes to satisfy the query. This is often referred to as **Index Merge**.

## Case 1: Index Intersection (AND)

Consider a query that filters rows based on two different columns, both of which have their own separate indexes.

### Example Query
```sql
SELECT * FROM T WHERE F1 = 1 AND F2 = 4;
```

### Scenario
- **Index 1:** `F1_IDX` on column `F1`
- **Index 2:** `F2_IDX` on column `F2`

### How the Database Processes the Query
1.  **Parallel Index Scans:** The database engine uses both indexes simultaneously (or sequentially) to find matches for each condition.
    - It searches `F1_IDX` for values where `F1 = 1`.
    - It searches `F2_IDX` for values where `F2 = 4`.
2.  **Retrieving Row IDs:** Each index provides a list of internal Row IDs (pointers to the actual data on disk).
    - **From `F1_IDX`:** row7, row8, row9, row10
    - **From `F2_IDX`:** row7, row22, row12, row10
3.  **Merging (Intersection):** Since the query uses the `AND` operator, the database performs an intersection of these two sets of Row IDs. It keeps only the IDs that appear in both lists.
    - `row7` is in both lists.
    - `row8`, `row9`, `row22`, and `row12` are discarded because they don't satisfy both conditions.
    - `row10` is in both lists.
4.  **Final Result:** The final result set consists of `row7` and `row10`.

### Summary of Case 1
- **Both indexes are used** to narrow down the search.
- The **Row IDs are merged** using an intersection logic.
- This approach is typically used when the resulting dataset is expected to be relatively small compared to the total number of rows.

## Case 2: Using Only One Index

In some scenarios, even if multiple indexes are available, the database optimizer may decide to use only one of them.

### Example Query
```sql
SELECT * FROM T WHERE F1 = 1 AND F2 = 4;
```

### Scenario
- **Index 1:** `F1_IDX` on column `F1`
- **Index 2:** `F2_IDX` on column `F2` (available but potentially not used)

### How the Database Processes the Query
1.  **Selective Index Scan:** The database engine chooses the most "selective" index (the one that will return the fewest rows). In this case, it picks `F1_IDX`.
    - It searches `F1_IDX` for `F1 = 1`.
2.  **Retrieving Row IDs:** It collects the Row IDs matching the condition from the index.
    - **From `F1_IDX`:** row7, row8, row9, row10
3.  **Table Access (Bookmark Lookup):** The engine uses these Row IDs to go directly to the actual table in memory/disk to fetch the full records.
4.  **In-Memory Filtering:** Once the rows are fetched from the table, the database checks the second condition (`F2 = 4`) on the actual data.
    - Row 7: `F1=1, F2=4` -> **Keep**
    - Row 8: `F1=1, F2=8` -> **Discard**
    - Row 9: `F1=1, F2=9` -> **Discard**
    - Row 10: `F1=1, F2=4` -> **Keep**

### Why Choose Case 2 over Case 1?
- **High Selectivity**: If `F1` is a primary key or a highly unique column, the index lookup might return only a handful of rows, making it faster to just fetch and filter them rather than scanning a second index.
- **Low Cost**: Accessing a second index involves additional I/O. If the first index already narrows the results down significantly, the overhead of the second index scan might be higher than the cost of simple filtering.
- **Statistics**: Database statistics might indicate that `F1=1` is very rare, whereas `F2=4` is very common.
