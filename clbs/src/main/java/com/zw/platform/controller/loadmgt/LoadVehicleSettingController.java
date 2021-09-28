package com.zw.platform.controller.loadmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.loadmgt.LoadVehicleSettingInfo;
import com.zw.platform.domain.vas.loadmgt.PersonLoadParam;
import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.AdValueForm;
import com.zw.platform.domain.vas.loadmgt.form.LoadVehicleSettingSensorForm;
import com.zw.platform.domain.vas.loadmgt.query.LoadVehicleSettingQuery;
import com.zw.platform.service.loadmgt.LoadVehicleSettingService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.workhourmgt.impl.WorkHourSettingServiceImpl;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/***
 @Author gfw
 @Date 2018/9/10 9:16
 @Description 载重车辆设置
 @version 1.0
 **/
@Controller
@RequestMapping(value = "/v/loadmgt/loadvehiclesetting")
public class LoadVehicleSettingController {
    private static Logger log = LogManager.getLogger(LoadVehicleSettingController.class);

    /**
     * 载重管理列表展示
     */
    private static final String LIST_PAGE = "vas/loadmgt/loadvehiclesetting/list";

    /**
     * 载重管理 设置页面
     */
    private static final String BIND_PAGE = "vas/loadmgt/loadvehiclesetting/bind";

    /**
     * 载重管理 修改页面
     */
    private static final String EDIT_PAGE = "vas/loadmgt/loadvehiclesetting/edit";

    /**
     * 其他 -- 详情页面
     */
    private static final String DETAIL_PAGE = "vas/loadmgt/loadvehiclesetting/detail";

    /**
     * 其他 -- 读取基本信息页面
     */
    public static final String BASICINFO_PAGE = "vas/loadmgt/loadvehiclesetting/basicInfo";

    /**
     * 其他 -- 远程升级页面
     */
    private static final String UPGRADE_PAGE = "vas/loadmgt/loadvehiclesetting/upgrade";

    /**
     * 其他 -- 私有参数页面
     */
    private static final String PARAMETERS_PAGE = "vas/loadmgt/loadvehiclesetting/parameters";
    /**
     * 常规参数
     */
    private static final String GENERAL_PAGE = "vas/loadmgt/loadvehiclesetting/general";

    /**
     * 通讯参数
     */
    private static final String NEWSLETTER_PAGE = "vas/loadmgt/loadvehiclesetting/newsletter";
    /**
     * 标定数据
     */
    private static final String CALIBRATION_PAGE = "vas/loadmgt/loadvehiclesetting/calibration";

    /**
     * 设置页面
     */
    private static final String ERROR_PAGE = "html/errors/error_exception";
    /**
     * 导入AD模板
     */
    private static final String IMPORT_PAGE = "vas/loadmgt/loadvehiclesetting/import";

    /**
     * 标定设置页面
     */
    private static final String CALIBRATION_SETTING_PAGE = "vas/loadmgt/loadvehiclesetting/loadCalibration";

    /**
     * 详情标定页面
     */
    private static final String CALIBRATION_DETAIL = "vas/loadmgt/loadvehiclesetting/calibrationDetail";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${vehicle.bind.workHour}")
    private String hasWorkHour;

    @Value("${device.info.null}")
    private String deviceInfoNull;

