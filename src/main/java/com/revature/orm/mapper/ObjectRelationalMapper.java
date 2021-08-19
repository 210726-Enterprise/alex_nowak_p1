package com.revature.orm.mapper;

import com.revature.orm.exceptions.FailedUpdateException;

import java.lang.reflect.InvocationTargetException;

/**
 * The Object Relational Mapper provides an interface for persisting and retrieving Java objects
 * to and from a relational database respectively.
 */
public interface ObjectRelationalMapper {
    /**
     *
     */
    boolean insert(Object entity) throws IllegalAccessException, InvocationTargetException;

    /**
     *
     * @return
     */
    <T> T get(Class<?> entityClass, int keyId);

    /**
     *
     */
    boolean update(Object entity, int keyId) throws IllegalAccessException, InvocationTargetException;

    /**
     *
     */
    boolean delete(Class<?> entityClass, int keyId) throws FailedUpdateException;
}
