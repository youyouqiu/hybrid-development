package com.zw.platform.util.common;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


/**
 * 基本form属性
 */
@Data
public abstract class BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * uuid
     */
    @ApiParam(value = "uuid")
    private String id = UUID.randomUUID().toString();

    /**
     * 是否显示
     */
    @ApiParam(value = "是否显示")
    private Integer flag = 1;

    /**
     * 优先级
     */
    @ApiParam(value = "优先级")
    private Integer priority = 1;

    /**
     * 顺序
     */
    @ApiParam(value = "顺序")
    private Integer sortOrder = 1;

    /**
     * 是否可编辑
     */
    @ApiParam(value = "是否可编辑")
    private Integer editable = 1;

    /**
     * 是否可用
     */
    @ApiParam(value = "是否可用")
    private Integer enabled = 1;

    /**
     * 数据创建时间
     */
    @ApiParam(value = "数据创建时间")
    private Date createDataTime = new Date();

    /**
     * 创建者username
     */
    @ApiParam(value = "创建者username")
    private String createDataUsername;

    /**
     * 数据修改时间
     */
    @ApiParam(value = "数据修改时间")
    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    @ApiParam(value = "修改者username")
    private String updateDataUsername;

}
