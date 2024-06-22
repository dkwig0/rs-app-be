package com.myorg.core;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Aliaksei Tsvirko
 */
public class ScanItemUtils {
    public static String getString(String field, Map<String, AttributeValue> item) {
        AttributeValue attributeValue = item.get(field);
        if (attributeValue != null) {
            return attributeValue.s();
        }
        return null;
    }

    public static Integer getInteger(String field, Map<String, AttributeValue> item) {
        return getNumber(field, item, Integer::parseInt);
    }

    public static Double getDouble(String field, Map<String, AttributeValue> item) {
        return getNumber(field, item, Double::parseDouble);
    }

    public static BigDecimal getBigDecimal(String field, Map<String, AttributeValue> item) {
        return getNumber(field, item, BigDecimal::new);
    }

    /**
     * Parses string to specific number.
     *
     * @param field name of field
     * @param item scan item
     * @param function number parsing function
     * @return Parsed number
     * @param <T> number type
     */
    private static  <T extends Number> T getNumber(String field, Map<String, AttributeValue> item,
                                                   Function<String, T> function) {
        AttributeValue attributeValue = item.get(field);
        if (attributeValue != null) {
            try {
                return function.apply(attributeValue.n());
            } catch (NumberFormatException ignore) {}
        }
        return null;
    }
}
