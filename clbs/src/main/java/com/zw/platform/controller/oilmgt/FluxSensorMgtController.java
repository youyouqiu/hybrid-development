package com.zw.platform.controller.oilmgt;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorQuery;
import com.zw.platform.service.oilmgt.FluxSensorService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
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
 * <p>Title: 流量传感器管理Controller</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: wangying
 * @date 2016年9月18日下午5:07:13
 */
@Controller
@RequestMapping("/v/oilmgt/fluxsensormgt")
public class FluxSensorMgtController {
    @Autowired
    private FluxSensorService fluxSensorService;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${fluxsensor.use}")
    private String fluxsensorUse;

    private static Logger logger = LogManager.getLogger(FluxSensorMgtController.class);

    private static final String LIST_PAGE = "vas/oilmgt/fluxsensormgt/list";

    private static final String ADD_PAGE = "vas/oilmgt/fluxsensormgt/add";

    private static final String EDIT_PAGE = "vas/oilmgt/fluxsensormgt/edit";

    private static final String IMPORT_PAGE = "vas/oilmgt/fluxsensormgt/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final FluxSensorQuery query) {
        try {
            if (query != null) {
                Page<FluxSensor> result = (Page<FluxSensor>) fluxSensorService.findFluxSensorByPage(query, true);
                return new PageGridBean(query, result, true);
            }
            return null;
        } catch (Exception e) {
            logger.error("分页查询分组（findFluxSensorByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String getAddPage(ModelMap map) {
        return ADD_PAGE;
    }

    /**
         *
     * @param form
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: 添加流量传感器
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addFluxSensor(@Validated({ValidGroupAdd.class})
                                        @ModelAttribute("form") final FluxSensorForm form,
                                        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(
                            JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);
                    return fluxSensorService.addFluxSensor(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("添加流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            FluxSensor sensor = fluxSensorService.findById(id);
            mav.addObject("result", sensor);
            return mav;
        } catch (Exception e) {
            logger.error("修改流量传感器弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         *
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 修改
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@Validated({ValidGroupUpdate.class})
                               @ModelAttribute("form") final FluxSensorForm form, final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(
                            JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);
                    return fluxSensorService.updateFluxSensor(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         *
     * @param id
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 删除
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) throws BusinessException {
        try {
            if (id != null && !"".equals(id)) {
                String ip = new GetIpAddr().getIpAddr(request);
                // 删除流量传感器
                return fluxSensorService.deleteFluxSensor(id, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("删除流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         *
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 批量删除t
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);
                // 删除流量传感器
                return fluxSensorService.deleteFluxSensor(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("批量删除流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         *
     * @param fluxSensorNumber
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: 编号去重
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repetition(@RequestParam("fluxSensorNumber") String fluxSensorNumber) {
        try {
            FluxSensor sensor = fluxSensorService.findByNumber(fluxSensorNumber);
            if (sensor == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("编号去重异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         *
     * @return String
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 导入
     */
    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importFluxSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = fluxSensorService.importSensor(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            logger.error("导入流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            String filename = "流量传感器信息列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                    "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fluxSensorService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("下载流量传感器模板异常", e);
        }
    }

    /**
     * 导出
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            String filename = "流量传感器信息列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                    "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fluxSensorService.export(null, 1, response);
        } catch (Exception e) {
            logger.error("导出流量传感器信息列表异常", e);
        }
    }
}
