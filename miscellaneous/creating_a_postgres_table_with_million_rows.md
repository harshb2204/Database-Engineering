# Creating a Postgres Table with Million Rows

Quick way to create a test table with a million rows for performance testing and experimentation.

```sql
-- Create a simple table with one integer column
CREATE TABLE temp(t int);

-- Insert 1,000,001 rows with random values between 0 and 100
INSERT INTO temp(t) 
SELECT random()*100 
FROM generate_series(0, 1000000);

-- Verify the data
SELECT t FROM temp LIMIT 20;
```

**Explanation:**
- `generate_series(0, 1000000)` generates numbers from 0 to 1,000,000 (1,000,001 rows)
- `random()*100` generates random floating-point numbers between 0 and 100, which are cast to integers
- This creates a table with 1,000,001 rows for testing queries, indexes, and performance

