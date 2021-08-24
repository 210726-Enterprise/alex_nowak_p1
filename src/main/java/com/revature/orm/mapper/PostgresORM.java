package com.revature.orm.mapper;

import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;
import com.revature.orm.annotations.Column;
import com.revature.orm.exceptions.FailedQueryException;
import com.revature.orm.jdbc.SQLExecutor;
import com.revature.util.ConnectionFactory;
import org.apache.log4j.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A PostgreSQL specific implementation of the ObjectRelationalMapper interface.
 */
public class PostgresORM implements ObjectRelationalMapper{
    private final static Logger logger = Logger.getLogger(PostgresORM.class);
    private static Properties DB_CONNECTION_PROPS;

    public PostgresORM(Properties connectionProps){
        DB_CONNECTION_PROPS = connectionProps;
    }

    /**
     * Inserts a new row into an existing database. The class must have publicly accessible
     * getters in order to use reflection at run time. A boolean is returned to show the success
     * or failure of the operation.
     * @param entity an Object representing a new row to be added to a database table
     * @return a boolean showing if the new row insertion was successful or not
     */
    @Override
    public boolean insert(Object entity) {
        //Get the table that we will insert into
        String table = entity.getClass().getAnnotation(Table.class).tableName();
        //Get a stream of all the entity fields and filter it down to only fields
        //with a @Column annotation, then get the collection as a List
        //Primary Keys are excluded, assume the user's tables are setup with auto-increment id keys
        Stream<Field> fieldsStream = Stream.of(entity.getClass().getDeclaredFields());
        List<Field> columns = fieldsStream.filter((field) -> field.getAnnotation(Column.class) != null && field.getAnnotation(PrimaryKey.class) == null)
                                            .collect(Collectors.toList());
        List<Method> getters = Stream.of(entity.getClass().getMethods()).filter((method) -> method.getName().startsWith("get"))
                                                                                .collect(Collectors.toList());

        Map<Field, Method> fieldGetters = mapGettersToFields(columns, getters);

        StringBuilder sql = new StringBuilder("INSERT into \"" + table + "\" (");
        StringBuilder values = new StringBuilder();
        Iterator<Field> columnsIterator = columns.iterator();
        while(columnsIterator.hasNext()){
            Field field = columnsIterator.next();
            sql.append("\"");
            sql.append(field.getAnnotation(Column.class).columnName());
            sql.append("\"");
            //String field values require single quotes for the SQL statement
            try{
                if(field.getType().equals(String.class)){
                    values.append("'");
                    values.append(fieldGetters.get(field).invoke(entity));
                    values.append("'");
                }
                else{
                    values.append(fieldGetters.get(field).invoke(entity));
                }
            } catch (ReflectiveOperationException e) {
                System.out.println(e.getMessage());
                logger.error(e.getMessage(), e);
            }

            if(columnsIterator.hasNext()){
                sql.append(",");
                values.append((','));
            }
        }
        sql.append(") values (");
        sql.append(values);
        sql.append(")");

        int rows = SQLExecutor.doUpdate(sql.toString(), DB_CONNECTION_PROPS);
        if(rows > 0){
            logger.info("New " + entity.getClass().getSimpleName() + " created in the database!");
            return true;
        }
        else{
            logger.warn("No records inserted into the database.");
            return false;
        }
    }

