package com.zw.platform.domain.leaderboard;


public enum RiskResultEnum {

    SUCCESS_FILE("事故未发生", 0), FAILED_FILE("事故已发生", 1);

    private int code;

    private String name;

    RiskResultEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}
