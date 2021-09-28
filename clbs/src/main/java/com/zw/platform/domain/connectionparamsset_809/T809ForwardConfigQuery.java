package com.zw.platform.domain.connectionparamsset_809;

import java.io.Serializable;
import java.util.Set;

import com.zw.platform.util.common.BaseQueryBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 809转发绑定关系数据查询类
 * @author hujun
 * @Date 创建时间：2018年3月2日 下午5:31:54
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class T809ForwardConfigQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Set<String> assignList;
}
