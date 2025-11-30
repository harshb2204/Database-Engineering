## Durability

- Changes made by committed transactions must be persisted to durable, non-volatile storage.
- Durability techniques ensure data survives crashes or restarts.
  - WAL (Write-Ahead Log)
  - Asynchronous snapshots
  - AOF (Append-Only File)

Durability guarantees that once a transaction is committed, its effects are not lost even if the system crashes immediately afterward.

## Durability – WAL

- Writing full data pages, indexes, and rows to disk is expensive.
- DBMSs therefore persist a compact description of changes first—a write-ahead log (WAL) segment.
- Dimage.pngatabase first writes a small log entry describing the change. This is fast.
- On recovery, the WAL is replayed to reconstruct the latest committed state before serving new transactions.

## Durability – OS Cache

- When an application issues a write, the operating system usually places the bytes in its page cache rather than flushing to disk immediately.
- If the OS or machine crashes before those cached bytes are written out, the database can lose data even though it “wrote” the change.
- Calling `fsync` (or similar) forces the OS to flush the write to disk, guaranteeing persistence.
- `fsync` is expensive because it waits for the storage device, so databases batch or group commits to balance durability and throughput.

