package com.zw.platform.domain.oil;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PrecisionUtils;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/11/15 15:19
 @Description f3高精度报表实体
 @version 1.0
 **/
@Data
public class F3HighPrecisionReport {

    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 车辆位置信息时间
     */
    private transient long vehTime;
    /**
     * 监控对象
     */
    @ExcelField(title = "监控对象")
    private String brand;

    /**
     * 分组
     */
    @ExcelField(title = "分组")
    private String assignment;

    /**
     * 时间
     */
    @ExcelField(title = "时间")
    private String time;
    /**
     * 速度
     */
    @ExcelField(title = "速度")
    private String speedStr;
    private Double speed;

    /**
     * 电压（行车电压）
     */
    @ExcelField(title = "电压")
    private String travelVoltageStr;
    private Double travelVoltage;

    /**
     * 电量(设备)
     */
    @ExcelField(title = "电量")
    private String deviceElectricityStr;
    private Double deviceElectricity;
    /**
     * 气压值
     */
    @ExcelField(title = "气压值")
    private String airPressureStr;
    private Long airPressure;
    /**
     * 电压有效性
     */
    @ExcelField(title = "电压有效性")
    private String voltageValidStr;
    private Integer voltageValid;
    /**
     * acc有效性
     */
    @ExcelField(title = "ACC有效性")
    private String accValidStr;
    private Integer accValid;
    private Integer accValidReverse;

    /**
     * 发动机状态
     */
    @ExcelField(title = "发动机状态")
    private String engineConditionStr;
    private Integer engineCondition;
    /**
     * 行驶状态
     */
    @ExcelField(title = "行驶状态")
    private String travelStateStr;
    private Integer travelState;
    /**
     * 通讯类型
     */
    @ExcelField(title = "通讯类型")
    private String communicationTypeStr;
    private Integer communicationType;
    /**
     * 运营商
     */
    @ExcelField(title = "运营商")
    private String operatorStr;
    private Integer operator;

    /**
     * 电压最高值
     */
    @ExcelField(title = "电压最高值")
    private String vhStr;
    private Double vh;
    /**
     * 电压最低值
     */
    @ExcelField(title = "电压最低值")
    private String vlStr;
    private Double vl;
    /**
     * 启动电压阈值
     */
    // @ExcelField(title = "打火电压阈值")
    private String startValueStr;
    private Double startValue;
    /**
     * 熄火电压阈值
     */
    // @ExcelField(title = "熄火电压阈值")
    private String stopValueStr;
    private Double stopValue;
    /**
     * 车辆状态 0位:0：熄火；1：打火
     * 1位：0：停止；1：行驶；
     */
    private transient Integer terminalState;

    /**
     * 1：表示数据为电压值，0.1V
     * 电量类型
     */
    private transient Integer electricType;

    private static final Map<Integer, String> voltageValidMap = new HashMap<>(3);
    private static final Map<Integer, String> accMap = new HashMap<>(4);
    private static final Map<Integer, String> accReverseMap = new HashMap<>(4);
    private static final Map<Integer, String> engineConditionMap = new HashMap<>(2);
    private static final Map<Integer, String> travelStateMap = new HashMap<>(2);
    private static final Map<Integer, String> communicationTypeMap = new HashMap<>(6);
    private static final Map<Integer, String> operatorMap = new HashMap<>(4);

    static {
        voltageValidMap.put(0, "有效");
        voltageValidMap.put(1, "无效");
        voltageValidMap.put(2, "未确认");

        accMap.put(0, "接线正常");
        accMap.put(1, "接常电");
        accMap.put(2, "未接线");
        accMap.put(3, "未确认");

        accReverseMap.put(3, "接线正常");
        accReverseMap.put(2, "接常电");
        accReverseMap.put(1, "未接线");
        accReverseMap.put(0, "未确认");

        engineConditionMap.put(0, "熄火");
        engineConditionMap.put(1, "打火");

        travelStateMap.put(0, "停止");
        travelStateMap.put(1, "行驶");

        communicationTypeMap.put(1, "Wifi通讯");
        communicationTypeMap.put(2, "2G通讯");
        communicationTypeMap.put(3, "3G通讯");
        communicationTypeMap.put(4, "4G通讯");
        communicationTypeMap.put(5, "5G通讯");
        communicationTypeMap.put(6, "E");

        operatorMap.put(1, "移动");
        operatorMap.put(2, "联通");
        operatorMap.put(3, "电信");
        operatorMap.put(4, "其他");

    }

    public void initData(BindDTO vehicle) {
        assembleVehicleState();
        brand = vehicle.getName();
        assignment = vehicle.getGroupName();
        vehicleId = vehicle.getId();
        time = DateUtil.getLongToDateStr(vehTime * 1000, null);
        if (speedStr == null) {
            speed = null;
            speedStr = "";
        } else {
            speedStr = roundByScale(Double.valueOf(speedStr), 1);
            speed = Double.valueOf(speedStr);
            speedStr += "km/h";
        }

        if (electricType != null && electricType != 1) {
            travelVoltage = null;
        }

        travelVoltageStr = travelVoltage == null ? "" : roundByScale(travelVoltage, 1) + "V";
        deviceElectricityStr = deviceElectricity == null ? "" : roundByScale(deviceElectricity, 1) + "%";
        airPressureStr = airPressure == null ? "" : airPressure + "Pa";
        voltageValidStr = StrUtil.getOrBlank(voltageValidMap.get(voltageValid));
        assembleReverseAccValid();
        accValidStr = StrUtil.getOrBlank(accReverseMap.get(accValidReverse));
        engineConditionStr = StrUtil.getOrBlank(engineConditionMap.get(engineCondition));

        travelStateStr = StrUtil.getOrBlank(travelStateMap.get(travelState));
        communicationTypeStr = StrUtil.getOrBlank(communicationTypeMap.get(communicationType));
        operatorStr = StrUtil.getOrBlank(operatorMap.get(operator));
        vhStr = getOrDefault(vh);
        vlStr = getOrDefault(vl);
        startValueStr = getOrDefault(startValue);
        stopValueStr = getOrDefault(stopValue);
    }

    /**
     * 2012121619需求变更，前端无法调整，后端进行调整
     */
    private void assembleReverseAccValid() {
        if (accValid != null) {
            switch (accValid.intValue()) {
                case 0:
                    accValidReverse = 3;
                    break;
                case 1:
                    accValidReverse = 2;
                    break;
                case 2:
                    accValidReverse = 1;
                    break;
                case 3:
                    accValidReverse = 0;
                    break;
                default:
                    break;

            }
        }
    }

    private void assembleVehicleState() {
        if (terminalState != null) {
            String binaryStr = Integer.toBinaryString(terminalState);
            int len = binaryStr.length();
            if (len == 1) {
                engineCondition = Integer.valueOf(binaryStr);
                travelState = 0;
            } else if (len >= 2) {
                engineCondition = Integer.valueOf(binaryStr.substring(len - 1));
                travelState = Integer.valueOf(binaryStr.substring(len - 2, len - 1));
            }
        }
    }

    private String roundByScale(double v, int scale) {
        String val = PrecisionUtils.roundByScale(v, scale);
        return "0.0".equals(val) ? "0" : val;
    }

    private String getOrDefault(Double val) {
        if (val == null || val.doubleValue() == 6553.5) {
            return "-";
        }
        return roundByScale(val, 1) + "V";
    }
}
