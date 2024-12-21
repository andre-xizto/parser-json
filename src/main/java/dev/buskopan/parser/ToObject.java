package dev.buskopan.parser;

import dev.buskopan.annotation.JsonFieldAnnotation;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToObject {

    public static <T> T convert(Object object, Class<T> target) {
        if (object instanceof Map<?, ?> map) {
            return convertMap(map, target);
        }

        throw new ConvertToObjectException("Couldn't convert an instance of " + object.getClass().getName());
    }

    public static <T> List<T> convertList(List<?> objects, Class<T> target) {
        return objects.stream()
                .filter(obj -> obj instanceof Map<?, ?>) // Garante que apenas mapas são processados
                .map(el -> convertMap((Map<?, ?>) el, target))
                .collect(Collectors.toList());
    }

    private static <T> T convertMap(Map<?, ?> map, Class<T> target) {
        try {
            if (map == null || map.isEmpty()) {
                throw new IllegalArgumentException("Input map cannot be null or empty");
            }

            T instance = target.getConstructor().newInstance();

            for (Field field : target.getDeclaredFields()) {
                field.setAccessible(true);

                JsonFieldAnnotation annotation = field.getAnnotation(JsonFieldAnnotation.class);
                String jsonKey = annotation != null ? annotation.value() : field.getName();

                Object value = map.get(jsonKey);

                // Se for um campo composto
                if (annotation != null && annotation.composite().length > 0) {
                    if (value instanceof Map<?,?> nestedMap) {
                        StringBuilder sb = new StringBuilder();

                        for (String compositeKey : annotation.composite()) {
                            Object part = nestedMap.get(compositeKey);
                            if (part != null) {
                                sb.append(part).append(" ");
                            }
                        }

                        field.set(instance, sb.toString().trim().isEmpty() ? null : sb.toString().trim());
                    } else if (value instanceof List<?>) {
                        throw new ConvertToObjectException("cannot use JsonField annotation on fields that are arrays!");
                    }
                }
                // Se for um objeto aninhado
                else if (value instanceof Map<?, ?> nestedMap) {
                    Object nestedObject = convert(nestedMap, field.getType());
                    field.set(instance, nestedObject);
                }
                // Se for um valor nulo ou "null"
                else if (value == null || "null".equals(value)) {
                    field.set(instance, null);
                }
                // Se for um valor normal
                else {
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (Exception ex) {
            throw new RuntimeException("Error during object conversion: " + ex.getMessage(), ex);
        }
    }
}
