package com.zw.platform.domain.statistic.info;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/12/25 9:51
 */
@Data
public class LatestLocationInfo implements Serializable {

    private static final long serialVersionUID = -35887998535559320L;

    /**
     * 车牌号
     */
    @ExcelField(title = "监控对象")
    private String plateNumber;

    /**
     * 监听对象类型  0：车辆；1：人；2：物品
     */
    private Integer monitorType;
    @ExcelField(title = "对象类型")
    private String monitorTypeStr;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;

    /**
     * 是否在线 1:在线; 2:不在线;
     */
    private Integer isOnline;
    @ExcelField(title = "在离线状态")
    private String isOnlineStr;

    /**
     * 协议类型
     */
    @ExcelField(title = "协议类型")
    private String deviceTypeStr;

    /**
     * gps时间
     */
    private Long vtime = 0L;
    @ExcelField(title = "定位时间")
    private String locationTime;

    /**
     * 速度
     */
    @ExcelField(title = "速度(km/h)")
    private String speed;

    /**
     * acc状态
     */
    @ExcelField(title = "ACC")
    private String accStatus;

    /**
     * 位置
     */
    @ExcelField(title = "位置")
    private String location;

    /**
     * 车id
     */
    private byte[] monitorIdByte;
    private String monitorId;

    /**
     * 位置状态
     */
    private String status;

    /**
     * 经度
     */
    private String longtitude;

    /**
     * 纬度
     */
    private String latitude;
}
