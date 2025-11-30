






## Row Oriented Database
- Tables are stored as rows in disk
- A single block io read to the table fetches multiple rows with all their columns. 
- More IOs are required to find a particular row in a table scan but once you find the row you get all columns for that row.

### Table
![](/images/table2.png)
Above table would look like as follows in row oriented db:-

![](/images/rowdb1.png)
4 blocks in the db


### query 1
``` 
Select first_name from emp where ssn=666
```
![](/images/rowdb2.png)

### query 2
```
Select * from Emp where id = 1
```
![](/images/rowdb3.png)
- Once we found the block we need all the columns and it is very cheap as it is already in the memory
- A row can span multiple blocks if it is huge 

### query 3
```
Select sum(salary) from emp 
```
![](/images/rowdb1.png)
- We are asking salary but pulling all the rows so thats a lot of IO 
- row dbs dont do well with these queries

## Column Oriented Database
- Tables are stored as columns first in disk
- A single block io read to the table fetches multiple columns with all matching rows
- Less IOs are required to get more values of a given column. But working with multiple columns require more IOs.
- OLAP
![](/images/table2.png)
Above table would be represented as follows:- 
![](/images/coldb1.png)

### query 1
``` 
Select first_name from emp where ssn = 666
```
- We need the ssn so we know in the disk where to point and read the stuff. In the first block we get a miss for ssn = 666 then we go to the second block where we find 666:1006. It points to the row 1006
and then we jump to the block where there is 1006 rowid. 

### query 2
```
Select * from emp where id = 1
```
![](/images/coldb2.png)
- this is a bad query for col db.(or, and queries)

### query 3
```
Select sum(salary) from emp 
```
![](/images/coldb3.png)

## Pros & Cons

### Row-Based
- Optimal for read/writes
- OLTP
- Compression isn't efficient
- Aggregation isn't efficient
- Efficient queries w/multi-columns

### Column-Based
- Writes are slower
- OLAP
- Compress greatly
- Amazing for aggregation
- Inefficient queries w/multi-columns

OLTP - Online Transaction Processing

OLTP is all about handling day-to-day business operations - the transactions that happen constantly as your business runs.

What is a "Transaction"?

A transaction is a single, complete unit of work. Think of it like a business event:

E-commerce example:

```
BEGIN TRANSACTION;

  -- Customer places order
  INSERT INTO orders (customer_id, total) VALUES (123, 99.99);
  
  -- Reduce inventory
  UPDATE inventory 
SET quantity = quantity - 1 WHERE product_id = 456;
  
  -- Charge customer
  INSERT INTO payments (order_id, amount) VALUES (789, 99.99);

COMMIT;
```

This whole sequence must succeed or fail together. If the payment fails, you don't want to reduce inventory!

Characteristics of OLTP

1. High Volume of Short Transactions

```
-- Thousands of these per second:
INSERT INTO page_views (user_id, page, timestamp) 
VALUES (12345, '/products', NOW());
UPDATE users SET last_login = NOW() WHERE user_id = 12345;
SELECT * FROM shopping_cart WHERE user_id = 12345;
```

Each query:

- Affects 1 or a few rows
- Completes in milliseconds
- Happens constantly (thousands per second)

2. Read and Write Heavy

In a typical web app:

- User logs in → INSERT into sessions table
- User views profile → SELECT from users table
- User updates email → UPDATE users table
- User adds to cart → INSERT into cart table
- User deletes item → DELETE from cart table

Lots of reading AND writing!

3. Current Data

OLTP always works with the latest data:

- What's the user's current cart?
- What's the available inventory RIGHT NOW?
- Did the payment succeed just now?

You're not looking at last month's data - you need to know what's happening NOW.

4. Works with Individual Records

```
-- Get one user's profile
SELECT * FROM users WHERE user_id = 12345;

-- Update one order
UPDATE orders SET status = 'shipped' WHERE order_id = 67890;

-- Get one product's details
SELECT * FROM products WHERE product_id = 111;
```

Notice: every query targets specific rows, not aggregate analysis.

OLTP Examples

Banking Application:

```
-- Check balance
SELECT balance FROM accounts WHERE account_id = 12345;

-- Transfer money
BEGIN TRANSACTION;

  UPDATE accounts SET balance = balance - 500 WHERE account_id = 12345;
  UPDATE accounts SET balance = balance + 500 WHERE account_id = 67890;
  INSERT INTO transactions (from_account, to_account, amount) 
  VALUES (12345, 67890, 500);

COMMIT;
```

Social Media App:

```
-- Post a tweet
INSERT INTO tweets (user_id, content, timestamp) 
VALUES (12345, 'Hello world!', NOW());

-- Like a post
INSERT INTO likes (user_id, post_id) VALUES (12345, 67890);

-- Load feed
SELECT * FROM posts WHERE user_id IN (/* following list */) 
ORDER BY timestamp DESC LIMIT 20;
```

E-commerce Site:

