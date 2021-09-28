package com.zw.adas.domain.common;

import com.zw.platform.util.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/***
 @Author zhengjc
 @Date 2019/6/10 15:24
 @Description 风险类型枚举
 @version 1.0
 **/
public enum AdasRiskStatus {
    /**
     * 主动安全风险类型枚举
     */
    UNTREATED("未处理", 1), TREATED("已处理", 6), BLANK("", -1);

    private static final Map<Integer, AdasRiskStatus> codeMap = new HashMap<>();

    static {
        for (AdasRiskStatus ars : AdasRiskStatus.values()) {
            codeMap.put(ars.code, ars);
        }

    }

    private String name;

    private Integer code;

    AdasRiskStatus(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }

    /**
     * @param code 状态的数字，多个按照逗号隔开
     * @return
     */
    public static String getRiskStatus(String code) {
        Integer codeNum = StrUtil.isNotBlank(code) ? Integer.parseInt(code) : -1;
        return Optional.ofNullable(codeMap.get(codeNum)).orElse(BLANK).name;
    }

}
