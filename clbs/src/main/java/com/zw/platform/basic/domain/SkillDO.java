package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技能管理
 *
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SkillDO extends BaseDO {
    /**
     * 名称
     */
    private String name;
    /**
     * 类别id
     */
    private String categoriesId;

    /**
     * 备注
     */
    private String remark;
}
