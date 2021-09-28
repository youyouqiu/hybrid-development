package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: wangjianyu
 */
@Data
public class ManageFenceInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;//名称
    private String type;//类型
    private String shape;//形状
    private String description;//描述
    private String createDataUsername;//创建人
    private String createDataTime;//创建时间
}
