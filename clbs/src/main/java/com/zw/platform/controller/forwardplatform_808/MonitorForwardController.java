package com.zw.platform.controller.forwardplatform_808;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfig;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfigView;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.service.thirdplatform.ThirdPlatFormService;
import com.zw.platform.util.GetIpAddr;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * 监控对象转发 @author  Tdz
 **/
@Controller
@RequestMapping("/m/forwardplatform/mf")
public class MonitorForwardController {
    private static final Logger log = LogManager.getLogger(MonitorForwardController.class);

    private static final String LIST_PAGE = "modules/forwardplatform/mf/list";

    private static final String EDIT_PAGE = "modules/forwardplatform/mf/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private ThirdPlatFormService service;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/thirdmVehicelList", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean getThirdVehicleList() {
        try {
            JSONObject msg = new JSONObject();
            List<VehicleInfo> list = service.getVehicleList();
            msg.put("list", list);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("监控对象转发管理查询监控对象信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean savePage(final ThirdPlatFormConfig config) {
        try {
            if (config != null) {
                service.updateConfigById(config);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改监控对象转发信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = {"/edit_{configId}"}, method = RequestMethod.GET)
    @Deprecated
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("configId") String configId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            ThirdPlatFormConfigView form = service.findConfigViewByConfigId(configId);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("修改监控对象转发信息界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }

    }

    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                List<String> ids = Collections.singletonList(id);
                // 获取访问服务器的客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.deleteConfigById(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("解除监控对象转发管理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 批量删除
     * @return JsonResultBean
     * @author Liubangquan
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                // 获取访问服务器的客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.deleteConfigById(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("监控对象转发管理页面批量解除监控对象转发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @RequestMapping(value = {"/platformListUrl"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean platformListUrl() {
        try {
            IntercomPlatFormQuery query = new IntercomPlatFormQuery();
            JSONObject msg = new JSONObject();
            msg.put("platformList", service.findList(query, false));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("监控对象转发管理页面查询数据异常(findList)", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @RequestMapping(value = {"/addConfig"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addConfig(String thirdPlatformId, String vehicleIds) {
        try {
            if (thirdPlatformId != null && vehicleIds != null && !thirdPlatformId.isEmpty()
                 && !vehicleIds.isEmpty()) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                service.addConfig(thirdPlatformId, vehicleIds, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("监控对象转发管理页面增加监控对象转发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 查询角色(分页)
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(IntercomPlatFormConfigQuery query) {
        Page<ThirdPlatFormConfigView> formList = service.findConfigViewList(query);
        return new PageGridBean(query, formList, true);
    }

}
