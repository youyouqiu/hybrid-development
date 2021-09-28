package com.zw.platform.controller.core;

import com.alibaba.fastjson.JSON;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.platform.service.core.CustomColumnService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 定制列controller
 * @author zhouzongbo on 2019/3/11 14:36
 */
@Controller
@RequestMapping("/core/uum/custom/")
public class CustomColumnController {

    private static final Logger logger = LogManager.getLogger(CustomColumnController.class);

    private static final String CUSTOM_COLUMN_SETTING_PAGE = "/core/uum/custom/setting";
    private static final String CUSTOM_COLUMN_TRACKPLAY_SETTING_PAGE = "/core/uum/custom/trackPlaySetting";
    private static final String CUSTOM_COLUMN_MULTI_WINDOW_SETTING_PAGE = "/core/uum/custom/multiWindowSetting";

    private static final String ERROR_PAGE = "html/errors/error_exception";
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private CustomColumnService customColumnService;

    /**
     * 获取自定列设置页面
     * @param columnModule 功能点标识: 如实时监控: REALTIME_MONITORING
     * @return
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/setting_{columnModule}", method = RequestMethod.GET)
    public ModelAndView getCustomColumnSettingPage(@PathVariable("columnModule") String columnModule) {
        try {
            ModelAndView modelAndView = new ModelAndView(getViewName(columnModule));
            List<Map<String, Object>> resultList = customColumnService.findCustomColumnModule(columnModule);
            modelAndView.addObject("resultList", JSON.toJSONString(resultList));
            return modelAndView;
        } catch (Exception e) {
            logger.error("获取自定列设置页面错误", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/getSettingByColumnModule", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSettingByColumnModule(String columnModule) {
        return AdasControllerTemplate
            .getResultBean(() -> customColumnService.findCustomColumnModule(columnModule), "获取自定列设置页面错误");

    }

    private String getViewName(String columnModule) {
        String viewName = "";
        switch (columnModule) {
            case "REALTIME_MONITORING":
                viewName = CUSTOM_COLUMN_SETTING_PAGE;
                break;
            case "TRACKPLAY":
                viewName = CUSTOM_COLUMN_TRACKPLAY_SETTING_PAGE;
                break;
            case "MULTI_WINDOW_REALTIME_MONITORING":
                viewName = CUSTOM_COLUMN_MULTI_WINDOW_SETTING_PAGE;
                break;
            default:
                break;
        }
        return viewName;
    }

    /**
     * 添加绑定关系
     * @param request                request
     * @param customColumnConfigJson customColumnConfigJson
     * @param title                  用于打印日志
     * @return JsonResultBean
     */
    @RequestMapping(value = "/addCustomColumnConfig", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addCustomColumnConfig(HttpServletRequest request, String customColumnConfigJson,
        String title) {
        try {
            if (StrUtil.areNotBlank(customColumnConfigJson, title)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return customColumnService.addCustomColumnConfig(customColumnConfigJson, title, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
        } catch (Exception e) {
            logger.error("添加用户自定义列绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 查询自定义类数据
     * @return
     */
    @RequestMapping(value = "/findCustomColumnInfoByMark", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findCustomColumnInfoByMark(String marks) {
        try {
            if (StringUtils.isNotEmpty(marks)) {
                return customColumnService.findCustomColumnInfoByMark(marks);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("查询用户自定义列失败");
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/deleteUserMarkColumn", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteUserMarkColumn(String columnId, String mark) {
        return AdasControllerTemplate
            .getResultBean(() -> customColumnService.deleteUserMarkColumn(columnId, mark), "删除用户模块定制列异常");
    }

}
