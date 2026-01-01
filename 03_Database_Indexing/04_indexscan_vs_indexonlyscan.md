# Index Scan vs. Index Only Scan

![](/images/normalscan.png)

When we run `EXPLAIN ANALYZE` for the query `SELECT name FROM grades WHERE id = 7;` on a table without an index on the `id` column, the database performs a **Parallel Sequential Scan**. 

Since there is no index, the database must scan the entire table row-by-row to find the matching ID. This is essentially a "Table Scan" and is generally slow for large datasets because it requires reading all pages of the table from disk.

```sql
CREATE INDEX id_idx ON grades(id);
```

![](/images/indexscan3.png)

After creating an index on the `id` column, the same query results in an **Index Scan**. 

In an Index Scan, the database uses the `id_idx` index to quickly locate the entry where `id = 7`. However, the query asks for the `name` column, which is not stored in this specific index. To retrieve the name, the database must use the row reference (TID) found in the index to jump back to the actual table (the heap) and fetch the data. This extra trip to the table is what characterizes a standard Index Scan.



### Index Only Scan

An **Index Only Scan** is a highly efficient operation where the database retrieves all the required data directly from the index, avoiding a trip to the table (heap).

To achieve this, we can use the `INCLUDE` clause to store additional non-key columns in the index. In this example, we drop the old index and create a new one that includes the `name` column:

```sql
DROP INDEX id_idx;
CREATE INDEX id_idx ON grades(id) INCLUDE (name);
```

Now, when we query for the `name` using the `id`, the database can find everything it needs in the index:

```sql
EXPLAIN ANALYZE SELECT name FROM grades WHERE id = 7;
```

![](/images/indexonlyscan.png)

As shown in the output, the database performs an **Index Only Scan**. Because the `name` column is now stored in the index (thanks to the `INCLUDE` clause), the database doesn't have to fetch the actual row from the table heap. 


