package com.zw.platform.controller.basicinfo;


import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.query.RodSensorQuery;
import com.zw.platform.service.basicinfo.RodSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Created by Tdz on 2016/7/20.
 */
@Controller
@RequestMapping("/m/basicinfo/equipment/rodsensor")
public class RodSensorController {

    private static final String LIST_PAGE = "modules/basicinfo/equipment/rodsensor/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/rodsensor/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/rodsensor/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/rodsensor/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private RodSensorService rodSensorService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${add.fail}")
    private String addFail;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${set.fail}")
    private String setFail;

    private static Logger logger = LogManager.getLogger(RodSensorController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String getLoginPage(ModelMap map) {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final RodSensorQuery query) {
        try {
            Page<RodSensor> result = rodSensorService.findByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            logger.error("分页查询（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 新增
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ValidGroupAdd.class})
                              @ModelAttribute("form") final RodSensorForm form, final BindingResult bindingResult) {
        // 数据校验
        // if (bindingResult.hasErrors()) {
        // return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        // } else {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return rodSensorService.add(form, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, addFail);
            }
        } catch (Exception e) {
            logger.error("新增油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
        // }
    }

    /**
     * 根据id（油位id）删除 Personnl
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return rodSensorService.delete(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("删除油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", rodSensorService.get(id));
            return mav;
        } catch (Exception e) {
            logger.error("修改油位传感器参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@Validated({ValidGroupUpdate.class})
                               @ModelAttribute("form") final RodSensorForm form, final BindingResult bindingResult) {
        // 数据校验
        // if (bindingResult.hasErrors()) {
        // return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        // } else {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获取客户端的IP地址
                return rodSensorService.update(form, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, setFail);
            }
        } catch (Exception e) {
            logger.error("修改油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
        // }
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return rodSensorService.delete(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("批量删除油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 导出excel表
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "油位传感器列表");
            rodSensorService.exportInfo(null, 1, response);
        } catch (Exception e) {
            logger.error("导出油位传感器列表异常", e);
        }
    }

    /**
     * @return String
     * @throws BusinessException
     * @throws @author
     * @Title: 导入
     */
    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = rodSensorService.importSensor(file, request, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            logger.error("导入油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }

    /**
     * 下载模板
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "油位传感器列表模板");
            rodSensorService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("下载油位传感器列表模板异常", e);
        }
    }

    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("sensorNumber") String sensorNumber) {
        try {
            RodSensor vt = rodSensorService.findByRodSensor(sensorNumber);
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("油位传感器检查异常", e);
            return false;
        }
    }
}
