package com.zw.platform.domain.core;

import com.zw.platform.util.common.BaseTreeNodeBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 资源
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Resource extends BaseTreeNodeBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String codeNum; // 编码
    private String resourceName; // 名称
    private String iconCls; // 图标
    private Integer type; // 资源类型
    private String code; // 权限代码
    private String permission; // 权限标识
    private String permValue; // 权限
    private String description; // 描述
    private boolean expanded = true; // 是否展开
    private Integer sortOrder; // 排序
    private Integer editable; // 是否可编辑
    private Integer enabled; // 是否可用
    
    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private Integer flag;
    
    private List<Resource> childMenus = new ArrayList<Resource>();
    public void addChild(Resource child){
		this.childMenus.add(child);
	}
}