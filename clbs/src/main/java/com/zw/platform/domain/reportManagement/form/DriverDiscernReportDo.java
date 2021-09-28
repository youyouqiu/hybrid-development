package com.zw.platform.domain.reportManagement.form;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @author denghuabing
 * @version V1.0
 * @description: 驾驶员身份识别上报实体
 * @date 2020/9/27
 **/
@Data
public class DriverDiscernReportDo {

    private String id = UUID.randomUUID().toString();

    /**
     * 车辆id
     */
    private String monitorId;

    /**
     * 驾驶员id
     */
    private String driverId;

    /**
     * 比对时间
     */
    private Date identificationTime;

    /**
     * 比对类型，0插卡比对 1巡检比对 2点火比对 3离开返回比对4 动态对比
     */
    private Integer identificationType;

    /**
     * 比对结果，0匹配成功 1匹配失败 2超时 3没有启用该功能 4连接异常 5无指定人脸图片 6无人脸库 7 匹配失败，人证不符
     * 8 匹配失败，比对超时 9 匹配失败，无指定人脸信息 10 无驾驶员图片 11 终端人脸库为空
     */
    private Integer identificationResult;

    /**
     * 比对相似度，单位%
     */
    private String matchRate;

    /**
     * 比对相似度阈值，单位%
     */
    private Integer matchThreshold;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 图像url
     */
    private String imageUrl;

    /**
     * 照片是否已删除（一般定义7天删除）
     */
    private Integer photoFlag;

    /**
     * 驾驶员人脸id
     */
    private String faceId;

    /**
     * 从业资格证号
     */
    private String cardNumber;

    /**
     * 视频附件地址
     */
    private String videoUrl;



}
