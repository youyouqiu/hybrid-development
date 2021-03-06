package com.zw.adas.controller.realTimeMonitoring;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventAlarmForm;
import com.zw.adas.service.realTimeMonitoring.AdasRealTimeMonitoringService;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.service.realTimeVideo.RealTimeVideoService;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/adas/v/monitoring")
public class AdasRealTimeMonitoringController {

    private static final Logger log = LogManager.getLogger(AdasRealTimeMonitoringController.class);

    @Autowired
    AdasRealTimeMonitoringService adasRealTimeMonitoringService;

    @Autowired
    RealTimeVideoService realTimeVideoService;

    @Autowired
    VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    ProfessionalService professionalService;

    private static final String INDEX_PAGE = "vas/adas/monitoring/realTimeMonitoring";

    /**
     * ????????????????????????????????????
     */
    @RequestMapping(value = { "/getRiskList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRiskList(int pageNum, int pageSize, String riskIds, String eventField) {
        return AdasControllerTemplate
            .getResultBean(() -> adasRealTimeMonitoringService.listRisks(pageNum, pageSize, riskIds, eventField),
                "????????????????????????!");
    }

    /**
     * ????????????????????????????????????
     * @param eventFields ??????????????????
     */
    @RequestMapping(value = { "/getEventCountByEventFields" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getEventCountByEventField(String eventFields) {
        return AdasControllerTemplate
            .getResultBean(() -> adasRealTimeMonitoringService.getEventCountByEventFields(eventFields),
                "???????????????????????????????????????????????????");
    }

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView index(String id) {
        ModelAndView mv = new ModelAndView(INDEX_PAGE);
        mv.addObject("jumpId", id);
        return mv;
    }

    /**
     * ??????????????????????????????????????????
     */
    @RequestMapping(value = { "/getRiskEventByRiskId" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRiskEventByRiskId(String riskId) {
        try {
            if (riskId != null) {
                List<AdasRiskEventAlarmForm> list = adasRealTimeMonitoringService.getRiskEvents(riskId);
                if (list != null) {
                    return new JsonResultBean(list);
                }
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("??????????????????????????????!", e);
            return new JsonResultBean(false);
        }
    }

    /**
     * ???????????????????????????????????????
     */
    @RequestMapping(value = { "/getRiskProfessionalsInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRiskProfessionalsInfo(String vehicleId) {
        try {
            if (vehicleId != null) {
                List<ProfessionalDTO> professionalDTOS = professionalService.getRiskProfessionalsInfo(vehicleId);
                if (professionalDTOS != null) {
                    return new JsonResultBean(professionalDTOS);
                }
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("??????????????????????????????!" + vehicleId, e);
            return new JsonResultBean(false);
        }
    }

    /**
     * ?????????????????????????????????
     */
    @RequestMapping(value = { "/getMediaInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMediaInfo(String riskId) {
        try {
            if (riskId != null) {
                JsonResultBean jsonResultBean = adasRealTimeMonitoringService.getMediaInfo(riskId);
                if (jsonResultBean != null) {
                    return jsonResultBean;
                }
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("??????????????????????????????!", e);
            return new JsonResultBean(false);
        }
    }

    /**
     * ??????????????????TTS??????
     */
    @RequestMapping(value = "/sendTTS", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendTTS(HttpServletRequest request, String sendTextContent, String vehicleId) {
        try {
            return realTimeVideoService.sendTtsByBatch(sendTextContent, vehicleId, IPAddrUtil.getClientIp(request));
        } catch (Exception e) {
            log.error("TTS??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    // /**
    //  * ??????????????????????????????????????????
    //  */
    // @RequestMapping(value = "/talkBack", method = RequestMethod.POST)
    // @ResponseBody
    // public JsonResultBean sendTalkBack(HttpServletRequest request, VideoSendForm videoSendForm, String riskNumber) {
    //     try {
    //         if (videoSendForm != null) {
    //             // ??????????????????IP??????
    //             String ipAddress = new GetIpAddr().getIpAddr(request);
    //             String userName = SystemHelper.getCurrentUsername();
    //             Map<String, Object> re =
    //                 adasRealTimeMonitoringService.sendTalkBack(ipAddress, userName, videoSendForm, riskNumber);
    //             return new JsonResultBean(re);
    //         } else {
    //             return new JsonResultBean(JsonResultBean.FAULT, "????????????!");
    //         }
    //     } catch (Exception e) {
    //         log.error("??????????????????????????????????????????", e);
    //         return new JsonResultBean(JsonResultBean.FAULT);
    //     }
    // }

    // /**
    //  * ????????????????????????????????????????????????
    //  */
    // @RequestMapping(value = "/endTalkBack", method = RequestMethod.POST)
    // @ResponseBody
    // public JsonResultBean sendEndTalkBack(HttpServletRequest request, AudioVideoRransmitForm audioVideoRransmitForm,
    //     String talkStartTime, String warningTime, String riskId) {
    //     try {
    //         if (audioVideoRransmitForm != null) {
    //             // ??????????????????IP??????
    //             String ipAddress = new GetIpAddr().getIpAddr(request);
    //             String userName = SystemHelper.getCurrentUsername();
    //             audioVideoRransmitForm.setUserName(userName);
    //             boolean re = adasRealTimeMonitoringService
    //                 .sendEndTalkBack(ipAddress, audioVideoRransmitForm, talkStartTime, warningTime, riskId);
    //             return new JsonResultBean(re);
    //         } else {
    //             return new JsonResultBean(JsonResultBean.FAULT, "????????????!");
    //         }
    //     } catch (Exception e) {
    //         log.error("??????????????????????????????????????????????????????", e);
    //         return new JsonResultBean(JsonResultBean.FAULT);
    //     }
    // }

    /**
     * ????????????9208
     */
    @RequestMapping(value = { "/getAdasMedia" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean send9208(String riskEventId, String vehicleId) {
        try {
            if (riskEventId != null && vehicleId != null) {
                return adasRealTimeMonitoringService.send9208(riskEventId, vehicleId);
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("????????????9208????????????????????????!", e);
            return new JsonResultBean(false);
        }
    }

    /**
     * ?????????????????????8801?????????
     */
    @RequestMapping(value = { "/faceMatch/photograph" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean send8801(String vehicleId, String icMediaUrl,
        @RequestParam(name = "brand", required = false) String brand) {
        try {
            if (!adasRealTimeMonitoringService.checkIcPhoto(vehicleId, icMediaUrl)) {
                return new JsonResultBean(JsonResultBean.FAULT, "??????ic?????????????????????");
            }
            if (!MonitorUtils.isOnLine(vehicleId)) {
                return new JsonResultBean(JsonResultBean.FAULT, brand + "?????????????????????");
            }
            Photograph photograph;
            if (vehicleId != null) {
                String userPhotoParam = RedisHelper.getString(
                    HistoryRedisKeyEnum.INTELLIGENCE_PHOTO_PARAM_SETTING.of(SystemHelper.getCurrentUsername()));
                if (StringUtils.isNotBlank(userPhotoParam)) {
                    photograph = JSONObject.parseObject(userPhotoParam, Photograph.class);
                } else {
                    photograph = new Photograph();
                    photograph.setChroma(125);
                    photograph.setCommand(1);
                    photograph.setContrast(127);
                    photograph.setDistinguishability(1);
                    photograph.setLuminance(125);
                    photograph.setQuality(5);
                    photograph.setSaturability(127);
                    photograph.setWayID(0x65);
                    photograph.setSaveSign(0);
                    photograph.setTime(0);
                }
                return adasRealTimeMonitoringService.send8801(vehicleId, photograph);
            }
            return new JsonResultBean(false, "????????????????????????");
        } catch (Exception e) {
            log.error("??????????????????8801??????!", e);
            return new JsonResultBean(false, "????????????????????????");
        }
    }

    /**
     * ??????????????????????????????????????????
     */
    @RequestMapping(value = { "/checkVehicle" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkVehicle(String vehicleId, String brand) {
        try {
            if (vehicleId == null) {
                return null;
            }
            if (!MonitorUtils.isOnLine(vehicleId)) {
                return new JsonResultBean(JsonResultBean.FAULT, brand + "?????????????????????");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????????????????????????????!", e);
            return new JsonResultBean(false, "????????????????????????????????????????????????");
        }
    }

    /**
     * ???????????????????????????api?????????
     */
    @RequestMapping(value = { "/faceMatch" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean faceMatch(String vehicleId, String address, String mediaUrl, String icMediaUrl) {
        try {
            if (vehicleId != null) {
                return adasRealTimeMonitoringService.faceMatch(vehicleId, address, mediaUrl, icMediaUrl);
            }
            return new JsonResultBean(false, "????????????");
        } catch (Exception e) {
            log.error("??????????????????8801??????!", e);
            return new JsonResultBean(false, "????????????????????????");
        }
    }

}
