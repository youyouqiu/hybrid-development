package com.zw.adas.controller.supervise;

import com.zw.adas.domain.riskManagement.form.AdasDealRiskForm;
import com.zw.adas.domain.riskManagement.param.AdasRiskBattleParam;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.service.supersive.AdasRiskManageIntelligenceService;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.platform.basic.util.RedisServiceUtils;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.domain.sendTxt.SendTxt;
import com.zw.platform.service.realTimeVideo.RealTimeVideoService;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 智能安全二期监管用户主动安全Controller
 * @author gfw
 * @version 1.0
 **/
@Controller
@RequestMapping(value = "adas/s/riskManage/intelligence")
public class RiskManageIntelligenceController {
    private static final Logger log = LogManager.getLogger(RiskManageIntelligenceController.class);

    /**
     * 监管用户界面
     */
    private static final String LIST_ADMIN_PAGE = "modules/security/administration/list";

    /**
     * 企业用户界面
     */
    private static final String LIST_ENt_PAGE = "modules/security/enterprise/list";

    @Autowired
    private AdasRiskManageIntelligenceService riskManageIntelligenceService;
    
    @Autowired
    private AdasRiskService adasRiskService;

    @Autowired
    private RealTimeVideoService realTimeVideoService;


    /**
     * 获取行政主动安全页面
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_ADMIN_PAGE;
    }

    /**
     * 获取当前报警分析数据
     * 饼状图和柱状图数据
     */
    @RequestMapping(value = "/alarmAnalysis", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean alarmAnalysis() {
        try {
            return new JsonResultBean(riskManageIntelligenceService.getAlarmAnalysisData());
        } catch (Exception e) {
            log.error("获取当前报警分析数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取当前报警分析数据异常");
        }
    }

    /**
     * 获取企业主动安全页面
     */
    @Auth
    @RequestMapping(value = {"enterprise/list"}, method = RequestMethod.GET)
    public String enterpriseListPage() {
        return LIST_ENt_PAGE;
    }

    /**
     * 获取风险高发时段数据
     */
    @RequestMapping(value = "/alarmTimes", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean alarmTimes() {
        try {
            return new JsonResultBean(riskManageIntelligenceService.getAlarmTimesData());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取监管风险高发时段数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取监管风险高发时段数据异常");
        }
    }

    /**
     * 主动安全页面TTS读播
     */
    @RequestMapping(value = "/sendTTS", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendTTS(HttpServletRequest request, SendTxt sendTxt, String vehicleId) {
        try {
            if (sendTxt != null && StringUtils.isNotBlank(vehicleId)) {
                List<String> vehicleIdList = Arrays.asList(vehicleId.split(","));
                if (CollectionUtils.isNotEmpty(vehicleIdList)) {
                    String ipAddress = IPAddrUtil.getClientIp(request);
                    return realTimeVideoService.sendTextByBatch(sendTxt, vehicleIdList, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("TTS读播下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }


    @RequestMapping(value = "/getRisks", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRisks(AdasRiskBattleParam adasRiskBattleParam) {
        return AdasControllerTemplate.getResultBean(()
            -> riskManageIntelligenceService.getRisks(adasRiskBattleParam), "查询报警列表数据异常");

    }

    @RequestMapping(value = "/getEvents", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getEvents(String riskId) {
        return AdasControllerTemplate.getResultBean(() -> riskManageIntelligenceService.getEvents(riskId),
                "根据风险id查询报警事件列表数据异常");
    }

    @RequestMapping(value = "/getDrivers", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDrivers(String riskId, String vehicleId) {
        return AdasControllerTemplate.getResultBean(() -> riskManageIntelligenceService.getDrivers(riskId, vehicleId),
                "根据风险id查询司机列表数据异常");
    }

    @RequestMapping(value = "/setCurrentUserPhotoParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean setCurrentUserPhotoParam(@Validated Photograph photograph, BindingResult result) {
        if (result.hasErrors()) {
            List<ObjectError> errors = result.getAllErrors();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < errors.size(); i++) {
                if (i != errors.size() - 1) {
                    stringBuilder.append(errors.get(i).getDefaultMessage()).append(",");
                } else {
                    stringBuilder.append(errors.get(i).getDefaultMessage());
                }
            }
            return  AdasControllerTemplate.getResultBean(() -> stringBuilder.toString(),
                "设置风险监管拍照设置报错");
        }
        return AdasControllerTemplate.getResultBean(() -> riskManageIntelligenceService.setPhotoParam(photograph),
            "设置风险监管拍照设置报错");
    }

    @RequestMapping(value = "/getCurrentUserPhotoParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getCurrentUserPhotoParam() {
        String currentUsername = SystemHelper.getCurrentUsername();
        return AdasControllerTemplate.getResultBean(() -> riskManageIntelligenceService.getPhotoParam(currentUsername),
            "获取风险监管拍照设置报错");
    }


    @RequestMapping(value = { "/dealRisk" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean dealRisk(AdasDealRiskForm adasDealRiskForm) {
        try {
            if (RedisServiceUtils.lockRisk(adasDealRiskForm.getRiskId())) {
                String nowStatus = adasRiskService.getRiskStatus(adasDealRiskForm.getRiskId());
                if (nowStatus == null || !nowStatus.equals("6")) {
                    boolean flag = adasRiskService.saveRiskDealInfo(adasDealRiskForm);
                    return new JsonResultBean(flag);
                } else {
                    RedisServiceUtils.releaseRiskLock(adasDealRiskForm.getRiskId());
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT, "不好意思,该报警已被处理了");
        } catch (Exception e) {
            log.error("实时监控页面处理风险异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "风险监控页面处理风险异常!");
        }
    }

}