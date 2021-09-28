package com.zw.talkback.repository.mysql;

import com.zw.talkback.domain.basicinfo.form.SkillForm;
import com.zw.talkback.domain.basicinfo.model.SkillInfo;
import com.zw.talkback.domain.basicinfo.model.SkillsCategoriesInfo;
import com.zw.talkback.domain.basicinfo.query.SkillQuery;
import com.zw.talkback.domain.basicinfo.form.SkillsCategoriesForm;

import java.util.List;

/**
 * 人员技能管理
 */
public interface SkillDao {

    /**
     * 查询符合条件的所有技能类别详情
     * @param query 技能查询条件，目前只支持按技能类别查询
     * @return 技能详细列表
     */
    List<SkillsCategoriesForm> getSkillsCategories(SkillQuery query);

    /**
     * 添加职位类别信息
     * @param form 职位信息
     * @return 是否添加成功
     */
    boolean addSkillsCategories(SkillsCategoriesForm form);

    SkillsCategoriesForm findCategoriesByName(String name);

    SkillsCategoriesForm findCategoriesById(String id);

    boolean updateSkillsCategories(SkillsCategoriesForm form);

    List<SkillForm> findSkillByCategoriesId(String id);

    boolean deleteSkillsCategories(String id);

    List<SkillForm> getSkills(SkillQuery query);

    boolean addSkill(SkillForm form);

    SkillForm findSkillByName(String name);

    SkillForm findSkillById(String id);

    boolean updateSkill(SkillForm form);

    List<String> findPeopleIdBySkillId(String id);

    boolean deleteSkill(String id);

    List<SkillsCategoriesInfo> getAllCategories();

    List<SkillForm> getAllSkill();

    /**
     * 获得技能列表
     * @return  List<SkillInfo>
     */
    List<SkillInfo> getAllSkillList();

}
