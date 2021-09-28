package com.zw.adas.controller.defineSetting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.base.Joiner;
import com.zw.adas.domain.define.enumcontant.AdasReadParamPageEnum;
import com.zw.adas.domain.define.enumcontant.AdasReadPeripheralMsgPageEnum;
import com.zw.adas.domain.define.enumcontant.AdasReadPeripheralStatePageEnum;
import com.zw.adas.domain.define.enumcontant.AdasSettingPageEnum;
import com.zw.adas.domain.define.setting.AdasJingParamSetting;
import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.adas.domain.define.setting.AdasSettingListDo;
import com.zw.adas.domain.define.setting.dto.AdasParamRequestDTO;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.adas.domain.define.setting.query.AdasRiskParamQuery;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasCommonParamSettingDao;
import com.zw.adas.service.defineSetting.AdasParamSettingService;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.spring.InitData;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.talkback.common.ControllerTemplate;
import com.zw.ws.entity.t808.parameter.ParamItem;
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
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 川冀表参数设置管理
 * @author gfw
 * @version 1.0
 * @since 2019/6/6 15:49
 **/
@Controller
@RequestMapping("/adas/standard/param")
public class AdasParamSettingController {
    private static final Logger log = LogManager.getLogger(AdasParamSettingController.class);

    /**
     * 读取终端参数（京标）
     */
    private static final String BEI_JING_TERMINAL_PAGE = "risk/riskManagement/DefineSettings/terminalParameters";
    /**
     * 远程升级
     */
    private static final String UPGRADE_PAGE = "risk/riskManagement/DefineSettings/jiUpgrade";
    /**
     * 远程升级(中位标准)
     */
    private static final String ZW_UPGRADE_PAGE = "risk/riskManagement/DefineSettings/zwUpgrade";
    /**
     * 错误页面
     */
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    AdasParamSettingService adasParamSettingService;
    @Autowired
    AdasCommonParamSettingDao commonParamDao;
    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private HttpServletRequest request;
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 条件搜索
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getParamList(AdasRiskParamQuery adasRiskParamQuery) {
        try {
            Page<AdasSettingListDo> result = adasParamSettingService.selectParamByCondition(adasRiskParamQuery);
            return new PageGridBean(adasRiskParamQuery, result, true);
        } catch (Exception e) {
            log.error("条件搜索出错", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/getAutoCache", method = RequestMethod.GET)
    @ResponseBody
    public Object getAutoCache(String vid) {
        if (StrUtil.isNotBlank(vid)) {
            return InitData.automaticVehicleMap.get(vid);
        }
        return InitData.automaticVehicleMap;
    }

    @RequestMapping(value = "/getPlatCache", method = RequestMethod.GET)
    @ResponseBody
    public Object getPlatCache(String vid) {
        if (StrUtil.isNotBlank(vid)) {
            return InitData.platformParamMap.get(vid);
        }
        return InitData.platformParamMap;
    }

    /**
     * 新增/批量设置/修改页面弹出
     */
    @RequestMapping(value = { "/setting_{type}_{vehicleId}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("type") final Integer type, HttpServletResponse response) {

        ModelAndView mav = new ModelAndView(AdasSettingPageEnum.getPage(String.valueOf(type)));
        try {
            if (StringUtils.isBlank(vehicleId)) {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('请选择一条数据！');");
                out.println("</script>");
                return null;
            }
            String[] split = vehicleId.split(",");
            List<String> vehicleIds = Arrays.asList(split);
            List<String> brandsByIds = vehicleService.findBrandsByIds((vehicleIds));
            String brands = "";
            if (brandsByIds != null && brandsByIds.size() != 0) {
                brands = Joiner.on(",").join(brandsByIds);
            }
            List<Map<String, Object>> referVehicle = adasParamSettingService.findReferVehicle(type);
            List<Map<String, String>> tireModel = adasParamSettingService.findAllTireModel();
            // 轮胎型号
            mav.addObject("tireModel", JSON.parseArray(JSON.toJSONString(tireModel)));
            // 车id
            mav.addObject("vehicleId", vehicleId);
            // 车牌
            mav.addObject("brand", brands);
            // 参考对象
            mav.addObject("referVehicleList", JSON.parseArray(JSON.toJSONString(referVehicle)));
            if (!vehicleId.contains(",")) {
                String settingParam;
                if (ProtocolTypeUtil.JING_PROTOCOL_808_2019.equals(String.valueOf(type))) {
                    List<AdasJingParamSetting> vehicleParam =
                        adasParamSettingService.findJingParamByVehicleId(vehicleId);
                    settingParam = JSON.toJSONString(vehicleParam);
                } else {
                    // 当前监控对象的参数(非京标)
                    List<AdasParamSettingForm> vehicleParam = adasParamSettingService.findParamByVehicleId(vehicleId);
                    settingParam = JSON.toJSONString(vehicleParam);
                }
                mav.addObject("settingParam", JSON.parseArray(settingParam));
                List<AdasPlatformParamSetting> platformParamSettings =
                    adasParamSettingService.findPlatformParamByVehicleId(vehicleId);
                mav.addObject("platformParamSetting", JSON.parseArray(JSON.toJSONString(platformParamSettings)));

            } else {
                mav.addObject("settingParam", null);
                mav.addObject("platformParamSetting", null);
            }
            if (ProtocolTypeUtil.ZW_PROTOCOL_808_2019.equals(String.valueOf(type))) {
                mav.addObject("logicChannels", adasParamSettingService.findLogicChannelsByVehicleId(vehicleIds));
            }
            return mav;
        } catch (Exception e) {
            log.error("风险定义设置弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改绑定参数
     */
    @RequestMapping(value = { "/setting.gsp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateParam(AdasParamRequestDTO requestDTO) {
        return ControllerTemplate
            .getResultBean(() -> adasParamSettingService.updateParamByVehicleId(requestDTO), "修改参数出错");
    }

    @RequestMapping(value = { "/getStatus" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getStatus(String vehicleId, Integer protocolType, String paramTypes) {
        try {
            return new JsonResultBean(adasParamSettingService.getStatus(vehicleId, protocolType, paramTypes));
        } catch (Exception e) {
            log.error("获取下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取下发状态异常");
        }
    }

    /**
     * 根据监控对象id 获取对应的参数信息
     */
    @RequestMapping(value = { "/get_{vehicleId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable("vehicleId") final String vehicleId) {
        try {
            List<AdasParamSettingForm> vehicleParam = adasParamSettingService.findParamByVehicleId(vehicleId);
            List<AdasPlatformParamSetting> platformParam =
                adasParamSettingService.findPlatformParamByVehicleId(vehicleId);
            JSONObject result = new JSONObject();
            result.put("alarmParam", vehicleParam);
            result.put("platformParam", platformParam);
            return new JsonResultBean(JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("获取参数信息出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取参数信息出错");
        }
    }

    /**
     * 根据参数绑定设备
     */
    @RequestMapping(value = "batch/config", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertParamSetting(AdasParamRequestDTO requestDTO) {
        return ControllerTemplate.getResultBean(() -> adasParamSettingService.addAndSendParam(requestDTO), "新增参数设置出错");
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
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                adasParamSettingService.deleteRiskVehicleIds(vehicleIds, ipAddress, "1");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("恢复默认参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String[] item = items.split(",");
                // 获得访问ip
                String ip = new GetIpAddr().getIpAddr(request);
                List<String> ids = Arrays.asList(item);
                adasParamSettingService.deleteRiskVehicleIds(ids, ip, "1");
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
     */
    @RequestMapping(value = "sendParamSet", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParamSet(String vehicleIds) {
        try {
            if (!"".equals(vehicleIds)) {
                List<String> vehIds = new ArrayList<>();
                String[] vids = vehicleIds.split(",");
                for (String vid : vids) {
                    if (vid != null && !"".equals(vid)) {
                        VehicleInfo ss = vehicleService.findVehicleById(vid);
                        if (ss == null) {
                            continue;
                        }
                        vehIds.add(vid);
                    }
                }
                if (vehIds.size() > 0) {
                    // 获得访问ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    Integer protocol = commonParamDao.selectProtocolByVid(vehIds.get(0));
                    LinkedBlockingQueue<Map<String, String>> paramStatusQueue = new LinkedBlockingQueue<>();
                    adasParamSettingService.sendParamSet(vehIds, ipAddress, paramStatusQueue, protocol);
                    if (paramStatusQueue.size() != 0) {
                        adasParamSettingService.processingThreads(paramStatusQueue, protocol);
                    }

                }
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
     * 获取外设基本信息和外设状态（川冀苏桂标下发8900）
     * @param vehicleId   车辆id
     * @param commandType 消息类型  247（f7）外设状态信息和248（f8）外设传感器基本信息
     * @param sensorID    外设id  100（0x64）驾驶辅助设备，101（0x65）驾驶员行为监测设备 102（0x66）胎压监测，103（0x67）盲区监测
     */
    @RequestMapping(value = "/getJiPeripheralInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeripheralInfo(String vehicleId, Integer commandType, Integer sensorID) {
        try {
            if (StringUtils.isNotEmpty(vehicleId) && commandType != null && sensorID != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return adasParamSettingService
                    .sendF3PInfo(vehicleId, Integer.toHexString(sensorID), Integer.toHexString(commandType), ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "参数异常");
        } catch (Exception e) {
            log.error("获取外设信息失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 读取外设信基本信息
     */
    @RequestMapping(value = { "/readPerInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getPerInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand, String protocolType) {
        String page = AdasReadPeripheralMsgPageEnum.getPage(protocolType);
        if (StringUtils.isBlank(page)) {
            log.error("未匹配到对应协议,读取外设信基本信息异常");
            return new ModelAndView(ERROR_PAGE);
        }
        ModelAndView mav = new ModelAndView(page);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("读取外设信基本信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 读取外设状态
     */
    @RequestMapping(value = { "/readPerState_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getPerState(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand, String protocolType) {
        String page = AdasReadPeripheralStatePageEnum.getPage(protocolType);
        if (StringUtils.isBlank(page)) {
            log.error("未匹配到对应协议,读取外设状态页面异常");
            return new ModelAndView(ERROR_PAGE);
        }
        ModelAndView mav = new ModelAndView(page);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("读取外设状态异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 读取外设参数设置
     */
    @RequestMapping(value = { "/readParamInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getAdasInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand, String protocolType) {
        String page = AdasReadParamPageEnum.getPage(protocolType);
        if (StringUtils.isBlank(page)) {
            log.error("未匹配到对应协议,读取外设参数设置页面异常");
            return new ModelAndView(ERROR_PAGE);
        }

        ModelAndView mav = new ModelAndView(page);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            List<Map<String, String>> tireModel = adasParamSettingService.findAllTireModel();
            Map<String, String> tireModelMap = new HashMap<>();
            for (Map<String, String> map : tireModel) {
                tireModelMap.put(map.get("name"), map.get("tireModelId"));
            }
            mav.addObject("tireModelMap", JSON.toJSONString(tireModelMap));
            String settingParam;
            if (ProtocolTypeUtil.JING_PROTOCOL_808_2019.equals(String.valueOf(protocolType))) {
                List<AdasJingParamSetting> vehicleParam = adasParamSettingService.findJingParamByVehicleId(vehicleId);
                settingParam = JSON.toJSONString(vehicleParam);
            } else {
                // 当前监控对象的参数(非京标)
                List<AdasParamSettingForm> vehicleParam = adasParamSettingService.findParamByVehicleId(vehicleId);
                settingParam = JSON.toJSONString(vehicleParam);
            }
            mav.addObject("settingParam", JSON.parseArray(settingParam));
            return mav;
        } catch (Exception e) {
            log.error("读取外设参数设置异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 远程升级
     */
    @RequestMapping(value = { "/saveJiWirelessUp" }, method = RequestMethod.POST)
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
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 3);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 远程升级页面
     */
    @RequestMapping(value = { "/jiUpgrade_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getUpgradePage(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
        try {
            mav.addObject("vehicleId", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 远程升级(中位标准2019)
     */
    @RequestMapping(value = { "/saveZhongWeiWirelessUp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveZhongWeiWirelessUp(WirelessUpdateParam wirelessParam, String vehicleId,
        final BindingResult bindingResult) {
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
            adasParamSettingService.maintenanceRemoteUpgradeCache(vehicleId);//维护远程升级推送缓存
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 3);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 远程升级页面
     */
    @RequestMapping(value = { "/zwUpgrade_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getZwUpgradePage(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(ZW_UPGRADE_PAGE);
        try {
            mav.addObject("vehicleId", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }
    //*************************************************京标分割线********************************

    /**
     * 批量或单个新增京标参数设置
     */
    @RequestMapping(value = "batch/upsert", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertJingParamSetting(@RequestParam(value = "vehicleIds") String vehicleIds,
        String alarmParam, String platformParam, @RequestParam(value = "sendFlag") boolean sendFlag) {
        try {
            String[] split = vehicleIds.split(",");
            if (split.length == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数出错");
            }
            List<String> list = Arrays.asList(split);
            List<AdasJingParamSetting> adasParamSettingForms = AdasJingParamSetting.convertList(alarmParam);
            List<AdasPlatformParamSetting> platformParamSettings = AdasPlatformParamSetting.convertList(platformParam);
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map<String, Map<Integer, List<ParamItem>>> sendMap = adasParamSettingService
                .insertJingParamSetting(list, adasParamSettingForms, platformParamSettings, sendFlag, ipAddress);
            if (sendFlag && sendMap.size() > 0) {
                adasParamSettingService.sendJing8103(list, sendMap);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("新增参数设置出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, "新增参数出错");
        }
    }

    /**
     * 根据监控对象id 获取对应的参数信息(京标)
     */
    @RequestMapping(value = { "jing/get_{vehicleId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getJingParams(@PathVariable("vehicleId") final String vehicleId) {
        try {
            List<AdasJingParamSetting> vehicleParam = adasParamSettingService.findJingParamByVehicleId(vehicleId);
            List<AdasPlatformParamSetting> platformParam =
                adasParamSettingService.findPlatformParamByVehicleId(vehicleId);
            JSONObject result = new JSONObject();
            result.put("alarmParam", vehicleParam);
            result.put("platformParam", platformParam);
            return new JsonResultBean(JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("获取参数信息出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取参数信息出错");
        }
    }

    /**
     * 参数下发
     */
    @RequestMapping(value = "jing/sendParamSet", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendJingParamSet(String vehicleIds) {
        try {
            if (!"".equals(vehicleIds)) {
                List<String> vehIds = new ArrayList<>();
                String[] vids = vehicleIds.split(",");
                for (String vid : vids) {
                    if (vid != null && !"".equals(vid)) {
                        VehicleInfo ss = vehicleService.findVehicleById(vid);
                        if (ss == null) {
                            continue;
                        }
                        vehIds.add(vid);
                    }
                }
                if (vehIds.size() > 0) {
                    // 获得访问ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    adasParamSettingService.sendJingParamSet(vehIds, ipAddress);
                }
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
     * @param paramType 51 代表京标驾驶行为报警参数设置查询 52 代表京标车辆运行监测报警参数设置查询  53 代表京标终端参数查询
     */
    @RequestMapping(value = "/getJingPeripheralInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeripheralInfo(String vid, Integer paramType) {
        try {
            //51 代表京标驾驶行为报警参数设置查询 52 代表京标车辆运行监测报警参数设置查询  53 代表京标终端参数查询
            if (vid != null && !vid.isEmpty() && paramType != null && (paramType == 51 || paramType == 52
                || paramType == 53)) {
                return adasParamSettingService.sendPInfo(vid, paramType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取外设信息失败");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询终端参数
     */
    @RequestMapping(value = { "/readTerminalInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getTerminalInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand) {
        ModelAndView mav = new ModelAndView(BEI_JING_TERMINAL_PAGE);
        try {
            mav.addObject("vehicleId", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("查询终端参数异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

}
