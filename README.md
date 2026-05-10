# Contact Book CLI

A command-line based Contact Management System built using Java, JDBC, MySQL, Maven, and HikariCP connection pooling.

This project demonstrates backend development fundamentals including layered architecture, database connectivity, connection pooling, schema initialization, and CRUD operations.

---

# Features

* Add new contacts
* View all contacts
* Search contacts
* Update contact details
* Delete contacts
* MySQL database integration
* JDBC-based persistence layer
* HikariCP connection pooling
* Maven project structure
* Layered backend architecture

---

# Tech Stack

* Java 24
* JDBC
* MySQL
* Maven
* HikariCP
* IntelliJ IDEA
* Git & GitHub

---

# Project Structure

```text
src/main/java/com/contactbook
│
├── cli
│     └── ContactBookCLI.java
│
├── config
│     └── DatabaseConfig.java
│
├── dao
│     └── ContactDAO.java
│
├── model
│     └── Contact.java
│
└── service
      └── ContactService.java
```

---

# Database Schema

```sql
CREATE TABLE IF NOT EXISTS contacts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(30),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

# Setup Instructions

## 1. Clone Repository

```bash
git clone https://github.com/your-username/contact-book-cli.git
```

---

## 2. Open Project

Open the project in IntelliJ IDEA.

---

## 3. Configure MySQL

Create database:

```sql
CREATE DATABASE contactbook;
```

---

## 4. Create `db.properties`

Create file:

```text
src/main/resources/db.properties
```

Example:

```properties
db.url=jdbc:mysql://localhost:3306/contactbook
db.username=your_username
db.password=your_password

pool.maximumPoolSize=10
pool.minimumIdle=2
pool.connectionTimeout=30000
pool.idleTimeout=600000
pool.maxLifetime=1800000
```

---

# Important

`db.properties` is excluded from GitHub using `.gitignore` for security reasons.

---

# Build Project

```bash
mvn clean install
```

---

# Run Application

```bash
mvn exec:java "-Dexec.mainClass=com.contactbook.cli.ContactBookCLI"
```

OR run directly from IntelliJ IDEA.

---

# Maven Dependencies

* MySQL Connector/J
* HikariCP
* SLF4J

---

# Future Improvements

* Pagination
* Contact groups
* Favorites
* REST API with Spring Boot
* Authentication
* Docker support
* Unit testing with JUnit
* Logging system
* Export contacts to CSV

---

# Learning Outcomes

This project helped in understanding:

* JDBC architecture
* DAO pattern
* Connection pooling
* SQL schema design
* Maven build system
* Backend project structure
* MySQL indexing
* Exception handling
* Git & GitHub workflow

---

# Author

Harshavardhan Doddi

Aspiring Full Stack Java Developer focused on backend engineering, system design, and scalable application development.
