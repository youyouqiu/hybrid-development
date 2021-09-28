package com.zw.talkback.controller.basicinfo;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.domain.basicinfo.form.SkillForm;
import com.zw.talkback.domain.basicinfo.form.SkillsCategoriesForm;
import com.zw.talkback.domain.basicinfo.model.SkillsCategoriesInfo;
import com.zw.talkback.domain.basicinfo.query.SkillQuery;
import com.zw.talkback.service.baseinfo.SkillService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 人员技能管理Controller层
 */
@Controller
@RequestMapping("/talkback/basicinfo/skill")
public class SkillController {

    private Logger logger = LogManager.getLogger(SkillController.class);

    @Autowired
    private SkillService skillService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;

    private static final String LIST_PAGE = "talkback/basicinfo/enterprise/skill/list";

    private static final String ADD_CATEGORIES_PAGE = "talkback/basicinfo/enterprise/skill/addCategories";

    private static final String ADD_SKILL_PAGE = "talkback/basicinfo/enterprise/skill/add";

    private static final String EDIT_CATEGORIES_PAGE = "talkback/basicinfo/enterprise/skill/editCategories";

    private static final String EDIT_SKILL_PAGE = "talkback/basicinfo/enterprise/skill/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 查询职位类别详情
     * @param query 获取职位类别请求体
     * @return 分业的职位类别列表数据
     */
    @RequestMapping(value = "/getSkillsCategories", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getSkillsCategories(SkillQuery query) {
        try {
            return skillService.getSkillsCategories(query);
        } catch (Exception e) {
            logger.error("获取技能类别异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/addSkillsCategories", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String getAddSkillsCategoriesPage() {
        return ADD_CATEGORIES_PAGE;
    }

    @RequestMapping(value = "/addSkillsCategories", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addSkillsCategories(SkillsCategoriesForm form) {
        try {
            if (form == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return skillService.addSkillsCategories(form, ipAddress);
        } catch (Exception e) {
            logger.error("新增技能类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/checkCategoriesName", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkCategoriesName(String name, String id) {
        try {
            return skillService.checkCategoriesName(name, id);
        } catch (Exception e) {
            logger.error("校验类别名称异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/editSkillsCategories", method = RequestMethod.GET)
    public ModelAndView getEditSkillsCategoriesPage(String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                SkillsCategoriesForm categoriesById = skillService.findCategoriesById(id);
                if (categoriesById == null) {
                    return new ModelAndView(ERROR_PAGE);
                }
                ModelAndView mav = new ModelAndView(EDIT_CATEGORIES_PAGE);
                mav.addObject("result", categoriesById);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("获取修改技能类别页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/editSkillsCategories", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editSkillsCategories(SkillsCategoriesForm form) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return skillService.updateSkillsCategories(form, ipAddress);
        } catch (Exception e) {
            logger.error("修改技能类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/deleteSkillsCategories", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteSkillsCategories(String id) {
        try {
            if (StringUtils.isEmpty(id)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            List<SkillForm> skillByCategoriesId = skillService.findSkillByCategoriesId(id);
            if (CollectionUtils.isNotEmpty(skillByCategoriesId)) {
                return new JsonResultBean(JsonResultBean.FAULT, "该技能类别中存在技能，不能删除！");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return skillService.deleteSkillsCategories(id, ipAddress);
        } catch (Exception e) {
            logger.error("删除技能类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/getSkills", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getSkills(SkillQuery query) {
        try {
            return skillService.getSkills(query);
        } catch (Exception e) {
            logger.error("获取技能列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/addSkill", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView getAddSkillPage() {
        try {
            ModelAndView mav = new ModelAndView(ADD_SKILL_PAGE);
            List<SkillsCategoriesInfo> allCategories = skillService.getAllCategories();
            mav.addObject("allCategories", JSON.toJSONString(allCategories));
            return mav;
        } catch (Exception e) {
            logger.error("获取新增技能页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/addSkill", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addSkill(SkillForm form) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            if (form.getCategoriesId() != null) {
                return skillService.addSkill(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "技能类别不能为空");
        } catch (Exception e) {
            logger.error("新增技能异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/checkSkillName", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkSkillName(String name, String id) {
        try {
            return skillService.checkSkillName(name, id);
        } catch (Exception e) {
            logger.error("校验技能名称异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/editSkill", method = RequestMethod.GET)
    public ModelAndView getEditSkillPage(String id) {
        try {
            if (StringUtils.isEmpty(id)) {
                return new ModelAndView(ERROR_PAGE);
            }
            SkillForm skillById = skillService.findSkillById(id);
            List<SkillsCategoriesInfo> allCategories = skillService.getAllCategories();
            ModelAndView mav = new ModelAndView(EDIT_SKILL_PAGE);
            mav.addObject("result", skillById);
            mav.addObject("allCategories", allCategories);
            return mav;
        } catch (Exception e) {
            logger.error("获取修改技能页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/editSkill", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editSkill(SkillForm form) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return skillService.updateSkill(form, ipAddress);
        } catch (Exception e) {
            logger.error("修改技能异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/deleteSkill", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteSkill(String id) {
        try {
            if (StringUtils.isEmpty(id)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            List<String> peopleIdBySkillId = skillService.findPeopleIdBySkillId(id);
            if (CollectionUtils.isNotEmpty(peopleIdBySkillId)) {
                return new JsonResultBean(JsonResultBean.FAULT, "已存在人员设置了该技能，不能删除！");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return skillService.deleteSkill(id, ipAddress);
        } catch (Exception e) {
            logger.error("删除技能异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }
}
