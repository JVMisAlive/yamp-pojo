package ru.yojo.yamltopojo.generator;

import org.apache.commons.lang3.StringUtils;
import ru.yojo.yamltopojo.constants.ConstantsEnum;
import ru.yojo.yamltopojo.domain.LombokProperties;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.yojo.yamltopojo.constants.ConstantsEnum.*;
import static ru.yojo.yamltopojo.constants.ConstantsEnum.JAVA_DOC_END;
import static ru.yojo.yamltopojo.util.MapperUtil.*;

@SuppressWarnings("all")
public class OldGenerator {
    //StringBuilderOnly generator
    private void generateAndWriteToFile(String outputDirectory, LombokProperties lombokProperties, Map<String, Object> schemasMap, String schema, Object schemaValues) {
        String schemaName = capitalize(schema);
//        System.out.println(schemaName);
//        System.out.println(schemaValues);

        StringBuilder stringBuilder = getClassBuilder(schemaName);

        Set<String> requiredAttributes = new HashSet<>();
        Set<String> requiredImports = new HashSet<>();
        Set<String> gettersAndSetters = new HashSet<>();

        //Let's dive into the schema value map
        castObjectToMap(schemaValues).forEach((valueName, fillingValue) -> {
            fillBuilder(schemasMap, stringBuilder, requiredAttributes, requiredImports, gettersAndSetters, valueName, fillingValue);
        });

        //Add getters and setters if Lombok disabled
        if (!lombokProperties.enableLombok()) {
            gettersAndSetters.forEach(gAs -> stringBuilder
                    .append(lineSeparator())
                    .append(gAs)
                    .append(lineSeparator()));
        } else {
            StringBuilder lombokBuilder = new StringBuilder();
            lombokBuilder
                    .append(LOMBOK_DATA_ANNOTATION.getValue())
                    .append(lineSeparator())
                    .append(LOMBOK_NO_ARGS_CONSTRUCTOR_ANNOTATION.getValue())
                    .append(lineSeparator());
            requiredImports.addAll(List.of(LOMBOK_DATA_IMPORT.getValue(), LOMBOK_NO_ARGS_CONSTRUCTOR_IMPORT.getValue()));
            if (lombokProperties.accessors()) {
                lombokBuilder.append(LOMBOK_ACCESSORS_ANNOTATION.getValue())
                        .append(lineSeparator());
                requiredImports.add(LOMBOK_ACCESSORS_IMPORT.getValue());
            }
            if (lombokProperties.allArgsConstructor()) {
                lombokBuilder.append(LOMBOK_ALL_ARGS_CONSTRUCTOR_ANNOTATION.getValue())
                        .append(lineSeparator());
                requiredImports.add(LOMBOK_ALL_ARGS_CONSTRUCTOR_IMPORT.getValue());
            }
            stringBuilder.insert(0, lombokBuilder);
            //requiredImports.add();
        }

        //Adding imports according to annotations
        StringBuilder importBuilder = new StringBuilder();
        requiredImports.forEach(requiredImport -> importBuilder
                .append(requiredImport)
                .append(lineSeparator()));
        importBuilder.append(lineSeparator());
        stringBuilder.insert(0, importBuilder);

        //Close generated class
        stringBuilder.append(lineSeparator()).append("}");

        //Create a file by the name of a specific schema and write the generated StringBuilder into it
        File file = new File(outputDirectory + "/" + schemaName + ".java");
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.write(stringBuilder.toString());
            pw.flush();
            System.out.println("The file has been written");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void fillBuilder(Map<String, Object> schemasMap, StringBuilder stringBuilder, Set<String> requiredAttributes, Set<String> requiredImports, Set<String> gettersAndSetters, String valueName, Object fillingValue) {
        //Check if attributes are required and add to Set
        if (valueName.equals(REQUIRED.getValue())) {
            requiredAttributes.addAll((ArrayList<String>) fillingValue);
        }
        //Diving into the Schema Variables Map
        if (valueName.equals(PROPERTIES.getValue())) {
            //Diving into the Properties Variables Map
            castObjectToMap(fillingValue).forEach((variableName, variableProperties) -> {
                Map<String, Object> variablePropertiesMap = castObjectToMap(variableProperties);
                variablePropertiesMap.forEach((propertyName, propertyValue) ->
                {
                    if (propertyName.equals(TYPE.getValue())) {
                        generateJavaDocAndRequiredAnnotation(stringBuilder, requiredAttributes, requiredImports, gettersAndSetters, variableName, variablePropertiesMap, propertyValue);
                    } else if (propertyName.equals(REFERENCE.getValue())) {
                        //Getting the name of an object type variable
                        String nameOfRefVariable = refReplace(propertyValue.toString());
                        Object referencedObject = schemasMap.get(refReplace(propertyValue.toString()));
                        //Checking for mandatory
                        if (requiredAttributes.contains(variableName)) {
                            generateVariableHeaderAndFillListWithRequiredAttributes(
                                    stringBuilder,
                                    requiredAttributes,
                                    variableName,
                                    variablePropertiesMap,
                                    propertyValue.toString(),
                                    null,
                                    requiredImports,
                                    gettersAndSetters);
                        }

                        //Getting the nested object type(Object, String, Integer и т.п.)
                        if (referencedObject != null) {
                            String referencedObjectType = castObjectToMap(referencedObject).get(TYPE.getValue()).toString();
                            if (!OBJECT.getValue().equals(referencedObjectType))
                                nameOfRefVariable = capitalize(referencedObjectType);
                        }

                        //Write the annotation Valid to objects except(String, Integer и т.п.)
                        if (propertyValue.toString().startsWith("#") && !JAVA_DEFAULT_TYPES.contains(nameOfRefVariable)) {
                            generateValidAnnotation(stringBuilder);

                            requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(VALID_ANNOTATION.getValue()));
                        }
                        //Write an optional variable of a reference type
                        stringBuilder.append(lineSeparator())
                                .append(formatString(FIELD, nameOfRefVariable, variableName))
                                .append(lineSeparator());

                        gettersAndSetters.add(generateGetter(nameOfRefVariable, variableName));
                        gettersAndSetters.add(generateSetter(nameOfRefVariable, variableName));
                    }
                });
            });
        }
    }

