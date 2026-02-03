## Full Table Scan

- To find a row in a large table, we perform a **Full Table Scan** (also known as a sequential scan).
- Reading large tables is slow because the database engine must check every single record to find a match.
- This process requires many I/O operations to read all pages from disk, which is often the primary bottleneck.
- We need a more efficient way to **reduce the search space**, which is why data structures like **B-Trees** and **B+ Trees** are used for indexing.
 
## B-Tree

- Balanced Data structure for fast traversal
- B-Tree has Nodes
- In B-Tree of "m" degree some nodes can have (m) child nodes
- Node has up to (m-1) elements

## B-Tree details

- Each element has a key and a value
- The value is usually data pointer to the row
- Data pointer can point to primary key or tuple
- Root Node, internal node and leaf nodes
- A node = disk page


![](/diagrams/btree.png)

![](/diagrams/btree2.png)


