package com.it.userservis.exception;

public class AccountDisabledException extends RuntimeException{

    public AccountDisabledException(String message){
        super(message);
    }
}
