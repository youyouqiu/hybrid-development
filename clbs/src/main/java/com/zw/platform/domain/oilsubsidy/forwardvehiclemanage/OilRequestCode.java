package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum OilRequestCode {

    EMPTY_DATA(0, "验证成功但是没有查询到数据"), SUCCESS(1, "验证成功并返回数据"), VALIDATE_FAILED(-1, "验证失败");
    static Map<Integer, String> oilRequestCodeMap = new HashMap<>();

    static {

        for (OilRequestCode oilRequestCode : OilRequestCode.values()) {
            oilRequestCodeMap.put(oilRequestCode.getCode(), oilRequestCode.getName());
        }
    }

    public static String getNameByCode(Integer code) {
        return Optional.ofNullable(oilRequestCodeMap.get(code)).orElse("未知状态码");
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    Integer code;
    String name;

    OilRequestCode(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
