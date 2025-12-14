

## index and seq scan
![](/images/indexscan1.png)
- postgres is using an index scan using the grades_pkey which is the id, which will pull one row. 
- once we pull that row the index is responsible to go back to the table to bring the name field. (name is part of table which is part of the heap)
![](/images/indexscan2.png)
- here we are bringing around 99 values, so postgres still decided to do an index scan. Db says its going to use the index to do an index scan on the id and them go scan one by one and find anything less than 100 (quick in a btree)
- for each value that it finds it finds the page where the row exists and goes back to the heap to pull that page which kind of have one or more rows and then pulls that value. This is called random access. this can get slow if you have a lot of rows 

![](/images/seqscan.png)
- almost all the rows will satify this condition, it is expensive to jump back and do a random access. so postgres decided to do a sequential scan. If postgres thinks that you have less rows it will do an index scan and if many rows it is way cheaper to do a sequential scan 

## bitmap scan

![](/images/bitmapscan.png)
- here we are getting grades greater than 95, but since there is an index on g its worth doing an index scan, but jumping to the table for every value we find is expensive. Its going to do a bitmap index scan.


![](/diagrams/bitmapscan.png)
- postgres builds a bitmap (array of bits). Values represent a page number. It will do a scan on g when it finds a value that satisfies the condition it is going to do an index scan, but it will not directly jump to the table when we find a page. It will set a bit on that bitmap. For example it found a row that belongs to page 9. So it will go and set bit 9 as 1.
- You then have a bitmap that you use to jump once to the heap table and pull all the pages.
- You can scan multiple indexes, build the bitmaps and get one bitmap   