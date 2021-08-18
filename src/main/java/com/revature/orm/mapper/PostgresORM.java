package com.revature.orm.mapper;

import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;
import com.revature.orm.annotations.Column;
import com.revature.orm.jdbc.SQLExecutor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresORM implements ObjectRelationalMapper{


    /**
     *
     */
    @Override
    public void insert(Object entity) throws IllegalAccessException, InvocationTargetException {
        //Get the table that we will insert into
        String table = entity.getClass().getAnnotation(Table.class).tableName();
        //Get a stream of all the entity fields and filter it down to only fields
        //with a @Column annotation, then get the collection as a List
        Stream<Field> fieldsStream = Stream.of(entity.getClass().getDeclaredFields());
        List<Field> columns = fieldsStream.filter((field) -> field.getAnnotation(Column.class) != null)
                                            .collect(Collectors.toList());
        List<Method> getters = Stream.of(entity.getClass().getDeclaredMethods()).filter((method) -> method.getName().startsWith("get"))
                                                                                .collect(Collectors.toList());

        Map<Field, Method> fieldGetters = new HashMap<>();
        for(Field field : columns){
            for(Method method : getters){
                String getterField = method.getName().substring(3).toLowerCase();
                if(field.getName().equals(getterField)){
                    fieldGetters.put(field, method);
                    break;
                }
            }
        }

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
        SQLExecutor.doUpdate(sql.toString());
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
        //ResultSet resultSet = SQLExecutor.doQuery(sql.toString());

        return null;
    }

    /**
     *
     */
    @Override
    public void update(Object entity, int keyId) throws IllegalAccessException, InvocationTargetException{
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
        //SQLExecutor.doUpdate(sql.toString());
    }

    /**
     *
     */
    @Override
    public void delete(Class<?> entityClass, int keyId) throws IndexOutOfBoundsException{
        String table = entityClass.getAnnotation(Table.class).tableName();
        String primaryKeyName = getPrimaryKeyName(entityClass);

        StringBuilder sql = new StringBuilder("DELETE from \"");
        sql.append(table);
        sql.append("\" where \"").append(primaryKeyName).append("\"=").append(keyId);
        System.out.println(sql);
        //SQLExecutor.doUpdate(sql.toString());
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
