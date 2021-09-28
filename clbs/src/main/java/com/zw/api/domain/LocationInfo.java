package com.zw.api.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ApiModel("位置信息")
public class LocationInfo {
    @ApiModelProperty("监控对象名称")
    private String monitorName;

    @ApiModelProperty("经度,单位:1/1000000 WGS84坐标系")
    private long latitude;

    @ApiModelProperty("纬度,单位:1/1000000 WGS84坐标系")
    private long longitude;

    @ApiModelProperty("高程,单位:米")
    private int altitude;

    @ApiModelProperty("方向角,单位:度")
    private int angle;

    @ApiModelProperty("定位时间,格式:yyMMddHHmmss")
    private String time;

    @ApiModelProperty("速度,单位:千米/小时")
    private int speed;

    @ApiModelProperty("卫星颗数")
    private int satellitesNumber;

    @ApiModelProperty("总里程,单位:千米")
    private int mileage;

    @ApiModelProperty("状态, 参考JT/T 808状态位定义表")
    private int status;

    @ApiModelProperty("报警, 参考JT/T 808报警标志位定义表")
    private int alarm;

    @ApiModelProperty("油箱油量,单位:升")
    private double[] fuelTankCapacity;

    @ApiModelProperty("油箱加油量,单位:升")
    private double[] fuelTankAdd;

    @ApiModelProperty("油箱漏油量,单位:升")
    private double[] fuelTankLeak;

    @ApiModelProperty("油箱环境温度,单位:摄氏度")
    private double[] fuelTankTemp;

    @ApiModelProperty("油箱燃油温度,单位:摄氏度")
    private double[] fuelTankOilTemp;

    @ApiModelProperty("油量传感器状态,0:异常,1:正常")
    private int[] fuelStatus;

    @ApiModelProperty("载荷重量,单位:千克")
    private double[] loadWeight;

    @ApiModelProperty("载重状态,0:异常,1:空载,2:满载,3:超载,4:装载,5:卸载,6:轻载,7:重载")
    private int[] loadStatus;

    @ApiModelProperty("胎压,单位:bar")
    private double[] tyrePressure;

    @ApiModelProperty("胎温,单位:摄氏度")
    private double[] tyreTemp;

    @ApiModelProperty("胎压传感器状态,0:异常,1:正常")
    private int[] tyreStatus;

    @ApiModelProperty("rfid数据")
    private List<Rfid> rfidList;

    public static LocationInfo fromCache(JSONObject locationObj) {
        JSONObject data = locationObj.getJSONObject("data");
        if (data == null) {
            return null;
        }
        JSONObject msgBody = data.getJSONObject("msgBody");
        if (msgBody == null) {
            return null;
        }
        LocationInfo info = new LocationInfo();
        info.setMonitorName(msgBody.getJSONObject("monitorInfo").getString("monitorName"));
        info.setLatitude((long) (msgBody.getDoubleValue("originalLatitude") * 1e6));
        info.setLongitude((long) (msgBody.getDoubleValue("originalLongitude") * 1e6));
        info.setAltitude(msgBody.getIntValue("altitude"));
        info.setAngle(msgBody.getIntValue("direction"));
        info.setTime(msgBody.getString("gpsTime"));
        info.setSpeed(msgBody.getIntValue("gpsSpeed"));
        info.setSatellitesNumber(msgBody.getIntValue("satellitesNumber"));
        info.setMileage(msgBody.getIntValue("gpsMileage"));
        info.setStatus(msgBody.getIntValue("status"));
        info.setAlarm(msgBody.getIntValue("alarm"));

        parseFuelData(msgBody, info); //油量数据
        parseLoadData(msgBody, info); //载重数据
        parseTyreData(msgBody, info); //胎压数据
        final List<Rfid> rfidList = JSON.parseArray(msgBody.getString("rfidList"), Rfid.class);
        if (CollectionUtils.isEmpty(rfidList)) {
            info.setRfidList(Collections.emptyList());
        } else {
            info.setRfidList(rfidList);
        }
        return info;
    }

    private static void parseFuelData(JSONObject msgBody, LocationInfo info) {
        JSONArray fuelInfoList = msgBody.getJSONArray("oilMass");
        if (fuelInfoList == null || fuelInfoList.isEmpty()) {
            return;
        }
        int length = fuelInfoList.size();
        JSONObject fuelObj;
        double[] tankCapacity = new double[length];
        double[] tankAdd = new double[length];
        double[] tankLeak = new double[length];
        double[] tankTemp = new double[length];
        double[] tankOilTemp = new double[length];
        int[] fuelStatus = new int[length];
        for (int i = 0; i < length; i++) {
            fuelObj = (JSONObject) fuelInfoList.get(i);
            tankCapacity[i] = fuelObj.getDoubleValue("oilMass");
            tankAdd[i] = fuelObj.getDoubleValue("add");
            tankLeak[i] = fuelObj.getDoubleValue("del");
            tankTemp[i] = fuelObj.getDoubleValue("envTem");
            tankOilTemp[i] = fuelObj.getDoubleValue("oilTem");
            fuelStatus[i] = fuelObj.getIntValue("unusual") == 1 ? 0 : 1;
        }
        info.setFuelTankCapacity(tankCapacity);
        info.setFuelTankAdd(tankAdd);
        info.setFuelTankLeak(tankLeak);
        info.setFuelTankTemp(tankTemp);
        info.setFuelTankOilTemp(tankOilTemp);
        info.setFuelStatus(fuelStatus);
    }

    private static void parseLoadData(JSONObject msgBody, LocationInfo info) {
        JSONArray loadInfoList = msgBody.getJSONArray("loadInfos");
        if (loadInfoList == null || loadInfoList.isEmpty()) {
            return;
        }
        int length = loadInfoList.size();
        double[] loadWeight = new double[length];
        int[] loadStatus = new int[length];
        JSONObject loadObj;
        for (int i = 0; i < length; i++) {
            loadObj = (JSONObject) loadInfoList.get(i);
            // double load = loadObj.getDoubleValue("loadWeight");
            // int unit = loadObj.getIntValue("unit");
            // loadWeight[i] = Math.pow(10, unit) / 10 * load;
            loadWeight[i] = new BigDecimal(loadObj.getOrDefault("loadWeight", "0").toString()).doubleValue();
            loadStatus[i] = loadObj.getIntValue("status");
            if (loadObj.getIntValue("unusual") == 1) {
                loadStatus[i] = 0;
            }
        }
        info.setLoadWeight(loadWeight);
        info.setLoadStatus(loadStatus);
    }

    private static void parseTyreData(JSONObject msgBody, LocationInfo info) {
        JSONObject tyreData = msgBody.getJSONObject("tyreInfos");
        if (tyreData == null) {
            return;
        }
        JSONArray tyreInfoList = tyreData.getJSONArray("list");
        if (tyreInfoList == null || tyreInfoList.isEmpty()) {
            return;
        }
        int length = tyreInfoList.size();
        double[] tyrePressure = new double[length];
        double[] tyreTemp = new double[length];
        int[] tyreStatus = new int[length];
        JSONObject tyreObj;
        for (int i = 0; i < length; i++) {
            tyreObj = (JSONObject) tyreInfoList.get(i);
            tyrePressure[i] = tyreObj.getDoubleValue("pressure");
            tyreTemp[i] = tyreObj.getDoubleValue("temperature");
            tyreStatus[i] = tyreObj.getIntValue("unusual") == 1 ? 0 : 1;
        }
        info.setTyrePressure(tyrePressure);
        info.setTyreTemp(tyreTemp);
        info.setTyreStatus(tyreStatus);
    }
}
