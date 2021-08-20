package com.revature.orm.mapper;

import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;
import com.revature.orm.annotations.Column;
import com.revature.orm.exceptions.FailedUpdateException;
import com.revature.orm.jdbc.SQLExecutor;
import com.revature.util.ConnectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresORM implements ObjectRelationalMapper{


    /**
     *
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
                //TODO add logging
            }

            if(columnsIterator.hasNext()){
                sql.append(",");
                values.append((','));
            }
        }
        sql.append(") values (");
        sql.append(values);
        sql.append(")");
        System.out.println(sql);
        int rows = SQLExecutor.doUpdate(sql.toString());
        if(rows > 0){
            return true;
        }
        else{
            return false;
            //TODO add logging message notifying that insertion did not work
        }
    }

    /**
     * @return
     */
    @Override
    public <T> Optional<T> get(Class<?> entityClass, int keyId) {
        T newInstance = null;

        String table = entityClass.getAnnotation(Table.class).tableName();
        String primaryKey = getPrimaryKeyName(entityClass);

        StringBuilder sql = new StringBuilder("SELECT * from ");
        sql.append("\"").append(table).append("\"");
        sql.append(" where ");
        sql.append("\"").append(primaryKey).append("\"").append("=").append(keyId);

        System.out.println(sql);

        List<Constructor<?>> defaultConstructorList = Stream.of(entityClass.getConstructors()).filter((constructor -> constructor.getParameterCount() == 0))
                .collect(Collectors.toList());
        Constructor<?> defaultConstructor = defaultConstructorList.get(0);

        try(Connection connection = ConnectionFactory.getConnection()){
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

        } catch (SQLException e){
            e.printStackTrace();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return Optional.of(newInstance);
    }

    /**
     *
     */
    @Override
    public boolean update(Object entity, int keyId) throws IllegalAccessException, InvocationTargetException{
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
        while(columnsIterator.hasNext()){
            Field field = columnsIterator.next();
            sql.append("\"").append(field.getAnnotation(Column.class).columnName()).append("\"").append("=");
            //String field values require single quotes for the SQL statement
            if(field.getType().equals(String.class)){
                sql.append("'").append(fieldGetters.get(field).invoke(entity)).append("'");
            }
            else{
                sql.append(fieldGetters.get(field).invoke(entity));
            }

            if(columnsIterator.hasNext()){
                sql.append(",");
            }
        }
        sql.append(" where \"").append(primaryKey).append("\" =").append(keyId);
        System.out.println(sql);
        int rows = SQLExecutor.doUpdate(sql.toString());
        if(rows > 0){
            return true;
        }
        else{
            return false;
            //TODO add logging for failure
        }
    }

    /**
     *
     */
    @Override
    public boolean delete(Class<?> entityClass, int keyId) throws FailedUpdateException{
        String table = entityClass.getAnnotation(Table.class).tableName();
        String primaryKeyName = getPrimaryKeyName(entityClass);

        StringBuilder sql = new StringBuilder("DELETE from \"");
        sql.append(table);
        sql.append("\" where \"").append(primaryKeyName).append("\"=").append(keyId);
        System.out.println(sql);
        int rows = SQLExecutor.doUpdate(sql.toString());
        if(rows > 0){
            return true;
        }
        else{
            throw new FailedUpdateException();
        }
    }

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

    private Map<String, Class<?>> mapTypeToFieldNames(List<Field> fields){
        Map<String, Class<?>> fieldTypes = new HashMap<>();
        for(Field field : fields){
            fieldTypes.put(field.getAnnotation(Column.class).columnName(), field.getType());
        }
        return fieldTypes;
    }

    private String getPrimaryKeyName(Class<?> entityClass){
        Stream<Field> fieldsStream = Arrays.stream(entityClass.getDeclaredFields());
        List<Field> primaryKeys = fieldsStream.filter((field) -> field.getAnnotation(PrimaryKey.class) != null)
                .collect(Collectors.toList());
        return primaryKeys.get(0).getAnnotation(Column.class).columnName();
    }
}
