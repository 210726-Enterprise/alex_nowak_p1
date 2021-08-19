package com.revature.orm.mapper;

import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;
import com.revature.orm.annotations.Column;
import com.revature.orm.exceptions.FailedUpdateException;
import com.revature.orm.jdbc.SQLExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresORM implements ObjectRelationalMapper{


    /**
     *
     */
    @Override
    public boolean insert(Object entity) throws IllegalAccessException, InvocationTargetException {
        //Get the table that we will insert into
        String table = entity.getClass().getAnnotation(Table.class).tableName();
        //Get a stream of all the entity fields and filter it down to only fields
        //with a @Column annotation, then get the collection as a List
        Stream<Field> fieldsStream = Stream.of(entity.getClass().getDeclaredFields());
        List<Field> columns = fieldsStream.filter((field) -> field.getAnnotation(Column.class) != null)
                                            .collect(Collectors.toList());
        List<Method> getters = Stream.of(entity.getClass().getDeclaredMethods()).filter((method) -> method.getName().startsWith("get"))
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
            if(field.getType().equals(String.class)){
                values.append("'");
                values.append(fieldGetters.get(field).invoke(entity));
                values.append("'");
            }
            else{
                values.append(fieldGetters.get(field).invoke(entity));
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
    public <T> T get(Class<?> entityClass, int keyId) {
        String table = entityClass.getAnnotation(Table.class).tableName();
        String primaryKey = getPrimaryKeyName(entityClass);

        StringBuilder sql = new StringBuilder("SELECT * from ");
        sql.append("\"").append(table).append("\"");
        sql.append(" where ");
        sql.append("\"").append(primaryKey).append("\"").append("=").append(keyId);

        System.out.println(sql);
        //TODO build out new Object from returned values
        ResultSet resultSet = SQLExecutor.doQuery(sql.toString());
//        List<Constructor<?>> defaultConstructorList = Stream.of(entityClass.getConstructors()).filter((constructor -> constructor.getParameterCount() == 0))
//                                                .collect(Collectors.toList());
//        Constructor<?> defaultConstructor = defaultConstructorList.get(0);
        try {
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        // Create new object instance
        // Use setters to set values, get the values from the resultSet

        return null;
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
                String getterField = method.getName().substring(3).toLowerCase();
                if(field.getName().toLowerCase().equals(getterField)){
                    fieldGetters.put(field, method);
                    break;
                }
            }
        }
        return fieldGetters;
    }

    private String getPrimaryKeyName(Class<?> entityClass){
        Stream<Field> fieldsStream = Arrays.stream(entityClass.getDeclaredFields());
        List<Field> primaryKeys = fieldsStream.filter((field) -> field.getAnnotation(PrimaryKey.class) != null)
                .collect(Collectors.toList());
        return primaryKeys.get(0).getAnnotation(Column.class).columnName();
    }
}
