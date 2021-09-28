package com.zw.api.domain;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@ApiModel("监控对象信息")
public class MonitorInfo {
    @ApiModelProperty("监控对象id")
    private String id;

    @ApiModelProperty("监控对象名称")
    private String name;

    @ApiModelProperty("监控对象类型")
    private String type;

    @ApiModelProperty("所属组织id")
    private String orgId;

    @ApiModelProperty("所属组织名称")
    private String orgName;

    @ApiModelProperty("分组id,分组间用逗号隔开")
    private String assignmentId;

    @ApiModelProperty("分组名称,分组间用逗号隔开")
    private String assignmentName;

    @ApiModelProperty("终端id")
    private String deviceId;

    @ApiModelProperty("终端编号")
    private String deviceNumber;

    @ApiModelProperty("sim卡id")
    private String simcardId;

    @ApiModelProperty("终端手机号")
    private String simcardNumber;

    public static MonitorInfo fromCache(JSONObject monitorObj) {
        MonitorInfo info = new MonitorInfo();
        info.setType(getMonitorType(monitorObj.getString("monitorType")));
        info.setAssignmentId(monitorObj.getString("assignmentId"));
        info.setAssignmentName(monitorObj.getString("assignmentName"));
        info.setOrgId(monitorObj.getString("groupId"));
        info.setOrgName(monitorObj.getString("groupName"));
        info.setSimcardId(monitorObj.getString("simcardId"));
        info.setSimcardNumber(monitorObj.getString("simcardNumber"));
        info.setDeviceId(monitorObj.getString("deviceId"));
        info.setDeviceNumber(monitorObj.getString("deviceNumber"));
        return info;
    }

    public static MonitorInfo fromCache(Map<String, String> monitorObj) {
        MonitorInfo info = new MonitorInfo();
        info.setType(getMonitorType(monitorObj.get("monitorType")));
        info.setAssignmentId(monitorObj.get("groupId"));
        info.setAssignmentName(monitorObj.get("groupName"));
        info.setOrgId(monitorObj.get("orgId"));
        info.setOrgName(monitorObj.get("orgName"));
        info.setSimcardId(monitorObj.get("simCardId"));
        info.setSimcardNumber(monitorObj.get("simCardNumber"));
        info.setDeviceId(monitorObj.get("deviceId"));
        info.setDeviceNumber(monitorObj.get("deviceNumber"));
        return info;
    }

    private static String getMonitorType(String type) {
        switch (type) {
            case "0":
                return "车辆";
            case "1":
                return "人";
            case "2":
                return "物品";
            default:
                return "未知";
        }
    }
}
