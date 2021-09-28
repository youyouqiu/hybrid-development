package com.zw.api2.controller.apiInfoFastInput;

import com.alibaba.fastjson.JSONObject;
import com.zw.api2.swaggerEntity.SwaggerConfigForm;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.basicinfo.DeviceService;
import com.zw.platform.service.basicinfo.SimcardService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.infoconfig.InfoFastInputService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.topspeed.TopSpeedService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ValidGroupAdd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("api/m/infoconfig/infoFastInput")
@Api(tags = { "快速录入_dev" }, description = "快速录入相关api接口")
public class ApiInfoFastInputController {
    private static Logger log = LogManager.getLogger(ApiInfoFastInputController.class);

    private static final String ADD_PAGE = "modules/infoconfig/infoFastInput/add";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private InfoFastInputService infoFastInputService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SimcardService simcardService;

    @Autowired
    private TopSpeedService topSpeedService;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 信息配置-快速录入界面
     */
    @ApiIgnore
    @Auth
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String getPage() {
        return ADD_PAGE;
    }

    /**
     * 根据组织信息获得组织ID
     * @return JsonResultBean
     * @throws @Title: getGroupIDfoByGroupName
     * @author wangjanyu
     */
    @ApiIgnore
    @RequestMapping(value = { "/getGroupIDfoByGroupName" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupIDfoByGroupName(String groupID) {
        try {
            JSONObject msg = new JSONObject();
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("根据组织信息获得组织ID异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 保存提交的信息
     * @return JsonResultBean
     * @throws @Title: submits
     * @author wangjianyu
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
        @ApiImplicitParam(name = "sims", value = "终端手机号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceType", value = "终端类型，（类型： 1 ：交通部JTB808；2：移为GV320；3：天禾）", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "监控对象类型（0：车，1：人，2：物）", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupid", value = "分组名字", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/submits" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("swaggerForm") final SwaggerConfigForm swaggerForm,
        final BindingResult bindingResult) {
        try {
            Config1Form form = new Config1Form();
            BeanUtils.copyProperties(swaggerForm, form);
            boolean isBound = checkConfigIsBound(form);
            if (!isBound) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                // infoFastInputService.addConfig(form, ip, 1, request);
                // 维护绑定信息
                configService.addBindInfo(form.getDeviceID(), form);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("快速录入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 极速录入
     * @return JsonResultBean
     * @throws @Title: submits
     * @author
     */
    @ApiOperation(value = "保存极速录入数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "brandID", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceID", value = "终端id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "simID", value = "SIM卡id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "citySelID", value = "分组id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "brands", value = "车牌号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "devices", value = "终端号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sims", value = "SIM卡号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceType", value = "终端类型，（类型： 1 ：交通部JTB808；2：移为GV320；3：天禾）", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "监控对象类型（0：车，1：人，2：物）", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupid", value = "分组名字", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/saveTopspeed" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveTopspeed(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("swaggerForm") final SwaggerConfigForm swaggerForm,
        final BindingResult bindingResult, HttpServletRequest request) {
        try {
            Config1Form form = new Config1Form();
            BeanUtils.copyProperties(swaggerForm, form);
            boolean isBound = checkConfigIsBound(form);
            if (!isBound) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                // infoFastInputService.addConfig(form, ip, 2, request);
                // 维护绑定信息
                configService.addBindInfo(form.getDeviceID(), form);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("极速录入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 扫码录入
     * @return JsonResultBean
     * @throws @Title: submits
     * @author
     */
    @ApiOperation(value = "保存扫码录入数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "brandID", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceID", value = "终端id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "simID", value = "SIM卡id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "citySelID", value = "分组id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "brands", value = "车牌号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "devices", value = "终端号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sims", value = "SIM卡号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceType", value = "终端类型，（类型： 1 ：交通部JTB808；2：移为GV320；3：天禾）", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "监控对象类型（0：车，1：人，2：物）", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupid", value = "分组名字", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/scanningRecord" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean scanningRecord(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("swaggerForm") final SwaggerConfigForm swaggerForm,
        final BindingResult bindingResult, HttpServletRequest request) {
        try {
            Config1Form form = new Config1Form();
            BeanUtils.copyProperties(swaggerForm, form);
            boolean isBound = checkConfigIsBound(form);
            if (!isBound) {
                if ("0".equals(form.getDeviceType()) || "1".equals(form.getDeviceType())) {
                    String devices = form.getDevices();
                    form.setDevices(devices.substring(devices.length() - 7, devices.length()));
                }
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                // infoFastInputService.addConfig(form, ip, 3, request);
                // 维护绑定信息
                configService.addBindInfo(form.getDeviceID(), form);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("扫码录入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 生成随机监控对象编号
     * @return JsonResultBean
     * @throws @Title: getRandomNumbers
     * @author hujun
     */
    @ApiOperation(value = "生成随机监控对象编号(扫码录入时)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "sim", value = "sim卡号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "监控对象类型（0:车，1:人，2:物）", required = true, paramType = "query",
            dataType = "Integer") })
    @RequestMapping(value = { "/getRandomNumbers" }, method = RequestMethod.POST)
    @ResponseBody
    public String getRandomNumbers(String sim, int monitorType) {
        try {
            return infoFastInputService.getRandomNumbers(sim, monitorType);
        } catch (Exception e) {
            log.error("生成随机监控对象编号异常", e);
            return "-1";
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
            if (!Converter.toBlank(form.getBrandID()).equals("") || !Converter.toBlank(form.getDeviceID()).equals("")
                || !Converter.toBlank(form.getSimID()).equals("")) {
                ConfigForm cf = configService
                    .getIsBand(Converter.toBlank(form.getBrandID()), Converter.toBlank(form.getDeviceID()),
                        Converter.toBlank(form.getSimID()), "");
                if (null != cf) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("验证车辆、终端、sim卡是否已经被绑定", e);
            return false;
        }
        return isBound;
    }
}

