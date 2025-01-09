# Platform for Creating Rooms and Conferences

### This platform allows the creation of rooms and conferences within them, and provides functionality for registering participants.

## Technologies Used

- **Java 17** | 17.0.8-tem
- **Kotlin** | 2.1.0
- **Maven 3.8** for building the project
- **Spring** for runtime framework
- **Postgres** for Database
- **Flyway** for database migrations
- **Hibernate** for ORM (Object Relational Mapping)
- **Lombok** for reducing boilerplate code
- **Validation** for input validation
- **Mapstruct** for code clean
- **Springdoc** for API documentation

### Testing Tools:
- **SpringTest** for testing framework
- **Mockito** for mocking dependencies
- **TestContainers** for database testing (without a real database)

### Other:

- **Sdkman 5.18** - as system configs
- **Kubuntu 24** - as OS
- **Postgres 16** - for local database
---

## Project Structure

- platform/
  - ├── config 
  - ├── product 
    - ├── backoffice (Java)
    - ├── conference (Kotlin) 
    - └── data (Java)

---

## Setup Instructions

### 1. Configure Database Connection

Before running the application :
- Ensure `maven` is installed.
- Ensure installed `docker and it's components`. 
- Ensure using `JAVA 17.0.8-tem` for build.
- Ensure to update the database connection details in `/config/local.yml`, or create a database using those values.
  - Local database from containers are reachable at `172.17.0.1:(default port 5432)`

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
- -t — Skip tests (-DskipTests)


### 3. Stop and Clean the Application

Stops containers and cleans maven

```
./down.sh
```

#### Available Flag for down.sh:

- -s — Perform silent cleanup (without logs)
- -f — Perform `docker system prune -af` after clean

### 4. Springdoc OpenAPI
- #### backoffice
  - Swagger UI for the backoffice is reachable (after successfully starting application) by:   
    [http://172.17.0.1:8082/swagger-ui/index.html](http://172.17.0.1:8082/swagger-ui/index.html)
- #### conference:
  - Swagger UI for the conference is reachable (after successfully starting application) by:   
    [http://172.17.0.1:8083/swagger-ui/index.html](http://172.17.0.1:8083/swagger-ui/index.html)
---

## Info


👤 Creator:
Toomas Park

📧 toomas.park.work@gmail.com

---
### Test task for Tallink

---