package com.revature.orm.exceptions;

public class FailedUpdateException extends Exception{
    public FailedUpdateException(){
        super("Failed to update the database.");
    }

}
