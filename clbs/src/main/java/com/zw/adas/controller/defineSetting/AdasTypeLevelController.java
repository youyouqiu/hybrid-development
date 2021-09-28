package com.zw.adas.controller.defineSetting;


import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.AdasRiskEvent;
import com.zw.adas.domain.riskManagement.bean.AdasRiskLevelFromBean;
import com.zw.adas.domain.riskManagement.query.AdasRiskEventQuery;
import com.zw.adas.domain.riskManagement.query.AdasRiskLevelQuery;
import com.zw.adas.service.defineSetting.AdasRiskEventService;
import com.zw.adas.service.defineSetting.AdasRiskLevelService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;


/**
 * 风险类型等级Controller
 * Created by wjy on 2017/8/15.
 */
@Controller
@RequestMapping("/r/riskManagement/TypeLevel")
public class AdasTypeLevelController {
    private static Logger log = LogManager.getLogger(AdasTypeLevelController.class);

    private static final String LIST_PAGE = "risk/riskManagement/TypeLevel/list";

    private static final String ADD_RISKLEVEL_PAGE = "risk/riskManagement/TypeLevel/add";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String EDIT_RISKLEVEL_PAGE = "risk/riskManagement/TypeLevel/edit";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${risklevel.exist}")
    private String risklevelExist;

    @Autowired
    private AdasRiskEventService adasRiskEventService;

    @Autowired
    private AdasRiskLevelService adasRiskLevelService;

    /**
     * 风险类型等级页面
     *
     * @return 页面资源
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 风险等级新增页面
     *
     * @return 页面资源
     */
    @RequestMapping(value = {"/addRiskLevel"}, method = RequestMethod.GET)
    public String addRiskLevelPage() {
        try {
            return ADD_RISKLEVEL_PAGE;
        } catch (Exception e) {
            log.error("跳转到新增风险等级页面失败", e);
            return ERROR_PAGE;
        }
    }

    /**
     * 风险等级修改页面
     *
     * @param uuid 风险等级Id
     * @return 页面资源
     */
    @RequestMapping(value = {"/editLevel_{uuid}.gsp"}, method = RequestMethod.GET)
    public ModelAndView editLevel(@PathVariable("uuid") String uuid) {
        try {
            AdasRiskLevelQuery query = new AdasRiskLevelQuery();
            query.setId(uuid);
            query.setSimpleQueryParam(null);
            ModelAndView model = new ModelAndView(EDIT_RISKLEVEL_PAGE);
            List<Map<String, String>> list = adasRiskLevelService.getRiskLevel(query);
            if (list.isEmpty() || list.size() != 1) {
                log.error("跳转到修改风险等级页面失败,请选择一条风险数据");
                return new ModelAndView(ERROR_PAGE);
            }
            model.addObject("levelInfo", list.get(0));
            return model;
        } catch (Exception e) {
            log.error("跳转到修改风险等级页面失败", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 风险事件表格数据
     *
     * @param query 查询参数
     * @return 分页数据
     */
    @RequestMapping(value = "/riskEvent/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final AdasRiskEventQuery query) {
        try {
            Page<AdasRiskEvent> result = adasRiskEventService.findByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 风险等级表格数据
     *
     * @param query 查询参数
     * @return 分页数据
     */
    @RequestMapping(value = {"/levelList"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAllRiskLevel(final AdasRiskLevelQuery query) {
        try {
            if (query != null) {
                Page<Map<String, String>> result = PageHelperUtil.doSelect(query, () -> adasRiskLevelService.getRiskLevel(query));
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("分页查询分组（getAllRiskLevel）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 风险等级删除
     *
     * @param riskId 风险等级Id
     * @return 删除结果
     */
    @RequestMapping(value = {"/levelDelete_{riskId}.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteLevel(@PathVariable("riskId") String riskId) {
        try {
            String[] uuids = riskId.split(",");
            adasRiskLevelService.deleteLevels(uuids);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除风险等级(deleteLevel)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 添加风险等级信息
     *
     * @param bean 页面输入的风险等级信息
     * @return 结果
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addRiskLevel(AdasRiskLevelFromBean bean) {
        try {
            if (adasRiskLevelService.isNotExsit(bean)) {
                adasRiskLevelService.addRiskLevel(bean);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, risklevelExist);
        } catch (Exception e) {
            log.error("添加风险等级(addRiskLevel)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改风险等级
     *
     * @param bean 页面输入的风险等级信息
     * @return 结果
     */
    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateRiskLevel(AdasRiskLevelFromBean bean) {
        try {
            if (adasRiskLevelService.isNotExsit(bean)) {
                adasRiskLevelService.updateRiskLevel(bean);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, risklevelExist);
        } catch (Exception e) {
            log.error("修改风险等级(updateRiskLevel)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取风险等级名称
     *
     * @return 风险等级名称和结果
     */
    @RequestMapping(value = {"/riskLevelName"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllRiskLevelName() {
        try {
            return new JsonResultBean(adasRiskLevelService.getAllLevelName());
        } catch (Exception e) {
            log.error("获取所有风险等级名称失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}
