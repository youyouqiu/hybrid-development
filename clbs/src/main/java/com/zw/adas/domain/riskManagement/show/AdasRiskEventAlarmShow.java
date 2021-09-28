package com.zw.adas.domain.riskManagement.show;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 @Author zhengjc
 @Date 2019/2/18 16:22
 @Description 风险处置记录展现实体实体bean
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventAlarmShow {

    /**
     * 事件id
     */
    private String id;

    /**
     * 事件编号
     */
    private String eventNumber;

    /**
     * 事件的时间
     */

    private String eventTime;

    /**
     * 事件名称
     */
    private String riskEvent;
    /**
     * 事件所属的风险类型
     */

    private String riskType;

    /**
     * 事件id
     */

    private String eventId;
    /**
     * 速度
     */

    private String speed;

    /**
     * 是否有终端多媒体文件
     */
    private Boolean hasMedia;

    /**
     * 视频终端证据
     */
    private Boolean hasVideo;

    /**
     * 图片终端证据
     */
    private Boolean hasPic;

    /**
     * 手动下发9208的媒体状态（0代表可以获取附件，1代表附件失效不能点击获取附件，2代表附件获取中）
     */
    private Byte attachmentStatus;

    /**
     * 原车纬度 ORIGINAL_LONGITUDE
     */
    private String originalLatitude;

    /**
     * 原车经度 ORIGINAL_LATITUDE
     */
    private String originalLongitude;

    /**
     * 报警等级
     */
    private String level;

    /**
     * 车辆状态
     */
    private String vehicleStatus;

    /**
     * 监控对象
     */
    private String brand;

    /**
     *车辆颜色 PLATE_COLOR
     */
    private String plateColor;

    /**
     * 经度
     */
    private String  latitude;

    /**
     * 纬度
     */
    private String longitude;

    /**
     * 道路类型
     */
    private Integer roadType;

    private String roadTypeStr;

    /**
     * 路网限速
     */
    private String roadLimitSpeed;

    /**
     * 数量
     */
    private Integer mediaCount;

    /**
     * 媒体信息mediaInfo
     */
    private String mediaInfoStr;

    /**
     * 历史(事件中存的，用于前端控制车辆状态的展现，黑标车不展现)
     */
    private Integer eventProtocolType;
}