    private void generateJavaDocAndRequiredAnnotation(StringBuilder stringBuilder, Set<String> requiredAttributes, Set<String> requiredImports, Set<String> gettersAndSetters, String variableName, Map<String, Object> variablePropertiesMap, Object propertyValue) {
        String formatDate = getVariablePropertyIfExistsOrElseNull(variablePropertiesMap, FORMAT);

        //Add JavaDoc and Annotations, as well as immediately fill the List with required attributes
        generateVariableHeaderAndFillListWithRequiredAttributes(
                stringBuilder,
                requiredAttributes,
                variableName,
                variablePropertiesMap,
                propertyValue.toString(),
                formatDate,
                requiredImports,
                gettersAndSetters);

        //Filling List
        if (!requiredAttributes.contains(variableName) && propertyValue.equals(ARRAY.getValue())) {
            String item = refReplace(variablePropertiesMap.get(ITEMS.getValue()).toString().replaceFirst(".$", ""));
            generateValidAnnotation(stringBuilder);
            stringBuilder.append(lineSeparator())
                    .append(formatString(ARRAY_LIST, item, variableName))
                    .append(lineSeparator());

            requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(VALID_ANNOTATION.getValue()));
            requiredImports.add(LIST_IMPORT.getValue());

            gettersAndSetters.add(generateGetter(format(LIST_TYPE.getValue(), item), variableName));
            gettersAndSetters.add(generateSetter(format(LIST_TYPE.getValue(), item), variableName));
        }

