package ru.yojo.yamltopojo.constants;

import java.util.List;
import java.util.Map;

import static java.lang.System.lineSeparator;

public enum ConstantsEnum {

    /**
     * Attributes Yaml
     */
    PROPERTIES("properties"),
    TYPE("type"),
    FORMAT("format"),
    EXAMPLE("example"),
    DESCRIPTION("description"),
    REFERENCE("$ref"),
    REQUIRED("required"),
    MAX_LENGTH("maxLength"),
    MIN_LENGTH("minLength"),
    ITEMS("items"),
    PATTERN("pattern"),
    ENUMERATION("enum"),
    ARRAY("array"),
    OBJECT("object"),

    /**
     * Annotations
     */
    NOT_EMPTY_ANNOTATION("    @NotEmpty"),
    NOT_NULL_ANNOTATION("    @NotNull"),
    SIZE_MIN_MAX_ANNOTATION("    @Size(min = %s, max = %s)"),
    SIZE_MAX_ANNOTATION("    @Size(max = %s)"),
    SIZE_MIN_ANNOTATION("    @Size(min = %s)"),
    PATTERN_ANNOTATION("    @Pattern(regexp = \"%s\")"),
    VALID_ANNOTATION("    @Valid"),

    /**
     * Lombok annotations
     */
    LOMBOK_ALL_ARGS_CONSTRUCTOR_ANNOTATION("@AllArgsConstructor"),
    LOMBOK_NO_ARGS_CONSTRUCTOR_ANNOTATION("@NoArgsConstructor"),
    LOMBOK_DATA_ANNOTATION("@Data"),
    LOMBOK_ACCESSORS_ANNOTATION("@Accessors(fluent = true, chain = true)"),

    /**
     * Lombok imports
     */
    LOMBOK_ALL_ARGS_CONSTRUCTOR_IMPORT("import lombok.AllArgsConstructor;"),
    LOMBOK_NO_ARGS_CONSTRUCTOR_IMPORT("import lombok.NoArgsConstructor;"),
    LOMBOK_DATA_IMPORT("import lombok.Data;"),
    LOMBOK_ACCESSORS_IMPORT("import lombok.experimental.Accessors;"),

    /**
     * Other
     */
    LOCAL_DATE("LocalDate"),
    LOCAL_DATE_IMPORT("import java.time.LocalDate;"),
    LIST_IMPORT("import java.util.List;"),
    ARRAY_LIST("    private List<%s> %s;"),
    FIELD("    private %s %s;"),

    JAVA_DOC_START("    /**"),
    JAVA_DOC_END("     */"),
    JAVA_DOC_LINE("     * %s"),
    JAVA_DOC_EXAMPLE("     * Example: %s"),
    LIST_TYPE("List<%s>"),

    GETTER("    public %s get%s() {" +
            lineSeparator() +
            "        return %s;" +
            lineSeparator() + "    }"),
    SETTER("    public void set%s(%s %s) {" +
            lineSeparator() +
            "        this.%s = %s;" +
            lineSeparator() + "    }");

    public String getValue() {
        return value;
    }

    ConstantsEnum(String value) {
        this.value = value;
    }

    public static final Map<String, String> JAVA_TYPES_REQUIRED_ANNOTATIONS = Map.of(
            "String", "    @NotBlank",
            "Integer", "    @NotNull",
            "Boolean", "    @NotNull",
            "LocalDate", "    @NotNull",
            "Object", "    @NotNull");

    public static final Map<String, String> JAVA_TYPES_REQUIRED_IMPORTS = Map.of(
            "    @NotBlank", "import javax.validation.constrains.NotBlank;",
            "    @NotEmpty", "import javax.validation.constrains.NotEmpty;",
            "    @NotNull", "import javax.validation.constrains.NotNull;",
            "    @Size", "import javax.validation.constrains.Size;",
            "    @Pattern", "import javax.validation.constrains.Pattern;",
            "    @Valid", "import javax.validation.Valid;");

    public static final List<String> JAVA_DEFAULT_TYPES = List.of(
            "String", "Integer", "Long", "Boolean", "BigDecimal", "LocalDate"
    );
    private final String value;

    public static String formatString(ConstantsEnum constantsEnum, Object... strings) {
        return constantsEnum.getValue().formatted(strings);
    }
}
