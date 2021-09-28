package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 驾驶员评分
 */
@Controller
@RequestMapping("/m/reportManagement/vehicleScore")
public class VehicleScoreController {

    private static Logger logger = LogManager.getLogger(VehicleScoreController.class);

    private static final String LIST_PAGE = "modules/reportManagement/vehicleScore/list";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }
}
