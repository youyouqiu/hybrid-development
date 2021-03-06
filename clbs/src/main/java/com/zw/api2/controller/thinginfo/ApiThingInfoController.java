package com.zw.api2.controller.thinginfo;

import com.alibaba.fastjson.JSONObject;
import com.zw.api2.swaggerEntity.SwaggerThingInfoForm;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ????????????Controller Modification by Wjy on 2016/7/26.
 */
@Controller
@RequestMapping("/api/basicinfo/monitoring/ThingInfo")
@Api(tags = { "????????????_dev" }, description = "??????????????????api")
public class ApiThingInfoController {
    private static Logger log = LogManager.getLogger(ApiThingInfoController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/ThingInfo/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/ThingInfo/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/ThingInfo/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/ThingInfo/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    @Value("${sys.error.msg}")
    private String syError;

    @Value("${vehicle.brand.bound}")
    private String vehicleBrandBound;

    @Autowired
    private ConfigService configService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ApiIgnore
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * ????????????
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @ApiIgnore
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * ????????????
     */
    @Auth
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final SwaggerThingInfoForm formData,
        final BindingResult bindingResult) {
        //??????????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }

        //????????????
        ThingInfoForm form = new ThingInfoForm();
        BeanUtils.copyProperties(formData, form);

        //????????????
        try {
            // ThingInfo vt = thingInfoService.findByThingInfo(form.getThingNumber());
            // if (vt != null) {
            //     return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????");
            // }
            // thingInfoService.add(form, new GetIpAddr().getIpAddr(request));
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????ID??????ThingInfo
     */

    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable("id") @ApiParam("??????id") final String id) {
        try {
            Map<String, Object> mav = new HashMap<>();
            // ThingInfo thingInfo = thingInfoService.get(id);
            // thingInfo.setProductDate(
            // StringUtils.isNotEmpty(thingInfo.getProductDate()) ? thingInfo.getProductDate().split(" ")[0] : null);
            // mav.put("result", thingInfo);
            // OrganizationLdap organization = userService.getOrgByUuid(thingInfo.getGroupId());
            mav.put("groupName", "organization.getName()");
            ConfigForm cfg = configService.getIsBand(id, "", "", "");
            mav.put("bandState", cfg == null ? false : true);
            return new JsonResultBean(mav);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(syError);
        }
    }

    /**
     * ??????ThingInfo
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody

    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerThingInfoForm formData,
        final BindingResult bindingResult) {
        //??????????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }

        //????????????
        ThingInfoForm form = new ThingInfoForm();
        BeanUtils.copyProperties(formData, form);

        //????????????
        try {
            String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
            // return thingInfoService.update(form, ip);
            return null;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     */
    @Auth
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "delIds", value = "??????id,???????????????????????????",
         required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(@RequestParam(value = "delIds") String delIds) {
        try {
            String[] item = delIds.split(",");
            // ????????????????????????????????????????????????????????????
            StringBuilder boundThingNumbersBuilder = new StringBuilder();
            // ?????????????????????id??? ?????????????????????
            StringBuilder boundThingIdsBuilder = new StringBuilder();
            // ?????????????????????????????????id
            StringBuilder notBoundThingIdsBuilder = new StringBuilder();
            ConfigForm c;
            for (String anItem : item) {
                c = configService.getIsBand(anItem, "", "", "");
                if (c != null) {
                    boundThingNumbersBuilder.append(c.getBrands() + " ,");
                    boundThingIdsBuilder.append(anItem + ",");
                } else {
                    notBoundThingIdsBuilder.append(anItem + ",");
                }
            }
            // ???????????????????????????????????????
            if (notBoundThingIdsBuilder.length() > 0) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                String notBoundThingIds = Converter.removeStringLastChar(notBoundThingIdsBuilder.toString());
                // thingInfoService.deleteBatch(notBoundThingIds.split(","), ip);
            }
            // ?????????????????????????????????????????????????????????
            JSONObject msg = new JSONObject();
            if (boundThingNumbersBuilder.length() > 0) {
                String boundThingNumbers = Converter.removeStringLastChar(boundThingNumbersBuilder.toString());
                String boundThingIds = Converter.removeStringLastChar(boundThingIdsBuilder.toString());
                msg.put("boundThingNumbers", boundThingNumbers);
                msg.put("boundThingIds", boundThingIds);
                msg.put("infoMsg", vehicleBrandBound);
            } else {
                msg.put("boundThingNumbers", "");
                msg.put("boundBrandIds", "");
                msg.put("infoMsg", "");
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????id?????? ThingInfo
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") @ApiParam("??????id") final String id) {
        try {
            if (id != null) {
                JSONObject msg = new JSONObject();
                msg.put("thingId", id);
                ConfigForm c = configService.getIsBand(id, "", "", "");
                if (c != null) {
                    msg.put("infoMsg", vehicleBrandBound);
                    return new JsonResultBean(msg);
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                    // thingInfoService.delete(id, ip);
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    @ApiIgnore
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    @ApiIgnore
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // ??????????????????IP??????
            // return thingInfoService.importThingInfo(file, ipAddress);
            return null;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ApiIgnore
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            // thingInfoService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    @ApiOperation(value = "????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "thingNumber", value = "????????????",
         required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "id", value = "?????????uuid", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("thingNumber") String thingNumber, @RequestParam("id") String id) {
        try {
            // ThingInfo vt = thingInfoService.findByThingInfo(id, thingNumber);
            ThingInfo vt = null;
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return false;
        }
    }

    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "thingNumber", value = "????????????",
         required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/checkThingNumberSole", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkThingNumberSole(String thingNumber) {
        try {
            // return thingInfoService.checkThingNumberSole(thingNumber);
            return false;
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return false;
        }
    }
}
