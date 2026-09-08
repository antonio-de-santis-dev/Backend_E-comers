package com.it.productservis.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException (String message){
        super(message);
    }

}
