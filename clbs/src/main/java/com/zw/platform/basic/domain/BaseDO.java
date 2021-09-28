package com.zw.platform.basic.domain;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @Author: zjc
 * @Description:继承一些基础信息和方法
 * @Date: create in 2020/10/22 14:15
 */
@Data
public class BaseDO {
    /**
     * id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 数据创建时间
     */

    private Date createDataTime = new Date();

    /**
     * 创建者username
     */

    private String createDataUsername;

    /**
     * 数据修改时间
     */

    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    private String updateDataUsername;
    /**
     * 逻辑删除字段
     */
    private Integer flag = 1;

}
