package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.util.Date;

/**
 * @Author: zjc
 * @Description:转发车辆列表信息
 * @Date: create in 2020/9/30 16:06
 */
@Data
public class OilForwardVehicleForm extends BaseFormBean {

    /**
     * 油补对接码组织
     */
    private String dockingCodeOrgId;

    /**
     * brand
     */
    private String brand;

    /**
     * 车辆所属企业id
     */
    private String vehicleOrgId;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 转发平台id
     */
    private String forwardingPlatformId;

    /**
     * 行业类别(1 公交 2 出租 3 农客 )
     */
    private Integer industryCategory;

    /**
     * 车辆状态(1 正常 2 停运 3 注销 4 删除 )
     */
    private Integer vehicleStatus;

    /**
     * 车架号
     */
    private String frameNumber;

    /**
     * 匹配状态(0代表失败，1代表成功)
     */
    private Integer matchStatus;

    /**
     * failed_reason 0企业未找到对应车辆档案1.油补平台没有此车辆2.油补平台与企业都无此车辆
     */
    private Integer failedReason;

    /**
     * 匹配时间
     */
    private Date matchTime;

    /**
     * 所属线路id
     */
    private String lineId;

    /**
     * 匹配的车辆id
     */
    private String matchVehicleId;

    /**
     * 车辆编码
     */
    private String vehicleCode;

    /**
     * 省编码
     */
    private String provinceCode;

    /**
     * 企业编码
     */
    private String orgCode;

    /**
     * 市编码
     */
    private String cityCode;

    /**
     * 县编码
     */
    private String countyCode;

    /**
     * 对接码
     */
    private String dockingCode;

}
