package com.zw.api2.controller;

import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerRodSensorForm;
import com.zw.api2.swaggerEntity.SwaggerRodSensorUpdateForm;
import com.zw.api2.swaggerEntity.SwaggerSimpleRodSensorQuery;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.query.RodSensorQuery;
import com.zw.platform.service.basicinfo.RodSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Created by Tdz on 2016/7/20.
 */
@RestController
@RequestMapping("/api/m/basicinfo/equipment/rodsensor")
@Api(tags = {"???????????????????????????????????????dev"}, description = "?????????????????????api")
public class ApiRodSensorController {

    private static final String ADD_PAGE = "modules/basicinfo/equipment/rodsensor/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/rodsensor/edit";

    @Autowired
    private RodSensorService rodSensorService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${add.fail}")
    private String addFail;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${set.fail}")
    private String setFail;

    private static Logger logger = LogManager.getLogger(ApiRodSensorController.class);

    /**
     * ????????????
     */
    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(
        @ModelAttribute("swaggerSimpleRodSensorQuery") final SwaggerSimpleRodSensorQuery swaggerSimpleRodSensorQuery) {
        RodSensorQuery query = new RodSensorQuery();

        BeanUtils.copyProperties(swaggerSimpleRodSensorQuery, query);
        try {
            Page<RodSensor> result = rodSensorService.findByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            logger.error("???????????????findByPage?????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ??????
     */
    @ApiIgnore
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * ??????
     */
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ValidGroupAdd.class})
                                    @ModelAttribute("form") final SwaggerRodSensorForm swaggerRodSensorForm) {
        RodSensorForm form = new RodSensorForm();
        try {
            BeanUtils.copyProperties(swaggerRodSensorForm, form);
            String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
            return rodSensorService.add(form, ipAddress);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ??????id?????????id????????? Personnl
     */
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") @ApiParam("??????id") final String id) {
        try {
            if (id != null && !"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
                return rodSensorService.delete(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????
     */
    @ApiOperation(value = "??????id???????????????????????????", notes = "??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ResponseBody
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable("id") @ApiParam("???????????????id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", rodSensorService.get(id));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            logger.error("?????????????????????????????????????????????", e);
            return new JsonResultBean(syError);
        }
    }

    /**
     * ??????
     */
    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class })
        @ModelAttribute("form") final SwaggerRodSensorUpdateForm swaggerRodSensorUpdateForm) {
        RodSensorForm form = new RodSensorForm();
        BeanUtils.copyProperties(swaggerRodSensorUpdateForm, form);
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);// ??????????????????IP??????
            return rodSensorService.update(form, ipAddress);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????ids???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "deltems", value = "??????????????????????????????ids(???????????????)",
        required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return rodSensorService.delete(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ??????excel???
     *
     * @throws UnsupportedEncodingException
     */
    @ApiIgnore
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "?????????????????????");
            rodSensorService.exportInfo(null, 1, response);
        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // ????????????IP??????
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = rodSensorService.importSensor(file, request, ipAddress);
            String msg = "???????????????" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }

    /**
     * ????????????
     *
     * @throws UnsupportedEncodingException
     */
    @ApiIgnore
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "???????????????????????????");
            rodSensorService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("???????????????????????????????????????", e);
        }
    }

    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("sensorNumber") String sensorNumber) {
        try {
            RodSensor vt = rodSensorService.findByRodSensor(sensorNumber);
            return vt == null;
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
            return false;
        }
    }
}
