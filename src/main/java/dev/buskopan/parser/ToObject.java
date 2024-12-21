package dev.buskopan.parser;

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
        return objects.stream().map(el -> convertMap((Map<?, ?>) el, target)).collect(Collectors.toList());
    }

    private static  <T> T convertMap(Map<?, ?> map, Class<T> target) {
        try {
            T instance = target.getConstructor().newInstance();

            for (Map.Entry entries : map.entrySet()) {
                String key = (String) entries.getKey();
                Object value = entries.getValue();

                Field field;

                try {
                    field = target.getDeclaredField(key);
                } catch (Exception ex) {
                    continue;
                }

                field.setAccessible(true);

                if (value instanceof Map<?, ?> otherMap) {
                    Class<?> fieldType = field.getType();
                    Object nestedObject = convert(otherMap, fieldType);
                    field.set(instance, nestedObject);
                } else {
                    if (value == null || "null".equals(value)) {
                        field.set(instance,null);
                        continue;
                    }
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error when convert object " + ex.getMessage() + " > " + ex.getCause());
        }
    }

}
