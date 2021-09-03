# BORM - Basic ORM

This is a basic Java ORM library for mapping Java objects to SQL database tables. To use this library clone this reposity and use Maven to build the target JAR.
```bash
mvn install
```
Using an IDE like IntelliJ makes the installation process significantly easier.

Once you have the JAR installed into your local .m2 directory, add it as a dependency on a new Maven project. Place the following 
in your pom.xml.
```xml
<dependency>
    <groupId>com.revature</groupId>
    <artifactId>ORM</artifactId>
    <version>1.0.1</version>
</dependency>
```
# How to Use

**Note** -  As of this writing the only SQL variant supported
is PostgreSQL.

This ORM works on existing tables in a SQL database. BORM uses annotations to recognize
how your Java classes map to your database tables. 

- **@Table**: used on a class declaration to denote that the class represents a database table.
- **@Column**: used on a class field to denote a table column.
- **@PrimaryKey**: used on a class field to mark a table's primary key. Note that BORM expects
that your primary key is a serial integer.

Here's an example of it in action.

```java
import com.revature.orm.annotations.Column;
import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;

@Table(tableName = "user")
public class User() {
    @PrimaryKey
    @Column(columnName = "userId")
    private int userId;
    @Column(columnName = "username")
    private String username;
}
```

The tableName and columnName fields should be set to the same names used for the tables and columns in your database.

When you want to persist data, instantiate the ORM and call upon its methods. BORM has methods for each
part of CRUD.

- create - takes a Java object and persists it as a new database row
- get - retrieve a single database row and return it as a Java object
- getAll - retrieve all the rows for a defined entity, return a Java List
- update - update an existing row with new values
- delete - delete a database row

# SQL Types

BORM currently supports the following SQL types:
- serial
- integer, int4
- numeric
- boolean

# Database Connection Configuration

BORM expects a `Properties` object when you instantiate a BORM class that implements the 
`ObjectRelationalMapper` interface. The library will look for the following properties:
- DB_URL
- DB_USERNAME
- DB_PASSWORD

It is recommended that you place these in a `.properties` file in your `src/main/resources`
directory and have your application load them.