package com.zw.platform.domain.leaderboard;

public enum RiskStatus {
    UNTREATED("未处理", 1),
    WAIT_VISIT("待回访", 2),
    TREATED("已处理", 8),
    ARCHIVE("归档", 6),
    ALL("所有", -1);

    private int code;

    private String name;

    RiskStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return this.code;
    }

    public boolean eq(int code) {
        return this.code == code;
    }
}
