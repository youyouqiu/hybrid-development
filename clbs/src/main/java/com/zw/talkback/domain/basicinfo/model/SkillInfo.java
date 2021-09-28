package com.zw.talkback.domain.basicinfo.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/8/15 15:06
 */
@Data
public class SkillInfo implements Serializable {
    private static final long serialVersionUID = 6622861133299906564L;
    /**
     * 技能id
     */
    private String id;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 技能类别名称
     */
    private String skillCategoryName;
}
