package com.eventflow.platform.enums;

public enum Language {
    EN("en"),
    ZH_CN("zh-CN");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Language fromCode(String code) {
        if (code == null) {
            return EN;
        }
        for (Language value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return EN;
    }
}
