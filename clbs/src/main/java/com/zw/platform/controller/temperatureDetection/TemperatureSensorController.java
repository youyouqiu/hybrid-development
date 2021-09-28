package com.zw.platform.controller.temperatureDetection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.service.transdu.TransduserService;
import com.zw.platform.util.common.BusinessException;


/**
 * 温度传感器管理Controller
 * @author hujun 2017/7/6
 */
@Controller
@RequestMapping("/v/temperatureDetection/temperatureSensor")
public class TemperatureSensorController {

    private static final String LIST_PAGE = "vas/temperatureDetection/temperatureSensor/list";

    private static final String ADD_PAGE = "vas/temperatureDetection/temperatureSensor/add";

    private static final String EDIT_PAGE = "vas/temperatureDetection/temperatureSensor/edit";

    private static final String IMPORT_PAGE = "vas/temperatureDetection/temperatureSensor/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private TransduserService transduserService;

    private static Logger log = LogManager.getLogger(TemperatureSensorController.class);

    /*
     * 加载界面
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() throws BusinessException {
        return ADD_PAGE;
    }

    @RequestMapping(value = {"/edit"}, method = RequestMethod.GET)
    public String editPage() throws BusinessException {
        return EDIT_PAGE;
    }

    @RequestMapping(value = {"/edit_{id}"}, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView editInfo(@PathVariable("id") String id) {
        try {
            ModelAndView m = new ModelAndView(EDIT_PAGE);
            if (id != null && !"".equals(id)) {
                TransduserManage t = transduserService.findTransduserManageById(id);
                m.addObject("result", t);
            }
            return m;
        } catch (Exception e) {
            log.error("修改界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

}
