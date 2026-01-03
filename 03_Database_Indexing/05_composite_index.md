![](/images/sampletable.png)
- Around 12 million rows on this table
- We create an index on a and b
```sql
create index on test(a);
create index on test(b);
```
![](/images/explainanalyzetest.png)
- postgres decided to use an index, but create a bitmap in order to query the index, because we have to jump back to the heap to pull c. c is not included in the index.
- we can easily change this kind of infer the decision of Postgres by actually limiting the number of rows. So now postgres doesnt build a bitmap. So it does a index scan.

![](/images/explainanalyzetest2.png)
- similar result to before

```sql
explain analyze select c from test where a = 100 and b = 200;
```
![](/images/explainanalyzetest3.png)
- So Postgres decides says, okay, in parallel to scan the A index and B index.
- And scanning those two values, it is going to scan them and it is going to build a bitmap.
- And literally you get a bitmap and then literally just AND those bitmaps.
- You're going to end up with a certain values.
- Some values will zero up, some values won't.
- And then you're going to end up with a set of tuples that you're going to hit the heap and pull the table directly.

Similarly we do the OR query and it would obviously take more time to do that. 

```sql
drop index test_a_index, test_b_index;

```
Now we create a composite index
```sql
create index on test(a, b);
```
(One index that has both values)
![](/images/explainanalyzetest4.png)
### How Multi-column Indexes Work (Simple Terms)
When you have an index on multiple columns, like `(A, B, C)`, here is how the database actually uses it:

*   **Order Matters**: The index is most efficient when your query filters starting from the leftmost column. For an index on `(A, B, C)`, filtering by `A` is fast, but filtering only by `B` or `C` might be slow.
*   **The "Range" Limit**: The database uses the index to narrow down the search area as long as it sees "equals" (`=`) filters from the left. Once it hits a "range" filter (like `>`, `<`, or `BETWEEN`), it can't narrow the search any further for the columns that follow.
*   **Avoiding the Heap**: Even if a column doesn't help "narrow down" the search area, if it's in the index, the database can check its value without jumping to the main table (the "heap"), which still saves time.
*   **Skipping the First Column**: If you don't filter by the first column of the index at all, the database often decides it's faster to just scan the whole table instead of using the index.

![](/images/explainanalyzetest4.png)
- This is the best case scenario.
- Querying only on b would result in a seq scan of the table.


