package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

public enum FailReason {

    PLAT_NOT_EXIST(0, "企业未找到对应车辆档案"), OIL_NOT_EXIST(1, "油补平台没有此车辆"), OIL_PLAT_NOT_EXIST(2, "油补平台与企业都无此车辆");

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    int code;
    String name;

    FailReason(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
