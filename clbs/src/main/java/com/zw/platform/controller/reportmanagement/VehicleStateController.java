package com.zw.platform.controller.reportmanagement;

import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.reportManagement.query.VehStateQuery;
import com.zw.platform.service.reportManagement.VehStateService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.common.ControllerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/10 9:35
 */
@Controller
@RequestMapping("/m/reportManagement/vehState")
public class VehicleStateController {
    @Autowired
    private UserService userService;
    @Autowired
    private VehStateService vehStateService;

    @RequestMapping(value = { "/getOrgIdsByName" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getOrgIdsByName(String orgName) {
        return ControllerTemplate
            .getResultBean(() -> userService.fuzzSearchUserOrgIdsByOrgName(orgName), "模糊搜索用户下的企业id异常");
    }

    @RequestMapping(value = { "/getData" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getData(VehStateQuery query) {
        return ControllerTemplate.getResultBean(() -> vehStateService.getData(query), "查询某一个企业的车辆状态列表信息异常");
    }

    @RequestMapping(value = { "/getPrintInfo" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getPrintInfo(String id) {
        return ControllerTemplate.getResultBean(() -> vehStateService.getPrintInfo(id), "查询某一个企业打印所需要的的信息异常");
    }

    @RequestMapping(value = { "/deleteData" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean deleteData() {
        return ControllerTemplate.getResultBean(() -> vehStateService.deleteData(), "查询某一个企业打印所需要的的信息异常");
    }

    @RequestMapping(value = { "/export" }, method = RequestMethod.POST)
    @ResponseBody
    public void export(VehStateQuery query) {
        ControllerTemplate.execute(() -> vehStateService.export(query), "导出车辆状态列表信息异常");
    }

}
