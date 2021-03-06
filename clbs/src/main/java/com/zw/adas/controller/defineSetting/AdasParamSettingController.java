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
 * ???????????????????????????
 * @author gfw
 * @version 1.0
 * @since 2019/6/6 15:49
 **/
@Controller
@RequestMapping("/adas/standard/param")
public class AdasParamSettingController {
    private static final Logger log = LogManager.getLogger(AdasParamSettingController.class);

    /**
     * ??????????????????????????????
     */
    private static final String BEI_JING_TERMINAL_PAGE = "risk/riskManagement/DefineSettings/terminalParameters";
    /**
     * ????????????
     */
    private static final String UPGRADE_PAGE = "risk/riskManagement/DefineSettings/jiUpgrade";
    /**
     * ????????????(????????????)
     */
    private static final String ZW_UPGRADE_PAGE = "risk/riskManagement/DefineSettings/zwUpgrade";
    /**
     * ????????????
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
     * ????????????
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getParamList(AdasRiskParamQuery adasRiskParamQuery) {
        try {
            Page<AdasSettingListDo> result = adasParamSettingService.selectParamByCondition(adasRiskParamQuery);
            return new PageGridBean(adasRiskParamQuery, result, true);
        } catch (Exception e) {
            log.error("??????????????????", e);
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
     * ??????/????????????/??????????????????
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
                out.println("layer.msg('????????????????????????');");
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
            // ????????????
            mav.addObject("tireModel", JSON.parseArray(JSON.toJSONString(tireModel)));
            // ???id
            mav.addObject("vehicleId", vehicleId);
            // ??????
            mav.addObject("brand", brands);
            // ????????????
            mav.addObject("referVehicleList", JSON.parseArray(JSON.toJSONString(referVehicle)));
            if (!vehicleId.contains(",")) {
                String settingParam;
                if (ProtocolTypeUtil.JING_PROTOCOL_808_2019.equals(String.valueOf(type))) {
                    List<AdasJingParamSetting> vehicleParam =
                        adasParamSettingService.findJingParamByVehicleId(vehicleId);
                    settingParam = JSON.toJSONString(vehicleParam);
                } else {
                    // ???????????????????????????(?????????)
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
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = { "/setting.gsp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateParam(AdasParamRequestDTO requestDTO) {
        return ControllerTemplate
            .getResultBean(() -> adasParamSettingService.updateParamByVehicleId(requestDTO), "??????????????????");
    }

    @RequestMapping(value = { "/getStatus" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getStatus(String vehicleId, Integer protocolType, String paramTypes) {
        try {
            return new JsonResultBean(adasParamSettingService.getStatus(vehicleId, protocolType, paramTypes));
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????");
        }
    }

    /**
     * ??????????????????id ???????????????????????????
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????");
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "batch/config", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertParamSetting(AdasParamRequestDTO requestDTO) {
        return ControllerTemplate.getResultBean(() -> adasParamSettingService.addAndSendParam(requestDTO), "????????????????????????");
    }

    /**
     * ??????id??????????????????
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (!"".equals(id)) {
                List<String> vehicleIds = new ArrayList<>();
                vehicleIds.add(id);
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                adasParamSettingService.deleteRiskVehicleIds(vehicleIds, ipAddress, "1");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String[] item = items.split(",");
                // ????????????ip
                String ip = new GetIpAddr().getIpAddr(request);
                List<String> ids = Arrays.asList(item);
                adasParamSettingService.deleteRiskVehicleIds(ids, ip, "1");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
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
                    // ????????????ip
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
            log.error("????????????????????????", ex);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????8900???
     * @param vehicleId   ??????id
     * @param commandType ????????????  247???f7????????????????????????248???f8??????????????????????????????
     * @param sensorID    ??????id  100???0x64????????????????????????101???0x65?????????????????????????????? 102???0x66??????????????????103???0x67???????????????
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
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????????????????
     */
    @RequestMapping(value = { "/readPerInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getPerInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand, String protocolType) {
        String page = AdasReadPeripheralMsgPageEnum.getPage(protocolType);
        if (StringUtils.isBlank(page)) {
            log.error("????????????????????????,?????????????????????????????????");
            return new ModelAndView(ERROR_PAGE);
        }
        ModelAndView mav = new ModelAndView(page);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = { "/readPerState_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getPerState(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand, String protocolType) {
        String page = AdasReadPeripheralStatePageEnum.getPage(protocolType);
        if (StringUtils.isBlank(page)) {
            log.error("????????????????????????,??????????????????????????????");
            return new ModelAndView(ERROR_PAGE);
        }
        ModelAndView mav = new ModelAndView(page);
        try {
            mav.addObject("vehicleid", vehicleId);
            mav.addObject("brand", brand);
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = { "/readParamInfo_{vehicleId}_{brand}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getAdasInfo(@PathVariable("vehicleId") final String vehicleId,
        @PathVariable("brand") final String brand, String protocolType) {
        String page = AdasReadParamPageEnum.getPage(protocolType);
        if (StringUtils.isBlank(page)) {
            log.error("????????????????????????,????????????????????????????????????");
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
                // ???????????????????????????(?????????)
                List<AdasParamSettingForm> vehicleParam = adasParamSettingService.findParamByVehicleId(vehicleId);
                settingParam = JSON.toJSONString(vehicleParam);
            }
            mav.addObject("settingParam", JSON.parseArray(settingParam));
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = { "/saveJiWirelessUp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(WirelessUpdateParam wirelessParam, String vehicleId, final BindingResult bindingResult) {
        // ????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        if (wirelessParam.getFirmwareVersion() != null && wirelessParam.getFirmwareVersion().length() > 100) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????100???????????????????????????");
        }
        try {
            Integer commandType = 13141;
            String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 3);
        } catch (Exception e) {
            log.error("?????????????????????" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????(????????????2019)
     */
    @RequestMapping(value = { "/saveZhongWeiWirelessUp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveZhongWeiWirelessUp(WirelessUpdateParam wirelessParam, String vehicleId,
        final BindingResult bindingResult) {
        // ????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        if (wirelessParam.getFirmwareVersion() != null && wirelessParam.getFirmwareVersion().length() > 100) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????100???????????????????????????");
        }
        try {
            Integer commandType = 13141;
            String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
            adasParamSettingService.maintenanceRemoteUpgradeCache(vehicleId);//??????????????????????????????
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 3);
        } catch (Exception e) {
            log.error("?????????????????????" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }
    //*************************************************???????????????********************************

    /**
     * ???????????????????????????????????????
     */
    @RequestMapping(value = "batch/upsert", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertJingParamSetting(@RequestParam(value = "vehicleIds") String vehicleIds,
        String alarmParam, String platformParam, @RequestParam(value = "sendFlag") boolean sendFlag) {
        try {
            String[] split = vehicleIds.split(",");
            if (split.length == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, "????????????");
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
    }

    /**
     * ??????????????????id ???????????????????????????(??????)
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????");
        }
    }

    /**
     * ????????????
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
                    // ????????????ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    adasParamSettingService.sendJingParamSet(vehIds, ipAddress);
                }
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }

        } catch (Exception ex) {
            log.error("????????????????????????", ex);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @param paramType 51 ???????????????????????????????????????????????? 52 ??????????????????????????????????????????????????????  53 ??????????????????????????????
     */
    @RequestMapping(value = "/getJingPeripheralInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeripheralInfo(String vid, Integer paramType) {
        try {
            //51 ???????????????????????????????????????????????? 52 ??????????????????????????????????????????????????????  53 ??????????????????????????????
            if (vid != null && !vid.isEmpty() && paramType != null && (paramType == 51 || paramType == 52
                || paramType == 53)) {
                return adasParamSettingService.sendPInfo(vid, paramType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
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
            log.error("????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

}
