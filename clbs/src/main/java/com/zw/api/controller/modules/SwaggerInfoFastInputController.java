package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.infoconfig.InfoFastInputService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/swagger/m/infoFastInput")
@Api(tags = { "快速录入" }, description = "快速录入相关api接口")
public class SwaggerInfoFastInputController {
    private static Logger log = LogManager.getLogger(SwaggerInfoFastInputController.class);

    @Autowired
    private InfoFastInputService infoFastInputService;

    @Autowired
    private ConfigService configService;

    /**
     * 信息配置-快速录入界面
     */
    @Auth

    @ApiOperation(value = "分别获取可选车辆，终端，SIM卡数组list", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean pageBean() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("vehicleInfoList", infoFastInputService.getVehicleInfoList());
            /*
             * msg.put("peopleInfoList", infoFastInputService.getPeopleInfoList()); msg.put("ThingInfoList",
             * infoFastInputService.getThingInfoList());
             */
            msg.put("deviceInfoList", infoFastInputService.getdeviceInfoList());
            msg.put("simcardInfoList", infoFastInputService.getSimcardInfoList());
            // msg.put("groupList", infoFastInputService.getgetGroupList());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("快速录入界面弹出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据组id获取组织信息
     * @return JsonResultBean
     * @throws @author wangjianyu
     * @Title: submits
     */
    @ApiOperation(value = "保存快速录入数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "brandID", value = "车辆id,若是新增车辆，则不填", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceID", value = "终端id,若是新增终端，则不填", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "simID", value = "SIM卡id,若是新增SIM卡，则不填", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "citySelID", value = "分组id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "brands", value = "车牌号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "devices", value = "终端号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sims", value = "SIM卡号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceType", value = "终端类型，（类型： 1 ：交通部JTB808；2：移为GV320；3：天禾）（若是新增终端，该变量值必填）",
            required = false, paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/submits" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) final Config1Form form,
        final BindingResult bindingResult, HttpServletRequest request) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                boolean isBound = checkConfigIsBound(form);
                if (!isBound) {
                    String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                    // infoFastInputService.addConfig(form, ip, 1, request);
                    // 维护极速录入列表
                    configService.addBindInfo(form.getDeviceID(), form);
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("保存快速录入数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存提交时，验证车辆、终端、sim卡是否已经被绑定，避免两个用户同时操作一条数据引起冲突
     * @param form
     * @return boolean
     * @Title: checkConfigIsBound
     * @author fanlu
     */
    private boolean checkConfigIsBound(Config1Form form) {
        boolean isBound = false;
        try {
            if (!"".equals(Converter.toBlank(form.getBrandID())) || !"".equals(Converter.toBlank(form.getDeviceID()))
                || !"".equals(Converter.toBlank(form.getSimID()))) {
                ConfigForm cf =
                    configService.getIsBand(Converter.toBlank(form.getBrandID()), Converter.toBlank(form.getDeviceID()),
                        Converter.toBlank(form.getSimID()), "");
                return null != cf;
            }
        } catch (Exception e) {
            log.error("验证车辆、终端、sim卡是否已经被绑定", e);
            return false;
        }
        return isBound;
    }
}
