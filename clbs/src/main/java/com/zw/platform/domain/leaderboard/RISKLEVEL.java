package com.zw.platform.domain.leaderboard;

public enum RISKLEVEL {

    GENERAL("一般", "1~3级"), HEAVIER("较重", "4~6级"),

    SERIOUS("严重", "7~9级"), SPECIAL_SERIOUS("特重", "10~12级");

    private String level;

    private String range;

    RISKLEVEL(String level, String range) {
        this.level = level;
        this.range = range;
    }

    public String getLevel() {
        return this.level;
    }

    public String getRange() {
        return this.range;
    }

}
