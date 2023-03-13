package ru.yojo.yamltopojo.util;

import ru.yojo.yamltopojo.constants.ConstantsEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.lineSeparator;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static ru.yojo.yamltopojo.constants.ConstantsEnum.*;

public class MapperUtil {

    public static Set<String> getSetValueIfExistsOrElseEmptySet(String value, Map<String, Object> schemaMap) {
        Set<String> values = new HashSet<>();
        if (schemaMap.containsKey(value)) {
            values.addAll((ArrayList<String>) schemaMap.get(value));
        }
        return values;
    }

    public static String getStringValueIfExistOrElseNull(ConstantsEnum valueEnum, Map<String, Object> map) {
        if (map.containsKey(valueEnum.getValue())) {
            return map.get(valueEnum.getValue()).toString();
        }
        return null;
    }

    public static Map<String, Object> castObjectToMap(Object map) {
        return (Map<String, Object>) map;
    }

    public static String refReplace(String ref) {
        return capitalize(ref.replaceAll(".+/", ""));
    }

    public static String generateSetter(String type, String variableName) {
        return SETTER.getValue().formatted(capitalize(variableName), capitalize(type), variableName, variableName, variableName);
    }

    public static String generateGetter(String type, String variableName) {
        return GETTER.getValue().formatted(capitalize(type), capitalize(variableName), variableName);
    }

    public static void generateValidAnnotation(StringBuilder stringBuilder) {
        stringBuilder.append(lineSeparator())
                .append(VALID_ANNOTATION.getValue());
    }
}
