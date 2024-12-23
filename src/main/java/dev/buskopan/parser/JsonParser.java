package dev.buskopan.parser;

import dev.buskopan.exception.InvalidSyntaxException;
import dev.buskopan.lexer.Lexer;
import dev.buskopan.lexer.Token;
import dev.buskopan.lexer.TypeToken;

import java.util.*;

public class JsonParser {

    private static int structure = 0;

    public static <T> T parseSingle(String json, Class<T> targetClass) {
        List<Token> tokenize = Lexer.tokenize(json);
        Object parse = JsonParser.parse(tokenize);
        T converted = ToObject.convert(parse, targetClass);
        return converted;
    }

    public static <T> List<T> parseList(String json, Class<T> targetClass) {
        List<Token> tokenize = Lexer.tokenize(json);
        List<?> parse = (List<?>) JsonParser.parse(tokenize);
        List<T> converted = ToObject.convertList(parse, targetClass);
        return converted;
    }

    private static Object parse(List<Token> tokens) {
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

    private static Token consume(Iterator<Token> iterator) {
        Token token = iterator.next();
        structure++;
        return token;
    }

    private static List<Object> parseArray(Iterator<Token> iterator) {
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

    private static Map<String, Object> parseObject(Iterator<Token> iterator) {
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

    private static Object parseValue(Token valueToken, Iterator<Token> iterator) {
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
