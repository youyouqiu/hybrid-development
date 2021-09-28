package com.zw.adas.domain.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.zw.adas.domain.riskManagement.AdasRiskEvent;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.spring.InitData;

/***
 @Author zhengjc
 @Date 2019/6/10 15:24
 @Description 风险类型枚举
 @version 1.0
 **/
public enum AdasRiskType {
    /**
     * 主动安全风险类型枚举
     */
    TIRED("疑似疲劳", "1"), DISTRACTION("注意力分散", "2"), EXCEPTION("违规异常", "3"), CRASH("碰撞危险", "4"), CLUSTER("组合风险",
        "5"), IntenseDriving("激烈驾驶", "6"), BLANK("", "");

    private static final Map<String, AdasRiskType> codeMap = new HashMap<>();

    static {
        for (AdasRiskType art : AdasRiskType.values()) {
            codeMap.put(art.code, art);
        }

    }

    private String name;

    private String code;

    AdasRiskType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /**
     * @param type 类型的数字，多个按照逗号隔开
     * @return
     */
    public static String getRiskType(String type) {
        String result = "";
        if (StrUtil.isNotBlank(type)) {
            StringBuilder sb = new StringBuilder();
            String[] codes = type.split(",");
            for (String code : codes) {
                sb.append(Optional.ofNullable(codeMap.get(code)).orElse(BLANK).name).append("、");
            }
            result = sb.substring(0, sb.length() - 1);

        }
        return result;
    }

    public static String getAppRiskType(String type) {
        String result = "";
        if (StrUtil.isNotBlank(type)) {
            StringBuilder sb = new StringBuilder();
            String[] codes = type.split(",");
            String code = codes.length > 1 ? CLUSTER.code : codes[0];
            sb.append(Optional.ofNullable(codeMap.get(code)).orElse(BLANK).name);
            result = sb.toString();

        }
        return result;
    }

    public static String getAppRiskEventType(Integer eventType) {
        String result = "";
        if (eventType == null) {
            return result;
        }
        if (InitData.riskEventMap.size() == 0) {
            return result;
        }
        AdasRiskEvent riskEvent = InitData.riskEventMap.get(eventType);
        if (riskEvent != null) {
            result = riskEvent.getRiskType();
        }
        return result;
    }

    public String getRiskType() {
        return this.name;
    }


}
