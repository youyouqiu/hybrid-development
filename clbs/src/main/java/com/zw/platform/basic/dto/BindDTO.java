package com.zw.platform.basic.dto;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 监控对象绑定信息DTO
 * @author zhangjuan
 * @date 2020/9/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BindDTO extends MonitorBaseDTO {
    @ApiModelProperty(value = "信息配置ID")
    private String configId;

    @ApiModelProperty(value = "终端ID")
    private String deviceId;

    @ApiModelProperty(value = "终端编号")
    private String deviceNumber;

    @ApiModelProperty(value = "终端手机号ID")
    private String simCardId;

    @ApiModelProperty(value = "终端手机号")
    private String simCardNumber;

    @ApiModelProperty(value = "真实终端手机号")
    private String realSimCardNumber;

    @ApiModelProperty(value = "终端类型 0:交通部JT/T808-2011(扩展);1:交通部JT/T808-2013;2:移为GV320;3:天禾;5:北斗天地协议;"
        + "8:博实结;9:ASO;10:F3超长待机;11:808-2019")
    private String deviceType;

    @ApiModelProperty(value = "功能类型 1:简易型车机；2：行车记录仪； 3：对讲设备；4：手咪设备")
    private String functionalType;

    @ApiModelProperty(value = "终端型号ID")
    private String terminalTypeId;

    @ApiModelProperty(value = "终端型号")
    private String terminalType;

    @ApiModelProperty(value = "终端厂商")
    private String terminalManufacturer;

    @ApiModelProperty(value = "分组ID,多个用逗号隔开")
    private String groupId;

    @ApiModelProperty(value = "分组名称,多个用逗号隔开")
    private String groupName;

    @ApiModelProperty(value = "绑定时间")
    private String bindDate;

    @ApiModelProperty(value = "修改绑定时间")
    private String updateBindDate;

    private String serviceLifecycleId;

    @ApiModelProperty(value = "计费日期")
    private String billingDate;

    @ApiModelProperty(value = "到期日期")
    private String expireDate;

    @ApiModelProperty(value = "从业人员id,多个用逗号隔开")
    private String professionalIds;

    @ApiModelProperty(value = "从业人员,多个用逗号隔开")
    private String professionalNames;

    @ApiParam(value = "车牌颜色（0蓝、1黄、2白、3黑）")
    private Integer plateColor;

    @ApiParam(value = "车牌颜色,一般在查询和导入时使用")
    private String plateColorStr;

    @ApiParam(value = "sim卡的鉴权码")
    private String authCode;

    /**
     * 终端所属组织ID
     */
    private String deviceOrgId;

    /**
     * 注册信息-制造商ID
     */
    private String manufacturerId;

    /**
     * Sim卡所属组织ID
     */
    private String simCardOrgId;

    /**
     * 车辆密码
     */
    private String vehiclePassword;

    /**
     * 极速录入唯一标识
     */
    private String uniqueNumber;

    /**
     * 标识视频
     */
    private Integer isVideo;
    /********对讲使用的字段********/
    @ApiParam(value = "对讲信息ID")
    private String intercomInfoId;
    @ApiParam(value = "对讲设备标识")
    private String intercomDeviceNumber;
    @ApiParam(value = "对讲设备标识, 调度平台的USER_ID")
    private Long userId;
    @ApiParam(value = "个呼号码,对讲使用")
    private String callNumber;

    /**
     * 导入使用
     */
    private String errorMsg;

    public JSONObject convertToTreeNode(String pid) {
        JSONObject treeNode = new JSONObject();
        treeNode.put("id", this.getId());
        treeNode.put("pId", pid);
        treeNode.put("name", this.getName());
        MonitorTypeEnum monitorTypeEnum = MonitorTypeEnum.getByType(this.getMonitorType());
        if (Objects.nonNull(monitorTypeEnum)) {
            treeNode.put("iconSkin", monitorTypeEnum.getIconSkin());
            treeNode.put("type", monitorTypeEnum.getEnName());
        }
        treeNode.put("monitorType", this.getMonitorType());
        treeNode.put("userId", this.getUserId());
        return treeNode;
    }
}
