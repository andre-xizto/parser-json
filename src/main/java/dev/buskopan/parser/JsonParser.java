package dev.buskopan.parser;

import dev.buskopan.exception.InvalidSyntaxException;
import dev.buskopan.internal.lexer.Lexer;
import dev.buskopan.internal.lexer.Token;
import dev.buskopan.internal.lexer.TypeToken;
import dev.buskopan.internal.parser.ToObject;

import java.util.*;

public class JsonParser {

    private final Lexer lexer;
    private final ToObject toObject;
    private int structure = 0;
    private static JsonParser instance;

    private JsonParser(Lexer lexer, ToObject toObject) {
        this.lexer = lexer;
        this.toObject = toObject;
    }

    public static JsonParser getInstance() {
        if (instance == null) {
            instance = new JsonParser(Lexer.getInstance(), ToObject.getInstance());
        }
        return instance;
    }

    public <T> T parseSingle(String json, Class<T> targetClass) {
        List<Token> tokenize = lexer.tokenize(json);
        Object parse = parse(tokenize);
        T converted = toObject.convert(parse, targetClass);
        return converted;
    }

    public <T> List<T> parseList(String json, Class<T> targetClass) {
        List<Token> tokenize = lexer.tokenize(json);
        List<?> parse = (List<?>) parse(tokenize);
        List<T> converted = toObject.convertList(parse, targetClass);
        return converted;
    }

    private Object parse(List<Token> tokens) {
        Iterator<Token> iterator = tokens.iterator();

        if (!iterator.hasNext()) {
            throw new InvalidSyntaxException("empty JSON");
        }

        Token first = consume(iterator);

        if (first.getType().equals(TypeToken.INICIO_OBJETO)) {
            return parseObject(iterator);
        }

        if (first.getType().equals(TypeToken.INICIO_ARRAY)) {
            return parseArray(iterator);
        }

        throw new InvalidSyntaxException("JSON should start with { or [");

    }

    private Token consume(Iterator<Token> iterator) {
        Token token = iterator.next();
        structure++;
        return token;
    }

    private List<Object> parseArray(Iterator<Token> iterator) {
        List<Object> list = new ArrayList<>();

        while (iterator.hasNext()) {
            Token token = consume(iterator);
            if (token.getType().equals(TypeToken.FIM_ARRAY)) {
                return list;
            }

            Object value = parseValue(token, iterator);
            list.add(value);

            Token nextToken = consume(iterator);

            if (nextToken.getType().equals(TypeToken.FIM_ARRAY)) {
                return list;
            }

            if (!nextToken.getType().equals(TypeToken.VIRGULA)) {
                throw new InvalidSyntaxException("expected ',' or ']' at structure " + structure);
            }

        }

        throw new InvalidSyntaxException("expected ] or value");
    }

    private Map<String, Object> parseObject(Iterator<Token> iterator) {
        Map<String, Object> map = new HashMap<>();

        if (!iterator.hasNext()) {
            throw new InvalidSyntaxException("Expected } or string");
        }

        while(iterator.hasNext()) {
            Token token = consume(iterator);

            if (token.getType().equals(TypeToken.FIM_OBJETO)) {
                return map;
            }

            if (!token.getType().equals(TypeToken.TEXTO)) {
                throw new InvalidSyntaxException("Expected: string");
            }

            String key = token.getValue();

            if (!iterator.hasNext() || !consume(iterator).getType().equals(TypeToken.DOIS_PONTO)) {
                throw new InvalidSyntaxException("expected: ';' after json key");
            }

            Token valueToken = consume(iterator);
            Object value = parseValue(valueToken, iterator);

            map.put(key,value);

            if (iterator.hasNext()) {
                Token nextToken = consume(iterator);
                if (nextToken.getType().equals(TypeToken.FIM_OBJETO)) {
                    return map;
                }

                if (!nextToken.getType().equals(TypeToken.VIRGULA)) {
                    throw new InvalidSyntaxException("Expected ',' or '}'");
                }
            } else {
                throw new InvalidSyntaxException("Expected } or comma");
            }
        }

        return null;
    }

    private Object parseValue(Token valueToken, Iterator<Token> iterator) {
        String value = valueToken.getValue();
        TypeToken type = valueToken.getType();
        return switch (type) {
            case BOOLEANO -> Boolean.valueOf(value);
            case TEXTO -> value;
            case NUMERO -> {
                if (value.contains(".")) {
                    yield Double.valueOf(value);
                }
             yield Long.valueOf(value);
            }
            case INICIO_OBJETO -> parseObject(iterator);
            case INICIO_ARRAY -> parseArray(iterator);
            case NULO -> null;
            default -> throw new InvalidSyntaxException("invalid token: " + type.name());
        };
    }
}
