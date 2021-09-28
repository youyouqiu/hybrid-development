package com.zw.adas.controller.defineSetting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleConfigForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskEventConfigQuery;
import com.zw.adas.service.defineSetting.AdasRiskEventConfigService;
import com.zw.adas.service.defineSetting.AdasRiskLevelService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.MyBeanUtils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 风险定义设置Controller Created by wjy on 2017/8/15.
 */
@Controller
@RequestMapping("/r/riskManagement/DefineSettings")
public class AdasDefineSettingsController {
    private static Logger log = LogManager.getLogger(AdasDefineSettingsController.class);

    private static final String LIST_PAGE = "risk/riskManagement/DefineSettings/list";

    private static final String ADD_PAGE = "risk/riskManagement/DefineSettings/add";

    private static final String PERINFO_PAGE = "risk/riskManagement/DefineSettings/perInfo";

    private static final String PERSTATE_PAGE = "risk/riskManagement/DefineSettings/perState";

    private static final String DSMINFO_PAGE = "risk/riskManagement/DefineSettings/dsmInfo";

    private static final String ADASINFO_PAGE = "risk/riskManagement/DefineSettings/adasInfo";

    private static final String EDIT_PAGE = "risk/riskManagement/DefineSettings/edit";

    private static final String UPGRADE_PAGE = "risk/riskManagement/DefineSettings/upgrade"; // 远程升级

    private static final String PARAMETERS_PAGE = "risk/riskManagement/DefineSettings/parameters";//私有参数

    private static final String COLUMN_STR = "id,paramId,brand,status,groupName,vehicleId,dId,vehicleConfigId";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${resolution.param.error}")
    private String resolutionParamError;

    @Value("${vehicle.null}")
    private String vehicleNull;

