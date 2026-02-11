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

## Practical
```sql

create table grades_org (id serial not null, g int not null);
insert into grades_org select floor(random() * 1000) from generate_series(0,1000000);

create index grades_org_idx on grades_org(g);

```

### Range Partitioning Example
This example shows how to set up a range-partitioned table.

```sql
-- Step 1: Create the parent table with the partitioning strategy
create table grades_parts (id serial not null, g int not null) partition by range(g);

-- Step 2: Create individual tables for specific ranges
-- (These will store the actual data once attached)
create table g0035 (like grades_parts including indexes);
create table g3560 (like grades_parts including indexes);
create table g6080 (like grades_parts including indexes);
create table g80100 (like grades_parts including indexes);

-- Step 3: Attach the partitions to the parent table
-- This is where we define the actual range boundaries
alter table grades_parts attach partition g0035 for values from (0) to (35);
alter table grades_parts attach partition g3560 for values from (35) to (60);
alter table grades_parts attach partition g6080 for values from (60) to (80);
alter table grades_parts attach partition g80100 for values from (80) to (100);
```

**Explanation & Partitioning Logic:**
The queries above implement **Range Partitioning** on the `g` (grade) column. Here is how the data is partitioned:

*   **Master Table (`grades_parts`)**: This is a declarative partitioned table. It defines the schema and the partitioning key (`g`), but it does not store data directly.
*   **Partition Strategy**: The data is split into segments based on numeric ranges.
*   **Boundary Logic**: In PostgreSQL, the `FROM` value is **inclusive** and the `TO` value is **exclusive**.
*   **Logical Distribution**:
    *   **`g0035`**: Stores values where `0 <= g < 35`.
    *   **`g3560`**: Stores values where `35 <= g < 60`.
    *   **`g6080`**: Stores values where `60 <= g < 80`.
    *   **`g80100`**: Stores values where `80 <= g < 100`.

By using `(like grades_parts including indexes)`, each partition table is guaranteed to have the exact same structure and indexes as the parent. The `ATTACH PARTITION` command is the final step that tells the database exactly which rows belong to which physical table. Once attached, any query or insert onto `grades_parts` will be automatically routed to the correct partition.
