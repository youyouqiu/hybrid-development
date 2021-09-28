package com.zw.platform.controller.intercomplatform;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.intercomplatform.IntercomPlatForm;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.service.intercomplatform.IntercomPlatFormService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
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
import java.util.List;

/**
 * 连接参数设置
 *
 * @author  Fan Lu
 * @create 2017-03-15 17:18
 **/
@Controller
@RequestMapping("/m/intercomplatform/addrconfig")
public class IntercomAddrConfigController {
    @Autowired
    private IntercomPlatFormService service;

    @Autowired
    private HttpServletRequest request;

    private static Logger log = LogManager.getLogger(IntercomAddrConfigController.class);

    private static final String LIST_PAGE = "modules/intercomplatform/ipmgt/list";

    private static final String ADD_PAGE = "modules/intercomplatform/ipmgt/add";

    private static final String EDIT_PAGE = "modules/intercomplatform/ipmgt/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(IntercomPlatFormQuery query) {
        try {
            List<IntercomPlatForm> formList = service.findList(query, true);
            return new PageGridBean(query, (Page<IntercomPlatForm>) formList, true);
        } catch (Exception e) {
            log.error("对讲地址配置页面分页查询(findList)异常", e);
            return new PageGridBean(false);
        }
    }

    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() {
        try {
            return ADD_PAGE;
        } catch (Exception e) {
            log.error("弹出新增对讲地址界面异常", e);
            return ERROR_PAGE;
        }
    }

    /**
     * 批量删除
     *
     * @return JsonResultBean
     * @throws BusinessException
     * @throws
     * @Title: deleteMore
     * @author Liubangquan
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(";");
                List<String> ids = Arrays.asList(item);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.deleteById(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除对讲地址信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = {"/edit_{platFormId}"}, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("platFormId") String platFormId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            IntercomPlatForm form = service.findById(platFormId);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("弹出修改对讲信息界面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                List<String> ids = Arrays.asList(id);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.deleteById(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除对讲地址信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean savePage(final IntercomPlatForm form) {
        try {
            if (form != null) {
                // 获取客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.update(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改对讲地址信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = {"/addPlant"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addPlant(final IntercomPlatForm form) {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.add(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增对讲地址信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
