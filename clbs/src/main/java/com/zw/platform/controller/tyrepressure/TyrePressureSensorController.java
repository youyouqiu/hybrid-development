package com.zw.platform.controller.tyrepressure;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.form.TyrePressureSensorForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSensorQuery;
import com.zw.platform.service.sensor.TyrePressureSensorService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 胎压传感器管理
 * create by denghuabing 2019.2.22
 */
@RequestMapping("/v/tyrepressure/sensor")
@Controller
public class TyrePressureSensorController {

    private Logger logger = LogManager.getLogger(TyrePressureSensorController.class);

    @Autowired
    private TyrePressureSensorService tyrePressureSensorService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;
    private static final String LIST_PAGE = "vas/tirePressureManager/tirePressureSensor/list";
    private static final String ADD_PAGE = "vas/tirePressureManager/tirePressureSensor/add";
    private static final String EDIT_PAGE = "vas/tirePressureManager/tirePressureSensor/edit";
    private static final String IMPORT_PAGE = "vas/tirePressureManager/tirePressureSensor/import";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(TyrePressureSensorQuery query) {
        try {
            List<TyrePressureSensorForm> result = tyrePressureSensorService.getList(query);
            return new PageGridBean((Page<TyrePressureSensorForm>) result, true);
        } catch (Exception e) {
            logger.error("胎压传感器管理列表页面异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String getAddPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean saveSensor(TyrePressureSensorForm form) {
        try {
            if (form != null) {
                return tyrePressureSensorService.saveSensor(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("新增胎压传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/edit_{id}", method = RequestMethod.GET)
    public ModelAndView getEditPage(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
                TyrePressureSensorForm form = tyrePressureSensorService.findSensorById(id);
                modelAndView.addObject("result", form);
                return modelAndView;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("获取胎压传感器修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateSensor(TyrePressureSensorForm form) {
        try {
            if (form != null) {
                return tyrePressureSensorService.updateSensor(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改胎压传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/delete_{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteSensor(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                return tyrePressureSensorService.deleteSensor(id);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除胎压传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkSensorName(String name, String id) {
        try {
            if (StringUtils.isNotEmpty(name)) {
                return tyrePressureSensorService.checkSensorName(name, id);
            }
            return false;
        } catch (Exception e) {
            logger.error("校验传感器名称异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/deleteMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String ids) {
        try {
            if (StringUtils.isNotEmpty(ids)) {
                return tyrePressureSensorService.deleteMore(ids);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("批量删除胎压传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/import", method = RequestMethod.GET)
    public String getImportPage() {
        return IMPORT_PAGE;
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "胎压传感器模板");
            tyrePressureSensorService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("胎压传感器下载模板异常", e);
        }
    }

    /**
     * 导出excel表
     * @param response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "胎压传感器表");
            tyrePressureSensorService.exportSensor(null, 1, response);
        } catch (Exception e) {
            logger.error("导出胎压传感器excel表异常");
        }
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (file != null) {
                Map<String, Object> resultMap = tyrePressureSensorService.importSensor(file);
                String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
                return new JsonResultBean(true, msg);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("批量导入胎压传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }
}
