package com.zw.platform.util.jl;

import lombok.AllArgsConstructor;

/**
 * 吉林企业考核接口基础参数
 * @author penghj
 * @version 1.0
 * @date 2020/6/12 9:17
 */
@AllArgsConstructor
public enum JiLinOrgExamineInterfaceBaseParamEnum {
    /**
     * 上传停运车辆信息
     */
    ADD_VEHICLE_STOP_INFO("thirdpartymanage.uploadManage", "addVehicleStopInfo", "上传停运车辆信息"),
    /**
     * 上传违规车辆
     */
    ADD_VIOLATION_VEHICLES("thirdpartymanage.uploadManage", "addViolationVehicles", "上传违规车辆"),
    /**
     * 上传报警车辆
     */
    ADD_VEHICLE_ALARM_RELIEVE_INFO("thirdpartymanage.uploadManage", "addVehicleAlarmRelieveInfo", "上传报警车辆"),
    /**
     * 请求车辆信息
     */
    QUERY_ALONE_VEHICLE_INFO("thirdpartymanage.issuedManage", "queryAloneVehicleInfo", "请求车辆信息"),
    /**
     * 请求企业信息
     */
    QUERY_ALONE_CORP_INFO("thirdpartymanage.issuedManage", "queryAloneCorpInfo", "请求企业信息"),
    /**
     * 请求车辆营运状态
     */
    QUERY_VEHICLE_SERVICE_INFO("thirdpartymanage.issuedManage", "queryVehicleServiceInfo", "请求车辆营运状态"),
    /**
     * 请求监控平台考核信息
     */
    QUERY_PLATFORM_CHECK_INFO("thirdpartymanage.issuedManage", "queryPlatformCheckInfo", "请求监控平台考核信息"),
    /**
     * 请求企业考核信息
     */
    QUERY_CORP_CHECK_INFO("thirdpartymanage.issuedManage", "queryCorpCheckInfo", "请求企业考核信息"),
    /**
     * 请求企业车辆违规情况考核
     */
    QUERY_CORP_ALARM_CHECK_INFO("thirdpartymanage.issuedManage", "queryCorpAlarmCheckInfo", "请求企业车辆违规情况考核"),
    ;

    /**
     * 服务名称
     */
    private final String server;
    /**
     * 接口名称
     */
    private final String type;
    /**
     * 接口描述
     */
    private final String typename;

    public String getServer() {
        return server;
    }

    public String getType() {
        return type;
    }

    public String getTypename() {
        return typename;
    }
}
