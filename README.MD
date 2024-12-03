# Platform for Creating Rooms and Conferences

This platform allows the creation of rooms and conferences within them, and provides functionality for registering participants.

## Technologies Used

- **Java 17**
- **Maven** for building the project
- **Spring** for runtime framework
- **Flyway** for database migrations
- **Hibernate** for ORM (Object Relational Mapping)
- **Lombok** for reducing boilerplate code
- **Validation** for input validation
- **Springdoc** for API documentation

### Testing Tools:
- **SpringTest** for testing framework
- **Mockito** for mocking dependencies
- **TestContainers** for database testing (without a real database)

---

## Project Structure

- platform/
  - ├── config 
  - ├── product 
    - ├── backoffice 
    - ├── conference 
    - └── data

---

## Setup Instructions

### 1. Configure Database Connection
Before running the application :

Ensure you update the database connection details in ```/config/local.yml```, or create a database using those values.


### 2. Start the Application

From the platform directory, run the following command to start the application

Script will: 
1. check is PostgreSQL running locally
2. compile code
3. run docker-compose

```
./up.sh
```

#### Available Flags for up.sh:

- -f — Skip the PostgreSQL check
- -s — Run silently without logs


### 3. Stop and Clean the Application

Stops containers and cleans maven

```
./down.sh
```

#### Available Flag for down.sh:

- -s — Perform silent cleanup (without logs)

---

## Info


👤 Creator:
Toomas Park

📧 toomas.park.work@gmail.com

---
### Test task for Tallink

---