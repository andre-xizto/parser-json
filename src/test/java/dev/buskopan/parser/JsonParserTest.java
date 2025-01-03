    package dev.buskopan.parser;

    import dev.buskopan.annotation.JsonFieldAnnotation;
    import dev.buskopan.exception.ConvertToObjectException;
    import dev.buskopan.exception.InvalidSyntaxException;
    import dev.buskopan.internal.lexer.Lexer;
    import dev.buskopan.internal.parser.ToObject;
    import org.junit.jupiter.api.BeforeAll;
    import org.junit.jupiter.api.Test;

    import java.util.List;

    import static org.junit.jupiter.api.Assertions.*;

    class JsonParserTest {

        private static JsonParser jsonParser;
        private static Lexer lexer;
        private static ToObject toObject;

        @BeforeAll
        public static void setup() {

            if (lexer == null ) {
                lexer = Lexer.getInstance();
            }

            if (toObject == null) {
                toObject = ToObject.getInstance();
            }

            if (jsonParser == null) {
                jsonParser = JsonParser.getInstance();
            }
        }

        public static class Address {
            private String estado;
            private String cidade;

            public Address() {
            }

            public String getEstado() {
                return estado;
            }

            public void setEstado(String estado) {
                this.estado = estado;
            }

            public String getCidade() {
                return cidade;
            }

            public void setCidade(String cidade) {
                this.cidade = cidade;
            }
        }

        public static class Order {
            private long id;
            private List<Product> products;
            private List<Integer> numbers;

            public Order() {
            }

            public List<Integer> getNumbers() {
                return numbers;
            }

            public void setNumbers(List<Integer> numbers) {
                this.numbers = numbers;
            }

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public List<Product> getProducts() {
                return products;
            }

            public void setProducts(List<Product> products) {
                this.products = products;
            }

            @Override
            public String toString() {
                return "Order{" +
                        "id=" + id +
                        ", products=" + products +
                        '}';
            }
        }

        public static class User {
            private String name;
            private String email;
            private Address address;
            private int age;

            public User() {
            }

            public int getAge() {
                return age;
            }

            public void setAge(int age) {
                this.age = age;
            }

            public Address getAddress() {
                return address;
            }

            public void setAddress(Address address) {
                this.address = address;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }
        }

        public static class Product {
            private Long id;
            private String name;

            @JsonFieldAnnotation(value = "custom_field")
            private String reference;

            @JsonFieldAnnotation(value = "date", composite = {"day", "month"})
            private String bought_at;

            public Product() {
            }

            public String getBought_at() {
                return bought_at;
            }

            public void setBought_at(String bought_at) {
                this.bought_at = bought_at;
            }

            public String getReference() {
                return reference;
            }

            public void setReference(String reference) {
                this.reference = reference;
            }

            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        @Test
        public void checkSimpleValidJson() {

            String json = """
                    {
                    "name": "teste",
                    "email": "email@email.com",
                    "age": 20
                    }
                    """;

            User user = jsonParser.parseSingle(json, User.class);
            assertNotNull(user.getEmail());
            assertNotNull(user.getName());
            assertEquals(20, user.getAge());
            assertEquals("teste", user.getName());
            assertEquals("email@email.com", user.getEmail());
        }

        @Test
        public void checkNestedValidJson() {

            String json = """
                    {
                    "name": "teste",
                    "email": "email@email.com",
                    "age": 20,
                    "address": {"estado": "RJ", "cidade": "Rio de Janeiro"}
                    }
                    """;

            User user = jsonParser.parseSingle(json, User.class);
            assertNotNull(user.getEmail());
            assertNotNull(user.getName());
            assertNotNull(user.getAddress());
            assertEquals("teste", user.getName());
            assertEquals("email@email.com", user.getEmail());
            assertEquals("RJ", user.getAddress().getEstado());
            assertEquals("Rio de Janeiro", user.getAddress().getCidade());
        }

        @Test
        public void checkValidJsonWithArray() {

            String json = """
                    {
                    "id": 64747457,
                    "numbers": [1,2,3,4,5]
                    }
                    """;

            Order order = jsonParser.parseSingle(json, Order.class);
            assertEquals(64747457, order.getId());
            assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, order.getNumbers().toArray());
        }

        @Test
        public void checkValidJsonWithObjectArray() {

            String json = """
                    {
                    "id": 64747457,
                    "products": [
                        {
                            "id": 12312,
                            "name": "teste"
                        }
                    ]
                    }
                    """;

            Order order = jsonParser.parseSingle(json, Order.class);
            assertEquals(64747457, order.getId());
            assertEquals(12312, order.getProducts().getFirst().getId());
            assertEquals("teste", order.getProducts().getFirst().getName());
        }

        @Test
        public void checkValidArrayJson() {

            String json = """
                    [
                        {
                            "id": 64747457,
                            "products": [
                                {
                                    "id": 12312,
                                    "name": "teste"
                                }
                            ]
                        }
                    ]
                    """;

            List<Order> order = jsonParser.parseList(json, Order.class);
            assertEquals(64747457, order.getFirst().getId());
            assertEquals(12312, order.getFirst().getProducts().getFirst().getId());
            assertEquals("teste", order.getFirst().getProducts().getFirst().getName());
        }


        @Test
        public void checkEmptyJson() {

            String json = """
                    {}
                    """;

            assertThrows(ConvertToObjectException.class, () -> jsonParser.parseSingle(json, User.class));
        }

        @Test
        public void checkInvalidJson() {
            String json = """
                    {
                    "name": "André"
                    "age": 20
                    }
                    """;

            assertThrows(InvalidSyntaxException.class, () -> jsonParser.parseSingle(json, User.class));
        }

        @Test
        public void checkAnnotationJsonField() {
            String json = """
                    {
                                    "id": 12312,
                                    "name": "teste",
                                    "custom_field": "3232d3f3j"
                    }
                    """;

            Product product = jsonParser.parseSingle(json, Product.class);
            assertEquals("3232d3f3j", product.getReference());
        }

        @Test
        public void checkAnnotationJsonFieldWithComposite() {
            String json = """
                    {
                                    "id": 12312,
                                    "name": "teste",
                                    "date": {
                                        "day": 1,
                                        "month": "February"
                                    }
                    }
                    """;

            Product product = jsonParser.parseSingle(json, Product.class);
            assertEquals("1 February", product.getBought_at());
        }

        @Test
        public void checkJsonWithDifferentTypeFromObject() {
            String json = """
                    {
                        "name": "user",
                        "email": "email@mail.com",
                        "age": 12.5
                    }
                    """;

            assertThrows(ConvertToObjectException.class, () -> jsonParser.parseSingle(json, User.class));
        }

        @Test
        public void checkJsonWithEmptyArray() {
            String json = """
                    {
                        "id": 12312,
                        "products": []
                    }
                    """;
            Order order = jsonParser.parseSingle(json, Order.class);
            assertTrue(order.products.isEmpty());
            assertEquals(12312, order.getId());
        }

        @Test
        public void checkJsonWithMixedArrays() {
            String json = """
            {
                "id": 12345,
                "products": [
                    {"id": 1, "name": "item1"},
                    {"id": 2, "name": "item2"}
                ],
                "numbers": [1, 2, 3]
            }
            """;

            Order order = jsonParser.parseSingle(json, Order.class);
            assertEquals(12345, order.getId());
            assertEquals(2, order.getProducts().size());
            assertEquals(3, order.getNumbers().size());
            assertEquals(1, order.getNumbers().get(0));
            assertEquals("item1", order.getProducts().get(0).getName());
        }

        @Test
        public void checkEmptyJsonArray() {
            String json = "[]";

            List<Order> orders = jsonParser.parseList(json, Order.class);
            assertTrue(orders.isEmpty());
        }

    }