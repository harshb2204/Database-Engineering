

![](/images/randomuuids.png)

## The Impact of Random UUIDs on Database Performance

While the image above illustrates the basic concept of page splits, there are deeper architectural reasons why randomness (specifically **UUID v4**) hurts database performance.

### 1. UUID Versions: Random vs. Ordered
*   **UUID v4 (Random):** The most common default. Since these are completely random, two consecutive IDs will be stored in completely different locations in an index.
*   **UUID v1/v7/v8 (Ordered):** These are time-ordered. UUID v7 is increasingly popular because it combines a timestamp with randomness, making it "lexicographically sortable" while remaining unique and secure.
*   **The "Number" Factor:** Databases treat UUIDs as 128-bit numbers (or strings converted to bytes). Since indexes must be ordered to be useful, randomness forces the B-Tree to constantly re-balance itself.

### 2. The Cost of Page Splits & Re-referencing
*   **Leaf Page Links:** In a **B+ Tree**, leaf pages are doubly linked. Inserting a random ID in the middle of a full page requires:
    1.  Creating a new page.
    2.  Moving half the data.
    3.  Updating the prefix/next pointers of neighboring pages.
    4.  Updating internal "parent" nodes to reflect the new structure.
*   **Disk Offsets:** Page numbers correspond to physical offsets on disk. Random inserts lead to high **Seek Time** as the disk head moves across the data file.

### 3. Shared Buffer Pool & "IO Thrashing"
Databases use a **Shared Buffer Pool** (an LRU cache in RAM) to minimize disk I/O.
*   **Sequential Workflow:** If IDs are ordered, you always write to the "tail" page. That page stays "hot" in the buffer pool, making inserts nearly instant.
*   **Random Workflow:** To insert a random UUID, the database must find the correct page. If that page isn't in memory, it must be fetched from disk.
*   **Thrashing:** If the index is large, every random insert might force the database to flush an old page to make room for a new one. If the "old" page is needed again a millisecond later, you enter a cycle of constant disk I/O called **Thrashing**.

### 4. Database-Specific Impacts
*   **MySQL (InnoDB):** Uses **Clustered Indexes** (Index Organized Tables). The table *is* the index. Furthermore, all secondary indexes point to the Primary Key. A random PK "poisons the well," making every single index in your database less efficient.
*   **PostgreSQL:** Uses **Heap Storage** by default. While the table insertion isn't affected by randomness, the **B-Tree indexes** on that table still suffer from the same page split and thrashing issues.

### 5. Real-World Case: Shopify & ULIDs
Shopify transitioned from UUID v4 to **ULIDs** (Universally Unique Lexicographically Sortable Identifiers) for better performance.
*   **Idempotency:** For purchase requests, Shopify uses IDs to prevent double-charging.
*   **Temporal Locality:** Since purchases happen in "time-clumps," using ordered IDs ensures that checks for recent requests hit the same "hot" pages in the buffer pool, dramatically reducing latency during high-traffic events like flash sales.
