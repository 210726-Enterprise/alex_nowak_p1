package com.revature.orm.mapper;

import com.revature.orm.exceptions.FailedQueryException;

import java.util.List;
import java.util.Optional;

/**
 * The Object Relational Mapper provides an interface for persisting and retrieving Java objects
 * to and from a relational database respectively. Java object models that you create must use the
 * provided annotations in this library in order to be properly mapped to a SQL database table.
 *
 * The Table annotation denotes that a Java class is a representation of a table in your database.
 * An instance of a Java object annotated with Table maps to a row of the table.
 * Set the tableName field of the Table annotation with the name of the table in the database.
 *
 * The PrimaryKey annotation marks a class field as the primary key of a database table.
 * This field is used to retrieve, update, and delete records from your table.
 *
 * The Column annotation marks a class field as a column of the database table. Set the columnName
 * field of the Column annotation with the name of the column in the database.
 */
public interface ObjectRelationalMapper {
    /**
     * Inserts a new row into an existing database. The class must have publicly accessible
     * getters in order to use reflection at run time. A boolean is returned to show the success
     * or failure of the operation.
     * @param entity an Object representing a new row to be added to a database table
     * @return a boolean showing if the new row insertion was successful or not
     */
    boolean insert(Object entity);

    /**
     * Gets a single row from a table and return it with an Optional wrapper.
     * @param entityClass a Class object of the type to be retrieved, used for reflection to
     *                    build out the Java object and to know what table to query
     * @param keyId the primary key ID of the row to be retrieved
     * @param <T> the type that will be built and returned
     * @return an Optional of containing an object of type T built with the values retrieved
     * from the database
     */
    <T> Optional<T> getById(Class<?> entityClass, int keyId);

    /**
     * Gets all the rows from a table and return it as a List.
     * @param entityClass a Class object of the type to be retrieved, used for reflection to
     *                   build out the Java object and to know what table to query
     * @param <T> the type that will be built and returned
     * @return a List containing the new objects built from the database table data
     * @throws FailedQueryException if there are errors getting data from the database or
     *                              using reflection
     */
    <T> List<T> getAll(Class<?> entityClass) throws FailedQueryException;

    /**
     * Updates an existing row in a database table.
     * @param entity an Object with new values to be placed on an existing database row
     * @param keyId the primary key ID of the row to be updated
     * @return a boolean showing if the update was successful or not
     */
    boolean update(Object entity, int keyId);

    /**
     * Deletes an existing row in a database table.
     * @param entityClass a Class object to reference the table that will be
     * @param keyId the primary key ID of the row to be deleted
     * @return a boolean showing if the deletion was successful or not
     */
    boolean delete(Class<?> entityClass, int keyId);
}
