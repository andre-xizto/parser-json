package dev.buskopan.exception;

public class InvalidCharacterException extends RuntimeException{
    private String msg;

    public InvalidCharacterException(String msg) {
        this.msg = msg;
    }
}
