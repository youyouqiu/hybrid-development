package com.zw.adas.controller.leaderboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.zw.platform.commons.Auth;

@Controller
@RequestMapping("/adas/safetyBoard")
public class SafetyBoardController {
    private static final Logger log = LogManager.getLogger(SafetyBoardController.class);

    private static final String LIST_PAGE = "modules/adas/leaderboard/safetyboard/list";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        return modelAndView;
    }

}
