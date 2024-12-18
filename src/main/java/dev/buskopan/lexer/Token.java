package dev.buskopan.lexer;

public class Token {
    private final TypeToken type;
    private final String value;

    public Token(TypeToken type, String value) {
        this.type = type;
        this.value = value;
    }

    public TypeToken getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
