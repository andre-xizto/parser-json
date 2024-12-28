package dev.buskopan.internal.lexer;

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

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
