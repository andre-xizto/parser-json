package dev.buskopan.lexer;

import dev.buskopan.exception.InvalidCharacterException;
import dev.buskopan.internal.lexer.Lexer;
import dev.buskopan.internal.lexer.Token;
import dev.buskopan.internal.lexer.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {
    
    private static Lexer lexer;
    
    @BeforeAll
    public static void setup() {
        if (lexer == null) {
            lexer = Lexer.getInstance();
        }
    }

    @Test
    public void checkJsonTokens() {
        String json = """
                {
                    "nome":"andré",
                    "idade": 12
                }
                """;
        List<Token> tokens = lexer.tokenize(json);
        assertEquals(9, tokens.size());
        assertEquals(TypeToken.INICIO_OBJETO, tokens.getFirst().getType());
        assertEquals(TypeToken.FIM_OBJETO, tokens.getLast().getType());
        assertEquals(TypeToken.TEXTO, tokens.get(1).getType());
        assertEquals(TypeToken.DOIS_PONTO, tokens.get(2).getType());
        assertEquals(TypeToken.TEXTO, tokens.get(3).getType());
        assertEquals(TypeToken.VIRGULA, tokens.get(4).getType());
        assertEquals(TypeToken.TEXTO, tokens.get(5).getType());
        assertEquals(TypeToken.DOIS_PONTO, tokens.get(6).getType());
        assertEquals(TypeToken.NUMERO, tokens.get(7).getType());

    }

    @Test
    public void checkEmptyJson() {
        String json = "";
        List<Token> tokens = lexer.tokenize(json);
        assertTrue(tokens.isEmpty());
    }

    @Test
    public void checkInvalidJson() {
        String json1 = """
               {
                    'nome':'andré',
                    'idade': 12
               }
                """;
        String json2 = """
               {
                    "nome":NOT TRUE,
                    "idade": 12
               }
                """;
        String json3 = """
               {
                    "nome";"andré",
                    "idade; 12
               }
                """;
        String json4 = """
               {
                    "nome":"andré"
                    "idade": &12
               }
                """;

        assertThrows(InvalidCharacterException.class, () -> lexer.tokenize(json1));
        assertThrows(InvalidCharacterException.class, () -> lexer.tokenize(json2));
        assertThrows(InvalidCharacterException.class, () -> lexer.tokenize(json3));
        assertThrows(InvalidCharacterException.class, () -> lexer.tokenize(json4));
    }

}