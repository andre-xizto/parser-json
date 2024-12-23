package dev.buskopan.lexer;

import dev.buskopan.exception.InvalidCharacterException;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        char[] chars = input.toCharArray();
        int i = 0;

        while (i < chars.length) {
            char c = chars[i];

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            switch (c) {
                case '{':
                    tokens.add(new Token(TypeToken.INICIO_OBJETO, String.valueOf(c)));
                    i++;
                    break;
                case '}':
                    tokens.add(new Token(TypeToken.FIM_OBJETO, String.valueOf(c)));
                    i++;
                    break;
                case '[':
                    tokens.add(new Token(TypeToken.INICIO_ARRAY, String.valueOf(c)));
                    i++;
                    break;
                case ']':
                    tokens.add(new Token(TypeToken.FIM_ARRAY, String.valueOf(c)));
                    i++;
                    break;
                case ',':
                    tokens.add(new Token(TypeToken.VIRGULA, String.valueOf(c)));
                    i++;
                    break;
                case ':':
                    tokens.add(new Token(TypeToken.DOIS_PONTO, String.valueOf(c)));
                    i++;
                    break;
                case '"':
                    StringBuilder sb = new StringBuilder();
                    i++;
                    while (i < chars.length && chars[i] != '"') {
                        sb.append(chars[i]);
                        i++;
                    }
                    i++;
                    tokens.add(new Token(TypeToken.TEXTO, sb.toString()));
                    break;
                default:
                    if (Character.isDigit(c)) {
                        StringBuilder numbers = new StringBuilder();
                        while (i < chars.length && (Character.isDigit(chars[i]) || chars[i] == '.') ) {
                            numbers.append(chars[i]);
                            i++;
                        }
                        tokens.add(new Token(TypeToken.NUMERO, numbers.toString()));
                    } else if (input.startsWith("true", i) || input.startsWith("false", i)) {
                        String value = input.startsWith("true", i) ? "true" : "false";
                        tokens.add(new Token(TypeToken.BOOLEANO, value));
                        i += value.length();
                    } else if (input.startsWith("null", i)) {
                        tokens.add(new Token(TypeToken.NULO, "null"));
                        i += "null".length();
                    } else {
                        throw new InvalidCharacterException("Character invalid " + c);
                    }
            }

        }


        return tokens;
    }
}
