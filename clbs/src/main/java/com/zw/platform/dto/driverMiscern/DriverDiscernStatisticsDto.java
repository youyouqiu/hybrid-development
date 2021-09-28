package com.zw.platform.dto.driverMiscern;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author penghj
 * @version 1.0
 */
@Data
public class DriverDiscernStatisticsDto implements Serializable {
    private static final long serialVersionUID = -2054221868991997925L;

    /**
     * 车辆驾驶员识别记录表id
     */
    private String id;
    /**
     * 车辆id
     */
    private String monitorId;
    /**
     * 车辆名称
     */
    private String monitorName;
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 比对结果，0匹配成功 1匹配失败 2超时 3没有启用该功能 4连接异常 5无指定人脸图片 6无人脸库 7 匹配失败，人证不符 8 匹配失败，比对超时 9 匹配失败，无指定人脸信息
     */
    private Integer identificationResult;
    /**
     * 比对相似度
     */
    private String matchRate;
    /**
     * 比对相似度阈值
     */
    private Integer matchThreshold;
    /**
     * 比对类型，0插卡比对 1巡检比对 2点火比对 3离开返回比对4 动态对比
     */
    private Integer identificationType;
    /**
     * 比对人脸id
     */
    private String driverId;
    /**
     * 比对驾驶员姓名
     */
    private String driverName;
    /**
     * 从业资格证号
     */
    private String cardNumber;
    /**
     * 比对时间
     */
    private String identificationTimeStr;

    private Date identificationTime;

    private String latitude;

    private String longitude;

    private String faceId;

    /**
     * 图像url
     */
    private String imageUrl;

    /**
     * 照片是否已删除（一般定义7天删除）
     */
    private Integer photoFlag;

    /**
     * 视频附件地址
     */
    private String videoUrl;


}
