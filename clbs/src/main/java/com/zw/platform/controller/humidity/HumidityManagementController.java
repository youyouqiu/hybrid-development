package com.zw.platform.controller.humidity;


import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.service.transdu.TransduserService;
import com.zw.platform.util.common.BusinessException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


/**
 * Created by yangyi on 2017/7/6
 */
@Controller
@RequestMapping("/v/humidity/management")
public class HumidityManagementController {
    private static Logger log = LogManager.getLogger(HumidityManagementController.class);

    private static final String LIST_PAGE = "vas/humidity/humidityManagement/list";

    private static final String EDIT_PAGE = "vas/humidity/humidityManagement/edit";

    private static final String ADD_PAGE = "vas/humidity/humidityManagement/add";

    private static final String IMPORT_PAGE = "vas/humidity/humidityManagement/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private TransduserService transduserService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String editPage() throws BusinessException {
        return ADD_PAGE;
    }

    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            TransduserManage transduserManage = transduserService.findTransduserManageById(id);
            mav.addObject("result", transduserManage);
            return mav;
        } catch (Exception e) {
            log.error("修改温度传感器弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }
}
