package com.revature.orm.mapper;

import com.revature.orm.annotations.Table;
import com.revature.orm.annotations.Column;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectRelationalMapperImpl implements ObjectRelationalMapper{

    /**
     *
     */
    @Override
    public void insert(Object entity) throws IllegalAccessException{
        //Get the table that we will insert into
        String table = entity.getClass().getAnnotation(Table.class).tableName();
        //Get a stream of all the entity fields and filter it down to only fields
        //with a @Column annotation
        Stream<Field> columnsStream = Stream.of(entity.getClass().getDeclaredFields());
        List<Field> columns = columnsStream.filter((f) -> f.getAnnotation(Column.class) != null)
                .collect(Collectors.toList());
        StringBuilder sql = new StringBuilder("INSERT into \"" + table + "\" (");
        StringBuilder values = new StringBuilder();
        for(Field field : columns){
            sql.append(field.getAnnotation(Column.class).columnName() + ",");
            values.append(field.get(entity) + ",");
        }
        sql.append(") values (");
        sql.append(values);
        sql.append(")");
    }

    /**
     * @return
     */
    @Override
    public <T> T get(String entityType, int keyId) {
        return null;
    }

    /**
     *
     */
    @Override
    public void update(Object entity, int keyId) {

    }

    /**
     *
     */
    @Override
    public void delete(String entityType, int keyId) {

    }
}
