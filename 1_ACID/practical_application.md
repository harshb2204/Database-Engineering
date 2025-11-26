## Practical Application Setup



1. Pulled and ran PostgreSQL 13 in Docker:
   - `docker run --name pgacid -d -e POSTGRES_PASSWORD={whatever u want to add as passowrd} postgres:13`
   - Docker automatically downloaded the `postgres:13` image layers and started container `pgacid`.
2. Verified running containers:
   - `docker ps`
   - Observed `pgacid` (PostgreSQL 13) and other existing containers (`openelisglobal-database`, `oe-certs`).
3. Connected to the new container using `psql`:
   - `docker exec -it pgacid psql -U postgres`
   - Confirmed access to the `postgres` database prompt.

## Setup queries

```
create table products (pid serial primary key, name text, price float, inventory integer);
create table sales (saleid serial primary key, pid integer, price float, quantity integer);
insert into products(name, price, inventory) values('phobe', 100.00,
100);


```
![](/images/sqlquery1.png)

- We are going to sell 10 phones. What does that mean though? That is one task right, but in the db its broken into multiple queries in a single transaction. 

![](/images/sqlquery2.png)
- We have exited the container here. Without atomicity this should be a disaster. We simulated a crash, so the quantity remains 100.

![](/images/sqlquery3.png)
- Here we can see the changes are only viewable in that transaction only.(Isolation)

``` begin transaction isolation level repeatable read; ```
- Prevents you from seeing stuff that other people's changing. Suppose we start a transaction in one terminal and query count of sales and in other terminal in an another transaction we do a sale the count of sale in the first transaction does not change even if we commit it from the other terminal. 
-This is a feature of posgtgress that it prevents phantoms reads even with isolation level repeatable read (usually it is done by serializable). Phantom read -> All of a sudden you get a new row (in mysql if u do a repeatable read you dont get rid of 
phantom reads)





