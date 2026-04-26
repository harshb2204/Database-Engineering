# Database Sharding Implementation Example

This directory contains a sample implementation demonstrating how to shard a PostgreSQL database using a URL shortener application as an example.

## Files Overview

### 1. `Index.java`
This is the main Java application using Java's built-in `HttpServer` and JDBC. It acts as the API and router for our sharded database architecture.
- **Consistent Hashing**: It implements a custom `HashRing` to create a hash ring consisting of three PostgreSQL servers (running on ports `5432`, `5433`, and `5434`).
- **Insertion (`POST /`)**: When a URL is submitted, the server generates a SHA-256 hash of the URL, extracts the first 5 characters to create a `URL_ID`, and then checks the hash ring to determine which database shard should store this URL. The URL is then inserted *only* into the designated shard via JDBC.
- **Retrieval (`GET /:urlId`)**: When a user queries a `URL_ID`, the server uses the hash ring to find out which shard *should* contain the URL, and it queries only that specific database instance. This avoids having to search across all databases.

### 2. `Text.java`
A simple test script showing the basic mechanics of how a consistent hashing module distributes keys across a predefined set of nodes with virtual weights.

### 3. `init.sql`
A SQL initialization script that gets copied into the PostgreSQL Docker containers. When each database spins up for the first time, this script creates the `URL_TABLE` that will be used to store the original URLs and their corresponding 5-character `URL_ID`s.

### 4. `Dockerfile`
A basic configuration used to build a custom PostgreSQL Docker image. It simply takes the official `postgres` image and copies the `init.sql` file into the `/docker-entrypoint-initdb.d` directory so the schema is initialized automatically upon container startup.

### 5. `commands.txt`
Contains the necessary Docker commands to build the custom PostgreSQL image, run the three independent database containers mapping to different local ports (acting as our shards), and also spin up a `pgadmin` instance for database management and visualization.

## How It Works in Practice

1. We use Docker to spin up three identical PostgreSQL instances. They all run the exact same schema but will house different pieces of data.
2. The application (`index.js`) maintains a list of these instances and builds a hash ring.
3. Every time a write or read request is initiated, the routing key (`url` for writes, `urlId` for reads) is hashed.
4. The router determines the appropriate database partition based on the hash and routes the request solely to that partition.
5. This logic represents horizontal partitioning across separate servers, fundamentally allowing the database to scale horizontally as data volume grows.
