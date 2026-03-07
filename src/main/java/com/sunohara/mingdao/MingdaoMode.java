package com.sunohara.mingdao;

/**
 * 名刀模式枚举
 */
public enum MingdaoMode {
    ONCE("once", "一次性"),
    REGENERATE("regenerate", "可再生");

    private final String configKey;
    private final String displayName;

    MingdaoMode(String configKey, String displayName) {
        this.configKey = configKey;
        this.displayName = displayName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MingdaoMode fromString(String str) {
        for (MingdaoMode mode : MingdaoMode.values()) {
            if (mode.configKey.equalsIgnoreCase(str)) {
                return mode;
            }
        }
        return ONCE;
    }
}