        //Write the variable itself with a private modifier
        //If the date format is specified, put down LocalDate
        if (formatDate != null) {
            stringBuilder.append(lineSeparator())
                    .append(formatString(FIELD, LOCAL_DATE.getValue(), variableName))
                    .append(lineSeparator());

            requiredImports.add(LOCAL_DATE_IMPORT.getValue());

            gettersAndSetters.add(generateGetter(LOCAL_DATE.getValue(), variableName));
            gettersAndSetters.add(generateSetter(LOCAL_DATE.getValue(), variableName));
        } else if (!StringUtils.equals(propertyValue.toString(), ARRAY.getValue())) {
            stringBuilder.append(lineSeparator())
                    .append(formatString(FIELD, capitalize(propertyValue.toString()), variableName))
                    .append(lineSeparator());

            gettersAndSetters.add(generateGetter(propertyValue.toString(), variableName));
            gettersAndSetters.add(generateSetter(propertyValue.toString(), variableName));
        }
    }

    private static StringBuilder getClassBuilder(String schemaName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public class ")
                .append(schemaName)
                .append(" {")
                .append(lineSeparator());
        return stringBuilder;
    }

    private void generateVariableHeaderAndFillListWithRequiredAttributes(StringBuilder stringBuilder, Set<String> requiredAttributes, String variableName, Map<String, Object> variableProperties, String propertyValue, String formatDate, Set<String> requiredImports, Set<String> gettersAndSetters) {
        //Getting the properties of each variable
        String description = getVariablePropertyIfExistsOrElseNull(variableProperties, DESCRIPTION);
        String example = getVariablePropertyIfExistsOrElseNull(variableProperties, EXAMPLE);
        String enumeration = getVariablePropertyIfExistsOrElseNull(variableProperties, ENUMERATION);
        String minLength = getVariablePropertyIfExistsOrElseNull(variableProperties, MIN_LENGTH);
        String maxLength = getVariablePropertyIfExistsOrElseNull(variableProperties, MAX_LENGTH);
        String pattern = getVariablePropertyIfExistsOrElseNull(variableProperties, PATTERN);
        //Generate JAVA_DOC
        if (isNotBlank(description) || isNotBlank(enumeration) || isNotBlank(example)) {
            generateJavaDoc(stringBuilder, description, enumeration, example);
        }
        //Checking Set requiredAttributes for a required attribute
        if (requiredAttributes.contains(variableName)) {
            generateRequiredAttributesAnnotation(stringBuilder, variableName, variableProperties, propertyValue, formatDate, requiredImports, gettersAndSetters);
        }
        //Write the annotation Size
        if (isNotBlank(maxLength) || isNotBlank(minLength)) {
            generateSizeAnnotation(stringBuilder, minLength, maxLength);

            requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(StringUtils.substringBefore(SIZE_MIN_MAX_ANNOTATION.getValue(), "(")));
        }
        //Write the annotation Pattern
        if (isNotBlank(pattern)) {
            stringBuilder.append(lineSeparator())
                    .append(TABULATION.getValue())
                    .append(formatString(PATTERN_ANNOTATION, pattern));

            requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(StringUtils.substringBefore(PATTERN_ANNOTATION.getValue(), "(")));
        }
    }

    private void generateRequiredAttributesAnnotation(StringBuilder stringBuilder, String variableName, Map<String, Object> variableProperties, String propertyValue, String formatDate, Set<String> requiredImports, Set<String> gettersAndSetters) {
        //Annotating NotEmpty and Valid to the Array type
        if (propertyValue.equals(ARRAY.getValue())) {
            fillArrayWithRequiredAttributes(stringBuilder, variableName, variableProperties, requiredImports, gettersAndSetters);
        } else {
            //Write an annotation according to the variable type
            //If the date format is specified, then put down NotNull
            generateRequiredAnnotationsByFieldType(stringBuilder, propertyValue, formatDate, requiredImports);
        }
    }

    /**
     * The method adds the Size annotation
     *
     * @param stringBuilder StringBuilder
     * @param minLength     minLength
     * @param maxLength     maxLength
     */
    private static void generateSizeAnnotation(StringBuilder stringBuilder, String minLength, String maxLength) {
        stringBuilder.append(lineSeparator());
        if (isNotBlank(minLength) && isNotBlank(maxLength)) {
            stringBuilder.append(formatString(SIZE_MIN_MAX_ANNOTATION, minLength, maxLength));
        } else if (isNotBlank(minLength)) {
            stringBuilder.append(formatString(SIZE_MIN_ANNOTATION, minLength));
        } else {
            stringBuilder.append(formatString(SIZE_MAX_ANNOTATION, maxLength));
        }
    }

    /**
     * The method adds an annotation on the variable type
     *
     * @param stringBuilder   StringBuilder
     * @param propertyValue   Variable type
     * @param formatDate      The format attribute with the value date (if present)
     * @param requiredImports Set to collect imports
     */
    private static void generateRequiredAnnotationsByFieldType(StringBuilder stringBuilder, String propertyValue, String formatDate, Set<String> requiredImports) {
        stringBuilder.append(lineSeparator());
        if (isNotBlank(formatDate)) {
            stringBuilder.append(JAVA_TYPES_REQUIRED_ANNOTATIONS.get(LOCAL_DATE.getValue()));
            requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(NOT_NULL_ANNOTATION.getValue()));
        } else if (propertyValue.startsWith("#")) {
            stringBuilder.append(JAVA_TYPES_REQUIRED_ANNOTATIONS.get("Object"));
            requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(NOT_NULL_ANNOTATION.getValue()));
        } else {
            String annotation = JAVA_TYPES_REQUIRED_ANNOTATIONS.get(capitalize(propertyValue));
            stringBuilder.append(annotation);
            requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(annotation));
        }
    }

    /**
     * The method adds the NotEmpty annotation and a variable of type List
     *
     * @param stringBuilder      StringBuilder
     * @param variableName       Variable name
     * @param variableProperties Properties of variable
     * @param requiredImports    Set to collect imports
     */
    private void fillArrayWithRequiredAttributes(StringBuilder stringBuilder, String variableName, Map<String, Object> variableProperties, Set<String> requiredImports, Set<String> gettersAndSetters) {
        String item = refReplace(variableProperties.get(ITEMS.getValue()).toString().replaceFirst(".$", ""));
        stringBuilder.append(lineSeparator())
                .append(TABULATION.getValue())
                .append(NOT_EMPTY_ANNOTATION.getValue());
        generateValidAnnotation(stringBuilder);
        stringBuilder.append(lineSeparator())
                .append(formatString(ARRAY_LIST, item, variableName))
                .append(lineSeparator());

        requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(NOT_EMPTY_ANNOTATION.getValue()));
        requiredImports.add(JAVA_TYPES_REQUIRED_IMPORTS.get(VALID_ANNOTATION.getValue()));
        requiredImports.add(LIST_IMPORT.getValue());

        gettersAndSetters.add(generateGetter(format(LIST_TYPE.getValue(), item), variableName));
        gettersAndSetters.add(generateSetter(format(LIST_TYPE.getValue(), item), variableName));
    }

    private static String getVariablePropertyIfExistsOrElseNull(Map<String, Object> variableProperties, ConstantsEnum valueEnum) {
        if (variableProperties.containsKey(valueEnum.getValue())) {
            return variableProperties.get(valueEnum.getValue()).toString();
        }
        return null;
    }

    /**
     * The method adds to a JavaDoc file
     *
     * @param stringBuilder StringBuilder
     * @param description   Description
     * @param enumeration   Enum
     * @param example       Example
     */
    private void generateJavaDoc(StringBuilder stringBuilder, String description, String enumeration, String example) {
        stringBuilder.append(lineSeparator()).append(JAVA_DOC_START.getValue());
        if (isNotBlank(description)) {
            stringBuilder.append(lineSeparator()).append(formatString(JAVA_DOC_LINE, description));
        }
        if (isNotBlank(example)) {
            stringBuilder.append(lineSeparator()).append(formatString(JAVA_DOC_EXAMPLE, example));
        }
        if (isNotBlank(enumeration)) {
            stringBuilder.append(lineSeparator()).append(formatString(JAVA_DOC_LINE, enumeration));
        }
        stringBuilder.append(lineSeparator()).append(JAVA_DOC_END.getValue());
    }
}
