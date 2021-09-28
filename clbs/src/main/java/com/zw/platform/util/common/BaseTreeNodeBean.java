package com.zw.platform.util.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 树
 */
@Data
public abstract class BaseTreeNodeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;

    private String text;

    private String parentId;

    /**
     * 是否展开
     */
    private boolean expanded;

    /**
     * 是否叶子节点
     */
    private boolean leaf = true;

    private int nodeLevel = 0;

    /**
     * 子节点
     */
    private List<BaseTreeNodeBean> children = new ArrayList<BaseTreeNodeBean>();

    private List<BaseTreeNodeBean> nodes = null;
}
