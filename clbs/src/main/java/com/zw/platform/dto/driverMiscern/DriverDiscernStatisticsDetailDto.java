package com.zw.platform.dto.driverMiscern;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/24 16:02
 */
@Data
public class DriverDiscernStatisticsDetailDto implements Serializable {
    private static final long serialVersionUID = 2754283634075288767L;
    /**
     * 抓拍照片路径
     */
    private String imageUrl;
    /**
     * 比对类型，1插卡比对 2巡检比对 3点火比对 4离开返回比对
     */
    private Integer identificationType;
    /**
     * 比对时间
     */
    private String identificationTimeStr;

    private Date identificationTime;
    /**
     * 比对结果，1匹配成功 2匹配失败 3超时 4没有启用该功能 5连接异常 6无指定人脸图片 7无人脸库
     */
    private Integer identificationResult;
    /**
     * 比对相似度
     */
    private String matchRate;
    /**
     * 驾驶员照片路径
     */
    private String driverPhotoUrl;
    /**
     * 人脸id
     */
    private String driverId;

    /**
     * 驾驶员人脸id
     */
    private String faceId;

    /**
     * 姓名
     */
    private String driverName;
    /**
     * 从业资格证号
     */
    private String cardNumber;
    /**
     * 车辆名称
     */
    private String monitorName;
    /**
     * 企业名称
     */
    private String orgName;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 照片是否已删除  0 已删除  1  未删除
     */
    private Integer photoFlag;
}
