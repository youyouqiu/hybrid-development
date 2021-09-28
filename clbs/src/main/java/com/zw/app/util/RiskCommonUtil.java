package com.zw.app.util;

import com.zw.platform.domain.leaderboard.RiskStatus;
import com.zw.platform.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RiskCommonUtil {

    private static Map<Integer, RiskStatus> riskStatusMap = new HashMap<>();

    private static String[] riskTypes = {"", "疑似疲劳", "注意力分散", "违规异常", "碰撞危险", "", "激烈驾驶", "组合报警"};

    static {

        for (RiskStatus riskStatus : RiskStatus.values()) {
            riskStatusMap.put(riskStatus.getCode(), riskStatus);
        }

    }

    public static RiskStatus getRiskStatus(int code) {
        return riskStatusMap.get(code);
    }

    public static String getRiskTypes(String riskType) {
        StringBuilder type = new StringBuilder("");
        if (!StringUtil.isNullOrBlank(riskType)) {
            String[] types = riskType.split(",");
            for (String typeN : types) {
                if (StringUtils.isNumeric(types[0])) {
                    type.append(riskTypes[Integer.parseInt(typeN)]).append("、");
                }
            }
            return type.toString().substring(0, type.length() - 1);
        }

        return type.toString();
    }

}