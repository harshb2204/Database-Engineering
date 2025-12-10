## explain command 

### Scenario: Creating and Populating a Grades Table

```sql
-- Step 1: Create the grades table
CREATE TABLE grades (
    id serial primary key, 
    g int,
    name text 
); 

-- Step 2: Insert random data
INSERT INTO grades (g, name) 
SELECT 
    random()*100,
    substring(md5(random()::text), 0, floor(random()*31)::int)
FROM generate_series(0, 500);

-- Step 3: Vacuum the table
VACUUM (ANALYZE, VERBOSE, FULL);
```

---

### Command Explanations

#### 3. `VACUUM (ANALYZE, VERBOSE, FULL)`

 **What VACUUM Actually Does**

PostgreSQL uses MVCC (Multi-Version Concurrency Control) — meaning old row versions remain in the table after updates/deletes.

VACUUM removes those old/dead rows.

When you UPDATE or DELETE, the old row isn't removed immediately.

PostgreSQL marks it as dead.

Over time, tables become full of garbage → performance becomes slow.

VACUUM cleans that garbage.

 **Types of VACUUM**

**1. VACUUM (normal)**

Frees dead rows.

Returns space to reuse within the table (not to the OS).

Keeps table size stable.

Fast, safe, non-blocking.

**2. VACUUM FULL**

You used this.

Completely rewrites the table into a new file.

Removes all unused space.

Shrinks table size physically and returns space to OS.

But locks the table (cannot read/write during the operation).

This is heavy but gives the most cleanup.

**3. ANALYZE**

Also included in your command.

Collects statistics about table contents (row counts, value distributions).

Helps the PostgreSQL query planner choose the fastest execution plan.

**4. VERBOSE**

Shows detailed log messages about what VACUUM is doing internally.

---


![](/images/explain1.png)
- First part is the query plan. Here it is sequential scan. Equivalent to you are selecting everything 
and you have no filter. Go directly to the heap and fetch everything. 
- Sometimes postgres does a parellel sequential scan with threading. They spin up multiple threads, just do the scanning on grades.
- Cost has 2 numbers. First number means how many ms it took me to fetch the first page, here it took 0.
Postgres immediately went to the table and immediately got the result. This can inc if if db decides to do some work before fetching such as aggregating. 
- The second number is the total amount of time it thinks it will take it to fetch the rows.
- Next are the number of rows it says it is going to fetch.It does not know how many rows its going to fetch usually but as i ran analyze it updates the statistics.
- Thats why when you want to do a count and you dont actually care about the actual number like you are counting number of likes on insta. Use this and dont use select count, it will kill your performance
![](/images/explain2.png)

![](/images/explain3.png)
- here we do some work (it is trivial as we indexed g here) but we can see the difference.
![](/images/explain4.png)
- here as we can see work is done and the cost has increased.


![](/images/explain5.png)
- here the width is 4 bytes. id is by default int