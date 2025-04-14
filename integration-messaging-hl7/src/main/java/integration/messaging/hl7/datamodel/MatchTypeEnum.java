package integration.messaging.hl7.datamodel;

/**
 * The type of match to perform.
 * 
 * @author Brendan Douglas
 *
 */
public enum MatchTypeEnum {
    EQUALS("equals"), EQUALS_IGNORE_CASE("equals-ignore-case"), CONTAINS("contains"), STARTS_WITH("starts-with"),
    ENDS_WITH("ends-with"), NOT_EQUALS("not-equals"), NOT_EQUALS_IGNORE_CASE("not-equals-ignore-case"),
    NOT_CONTAINS("not-contains"), NOT_STARTS_WITH("not-starts-with"), NOT_ENDS_WITH("not-ends-with");
    ;

    private String type;

    MatchTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static MatchTypeEnum get(String type) {
        for (MatchTypeEnum matchType : values()) {
            if (matchType.type.equalsIgnoreCase(type)) {
                return matchType;
            }
        }

        return EQUALS; // The default is equals.
    }
}
