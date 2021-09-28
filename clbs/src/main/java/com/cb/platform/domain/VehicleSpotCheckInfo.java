package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/21 11:03
 */
@Data
public class VehicleSpotCheckInfo implements Serializable {
    private static final long serialVersionUID = -4147490871404194745L;
    public static final int SPOT_CHECK_CONTENT_LOCATION = 0;
    public static final int SPOT_CHECK_CONTENT_TRACK_PLAY_BACK = 1;
    public static final int SPOT_CHECK_CONTENT_VIDEO = 2;
    public static final int SPOT_CHECK_CONTENT_DEAL_ALARM = 3;

    /**
     * id
     */
    private String id = UUID.randomUUID().toString();
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 车牌号
     */
    @ExcelField(title = "车牌号")
    private String plateNumber;
    /**
     * 车辆颜色
     */
    @ExcelField(title = "车辆颜色")
    private String plateColor;
    /**
     * 车辆类型
     */
    @ExcelField(title = "车辆类型")
    private String vehicleType;
    /**
     * 所属道路运输企业
     */
    @ExcelField(title = "所属道路运输企业")
    private String groupName;
    /**
     * 定位时间
     */
    private Date locationTime;
    @ExcelField(title = "定位时间")
    private String locationTimeStr;
    /**
     * 速度
     */
    @ExcelField(title = "速度")
    private String speed;
    /**
     * 限速
     */
    @ExcelField(title = "限速")
    private String speedLimit;
    /**
     * 经度
     */
    private String longtitude;
    /**
     * 纬度
     */
    private String latitude;
    /**
     * 位置
     */
    @ExcelField(title = "位置")
    private String address;
    /**
     * 抽查内容 0:查看定位信息; 1:查看历史轨迹; 2:查看视频; 3:违章处理
     */
    private Integer spotCheckContent;
    @ExcelField(title = "抽查内容")
    private String spotCheckContentStr;
    /**
     * 抽查人
     */
    @ExcelField(title = "抽查人")
    private String spotCheckUser;
    /**
     * 抽查人id
     */
    private String spotCheckUserId;
    /**
     * 抽查时间
     */
    private Date spotCheckTime;
    @ExcelField(title = "抽查时间")
    private String spotCheckTimeStr;
    /**
     * 实际查看日期
     */
    private Date actualViewDate;
}
