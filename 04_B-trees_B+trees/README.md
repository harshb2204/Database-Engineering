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

## Inserting a key into a B-Tree (high level)

- First, **search down the tree** to the leaf node where the new key should go (using the find procedure above).
- Insert the key into the leaf node in **sorted order**.
- If the node now has **at most (m - 1) keys**, we are done.
- If the node has **m keys (overflow)**:
  - Split the node into **two nodes**, each with about half the keys.
  - **Promote** the middle key to the parent node and adjust child pointers.
  - If the parent also overflows, **repeat the split upward**; if the root overflows, a new root is created and the tree height increases by 1.


