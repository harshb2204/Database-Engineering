# Working with Billion-Row Tables — Lecture Notes

## 1. Core Question in System Design
When designing a database:
- Will this table grow over years?
- Can it reach hundreds of millions or billions of rows?

Table growth must be anticipated early, not treated as an afterthought.

## Approaches to Handling Huge Tables

## 2. Brute Force Processing (Big Data Style)
**Idea:** Process the entire table in parallel.

**How it works:**
- Split the table into chunks
- Process chunks concurrently using:
    - Multi-threading
    - Multi-processing
    - Distributed systems (e.g., Hadoop, MapReduce)

**Used when:**
- You cannot reduce the dataset size
- Batch processing / analytics workloads

**Limitations:**
- Inefficient for transactional databases
- Data may change during processing
- Expensive infrastructure (many machines)

## 3. Avoid Scanning the Entire Table
Instead of scanning billions of rows, reduce the search space.

### 3.1 Indexing
**What it does:**
- Creates a data structure (B-tree / LSM-tree) on disk
- Narrows down search to a small subset of rows



**Benefits:**
- Reduces billions -> millions (or less)
- Transparent to the application

**Tradeoff:**
- Slower writes
- Extra storage for index structures

### 3.2 Partitioning (Horizontal Partitioning)
**What it does:**
- Splits a large table into smaller physical chunks
- Each partition contains a range of rows

**Key Concepts:**
- Partition key determines where rows live
- Each partition can have its own indexes
- Database handles routing automatically

**Example:**
- Partition 1: rows 1 – 10M
- Partition 2: rows 10M – 20M
- Partition 3: rows 20M – 30M

**Benefits:**
- Faster queries
- Smaller indexes per partition
- Still a single database

### 3.3 Sharding (Distributed Partitioning)
**What it does:**
- Distributes data across multiple databases / machines

**Example:**
- Shard 1 -> users 1–100k
- Shard 2 -> users 100k–200k

**Client responsibility:**
- Must know which shard to query

**Benefits:**
- Dramatically reduces table size per node
- Improves scalability and availability

**Problems Introduced:**
- Cross-shard transactions
- Higher system complexity
- More operational overhead

## 4. Complete Data-Narrowing Pipeline
**Shard -> Partition -> Index -> Row**

This reduces: Billions -> Millions -> Thousands -> Single Row

## 5. Avoid the Billion-Row Table Entirely
The best optimization is not needing it at all.

**Example: Twitter "Follow" Feature**
*Naive Design:* `follower_id | following_id`
- Creates a massive relational table
- Grows extremely fast

*Alternative Design:*
- Store followers inside the Profile table
- Use:
    - `followers_count` (integer)
    - `followers` (JSON / list field)

**Benefits:**
- Single-row reads
- No massive join table

**Tradeoffs:**
- Higher write cost
- Needs async updates (message queues)
- Eventual consistency (acceptable for follower count)

## 6. Write Throughput Considerations
Writes can be:
- Asynchronous
- Buffered via message queues

Slight delays are acceptable for:
- Follower counts
- Metrics
- Social features

## 7. Final Design Strategy (Priority Order)
1. Avoid large tables via better schema design
2. Index to reduce scanned data
3. Partition to reduce table size per query
4. Shard when a single machine is not enough
5. MapReduce / brute force as a last resort
