package com.revature.orm.mapper;

import java.lang.reflect.InvocationTargetException;

/**
 * The Object Relational Mapper provides an interface for persisting and retrieving Java objects
 * to and from a relational database respectively.
 */
public interface ObjectRelationalMapper {
    /**
     *
     */
    void insert(Object entity) throws IllegalAccessException, InvocationTargetException;

    /**
     *
     * @return
     */
    <T> T get(Class<?> entityClass, int keyId);

    /**
     *
     */
    void update(Object entity, int keyId) throws IllegalAccessException, InvocationTargetException;

    /**
     *
     */
    void delete(Class<?> entityClass, int keyId);
}
