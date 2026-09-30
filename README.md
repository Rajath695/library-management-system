# Library Management System

A Java console application for managing a small library, backed by a 
cloud-hosted PostgreSQL database (Neon).

## Features

- **User:** view available books, borrow books, return books
- **Admin:** register new books
- **Registration:** create a new user account
- Persistence via PostgreSQL

## Setup

### 1. Create a Neon database
1. Sign up at https://neon.tech
2. Create a project
3. Run the schema in the SQL Editor (see `schema.sql`)

### 2. Download the JDBC driver
Download `postgresql-42.7.13.jar` from https://jdbc.postgresql.org/download/
and place it in the `lib/` folder.

### 3. Configure credentials
Create a file named `config.properties` in the project root:



db.url=jdbc:postgresql://YOUR-HOST/neondb?sslmode=require
db.user=YOUR_USER
db.pass=YOUR_PASSWORD

text

### 4. Compile and run
javac -cp "lib/postgresql-42.7.13.jar" Database.java manager.java Main.java
java -cp "lib/postgresql-42.7.13.jar;." Main

text
(On macOS/Linux, use `:` instead of `;` in the classpath.)

## Default accounts
- Admin: `admin` / `admin`
- User:  `user` / `p`

## Schema
See `schema.sql` for the `CREATE TABLE` statements.