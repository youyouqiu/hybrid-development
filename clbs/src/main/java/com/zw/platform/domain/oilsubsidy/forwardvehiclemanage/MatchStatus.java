package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

public enum MatchStatus {

    Failed(0, "匹配失败"), SUCCESS(1, "匹配成功");
    int code;
    String name;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    MatchStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
