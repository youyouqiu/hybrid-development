package com.zw.adas.controller.defineSetting;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zw.adas.service.defineSetting.AdasActiveSafetyService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;

/**
 * 主动安全参数设置
 */
@Controller
@RequestMapping("/adas/paramSetting")
public class AdasActiveSafetyController {
    private static Logger log = LogManager.getLogger(AdasActiveSafetyController.class);

    @Autowired
    private AdasActiveSafetyService adasActiveSafetyService;

    @RequestMapping(value = {"/sendParameter"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParameter(String vehicleIds, HttpServletRequest request) {
        String ipAddress = new GetIpAddr().getIpAddr(request);
        try {
            adasActiveSafetyService.sendParamSet(vehicleIds, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("发送参数失败了" + e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    @RequestMapping(value = {"/sendAdasParameter"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendAdasParamter(String vehicleId, HttpServletRequest request) {
        String ipAddress = new GetIpAddr().getIpAddr(request);
        try {
            return new JsonResultBean(adasActiveSafetyService.sendAdasParamter(vehicleId, ipAddress));
        } catch (Exception e) {
            log.error("发送参数失败了" + e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    @RequestMapping(value = {"/sendDsmParameter"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendDsmParamter(String vehicleId, HttpServletRequest request) {
        String ipAddress = new GetIpAddr().getIpAddr(request);
        try {
            return new JsonResultBean(adasActiveSafetyService.sendDsmParamter(vehicleId, ipAddress));
        } catch (Exception e) {
            log.error("发送参数失败了" + e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }
}
