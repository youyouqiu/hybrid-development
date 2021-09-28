package com.zw.platform.controller.realTimeVideo;


import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zw.platform.domain.realTimeVideo.CloudTerraceForm;
import com.zw.platform.service.realTimeVideo.CloudTerraceService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;


/**
 * 云台
 */
@Controller
@RequestMapping("/cloudTerrace")
public class CloudTerraceController {
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static Logger log = LogManager.getLogger(CloudTerraceController.class);

    @Autowired
    CloudTerraceService cloudTerraceService;

    /**
     * 云台指令下发
     * @author zjc
     * @version 创建时间：2018年1月3日
     * @param form
     * @return
     */
    @RequestMapping(value = {"/sendParam"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParam(CloudTerraceForm form, HttpServletRequest request) {
        try {
            if (form != null) {
                // 获取客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                cloudTerraceService.sendParam(form,ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("云台指令下发异常:" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
