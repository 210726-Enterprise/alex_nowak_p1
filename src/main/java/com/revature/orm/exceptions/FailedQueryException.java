package com.revature.orm.exceptions;

public class FailedQueryException extends Exception{
    public FailedQueryException(){
        super("Failed to retrieve the query from the database.");
    }

}
