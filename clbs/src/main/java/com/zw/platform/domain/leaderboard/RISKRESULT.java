package com.zw.platform.domain.leaderboard;

public enum RISKRESULT {

    SUCCESS_FILE("督导高敏", 1), FAILED_FILE("督导低敏", 2), ACCIDENT_FILE("事故归档", 3);

    private int code;

    private String name;

    RISKRESULT(String name, int code) {
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
