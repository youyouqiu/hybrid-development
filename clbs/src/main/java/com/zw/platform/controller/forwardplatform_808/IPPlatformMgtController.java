package com.zw.platform.controller.forwardplatform_808;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.forwardplatform.ThirdPlatForm;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.service.thirdplatform.ThirdPlatFormService;
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
import org.springframework.ui.ModelMap;
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
 * 转发平台IP管理 @author  Tdz
 *
 * @create 2017-02-16 13:42
 **/
@Controller
@RequestMapping("/m/forwardplatform/ipmgt")
public class IPPlatformMgtController {
    @Autowired
    private ThirdPlatFormService service;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private HttpServletRequest request;

    private static Logger log = LogManager.getLogger(IPPlatformMgtController.class);

    private static final String LIST_PAGE = "modules/forwardplatform/ipmgt/list";

    private static final String ADD_PAGE = "modules/forwardplatform/ipmgt/add";

    private static final String EDIT_PAGE = "modules/forwardplatform/ipmgt/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(IntercomPlatFormQuery query) {
        try {
            List<ThirdPlatForm> formList = service.findList(query, true);
            return new PageGridBean(query, (Page<ThirdPlatForm>) formList, true);
        } catch (Exception e) {
            log.error("分页查询转发平台信息异常(findList)", e);
            return new PageGridBean(false);
        }

    }

    /**
     * 批量删除
     *
     * @param request
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: deleteMore
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                String[] item = items.split(";");
                List<String> ids = Arrays.asList(item);
                // 访问服务器的客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                service.deleteById(ids, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除转发平台IP信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = {"/edit_{platFormId}"}, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("platFormId") String platFormId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            ThirdPlatForm form = service.findById(platFormId);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("弹出修改转发平台信息界面时发生异常", e);
            return new ModelAndView(ERROR_PAGE);
        }

    }

    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                List<String> ids = Collections.singletonList(id);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.deleteById(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("转发平台IP管理页面删除转发平台信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean savePage(final ThirdPlatForm form, ModelMap model) {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return service.update(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("转发平台IP管理页面修改转发平台信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() throws BusinessException {
        return ADD_PAGE;
    }

    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = {"/addPlant"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addPlant(final ThirdPlatForm form) {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // 访问服务器的客户端的IP地址
                return service.add(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("转发平台IP管理新增转发平台信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 校验808平台名称是否唯一
     *
     * @param platFormName
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月12日 下午3:11:59
     */
    @RequestMapping(value = {"/check808PlatFormSole"}, method = RequestMethod.POST)
    @ResponseBody
    public boolean check808PlatFormSole(String platFormName, String pid) {
        try {
            return service.check808PlatFormSole(platFormName, pid);
        } catch (Exception e) {
            return false;
        }
    }

}
