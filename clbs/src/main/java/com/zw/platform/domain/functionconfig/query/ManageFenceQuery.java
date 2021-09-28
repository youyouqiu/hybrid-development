package com.zw.platform.domain.functionconfig.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @author: wangjianyu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ManageFenceQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;//名称
    private String type;//类型
    private String shape;//形状
    private String description;//描述
    private String createdatausername;//创建人
}
