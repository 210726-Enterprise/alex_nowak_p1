package com.revature.orm;

/**
 * The Object Relational Mapper provides an interface for persisting and retrieving Java objects
 * to and from a relational database respectively.
 */
public interface ObjectRelationalMapper {
    /**
     *
     */
    void insert(Object entity) throws IllegalAccessException;

    /**
     *
     * @return
     */
    <T> T get(String entityType, int keyId);

    /**
     *
     */
    void update(Object entity, int keyId);

    /**
     *
     */
    void delete(String entityType, int keyId);
}