    // 用于校验前台传递的值
    private static final String[] resolutionArray = { "0x01", "0x02", "0x03", "0x04", "0x05", "0x06", "0x07" };

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    @Autowired
    private AdasRiskEventConfigService adasRiskEventConfigService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private AdasRiskLevelService adasRiskLevelService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 读取外设信基本信息
     * @return
     */
    @RequestMapping(value = { "/readPerInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getPerInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(PERINFO_PAGE);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("读取外设信基本信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/setRiskSetting" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean setRiskSetting(@RequestParam("params") String checkedParams) {
        try {
            List<AdasRiskEventVehicleConfigForm> list;
            JSONArray jsonArray;
            if (StringUtils.isNotBlank(checkedParams)) {
                jsonArray = JSON.parseArray(checkedParams);
                list = jsonArray.toJavaList(AdasRiskEventVehicleConfigForm.class);
                if (isErrorResolution(list)) {
                    return new JsonResultBean(JsonResultBean.FAULT, resolutionParamError);
                }
                List<String> vehicleIds = Lists.newLinkedList();
                // 获取有序的车的id
                List<String> sortVehicleId = RedisHelper.getList(RedisKeyEnum.VEHICLE_SORT_LIST.of());
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                Map<String, RedisKey> redisKeyMap = new HashMap<>();
                for (int i = 0; i < sortVehicleId.size(); i++) {
                    redisKeyMap.put(sortVehicleId.get(i),
                        HistoryRedisKeyEnum.ADAS_VEHICLE_ALARM.of(sortVehicleId.get(i), "6401"));
                }
                Map<String, RedisKey> containsKey = RedisHelper.isContainsKey(redisKeyMap);
                sortVehicleId.removeAll(containsKey.keySet());
                for (String id : sortVehicleId) {
                    if (vehicleIds.size() >= 200) {
                        vehicleIds.add(id);
                        adasRiskEventConfigService.updateRiskSettingByBatch(vehicleIds, list, ipAddress);
                        vehicleIds = Lists.newLinkedList();
                    } else {
                        vehicleIds.add(id);
                    }
                }

                if (vehicleIds.size() > 0) {
                    adasRiskEventConfigService.updateRiskSettingByBatch(vehicleIds, list, ipAddress);
                }
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量修改风险定义设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 远程升级页面
     * @param vehicleId
     * @param brand
     * @return
     */
    @RequestMapping(value = { "/upgrade_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getUpgradePage(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @return 私有参数
     * @author angbike
     */
    @RequestMapping(value = { "/parameters_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getParametersPage(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("私有参数下发弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 读取外设状态
     * @return
     */
    @RequestMapping(value = { "/readPerState_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getPerState(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(PERSTATE_PAGE);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("读取外设状态信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/refresh_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean refresh(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        try {
            Map map = new HashMap();
            // 风险事件设置
            List<AdasRiskEventVehicleConfigForm> settingList =
                adasRiskEventConfigService.findRiskSettingByVid(vehicleId);
            // 风险等级查询
            List<Map<String, String>> riskLevelList = adasRiskLevelService.getRiskLevel(null);
            String settingListJsonStr = JSON.toJSONString(settingList);
            String riskLevelListStr = JSON.toJSONString(riskLevelList);
            map.put("riskSettingList", settingListJsonStr);
            map.put("vehicleid", vehicleId);
            map.put("brand", brand);
            map.put("riskLevelList", JSON.parseArray(riskLevelListStr));
            return new JsonResultBean(map);
        } catch (Exception e) {
            log.error("刷新页面异常");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 读取DSM外设参数信息
     * @return
     */
    @RequestMapping(value = { "/readDsmInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getDsmInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(DSMINFO_PAGE);
        try {
            queryRiskSetting(vehicleId, brand, mav);

            return mav;
        } catch (Exception e) {
            log.error("读取DSM外设参数信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    private void queryRiskSetting(@PathVariable("vehicleId") String vehicleId, @PathVariable("brand") String brand,
        ModelAndView mav) {
        // 风险事件设置
        List<AdasRiskEventVehicleConfigForm> settingList = adasRiskEventConfigService.findRiskSettingByVid(vehicleId);
        // 风险等级查询
        List<Map<String, String>> riskLevelList = adasRiskLevelService.getRiskLevel(null);
        String settingListJsonStr = JSON.toJSONString(settingList);
        String riskLevelListStr = JSON.toJSONString(riskLevelList);

        mav.addObject("riskSettingList", settingListJsonStr);
        mav.addObject("vehicleid", vehicleId);
        mav.addObject("brand", brand);
        mav.addObject("riskLevelList", JSON.parseArray(riskLevelListStr));
    }

    /**
     * 读取adas外设参数信息
     * @return
     */
    @RequestMapping(value = { "/readAdasInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getAdasInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(ADASINFO_PAGE);
        try {
            // 风险事件设置
            queryRiskSetting(vehicleId, brand, mav);
            return mav;
        } catch (Exception e) {
            log.error("读取adas外设参数信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }

    }

    /**
     * 获取指定车辆参数下发状态
     * true获取ADAS状态，flase获取DSM状态
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = { "getStatus" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getStatus(String vehicleId, String swiftNumber, boolean flag) {
        try {
            Map<String, Integer> map = new HashMap<>();
            List<Directive> paramlist;
            String paramName = "ADAS_" + vehicleId;
            if (flag) {
                String paramType64 = "F3-8103-64"; // 64Adas
                paramlist = parameterDao.findParameterStatus(vehicleId, paramName, paramType64, swiftNumber);
            } else {
                String paramType65 = "F3-8103-65"; // 65dsm
                paramlist = parameterDao.findParameterStatus(vehicleId, paramName, paramType65, swiftNumber);
            }
            if (paramlist != null && paramlist.size() > 0) {
                Directive param = paramlist.get(0);
                map.put("status", param.getStatus());
            }
            if (map.get("status") != null) {
                return new JsonResultBean(map);
            }
            map.put("status", 1);
            return new JsonResultBean(map);

        } catch (Exception e) {
            log.error("返回状态失败");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(AdasRiskEventConfigQuery query) {
        try {
            Page<Map<String, Object>> result;
            List<Map<String, Object>> dataMap;
            result = (Page<Map<String, Object>>) adasRiskEventConfigService.findRiskVehicleList(query);
            dataMap = result.getResult();
            // 遍历所有列名，若没有值，默认设置为""
            String[] column = COLUMN_STR.split(",");
            for (Map<String, Object> map : dataMap) {
                for (String keyStr : column) {
                    if (!map.containsKey(keyStr)) {
                        map.put(keyStr, "");
                    }
                }

                // 下发状态
                if (StringUtils.isNotBlank((String) map.get("id")) || StringUtils
                    .isNotBlank((String) map.get("vehicleConfigId"))) { // 已绑定
                    String paramType64 = "F3-8103-64"; // 64Adas
                    String paramType65 = "F3-8103-65"; // 65Dsm
                    String paramName = "ADAS_" + map.get("vehicleId");

                    List<Directive> paramlist1 =
                        parameterDao.findParameterByType((String) map.get("vehicleId"), paramName, paramType64); // 64
                    List<Directive> paramlist2 =
                        parameterDao.findParameterByType((String) map.get("vehicleId"), paramName, paramType65); // 65
                    Directive param1 = null;
                    Directive param2 = null;
                    if (paramlist1 != null && paramlist1.size() > 0) {
                        param1 = paramlist1.get(0);
                    }
                    if (paramlist2 != null && paramlist2.size() > 0) {
                        param2 = paramlist2.get(0);
                    }
                    if (param1 != null && param2 != null) {
                        if (param1.getStatus().equals(param2.getStatus())) {
                            map.put("status", param1.getStatus());
                        } else if (param1.getStatus().intValue() == 4
                            || param2.getStatus().intValue() == 4) { // 有一个没有收到回应，则状态为已下发
                            map.put("status", 4);
                        } else if (param1.getStatus().intValue() == 7
                            || param2.getStatus().intValue() == 7) { // 有一个没有收到回应，则状态为已下发
                            map.put("status", 7);
                        } else {
                            map.put("status", 1);
                        }
                    }
                }
            }
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询风险定义绑定列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = { "/getParameter_{vehicleId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getSetParameter(@PathVariable("vehicleId") final String vehicleId) {
        // 风险事件设置
        List<AdasRiskEventVehicleConfigForm> settingList = adasRiskEventConfigService.findRiskSettingByVid(vehicleId);
        String settingListJsonStr = JSON.toJSONString(settingList);
        return new JsonResultBean(JsonResultBean.SUCCESS, JSON.parseArray(settingListJsonStr).toJSONString());
    }

    @RequestMapping(value = { "/setting_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(EDIT_PAGE);
        try {
            // 风险事件设置
            List<AdasRiskEventVehicleConfigForm> settingList =
                adasRiskEventConfigService.findRiskSettingByVid(vehicleId);
            // 风险等级查询
            List<Map<String, String>> riskLevelList = adasRiskLevelService.getRiskLevel(null);

            // 查询参考车牌下拉列表
            List<AdasRiskEventVehicleConfigForm> referVehicleList = adasRiskEventConfigService.findReferVehicle();
            // 根据id查询车辆

            String settingListJsonStr = JSON.toJSONString(settingList);
            String riskLevelListStr = JSON.toJSONString(riskLevelList);
            mav.addObject("riskSettingList", JSON.parseArray(settingListJsonStr));
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            mav.addObject("riskLevelList", JSON.parseArray(riskLevelListStr));

            String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);
            mav.addObject("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
            return mav;
        } catch (Exception e) {
            log.error("风险定义设置弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/settingmore_{vehicleId}.gsp" }, method = RequestMethod.GET)
    public ModelAndView settingMorePage(@PathVariable("vehicleId") final String vehicleId, HttpServletResponse response)
        throws Exception {
        try {
            if (StringUtils.isBlank(vehicleId)) {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('请选择一条数据！');");
                out.println("</script>");
                return null;
            } else {
                ModelAndView mav = new ModelAndView(EDIT_PAGE);
                String[] item = vehicleId.split(",");
                String brands = "";
                if (item != null && item.length > 0) {
                    List<String> ids = Arrays.asList(item);
                    List<String> brandList = RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(ids), "name");
                    if (brandList != null && !brandList.isEmpty()) {
                        brands = Joiner.on(",").join(brandList);
                    }
                }
                // 风险事件设置
                List<RiskEventVehicleConfigForm> settingList = new ArrayList<>();
                // 风险等级查询
                List<Map<String, String>> riskLevelList = adasRiskLevelService.getRiskLevel(null);

                // 查询参考车牌下拉列表
                List<AdasRiskEventVehicleConfigForm> referVehicleList = adasRiskEventConfigService.findReferVehicle();
                // 风控定义配置 批处理的时候，参考去除自己 ,去除车牌 by wanxing
                referVehicleList = referVehicleList.stream().filter(s -> !vehicleId.contains(s.getVehicleId()))
                    .collect(Collectors.toList());// java 8写法
                /*
                 * ListIterator<RiskEventVehicleConfigForm> listIterator = referVehicleList.listIterator();
                 * while(listIterator.hasNext()){ RiskEventVehicleConfigForm referVehicle = listIterator.next();
                 * if(vehicleId.contains(referVehicle.getVehicleId())){ //listIterator.remove(); }; }
                 */
                // 根据id查询车辆

                String settingListJsonStr = JSON.toJSONString(settingList);
                String riskLevelListStr = JSON.toJSONString(riskLevelList);
                String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);
                mav.addObject("riskSettingList", JSON.parseArray(settingListJsonStr));
                mav.addObject("vehicleId", vehicleId);
                mav.addObject("brand", brands);
                mav.addObject("riskLevelList", JSON.parseArray(riskLevelListStr));
                mav.addObject("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
                return mav;
            }
        } catch (Exception e) {
            log.error("风险定义设置弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * true下发ADAS修改参数，flase下发DSM修改参数
     * 修正
     * @param vehicleId
     * @param checkedParams
     * @return
     */
    @RequestMapping(value = "/settingFlag.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateADSetting(@RequestParam("id") final String vehicleId,
        @RequestParam("checkedParams") final String checkedParams, boolean flag) {
        try {
            List<AdasRiskEventVehicleConfigForm> paramSettings =
                JSON.parseArray(checkedParams).toJavaList(AdasRiskEventVehicleConfigForm.class);
            List<AdasRiskEventVehicleConfigForm> dbSettings;
            if (flag) {
                dbSettings = adasRiskEventConfigService.findAdasRiskSettingByVid(vehicleId);
            } else {
                dbSettings = adasRiskEventConfigService.findDsmRiskSettingByVid(vehicleId);
            }
            for (AdasRiskEventVehicleConfigForm paramSetting : paramSettings) {
                for (AdasRiskEventVehicleConfigForm dbSetting : dbSettings) {
                    if (paramSetting.getRiskId().equals(dbSetting.getRiskId())) {
                        MyBeanUtils.copyNotNullProperties(paramSetting, dbSetting);
                        break;
                    }
                }
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            adasRiskEventConfigService.updateADRiskSetting(vehicleId, dbSettings, ipAddress, flag);
            return new JsonResultBean(true);
        } catch (Exception e) {
            log.error("修改ADAS或DSM外设参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @RequestMapping(value = "/setting.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateResourcesByRole(@RequestParam("id") final String vehicleIds,
        @RequestParam("checkedParams") final String checkedParams) {
        try {
            if (!"".equals(vehicleIds)) {
                List<AdasRiskEventVehicleConfigForm> list = new ArrayList<>();
                JSONArray jsonArray;
                if (StringUtils.isNotBlank(checkedParams)) {
                    jsonArray = JSON.parseArray(checkedParams);
                    list = jsonArray.toJavaList(AdasRiskEventVehicleConfigForm.class);
                    if (isErrorResolution(list)) {
                        return new JsonResultBean(JsonResultBean.FAULT, resolutionParamError);
                    }
                }
                if (StringUtils.isBlank(vehicleIds)) {
                    return new JsonResultBean(JsonResultBean.FAULT, vehicleNull);
                }
                String[] item = vehicleIds.split(",");
                List<String> ids = new ArrayList<>();
                if (item != null && item.length > 0) {
                    ids = Arrays.asList(item);
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                adasRiskEventConfigService.updateRiskSettingByBatch(ids, list, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量修改风险定义设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    private boolean isErrorResolution(List<AdasRiskEventVehicleConfigForm> list) {
        // 校验分辨率
        List<AdasRiskEventVehicleConfigForm> errorDatas = list.stream().filter(
            s -> !Arrays.asList(resolutionArray).contains(s.getCameraResolution()) || !Arrays.asList(resolutionArray)
                .contains(s.getVideoResolution())).collect(Collectors.toList());
        return errorDatas.size() > 0;
    }

    /**
     * 根据车辆id查询车辆的风险设置
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = "/getRiskSetting_{vehicleId}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRiskSetting(@PathVariable("vehicleId") String vehicleId) {
        try {
            // 风险事件设置
            List<AdasRiskEventVehicleConfigForm> settingList =
                adasRiskEventConfigService.findRiskSettingByVid(vehicleId);
            return new JsonResultBean(settingList);
        } catch (Exception e) {
            log.error("根据车id查询风险设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除风险设置
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (!"".equals(id)) {
                List<String> vehicleIds = new ArrayList<>();
                vehicleIds.add(id);
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                adasRiskEventConfigService.deleteRiskSettingByVehicleIds(vehicleIds, ipAddress, "1");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除风险设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String[] item = items.split(",");
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                List<String> ids = Arrays.asList(item);
                adasRiskEventConfigService.deleteRiskSettingByVehicleIds(ids, ip, "1");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除风险设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 参数下发
     * @param vehicleIds
     */
    @RequestMapping(value = "/sendParamSet", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParamSet(String vehicleIds) {
        try {
            if (!"".equals(vehicleIds)) {
                String[] vids = vehicleIds.split(",");
                List<String> vehIds = new ArrayList<>();
                for (String vid : vids) {
                    if (vid != null && !"".equals(vid)) {
                        VehicleDTO vehicleDTO = vehicleService.getById(vid);
                        if (vehicleDTO == null) {
                            continue;
                        }
                        vehIds.add(vid);
                    }
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                adasRiskEventConfigService.sendParamSet(vehIds, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }

        } catch (Exception ex) {
            log.error("下发参数设置异常", ex);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取外设信息
     * @param vid         车辆id
     * @param commandType
     * @param sensorID
     * @return
     */
    @RequestMapping(value = "/getPeripheralInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeripheralInfo(String vid, Integer commandType, Integer sensorID) {
        try {
            if (vid != null && !vid.isEmpty() && commandType != null && sensorID != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return adasRiskEventConfigService
                    .sendPInfo(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType), ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取外设信息失败");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 远程升级
     * @param wirelessParam
     * @param vehicleId
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = { "/saveWirelessUp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(WirelessUpdateParam wirelessParam, String vehicleId, final BindingResult bindingResult) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        if (wirelessParam.getFirmwareVersion() != null && wirelessParam.getFirmwareVersion().length() > 100) {
            return new JsonResultBean(JsonResultBean.FAULT, "对不起，固件版本长度超过100，请确认后重新输入");
        }
        try {
            Integer commandType = 13141;
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return f3OilVehicleSettingService.updateWirelessUp(wirelessParam, vehicleId, commandType, ipAddress);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
