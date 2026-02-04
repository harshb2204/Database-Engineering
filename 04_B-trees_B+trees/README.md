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

## Finding a key in a B-Tree

- Start at the **root node** and do a binary search among the keys in the node.
- If the key is **found in the current node**, follow its data pointer (you’re done).
- If the key is **smaller** than a key, follow the pointer to the child **to the left** of that key.
- If the key is **greater than all keys** in the node, follow the **rightmost** child pointer.
- Repeat this process in the child node until you either find the key in a leaf node or reach a null pointer (key not present).



## Limitation B-Tree

- **Elements in all nodes store both the key and the value**
  - In a B-Tree, every node (root, internal, and leaf) contains both the key and its associated data pointer/value.
  - This means internal nodes are not just routing information—they also store actual data, making them larger.
  - Example: In the diagram above, the root node contains "4:704" and "8:802", where both the key (4, 8) and the TID pointer (704, 802) are stored together.

- **Internal nodes take more space thus require more IO and can slow down traversal**
  - Because internal nodes store both keys and values, each internal node occupies more disk space (more bytes per node).
  - When traversing the tree, you need to load larger nodes from disk into memory, which means:
    - More disk I/O operations (reading larger pages)
    - Fewer nodes can fit in the same memory buffer
    - Slower traversal because you're reading more data than necessary just to navigate the tree structure
  - Since internal nodes are only used for navigation, storing data in them is wasteful.

- **Range queries are slow because of random access (give me all values 1-5)**
  - In a B-Tree, data pointers are scattered across different levels (root, internal nodes, and leaf nodes).
  - To retrieve a range of values (e.g., all records with IDs 1-5), you must:
    - Traverse to find each key individually
    - Jump between different nodes at different levels (random disk access)
    - Cannot efficiently scan consecutive leaf nodes because data is not only in leaves
  - This results in many random disk seeks, which are much slower than sequential reads.


- **Hard to fit internal nodes in memory**
  - Because internal nodes contain both keys and values, they are larger than necessary.
  - With limited memory buffers, fewer internal nodes can be cached, leading to:
    - More frequent disk reads during tree traversal
    - Reduced cache hit rates
    - Overall slower query performance
  - B+Tree's smaller internal nodes (keys only) allow more nodes to fit in memory, improving performance.


  ## B+Tree
- Exactly like B-Tree but only stores keys in internal
nodes
- Values are only stored in leaf nodes
- Internal nodes are smaller since they only store
keys and they can fit more elements
- Leaf nodes are “linked” so once you find a key
you can find all values before and after that key.
- Great for range queries

![](/diagrams/bplustree.png)
B+Tree & DBMS Considerations
- Cost of leaf pointer (cheap)
- 1 Node fits a DBMS page (most DBMS)
- Can fit internal nodes easily in memory for fast
-raversal
- Leaf nodes can live in data files in the heap
- Most DBMS systems use B+Tree 