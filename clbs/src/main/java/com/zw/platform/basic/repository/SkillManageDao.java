package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.SkillDO;

import java.util.List;

/**
 * 技能管理DAO类
 *
 * @author zhangjuan
 */
public interface SkillManageDao {
    /**
     * 获取所有的技能
     *
     * @return 所有的技能列表
     */
    List<SkillDO> getAllSkill();
}
