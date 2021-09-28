package com.zw.app.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 监控对象常用工具类
 * @author hujun
 * @date 2018/8/22 14:30
 */
public class AppMonitorUtil {

    /**
     * 获取传感器正常、异常表示名称
     * @param status
     * @param type
     * @return
     */
    public static String getSensorUnusualName(Integer status, int type) {
        String name = "";
        if (status != null) {
            switch (type) {
                case 1://里程传感器
                case 2://油量传感器
                case 3://油耗传感器
                case 4://温度传感器
                case 5://湿度传感器
                    if (status == 0) {
                        name = "正常";
                    } else {
                        name = "异常";
                    }
                    break;
                case 6://正反转传感器
                    if (status == 0) {
                        name = "正转";
                    } else {
                        name = "反转";
                    }
                    break;
                case 7://工时传感器
                    if (status == 0) {
                        name = "正常";
                    } else {
                        name = "停机";
                    }
                    break;
                case 8://IO传感器
                    if (status == 0) {
                        name = "开";
                    } else {
                        name = "关";
                    }
                    break;
                default:
                    break;
            }
        }
        return name;
    }



    /**
     * 获取载重传感器状态
     * @param status
     * @return
     */
    public static String getLoadStatusName(Integer status) {
        String statusStr = "";
        switch (status) {
            case 1:
                statusStr = "空载";
                break;
            case 2:
                statusStr = "满载";
                break;
            case 3:
                statusStr = "超载";
                break;
            case 4:
                statusStr = "装载";
                break;
            case 5:
                statusStr = "卸载";
                break;
            case 6:
                statusStr = "轻载";
                break;
            case 7:
                statusStr = "重载";
                break;
            default:
                break;
        }
        return statusStr;
    }

    /**
     * 获取油量传感器外设名称
     * @param id
     * @return
     */
    public static String getOilMassName(String id) {
        String name = "";
        if (StringUtils.isNotBlank(id)) {
            switch (id) {
                case "65":
                    name = "1#";
                    break;
                case "66":
                    name = "2#";
                    break;
                case "67":
                    name = "3#";
                    break;
                case "68":
                    name = "4#";
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    /**
     * 获取油耗传感器外设名称
     * @param id
     * @return
     */
    public static String getOilConsumeName(String id) {
        String name = "";
        if (StringUtils.isNotBlank(id)) {
            switch (id) {
                case "69":
                    name = "1#";
                    break;
                case "70":
                    name = "2#";
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    /**
     * 获取温度传感器外设名称
     * @param id
     * @return
     */
    public static String getTemperatureName(String id) {
        String name = "";
        if (StringUtils.isNotBlank(id)) {
            switch (id) {
                case "33":
                    name = "1#";
                    break;
                case "34":
                    name = "2#";
                    break;
                case "35":
                    name = "3#";
                    break;
                case "36":
                    name = "4#";
                    break;
                case "37":
                    name = "5#";
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    /**
     * 获取湿度传感器外设名称
     * @param id
     * @return
     */
    public static String getHumidityName(String id) {
        String name = "";
        if (StringUtils.isNotBlank(id)) {
            switch (id) {
                case "38":
                    name = "1#";
                    break;
                case "39":
                    name = "2#";
                    break;
                case "40":
                    name = "3#";
                    break;
                case "41":
                    name = "4#";
                    break;
                case "42":
                    name = "5#";
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    /**
     * 获取载重传感器外设名称
     * @param id
     * @return
     */
    public static String getLoadName(String id) {
        String name = "";
        if (StringUtils.isNotBlank(id)) {
            switch (id) {
                case "112":
                    name = "1#";
                    break;
                case "113":
                    name = "2#";
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    /**
     * 获取工时传感器外设名称
     * @param id
     * @return
     */
    public static String getWorkHourName(String id) {
        String name = "";
        if (StringUtils.isNotBlank(id)) {
            switch (id) {
                case "128":
                    name = "1#";
                    break;
                case "129":
                    name = "2#";
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    /**
     * 获取监控对象默认图标
     * @param monitorType
     * @return
     */
    public static String getMonitorDefaultIco(String monitorType) {
        String icoName;
        if (StringUtils.isNotBlank(monitorType)) {
            switch (monitorType) {
                case "0":
                    icoName = "vehicle.png";
                    break;
                case "1":
                    icoName = "123.png";
                    break;
                case "2":
                    icoName = "thing.png";
                    break;
                default:
                    icoName = "";
                    break;
            }
        } else {
            icoName = "";
        }
        return icoName;
    }

}
