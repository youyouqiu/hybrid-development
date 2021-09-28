package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.query.VehicleTypePageQuery;
import com.zw.platform.basic.service.VehicleCategoryService;
import com.zw.platform.basic.service.VehicleSubTypeService;
import com.zw.platform.basic.service.VehicleTypeService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleSubTypeInfo;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 车型管理
 * @author zhangjuan
 */
@Controller
@RequestMapping("/m/basicinfo/monitoring/vehicle/type")
public class NewVehicleTypeController {
    private static Logger log = LogManager.getLogger(NewVehicleTypeController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/vehicle/type/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/vehicle/type/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/vehicle/type/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/vehicle/type/import";

    private static final String LOGOADD_PAGE = "modules/basicinfo/monitoring/vehicle/type/vTypeAdd";

    private static final String LOGOEDIT_PAGE = "modules/basicinfo/monitoring/vehicle/type/vTypeEdit";

    private static final String ICO_PAGE = "modules/basicinfo/monitoring/vehicle/type/ico";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String ADD_SUB_TYPE_PAGE = "modules/basicinfo/monitoring/vehicle/type/addSubTypePage";

    private static final String EDIT_SUB_TYPE_PAGE = "modules/basicinfo/monitoring/vehicle/type/editSubTypePage";

    private static final Integer BIGGEST_SERVICE_CYCLE = 99999;

    private static final Integer LEAST_SERVICE_CYCLE = 0;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

    @Autowired
    private VehicleTypeService vehicleTypeService;

    @Autowired
    private VehicleSubTypeService vehicleSubTypeService;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 新增车辆类别
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/addLogo", method = RequestMethod.GET)
    public String addLogo() {
        return LOGOADD_PAGE;
    }

    @RequestMapping(value = "/ico", method = RequestMethod.GET)
    public String ico() {
        return ICO_PAGE;
    }

    @RequestMapping(value = "/editLogo", method = RequestMethod.GET)
    public ModelAndView editLogo(HttpServletRequest request) {
        try {
            ModelAndView mav = new ModelAndView(LOGOEDIT_PAGE);
            String id = request.getParameter("cid");
            VehicleCategoryDTO vehicleCategory = vehicleCategoryService.getById(id);

            mav.addObject("result", new VehicleType(vehicleCategory));
            return mav;
        } catch (Exception e) {
            log.error("修改车联类别界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 新增车辆类型
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final VehicleTypeForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        Integer serviceCycle = form.getServiceCycle();
        if (serviceCycle != null && (serviceCycle > BIGGEST_SERVICE_CYCLE || serviceCycle <= LEAST_SERVICE_CYCLE)) {
            return new JsonResultBean(JsonResultBean.FAULT, "保养里程间隔(KM)不超过5位正整数");
        }
        VehicleTypeDTO vehicleType = form.convertType();
        vehicleType.setId(null);
        return ControllerTemplate.getBooleanResult(() -> vehicleTypeService.add(vehicleType));
    }

    /**
     * 分页查询车辆类型信息
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final VehicleTypeQuery query) {
        //查询获取到车型列表
        VehicleTypePageQuery pageQuery = new VehicleTypePageQuery();
        BeanUtils.copyProperties(query, pageQuery);
        Page<VehicleTypeDTO> vehicleTypes = vehicleTypeService.getByPage(pageQuery);

        //进行参数转换
        List<VehicleType> result = new ArrayList<>();
        vehicleTypes.forEach(type -> result.add(new VehicleType(type)));
        Integer total = Integer.parseInt(String.valueOf(vehicleTypes.getTotal()));
        Page<VehicleType> pageResult = RedisQueryUtil.getListToPage(result, query, total);
        return new PageGridBean(pageResult, true);
    }

    /**
     * 根据id删除 VehicleType
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return ControllerTemplate.getBooleanResult(() -> vehicleTypeService.delete(id));
    }

    /**
     * 通过ID得到VehicleType
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            VehicleTypeDTO vehicleType = vehicleTypeService.getById(id);
            mav.addObject("result", new VehicleType(vehicleType));
            return mav;
        } catch (Exception e) {
            log.error("修改车辆信息界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改车辆类型
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VehicleTypeForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        Integer serviceCycle = form.getServiceCycle();
        if (serviceCycle != null && (serviceCycle > BIGGEST_SERVICE_CYCLE || serviceCycle <= LEAST_SERVICE_CYCLE)) {
            return new JsonResultBean(JsonResultBean.FAULT, "保养里程间隔(KM)不超过5位正整数");
        }

        VehicleTypeDTO vehicleType = form.convertType();
        return ControllerTemplate.getBooleanResult(() -> vehicleTypeService.update(vehicleType));
    }

    /**
     * 车辆类型导出
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        ControllerTemplate.export(() -> vehicleTypeService.export(response), "车型列表", response, "导出车辆类型列表异常");
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        String vehicleTypeId = request.getParameter("deltems");
        if (StringUtils.isBlank(vehicleTypeId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<String> vehicleTypeIds = Arrays.asList(vehicleTypeId.split(","));
        String msg = vehicleTypeService.deleteBatch(vehicleTypeIds);
        boolean isSuccess = StringUtils.isBlank(msg);
        return new JsonResultBean(isSuccess, msg);
    }

    /**
     * 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Map<String, Object> resultMap = vehicleTypeService.importExcel(file);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                return new JsonResultBean(JsonResultBean.FAULT, ((BusinessException) e).getDetailMsg());
            }
            log.error("导入车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        response.setContentType("application/msexcel;charset=UTF-8");
        ControllerTemplate.export(() -> vehicleTypeService.generateTemplate(response), "车型模板", response, "下载车辆类型模板异常");
    }

    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(String vehicleType, String category, String vid) {
        return !vehicleTypeService.isExistType(vid, category, vehicleType);
    }

    /**
     * 新增 车辆类别
     */
    @RequestMapping(value = "/addCategory", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addcategory(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final VehicleTypeForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        VehicleCategoryDTO categoryDTO = form.convertCategory();
        categoryDTO.setId(null);
        return ControllerTemplate.getBooleanResult(() -> vehicleCategoryService.add(categoryDTO));
    }

    /**
     * 分页车辆类别信息
     */
    @ResponseBody
    @RequestMapping(value = "/listCategory", method = RequestMethod.POST)
    public PageGridBean listCategory(final VehicleTypeQuery query) {
        List<VehicleCategoryDTO> categoryList = vehicleCategoryService.getAllByKeyword(query.getVehicleCategory());
        List<VehicleType> result = new ArrayList<>();
        categoryList.forEach(category -> result.add(new VehicleType(category)));
        return new PageGridBean(RedisQueryUtil.getListToPage(result, query, result.size()));
    }

    @RequestMapping(value = "findCategoryById_{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findCategoryById(@PathVariable(value = "id") final String id) {
        List<VehicleTypeDTO> vehicleTypes = vehicleTypeService.getByCategoryId(id);
        List<VehicleType> result = new ArrayList<>();
        for (VehicleTypeDTO vehicleTypeDTO : vehicleTypes) {
            result.add(new VehicleType(vehicleTypeDTO));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("vehicleTypeList", result);
        return new JsonResultBean(jsonObject);
    }

    /**
     * 删除车辆类别
     * @param id 车辆类别ID
     * @return 删除结果
     */
    @RequestMapping(value = "/deleteCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteCategory(final String id) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        return ControllerTemplate.getResultBean(() -> vehicleCategoryService.delete(id));
    }

    /**
     * 批量删除车辆类别
     * @param ids 车辆类别id集合
     * @return 删除结果
     */
    @RequestMapping(value = "/deleteMoreCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMoreCategory(String ids) {
        if (StringUtils.isBlank(ids)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<String> categoryIds = Arrays.asList(ids.split(","));
        String message = vehicleCategoryService.deleteBatch(categoryIds);
        return new JsonResultBean(JsonResultBean.SUCCESS, message);
    }

    /**
     * 修改车辆类别
     * @param form
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/editCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editCategory(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VehicleTypeForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        VehicleCategoryDTO categoryDTO = form.convertCategory();
        // todo 车辆图标缓存维护，暂时先不维护
        return ControllerTemplate.getBooleanResult(() -> vehicleCategoryService.update(categoryDTO));
    }

    /**
     * @param vehicleCategory 车辆类别
     * @param id              车辆类别id
     * @return false 已经存在 true 车辆类别不存在
     */
    @RequestMapping(value = "/repetitions", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionCategory(String vehicleCategory, String id) {
        return !vehicleCategoryService.isExistCategory(id, vehicleCategory);
    }

    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/addSubType", method = RequestMethod.GET)
    public String addSubTypePage() {
        return ADD_SUB_TYPE_PAGE;
    }

    /**
     * 新增车辆子类型
     * @param form          this
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @RequestMapping(value = "/addSubType", method = RequestMethod.POST)
    @AvoidRepeatSubmitToken(removeToken = true)
    @ResponseBody
    public JsonResultBean addSubType(@Validated(ValidGroupAdd.class) VehicleSubTypeForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        VehicleSubTypeDTO vehicleSubType = form.convert();
        vehicleSubType.setId(null);
        return ControllerTemplate.getBooleanResult(() -> vehicleSubTypeService.add(vehicleSubType));
    }

    /**
     * (新增/编辑)验证子类型是否重复
     * @param vehicleType     类型名
     * @param vehicleSubtypes 子类型名
     * @param id              编辑时传入id
     * @return boolean
     * @author zhouzongbo
     */
    @RequestMapping(value = "checkSubTypeRepeat", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkSubTypeRepeat(String vehicleType, String vehicleSubtypes, String id) {
        return !vehicleSubTypeService.isExistSubType(id, vehicleType, vehicleSubtypes);
    }

    /**
     * 分页查询子类型列表
     * @param query query
     * @return PageGridBean
     * @author zhouzongbo
     */
    @RequestMapping(value = "/findVehicleSubType", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean findVehicleSubType(final VehicleSubTypeQuery query) {
        Page<VehicleSubTypeDTO> subTypeList = vehicleSubTypeService.getByPage(query);
        List<VehicleSubTypeInfo> result = new ArrayList<>();
        for (VehicleSubTypeDTO subTypeDTO : subTypeList) {
            result.add(new VehicleSubTypeInfo(subTypeDTO));
        }
        Integer total = Integer.parseInt(String.valueOf(subTypeList.getTotal()));
        return new PageGridBean(RedisQueryUtil.getListToPage(result, query, total), true);
    }

    /**
     * 修改页面
     * @return ModelAndView
     */
    @RequestMapping(value = "/updateSubType_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editSubType(@PathVariable(value = "id") String id) {
        try {
            VehicleSubTypeDTO subTypeDTO = vehicleSubTypeService.getById(id);
            VehicleSubTypeInfo subTypeInfo = new VehicleSubTypeInfo(subTypeDTO);
            ModelAndView mav = new ModelAndView(EDIT_SUB_TYPE_PAGE);
            mav.addObject("result", subTypeInfo);
            return mav;
        } catch (Exception e) {
            log.error("修改子类型界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 编辑子类型
     * @param form   this
     * @param result 验证
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @RequestMapping(value = "/updateSubType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSubType(@Validated(value = { ValidGroupUpdate.class }) VehicleSubTypeForm form,
        BindingResult result) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        VehicleSubTypeDTO subTypeDTO = form.convert();
        // todo 车辆图标暂时不维护
        return ControllerTemplate.getBooleanResult(() -> vehicleSubTypeService.update(subTypeDTO));
    }

    /**
     * 删除子类型
     * @param id 子类型id
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @RequestMapping(value = "/deleteSubType_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean deleteSubType(@PathVariable(value = "id") final String id) {
        return ControllerTemplate.getBooleanResult(() -> vehicleSubTypeService.delete(id));
    }

    /**
     * 批量删除子类型
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @RequestMapping(value = "/deleteMoreSubType", method = RequestMethod.POST)
    public @ResponseBody
    JsonResultBean deleteMoreSubType() {
        String vehicleSubTypeIds = request.getParameter("vehicleSubTypeIds");
        String resultMsg = vehicleSubTypeService.deleteBatch(Arrays.asList(vehicleSubTypeIds.split(",")));
        boolean isSuccess = StringUtils.isBlank(resultMsg);
        return new JsonResultBean(isSuccess, resultMsg);
    }

    /**
     * 查询车辆类型下的车辆子类型
     * @param id 车辆类型id
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @RequestMapping(value = "/findTypeIsBindingSubType_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findTypeIsBindingSubType(@PathVariable(value = "id") String id) {
        List<VehicleSubTypeDTO> subTypeList = vehicleSubTypeService.getByType(id);
        List<VehicleSubTypeForm> result =
            subTypeList.stream().map(VehicleSubTypeForm::new).collect(Collectors.toList());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", result);
        return new JsonResultBean(jsonObject);
    }

    /**
     * 查询类别标准是2(工程机械)的所有类型
     */
    @ResponseBody
    @RequestMapping(value = "/vehicleTypes", method = RequestMethod.POST)
    public JsonResultBean list() {
        List<VehicleTypeDTO> vehicleTypeList = vehicleTypeService.getByStandard(Vehicle.Standard.ENGINEERING);
        List<VehicleType> result = vehicleTypeList.stream().map(VehicleType::new).collect(Collectors.toList());
        return new JsonResultBean(result);
    }

    /**
     * 查询绑定了车辆类型的类别 -- 查询的是全部车辆类别
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @RequestMapping(value = "/findAllVehicleCategoryHasBindingVehicleType.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findAllVehicleCategoryHasBindingVehicleType() {
        List<VehicleCategoryDTO> categoryList = vehicleCategoryService.getAllByKeyword(null);
        List<VehicleType> result = categoryList.stream().map(VehicleType::new).collect(Collectors.toList());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", result);
        return new JsonResultBean(jsonObject);
    }
}
