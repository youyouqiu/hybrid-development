package com.cb.platform.domain.fatiguedriving.monitor;

import com.zw.platform.util.common.DateUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 监控对象疲劳明细列表
 * @author Administrator
 */
@Data
public class MonitorFatigueDrivingDetailListDTO {

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 报警时间(格式 yyyyMMdddHHmmssSSS)
     */
    private String time;
    private String alarmTime;

    /**
     * 报警位置(经纬度)
     */
    private String location;

    /**
     * 报警位置
     */
    private String address;

    /**
     * 驾驶员
     */
    private String driverName;

    /**
     * 驾驶员电话
     */
    private String driverMobile;

    private static MonitorFatigueDrivingDetailListDTO getInstance() {
        MonitorFatigueDrivingDetailListDTO data = new MonitorFatigueDrivingDetailListDTO();
        data.monitorId = UUID.randomUUID().toString();
        data.time = "20200501000000L";
        data.location = "0,0";
        data.address = "未定位";
        data.driverName = "老张";
        data.driverMobile = "18084787457";
        return data;
    }

    public void initData() {
        this.alarmTime = DateUtil.getStringToString(time, DateUtil.DATE_FORMAT_SSS, DateUtil.DATE_FORMAT_SHORT);
    }

    public static List<MonitorFatigueDrivingDetailListDTO> getList(long length) {
        List<MonitorFatigueDrivingDetailListDTO> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(getInstance());
        }
        return list;
    }

}
