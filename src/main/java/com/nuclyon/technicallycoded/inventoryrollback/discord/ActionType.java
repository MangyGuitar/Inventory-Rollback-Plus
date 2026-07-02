package com.nuclyon.technicallycoded.inventoryrollback.discord;

public enum ActionType {

    GIVE_SHULKERS("give-shulkers"),
    RESTORE_INVENTORY("restore-inventory"),
    RESTORE_ENDER_CHEST("restore-enderchest"),
    RESTORE_HEALTH("restore-health"),
    RESTORE_HUNGER("restore-hunger"),
    RESTORE_XP("restore-xp");

    private final String configKey;

    ActionType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

}
