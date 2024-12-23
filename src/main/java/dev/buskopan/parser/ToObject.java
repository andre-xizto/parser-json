package dev.buskopan.parser;

import dev.buskopan.annotation.JsonFieldAnnotation;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                .filter(obj -> obj instanceof Map<?, ?> || obj instanceof List<?>)
                .map(el -> convertMap((Map<?, ?>) el, target))
                .collect(Collectors.toList());
    }

    private static <T> T  convertMap(Map<?, ?> map, Class<T> target) {
        try {
            if (map == null || map.isEmpty()) {
                throw new ConvertToObjectException("Input map cannot be null or empty");
            }

            T instance = target.getConstructor().newInstance();

            for (Field field : target.getDeclaredFields()) {
                field.setAccessible(true);

                JsonFieldAnnotation annotation = field.getAnnotation(JsonFieldAnnotation.class);
                String jsonKey = annotation != null ? annotation.value() : field.getName();

                Object value = map.get(jsonKey);
                Class<?> fieldType = field.getType();

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
                else if (value instanceof Map<?, ?> nestedMap) {
                    Object nestedObject = convert(nestedMap, field.getType());
                    field.set(instance, nestedObject);
                }
                else if (value instanceof List<?> nestedList) {

                    if (List.class.isAssignableFrom(fieldType)) {
                        var clazz = getListComponentType(field);
                        // Converte para lista
                        List<Object> list = nestedList.stream()
                                .map(el -> {
                                    if (el instanceof Map<?, ?> nestedMap) {
                                        // Se for um objeto, converta recursivamente
                                        return convert(nestedMap, clazz);
                                    } else {
                                        // Se for um valor primitivo, parseie diretamente
                                        return parseValue(el, clazz);
                                    }
                                })
                                .collect(Collectors.toList());
                        field.set(instance, list);
                    }
                }
                else {
                    field.set(instance, parseValue(value,fieldType));
                }
            }

            return instance;
        } catch (Exception ex) {
            throw new ConvertToObjectException("Error during object conversion: " + ex.getMessage());
        }
    }

    private static Class<?> getListComponentType(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length == 1) {
                    Type componentType = typeArguments[0];
                    if (componentType instanceof Class<?> clazz) {
                        return clazz;
                    }
                }
            }
        }
        return Object.class;
    }

    private static Object parseValue(Object value, Class<?> type) {
        if (value == null || type == null) {
            return null;
        }

        if (type.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (type == String.class) {
            return String.valueOf(value);
        }

        if (type == Integer.class || type == int.class) {
            return Integer.valueOf(value.toString());
        }

        if (type == Float.class || type == float.class) {
            return Float.valueOf(value.toString());
        }

        if (type == Double.class || type == double.class) {
            if (value instanceof Number number) {
                return Double.valueOf(String.valueOf(number));
            }
            return Double.valueOf(String.valueOf(value));
        }

        if (type == Long.class || type == long.class) {
            if (value instanceof Number number) {
                return Long.valueOf(String.valueOf(number));
            }
            return Long.valueOf(String.valueOf(value));
        }

        if (type == Boolean.class || type == boolean.class) {
            return Boolean.valueOf(value.toString());
        }

        throw new ConvertToObjectException("cannot parse value " + value + " of type " + type);
    }
}
