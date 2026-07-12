package nexa.framework.runtime.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DeepCopyUtil {

    private DeepCopyUtil() {
    }

    public static Map<String, Object> deepCopyMap(Map<String, Object> input) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    public static Object deepCopyValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character
                || value instanceof BigDecimal || value instanceof BigInteger) {
            return value;
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copied = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copied.put(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
            }
            return copied;
        }

        if (value instanceof List<?> list) {
            List<Object> copied = new ArrayList<>();
            for (Object item : list) {
                copied.add(deepCopyValue(item));
            }
            return copied;
        }

        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            List<Object> copied = new ArrayList<>(array.length);
            for (Object item : array) {
                copied.add(deepCopyValue(item));
            }
            return copied;
        }

        throw new IllegalArgumentException("Unsupported message value type: " + value.getClass().getName());
    }
}