    /**
     * Gets a single row from a table and return it with an Optional wrapper. The model class
     * must have a default constructor in order for the new instance to be instantiated.
     * @param entityClass a Class object of the type to be retrieved, used for reflection to
     *                    build out the Java object and to know what table to query
     * @param keyId the primary key ID of the row to be retrieved
     * @param <T> the type that will be built and returned
     * @return an Optional of containing an object of type T built with the values retrieved
     * from the database
     */
    @Override
    public <T> Optional<T> getById(Class<?> entityClass, int keyId) {
        T newInstance = null;

        String table = entityClass.getAnnotation(Table.class).tableName();
        String primaryKey = getPrimaryKeyName(entityClass);

        StringBuilder sql = new StringBuilder("SELECT * from ");
        sql.append("\"").append(table).append("\"");
        sql.append(" where ");
        sql.append("\"").append(primaryKey).append("\"").append("=").append(keyId);

        List<Constructor<?>> defaultConstructorList = Stream.of(entityClass.getConstructors()).filter((constructor -> constructor.getParameterCount() == 0))
                .collect(Collectors.toList());
        Constructor<?> defaultConstructor = defaultConstructorList.get(0);

        try(Connection connection = ConnectionFactory.getConnection(DB_CONNECTION_PROPS)){
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();

            // Create new object instance
            // Use setters to set values, get the values from the resultSet
            newInstance = (T)defaultConstructor.newInstance();
            List<Field> columns = Stream.of(entityClass.getDeclaredFields()).filter((field) -> field.getAnnotation(Column.class) != null)
                    .collect(Collectors.toList());
            List<Method> setters = Stream.of(entityClass.getMethods()).filter((method) -> method.getName().startsWith("set"))
                    .collect(Collectors.toList());
            Map<String, Method> fieldSetters = mapSettersToFieldNames(columns, setters);
            while (resultSet.next()){
                for(int i = 1; i <= resultMetaData.getColumnCount(); i++){
                    String columnName = resultMetaData.getColumnName(i);
                    String columnType = resultMetaData.getColumnTypeName(i);
                    switch (columnType){
                        case "serial":
                        case "int4":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getInt(columnName));
                            break;
                        case "text":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getString(columnName));
                            break;
                        case "numeric":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getFloat(columnName));
                            break;
                        case "boolean":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getBoolean(columnName));
                            break;
                    }
                }
            }
            logger.info(entityClass.getSimpleName() + " " + keyId + " retrieved from database.");
        } catch (SQLException e){
            logger.warn(e.getMessage(), e);
        } catch (ReflectiveOperationException e) {
            logger.warn(e.getMessage(), e);
        }

        return Optional.of(newInstance);
    }

    /**
     * Gets all the rows from a table and return it as a List. The model class must have a
     * default constructor in order for this method to be able to be instantiate new instances.
     * @param entityClass a Class object of the type to be retrieved, used for reflection to
     *                   build out the Java object and to know what table to query
     * @param <T> the type that will be built and returned
     * @return a List containing the new objects built from the database table data
     * @throws FailedQueryException if there are errors getting data from the database or
     *                              using reflection
     */
    public <T> List<T> getAll(Class<?> entityClass) throws FailedQueryException{
        List<T> retrievals = new ArrayList<>();

        String table = entityClass.getAnnotation(Table.class).tableName();
        String primaryKey = getPrimaryKeyName(entityClass);

        StringBuilder sql = new StringBuilder("SELECT * from ");
        sql.append("\"").append(table).append("\"");

        List<Constructor<?>> defaultConstructorList = Stream.of(entityClass.getConstructors()).filter((constructor -> constructor.getParameterCount() == 0))
                .collect(Collectors.toList());
        Constructor<?> defaultConstructor = defaultConstructorList.get(0);

        try(Connection connection = ConnectionFactory.getConnection(DB_CONNECTION_PROPS)){
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();

            List<Field> columns = Stream.of(entityClass.getDeclaredFields()).filter((field) -> field.getAnnotation(Column.class) != null)
                    .collect(Collectors.toList());
            List<Method> setters = Stream.of(entityClass.getMethods()).filter((method) -> method.getName().startsWith("set"))
                    .collect(Collectors.toList());
            Map<String, Method> fieldSetters = mapSettersToFieldNames(columns, setters);
            // Create a new instance for every record retrieved
            while (resultSet.next()){
                // Create new object instance
                // Use setters to set values, get the values from the resultSet
                T newInstance = (T)defaultConstructor.newInstance();
                for(int i = 1; i <= resultMetaData.getColumnCount(); i++){
                    String columnName = resultMetaData.getColumnName(i);
                    String columnType = resultMetaData.getColumnTypeName(i);
                    switch (columnType){
                        case "serial":
                        case "int4":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getInt(columnName));
                            break;
                        case "text":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getString(columnName));
                            break;
                        case "numeric":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getFloat(columnName));
                            break;
                        case "boolean":
                            fieldSetters.get(columnName).invoke(newInstance, resultSet.getBoolean(columnName));
                            break;
                    }
                }
                retrievals.add(newInstance);
            }

        } catch (SQLException | ReflectiveOperationException e){
            logger.warn(e.getMessage(), e);
            throw new FailedQueryException();
        }

        if(!retrievals.isEmpty()){
            logger.info("All " + entityClass.getSimpleName() + "s retrieved.");
        }
        else{
            logger.info("No " + entityClass.getSimpleName() + "s in the database.");
        }

        return retrievals;
    }

    /**
     * Updates an existing row in a database table.
     * @param entity an Object with new values to be placed on an existing database row
     * @param keyId the primary key ID of the row to be updated
     * @return a boolean showing if the update was successful or not
     */
    @Override
    public boolean update(Object entity, int keyId){
        String table = entity.getClass().getAnnotation(Table.class).tableName();

        Stream<Field> fieldsStream = Stream.of(entity.getClass().getDeclaredFields());
        List<Field> columns = fieldsStream.filter((field) -> field.getAnnotation(Column.class) != null)
                .collect(Collectors.toList());
        List<Method> getters = Stream.of(entity.getClass().getDeclaredMethods()).filter((method) -> method.getName().startsWith("get"))
                .collect(Collectors.toList());

        Map<Field, Method> fieldGetters = mapGettersToFields(columns, getters);
        String primaryKey = getPrimaryKeyName(entity.getClass());

        StringBuilder sql = new StringBuilder("UPDATE \"");
        sql.append(table);
        sql.append("\" set ");
        Iterator<Field> columnsIterator = columns.iterator();
        try {
            while (columnsIterator.hasNext()) {
                Field field = columnsIterator.next();
                sql.append("\"").append(field.getAnnotation(Column.class).columnName()).append("\"").append("=");
                //String field values require single quotes for the SQL statement
                if (field.getType().equals(String.class)) {
                    sql.append("'").append(fieldGetters.get(field).invoke(entity)).append("'");
                } else {
                    sql.append(fieldGetters.get(field).invoke(entity));
                }

                if (columnsIterator.hasNext()) {
                    sql.append(",");
                }
            }
        } catch (ReflectiveOperationException e){
            logger.error(e.getMessage(), e);
        }
        sql.append(" where \"").append(primaryKey).append("\" =").append(keyId);
        System.out.println(sql);
        int rows = SQLExecutor.doUpdate(sql.toString(), DB_CONNECTION_PROPS);
        if(rows > 0){
            logger.info(entity.getClass().getSimpleName() + "(ID:" + keyId + ") has been successfully updated.");
            return true;
        }
        else{
            logger.warn("No updated records.");
            return false;
        }
    }

    /**
     * Deletes an existing row in a database table.
     * @param entityClass a Class object to reference the table that will be
     * @param keyId the primary key ID of the row to be deleted
     * @return a boolean showing if the deletion was successful or not
     */
    @Override
    public boolean delete(Class<?> entityClass, int keyId) {
        String table = entityClass.getAnnotation(Table.class).tableName();
        String primaryKeyName = getPrimaryKeyName(entityClass);

        StringBuilder sql = new StringBuilder("DELETE from \"");
        sql.append(table);
        sql.append("\" where \"").append(primaryKeyName).append("\"=").append(keyId);
        System.out.println(sql);
        int rows = SQLExecutor.doUpdate(sql.toString(), DB_CONNECTION_PROPS);
        if(rows > 0){
            logger.info(entityClass.getSimpleName() + "(ID:" + keyId + ") has been successfully deleted.");
            return true;
        }
        else{
            logger.warn("Deletion failure.");
            return false;
        }
    }

    /**
     * Private helper method to map getter methods to their respective fields.
     * @param columns a list of Fields representing database columns
     * @param getters a list of getter Methods
     * @return a Map of the matching Fields and getter Methods
     */
    private Map<Field, Method> mapGettersToFields(List<Field> columns, List<Method> getters){
        Map<Field, Method> fieldGetters = new HashMap<>();
        for(Field field : columns){
            for(Method method : getters){
                String getterName = method.getName().substring(3).toLowerCase();
                if(field.getName().toLowerCase().equals(getterName)){
                    fieldGetters.put(field, method);
                    break;
                }
            }
        }
        return fieldGetters;
    }

    /**
     * Private helper method to map the String names of database columns to their respective setter methods.
     * @param columns a list of Fields representing database columns
     * @param setters a list of setter Methods
     * @return a Map of the matching column names and setter Methods
     */
    private Map<String, Method> mapSettersToFieldNames(List<Field> columns, List<Method> setters){
        Map<String, Method> fieldSetters = new HashMap<>();
        for(Field field : columns){
            for(Method method : setters){
                String setterName = method.getName().substring(3).toLowerCase();
                if(field.getName().toLowerCase().equals(setterName)){
                    fieldSetters.put(field.getAnnotation(Column.class).columnName(), method);
                    break;
                }
            }
        }
        return fieldSetters;
    }

    /**
     * Private helper method to get the String name of the primary key of a table.
     * @param entityClass the Class for a database model
     * @return a String of the column name of the primary key of the table
     */
    private String getPrimaryKeyName(Class<?> entityClass){
        Stream<Field> fieldsStream = Arrays.stream(entityClass.getDeclaredFields());
        List<Field> primaryKeys = fieldsStream.filter((field) -> field.getAnnotation(PrimaryKey.class) != null)
                .collect(Collectors.toList());
        return primaryKeys.get(0).getAnnotation(Column.class).columnName();
    }
}
