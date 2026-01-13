## Full Table Scan

- To find a row in a large table, we perform a **Full Table Scan** (also known as a sequential scan).
- Reading large tables is slow because the database engine must check every single record to find a match.
- This process requires many I/O operations to read all pages from disk, which is often the primary bottleneck.
- We need a more efficient way to **reduce the search space**, which is why data structures like **B-Trees** and **B+ Trees** are used for indexing.
