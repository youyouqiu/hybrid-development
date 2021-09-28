package com.zw.platform.basic.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 监控对象查询类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorQuery {
    /**
     * 分组ID 可为空
     */
    private Collection<String> groupIds;

    /**
     * 监控模糊搜索查询类型
     * name/空:按监控对象名称，
     * simcardNumber/simCardNumber：按终端手机号
     * deviceNumber:按终端号
     * professional：从业人员
     * monitorId:监控对象Id
     */
    private String queryType;

    /**
     * 模糊搜索关键字
     */
    private String keyword;

    /**
     * 协议类型集合
     */
    private Collection<String> deviceTypes;

    /**
     * 监控对象在离线状态 1、在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
     */
    private Integer status;

    /**
     * 监控对象类型 vehicle：车 thing 物 people：人
     */
    private String monitorType;

    /**
     * 根据车辆类型名称
     */
    private String vehicleTypeName;

    public String getQueryType() {
        if (StringUtils.isBlank(queryType)) {
            return "name";
        }
        if (Objects.equals("simcardNumber", queryType)) {
            return "simCardNumber";
        }
        return queryType;
    }

    public MonitorQuery(Collection<String> deviceTypes, String queryType, String keyword, Integer status) {
        this.deviceTypes = deviceTypes;
        this.queryType = queryType;
        this.keyword = keyword;
        this.status = status;
    }
}
