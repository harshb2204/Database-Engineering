Consistency means the database is always in a correct state—both when you write data and when you read data.

## Consistency in Data 

This is about keeping the data valid based on rules you set. For example:

- A ride must always belong to a valid user → foreign key rule.
- A wallet balance cannot be negative → business rule.

The database helps with this using:

- Constraints (foreign keys, unique, not-null, etc.).
- Atomicity → either the whole write happens or none of it happens.
- Isolation → other transactions don’t see half-finished work.

So data is never stored in a broken or partial state.

### Example: Picture Likes

 Pictures
| (ID PK) | Blob | Likes |
| --- | --- | --- |
| 1 | xx | 2 |
| 2 | xx | 1 |


Picture_Likes
|  (User PK) | Picture_ID (PK/FK) |
| --- | --- |
| Jon | 1 |
| Edmond | 1 |
| Jon | 2 |

- The `Picture_Likes.picture_id` column references `Pictures.id`, so every like must point to an existing picture.
- The `Pictures.likes` column should equal the count of rows in `Picture_Likes` per picture.
- Transactions must update both tables together to keep counts accurate; otherwise, constraints or application logic should reject inconsistent states.

### Spot the Inconsistencies

Pictures
| (ID PK) | Blob | Likes |
| --- | --- | --- |
| 1 | xx | 5 |
| 2 | xx | 1 |

Picture_Likes
| (User PK) | Picture_ID (PK/FK) |
| --- | --- |
| Jon | 1 |
| Edmond | 1 |
| Jon | 2 |
| Edmond | 4 |

- Picture 1 reports 5 likes but only has 2 rows in `Picture_Likes`.
- `Picture_ID = 4` has likes but no corresponding picture, violating the foreign-key rule.
- These mismatches show how violating constraints (counts or foreign keys) breaks consistency and must be prevented by atomic, isolated updates.

## Consistency in Reads

- If a transaction commits a change, will the next transaction immediately see it?
- This affects the system as a whole—replication, caches, and distributed nodes can delay visibility.
- Both relational and NoSQL databases can experience staleness or lagged reads.
- Eventual consistency is a common model where changes propagate over time; readers may briefly see old data but will converge to the latest state eventually.


## Eventual Consistency
![](/diagrams/eventualconsistency.png)

Eventual consistency means replicas do not stay perfectly synchronized at every moment, but they will converge once all updates propagate. Reads may temporarily return stale data from one replica while another has the latest value, yet given enough time—and no new writes—every replica agrees on the same state. This trade-off boosts availability and throughput in distributed systems at the cost of temporarily inconsistent reads.
