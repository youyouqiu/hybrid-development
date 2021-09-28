package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zjc
 * @Description:转发车辆列表信息
 * @Date: create in 2020/9/30 16:06
 */
@Data
public class OilForwardVehicleInfo {

    private String id;

    /**
     * 油补对接码组织
     */
    private String dockingCodeOrg;

    /**
     * brand
     */
    private String brand;

    /**
     * 车辆所属企业id
     */
    private String vehicleOrgId;

    /**
     * 车辆编码
     */
    private String vehicleCode;

    /**
     * 企业编码
     */
    private String orgCode;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 转发平台
     */
    private String forwardingPlatform;

    /**
     * 行业类别(1 公交 2 出租 3 农客 )
     */
    private Integer industryCategory;

    private String industryCategoryStr;

    public void setIndustryCategoryStr(Integer industryCategory) {
        if (industryCategory == null) {
            return;
        }
        switch (industryCategory) {
            case 1:
                this.industryCategoryStr = "公交";
                break;
            case 2:
                this.industryCategoryStr = "出租";
                break;
            case 3:
                this.industryCategoryStr = "农客";
                break;
            default:
                break;
        }
    }

    /**
     * 车辆状态(1 正常 2 停运 3 注销 4 删除 )
     */
    private Integer vehicleStatus;

    /**
     * 车辆状态(1 正常 2 停运 3 注销 4 删除 )
     */
    private String vehicleStatusStr;

    public void setVehicleStatusStr(Integer vehicleStatus) {
        if (vehicleStatus == null) {
            return;
        }
        switch (vehicleStatus) {
            case 1:
                this.vehicleStatusStr = "正常";
                break;
            case 2:
                this.vehicleStatusStr = "停运";
                break;
            case 3:
                this.vehicleStatusStr = "注销";
                break;
            case 4:
                this.vehicleStatusStr = "删除";
                break;
            default:
                break;
        }
    }

    /**
     * 车架号
     */
    private String frameNumber;

    /**
     * 匹配状态(0代表失败，1代表成功)
     */
    private Integer matchStatus;

    /**
     * 匹配状态(0代表失败，1代表成功)
     */
    private String matchStatusStr;

    public void setMatchStatusStr(Integer matchStatus) {
        if (matchStatus == null) {
            return;
        }
        switch (matchStatus) {
            case 0:
                this.matchStatusStr = "失败";
                break;
            case 1:
                this.matchStatusStr = "成功";
                break;
            default:
                break;
        }
    }

    /**
     * failed_reason 0企业未找到对应车辆档案1.油补平台没有此车辆2.油补平台与企业都无此车辆
     */
    private Integer failedReason;

    private String failedReasonStr;

    public void setFailedReasonStr(Integer failedReason) {
        if (failedReason == null) {
            return;
        }
        switch (failedReason) {
            case 0:
                this.failedReasonStr = "企业未找到对应车辆档案";
                break;
            case 1:
                this.failedReasonStr = "油补平台没有此车辆";
                break;
            case 2:
                this.failedReasonStr = "油补平台与企业都无此车辆";
                break;
            default:
                break;
        }
    }

    /**
     * 匹配时间
     */
    private String matchTimeStr;

    /**
     * 匹配时间
     */
    private Date matchTime;

    /**
     * 所属线路
     */
    private String lineName;

    /**
     * 所属线路
     */
    private String lineId;

    /**
     * 车辆的所属组织
     */
    private String vehicleOrg;


    /**
     * 对接码组织id
     */
    private String dockingCodeOrgId;

    /**
     * 809转发平台id
     */
    private String forwardingPlatformId;


}
