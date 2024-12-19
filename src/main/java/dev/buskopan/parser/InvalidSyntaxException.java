package dev.buskopan.parser;

public class InvalidSyntaxException extends RuntimeException{
    public InvalidSyntaxException(String msg) {
        super(msg);
    }
}
