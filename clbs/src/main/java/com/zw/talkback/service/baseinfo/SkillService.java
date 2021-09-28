package com.zw.talkback.service.baseinfo;

import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.domain.basicinfo.form.SkillForm;
import com.zw.talkback.domain.basicinfo.form.SkillsCategoriesForm;
import com.zw.talkback.domain.basicinfo.model.SkillsCategoriesInfo;
import com.zw.talkback.domain.basicinfo.query.SkillQuery;

import java.util.List;

public interface SkillService {

    PageGridBean getSkillsCategories(SkillQuery query);

    JsonResultBean addSkillsCategories(SkillsCategoriesForm form, String ipAddress);

    boolean checkCategoriesName(String name, String id);

    SkillsCategoriesForm findCategoriesById(String id);

    JsonResultBean updateSkillsCategories(SkillsCategoriesForm form, String ipAddress);

    List<SkillForm> findSkillByCategoriesId(String id);

    JsonResultBean deleteSkillsCategories(String id, String ipAddress);

    PageGridBean getSkills(SkillQuery query);

    JsonResultBean addSkill(SkillForm form, String ipAddress);

    boolean checkSkillName(String name, String id);

    SkillForm findSkillById(String id);

    JsonResultBean updateSkill(SkillForm form, String ipAddress);

    List<String> findPeopleIdBySkillId(String id);

    JsonResultBean deleteSkill(String id, String ipAddress);

    List<SkillsCategoriesInfo> getAllCategories();

    String getSkillTree();

}
