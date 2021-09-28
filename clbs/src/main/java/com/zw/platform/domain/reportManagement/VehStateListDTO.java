package com.zw.platform.domain.reportManagement;

import com.zw.platform.domain.reportManagement.form.VehBasicDO;
import lombok.Data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: zjc
 * @Description:车辆状态报表列表对象(和查询列表一一对应)
 * @Date: create in 2020/11/12 17:53
 */
@Data
public class VehStateListDTO {
    /**
     * 唯一标识，用户前端进行打印的时候，传递该id，获取疲劳和超速车辆名称列表
     */
    private String id = UUID.randomUUID().toString();
    /**
     * 需要返回去，在前段进行导出时候使用
     */
    private String orgId;
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 车辆总数
     */
    private int total;

    /**
     * 在线车辆数
     */
    private int online;

    /**
     * 离线24小时报警车辆数
     */
    private int offLine;

    /**
     * 停运车辆数
     */
    private int outOfService;
    /**
     * 设备故障报警车辆数
     */
    private int equipmentFailure;
    /**
     * 其他（固定位0）
     */
    private int other = 0;

    /**
     * 超速报警车辆数
     */
    private int overSpeed;

    /**
     * 疲劳驾驶报警车辆数
     */
    private int tired;

    /**
     * 不按规定行驶报警车辆数
     */
    private int line;

    /**
     * 凌晨2-5点行驶报警车辆数
     */
    private int dawn;

    /**
     * 遮挡摄像头报警车辆数
     */
    private int camera;

    /**
     * 其他报警的车辆数
     */
    private int otherAlarm;
    /**
     * 报警处理条数
     */
    private transient int alarmHandled;

    /**
     * 处理方式是下发短信报警数
     */
    private transient int msgNum;

    /**
     * 疲劳车辆信息
     */
    private transient String tiredBrand;

    /**
     * 超速车辆信息
     */
    private transient String overSpeedBrand;

    /**
     * 离线24小时车辆数加1
     */
    public void incrOffLine() {
        offLine++;
    }

    /**
     * 停运车辆数+1
     */
    public void incrOutOfService() {
        outOfService++;
    }

    public void init(VehStateContainerDTO containerDTO, Map<String, VehBasicDO> bindVehicleMap) {
        online = containerDTO.getOnlineVidSet().size();
        initAlarmVidNum(containerDTO);
        initAlarmBrand(containerDTO, bindVehicleMap);
        initHandleMethodInfo();

    }

    /**
     * 初始化各种报警的车辆
     * @param containerDTO
     */
    private void initAlarmVidNum(VehStateContainerDTO containerDTO) {
        equipmentFailure = containerDTO.getEquipmentFailureVidSet().size();
        overSpeed = containerDTO.getOverSpeedVidSet().size();
        tired = containerDTO.getTiredVidSet().size();
        line = containerDTO.getLineVidSet().size();
        dawn = containerDTO.getDawnVidSet().size();
        camera = containerDTO.getCameraVidSet().size();
        otherAlarm = containerDTO.getOtherAlarmVidSet().size();
        msgNum = containerDTO.getMsgNum();
        alarmHandled = containerDTO.getAlarmHandled();

    }

    /**
     * 处理方式
     */
    private transient String handleResult;

    private void initHandleMethodInfo() {
        if (overSpeed > 0 || tired > 0 || line > 0 || dawn > 0 || camera > 0 || otherAlarm > 0) {
            handleResult = "终端播报";
        }

    }

    public void clearNotListField() {
        overSpeedBrand = null;
        tiredBrand = null;
        handleResult = null;
    }

    private void initAlarmBrand(VehStateContainerDTO container, Map<String, VehBasicDO> bindVehicleMap) {
        overSpeedBrand = getBrandStr(container.getOverSpeedVidSet(), bindVehicleMap);
        tiredBrand = getBrandStr(container.getTiredVidSet(), bindVehicleMap);
    }

    private String getBrandStr(Collection<String> vehIds, Map<String, VehBasicDO> bindVehicleMap) {

        StringBuilder brandStr = new StringBuilder();
        for (String vid : vehIds) {
            brandStr.append(bindVehicleMap.get(vid).getBrand());
            brandStr.append("、");
        }
        if (brandStr.length() > 0) {
            return String.format("[%s]", brandStr.substring(0, brandStr.length() - 1));
        } else {
            return "[无]";
        }
    }

}
