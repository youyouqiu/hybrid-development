package com.zw.talkback.controller.intercomplatform;

import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.form.RoleForm;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.talkback.common.ControllerTemplate;
import com.zw.talkback.service.baseinfo.IntercomCallNumberService;
import com.zw.talkback.service.intercom.IntercomInitService;
import com.zw.talkback.util.TalkCallUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/***
 @Author zhengjc
 @Date 2019/8/6 9:54
 @Description 测试方法，后续会删除掉
 @version 1.0
 **/
@Controller
@RequestMapping("/m/intercomplatform")
public class IntercomInitController {
    @Value("${sys.error.msg}")
    private String sysErrorMsg;
    @Autowired
    private IntercomCallNumberService intercomCallNumberService;

    @Autowired
    private IntercomInitService intercomInitService;

    @Autowired
    private TalkCallUtil talkCallUtils;

    @RequestMapping(value = { "/testInit" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testInit() {
        return ControllerTemplate
            .getResultBean(() -> intercomCallNumberService.addAndInitCallNumberToRedis(), "初始化组呼和个呼号码方法异常");
    }

    @RequestMapping(value = { "/testPopPersonNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testPopPersonNumber() {
        return ControllerTemplate
            .getResultBean(() -> intercomCallNumberService.updateAndReturnPersonCallNumber(), "弹出个呼号码异常");

    }

    @RequestMapping(value = { "/testPopGroupNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testPopGroupNumber() {
        return ControllerTemplate
            .getResultBean(() -> intercomCallNumberService.updateAndReturnGroupCallNumber(), "弹出组呼号码");

    }

    @RequestMapping(value = { "/testRecycleGroupNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testRecycleGroupNumber(String callNumber) {
        return ControllerTemplate
            .getResultBean(() -> intercomCallNumberService.updateAndRecycleGroupCallNumber(callNumber), "回收组呼号码异常");
    }

    @RequestMapping(value = { "/testRecyclePersonNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testRecyclePersonNumber(String callNumber) {
        return ControllerTemplate
            .getResultBean(() -> intercomCallNumberService.updateAndRecyclePersonCallNumber(callNumber), "回收个呼号码异常");

    }

    @RequestMapping(value = { "/testUpdateTopOrgName" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testUpdateTopOrgName(String name, HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> intercomInitService.updateTopOrgName(name, request), "初始化企业和顶级企业名称失败");

    }

    @RequestMapping(value = { "/testAddRole" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testAddRole(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final RoleForm form,
        @RequestParam("permissionTree") String permissionTree, HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> intercomInitService.addRole(form, permissionTree, IPAddrUtil.getClientIp(request)),
                "新增角色信息异常");

    }

    /**
     * 新增代理商和一级用户
     * @param user
     * @param request
     * @return
     */
    @RequestMapping(value = { "/testAddUser" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean testAddUser(UserBean user, HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> intercomInitService.addUser(user, IPAddrUtil.getClientIp(request)), "初始化代理商和组呼号码异常");
    }

    @RequestMapping(value = { "/getFirstCustomerId" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFirstCustomerId() {
        return ControllerTemplate.getResultBean(() -> talkCallUtils.getFirstCustomerPid(), "获取一级客户pid");
    }

    @RequestMapping(value = { "/getFirstCustomerInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFirstCustomerInfo() {
        return ControllerTemplate.getResultBean(() -> talkCallUtils.getFirstCustomerInfo(), "获取一级客户信息");
    }

    @RequestMapping(value = { "/getNewFirstCustomerId" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getNewFirstCustomerPid() {
        return ControllerTemplate.getResultBean(() -> talkCallUtils.getNewFirstCustomerPid(), "获取最新的一级客户pid");
    }

    @RequestMapping(value = { "/getNewFirstCustomerInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getNewFirstCustomerInfo() {
        return ControllerTemplate.getResultBean(() -> talkCallUtils.getNewFirstCustomerInfo(), "获取最新的一级客户信息");
    }

    @RequestMapping(value = { "/getDeviceType" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceType(String modelName, Integer pageSize, Integer pageIndex) {
        return ControllerTemplate
            .getResultBean(() -> talkCallUtils.getDeviceTypePageData(modelName, pageSize, pageIndex), "获取终端类型");
    }

}
