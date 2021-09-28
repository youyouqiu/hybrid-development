package com.zw.platform.controller.workhourmgt;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSensorInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSensorQuery;
import com.zw.platform.service.workhourmgt.WorkHourSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 工时传感器控制层
 * @author denghuabing
 * @version 1.0
 * @date 2018.5.29
 */
@Controller
@RequestMapping("/v/workhourmgt/workhoursensor")
public class WorkHourSensorController {
    private static Logger log = LogManager.getLogger(WorkHourSensorController.class);

    @Autowired
    private WorkHourSensorService workHourSensorService;

    @Autowired
    private HttpServletRequest request;

    //列表页面
    private static final String LIST_PAGE = "vas/workhourmgt/workhoursensor/list";

    //新增页面
    private static final String ADD_PAGE = "vas/workhourmgt/workhoursensor/add";

    //修改页面
    private static final String EDIT_PAGE = "vas/workhourmgt/workhoursensor/edit";

    //导入页面
    private static final String IMPORT_PAGE = "vas/workhourmgt/workhoursensor/import";

    //错误页面
    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 列表页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String workHourSensorPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean workHourSensorList(final WorkHourSensorQuery query) {
        try {
            if (query != null) {
                Page<WorkHourSensorInfo> result = workHourSensorService.findByPage(query);
                return new PageGridBean(result, true);
            } else {
                return new PageGridBean(PageGridBean.FAULT);
            }
        } catch (Exception e) {
            log.error("分页查询异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增页面
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String workHourSensorAddPage() {
        return ADD_PAGE;
    }

    /**
     * 传感器型号重复校验
     * 修改时传id
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(final String sensorNumber, String id) {
        if (sensorNumber != null) {
            boolean flag = workHourSensorService.repetition(sensorNumber, id);
            return flag;
        } else {
            return false;
        }
    }

    /**
     * 新增工时传感器
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @AvoidRepeatSubmitToken(removeToken = true)
    @ResponseBody
    public JsonResultBean addWorkHourSensor(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final WorkHourSensorForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                //校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    return workHourSensorService.addWorkHourSensor(form, ipAddress);
                }
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("新增工时传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改工时传感器页面
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView workHourSensorEditPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            WorkHourSensorForm result = workHourSensorService.findWorkHourSensorById(id);
            mav.addObject("result", result);
            return mav;
        } catch (Exception e) {
            log.error("修改传感器页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改传感器
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean editWorkHourSensor(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final WorkHourSensorForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    return workHourSensorService.updateWorkHourSensor(form, ipAddress);
                }
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("修改传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 删除工时传感器
     */
    @RequestMapping(value = "delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteWorkHourSensor(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSensorService.deleteWorkHourSensor(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除工时传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量删除
     * @param deltems 需要删除的id以','分割
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(final String deltems) {
        try {
            if (!StringUtil.isNullOrEmpty(deltems)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSensorService.deleteMore(deltems, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导入页面
     */
    @RequestMapping(value = "/import", method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "工时传感器模板");
            workHourSensorService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载模板异常", e);
        }
    }

    /**
     * 导出excel表
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "工时传感器表");
            workHourSensorService.exportWorkHourSensor(null, 1, response);
        } catch (Exception e) {
            log.error("导出excel表异常");
        }
    }

    /**
     * 批量导入
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importWorkHourSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (file != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                Map resultMap = workHourSensorService.importWorkHourSensor(file, ipAddress);
                String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
                return new JsonResultBean(true, msg);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量导入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}
