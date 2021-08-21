package com.revature.orm.mapper;

import com.revature.orm.exceptions.FailedUpdateException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

/**
 * The Object Relational Mapper provides an interface for persisting and retrieving Java objects
 * to and from a relational database respectively.
 */
public interface ObjectRelationalMapper {
    /**
     *
     */
    boolean insert(Object entity);

    /**
     *
     * @return
     */
    <T> Optional<T> getById(Class<?> entityClass, int keyId);

    /**
     *
     * @param entityClass
     * @param <T>
     * @return
     */
    <T> List<T> getAll(Class<?> entityClass);

    /**
     *
     */
    boolean update(Object entity, int keyId);

    /**
     *
     */
    boolean delete(Class<?> entityClass, int keyId);
}
