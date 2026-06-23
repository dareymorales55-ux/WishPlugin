package com.deejay.wishsmp;

public enum ClassType {

    STRENGTH(
            "Strength",
            "strength"
    ),

    SPEED(
            "Speed",
            "speed"
    ),

    HEALTH(
            "Health",
            "health"
    ),

    HASTE(
            "Haste",
            "haste"
    );

    private final String displayName;
    private final String teamName;

    ClassType(String displayName, String teamName) {
        this.displayName = displayName;
        this.teamName = teamName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTeamName() {
        return teamName;
    }

    public static ClassType fromString(String value) {

        for (ClassType type : values()) {

            if (type.name().equalsIgnoreCase(value)
                    || type.displayName.equalsIgnoreCase(value)) {

                return type;
            }
        }

        return null;
    }
}
