package com.zw.platform.controller.loadmgt;

import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.query.LoadSensorQuery;
import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.LoadSensorForm;
import com.zw.platform.service.loadmgt.LoadSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
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

/***
 @Author gfw
 @Date 2018/9/6 13:58
 @Description 载重传感器管理相关API
 @version 1.0
 **/
@Controller
@RequestMapping(value = "/v/loadmgt/loadsensor")
public class LoadSensorController {
    /**
     * 日志打印
     */
    private static Logger log = LogManager.getLogger(LoadSensorController.class);
    /**
     * 载重传感器 列表展示
     */
    private static final String LIST_PAGE = "vas/loadmgt/loadsensor/list";

    /**
     * 载重传感器 新增展示
     */
    private static final String ADD_PAGE = "vas/loadmgt/loadsensor/add";

    /**
     * 载重传感器 模板导入
     */
    private static final String IMPORT_PAGE = "vas/loadmgt/loadsensor/import";

    /**
     * 载重传感器 编辑页面
     */
    private static final String EDIT_PAGE = "vas/loadmgt/loadsensor/edit";

    /**
     * 错误页面
     */
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String syError;

    @Value("${add.fail}")
    private String addFail;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${set.fail}")
    private String setFail;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    LoadSensorService loadSensorService;

    /**
     * 列表页面展示
     * @param map
     * @return
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage(ModelMap map) {
        return LIST_PAGE;
    }

    /**
     * 分页列表查询
     * @param query
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final LoadSensorQuery query) {
        try {
            Page<ZwMSensorInfo> result = loadSensorService.findByPage(query);
            if (CollectionUtils.isNotEmpty(result)) {
                return new PageGridBean(query, result, true);
            } else {
                return new PageGridBean(Lists.newArrayList());
            }
        } catch (Exception e) {
            log.error("分页查询失败人员信息异常:", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增 页面
     * @return
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增 传感器
     * @param form
     * @param bindingResult
     * @return
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final LoadSensorForm form,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasFieldErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    bindingResult.getFieldErrors().get(0).getDefaultMessage());
            }

            // 获得访问ip
            if (null != form) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return loadSensorService.add(form, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, addFail);
            }
        } catch (Exception e) {
            log.error("新增载重传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }

    /**
     * 修改页面 传感器id
     * @param id
     * @return
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", loadSensorService.getById(id));
            return mav;
        } catch (Exception e) {
            log.error("修改油位传感器参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改 提交
     * @param form
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final LoadSensorForm form,
        final BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, bindingResult.getFieldErrors().get(0).getDefaultMessage());
        }
        try {
            if (null != form) {
                // 获取客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return loadSensorService.update(form, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, setFail);
            }
        } catch (Exception e) {
            log.error("修改载重传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 导出excel表
     * @param response
     * @param query
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, LoadSensorQuery query) {
        try {
            ExportExcelUtil.setResponseHead(response, "载重传感器列表");
            loadSensorService.exportList(null, 1, response, query);
        } catch (Exception e) {
            log.error("导出载重传感器列表异常", e);
        }
    }

    /**
     * 根据id删除传感器
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (!StringUtils.isEmpty(id)) {
                //获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return loadSensorService.deleteMore(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     * @return
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!StringUtils.isEmpty(items)) {
                // 获得访问ip
                String ip = new GetIpAddr().getIpAddr(request);
                return loadSensorService.deleteMore(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除载重传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "载重传感器列表模板");
            loadSensorService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载载重传感器列表模板异常", e);
        }
    }

    /**
     * @return String
     * @throws BusinessException
     * @throws @author
     * @Title: 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

    /**
     * 传感器型号重复校验
     * 修改时传id
     * @param sensorNumber
     * @param id           修改时使用
     * @return
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(final String sensorNumber, String id) {
        if (sensorNumber != null) {
            boolean flag = loadSensorService.repetition(sensorNumber, id);
            return flag;
        } else {
            return false;
        }
    }

    /**
     * 根据模板导入载重传感器
     * @param file
     * @return
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = loadSensorService.importBatch(file, request, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入载重传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }
}
