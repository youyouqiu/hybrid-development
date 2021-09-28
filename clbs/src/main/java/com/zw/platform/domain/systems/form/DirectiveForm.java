package com.zw.platform.domain.systems.form;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


/**
 * 参数下发Form
 * @author wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DirectiveForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 指令名称
     */
    private String directiveName;

    /**
     * 监控对象ID
     */
    private String monitorObjectId;

    /**
     * 参数类型
     */
    private String parameterType;

    /**
     * 名称
     */
    private String parameterName;

    /**
     * 下发状态：
     * 0:参数已生效; 1:参数未生效; 2:参数消息有误; 3:参数不支持;
     * 4:参数下发中; 5:终端离线，未下发; 7:终端处理中; 8:终端接收失败;
     */
    private Integer status;

    /**
     * 下发时间
     */
    private Date downTime;

    /**
     * 流水号
     */
    private Integer swiftNumber;

    /**
     * 下发回应code：0 已回应；1 未回应
     */
    private Integer replyCode;

    /**
     * 备注
     */
    private String remark;

    /**
     * 1:修改; 2:新增
     */
    private Integer updateOrAdd = 1;

    /**
     * 车牌号
     */
    private String plateNumber;

    public DirectiveForm() {

    }

    public DirectiveForm(String directiveName, String monitorObjectId, String parameterType, String parameterName,
        Integer status, Date downTime, Integer swiftNumber, Integer replyCode, String remark) {
        this.directiveName = directiveName;
        this.monitorObjectId = monitorObjectId;
        this.parameterType = parameterType;
        this.parameterName = parameterName;
        this.status = status;
        this.downTime = downTime;
        this.swiftNumber = swiftNumber;
        this.replyCode = replyCode;
        this.remark = remark;
    }

    public DirectiveForm(String directiveName, String monitorObjectId, String parameterType) {
        this.directiveName = directiveName;
        this.monitorObjectId = monitorObjectId;
        this.parameterType = parameterType;
        this.parameterName = parameterName;
    }
}
