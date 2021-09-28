package com.zw.platform.controller.veermanagement;


import com.zw.platform.commons.Auth;
import com.zw.platform.controller.temperatureDetection.TemperatureStatisticsController;
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


@Controller
@RequestMapping("/v/veerManagement/veerSensor")
public class VeerMgtController {
    private static final String LIST_PAGE = "vas/veerManagement/veerSensor/list";

    private static final String ADD_PAGE = "vas/veerManagement/veerSensor/add";

    private static final String EDIT_PAGE = "vas/veerManagement/veerSensor/edit";

    private static final String IMPORT_PAGE = "vas/veerManagement/veerSensor/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private TransduserService transduserService;

    private static Logger log = LogManager.getLogger(VeerMgtController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String getListPage()
        throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 新增
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = {"/edit"}, method = RequestMethod.GET)
    public String editPage()
        throws BusinessException {
        return EDIT_PAGE;
    }

    @RequestMapping(value = "/edit_{id}", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            if (id != null && !"".equals(id)) {
                TransduserManage transduser = transduserService.findTransduserManageById(id);
                mav.addObject("result", transduser);
            }
            return mav;
        } catch (Exception e) {
            log.error("修改传感器弹出界面信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 导入
     */
    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage()
        throws BusinessException {
        return IMPORT_PAGE;
    }

}
