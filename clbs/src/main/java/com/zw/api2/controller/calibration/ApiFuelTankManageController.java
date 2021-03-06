package com.zw.api2.controller.calibration;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerCalCalibration;
import com.zw.api2.swaggerEntity.SwaggerDoubleOilVehicleSetting;
import com.zw.api2.swaggerEntity.SwaggerEditFuelTankForm;
import com.zw.api2.swaggerEntity.SwaggerFuelTankForm;
import com.zw.api2.swaggerEntity.SwaggerPageParamQuery;
import com.zw.api2.swaggerEntity.SwaggerTheoryFuelTank;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.FuelTankQuery;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.OilMassMgtUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * ????????????Controller <p>Title: fuelTankManageController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016???10???24?????????4:24:19
 * @version 1.0
 */
@Controller
@RequestMapping("api/v/oilmassmgt/fueltankmgt")
@Api(tags = {"????????????_dev"}, description = "????????????api??????")
public class ApiFuelTankManageController implements ServletContextAware {
    private static Logger log = LogManager.getLogger(ApiFuelTankManageController.class);

    private static final String DETAIL_PAGE = "vas/oilmassmgt/fueltankmanage/detail";

    private static final String EDIT_PAGE = "vas/oilmassmgt/fueltankmanage/edit";

    private String oilLevelHeights = "";

    private String oilValues = "";

    private String oilLevelHeights2 = "";

    private String oilValues2 = "";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${delete.fuel.tank}")
    private String deleteFuelTank;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${add.fail}")
    private String addFail;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${set.fail}")
    private String setFail;

    // Spring?????????????????????ServletContextAware???????????????ServletContext??????
    private ServletContext servletContext;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private HttpServletRequest request;

