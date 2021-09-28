package com.zw.platform.domain.basicinfo;

import java.io.Serializable;

import lombok.Data;

@Data
public class AssignmentInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 分组id
     */
    private String id;

    /**
     * 该分组下的监控对象数量
     */
    private int vehicleNumber;

    /**
     * 分组名称
     */
    private String name;
}
