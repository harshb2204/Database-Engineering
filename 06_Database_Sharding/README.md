## Sharding

- Process of segmenting the data into partitions that are spread on multiple db instances.

![](/diagrams/sharding.png)
![](/diagrams/sharding1.png)

### Consistent Hashing
- You take an input or string or any user provided piece of data that you want to query on and you want to know which db to query on. 
- That hash is going to give you back that instance.
- As long as we get the same string, we should make sure to always consistently hash to the same server, to the same node essentially.

![](/diagrams/consistenthashing.png)