```
-- Add to cart
INSERT INTO cart_items (user_id, product_id, quantity) 
VALUES (12345, 67890, 2);

-- Checkout
BEGIN TRANSACTION;

  INSERT INTO orders (...) VALUES (...);
  UPDATE inventory SET stock = stock - 2 WHERE product_id = 67890;
  INSERT INTO order_items (...) VALUES (...);

COMMIT;
```

OLTP Database Characteristics

- Row-oriented storage (need full records frequently)
- Fast writes (constant inserts/updates)
- ACID compliance (transactions must be reliable)
- Normalized schema (avoid data duplication)
- Indexes on frequently queried columns (fast lookups)

OLTP Databases: PostgreSQL, MySQL, Oracle, SQL Server, MongoDB

OLAP - Online Analytical Processing

OLAP is all about analyzing data to make business decisions. You're asking questions about your business, not running it.

What is "Analytical Processing"?

Instead of "what's this user's cart?", you're asking:

- How much revenue did we make last quarter?
- Which products are most popular in California?
- What's our customer churn rate trend?
- Which marketing channels have the best ROI?

You're looking at patterns, trends, and aggregations across lots of data.

Characteristics of OLAP

1. Complex Queries on Large Datasets

```
-- Analyze sales performance
SELECT 
  p.category,
  DATE_TRUNC('month', o.order_date) as month,
  SUM(oi.quantity * oi.price) as revenue,
  COUNT(DISTINCT o.customer_id) as unique_customers,
  AVG(oi.quantity * oi.price) as avg_order_value
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE o.order_date >= '2023-01-01'
GROUP BY p.category, DATE_TRUNC('month', o.order_date)
ORDER BY month, revenue DESC;
```

This query:

- Scans millions of rows
- Joins multiple tables
- Calculates aggregates (SUM, COUNT, AVG)
- Groups by multiple dimensions
- Takes seconds or minutes to run

2. Read-Heavy, Almost No Writes

In analytics:

- You're mostly reading historical data
- Writes happen in batches (nightly ETL loads)
- No real-time inserts from users

```
-- Typical OLAP: lots of reading
SELECT COUNT(*) FROM page_views 
WHERE date BETWEEN '2024-01-01' AND '2024-12-31';

-- Rare OLAP: batch write (happens once a day)
INSERT INTO page_views 
SELECT * FROM production_db.page_views 
WHERE date = CURRENT_DATE - 1;
```

3. Historical Data

OLAP systems store years of data:

- All orders from the past 5 years
- All user behavior logs
- All financial transactions

You're analyzing the past to predict the future, not handling current transactions.

4. Works with Aggregations

```
-- Not this (OLTP style):
SELECT * FROM orders WHERE order_id = 12345;

-- But this (OLAP style):
SELECT 
  region,
  SUM(revenue) as total_revenue,
  AVG(revenue) as avg_revenue,
  COUNT(*) as order_count
FROM orders
GROUP BY region;
```

You don't care about individual orders - you care about patterns across ALL orders.

OLAP Examples

Business Intelligence Dashboard:

```
-- Monthly revenue trend
SELECT 
  DATE_TRUNC('month', order_date) as month,
  SUM(total) as revenue
FROM orders
WHERE order_date >= '2020-01-01'
GROUP BY month
ORDER BY month;

-- Customer acquisition by channel
SELECT 
  marketing_channel,
  COUNT(DISTINCT customer_id) as new_customers,
  SUM(first_order_value) as total_revenue,
  AVG(first_order_value) as avg_first_order
FROM customers
WHERE signup_date >= '2024-01-01'
GROUP BY marketing_channel;
```

Data Science / Machine Learning:

```
-- Get user behavior features for churn prediction model
SELECT 
  user_id,
  COUNT(*) as total_sessions,
  AVG(session_duration) as avg_session_length,
  SUM(pages_viewed) as total_pages,
  COUNT(DISTINCT DATE(session_start)) as active_days,
  MAX(session_start) as last_active_date
FROM user_sessions
WHERE session_start >= NOW() - INTERVAL '90 days'
GROUP BY user_id;
```

Financial Reporting:

```
-- Quarterly financial report
SELECT 
  EXTRACT(QUARTER FROM transaction_date) as quarter,
  account_category,
  SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END) as total_income,
  SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END) as total_expenses,
  SUM(CASE WHEN type = 'income' THEN amount ELSE -amount END) as net_profit
FROM transactions
WHERE EXTRACT(YEAR FROM transaction_date) = 2024
GROUP BY quarter, account_category
ORDER BY quarter, account_category;
```

OLAP Database Characteristics

- Column-oriented storage (scan specific columns across many rows)
- Optimized for reads (complex queries on large datasets)
- Denormalized schema (star/snowflake schemas with fact and dimension tables)
- Heavy compression (store years of data efficiently)
- Batch loading (ETL processes load data periodically)

OLAP Databases: Snowflake, Amazon Redshift, Google BigQuery, ClickHouse, Apache Druid

OLTP vs OLAP - Side by Side Comparison

Data Volume

OLTP:

- Megabytes to Gigabytes
- Current data (last few weeks/months)
- Example: 100GB database for an e-commerce site

OLAP:

- Terabytes to Petabytes
- Historical data (years of history)
- Example: 50TB data warehouse with 10 years of data
