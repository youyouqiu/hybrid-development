package com.zw.platform.domain.vas.alram.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:报警参数设置Query
 * @author:wangying
 * @time:2016年12月6日 下午5:00:49
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmSettingQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 报警参数设置
     */
    private String id;

    /**
     * 车辆Id
     */
    private String vehicleId;

    /**
     * 报警类型id
     */
    private String alarmParameterId;

    /**
     * 参数值
     */
    private String parameterValue;

    /**
     * 报警类型名称
     */
    private String name;

    /**
     * 报警类型的类型
     */
    private String type;

    /**
     * 是否可下发
     */
    private String sendFlag;

    /**
     * 报警类型
     */
    private String description;

    /**
     * 车辆id
     */
    private String vId;
    /**
     * 车辆类型
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

    /**
     * 下发参数id
     */
    private String paramId;

    /**
     * 通讯类型
     */
    private String deviceType;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private String groupId;

    private String assignmentId;
}
