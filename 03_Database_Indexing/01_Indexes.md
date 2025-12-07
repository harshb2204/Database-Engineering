

## Index
Index is a data structure that you build and assign upon an existing table that looks through your table and tries to analyze it so that it can create kind of shortcuts. 

### Scenarios 

![](/images/emptable.png)

### Example: Inserting Data and Query Performance

```sql
-- Insert 1 million rows into employees table
INSERT INTO employees (name) 
SELECT substr(md5(random()::text), 1, 10) 
FROM generate_series(1, 1000000);
```

**Output:**
```
INSERT 0 1000000
```

### Query with Index

```sql
EXPLAIN ANALYZE 
SELECT id FROM employees WHERE id = 2494041;
```

**Query Plan:**
![](/images/explainanalyze.png)

**Explanation:**
- **Index Only Scan**: The query uses the primary key index (`employees_pkey`) to find the row
- **Execution Time: 0.041 ms**: Extremely fast lookup thanks to the index
- **Heap Fetches: 1**: Only one page fetch needed from the heap
- This demonstrates how indexes dramatically improve query performance on large tables

![](/images/explainanalyze1.png)

### Index-Only Scan vs Index Scan

#### 1. Query: `SELECT id ...`

**Why?**

You're selecting ONLY `id`, which is stored inside the index itself.

So PostgreSQL tries to use **Index-Only Scan**.

But it needed to check row visibility → so it touched the heap once → shows `Heap Fetches: 1`.

This is normal.

#### 2. Query: `SELECT name ...`

**Why?**

Because now PostgreSQL CANNOT do an Index-Only Scan.

 **Reason:**

The index (`employees_pkey`) stores only the `id`, not the `name`.

So PostgreSQL must:
1. Find the row using the index → **Index Scan**
2. Fetch the actual row (heap) to read the `name` value

Since it's not an index-only scan, it does not show `Heap Fetches` — that line only appears for index-only scans.


![](/images/explainanalyze2.png)

**Query without Index on Filtered Column:**

```sql
EXPLAIN ANALYZE 
SELECT id FROM employees WHERE name = '91e2ab775c';
```

**Key Points:**
- **Parallel Seq Scan**: No index on `name` column, so PostgreSQL must scan the entire table sequentially
- **Parallel Workers**: Uses 2 parallel workers to scan different portions of the table
- **Rows Removed by Filter**: Scans all 1 million rows, filtering out 333,333 rows per worker
- This demonstrates the critical importance of indexes on frequently queried columns

### Why LIKE Queries are Slow

**LIKE queries are slow because:**

- **Pattern Matching**: Must evaluate the pattern against every row, even with an index
- **Index Limitations**: 
  - `LIKE 'prefix%'` can use index (prefix match)
  - `LIKE '%suffix'` cannot use index (must scan all rows)
  - `LIKE '%middle%'` cannot use index (must scan all rows)
- **Full Table Scan**: Queries with leading wildcards (`%...`) force sequential scans
- **Expensive Operations**: Pattern matching requires string comparison on every row, which is CPU-intensive

**Example:**
```sql
-- Fast: Can use index
SELECT * FROM employees WHERE name LIKE '91e2%';

-- Slow: Must scan entire table
SELECT * FROM employees WHERE name LIKE '%775c';
```