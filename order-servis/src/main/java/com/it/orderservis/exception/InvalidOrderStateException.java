package com.it.orderservis.exception;

public class InvalidOrderStateException  extends RuntimeException{

    public InvalidOrderStateException(String message){
        super(message);
    }
}