    @Autowired
    private LoadVehicleSettingService loadVehicleSettingService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    /**
     * 载重传感器 列表展示
     * @return
     * @throws BusinessException
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 载重列表展示 分页
     * @param query
     * @return
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final LoadVehicleSettingQuery query) {
        try {
            if (query != null) {
                Page<LoadVehicleSettingInfo> result = loadVehicleSettingService.findLoadVehicleList(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("分页查询分组（findLoadVehicleList）异常", e);
            return new PageGridBean(false);
        }

    }

    /**
     * 刷新参数下发状态
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = { "/refreshSendStatus" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean refreshSendStatus(String vehicleId) {
        try {
            LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingService.findLoadVehicleByVid(vehicleId);
            if (loadVehicleSettingInfo != null) {
                return new JsonResultBean(loadVehicleSettingInfo);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("载重刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }



    /**
     * 设置页面
     * id：监控对象id 对应 vehicleId
     * monitorType: 监控对象类型 1车 2物 3人
     * @return ModelAndView
     */
    @RequestMapping(value = "/getLoadSettingBindPage_{id}_{monitorType}", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView getWorkHourSettingPage(@PathVariable final String id, @PathVariable final Integer monitorType) {
        try {
            ModelAndView modelAndView = new ModelAndView(BIND_PAGE);
            // 车辆信息
            buildResultData(id, modelAndView, monitorType);
            return modelAndView;
        } catch (Exception e) {
            log.error("获取载重设置页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 设置
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @RequestMapping(value = "/addWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addWorkHourSetting(@Validated({ ValidGroupAdd.class }) LoadVehicleSettingSensorForm form,
        final BindingResult bindingResult) {
        try {
            if (Objects.nonNull(form)) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                }
                /**
                 * 数据组装
                 */
                if (!org.springframework.util.StringUtils.isEmpty(form.getPersonLoadJson())) {
                    form.setPersonLoadParam(JSONObject.parseObject(form.getPersonLoadJson(), PersonLoadParam.class));
                }
                if (!org.springframework.util.StringUtils.isEmpty(form.getTwoPersonLoadJson())) {
                    form.setTwoPersonLoadParam(
                        JSONObject.parseObject(form.getTwoPersonLoadJson(), PersonLoadParam.class));
                }
                /**
                 * 根据车辆id判定是否已经进行了绑定 (监控对象表 传感器监控对象绑定表 传感器表)
                 */
                // 排序
                if (getSortLoadAd(form)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "AD标定数据有误");
                }
                LoadVehicleSettingInfo info = loadVehicleSettingService.findLoadBingInfo(form.getVehicleId());
                if (Objects.nonNull(info) && StringUtils.isNotBlank(info.getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "车辆已绑定载重传感器");
                }
                /**
                 * 将传感器和车辆进行绑定
                 */
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return loadVehicleSettingService.addLoadVehicleSetting(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("设置载重异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @param form
     * @return
     */
    private boolean getSortLoadAd(LoadVehicleSettingSensorForm form) {
        // 没有参数 不需要进行排序判断
        if (org.springframework.util.StringUtils.isEmpty(form.getAdParamJson())) {
            return false;
        }
        String adParamJson = form.getAdParamJson();
        List<AdValueForm> adValueForms = JSONObject.parseArray(adParamJson, AdValueForm.class);
        for (int i = 0; i < adValueForms.size() - 1; i++) {
            if (new BigDecimal(adValueForms.get(i).getAdValue())
                .compareTo(new BigDecimal(adValueForms.get(i + 1).getAdValue())) > 0) {
                return true;
            }
            if (new BigDecimal(adValueForms.get(i).getAdActualValue())
                .compareTo(new BigDecimal(adValueForms.get(i + 1).getAdActualValue())) > 0) {
                return true;
            }
        }
        String twoAdParamJson = form.getTwoAdParamJson();
        if (!org.springframework.util.StringUtils.isEmpty(twoAdParamJson)) {
            List<AdValueForm> adValueForms1 = JSONObject.parseArray(twoAdParamJson, AdValueForm.class);
            for (int i = 0; i < adValueForms1.size() - 1; i++) {
                if (new BigDecimal(adValueForms1.get(i).getAdValue())
                    .compareTo(new BigDecimal(adValueForms1.get(i + 1).getAdValue())) > 0) {
                    return true;
                }
                if (new BigDecimal(adValueForms1.get(i).getAdActualValue())
                    .compareTo(new BigDecimal(adValueForms1.get(i + 1).getAdActualValue())) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据车辆Id查询车与载重的绑定信息 此处为选定参考对象时自动获取传感器参数
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = "findLoadBingInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findLoadBingInfo(String vehicleId) {
        try {
            if (StringUtils.isNotBlank(vehicleId)) {
                LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingService.findLoadBingInfo(vehicleId);
                return new JsonResultBean(loadVehicleSettingInfo);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据车辆Id查询车与载重的绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 修改 -- 页面
     * @param vehicleId   车辆id
     * @param type        当前传感器序号
     * @param monitorType 监控对象类型
     * @param response
     * @return
     */
    @RequestMapping(value = "/getLoadSettingEditPage_{vehicleId}_{type}_{monitorType}.gsp", method = RequestMethod.GET)
    public ModelAndView getWorkHourSettingEditPage(@PathVariable final String vehicleId,
        @PathVariable final String type, @PathVariable final Integer monitorType, HttpServletResponse response) {
        try {
            // 传感器型号调用SensorSettingController findSensorInfo();
            ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
            buildResultData(vehicleId, modelAndView, monitorType);
            LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingService.findLoadBingInfo(vehicleId);
            // AD模板查询
            List<AdValueForm> adList = loadVehicleSettingService.findAdList(null, loadVehicleSettingInfo.getId());
            loadVehicleSettingInfo.setAdParamJson(JSONObject.toJSONString(adList));
            if (!org.springframework.util.StringUtils.isEmpty(loadVehicleSettingInfo.getTwoId())) {
                List<AdValueForm> twoList =
                    loadVehicleSettingService.findAdList(null, loadVehicleSettingInfo.getTwoId());
                loadVehicleSettingInfo.setTwoAdParamJson(JSONObject.toJSONString(twoList));
            }
            // 编辑数据
            if (Objects.nonNull(loadVehicleSettingInfo)) {
                modelAndView.addObject("vehicleLoadSetting", loadVehicleSettingInfo);
                modelAndView.addObject("type", type);
                return modelAndView;
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
            log.error("获取载重修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 车辆信息和参考对象信息
     * @param id           监控对象id 如果监控对象是车 对应的id是vehicleId
     * @param modelAndView
     * @param monitorType
     * @throws Exception
     */
    private void buildResultData(String id, ModelAndView modelAndView, Integer monitorType) throws Exception {
        // 参考对象
        List<Integer> protocols = new ArrayList<>();
        JSONObject monitor = WorkHourSettingServiceImpl.getSensorJSONObject(id, protocols);
        if (monitor == null) {
            return;
        }
        List<LoadVehicleSettingInfo> vehicleList = loadVehicleSettingService.findReferenceVehicleByProtocols(protocols);
        modelAndView.addObject("vehicleInfo", monitor);
        modelAndView.addObject("monitorType", monitorType);
        modelAndView.addObject("vehicleList", JSON.toJSONString(vehicleList));
    }

    /**
     * 修改
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @RequestMapping(value = "/updateLoadSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateWorkHourSetting(
        @Validated({ ValidGroupUpdate.class }) LoadVehicleSettingSensorForm form, final BindingResult bindingResult) {
        try {
            if (Objects.nonNull(form)) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT, bindingResult.getAllErrors().get(0).toString());
                }
                if (!org.springframework.util.StringUtils.isEmpty(form.getPersonLoadJson())) {
                    form.setPersonLoadParam(JSONObject.parseObject(form.getPersonLoadJson(), PersonLoadParam.class));
                }
                if (!org.springframework.util.StringUtils.isEmpty(form.getTwoPersonLoadJson())) {
                    form.setTwoPersonLoadParam(
                        JSONObject.parseObject(form.getTwoPersonLoadJson(), PersonLoadParam.class));
                }
                // 排序
                if (getSortLoadAd(form)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "AD标定数据有误");
                }
                // 参数校验
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return loadVehicleSettingService.updateLoadSetting(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改载重异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 对AD进行排序
     * @param form
     */
    private void sortLoadAd(LoadVehicleSettingSensorForm form) {
        String adParamJson = form.getAdParamJson();
        String twoAdParamJson = form.getTwoAdParamJson();
        List<AdValueForm> adValueForms = JSONObject.parseArray(adParamJson, AdValueForm.class);
        if (CollectionUtils.isNotEmpty(adValueForms)) {
            Collections.sort(adValueForms);
        }
        if (!(twoAdParamJson == null || "".equals(twoAdParamJson))) {
            List<AdValueForm> twoAdValue = JSONObject.parseArray(adParamJson, AdValueForm.class);
            Collections.sort(twoAdValue);
        }
    }

    /**
     * 解绑
     * @param id id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deleteWorkHourSettingBind_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteWorkHourSettingBind(@PathVariable final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return loadVehicleSettingService.deleteLoadSettingBind(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("解绑载重异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量解绑
     * @param ids ids
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deleteMoreWorkHourSettingBind", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMoreWorkHourSettingBind(final String ids) {
        try {
            if (StringUtils.isNotBlank(ids)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return loadVehicleSettingService.deleteMoreLoadSettingBind(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解绑载重异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * “其他” 按钮 详情
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @RequestMapping(value = "/getWorkHourSettingDetailPage_{vehicleId}_{type}_{monitorType}.gsp",
        method = RequestMethod.GET)
    public ModelAndView getWorkHourSettingDetail(@PathVariable final String vehicleId, @PathVariable final String type,
        @PathVariable final Integer monitorType, HttpServletResponse response) {
        try {
            ModelAndView modelAndView = new ModelAndView(DETAIL_PAGE);
            buildResultData(vehicleId, modelAndView, monitorType);
            LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingService.findLoadBingInfo(vehicleId);
            // AD模板查询
            List<AdValueForm> adList = loadVehicleSettingService.findAdList(null, loadVehicleSettingInfo.getId());
            loadVehicleSettingInfo.setAdParamJson(JSONObject.toJSONString(adList));
            if (!org.springframework.util.StringUtils.isEmpty(loadVehicleSettingInfo.getTwoId())) {
                List<AdValueForm> twoList =
                    loadVehicleSettingService.findAdList(null, loadVehicleSettingInfo.getTwoId());
                loadVehicleSettingInfo.setTwoAdParamJson(JSONObject.toJSONString(twoList));
            }
            // 编辑数据
            if (Objects.nonNull(loadVehicleSettingInfo)) {
                modelAndView.addObject("vehicleLoadSetting", loadVehicleSettingInfo);
                modelAndView.addObject("type", type);
                return modelAndView;
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
            log.error("获取工时详情页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * “其他” 按钮 读取基本信息(getF3Param)
     * @param id sensorVehicleId
     * @return ModelAndView
     */
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView basicInfo(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // 根据id查询车绑定的载重传感器
            getLoadBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("基本信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    private void getLoadBindData(String id, ModelAndView mav) throws Exception {
        LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingService.getSensorVehicleByBindId(id);
        loadVehicleSettingInfo.setSensorPeripheralID("7" + loadVehicleSettingInfo.getSensorSequence());
        mav.addObject("result", loadVehicleSettingInfo);
    }

    /**
     * "其他" 常规参数(getF3Param)
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView generalPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingService.getSensorVehicleByBindId(id);
            loadVehicleSettingInfo.setSensorPeripheralID("7" + loadVehicleSettingInfo.getSensorSequence());
            loadVehicleSettingInfo.setPersonLoadParam(
                JSONObject.parseObject(loadVehicleSettingInfo.getPersonLoadParamJSON(), PersonLoadParam.class));
            if ("0".equals(loadVehicleSettingInfo.getPersonLoadParam().getLoadMeterWay())) {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterWayStr("单计重");
            } else if ("1".equals(loadVehicleSettingInfo.getPersonLoadParam().getLoadMeterWay())) {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterWayStr("双计重");
            } else if ("2".equals(loadVehicleSettingInfo.getPersonLoadParam().getLoadMeterWay())) {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterWayStr("四计重");
            } else {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterWayStr("单计重");
            }
            if ("0".equals(loadVehicleSettingInfo.getPersonLoadParam().getLoadMeterUnit())) {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterUnitStr("0.1kg");
            } else if ("1".equals(loadVehicleSettingInfo.getPersonLoadParam().getLoadMeterUnit())) {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterUnitStr("1kg");
            } else if ("2".equals(loadVehicleSettingInfo.getPersonLoadParam().getLoadMeterUnit())) {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterUnitStr("10kg");
            } else if ("3".equals(loadVehicleSettingInfo.getPersonLoadParam().getLoadMeterUnit())) {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterUnitStr("100kg");
            } else {
                loadVehicleSettingInfo.getPersonLoadParam().setLoadMeterUnitStr("10kg");
            }
            mav.addObject("result", loadVehicleSettingInfo);
            return mav;
        } catch (Exception e) {
            log.error("常规参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 其他" 标定数据(getF3Param)
     * @return 标定数据
     * @author angbike
     */
    @RequestMapping(value = { "/calibrantion_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView calibrationPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(CALIBRATION_PAGE);
            LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingService.getSensorVehicleByBindId(id);
            loadVehicleSettingInfo.setSensorPeripheralID("7" + loadVehicleSettingInfo.getSensorSequence());
            mav.addObject("result", loadVehicleSettingInfo);
            return mav;
        } catch (Exception e) {
            log.error("标定数据弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * “其他” 按钮 远程升级
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView upgradePage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
            getLoadBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * “其他” 按钮 私有参数（校验车辆是否在线: v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus）
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView parametersPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
            getLoadBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("私有参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * “其他” 按钮 通讯参数
     * @param id id
     * @return 通讯参数
     */
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView newsletterPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            getLoadBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("通讯参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 根据id下发参数
     * sendParam: [{"sensorVehicleId":"eaae4ea2-81e2-43f6-a579-1a5a3e59ceb4",
     * "paramId":"","vehicleId":"c0cd2974-b2c0-405c-ab47-282317820f59"}]
     * @param sendParam
     * @return JsonResultBean
     */
    @RequestMapping(value = "/sendWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendWorkHourSetting(String sendParam) {
        try {
            List<JSONObject> paramList = JSON.parseObject(sendParam, List.class);
            if (CollectionUtils.isNotEmpty(paramList)) {
                String ip = new GetIpAddr().getIpAddr(request);
                // 载重下发
                loadVehicleSettingService.sendLoadSetting(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据传感器类型查询传感器
     * @param sensorType
     * @return
     */
    @RequestMapping(value = "/findsensor", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findSensorInfo(final String sensorType) {
        try {
            List<ZwMSensorInfo> list = loadVehicleSettingService.findSensorInfo(sensorType);
            return new JsonResultBean(list);
        } catch (Exception e) {
            log.error("获取传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获取标定数组
     * @param id
     * @return
     */
    @RequestMapping(value = "getCalibrationList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getCalibrationList(String id, String sensorID) {
        try {
            LoadVehicleSettingInfo loadBingInfo = loadVehicleSettingService.findLoadBingInfo(id);
            List<AdValueForm> adValueForms;
            if (sensorID.equals(LoadVehicleSettingInfo.SENSOR_LOAD_ONE + "")) {
                adValueForms = loadVehicleSettingService.findAdList(null, loadBingInfo.getId());
            } else if (sensorID.equals(LoadVehicleSettingInfo.SENSOR_LOAD_TWO + "")) {
                adValueForms = loadVehicleSettingService.findAdList(null, loadBingInfo.getTwoId());
            } else {
                adValueForms = new ArrayList<>();
            }
            return new JsonResultBean(adValueForms);
        } catch (Exception e) {
            log.error("获取传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "标定AD模板");
            loadVehicleSettingService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载载重传感器列表模板异常", e);
        }
    }

    /**
     * 标定模板导入
     * @param file
     * @return
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 客户端ip地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = loadVehicleSettingService.importBatch(file, request, ipAddress);
            return new JsonResultBean(resultMap);
        } catch (Exception e) {
            log.error("导入载重传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 载重传感器 列表展示
     * @return
     * @throws BusinessException
     */
    @Auth
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

    /**
     * 标定设置查询
     * @param id
     * @return sensorVehicleId
     */
    @RequestMapping(value = "/calibration_list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean calibrationList(@RequestParam(value = "id", required = false) String id,
        @RequestParam(value = "sensorVehicleId", required = false) String sensorVehicleId) {
        try {
            if (org.springframework.util.StringUtils.isEmpty(id) && org.springframework.util.StringUtils
                .isEmpty(sensorVehicleId)) {
                return new JsonResultBean(JsonResultBean.FAULT, "必要参数没有填");
            }
            // AD模板查询
            List<AdValueForm> adList = loadVehicleSettingService.findAdList(id, sensorVehicleId);
            return new JsonResultBean(adList);
        } catch (Exception e) {
            log.error("下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 标定设置提交
     * @param id
     * @return sensorVehicleId
     */
    @RequestMapping(value = "/calibration_update", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean calibrationList(@RequestParam(value = "id", required = false) String id,
        @RequestParam(value = "sensorVehicleId", required = false) String sensorVehicleId,
        @RequestParam(value = "calibrationValue") String calibrationValue) {
        try {
            if (org.springframework.util.StringUtils.isEmpty(id) && org.springframework.util.StringUtils
                .isEmpty(sensorVehicleId)) {
                return new JsonResultBean(JsonResultBean.FAULT, "必要参数没有填");
            }
            // AD模板插入
            String calibrationId = loadVehicleSettingService.updateCalibration(id, sensorVehicleId, calibrationValue);
            if (calibrationId.equals("0")) {
                return new JsonResultBean(JsonResultBean.FAULT, "导入出错");
            }
            return new JsonResultBean(calibrationId);
        } catch (Exception e) {
            log.error("下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器数据
     * @param vid         vid
     * @param commandType 基本信息: 0xF8; 通讯参数: 0xF5; 常规参数: 0xF4; 读取标点 0xF6
     * @param sensorID    工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2：129):
     * @return JsonResultBean
     */
    @RequestMapping(value = "getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 标定设置页面
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = { "/CalibrationSettingPage" }, method = RequestMethod.GET)
    public String calibrationSettingPage() throws BusinessException {
        return CALIBRATION_SETTING_PAGE;
    }

    /**
     * 详情标定页面
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = { "/calibrationDetail" }, method = RequestMethod.GET)
    public String calibrationDetail() throws BusinessException {
        return CALIBRATION_DETAIL;
    }

    /**
     * 获取F3传感器数据
     * @param vid         vid
     * @param commandType 基本信息: 0xF8; 通讯参数: 0xF5; 常规参数: 0xF4;
     * @param sensorID    工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2：129):
     * @return JsonResultBean
     */
    @RequestMapping(value = "getf3pa", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getf3pa(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 载重传感器常规参数修正下发
     * @param setting setting
     * @return JsonResultBean
     */
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(LoadVehicleSettingInfo setting) {
        try {
            // pt:平台; report:以传感器为准
            String dealType = request.getParameter("deal_type");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            int mark = 0xf370;
            if (setting.getSensorPeripheralID().equals("71")) {
                mark = 0xf371;
            }
            return f3OilVehicleSettingService.updateLoadSetting(setting, dealType, ipAddress, mark);
        } catch (Exception e) {
            log.error("载重传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 外设软件升级
     * @param wirelessParam
     * @param vehicleId
     * @param bindingResult
     * @return ModelAndView
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(WirelessUpdateParam wirelessParam, String vehicleId, final BindingResult bindingResult) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            //  commandType：0x80: 128; 0x81:129;
            Integer commandType = 128;
            try {
                String commandTypeStr = request.getParameter("commandType");
                if (StringUtil.isNull(commandTypeStr)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "升级失败");
                }
                commandType = Integer.parseInt(commandTypeStr);
            } catch (Exception ex) {
                return new JsonResultBean(JsonResultBean.FAULT, "升级失败");

            }
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 2);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, "升级失败,异常");
        }
    }

    /**
     * 获取F3传感器平台设置常规参数
     * @param id id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorSetting(String id) {
        try {
            LoadVehicleSettingInfo loadBingInfo = loadVehicleSettingService.getSensorVehicleByBindId(id);
            JSONObject msg = new JSONObject();
            msg.put("setting", loadBingInfo);
            if (loadBingInfo != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
            }
            return new JsonResultBean(JsonResultBean.FAULT, deviceInfoNull);
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器私有数据(下发调用)
     * @param vid        vid
     * @param sensorID   工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2: 129):
     * @param commandStr 下发内容
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService
                .sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr, ipAddress, "3");
        } catch (Exception e) {
            log.error("获取F3传感器私有数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取最后一条位置数据附带的载重传感器AD值
     * @param vid 车辆id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getloactionad", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLoactionAd(String vid, int type) {
        try {
            if (vid == null || "".equals(vid)) {
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
            String value = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(vid));
            JSONObject jsonObject = JSONObject.parseObject(value);
            Object data = jsonObject.get("data");
            String originalAd = "";
            Map map = new HashMap();
            map.put("adValue", originalAd);
            if (null == data) {
                return new JsonResultBean(map);
            }
            JSONObject jsonObject1 = JSONObject.parseObject(data.toString());
            Object msgBody = jsonObject1.get("msgBody");
            if (null == msgBody || "".equals(msgBody)) {
                return new JsonResultBean(map);
            }
            int mark = 0x70;
            // 获取传感器id 1:表示1号传感器 2:表示2号传感器
            if (type == 1) {
                mark = 0x70;
            }
            if (type == 2) {
                mark = 0x71;
            }
            JSONArray loadInfos =
                JSONObject.parseArray(JSONObject.parseObject(msgBody.toString()).get("loadInfos").toString());
            if (null != loadInfos && !"".equals(loadInfos)) {
                for (Object loadInfo : loadInfos) {
                    JSONObject jsonob = JSONObject.parseObject(loadInfo.toString());
                    if (mark == Integer.parseInt(jsonob.get("id").toString())) {
                        if (null != jsonob.get("originalAd") && !"".equals(jsonob.get("originalAd"))) {
                            originalAd = jsonob.get("originalAd").toString();
                            map.put("adValue", originalAd);
                        }
                    }
                }
            }
            return new JsonResultBean(map);
        } catch (Exception e) {
            log.error("获取最后一条位置数据附带的载重传感器AD值", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 传感器标定参数修正下发
     * @param setting
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/updateDemarcateSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateDemarcateSetting(LoadVehicleSettingInfo setting) {
        try {
            String[] adValue = request.getParameterValues("adValue");
            String[] adActualValue = request.getParameterValues("adActualValue");
            String type = request.getParameter("deal_type");
            LoadVehicleSettingSensorForm form = new LoadVehicleSettingSensorForm();
            List list = new ArrayList();
            // 以上报为准
            if ("report".equals(type)) {
                for (int i = 0; i < adValue.length; i++) {
                    AdValueForm adValueForm = new AdValueForm();
                    adValueForm.setAdNumber(i + "");
                    adValueForm.setAdValue(adValue[i]);
                    adValueForm.setAdActualValue(adActualValue[i]);
                    list.add(adValueForm);
                }
                String str = JSONObject.toJSONString(list);
                form.setAdParamJson(str);
                if (getSortLoadAd(form)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "AD标定数据有误");
                }
            }
            form.setId(setting.getId());
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            form.setSensorId(setting.getSensorId());
            form.setVehicleId(setting.getVehicleId());
            // 获得访问ip
            String ip = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateLoadAdSetting(form, setting, ip);
        } catch (Exception e) {
            log.error("传感器标定参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 载重标定：获取最新一次的载重数据
     * @param vehicleId
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: getLatestOilData
     */
    @RequestMapping(value = "/getLatestOilData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLatestOilData(String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            // 查询车辆位置信息，同时返回下发指令的msgSN，用于对接设备返回的信息
            String msgSN = loadVehicleSettingService.getLatestPositional(vehicleId);
            msg.put("msgSN", msgSN);
            String type = request.getParameter("type");
            if (type == null || type.equals("")) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                if (vehicle != null) {
                    String brand = vehicle[0];
                    String plateColor = vehicle[1];
                    // 获得访问ip
                    String ip = new GetIpAddr().getIpAddr(request);
                    String logMsg = "监控对象：" + brand + " 车辆点名";
                    logSearchService.addLog(ip, logMsg, "3", "MONITORING", brand, plateColor);
                }
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取最新一次载重数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 校验车辆的在线状态
     * @param vehicleId
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: checkVehicleOnlineStatus
     */
    @RequestMapping(value = "/checkVehicleOnlineStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkVehicleOnlineStatus(String vehicleId) {
        try {
            return new JsonResultBean(MonitorUtils.isOnLine(vehicleId));
        } catch (Exception e) {
            log.error("校验车辆的在线状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
