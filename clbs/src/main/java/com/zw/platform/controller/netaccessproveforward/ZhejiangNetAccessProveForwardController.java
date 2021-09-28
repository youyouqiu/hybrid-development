package com.zw.platform.controller.netaccessproveforward;

import com.zw.platform.commons.Auth;
import com.zw.platform.dto.netaccessproveforward.NetAccessProveForwardVehicleQuery;
import com.zw.platform.service.netaccessproveforward.ZhejiangNetAccessProveForwardService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 浙江入网证明转发
 * @author penghj
 * @version 1.0
 * @date 2020/7/13 11:50
 */
@Controller
@RequestMapping("/m/netAccessProveForward")
public class ZhejiangNetAccessProveForwardController {
    private static Logger logger = LogManager.getLogger(ZhejiangNetAccessProveForwardController.class);

    private static final String LIST_PAGE = "modules/netAccessProveForward/list";

    @Autowired
    private ZhejiangNetAccessProveForwardService zhejiangNetAccessProveForwardService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(NetAccessProveForwardVehicleQuery query) {
        try {
            return zhejiangNetAccessProveForwardService.getList(query);
        } catch (Exception e) {
            logger.error("查询浙江入网证明转发异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addNetAccessProveForward(HttpServletRequest request, String vehicleIds) {
        try {
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return zhejiangNetAccessProveForwardService.addNetAccessProveForward(vehicleIds, ipAddress);
        } catch (Exception e) {
            logger.error("新增浙江入网证明转发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteNetAccessProveForward(HttpServletRequest request, String vehicleIds) {
        try {
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return zhejiangNetAccessProveForwardService.deleteNetAccessProveForward(vehicleIds, ipAddress);
        } catch (Exception e) {
            logger.error("删除浙江入网证明转发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
