## Sharding

- Process of segmenting the data into partitions that are spread on multiple db instances.

![](/diagrams/sharding.png)
![](/diagrams/sharding1.png)

### Consistent Hashing
- You take an input or string or any user provided piece of data that you want to query on and you want to know which db to query on. 
- That hash is going to give you back that instance.
- As long as we get the same string, we should make sure to always consistently hash to the same server, to the same node essentially.

![](/diagrams/consistenthashing.png)

### Horizontal Partitioning vs Sharding
- HP splits big table into multiple tables in the same database
- Sharding splits big table into multiple tables across multiple database servers
- HP table name changes (or schema)
- Sharding everything is the same but server changes

## When to Shard?
Sharding should be the **last resort**. It is a very complex architecture and operation to implement. You should optimize and exhaust other options first.

### Alternatives to Sharding
1. **Vertical Scaling**: Upgrade your server hardware (CPU, RAM, SSD).
2. **Indexing**: Optimize queries with proper indexing. However, keep in mind that very large tables lead to large indexes, which can slow down performance.
3. **Horizontal Partitioning**: Slice large tables into smaller chunks within the **same database instance**. This keeps indexes smaller and queries faster without the complexity of multiple servers.
4. **Caching**: Use a caching layer (like Redis) to handle frequent reads and reduce the load on the database.
5. **Replication (Read Replicas)**: Use a Master-Replica setup where one server handles writes and multiple replicas handle reads.
6. **Multi-Master Replication**: Segregate writes by region (e.g., US East vs. US West) to scale write capacity.


## Pros of Sharding
- **Scalability**
  - Data
  - Memory
- **Security** (users can access certain shards)
- **Optimal and Smaller index size**

## Cons of Sharding
- **Complex client** (aware of the shard)
- **Transactions across shards problem**
- **Rollbacks**
- **Schema changes are hard**
- **Joins**
- **Has to be something you know in the query**

## Tools
- **Vitess**: An open-source database clustering system for horizontal scaling of MySQL. It acts as a middleware that handles shard routing, allowing the application to send standard SQL queries without being "shard-aware." Used extensively by platforms like YouTube.
