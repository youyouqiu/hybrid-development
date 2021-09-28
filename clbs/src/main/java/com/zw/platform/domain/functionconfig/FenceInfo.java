package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 围栏实体
 * @author wangying
 * @version 1.0
 * @date 2016年8月4日上午11:57:54
 */
@Data
public class FenceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 电子围栏信息
     */
    private String id;

    /**
     * 类型
     */
    private String type;

    /**
     * 形状（每个形状的ID）
     */
    private String shape;

    /**
     * 围栏名称
     */
    private String fenceName;

    /**
     * 预览
     */
    private String preview;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 围栏种类id
     */
    private String fenceTypeId;

    /**
     * 围栏面积
     */
    private Double area;

}
