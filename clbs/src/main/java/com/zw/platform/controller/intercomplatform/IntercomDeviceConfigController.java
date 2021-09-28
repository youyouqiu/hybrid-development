package com.zw.platform.controller.intercomplatform;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfig;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigView;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.intercomplatform.IntercomPlatFormService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 连接参数设置
 *
 * @author  Fan Lu
 * @create 2017-03-15 17:20
 **/
@Controller
@RequestMapping("/m/intercomplatform/deviceconfig")
public class IntercomDeviceConfigController {
    @Autowired
    private IntercomPlatFormService service;

    @Autowired
    private UserService userService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private ConfigService configService;

    private static final String LIST_PAGE = "modules/intercomplatform/mf/list";

    private static final String EDIT_PAGE = "modules/intercomplatform/mf/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static Logger log = LogManager.getLogger(IntercomDeviceConfigController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/platformListUrl"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean platformListUrl() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("platformList", service.findList(new IntercomPlatFormQuery(), false));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("对讲设备配置页面分页查询对讲对讲地址信息(findList)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = {"/edit_{configId}"}, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("configId") String configId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            IntercomPlatFormConfigView form = service.findConfigViewByConfigId(configId);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("修改对讲设备配置界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            List<String> configIds = service.findConFigIdByPIds(Arrays.asList(id));
            service.deleteConfigById(id);
            List<ConfigDetailsQuery> list = configService.getConfigByConfigIds(configIds);
            JSONObject msg = new JSONObject();
            msg.put("list", list);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("对讲设备配置页面删除对讲配置信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @RequestMapping(value = {"/addConfig"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addConfig(String intercomPlatformId, String ids) {
        try {
            List<String> configIds = service.findConFigIdByVIds(Arrays.asList(ids.split(",")));
            IntercomPlatFormConfig config = new IntercomPlatFormConfig();
            config.setCreateDataTime(new Date());
            config.setIntercomPlatformId(intercomPlatformId);
            config.setCreateDataUsername(SystemHelper.getCurrentUsername());
            config.setFlag(1);
            for (String configId : configIds) {
                config.setId(UUID.randomUUID().toString());
                config.setConfigId(configId);
                service.addConfig(config);
            }
            List<ConfigDetailsQuery> list = configService.getConfigByConfigIds(configIds);
            JSONObject msg = new JSONObject();
            msg.put("list", list);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("对讲设备配置页面增加监控对象和对讲平台绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean savePage(final IntercomPlatFormConfig config) {
        try {
            service.updateConfigById(config);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("对讲设备配置页面修改对讲设备配置信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     *
     * @param request
     * @return JsonResultBean
     * @throws BusinessException
     * @throws
     * @Title: deleteMore
     * @author Liubangquan
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            List<String> configIds = service.findConFigIdByPIds(Arrays.asList(item));
            for (int i = 0; i < item.length; i++) {
                service.deleteConfigById(item[i]);
            }
            List<ConfigDetailsQuery> list = configService.getConfigByConfigIds(configIds);
            JSONObject msg = new JSONObject();
            msg.put("list", list);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("批量删除信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/getUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getUser() throws BusinessException {
        JSONObject msg = new JSONObject();
        UserLdap user = (UserLdap) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserBean currentUser = userService.findUser(user.getId().toString());

        String pwd = currentUser.getPassword();
        String[] bt = pwd.split(",");
        byte[] bb = new byte[bt.length];
        for (int i = 0; i < bt.length; i++) {
            int u = Integer.parseInt(bt[i]);
            bb[i] = (byte) u;
        }
        pwd = new String(bb);
        msg.put("pwd", pwd);
        msg.put("userName", currentUser.getUsername());
        msg.put("mobile", currentUser.getMobile());
        msg.put("email", currentUser.getMail() == null ? "" : currentUser.getMail() == null);
        msg.put("remarks", "");
        msg.put("organizationname", userService.findOrganization(userService.getOrgIdByUser()).getName());
        try {
            msg.put("uporganizationanme", userService.findOrganization(userService.getParentOrgIdByUser()).getName());
        } catch (Exception e) {
            msg.put("uporganizationanme", "");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
    }

    /**
     * 查询（分页）
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(IntercomPlatFormConfigQuery query) throws BusinessException {
        try {
            Page<IntercomPlatFormConfigView> formList = service.findConfigViewList(query);
            return new PageGridBean(query, formList, true);
        } catch (Exception e) {
            log.error("分页查询信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/intercomDeviceVehicelTree", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTree(String type) {
        try {
            JSONArray jsonResult = service.getVehicleTreeByPlatform();
            return jsonResult.toJSONString();
        } catch (Exception e) {
            log.error("获取车辆树异常", e);
            return null;
        }
    }

    @RequestMapping(value = "/thirdmVehicelTree", method = RequestMethod.POST)
    @ResponseBody
    public String getThirdVehicleTree(String type) {
        try {
            JSONArray jsonResult = service.getVehicleTreeByThirdPlatform();
            return jsonResult.toJSONString();
        } catch (Exception e) {
            log.error("获取车辆树异常", e);
            return null;
        }
    }
}
