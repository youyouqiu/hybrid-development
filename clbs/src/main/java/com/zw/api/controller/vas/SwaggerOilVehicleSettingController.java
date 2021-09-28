package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.OilVehicleSettingQuery;
import com.zw.platform.service.basicinfo.RodSensorService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 油量车辆设置Controller <p>Title: OilVehicleSettingController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:26:13
 */
@Controller
@RequestMapping("/swagger/v/oilvehiclesetting")
@Api(tags = { "油量车辆设置" }, description = "油量车辆设置相关api接口")
public class SwaggerOilVehicleSettingController {

    private static DecimalFormat dfInt = new DecimalFormat("#"); // 整数

    private static DecimalFormat df_1 = new DecimalFormat("0.0"); // 保留一位小数

    @Autowired
    private OilVehicleSettingService oilVehicleSettingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private RodSensorService rodSensorService;

    @Autowired
    private HttpServletRequest request;

    private static Logger logger = LogManager.getLogger(SwaggerOilVehicleSettingController.class);

    @Auth

    @ApiOperation(value = "分页查询油量车辆设置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属组织id", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "assignmentId", value = "所属分组id", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final OilVehicleSettingQuery query) {
        try {
            if (query != null) {
                // 校验传入字段
                if (query.getPage() == null || query.getLimit() == null) { // page和limit不能为空
                    return new PageGridBean(PageGridBean.FAULT);
                }
                if (StringUtils.isNotBlank(query.getSimpleQueryParam())
                    && query.getSimpleQueryParam().length() > 20) { // 模糊搜索长度小于20
                    return new PageGridBean(PageGridBean.FAULT);
                }

                Page<OilVehicleSetting> result =
                    PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                        .doSelectPage(() -> oilVehicleSettingService.findOilVehicleList(query));
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            logger.error("分页查询分组（findOilVehicleList）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: bindPage
     */
    @ApiOperation(value = "根据车辆id查询油量车辆绑定选项值", notes = "返回车辆实体，参考车辆集合，邮箱集合，油杆集合", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean bindPage(@PathVariable("id") final String id) {
        try {
            if (vehicleService.findVehicleById(id) == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
            }
            JSONObject objJson = new JSONObject();
            // 查询车
            VehicleInfo vehicle = vehicleService.findVehicleById(id);
            // 查询参考车辆
            List<DoubleOilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceVehicle();
            // 查询油箱
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // 查询油杆
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            // if (null != fuelTankList && fuelTankList.size() > 0) {
            // for (FuelTank f : fuelTankList) {
            // f.setShapeStr(fuelTankManageService.getOilBoxShapeStr(f.getShape()));
            // }
            // }
            // if (null != vehicleList && vehicleList.size() > 0) {
            // for (DoubleOilVehicleSetting dos : vehicleList) {
            // dos.setShapeStr(fuelTankManageService.getOilBoxShapeStr(dos.getShape()));
            // dos.setShape2Str(fuelTankManageService.getOilBoxShapeStr(dos.getShape2()));
            // }
            // }
            objJson.put("vehicle", vehicle);
            objJson.put("fuelTankList", fuelTankList);
            objJson.put("rodSensorList", rodSensorList);
            objJson.put("vehicleList", vehicleList);
            objJson.put("id", UUID.randomUUID().toString());
            objJson.put("id2", UUID.randomUUID().toString());
            return new JsonResultBean(objJson);
        } catch (Exception e) {
            logger.error("根据车辆id查询油量车辆绑定选项值异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * TODO 油箱车辆绑定
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: bind
     */
    @ApiOperation(value = "保存车辆与邮箱油杆的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "邮箱1与车辆绑定id,生成uuid", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId", value = "邮箱1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType", value = "油杆1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxType", value = "油箱类型（1：油箱1；2：油箱2）", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "id2", value = "邮箱2与车辆绑定id(未绑定邮箱2不填,若绑定邮箱2必填)", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId2", value = "邮箱2id(未绑定邮箱2不填,若绑定邮箱2必填)", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType2", value = "油杆2id(未绑定邮箱2不填,若绑定邮箱2必填)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxType2", value = "油箱类型（1：油箱1；2：油箱2）", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets", value = "油箱1标定组数", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime", value = "油箱1自动上传时间(01:被动,02:10s,03:20s,04:30s)",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK", value = "油箱1输出修正系数K,必须为整数，范围[1,200]", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB", value = "油箱1输出修正系数B,必须为整数，范围[0,200]", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold", value = "油箱1加油时间阈值（秒），必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol", value = "油箱1加油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold", value = "油箱1漏油时间阈值（秒），必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol", value = "油箱1漏油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets2", value = "邮箱2标定组数(未绑定邮箱2不填,若绑定邮箱2必填)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime2", value = "油箱2自动上传时间(01:被动,02:10s,03:20s,04:30s)",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK2", value = "油箱2输出修正系数K,必须为整数，范围[1,200]",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB2", value = "油箱2输出修正系数B,必须为整数，范围[0,200]",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold2", value = "油箱2加油时间阈值（秒）,必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol2", value = "油箱2加油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold2", value = "油箱2漏油时间阈值（秒），必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol2", value = "油箱2漏油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string") })
    @ResponseBody
    public JsonResultBean bind(@Validated({ ValidGroupUpdate.class }) final DoubleOilVehicleSetting bean,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 车辆是否存在
                if (vehicleService.findVehicleById(bean.getVehicleId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
                }
                // 油箱1是否存在
                if (fuelTankManageService.findFuelTankById(bean.getOilBoxId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油箱1不存在！");
                }
                // 油杆1传感器是否存在
                if (rodSensorService.get(bean.getSensorType()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油杆1不存在！");
                }
                if (StringUtils.isNotBlank(bean.getId2())) {
                    if (StringUtils.isBlank(bean.getOilBoxId2()) || StringUtils.isNotBlank(bean.getSensorType2())
                        || StringUtils.isNotBlank(bean.getCalibrationSets2())) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2的油箱id,油杆id,标定组数id不能为空！");
                    }
                }
                // 油箱2是否存在
                if (StringUtils.isNotBlank(bean.getOilBoxId2())
                    && fuelTankManageService.findFuelTankById(bean.getOilBoxId2()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油箱2不存在！");
                }
                // 油杆2传感器是否存在
                if (StringUtils.isNotBlank(bean.getSensorType2())
                    && rodSensorService.get(bean.getSensorType2()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油杆2不存在！");
                }

                // 根据车辆id 删除绑定关系（避免同时操作）
                if (StringUtils.isNotBlank(
                    oilVehicleSettingService.findOilVehicleSettingByVid(bean.getVehicleId()).getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆已绑定油杆传感器！");
                }
                String k1 = bean.getOutputCorrectionCoefficientK();
                String b1 = bean.getOutputCorrectionCoefficientB();
                String a1 = bean.getAddOilTimeThreshold();
                String aa1 = bean.getAddOilAmountThreshol();
                String s1 = bean.getSeepOilTimeThreshold();
                String sa1 = bean.getSeepOilAmountThreshol();
                if (StringUtils.isNotBlank(k1)) {
                    if (!StringUtils.isNumeric(k1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数K必须为非负整数！");
                    } else {
                        int ki = Integer.parseInt(k1);
                        if (ki < 1 || ki > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数K必须为1-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(b1)) {
                    if (!StringUtils.isNumeric(b1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数B必须为非负整数！");
                    } else {
                        int bi = Integer.parseInt(b1);
                        if (bi < 0 || bi > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数K必须为0-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(a1)) {
                    if (!StringUtils.isNumeric(a1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油时间阈值必须为非负整数！");
                    } else {
                        int ai = Integer.parseInt(a1);
                        if (ai < 0 || ai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(aa1)) {
                    if (!StringUtils.isNumeric(aa1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油量时间阈值必须为非负整数！");
                    } else {
                        int aai = Integer.parseInt(aa1);
                        if (aai < 0 || aai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(s1)) {
                    if (!StringUtils.isNumeric(s1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油时间阈值必须为非负整数！");
                    } else {
                        int si = Integer.parseInt(s1);
                        if (si < 0 || si > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(sa1)) {
                    if (!StringUtils.isNumeric(sa1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油时间阈值必须为非负整数！");
                    } else {
                        int sai = Integer.parseInt(sa1);
                        if (sai < 0 || sai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                String k2 = bean.getOutputCorrectionCoefficientK();
                String b2 = bean.getOutputCorrectionCoefficientB();
                String a2 = bean.getAddOilTimeThreshold();
                String aa2 = bean.getAddOilAmountThreshol();
                String s2 = bean.getSeepOilTimeThreshold();
                String sa2 = bean.getSeepOilAmountThreshol();
                if (StringUtils.isNotBlank(k2)) {
                    if (!StringUtils.isNumeric(k2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数K必须为非负整数！");
                    } else {
                        int ki = Integer.parseInt(k2);
                        if (ki < 1 || ki > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数K必须为1-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(b2)) {
                    if (!StringUtils.isNumeric(b2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数B必须为非负整数！");
                    } else {
                        int bi = Integer.parseInt(b2);
                        if (bi < 0 || bi > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数K必须为0-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(a2)) {
                    if (!StringUtils.isNumeric(a2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油时间阈值必须为非负整数！");
                    } else {
                        int ai = Integer.parseInt(a2);
                        if (ai < 0 || ai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(aa2)) {
                    if (!StringUtils.isNumeric(aa2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油量时间阈值必须为非负整数！");
                    } else {
                        int aai = Integer.parseInt(aa2);
                        if (aai < 0 || aai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(s2)) {
                    if (!StringUtils.isNumeric(s2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油时间阈值必须为非负整数！");
                    } else {
                        int si = Integer.parseInt(s2);
                        if (si < 0 || si > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(sa2)) {
                    if (!StringUtils.isNumeric(sa2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油时间阈值必须为非负整数！");
                    } else {
                        int sai = Integer.parseInt(sa2);
                        if (sai < 0 || sai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 新增绑定表
                return oilVehicleSettingService.addFuelTankBind(bean, ipAddress);
            }
        } catch (Exception e) {
            logger.error("绑定油杆传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "根据车辆id查询车辆与邮箱油杆的绑定详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/edit_{veId}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable("veId") final String veId, HttpServletResponse response) {
        try {
            if (vehicleService.findVehicleById(veId) == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
            }
            JSONObject objJson = new JSONObject();
            // 查询已绑定的车
            List<DoubleOilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceVehicle();
            // 查询油箱
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // 根据车辆id查询车与传感器的绑定
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(veId);
            // 查询油杆
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            // if (null != oilSetting) {
            // oilSetting.setShapeStr(fuelTankManageService.getOilBoxShapeStr(oilSetting.getShape()));
            // oilSetting.setShape2Str(fuelTankManageService.getOilBoxShapeStr(oilSetting.getShape2()));
            // }
            // if (null != fuelTankList && fuelTankList.size() > 0) {
            // for (FuelTank f : fuelTankList) {
            // f.setShapeStr(fuelTankManageService.getOilBoxShapeStr(f.getShape()));
            // }
            // }
            // if (null != vehicleList && vehicleList.size() > 0) {
            // for (DoubleOilVehicleSetting dos : vehicleList) {
            // dos.setShapeStr(fuelTankManageService.getOilBoxShapeStr(dos.getShape()));
            // dos.setShape2Str(fuelTankManageService.getOilBoxShapeStr(dos.getShape2()));
            // }
            // }
            if (StringUtils.isBlank(oilSetting.getId2())) { // 双油箱
                oilSetting.setNewId2(UUID.randomUUID().toString());
            }
            // 根据油量车辆设置表id读取油箱标定数据
            getOilCalibrationList(oilSetting);
            if (StringUtils.isNotBlank(oilSetting.getId())) {
                objJson.put("fuelTankList", fuelTankList);
                objJson.put("vehicleList", vehicleList);
                objJson.put("rodSensorList", rodSensorList);
                objJson.put("result", oilSetting);
                return new JsonResultBean(objJson);
            } else {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('该条数据已解除绑定！');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            logger.error("查询车辆与邮箱油杆的绑定详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 查询标定数据
     * @param oilSetting
     * @return void
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: getOilCalibrationList
     */
    private void getOilCalibrationList(DoubleOilVehicleSetting oilSetting) {
        try {
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId())) { // 油箱1标定数据
                List<OilCalibrationForm> list =
                    fuelTankManageService.getOilCalibrationList(oilSetting.getId()); // 读取油箱标定数据
                if (null != list && list.size() > 0) {
                    for (OilCalibrationForm ocf : list) {
                        ocf.setOilLevelHeight(dfInt.format(Converter.toDouble(ocf.getOilLevelHeight())));
                        ocf.setOilValue(df_1.format(Converter.toDouble(ocf.getOilValue())));
                    }
                }
                if (null != list && list.size() > 0) {
                    StringBuilder oilLevelHeightsBuilder = new StringBuilder();
                    StringBuilder oilValuesBuilder = new StringBuilder();
                    for (OilCalibrationForm of : list) {
                        oilLevelHeightsBuilder.append(of.getOilLevelHeight() + ",");
                        oilValuesBuilder.append(of.getOilValue() + ",");
                    }
                    String oilLevelHeights = oilLevelHeightsBuilder.toString();
                    if (oilLevelHeightsBuilder.length() > 0) {
                        oilLevelHeights = oilLevelHeights.substring(0, oilLevelHeights.length() - 1);
                    }
                    String oilValues = oilValuesBuilder.toString();
                    if (oilValues.length() > 0) {
                        oilValues = oilValues.substring(0, oilValues.length() - 1);
                    }
                    oilSetting.setOilLevelHeights(oilLevelHeights);
                    oilSetting.setOilValues(oilValues);
                }
            }
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId2())) { // 油箱2标定数据
                List<OilCalibrationForm> list =
                    fuelTankManageService.getOilCalibrationList(oilSetting.getId2()); // 读取油箱标定数据
                if (null != list && list.size() > 0) {
                    for (OilCalibrationForm ocf : list) {
                        ocf.setOilLevelHeight(dfInt.format(Converter.toDouble(ocf.getOilLevelHeight())));
                        ocf.setOilValue(df_1.format(Converter.toDouble(ocf.getOilValue())));
                    }
                }
                if (null != list && list.size() > 0) {
                    StringBuilder oilLevelHeightsBuilder = new StringBuilder();
                    StringBuilder oilValuesBuilder = new StringBuilder();
                    for (OilCalibrationForm of : list) {
                        oilLevelHeightsBuilder.append(of.getOilLevelHeight() + ",");
                        oilValuesBuilder.append(of.getOilValue() + ",");
                    }
                    String oilLevelHeights = oilLevelHeightsBuilder.toString();
                    if (oilLevelHeights.length() > 0) {
                        oilLevelHeights = oilLevelHeights.substring(0, oilLevelHeights.length() - 1);
                    }
                    String oilValues = oilValuesBuilder.toString();
                    if (oilValues.length() > 0) {
                        oilValues = oilValues.substring(0, oilValues.length() - 1);
                    }
                    oilSetting.setOilLevelHeights2(oilLevelHeights);
                    oilSetting.setOilValues2(oilValues);
                }
            }
        } catch (Exception e) {
            logger.error("查询标定数据异常", e);
        }
    }

    /**
     * TODO 修改油箱车辆设置
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: edit
     */
    @ApiOperation(value = "保存修改的车辆与邮箱油杆的绑定详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "邮箱1与车辆绑定id,生成uuid", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId", value = "邮箱1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType", value = "油杆1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxType", value = "油箱类型（1：油箱1；2：油箱2）", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "id2", value = "邮箱2与车辆关联id(未绑定邮箱2不填),当修改前为双油箱时，必填（修改/删除邮箱2）", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "newId2", value = "邮箱2与车辆关联id(未绑定邮箱2不填),当修改前为单油箱，修改后为双油箱时,必填（新增邮箱2）", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId2", value = "邮箱2id(未绑定邮箱2不填,若绑定邮箱2必填)", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType2", value = "油杆2id(未绑定邮箱2不填,若绑定邮箱2必填)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxType2", value = "油箱类型（1：油箱1；2：油箱2）", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets", value = "油箱1标定组数", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime", value = "油箱1自动上传时间(01:被动,02:10s,03:20s,04:30s)",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK", value = "油箱1输出修正系数K,必须为整数，范围[1,200]", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB", value = "油箱1输出修正系数B,必须为整数，范围[0,200]", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold", value = "油箱1加油时间阈值（秒），必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol", value = "油箱1加油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold", value = "油箱1漏油时间阈值（秒），必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol", value = "油箱1漏油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string"),

        @ApiImplicitParam(name = "calibrationSets2", value = "邮箱2标定组数(未绑定邮箱2不填,若绑定邮箱2必填)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime2", value = "油箱2自动上传时间(01:被动,02:10s,03:20s,04:30s)",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK2", value = "油箱2输出修正系数K,必须为整数，范围[1,200]",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB2", value = "油箱2输出修正系数B,必须为整数，范围[0,200]",
            required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold2", value = "油箱2加油时间阈值（秒）,必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol2", value = "油箱2加油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold2", value = "油箱2漏油时间阈值（秒），必须是1到120的整数", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol2", value = "油箱2漏油量时间阈值，必须为1到60的整数", required = false,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final DoubleOilVehicleSetting bean,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 绑定关系是否存在
                if (oilVehicleSettingService.findOilBoxVehicleByBindId(bean.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油箱1绑定关系不存在！");
                }
                // 车辆是否存在
                if (vehicleService.findVehicleById(bean.getVehicleId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
                }
                // 油箱1是否存在
                if (fuelTankManageService.findFuelTankById(bean.getOilBoxId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油箱1不存在！");
                }
                // 油杆1传感器是否存在
                if (rodSensorService.get(bean.getSensorType()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油杆1不存在！");
                }
                if (StringUtils.isNotBlank(bean.getNewId2())) {
                    if (StringUtils.isBlank(bean.getOilBoxId2()) || StringUtils.isNotBlank(bean.getSensorType2())
                        || StringUtils.isNotBlank(bean.getCalibrationSets2())) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2的油箱id,油杆id,标定组数id不能为空！");
                    }
                }
                // 绑定关系2是否存在
                if (StringUtils.isNotBlank(bean.getId2())
                    && oilVehicleSettingService.findOilBoxVehicleByBindId(bean.getId2()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油箱2绑定关系不存在！");
                }
                // 油箱2是否存在
                if (StringUtils.isNotBlank(bean.getOilBoxId2())
                    && fuelTankManageService.findFuelTankById(bean.getOilBoxId2()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油箱2不存在！");
                }
                // 油杆2传感器是否存在
                if (StringUtils.isNotBlank(bean.getSensorType2())
                    && rodSensorService.get(bean.getSensorType2()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油杆2不存在！");
                }

                if (oilVehicleSettingService.findOilBoxVehicleByBindId(bean.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该条数据已解除绑定！");
                }
                String k1 = bean.getOutputCorrectionCoefficientK();
                String b1 = bean.getOutputCorrectionCoefficientB();
                String a1 = bean.getAddOilTimeThreshold();
                String aa1 = bean.getAddOilAmountThreshol();
                String s1 = bean.getSeepOilTimeThreshold();
                String sa1 = bean.getSeepOilAmountThreshol();
                if (StringUtils.isNotBlank(k1)) {
                    if (!StringUtils.isNumeric(k1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数K必须为非负整数！");
                    } else {
                        int ki = Integer.parseInt(k1);
                        if (ki < 1 || ki > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数K必须为1-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(b1)) {
                    if (!StringUtils.isNumeric(b1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数B必须为非负整数！");
                    } else {
                        int bi = Integer.parseInt(b1);
                        if (bi < 0 || bi > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1输出修正系数K必须为0-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(a1)) {
                    if (!StringUtils.isNumeric(a1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油时间阈值必须为非负整数！");
                    } else {
                        int ai = Integer.parseInt(a1);
                        if (ai < 0 || ai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(aa1)) {
                    if (!StringUtils.isNumeric(aa1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油量时间阈值必须为非负整数！");
                    } else {
                        int aai = Integer.parseInt(aa1);
                        if (aai < 0 || aai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1加油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(s1)) {
                    if (!StringUtils.isNumeric(s1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油时间阈值必须为非负整数！");
                    } else {
                        int si = Integer.parseInt(s1);
                        if (si < 0 || si > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(sa1)) {
                    if (!StringUtils.isNumeric(sa1)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油时间阈值必须为非负整数！");
                    } else {
                        int sai = Integer.parseInt(sa1);
                        if (sai < 0 || sai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱1漏油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                String k2 = bean.getOutputCorrectionCoefficientK();
                String b2 = bean.getOutputCorrectionCoefficientB();
                String a2 = bean.getAddOilTimeThreshold();
                String aa2 = bean.getAddOilAmountThreshol();
                String s2 = bean.getSeepOilTimeThreshold();
                String sa2 = bean.getSeepOilAmountThreshol();
                if (StringUtils.isNotBlank(k2)) {
                    if (!StringUtils.isNumeric(k2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数K必须为非负整数！");
                    } else {
                        int ki = Integer.parseInt(k2);
                        if (ki < 1 || ki > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数K必须为1-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(b2)) {
                    if (!StringUtils.isNumeric(b2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数B必须为非负整数！");
                    } else {
                        int bi = Integer.parseInt(b2);
                        if (bi < 0 || bi > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2输出修正系数K必须为0-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(a2)) {
                    if (!StringUtils.isNumeric(a2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油时间阈值必须为非负整数！");
                    } else {
                        int ai = Integer.parseInt(a2);
                        if (ai < 0 || ai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(aa2)) {
                    if (!StringUtils.isNumeric(aa2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油量时间阈值必须为非负整数！");
                    } else {
                        int aai = Integer.parseInt(aa2);
                        if (aai < 0 || aai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2加油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(s2)) {
                    if (!StringUtils.isNumeric(s2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油时间阈值必须为非负整数！");
                    } else {
                        int si = Integer.parseInt(s2);
                        if (si < 0 || si > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油时间阈值必须为1-120之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(sa2)) {
                    if (!StringUtils.isNumeric(sa2)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油时间阈值必须为非负整数！");
                    } else {
                        int sai = Integer.parseInt(sa2);
                        if (sai < 0 || sai > 120) {
                            return new JsonResultBean(JsonResultBean.FAULT, "油箱2漏油量时间阈值必须为1-60之间的整数！");
                        }
                    }
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 新增绑定表
                return oilVehicleSettingService.updateOilVehicleSetting(bean, ipAddress);
            }
        } catch (Exception e) {
            logger.error("修改油箱参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除绑定
     */
    @ApiOperation(value = "根据绑定id删除车辆与邮箱油杆的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (!"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return oilVehicleSettingService.deleteFuelTankBindById(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("删除车辆与邮箱油杆的绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据绑定ids批量删除车辆与邮箱油杆的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除id集合String(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            if (item != null && item.length > 0) {
                for (int i = 0; i < item.length; i++) {
                    oilVehicleSettingService.deleteFuelTankBindById(item[i], ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("批量删除车辆与邮箱油杆的绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 详情
     */
    @ApiOperation(value = "根据绑定id查询车辆与邮箱油杆的绑定详情", notes = "用于详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean detailPage(@PathVariable("id") final String id) {
        try {
            JSONObject objJson = new JSONObject();
            // 根据车辆id查询车与油箱的绑定
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(id);
            if (null != oilSetting) {
                // oilSetting.setShapeStr(fuelTankManageService.getOilBoxShapeStr(oilSetting.getShape()));
                // oilSetting.setShape2Str(fuelTankManageService.getOilBoxShapeStr(oilSetting.getShape2()));
                VehicleInfo vehicle = vehicleService.findVehicleById(id);
                oilSetting.setBrand(vehicle.getBrand());
            }
            // 根据油量车辆设置表id读取油箱标定数据
            getOilCalibrationList(oilSetting);
            objJson.put("result", oilSetting);
            return new JsonResultBean(objJson);
        } catch (Exception e) {
            logger.error("查询车辆与邮箱油杆的绑定详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id下发围栏
     */
    @RequestMapping(value = "/sendOil", method = RequestMethod.POST)
    @ApiOperation(value = "油量车辆设置下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "sendParam",
        value = "下发参数json串，格式：[" + "{'oilVehicleId':'车辆与邮箱油杆的绑定id','vehicleId':'车辆id',"
            + "'settingParamId':'邮箱设置下发id','calibrationParamId':'标定下发id',"
            + "'transmissionParamId':'通讯参数下发id'},{}...]  "
            + "例：[{'oilVehicleId':'66263370-c772-4b99-8fc2-a63e0c36a875',"
            + "'settingParamId':'','calibrationParamId':'','transmissionParamId':"
            + "'','vehicleId':'b233ed24-722c-4b00-836a-f90ab6d7235f'}]", required = true, paramType = "query",
        dataType = "string")
    @ResponseBody
    public JsonResultBean sendOil(String sendParam) throws BusinessException {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            if (paramList != null && paramList.size() > 0) {
                oilVehicleSettingService.sendOil(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("油量车辆设置下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "下发参数格式不正确！");
        }

    }

}
