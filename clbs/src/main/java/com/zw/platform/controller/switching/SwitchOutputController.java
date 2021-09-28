package com.zw.platform.controller.switching;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.alram.OutputControl;
import com.zw.platform.service.switching.SwitchOutputService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 @Author lijie
 @Date 2020/5/7 10:20
 @Description 输出控制
 @version 1.0
 **/
@Controller
@RequestMapping("/m/switching/outputControl")
public class SwitchOutputController {

    private static Logger log = LogManager.getLogger(SwitchOutputController.class);

    private static final String LIST_PAGE = "vas/switching/outputControl/list";

    @Autowired
    private SwitchOutputService switchOutputService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @ResponseBody
    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public PageGridBean pageList(final SensorConfigQuery query) {
        try {
            return switchOutputService.pageList(query);
        } catch (Exception e) {
            log.error("输出控制分页查询异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 下发8500车辆输出控制
     * @return
     */
    @RequestMapping(value = { "/send8500" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean send8500(OutputControl outputControl) {
        try {
            if (!MonitorUtils.isOnLine(outputControl.getVehicleId())) {
                return new JsonResultBean(JsonResultBean.FAULT,  "该监控对象未在线");
            }
            return switchOutputService.send8500(outputControl);
        } catch (Exception e) {
            log.error("下发输出控制异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 关闭车辆的输出控制
     * @return
     */
    @RequestMapping(value = { "/close" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean close(String vehicleId, String protocolType) {
        try {
            if (!MonitorUtils.isOnLine(vehicleId)) {
                return new JsonResultBean(JsonResultBean.FAULT,  "该监控对象未在线");
            }
            return switchOutputService.sendCloseOutputControl(vehicleId, protocolType);
        } catch (Exception e) {
            log.error("下发关闭输出控制异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }




}
