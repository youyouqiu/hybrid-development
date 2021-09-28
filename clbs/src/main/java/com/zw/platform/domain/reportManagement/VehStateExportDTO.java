package com.zw.platform.domain.reportManagement;

import com.zw.platform.domain.reportManagement.query.VehStateQuery;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @Author: zjc
 * @Description:车辆状态报表导出
 * @Date: create in 2020/11/12 17:53
 */
@Data
public class VehStateExportDTO {

    /**
     * 查询开始时间（2020年10月29日00时00分）
     */
    private String startDate;
    /**
     * 查询结束时间（2020年10月29日00时00分）
     */
    private String endDate;

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
    private int alarmHandled;

    /**
     * 处理方式是下发短信报警数
     */
    private int msgNum;

    /**
     * 疲劳车辆信息
     */
    private String tiredBrand;

    /**
     * 超速车辆信息
     */
    private String overSpeedBrand;

    /**
     * 处理方式
     */
    private String handleResult;

    public static VehStateExportDTO getInstance(VehStateListDTO vehStateListDTO, VehStateQuery query) {
        VehStateExportDTO data = new VehStateExportDTO();
        BeanUtils.copyProperties(vehStateListDTO, data);
        data.startDate = query.getExportStartDate();
        data.endDate = query.getExportEndDate();

        return data;
    }

}