    /**
     * ??????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "simpleQueryParam", value = "????????????????????????????????????", required = false,
        paramType = "query", dataType = "string")
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(@ModelAttribute("query") SwaggerPageParamQuery query) {
        final FuelTankQuery query1 = new FuelTankQuery();
        BeanUtils.copyProperties(query, query1);
        try {
            Page<FuelTankForm> result = fuelTankManageService.findFuelTankByPage(query1);
            return new PageGridBean(query1, result, true);
        } catch (Exception e) {
            log.error("?????????????????????findFuelTankByPage?????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ??????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addFuelTank(@ModelAttribute("form")SwaggerFuelTankForm form) {
        FuelTankForm form1 = new FuelTankForm();
        BeanUtils.copyProperties(form, form1);
        try {
            String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
            return fuelTankManageService.addFuelTank(form1, ip);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "boxHeight", value = "????????????", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = {"/sensorList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initSensorList(String boxHeight) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("sensorList", fuelTankManageService.findRodSensorList(boxHeight));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????id???????????????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "???????????????id???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "sensorId", value = "?????????id", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = {"/sensorDetail"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorDetail(String sensorId) {
        try {
            JSONObject msg = new JSONObject();
            List<RodSensorForm> list = fuelTankManageService.getSensorDetail(sensorId);
            if (list == null || list.isEmpty()) {
                msg.put("sensorDetail", null);
                return new JsonResultBean(msg);
            }
            msg.put("sensorDetail", list.get(0));
            int oddEvenCheck = list.get(0).getOddEvenCheck() == null ? 3 : list.get(0).getOddEvenCheck();
            int compensationCanMake =
                list.get(0).getCompensationCanMake() == null ? 2 : list.get(0).getCompensationCanMake();
            String filteringFactor = list.get(0).getFilteringFactor();
            String baudRate = list.get(0).getBaudRate();
            if (oddEvenCheck == 1) {
                list.get(0).setOddEvenCheckStr("?????????");
            } else if (oddEvenCheck == 2) {
                list.get(0).setOddEvenCheckStr("?????????");
            } else {
                list.get(0).setOddEvenCheckStr("?????????");
            }
            if (compensationCanMake == 1) {
                list.get(0).setCompensationCanMakeStr("??????");
            } else {
                list.get(0).setCompensationCanMakeStr("??????");
            }
            if ("01".equals(filteringFactor)) {
                list.get(0).setFilteringFactorStr("??????");
            } else if ("02".equals(filteringFactor)) {
                list.get(0).setFilteringFactorStr("??????");
            } else {
                list.get(0).setFilteringFactorStr("??????");
            }

            switch (baudRate) {
                case "01":
                    list.get(0).setBaudRateStr("2400");
                    break;
                case "02":
                    list.get(0).setBaudRateStr("4800");
                    break;
                case "04":
                    list.get(0).setBaudRateStr("19200");
                    break;
                case "05":
                    list.get(0).setBaudRateStr("38400");
                    break;
                case "06":
                    list.get(0).setBaudRateStr("57600");
                    break;
                case "07":
                    list.get(0).setBaudRateStr("115200");
                    break;
                case "03":
                default:
                    list.get(0).setBaudRateStr("9600");
                    break;
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "??????id????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/delete_{id}"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id) {
        try {
            if (!"".equals(id)) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return fuelTankManageService.deleteFuelTankById(id, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "??????ids??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "deltems", value = "????????????id??????String(???????????????)", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return fuelTankManageService.deleteBatchFuelTankById(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "??????id????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})

    @RequestMapping(value = {"/fuelTankDetail_{id}.gsp"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detail(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            List<FuelTankForm> list = fuelTankManageService.getFuelTankDetail(id);
            if (null != list && !list.isEmpty()) {
                list.get(0).setShapeStr(fuelTankManageService.getOilBoxShapeStr(list.get(0).getShape()));
                mav.addObject("result", list.get(0));

            } else {
                mav.addObject("result", null);
            }
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????");
        }
    }

    /**
     * ????????????-????????????
     * @author Liubangquan
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            List<FuelTankForm> list = fuelTankManageService.getFuelTankDetail(id);
            if (null != list && !list.isEmpty()) {
                mav.addObject("result", list.get(0));
            } else {
                mav.addObject("result", null);
            }
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????");
        }
    }

    /**
     * ????????????-??????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public JsonResultBean edit(@ModelAttribute("form") SwaggerEditFuelTankForm form) {
        try {
            final FuelTankForm form1 = new FuelTankForm();
            BeanUtils.copyProperties(form, form1);
            String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
            return fuelTankManageService.updateFuelTank(form1, ip);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????
     * @param fuelTank ???????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "??????????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/getTheoryVol"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTheoryVol(@ModelAttribute("fuelTank") SwaggerTheoryFuelTank fuelTank) {
        FuelTank fuelTank1 = new FuelTank();
        BeanUtils.copyProperties(fuelTank, fuelTank1);
        JSONObject msg = new JSONObject();
        String theoryVol = OilMassMgtUtil.get_theory_Volume_by_shape(fuelTank1);
        msg.put("theoryVol", theoryVol);
        return new JsonResultBean(msg);
    }

    /**
     * ????????????????????????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})

    @RequestMapping(value = {"/calCalibration"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean calCalibration(@ModelAttribute("bean")SwaggerCalCalibration bean) {
        OilVehicleSetting bean1 = new OilVehicleSetting();
        BeanUtils.copyProperties(bean, bean1);
        try {
            JSONObject msg = new JSONObject();
            fuelTankManageService.addOilCalibration(bean1);
            List<OilCalibrationForm> list = fuelTankManageService.getOilCalibrationList(bean1.getId());
            msg.put("result", list);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ????????????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/editOilCal"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editOilCal(@ModelAttribute("bean") SwaggerDoubleOilVehicleSetting bean) {
        try {
            DoubleOilVehicleSetting bean1 = new DoubleOilVehicleSetting();
            BeanUtils.copyProperties(bean, bean1);
            JSONObject msg = new JSONObject();
            if (StringUtils.isNotBlank(bean1.getOilBoxId())) { // ?????????
                oilLevelHeights = bean1.getOilLevelHeights();
                oilValues = bean1.getOilValues();
            }
            if (StringUtils.isNotBlank(bean1.getOilBoxId2())) { // ?????????
                oilLevelHeights2 = bean1.getOilLevelHeights2();
                oilValues2 = bean1.getOilValues2();
            }
            fuelTankManageService.updateOilCalibration(bean1);
            msg.put("result", bean1);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     * @author Liubangquan
     */
    @RequestMapping(value = {"/getNewCalData"}, method = RequestMethod.POST)
    @ApiOperation(value = "????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "curBox", value = "????????????1??????2", required = true, paramType = "query", dataType = "string")
    @ResponseBody
    public JsonResultBean getNewCalData(String curBox) {
        JSONObject msg = new JSONObject();
        if ("1".equals(curBox)) {
            msg.put("oilLevelHeights", oilLevelHeights);
            msg.put("oilValues", oilValues);
        }
        if ("2".equals(curBox)) {
            msg.put("oilLevelHeights2", oilLevelHeights2);
            msg.put("oilValues2", oilValues2);
        }
        return new JsonResultBean(msg);
    }

    /**
     * ??????????????????
     * @author Liubangquan
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FuelTankForm form = fuelTankManageService.importOilCalibration(file);
            JSONObject msg = new JSONObject();
            if (Converter.toBlank(form.getBoxLength()).equals("") || Converter.toBlank(form.getWidth()).equals("")
                || Converter.toBlank(form.getHeight()).equals("")
                || Converter.toBlank(form.getSensorLength()).equals("")
                || Converter.toBlank(form.getThickness()).equals("")
                || Converter.toBlank(form.getOilLevelHeightList()).equals("")
                || Converter.toBlank(form.getOilValueList()).equals("")) {
                msg.put("flag", 0);
                msg.put("errorMsg", "??????????????????????????????????????????????????????");
                msg.put("resultInfo", "???????????????");
                String errMsg = "???????????????????????????!<br/>???????????????????????????????????????????????????????????????????????????????????????";
                return new JsonResultBean(JsonResultBean.FAULT, errMsg);
            }
            FuelTank fuelTank = new FuelTank();
            fuelTank.setShape("1");
            fuelTank.setBoxLength(form.getBoxLength());
            fuelTank.setWidth(form.getWidth());
            fuelTank.setHeight(form.getHeight());
            fuelTank.setThickness(form.getThickness());
            fuelTank.setButtomRadius(form.getButtomRadius());
            fuelTank.setTopRadius(form.getTopRadius());
            String theoryVol = OilMassMgtUtil.get_theory_Volume_by_shape(fuelTank);
            form.setTheoryVolume(theoryVol);
            form.setRealVolume(theoryVol);
            msg.put("result", form);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????:????????????????????????????????????????????????????????????
     * @author Liubangquan
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/exportTemplate", method = RequestMethod.POST)
    public void fileDownload(HttpServletResponse response, String value) {
        try (ServletOutputStream out = response.getOutputStream()) {
            response.setCharacterEncoding("utf-8");
            // ????????????????????????(??????ServletContext??????)??????????????????????????????????????????????????????
            String path = servletContext.getRealPath("/");

            String filename;
            // ????????????????????????File??????(???????????????????????????download.pdf??????)
            File file;
            if (Converter.toBlank(value).equals("1")) { // AD????????????
                filename = "?????????????????????-AD????????????";
                file = new File(path + "file/vas/" + "01.?????????????????????-AD????????????.xlsx");
            } else { // ???????????????
                filename = "?????????????????????-???????????????";
                file = new File(path + "file/vas/" + "02.?????????????????????-???????????????.xlsx");
            }
            // 1.????????????ContentType?????????????????????????????????????????????????????????
            response.setContentType("application/msexcel;charset=UTF-8");
            // 2.????????????????????????????????????????????????????????????(???????????????a.pdf)
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xlsx");

            writeFileToStream(out, file);
            out.flush();
        } catch (IOException e) {
            log.error("????????????????????????", e);
        }
    }

    private void writeFileToStream(ServletOutputStream out, File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1) {
                b = inputStream.read(buffer);
                // 4.???????????????(out)???
                out.write(buffer, 0, b);
            }
        }
    }

    /**
     * ??????
     * @author Liubangquan
     */
    @ApiOperation(value = "?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "????????????", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId", value = "??????id,??????????????????????????????", paramType = "query", dataType = "string")})
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(String type, String oilBoxId) {
        try {
            FuelTankForm form = fuelTankManageService.getOilBoxByType(type);
            if (oilBoxId == null || "".equals(oilBoxId)) {
                //??????
                return form == null;
            } else {
                //??????
                return form == null || (oilBoxId.equals(form.getId()));
            }
        } catch (Exception e) {
            log.error("??????????????????", e);
            return false;
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @ApiIgnore
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/importTank", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTank(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // ??????????????????IP??????
            Map resultMap = fuelTankManageService.importTank(file, ipAddress);
            String msg = "???????????????" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @ApiIgnore
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/downloadTank", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            String filename = "????????????????????????";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fuelTankManageService.generateTankTemplate(response);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }

    /**
     * ??????
     */
    @ApiIgnore
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/exportTank", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            String filename = "??????????????????";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fuelTankManageService.exportTank(null, 1, response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ???????????????????????????
     * @author Liubangquan
     */
    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "oilBoxId", value = "??????id)", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/checkBoxBound", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkBoxBound(String oilBoxId) {
        try {
            JSONObject msg = new JSONObject();
            boolean isBound = fuelTankManageService.findBoxBound(oilBoxId);
            msg.put("isBound", isBound);
            msg.put("oilBoxId", oilBoxId);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
