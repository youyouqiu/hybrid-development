package com.zw.platform.domain.systems;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 参数下发实体
 * 
 * @author wangying
 *
 */
@Data
public class Directive implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
     * 下发指令
     */
    private String id;

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
     * 下发状态：0表示失败、1表示成功
     */
    private Integer status;

    /**
     * 下发时间
     */
    private Date downTime;

    /**
     * 备注
     */
    private String remark;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

}
