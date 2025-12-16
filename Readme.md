# Lionic - Database Management System

## Project Description
Lionic is a robust Java application for managing person data with a focus on high-performance PostgreSQL database operations. It implements a professional-grade data access layer with connection pooling, comprehensive error handling, and an intuitive command-line interface.

## Key Features
- **Efficient Database Connectivity**: Uses HikariCP connection pooling for optimized database performance
- **Repository Pattern**: Clean separation of concerns with a repository-based architecture
- **Flexible API**: Simple CRUD operations and search functionality for person management
- **Robust Error Handling**: Comprehensive exception handling with detailed error classification
- **Type-Safe Data Access**: Strongly-typed operations with proper object mapping
- **Command Line Interface**: Interactive terminal for managing people records
- **Extensible Design**: Modular architecture that can be easily expanded

## Prerequisites
- Java Development Kit (JDK) 21 or higher
- PostgreSQL database server 13+
- Maven 3.8+ for building the project

## Setup Instructions

### 1. Clone the Repository
```sh
git clone https://github.com/RedNetty/Lionic.git
cd Lionic
```

### 2. Set Up PostgreSQL Database
1. Install and start PostgreSQL (if not already running)
2. Create a database for Lionic:
   ```sql
   CREATE DATABASE lionic;
   CREATE USER lionic_user WITH ENCRYPTED PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE lionic TO lionic_user;
   ```

### 3. Configure the Application
Create a configuration file at `./config/database.json` with the following content:
```json
{
  "dbType": "postgresql",
  "hostName": "localhost",
  "dbName": "lionic",
  "username": "lionic_user",
  "password": "your_password",
  "port": 5432,
  "connectionPoolSize": 10,
  "connectionTimeout": 30000,
  "idleTimeout": 600000
}
```

Alternatively, you can set these configuration values as environment variables:
```sh
export DB_TYPE=postgresql
export DB_HOST=localhost
export DB_NAME=lionic
export DB_USERNAME=lionic_user
export DB_PASSWORD=your_password
export DB_PORT=5432
```

### 4. Build the Project
```sh
mvn clean package
```

### 5. Run the Application
```sh
java -jar target/lionic.jar
```

## Usage Guide

### Command Line Interface
The application provides an interactive CLI with the following commands:

| Command | Description | Example |
|---------|-------------|---------|
| `-add` | Add a new person | `-add 50000,John Doe,30,1994` |
| `-remove` | Remove person by name or ID | `-remove John` or `-remove 1` |
| `-list` | List all people | `-list` |
| `-find` | Search for people by name | `-find Doe` |
| `-save` | Save all changes to database | `-save` |
| `-exit` | Exit the application | `-exit` |

### Java API Example
```java
// Initialize database service
DatabaseService dbService = new DatabaseService();

// Create a new person
Person person = new Person(1, "John", "Doe", 30, "john.doe@example.com");
person.addAttribute("networth", 50000.0);
person.addAttribute("birthYear", 1994);

// Save to database
dbService.savePerson(person);

// Find people by name
List<Person> results = dbService.findPeopleByName("Doe");

// Close resources when done
dbService.close();
```

## Architecture

### Components
- **DatabaseService**: Main entry point for database operations
- **ConnectionManager**: Manages database connections with HikariCP
- **Repository**: Base class for data access operations
- **PersonRepository**: Implements CRUD operations for Person entities
- **PersonManager**: Handles user interaction and business logic

### Data Flow
1. User interacts with **PersonManager** via CLI
2. **PersonManager** delegates operations to **DatabaseService**
3. **DatabaseService** uses **PersonRepository** for data access
4. **PersonRepository** uses **ConnectionManager** for database connections
5. Data is stored in PostgreSQL and returned as domain objects

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please adhere to:
- Clean code principles
- Proper error handling
- Unit testing for new features
- Documentation for public APIs

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - High-performance JDBC connection pool
- [Gson](https://github.com/google/gson) - JSON serialization/deserialization library
- [SLF4J](https://www.slf4j.org/) and [Logback](https://logback.qos.ch/) - Logging framework