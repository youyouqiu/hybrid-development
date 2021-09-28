package com.zw.talkback.service.baseinfo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.domain.basicinfo.form.SkillForm;
import com.zw.talkback.domain.basicinfo.form.SkillsCategoriesForm;
import com.zw.talkback.domain.basicinfo.model.SkillsCategoriesInfo;
import com.zw.talkback.domain.basicinfo.query.SkillQuery;
import com.zw.talkback.repository.mysql.SkillDao;
import com.zw.talkback.service.baseinfo.SkillService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SkillServiceImpl implements SkillService {

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private SkillDao skillDao;

    @Override
    public PageGridBean getSkillsCategories(SkillQuery query) {
        if (StringUtils.isNotEmpty(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        Page<SkillsCategoriesForm> skillsCategories =
                PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                        .doSelectPage(() -> skillDao.getSkillsCategories(query));
        return new PageGridBean(query, skillsCategories, true);
    }

    @Override
    public JsonResultBean addSkillsCategories(SkillsCategoriesForm form, String ipAddress) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = skillDao.addSkillsCategories(form);
        if (flag) {
            logSearchService.addLog(ipAddress, "新增技能类别", "3", "-");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public boolean checkCategoriesName(String name, String id) {
        SkillsCategoriesForm categoriesByName = skillDao.findCategoriesByName(name);
        if (categoriesByName != null) {
            if (StringUtils.isNotEmpty(id) && id.equals(categoriesByName.getId())) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public SkillsCategoriesForm findCategoriesById(String id) {
        return skillDao.findCategoriesById(id);
    }

    @Override
    public JsonResultBean updateSkillsCategories(SkillsCategoriesForm form, String ipAddress) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = skillDao.updateSkillsCategories(form);
        if (flag) {
            logSearchService.addLog(ipAddress, "修改技能类别", "3", "-");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public List<SkillForm> findSkillByCategoriesId(String id) {
        return skillDao.findSkillByCategoriesId(id);
    }

    @Override
    public JsonResultBean deleteSkillsCategories(String id, String ipAddress) {
        boolean flag = skillDao.deleteSkillsCategories(id);
        if (flag) {
            logSearchService.addLog(ipAddress, "删除技能类别", "3", "-");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public PageGridBean getSkills(SkillQuery query) {
        if (StringUtils.isNotEmpty(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        Page<SkillForm> skills = PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> skillDao.getSkills(query));
        return new PageGridBean(query, skills, true);
    }

    @Override
    public JsonResultBean addSkill(SkillForm form, String ipAddress) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = skillDao.addSkill(form);
        if (flag) {
            logSearchService.addLog(ipAddress, "新增技能", "3", "-");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public boolean checkSkillName(String name, String id) {
        SkillForm skillByName = skillDao.findSkillByName(name);

        if (skillByName == null) {
            return true;
        }
        if (StringUtils.isNotEmpty(id) && id.equals(skillByName.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public SkillForm findSkillById(String id) {
        return skillDao.findSkillById(id);
    }

    @Override
    public JsonResultBean updateSkill(SkillForm form, String ipAddress) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        SkillForm oldSkill = skillDao.findSkillById(form.getId());
        boolean flag = skillDao.updateSkill(form);
        if (flag) {
            // 维护人员技能缓存
            maintainPeopleSkillCache(form, oldSkill);
            logSearchService.addLog(ipAddress, "修改技能", "3", "-");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 维护人员技能缓存
     * @param nowSkill 修改后
     * @param oldSkill 修改前
     */
    private void maintainPeopleSkillCache(SkillForm nowSkill, SkillForm oldSkill) {
        String nowSkillName = nowSkill.getName();
        String oldSkillName = oldSkill.getName();
        if (!Objects.equals(oldSkillName, nowSkillName)) {
            List<String> peopleIdList = skillDao.findPeopleIdBySkillId(nowSkill.getId());
        }
    }

    @Override
    public List<String> findPeopleIdBySkillId(String id) {
        return skillDao.findPeopleIdBySkillId(id);
    }

    @Override
    public JsonResultBean deleteSkill(String id, String ipAddress) {
        boolean flag = skillDao.deleteSkill(id);
        if (flag) {
            logSearchService.addLog(ipAddress, "删除技能", "3", "-");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public List<SkillsCategoriesInfo> getAllCategories() {
        return skillDao.getAllCategories();
    }

    @Override
    public String getSkillTree() {
        List<SkillsCategoriesInfo> allCategories = skillDao.getAllCategories();
        List<SkillForm> allSkill = skillDao.getAllSkill();
        List<JSONObject> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allCategories) && CollectionUtils.isNotEmpty(allSkill)) {
            Set<String> categoriesIds = new HashSet<>();
            for (SkillForm form : allSkill) {
                JSONObject object = new JSONObject();
                object.put("id", form.getId());
                object.put("name", form.getName());
                object.put("pid", form.getCategoriesId());
                categoriesIds.add(form.getCategoriesId());
                result.add(object);
            }
            for (SkillsCategoriesInfo categoriesForm : allCategories) {
                if (categoriesIds.contains(categoriesForm.getId())) {
                    JSONObject object = new JSONObject();
                    object.put("id", categoriesForm.getId());
                    object.put("name", categoriesForm.getName());
                    result.add(object);
                }
            }
            return JSON.toJSONString(result);
        }
        return "";
    }
}
