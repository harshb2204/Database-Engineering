# Key vs Non-Key Columns in PostgreSQL

## Overview

When creating an index in PostgreSQL (or any other database), you specify one or more **Key Columns**. These columns are used to create the B-Tree structure and are utilized for searching purposes. 

### Key vs Non-Key Indexes
- **Key Columns**: These are the primary search criteria. The database planner uses these to traverse the index tree. The leaf nodes contain pointers (TIDs) back to the actual table pages (heap) to fetch other columns.
- **Non-Key Columns (Including Columns)**: These are additional columns stored in the leaf nodes of the index using the `INCLUDE` clause. They are not part of the search tree structure but allow the database to perform an **Index-Only Scan**, as the required data can be retrieved directly from the index without visiting the heap.

---

## Experimentation



### Create the Schema
We create a `students` table with a primary key and several data columns to simulate a realistic wide table.

```sql
create table students (
    id serial primary key, 
    g int,
    firstname text, 
    lastname text, 
    middlename text,
    address text,
    bio text,
    dob date,
    id1 int,
    id2 int,
    id3 int,
    id4 int,
    id5 int,
    id6 int,
    id7 int,
    id8 int,
    id9 int
);
```

## Generate Large Dataset
Insert 50 million rows of randomized data to test index performance at scale.

```sql
insert into students (
    g, firstname, lastname, middlename, address, bio, dob,
    id1, id2, id3, id4, id5, id6, id7, id8, id9
) 
select 
    random()*100,
    substring(md5(random()::text), 0, floor(random()*31)::int),
    substring(md5(random()::text), 0, floor(random()*31)::int),
    substring(md5(random()::text), 0, floor(random()*31)::int),
    substring(md5(random()::text), 0, floor(random()*31)::int),
    substring(md5(random()::text), 0, floor(random()*31)::int),
    now(),
    random()*100000, random()*100000, random()*100000,
    random()*100000, random()*100000, random()*100000,
    random()*100000, random()*100000, random()*100000
from generate_series(0, 50000000);
```

### Maintenance
Run `VACUUM` to compact storage and update statistics for the query planner.

```sql
vacuum (analyze, verbose, full);
```

###  Query Performance Analysis
Analyze how the database handles a query that selects both a search key (`g`) and another column (`id`).

```sql
explain analyze select id, g from students where g > 80 and g < 95 order by g;
```

![](/images/keyvsnonkey.png)
- It had to do a sort (due to order by)
- Didnt have any index on the g field, so it had to do a parallel seq scan (hit the disk directly)
- We also have to filter it out (g > 80 and g < 95)
![](/images/keyvsnonkey2.png)
- Also had to eliminate many rows.

```sql
create index idx_students_g on students(g);
```
- so that we can order by the g and filter on the g. Indexes are by default ordered. Randomness is chaos. 


```sql
explain analyze select id, g from students where g > 80 and g < 95 order by g;
```
- for this query again we cant guarantee that it will use the index. id is in the index and also in the heap as another field. 
![](/images/keyvsnonkey3.png)
- still slow. 
- in subsequent queries it might get faster as they are cached. 
```sql
create index idx_students_g on students(g) include (id);
```
![](/images/keyvsnonkey4.png)
- now it took only 4 seconds .
- In this index only scan we did not have to go to the heap



