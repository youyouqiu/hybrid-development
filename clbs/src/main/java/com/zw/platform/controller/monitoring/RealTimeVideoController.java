/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.controller.monitoring;

import com.zw.platform.domain.sendTxt.SendTextParam;
import com.zw.platform.domain.sendTxt.SendTxt;
import com.zw.platform.service.realTimeVideo.RealTimeVideoService;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;


/**
 * 实时视频Controller <p>Title: RealTimeVideoController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 */
@Controller
@RequestMapping("/v/monitoring")
public class RealTimeVideoController {
    /**
     * log日志记录
     */
    private static Logger log = LogManager.getLogger(RealTimeVideoController.class);

    @Autowired
    private RealTimeVideoService realTimeVideoService;

    private static final String INIT_PAGE = "vas/monitoring/realTimeVideo/realTimeVideo";

    private static final String SEND_TEXT_BY_BATCH_PAGE = "vas/monitoring/videoRealTime/batchSendTxt";

    /**
     * 实时视频页面
     */
    @RequestMapping(value = {"/realTimeVideo"}, method = RequestMethod.GET)
    public String index() {
        return INIT_PAGE;
    }

    /**
     * 批量下发文本页面
     */
    @RequestMapping(value = {"/getSendTextByBatchPage_{deviceType}"}, method = RequestMethod.GET)
    public ModelAndView getSendTextByBatchPage(@PathVariable final String deviceType) {
        ModelAndView mav = new ModelAndView(SEND_TEXT_BY_BATCH_PAGE);
        mav.addObject("deviceType", deviceType);
        return mav;
    }

    /**
     * 批量下发文本
     */
    @RequestMapping(value = {"/sendTextByBatch"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendTextByBatch(HttpServletRequest request, String sendTextContent, String vehicleIds,
        String marks) {
        try {
            if (StringUtil.areNotBlank(sendTextContent, vehicleIds)) {
                List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
                if (CollectionUtils.isNotEmpty(vehicleIdList)) {
                    String ipAddress = IPAddrUtil.getClientIp(request);
                    return realTimeVideoService.sendTextByBatch(SendTxt.getSendTxt2013(sendTextContent, marks),
                        vehicleIdList, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量下发文本异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量下发文本（2019）
     */
    @RequestMapping(value = {"/sendTextByBatch2019"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendTextByBatch2019(HttpServletRequest request, SendTextParam sendTextParam) {
        try {
            String vehicleIds = sendTextParam.getVehicleIds();
            if (StringUtil.areNotBlank(sendTextParam.getSendTextContent(), sendTextParam.getVehicleIds())) {
                List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
                if (CollectionUtils.isNotEmpty(vehicleIdList)) {
                    return realTimeVideoService.sendTextByBatch(SendTxt.getSendTxt2019(sendTextParam), vehicleIdList,
                        IPAddrUtil.getClientIp(request));
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量下发文本异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获得下发文本信息状态列表
     */
    @RequestMapping(value = {"/getSendTextStatusList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSendTextStatusList(String vehicleIds) {
        try {
            return realTimeVideoService.getSendTextStatusList(vehicleIds);
        } catch (Exception e) {
            log.error("获得下发文本信息状态列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获得io信息
     *
     * @param type      terminalIo:终端io; sensorIo:传感器io
     */
    @RequestMapping(value = {"/getIoSignalInfo"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getIoSignalInfo(String monitorId, String type) {
        try {
            return realTimeVideoService.getIoSignalInfo(monitorId, type);
        } catch (Exception e) {
            log.error("获得io信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/audioAndVideoParameters/{monitorId}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAudioAndVideoParameters(@PathVariable("monitorId") String monitorId) {
        try {
            return realTimeVideoService.getAudioAndVideoParameters(monitorId);
        } catch (Exception e) {
            log.error("获取音视频参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}
