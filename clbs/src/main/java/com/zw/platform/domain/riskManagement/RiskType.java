package com.zw.platform.domain.riskManagement;

public class RiskType {

    public static final int RISK_TIRED = 1; // 疲劳

    public static final int RISK_DISTRACTION = 2; // 分心

    public static final int RISK_EXCEPTION = 3; // 异常

    public static final int RISK_CRASH = 4; // 碰撞

    // 风险事件
    public static final int FORWARD_CRASH = 6401; // 前向碰撞

    public static final int LANE_DEPARTURE = 6402; // 车道偏移

    public static final int CONSECUTIVE_VEHICLE = 6403; // 车距过近

    public static final int PEDESTRIAN_CRASH = 6404; // 行人碰撞

    public static final int FREQUENTLY_CHANGE_LANES = 6405; // 频繁变道

    public static final int MAKE_CALL = 6502; // 接打手持电话

    public static final int SMOKE = 6503; // 吸烟

    public static final int DISTRACTION = 6504; // 注意力分散

    public static final int ABNORMITY = 6505; // 异常报警

    public static final int CLOSE_EYES = 6506; // 闭眼

    public static final int YAWN = 6507; // 打哈欠

    public static final int HEAD_DROP = 6508; // 低头

    /**
     * 根据风险事件的功能id获取事件编码
     */
    public static int getFormateCode(int riskEvent) {
        int formateCode = 0;
        switch (riskEvent) {
            case HEAD_DROP:
                formateCode = 29;
                break;
            case MAKE_CALL:
                riskEvent = 30;
                break;
            case SMOKE:
                riskEvent = 31;
                break;
            case DISTRACTION:
                riskEvent = 32;
                break;
            case CLOSE_EYES:
                riskEvent = 34;
                break;
            case YAWN:
                riskEvent = 35;
                break;
            case LANE_DEPARTURE:
                riskEvent = 51;
                break;
            case CONSECUTIVE_VEHICLE:
                riskEvent = 53;
                break;
            case FORWARD_CRASH:
                riskEvent = 54;
                break;
            case PEDESTRIAN_CRASH:
                riskEvent = 60;
                break;
            case FREQUENTLY_CHANGE_LANES:
                riskEvent = 61;
                break;
            case ABNORMITY:
                riskEvent = 66;
                break;
            default:
                riskEvent = 0;
                break;
        }
        return formateCode;
    }

    /**
     * 根据功能id，判断风险类型
     * @param functionId
     * @return
     */
    public static int getRiskType(String functionId) {
        int riskType;
        switch (functionId) {
            case "6401":
            case "64021":
            case "64022":
            case "6402":
            case "6403":
            case "6404":
            case "6405":
            case "6409":
            case "64081":
            case "64082":
            case "64083":
            case "6408":
                riskType = RiskType.RISK_CRASH;
                break;
            case "6502":
            case "6503":
            case "6504":
            case "6508":
                riskType = RiskType.RISK_DISTRACTION;
                break;
            case "6505":
            case "6509":
            case "6510":
            case "6512":
            case "6511":
                riskType = RiskType.RISK_EXCEPTION;
                break;
            case "6506":
            case "6507":
                riskType = RiskType.RISK_TIRED;
                break;
            default:
                riskType = 0;
                break;
        }
        return riskType;
    }

}
