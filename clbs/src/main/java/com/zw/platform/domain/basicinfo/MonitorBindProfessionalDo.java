package com.zw.platform.domain.basicinfo;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/2/9 11:13
 */
@Data
public class MonitorBindProfessionalDo {
    /**
     * 监控对象id
     */
    private String moId;
    /**
     * 监控对象绑定的从业人员id
     */
    private String professionalId;
    /**
     * 监控对象绑定的从业人员名称
     */
    private String professionalName;
    /**
     * 监控对象绑定的从业人员电话
     */
    private String phone;
}
