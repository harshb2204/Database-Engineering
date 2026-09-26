# Database Replication

## Master/Backup Replication

- One Master/Leader node that accepts writes/ddls
- One or more backup/standby nodes that receive those writes from the master
- Simple to implement no conflicts
![](/diagrams/masterbackup.png)
 

## Multi-Master Replication
- Multiple Master/Leader node that accepts writes/ddls
- One or more backup/follower nodes that receive those
writes from the masters
- Need to resolves conflict

## Synchronous vs Asynchronous Replication

- **Synchronous Replication**: A write transaction to the master will be blocked until it is written to the backup/standby nodes
  - First 2, First 1 or Any

- **Asynchronous Replication**: A write transaction is considered successful if it written to the master, then asynchronously the writes are applied to backup nodes

