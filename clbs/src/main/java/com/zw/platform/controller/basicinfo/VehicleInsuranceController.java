package com.zw.platform.controller.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInsuranceInfo;
import com.zw.platform.domain.basicinfo.form.VehicleInsuranceForm;
import com.zw.platform.domain.basicinfo.query.VehicleInsuranceQuery;
import com.zw.platform.service.basicinfo.VehicleInsuranceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
import java.util.Objects;

/**
 * 车辆保险
 * @author zhouzongbo on 2018/5/10 9:13
 */
@Controller
@RequestMapping("/m/basicinfo/monitoring/vehicle/insurance")
public class VehicleInsuranceController {

    private static final Logger log = LogManager.getLogger(VehicleInsuranceController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/vehicle/insurance/list";
    private static final String ADD_PAGE = "modules/basicinfo/monitoring/vehicle/insurance/add";
    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/vehicle/insurance/edit";
    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/vehicle/insurance/import";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private VehicleInsuranceService vehicleInsuranceService;
    @Autowired
    private LogSearchService logSearchService;

    /**
     * 车辆保险列表
     * @return 列表
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询车辆保险
     * @param vehicleInsuranceQuery this
     * @return this
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean findVehicleInsuranceList(final VehicleInsuranceQuery vehicleInsuranceQuery) {
        try {
            Page<VehicleInsuranceInfo> vehicleInsuranceList =
                vehicleInsuranceService.findVehicleInsuranceList(vehicleInsuranceQuery);
            return new PageGridBean(vehicleInsuranceList, true);
        } catch (Exception e) {
            log.error("分页查询车辆保险异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public ModelAndView addPage() {
        return new ModelAndView(ADD_PAGE);
    }

    /**
     * 新增
     * @param vehicleInsuranceForm form
     * @param bindingResult        数据校验
     * @return JsonResultBean
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated(ValidGroupAdd.class) final VehicleInsuranceForm vehicleInsuranceForm,
        final BindingResult bindingResult) {
        try {
            if (vehicleInsuranceForm != null) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);
                    boolean flag = vehicleInsuranceService.add(vehicleInsuranceForm, ip);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增车辆保险异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView editPage(@PathVariable String id) {
        try {
            ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
            VehicleInsuranceInfo vehicleInsuranceById = vehicleInsuranceService.getVehicleInsuranceById(id);
            modelAndView.addObject("result", vehicleInsuranceById);
            return modelAndView;
        } catch (Exception e) {
            log.error("车辆保险编辑页面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改车辆信息
     * @param vehicleInsuranceForm form
     * @param bindingResult        数据校验
     * @return JsonResultBean
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateVehicleInsurance(
        @Validated(ValidGroupUpdate.class) final VehicleInsuranceForm vehicleInsuranceForm,
        final BindingResult bindingResult) {
        try {
            if (vehicleInsuranceForm != null) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);
                    boolean flag = vehicleInsuranceService.updateVehicleInsurance(vehicleInsuranceForm, ip);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("编辑车辆保险异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 新增/编辑重复验证
     * @param insuranceId 保险单号
     * @param id          编辑时传递车辆保险id
     * @return Boolean
     */
    @RequestMapping(value = "/checkRepeat", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkRepeat(@RequestParam String insuranceId, String id) {

        try {
            VehicleInsuranceInfo vehicleInsuranceInfo =
                vehicleInsuranceService.getVehicleInsuranceByInsuranceId(insuranceId);

            // 编辑
            if (StringUtils.isNotBlank(id)) {
                VehicleInsuranceInfo vehicleInsurance = vehicleInsuranceService.getVehicleInsuranceById(id);
                if (Objects.nonNull(vehicleInsurance)) {
                    return Objects.isNull(vehicleInsuranceInfo) || insuranceId
                        .equals(vehicleInsurance.getInsuranceId());
                }
            }

            return vehicleInsuranceInfo == null;
        } catch (Exception e) {
            log.error("检查车辆保险单号是否存在", e);
            return false;
        }
    }

    /**
     * 单个删除车辆保险
     * @param id 车辆保险id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                String ip = new GetIpAddr().getIpAddr(request);
                boolean flag = vehicleInsuranceService.delete(id, ip);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除车辆保险信息出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 批量删除车辆保险
     * @param ids 车辆保险id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deleteMore.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(@RequestParam String ids) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            return vehicleInsuranceService.deleteMore(ids, ip);
        } catch (Exception e) {
            log.error("删除车辆保险信息出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 获取车牌号下拉选数据
     * @return JsonResultBean
     */
    @RequestMapping(value = "/findVehicleList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findVehicleList() {
        try {
            List<Map<String, Object>> maps = vehicleInsuranceService.findVehicleMapSelect();
            return new JsonResultBean(maps);
        } catch (Exception e) {
            log.error("获取车辆数据失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 导出车辆保险数据
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, String simpleQueryParam, Integer insuranceTipType) {
        try {
            ExportExcelUtil.setResponseHead(response, "车辆保险");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            vehicleInsuranceService.getExport(response, simpleQueryParam, insuranceTipType);
            logSearchService.addLog(ipAddress, "导出保险单号", "3", "", "");
        } catch (Exception e) {
            log.error("导出车辆保险数据失败", e);
        }
    }

    /**
     * 车辆保险模板下载
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "车辆保险模板");
            response.setContentType("application/msexcel;charset=UTF-8");
            vehicleInsuranceService.buildTemplate(response);
        } catch (Exception e) {
            log.error("下载车辆保险模板失败", e);
        }
    }

    /**
     * 车辆保险导入页面
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 车辆保险导入
     * @param file this
     * @return JsonResultBean
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importVehicleInsurance(
        @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map<String, Object> resultMap = vehicleInsuranceService.importVehicleInsurance(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入车辆保险信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }
}
