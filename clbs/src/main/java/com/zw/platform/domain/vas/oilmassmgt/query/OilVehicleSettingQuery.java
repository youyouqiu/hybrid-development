package com.zw.platform.domain.vas.oilmassmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 *  油箱车辆关联Query
 * <p>Title: OilVehicleSettingQuery.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年10月26日上午9:13:05
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OilVehicleSettingQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	 /**
     * 油箱与车辆关联
     */
   private String id;

    /**
     *  油箱id
     */
    private String oilBoxId;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     *  油箱类型  油箱1   油箱2
     */
    private String oilBoxType;

    /**
     * 自动上传时间
     */
    private Integer autoUploadTime;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

    /**
     *  液位报警阈值
     */
    private String liquidAlarmThreshold;

    /**
     *  油箱型号
     */
    private String type;

    /**
     *  油箱形状
     */
    private String shape;

    /**
     *  长度
     */
    private String boxLength;

    /**
     *  宽度
     */
    private String width;

    /**
     *  高度
     */
    private String height;

    /**
     *  壁厚
     */
    private String thickness;

    /**
     *  加油时间阈值
     */
    private String addOilTimeThreshold;

    /**
     * 加油量时间阈值
     */
    private String addOilAmountThreshol;

    /**
     *  漏油时间阈值
     */
    private String seepOilTimeThreshold;

    /**
     *  楼油量时间阈值
     */
    private String seepOilAmountThreshol;

    /**
     *  理论容积
     */
    private String theoryVolume;

    /**
     *  油箱容量
     */
    private String realVolume;

    /**
     *  标定数组
     */
    private String calibrationSets;

    /**
     *  车辆类型
     */
    private String vehicleType;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 组织
     */
    private String groups;

    /**
     * 下发状态
     */
    private Integer status;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private String groupId; // 组织

    private String assignmentId; // 分组
    private String protocol; // 协议类型


}
