package com.zw.platform.domain.reportManagement;

import com.zw.platform.domain.reportManagement.form.VehStateDO;
import com.zw.platform.util.VehStateAlarmUtil;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zjc
 * @Description:车辆状态报表列表容器对象，用户去重，计算车辆数
 * @Date: create in 2020/11/12 17:53
 */
@Data
public class VehStateContainerDTO {

    /**
     * 在线车辆数
     */
    private Set<String> onlineVidSet = new HashSet<>();

    /**
     * 离线24小时报警车辆数
     */
    private Set<String> offLineVidSet = new HashSet<>();

    /**
     * 设备故障报警车辆数
     */
    private Set<String> equipmentFailureVidSet = new HashSet<>();

    /**
     * 超速报警车辆数
     */
    private Set<String> overSpeedVidSet = new HashSet<>();

    /**
     * 疲劳驾驶报警车辆数
     */
    private Set<String> tiredVidSet = new HashSet<>();

    /**
     * 不按规定行驶报警车辆数
     */
    private Set<String> lineVidSet = new HashSet<>();

    /**
     * 凌晨2-5点行驶报警车辆数
     */
    private Set<String> dawnVidSet = new HashSet<>();

    /**
     * 遮挡摄像头报警车辆数
     */
    private Set<String> cameraVidSet = new HashSet<>();

    /**
     * 其他报警的车辆数
     */
    private Set<String> otherAlarmVidSet = new HashSet<>();

    /**
     * 报警处理条数
     */
    private int alarmHandled;

    /**
     * 处理方式是下发短信报警数
     */
    private int msgNum;

    /**
     * 既包含今天又包含以前的情况
     * @param list
     * @param orgVidSet
     * @return
     */
    public static VehStateContainerDTO calculateVehNum(List<VehStateDO> list, Set<String> orgVidSet,
        Set<String> onlineVehIdSet) {

        VehStateContainerDTO container = getVehStateContainerFromCache(orgVidSet);
        container.onlineVidSet = onlineVehIdSet;
        //计算报警处理条数以及下发短信处理方式的报警数量
        calculateMsgAndHandleAlarmNum(container, orgVidSet);
        return calculateVehNum(list, orgVidSet, container);
    }

    private static void calculateMsgAndHandleAlarmNum(VehStateContainerDTO container, Set<String> orgVidSet) {
        Map<String, VehDealMsgCacheDTO> todayDealAlarCache = VehStateAlarmUtil.getTodayDealAlarCache(orgVidSet);
        for (VehDealMsgCacheDTO vehDealMsgCacheDTO : todayDealAlarCache.values()) {
            container.alarmHandled = container.alarmHandled + vehDealMsgCacheDTO.getHandleTotal();
            container.msgNum = container.msgNum + vehDealMsgCacheDTO.getSendMsm();
        }
    }

    private static VehStateContainerDTO calculateVehNum(List<VehStateDO> list, Set<String> orgVidSet,
        VehStateContainerDTO container) {
        for (VehStateDO vehStateDO : list) {
            String vid = vehStateDO.getVid();
            if (!orgVidSet.contains(vid)) {
                continue;
            }
            //车辆在线数
            container.onlineVidSet.add(vid);
            //报警处理条数
            container.alarmHandled = container.alarmHandled + vehStateDO.getAlarmHandled();
            // 处理方式是下发短信报警数
            container.msgNum = container.msgNum + vehStateDO.getMsgNum();
            if (vehStateDO.getDeviceStatus() == 1) {
                container.equipmentFailureVidSet.add(vid);

            }
            if (vehStateDO.getAlarmSpeed() > 0) {
                container.overSpeedVidSet.add(vid);

            }
            if (vehStateDO.getAlarmTired() > 0) {
                container.tiredVidSet.add(vid);

            }
            if (vehStateDO.getAlarmLine() > 0) {
                container.lineVidSet.add(vid);

            }
            if (vehStateDO.getAlarmDawn() > 0) {
                container.dawnVidSet.add(vid);

            }
            if (vehStateDO.getAlarmCamera() > 0) {
                container.cameraVidSet.add(vid);

            }
            if (vehStateDO.getAlarmOther() > 0) {
                container.otherAlarmVidSet.add(vid);

            }

        }
        return container;
    }

    /**
     * 不包含今天只包含以前的情况
     * @param list
     * @param orgVidSet
     * @return
     */
    public static VehStateContainerDTO getNoContainsTodayVehNum(List<VehStateDO> list, Set<String> orgVidSet) {
        return calculateVehNum(list, orgVidSet, new VehStateContainerDTO());
    }

    /**
     * 只算今天的情况
     * @param orgVidList
     * @return
     */
    public static VehStateContainerDTO getVehStateContainerFromCache(Set<String> orgVidList) {
        return VehStateAlarmUtil.getVehStateContainer(orgVidList);
    }

}
