package com.zw.platform.controller.reportmanagement;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.form.VehGeneralInfo;
import com.zw.platform.domain.reportManagement.query.VehGeneralInfoQuery;
import com.zw.platform.service.reportManagement.VehGeneralInfoService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


/***
 @Author zhengjc
 @Date 2019/4/29 15:36
 @Description 车辆综合信息报表
 @version 1.0
 **/
@Controller
@RequestMapping("/m/reportManagement/vehGeneralInfo")
public class VehicleGeneralInfoController {

    private static Logger log = LogManager.getLogger(VehicleGeneralInfoController.class);

    private static final String LIST_PAGE = "modules/reportManagement/vehGeneralInfo";

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @Autowired
    private VehGeneralInfoService vehGeneralInfoService;

    /**
     * 查询车辆综合信息列表
     *
     * @return PageGridBean
     * @Title: list
     * @author zhengjc
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final VehGeneralInfoQuery query) {
        try {
            if (query != null) {
                Page<VehGeneralInfo> result = vehGeneralInfoService.listVehGeneralInfo(query);
                return new PageGridBean(query, result, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("查询车辆综合信息列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 查询车辆综合信息列表
     *
     * @return PageGridBean
     * @Title: list
     * @author zhengjc
     */
    @RequestMapping(value = {"/getVehTypes"}, method = RequestMethod.POST)
    @ResponseBody
    public List<VehicleTypeDTO> getVehTypes() {
        try {
            return vehGeneralInfoService.getVehTypes();
        } catch (Exception e) {
            log.error("查询车辆综合信息列表异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 测试809断线重连消息
     *
     * @return PageGridBean
     * @Title: list
     * @author zhengjc
     */
    @RequestMapping(value = {"/send808ReconnectMessage"}, method = RequestMethod.POST)
    @ResponseBody
    public void send808ReconnectMessage() {
        simpMessagingTemplate
            .convertAndSendToUser("zjc", ConstantUtil.WEB_SOCKET_T809_OFFLINE_RECONNECT, "ok啦！");
    }

    @RequestMapping(value = "/exportVehicleGeneralInfo", method = RequestMethod.GET)
    public void exportVehicleGeneralInfo(HttpServletResponse response) {
        try {
            vehGeneralInfoService.exportVehicleGeneralInfo(response);
        } catch (Exception e) {
            log.error("导出车辆综合信息报表异常", e);
        }
    }
}
