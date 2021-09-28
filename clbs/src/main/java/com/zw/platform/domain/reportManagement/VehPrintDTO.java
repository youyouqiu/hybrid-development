package com.zw.platform.domain.reportManagement;

import com.zw.platform.domain.reportManagement.query.VehStateQuery;
import lombok.Data;

/**
 * @Author: zjc
 * @Description:车辆打印所需要的的数据信息
 * @Date: create in 2020/11/18 9:21
 */
@Data
public class VehPrintDTO {

    /**
     * 疲劳车辆信息
     */
    private String tiredBrand;

    /**
     * 超速车辆信息
     */
    private String overSpeedBrand;

    /**
     * 报警处理条数
     */
    private int alarmHandled;

    /**
     * 处理方式是下发短信报警数
     */
    private int msgNum;

    /**
     * 处理方式
     */
    private String handleResult;

    /**
     * 查询开始时间（2020年10月29日00时00分）
     */
    private String startDate;
    /**
     * 查询结束时间（2020年10月29日00时00分）
     */
    private String endDate;

    public static VehPrintDTO getInstance(VehStateListDTO vehStateListDTO, VehStateQuery query) {
        VehPrintDTO vehPrintDTO = new VehPrintDTO();
        vehPrintDTO.msgNum = vehStateListDTO.getMsgNum();
        vehPrintDTO.alarmHandled = vehStateListDTO.getAlarmHandled();
        vehPrintDTO.tiredBrand = vehStateListDTO.getTiredBrand();
        vehPrintDTO.overSpeedBrand = vehStateListDTO.getOverSpeedBrand();
        vehPrintDTO.handleResult = vehStateListDTO.getHandleResult();
        vehPrintDTO.startDate = query.getExportStartDate();
        vehPrintDTO.endDate = query.getExportEndDate();
        return vehPrintDTO;
    }

}
