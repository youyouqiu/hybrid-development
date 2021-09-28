package com.zw.app.domain.videoResource;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppResourceListBean extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /* 车辆Id */
    private String vehicleId;

    /* 车牌 */
    private String brand;

    /* 终端编号 */
    private String deviceNumber;

    /* sim卡号 */
    private String simCardNumer;

    /* 通道号 */
    private String channlNumer;

    /* 视频开始时间 */
    private String startTime;

    /* 视频结束时间 */
    private String endTime;

    /* 每个音视频的文件下载后的全路径 */
    private String multiFilePath;

    /* 视频的日期 */
    private String date;

    /* 报警类型 */
    private String alarmType;

    /* 文件大小 */
    private String fileSize;

    /* ftp服务器的名称 */
    private String ftpName;

    /* 资源名称 */
    private String resourceName;

    /* 资源类型 */
    private String videoType;

    /* 码流 */
    private Integer streamType;

    /* 存储类型 */
    private Integer storageType;

    /* 0 终端，1 服务器 */
    private String type;

    /* 访问IP */
    private String ip;

    /* 下载存储路径 */
    private String downloadPath;

    /**
     * 流水号
     */
    private Integer msgSN;

    /**
     * 回放方式
     */
    private Integer remoteMode;

    /**
     * 快进快退倍数
     */
    private Integer forwardOrRewind;
}
