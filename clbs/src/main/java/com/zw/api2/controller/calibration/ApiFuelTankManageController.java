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
 * 油箱管理Controller <p>Title: fuelTankManageController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年10月24日下午4:24:19
 * @version 1.0
 */
@Controller
@RequestMapping("api/v/oilmassmgt/fueltankmgt")
@Api(tags = {"油箱管理_dev"}, description = "油箱相关api接口")
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

    // Spring这里是通过实现ServletContextAware接口来注入ServletContext对象
    private ServletContext servletContext;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 查询油箱列表
     * @author Liubangquan
     */
    @ApiOperation(value = "分页查询油箱列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "simpleQueryParam", value = "按照油箱型号进行模糊搜索", required = false,
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
            log.error("分页查询分组（findFuelTankByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 油箱新增页面
     * @author Liubangquan
     */
    @ApiOperation(value = "油箱新增", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addFuelTank(@ModelAttribute("form")SwaggerFuelTankForm form) {
        FuelTankForm form1 = new FuelTankForm();
        BeanUtils.copyProperties(form, form1);
        try {
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return fuelTankManageService.addFuelTank(form1, ip);
        } catch (Exception e) {
            log.error("新增油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 初始化油杆传感器列表
     * @author Liubangquan
     */
    @ApiOperation(value = "初始化油杆传感器列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "boxHeight", value = "油箱高度", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = {"/sensorList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initSensorList(String boxHeight) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("sensorList", fuelTankManageService.findRodSensorList(boxHeight));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("初始化油杆传感器列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据传感器id查询传感器详细信息
     * @author Liubangquan
     */
    @ApiOperation(value = "根据传感器id查询传感器详细信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "sensorId", value = "传感器id", required = true,
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
                list.get(0).setOddEvenCheckStr("奇校验");
            } else if (oddEvenCheck == 2) {
                list.get(0).setOddEvenCheckStr("偶校验");
            } else {
                list.get(0).setOddEvenCheckStr("无校验");
            }
            if (compensationCanMake == 1) {
                list.get(0).setCompensationCanMakeStr("使能");
            } else {
                list.get(0).setCompensationCanMakeStr("禁能");
            }
            if ("01".equals(filteringFactor)) {
                list.get(0).setFilteringFactorStr("实时");
            } else if ("02".equals(filteringFactor)) {
                list.get(0).setFilteringFactorStr("平滑");
            } else {
                list.get(0).setFilteringFactorStr("平稳");
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
            log.error("查询传感器详细信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 删除油箱
     * @author Liubangquan
     */
    @ApiOperation(value = "根据id删除油箱", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/delete_{id}"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id) {
        try {
            if (!"".equals(id)) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fuelTankManageService.deleteFuelTankById(id, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     * @author Liubangquan
     */
    @ApiOperation(value = "根据ids批量删除油箱", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "deltems", value = "批量删除id集合String(用逗号隔开)", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fuelTankManageService.deleteBatchFuelTankById(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询油箱详细信息
     * @author Liubangquan
     */
    @ApiOperation(value = "根据id查询油箱详细信息", authorizations = {
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
            log.error("查询油箱详细信息弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询油箱详细信息异常");
        }
    }

    /**
     * 油箱管理-修改页面
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
            log.error("油箱管理修改弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "油箱管理修改弹出页面异常");
        }
    }

    /**
     * 油箱管理-修改功能提交
     * @author Liubangquan
     */
    @ApiOperation(value = "修改油箱信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public JsonResultBean edit(@ModelAttribute("form") SwaggerEditFuelTankForm form) {
        try {
            final FuelTankForm form1 = new FuelTankForm();
            BeanUtils.copyProperties(form, form1);
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return fuelTankManageService.updateFuelTank(form1, ip);
        } catch (Exception e) {
            log.error("修改油箱信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据页面填写的长度、宽度、高度、壁厚计算油箱的理论容积
     * @param fuelTank 油箱实体类
     * @author Liubangquan
     */
    @ApiOperation(value = "根据长度、宽度、高度、壁厚计算油箱的理论容积", authorizations = {
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
     * 计算油箱标定数据
     */
    @ApiOperation(value = "计算油箱标定数据", authorizations = {
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
            log.error("计算油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 修改油箱标定数据
     * @author Liubangquan
     */
    @ApiOperation(value = "编辑标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/editOilCal"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editOilCal(@ModelAttribute("bean") SwaggerDoubleOilVehicleSetting bean) {
        try {
            DoubleOilVehicleSetting bean1 = new DoubleOilVehicleSetting();
            BeanUtils.copyProperties(bean, bean1);
            JSONObject msg = new JSONObject();
            if (StringUtils.isNotBlank(bean1.getOilBoxId())) { // 单油箱
                oilLevelHeights = bean1.getOilLevelHeights();
                oilValues = bean1.getOilValues();
            }
            if (StringUtils.isNotBlank(bean1.getOilBoxId2())) { // 双油箱
                oilLevelHeights2 = bean1.getOilLevelHeights2();
                oilValues2 = bean1.getOilValues2();
            }
            fuelTankManageService.updateOilCalibration(bean1);
            msg.put("result", bean1);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("修改油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 标定详情中点击提交后，重新赋值到详情界面
     * @author Liubangquan
     */
    @RequestMapping(value = {"/getNewCalData"}, method = RequestMethod.POST)
    @ApiOperation(value = "标定详情中点击提交后，重新赋值到详情界面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "curBox", value = "油箱传入1或者2", required = true, paramType = "query", dataType = "string")
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
     * 导入标定数据
     * @author Liubangquan
     */
    @ApiIgnore
    @ApiOperation(value = "导入标定数据", authorizations = {
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
                msg.put("errorMsg", "请将导入文件按照模板格式整理后再导入");
                msg.put("resultInfo", "导入失败！");
                String errMsg = "导入结果：导入失败!<br/>请检查数据是否填写正确或将导入文件按照模板格式整理后再导入";
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
            log.error("导入标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出标定模板:将文件存放到服务器上，然后从服务器上下载
     * @author Liubangquan
     */
    @ApiIgnore
    @ApiOperation(value = "导出标定模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/exportTemplate", method = RequestMethod.POST)
    public void fileDownload(HttpServletResponse response, String value) {
        try (ServletOutputStream out = response.getOutputStream()) {
            response.setCharacterEncoding("utf-8");
            // 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
            String path = servletContext.getRealPath("/");

            String filename;
            // 通过文件路径获得File对象(假如此路径中有一个download.pdf文件)
            File file;
            if (Converter.toBlank(value).equals("1")) { // AD值标定法
                filename = "油箱标定导入表-AD值标定法";
                file = new File(path + "file/vas/" + "01.油箱标定导入表-AD值标定法.xlsx");
            } else { // 标尺标定法
                filename = "油箱标定导入表-标尺标定法";
                file = new File(path + "file/vas/" + "02.油箱标定导入表-标尺标定法.xlsx");
            }
            // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("application/msexcel;charset=UTF-8");
            // 2.设置文件头：最后一个参数是设置下载文件名(假如我们叫a.pdf)
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xlsx");

            writeFileToStream(out, file);
            out.flush();
        } catch (IOException e) {
            log.error("导出标定模板异常", e);
        }
    }

    private void writeFileToStream(ServletOutputStream out, File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1) {
                b = inputStream.read(buffer);
                // 4.写到输出流(out)中
                out.write(buffer, 0, b);
            }
        }
    }

    /**
     * 去重
     * @author Liubangquan
     */
    @ApiOperation(value = "判断油箱型号是否已存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "油箱型号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId", value = "油箱id,新增非必填，修改必填", paramType = "query", dataType = "string")})
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(String type, String oilBoxId) {
        try {
            FuelTankForm form = fuelTankManageService.getOilBoxByType(type);
            if (oilBoxId == null || "".equals(oilBoxId)) {
                //新增
                return form == null;
            } else {
                //编辑
                return form == null || (oilBoxId.equals(form.getId()));
            }
        } catch (Exception e) {
            log.error("去除重复异常", e);
            return false;
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @ApiIgnore
    @ApiOperation(value = "导入油箱数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/importTank", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTank(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
            Map resultMap = fuelTankManageService.importTank(file, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     */
    @ApiIgnore
    @ApiOperation(value = "下载导入油箱模块", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/downloadTank", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            String filename = "油箱信息列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fuelTankManageService.generateTankTemplate(response);
        } catch (Exception e) {
            log.error("下载油箱信息列表模板异常", e);
        }
    }

    /**
     * 导出
     */
    @ApiIgnore
    @ApiOperation(value = "导出油箱信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/exportTank", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            String filename = "油箱信息列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fuelTankManageService.exportTank(null, 1, response);
        } catch (Exception e) {
            log.error("导出油箱信息列表异常", e);
        }
    }

    /**
     * 校验油箱是否被绑定
     * @author Liubangquan
     */
    @ApiOperation(value = "校验油箱是否被绑定", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "oilBoxId", value = "油箱id)", required = true,
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
            log.error("油箱是否被绑定校验异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
