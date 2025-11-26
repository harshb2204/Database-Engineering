## How tables and indexes are stored on disk And how they are queried


## Logical Tables
![](/diagrams/logicaltable.png)

## Row_ID
![](/diagrams/rowid.png)
- Internal and system maintained
- In certain databases (mysql -innoDB) it is the same as the primary key but other databases like  Postgres have a system column row_id (tuple_id)


## Page

![](/images/table1.png)

![](/images/page1.png)

- Depending on the storage model (row vs column store), rows are stored and read in logical pages.
- The database does not read a single row; it reads one or more pages per IO, pulling many rows in that operation.
- Each page has a fixed size (e.g., 8KB in Postgres, 16KB in MySQL).
- If each page holds 3 rows, then 1001 rows require roughly 1001 / 3 ≈ 333 pages.


## IO

- IO operation (input/output) is a read request to the disk.
- We try to minimize IO as much as possible.
- A single IO can fetch one or more pages depending on disk layout and partitioning.
- IO cannot read a single row; it always pulls a page containing many rows.
- Minimizing the number of IOs matters because each disk access is expensive relative to memory.
- Some IOs may be served from the OS cache instead of the physical disk, which is faster but depends on available memory.

## Heap

- The Heap is data structure where the table is stored with all its pages one after another.
- This is where the actual data is stored including everything
- Traversing the heap is expensive as we need to read so may data to find what we want
- That is why we need indexes that help tell us exactly what part of the heap we need to read. What page(s) of the heap we need to pull


## Index

- An index is another data structure separate from the heap that has "pointers" to the heap
- It has part of the data and used to quickly search for something
- You can index on one column or more.
- Once you find a value of the index, you go to the heap to fetch more information where everything is there
- Index tells you EXACTLY which page to fetch in the heap instead of taking the hit to scan every page in the heap
- The index is also stored as pages and cost IO to pull the entries of the index.
- The smaller the index, the more it can fit in memory the faster the search
- Popular data structure for index is b-trees, learn more on that in the b-tree section

## Storage(illustration purposes)
![](/diagrams/disk.png)
10(1,0) -> employee id 10 has rowid 1 and lives in page 0


### Query without index
```  SELECT * FROM EMP WHERE EMP_ID = 10000;
```
![](/diagrams/heap1.png)