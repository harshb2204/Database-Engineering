## Partitioning
![](/diagrams/partitioning1.png)
![](/diagrams/partitioning2.png)

### Vertical vs Horizontal Partitioning

- **Horizontal Partitioning** splits rows into partitions.
    - Examples: Range or list partitioning.
- **Vertical Partitioning** splits columns into partitions.
    - Useful for large columns (like BLOBs) that can be stored in a slower access drive within its own tablespace.

### Partitioning Types

- **By Range**
    - Dates, ids (e.g. by logdate or customerid from to)
- **By List**
    - Discrete values (e.g. states CA, AL, etc.) or zip codes
- **By Hash**
    - Hash functions (consistent hashing)

### Horizontal Partitioning vs Sharding

- **Horizontal Partitioning (HP)** splits a big table into multiple tables in the same database; the client is agnostic.
- **Sharding** splits a big table into multiple tables across multiple database servers.
- In **HP**, table names (or schema) might change.
- In **Sharding**, everything stays the same but the server changes.

---

Partitioning reduces "how much data"
Indexing reduces "how fast we find data"

