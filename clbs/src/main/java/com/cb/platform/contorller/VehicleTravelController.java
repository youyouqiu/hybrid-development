package com.cb.platform.contorller;

import com.cb.platform.domain.VehicleTravelForm;
import com.cb.platform.domain.VehicleTravelQuery;
import com.cb.platform.service.VehicleTravelService;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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


@Controller
@RequestMapping("/cb/vehicle/travel")
public class VehicleTravelController {
    private static final Logger logger = LogManager.getLogger(VehicleTravelController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/travel/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/travel/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/travel/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/travel/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private VehicleTravelService vehicleTravelService;

    @Autowired
    VehicleService vehicleService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@Validated({ValidGroupAdd.class})
                              @ModelAttribute("form") VehicleTravelForm form,
                              final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            vehicleTravelService.addVehicleTravel(form);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("新增旅游客车行程异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            VehicleTravelForm vehicleTravelForm = vehicleTravelService.findVehicleTravelById(id);
            mav.addObject("result", vehicleTravelForm);
            return mav;
        } catch (Exception e) {
            logger.error("修改车辆旅程管理界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            vehicleTravelService.deleteVehicleTravelById(id);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("删除旅游客车行程异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = "/deleteMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(final String ids) {
        try {
            vehicleTravelService.deleteVehicleTravelByIds(ids);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("删除旅游客车行程异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@Validated({ValidGroupAdd.class})
                                     @ModelAttribute("form") VehicleTravelForm form,
                                     final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            vehicleTravelService.updateVehicleTravel(form);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("修改旅游客车行程异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean queryVehicleTravel(VehicleTravelQuery query) {
        try {
            Page<VehicleTravelForm> result =
                (Page<VehicleTravelForm>) vehicleTravelService.searchVehicleTravels(query, true);
            logger.info(JsonUtil.object2Json(result));
            return new PageGridBean(result, true);
        } catch (Exception e) {
            logger.error("获取旅游客车行程失败！", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export(VehicleTravelQuery query, HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "旅游客车行程");
            vehicleTravelService.export(null, 1, response, vehicleTravelService.searchVehicleTravels(query, false));
        } catch (Exception e) {
            logger.error("导出旅游客车行程异常", e);
        }
    }

    @RequestMapping(value = "/isRepeateTravelId", method = RequestMethod.POST)
    @ResponseBody
    public boolean isRepeateTravelId(String travelId, String id) {
        return vehicleTravelService.isRepeateTravelId(id, travelId);
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importVehicleTravel(@RequestParam(value = "file", required = false) MultipartFile file,
                                                     HttpServletRequest request) {
        try {
            String ipAddress = request.getRemoteAddr();
            return vehicleTravelService.importVehicleTravel(file, ipAddress);
        } catch (Exception e) {
            logger.error("导入旅游客车行程异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "旅游客车行程管理模板");
            vehicleTravelService.generateTemplateType(response.getOutputStream());
        } catch (Exception e) {
            logger.error("下载旅游客车行程管理模板异常", e);
        }
    }

}
