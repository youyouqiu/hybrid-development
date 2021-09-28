package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.service.FuelTypeService;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehiclePurposeService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.basic.service.VehicleTypeService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.BrandInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.VehiclePurpose;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.BatchUpdateVehicleForm;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.basicinfo.form.VehiclePurposeForm;
import com.zw.platform.domain.basicinfo.query.BrandQuery;
import com.zw.platform.domain.basicinfo.query.MonitorTreeQuery;
import com.zw.platform.domain.basicinfo.query.VehiclePurposeQuery;
import com.zw.platform.domain.basicinfo.query.VehicleQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.basicinfo.BrandService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.imports.ZwImportException;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 车辆信息管理
 * @author zhangjuan
 */
@Controller
@RequestMapping("/m/basicinfo/monitoring/vehicle")
public class NewVehicleController {
    private static final Logger log = LogManager.getLogger(NewVehicleController.class);
    private static final String LIST_PAGE = "modules/basicinfo/monitoring/vehicle/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/vehicle/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/vehicle/edit";

    private static final String BATCH_EDIT_PAGE = "modules/basicinfo/monitoring/vehicle/batchEdit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/vehicle/import";

    private static final String IMPORTWO_PAGE = "modules/basicinfo/monitoring/vehicle/importTwo";

    private static final String LOGO_EDIT = "modules/basicinfo/monitoring/vehicle/vIcoEdit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String VEHICLE_DETAIL_PAGE = "modules/basicinfo/monitoring/vehicle/vehicleDetail";

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehiclePurposeService purposeService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private MessageConfig messageConfig;

    @Autowired
    private FuelTypeService findFuelType;

    @Autowired
    private BrandService brandService;

    @Autowired
    private VehicleTypeService vehicleTypeService;

    @Autowired
    private MonitorTreeService monitorTreeService;

    @Autowired
    private UserService userService;

    /**
     * table
     * @return resource Page
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * add page
     * @return resource Page
     */
    @RequestMapping(value = { "/vlist" }, method = RequestMethod.GET)
    public String submitPage() {
        return ADD_PAGE;
    }

    /**
     * vehicle icon edit page
     * @return resource Page
     */
    @RequestMapping(value = { "/editLogo" }, method = RequestMethod.GET)
    public String editLogo() {
        return LOGO_EDIT;
    }

    /**
     * 分页查询
     * @param query 查询参数
     * @return 分页数据
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final VehicleQuery query) {
        Page<VehicleDTO> pageResult = vehicleService.getByPage(query);
        List<Map<String, Object>> vehicleList = new ArrayList<>();
        for (VehicleDTO vehicle : pageResult.getResult()) {
            Map<String, Object> vehicleMap = MapUtil.objNonNullFieldToHash(vehicle);
            vehicleMap.remove("fuelTypeName");
            vehicleMap.remove("name");
            vehicleMap.remove("vehiclePurposeName");
            vehicleMap.remove("vehicleTypeName");
            vehicleMap.remove("simCardNumber");
            vehicleMap.put("groupId", vehicle.getOrgId());
            vehicleMap.put("flType", vehicle.getFuelType());
            vehicleMap.put("purposeCategory", vehicle.getVehiclePurposeName());
            vehicleMap.put("brand", vehicle.getName());
            vehicleMap.put("assignId", vehicle.getGroupId());
            vehicleMap.put("vehType", vehicle.getVehicleTypeName());
            vehicleMap.put("simcardNumber", vehicle.getSimCardNumber());
            vehicleMap.put("groupName", vehicle.getOrgName());
            vehicleMap.put("fuelType", vehicle.getFuelTypeName());
            vehicleMap.put("fuelTypeId", vehicle.getFuelType());
            vehicleMap.put("assign", vehicle.getGroupName());
            vehicleMap.put("phoneCheck", String.valueOf(vehicle.getPhoneCheck()));
            vehicleMap.put("aliases", vehicle.getAlias());
            vehicleList.add(vehicleMap);
        }

        int total = Integer.parseInt(String.valueOf(pageResult.getTotal()));
        Page<Map<String, Object>> result = RedisQueryUtil.getListToPage(vehicleList, query, total);
        return new PageGridBean(query, result, true);
    }

    /**
     * 运营类别查询分页
     * @param query 查询参数
     * @return 分页数据
     */
    @ResponseBody
    @RequestMapping(value = "/purposeCategoryList", method = RequestMethod.POST)
    public PageGridBean list(final VehiclePurposeQuery query) {
        Page<VehiclePurposeDTO> pageResult = purposeService.getListByKeyWord(query);
        return new PageGridBean(pageResult, true);
    }

    /**
     * 新增页面
     * @param uuid uuid
     * @return 页面
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public ModelAndView initNewUser(@RequestParam("uuid") final String uuid) {
        ModelAndView mav = new ModelAndView(ADD_PAGE);
        if (!"".equals(uuid) && !"ou=organization".equals(uuid)) {
            OrganizationLdap org = organizationService.getOrgByEntryDn(uuid);
            mav.addObject("orgId", org.getUuid());
            mav.addObject("groupName", org.getName());
        }
        return mav;
    }

    /**
     * 新增车辆信息
     * @param form          车辆信息
     * @param bindingResult 绑定信息
     * @return JsonResultBean
     * @author wangying
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final VehicleForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }

        VehicleDTO vehicleDTO = form.convert();
        vehicleDTO.setId(null);
        return ControllerTemplate.getBooleanResult(() -> vehicleService.add(vehicleDTO));
    }

    /**
     * 根据id删除 车辆
     * @param id 车辆Id
     * @return result
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        JsonResultBean result = ControllerTemplate.getBooleanResult(() -> vehicleService.delete(id));
        JSONObject msg = new JSONObject();
        msg.put("vehicleId", id);
        msg.put("infoMsg", result.getMsg());
        return StringUtils.isBlank(result.getMsg()) ? result : new JsonResultBean(msg);
    }

    /**
     * 批量删除
     * @param checkedList 选择的车Id
     * @return result
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String checkedList) {
        if (StringUtils.isBlank(checkedList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "请至少选择一辆车进行删除");
        }
        List<String> ids = new ArrayList<>(Arrays.asList(checkedList.split(",")));
        return ControllerTemplate.getResult(() -> vehicleService.batchDel(ids));
    }

    /**
     * 修改车辆
     * @param id vehicleId
     * @return edit page
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            VehicleDTO vehicle = vehicleService.getById(id);
            if (Objects.isNull(vehicle)) {
                mav.setViewName("html/errors/error_400");
                return mav;
            }
            VehicleInfo result = new VehicleInfo(vehicle);
            mav.addObject("result", result);
            return mav;
        } catch (Exception e) {
            log.error("修改车辆信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改车辆
     * @return edit page
     */
    @RequestMapping(value = "/batchEdit.gsp", method = RequestMethod.GET)
    public ModelAndView batchEditPage() {
        try {
            return new ModelAndView(BATCH_EDIT_PAGE);
        } catch (Exception e) {
            log.error("批量修改车辆信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改车辆
     * @return edit page
     */
    @RequestMapping(value = "/batchEdit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchEdit(BatchUpdateVehicleForm form) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //参数转换
        VehicleDTO vehicleDTO = form.convert();
        List<String> ids = new ArrayList<>(Arrays.asList(form.getId().split(",")));

        //行政区划校验，省市区，要门都不选，要么都需要选择
        if (StringUtils.isNotBlank(vehicleDTO.getProvince())) {
            if (StringUtils.isBlank(vehicleDTO.getCity())) {
                return new JsonResultBean(JsonResultBean.FAULT, "请选择市");
            }
            if (StringUtils.isBlank(vehicleDTO.getCounty())) {
                return new JsonResultBean(JsonResultBean.FAULT, "请选择县");
            }
        }
        return ControllerTemplate.getBooleanResult(() -> vehicleService.batchUpdate(ids, vehicleDTO));
    }

    /**
     * 详情页面
     * @param id vehicleId
     * @return detail page
     */
    @RequestMapping(value = "/vehicleDetail_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView vehicleDetail(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(VEHICLE_DETAIL_PAGE);
            VehicleDTO vehicle = vehicleService.getById(id);
            if (Objects.nonNull(vehicle)) {
                VehicleInfo result = new VehicleInfo(vehicle);
                mav.addObject("result", result);
            } else {
                mav.setViewName("html/errors/error_400");
            }
            return mav;
        } catch (Exception e) {
            log.error("车辆详情弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改车辆信息
     * @param form          vehicleForm
     * @param bindingResult bindingResult
     * @return JsonResultBean result
     * @author wangying
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VehicleForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        //参数校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        VehicleDTO vehicle = form.convert();
        return ControllerTemplate.getBooleanResult(() -> vehicleService.update(vehicle));
    }

    /**
     * 车辆列表导出
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        ControllerTemplate.export(() -> vehicleService.export(response), "车辆列表", response, "导出车辆信息异常");
    }

    /**
     * 运营类别类别导出
     * @param response response
     */
    @RequestMapping(value = "/exportVehiclePurpose", method = RequestMethod.GET)
    public void exportVehiclePurpose(HttpServletResponse response) {
        ControllerTemplate.export(() -> purposeService.export(response), "运营类别", response, "导出运营类别异常");
    }

    /**
     * 下载通用车辆列表模板
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate
            .export(() -> vehicleService.generateTemplate(response), "通用车辆列表模板", response, "下载通用车辆列表模板异常");
    }

    /**
     * 下载工程机械列表模板
     * @param response response
     */
    @RequestMapping(value = "/downloadEngineering", method = RequestMethod.GET)
    public void downloadEngineering(HttpServletResponse response) {
        ControllerTemplate
            .export(() -> vehicleService.generateTemplateEngineering(response), "工程机械列表模板", response, "下载工程机械列表模板异常");
    }

    /**
     * 下载货运车辆列表模板
     * @param response response
     */
    @RequestMapping(value = "/downloadFreight", method = RequestMethod.GET)
    public void downloadFreight(HttpServletResponse response) {
        ControllerTemplate
            .export(() -> vehicleService.generateTemplateFreight(response), "货运车辆列表模板", response, "下载货运车辆列表模板异常");
    }

    /**
     * 导入页面
     * @return String
     * @author wangying
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public ModelAndView importPage() {
        return new ModelAndView(IMPORT_PAGE);
    }

    /**
     * 导入
     * @param file 文件
     * @return result
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importVehicle(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            return vehicleService.importExcel(file);
        } catch (Exception e) {
            log.error("导入车辆信息异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
    }

    /**
     * 获取组织+用户树
     * @return List<Group>
     * @throws BusinessException exception
     */
    @RequestMapping(value = "/userTree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree() {
        //目前使用该接口的是否只有车辆管理添加调用，但没有地方展示和使用，后续可以考虑在前端删掉该接口的调用
        return null;
    }

    /**
     * 组织+用户树数据(编辑)
     * @param assignmentId 组织Id
     * @return List<Group>
     */
    @RequestMapping(value = "/userEditTree", method = RequestMethod.POST)
    @ResponseBody
    public String getTreeEdit(String assignmentId) {
        //目前使用该接口的是否只有车辆管理修改调用，但没有地方展示和使用，且没有传assignmentId，只传了参参数vehicleId，
        // 后续可以考虑在前端删掉该接口的调用
        return null;
    }

    @RequestMapping(value = { "/addList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean pageBean() {
        List<VehicleTypeDTO> vehicleTypeList = vehicleTypeService.getListByKeyword(null);
        List<VehicleType> result = vehicleTypeList.stream().map(VehicleType::new).collect(Collectors.toList());
        return new JsonResultBean(ImmutableMap.of("VehicleTypeList", result));
    }

    /**
     * repetition
     * @param brand 车牌号
     * @return result
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("brand") String brand) {
        VehicleDTO vehicleDTO = vehicleService.getByName(brand);
        return Objects.isNull(vehicleDTO);
    }

    /**
     * subscribeVehicleList
     * @return vehicle list
     */
    @RequestMapping(value = "/subscribeVehicleList", method = RequestMethod.POST)
    @ResponseBody
    public List<VehicleInfo> subscribeVehicleList() {
        Set<String> monitorIds = userService.getCurrentUserMonitorIds();
        Collection<BindDTO> bindList = MonitorUtils.getBindDTOMap(monitorIds, "id", "name", "monitorType").values();
        List<VehicleInfo> vehicleInfoList = new ArrayList<>();
        VehicleInfo vehicleInfo;
        for (BindDTO bindDTO : bindList) {
            vehicleInfo = new VehicleInfo();
            vehicleInfo.setId(bindDTO.getId());
            vehicleInfo.setBrand(bindDTO.getName());
            vehicleInfo.setMonitorType(bindDTO.getMonitorType());
            vehicleInfoList.add(vehicleInfo);
        }
        return vehicleInfoList;
    }

    /**
     * 新增运营类别
     * @param vehiclePurposes vehiclePurposes
     * @return result
     * @author tangshunyu
     */
    @RequestMapping(value = "/addVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addVehiclePurpose(@Validated({
        ValidGroupAdd.class }) @ModelAttribute("vehiclePurposes") final VehiclePurposeForm vehiclePurposes) {
        if (vehiclePurposes == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        VehiclePurposeDTO purposeDTO = vehiclePurposes.convert();
        purposeDTO.setId(null);
        return ControllerTemplate.getResultBean(() -> purposeService.add(purposeDTO));
    }

    /**
     * todo 未找到地方使用
     * 根据运营类别名称获取运营类别实体
     * @param purposeCategory purposeCategory
     * @return VehiclePurpose list
     */
    @RequestMapping(value = "/findVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public List<VehiclePurpose> findVehiclePurposes(String purposeCategory) {
        VehiclePurposeDTO purposeDTO = purposeService.getByName(purposeCategory);
        if (purposeDTO == null) {
            return new ArrayList<>();
        }
        VehiclePurpose vehiclePurpose = new VehiclePurpose(purposeDTO);
        return Collections.singletonList(vehiclePurpose);
    }

    /**
     * 根据id获取运营类别类别
     * @param id vehicleId
     * @return VehiclePurpose
     */
    @RequestMapping(value = "/findVehiclePurposeCategory", method = RequestMethod.POST)
    @ResponseBody
    public VehiclePurpose findPurposeCategoryById(String id) {
        VehiclePurposeDTO purposeDTO = purposeService.getById(id);
        if (purposeDTO == null) {
            return null;
        }
        return new VehiclePurpose(purposeDTO);
    }

    /**
     * 查询数据库是否有该运营类别,并对比修改前和修改后的用途,是否相同
     * @param newType
     * @param oldType
     * @return
     */
    @RequestMapping(value = "/comparison", method = RequestMethod.POST)
    @ResponseBody
    public boolean comparisonVehicleUserType(String newType, String oldType) {
        if (StringUtils.isBlank(oldType) || StringUtils.isBlank(newType)) {
            return false;
        }
        if (Objects.equals(oldType, newType)) {
            return true;
        }
        return Objects.isNull(purposeService.getByName(newType));
    }

    /**
     * 根据运营类别查询数据库是否有相同的运营类别(新增运营类别时的验证)
     * @param vehicleUseType
     * @return
     */
    @RequestMapping(value = "/findVehicleUseType", method = RequestMethod.POST)
    @ResponseBody
    public boolean findVehicleUseType(String vehicleUseType) {
        return !purposeService.isExistPurpose(null, vehicleUseType);
    }

    /**
     * 修改运营类别类别
     * @param id
     * @param purposeCategory
     * @param description
     * @return
     */
    @RequestMapping(value = "/updateVehiclePurposeCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateVehiclePurpose(String id, String purposeCategory, String description) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(purposeCategory)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        VehiclePurposeDTO purposeDTO = new VehiclePurposeDTO();
        purposeDTO.setId(id);
        purposeDTO.setPurposeCategory(purposeCategory);
        purposeDTO.setDescription(description);
        return ControllerTemplate.getResultBean(() -> purposeService.update(purposeDTO));
    }

    /**
     * 根据id删除运营类别
     * @param id 运营类别id
     * @return result
     * @author tangshunyu
     */
    @RequestMapping(value = "/deleteVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteVehiclePurpose(String id) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return ControllerTemplate.getResultBean(() -> purposeService.delete(id));
    }

    /**
     * 批量删除运营类别
     * @param ids 运营类别Ids
     * @return result
     */
    @RequestMapping(value = "/deleteVehiclePurposeMuch", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteVehiclePurposeMuch(String ids) {
        if (StringUtils.isBlank(ids)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        List<String> idList = Arrays.asList(ids.split(","));
        purposeService.delBatch(idList);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 运营类别下载模板
     * @param response response
     */
    @RequestMapping(value = "/downloadPurpose", method = RequestMethod.GET)
    public void downloadType(HttpServletResponse response) {
        ControllerTemplate.export(() -> purposeService.generateTemplate(response), "运营类别模板", response, "下载运营类别模板异常");
    }

    /**
     * 导入运营类别
     * @return String
     * @author tangshunyu
     */
    @RequestMapping(value = "/importTwo", method = RequestMethod.GET)
    public String importTwoPage() {
        return IMPORTWO_PAGE;
    }

    /**
     * 导入运营类别
     * @param file 文件
     * @return result
     */
    @RequestMapping(value = "/importPurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importType(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Map<String, Object> resultMap = purposeService.importExcel(file);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                return new JsonResultBean(JsonResultBean.FAULT, ((BusinessException) e).getDetailMsg());
            }
            log.error("导入运营类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * 查询运营类别
     * @return result
     */
    @RequestMapping(value = "/findAllPurposeCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAllPurposeCategory() {
        JSONObject msg = new JSONObject();
        msg.put("VehicleCategoryList", purposeService.getListByKeyWord(new VehiclePurposeQuery()).getResult());
        return new JsonResultBean(msg);
    }

    /**
     * 查询燃料类型
     * @return result
     */
    @RequestMapping(value = "/findAllFuelType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAllFuelType() {
        JSONObject msg = new JSONObject();
        msg.put("FuelTypeList", findFuelType.getByKeyword(null));
        return new JsonResultBean(msg);
    }

    /**
     * 获取组织+分组+监控对象树
     * @param type       1在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
     * @param webType    1:实时监控界面，2:实时视频界面
     * @param queryParam 模糊查询参数，根据监控对象名称，若为空则查询全部该状态的车辆
     * @param isCarousel 是否是轮播页面的查询
     * @return result
     */
    @RequestMapping(value = "/treeStateInfo", method = RequestMethod.POST)
    @ResponseBody
    public String stateInfo(int type, int webType, String queryParam, String devType, Integer isCarousel) {
        try {
            boolean needCarousel = Objects.equals(1, isCarousel);
            JSONArray treeNodes =
                monitorTreeService.getMonitorStateTree(webType, queryParam, type, devType, needCarousel, false);
            String treeNodeStr = treeNodes.toJSONString();
            //在进行查询离线或未上线的车辆时进行压缩处理
            if (Objects.equals(type, 7) || Objects.equals(type, 8)) {
                treeNodeStr = ZipUtil.compress(treeNodes.toJSONString());
            }
            return treeNodeStr;
        } catch (Exception e) {
            log.error("获取监控对象状态树异常", e);
        }
        return null;
    }

    /**
     * 获取组织+分组+监控对象树  需要返回监控对象ACC状态
     * @param type       1在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
     * @param webType    1:实时监控界面，2:实时视频界面
     * @param queryParam 模糊查询参数，根据监控对象名称，若为空则查询全部该状态的车辆
     * @param isCarousel 是否是轮播页面的查询
     * @return result
     */
    @RequestMapping(value = "/new/treeStateInfo", method = RequestMethod.POST)
    @ResponseBody
    public String stateInfoNew(int type, int webType, String queryParam, String devType, Integer isCarousel) {
        try {
            boolean needCarousel = Objects.equals(1, isCarousel);
            JSONArray treeNodes =
                monitorTreeService.getMonitorStateTree(webType, queryParam, type, devType, needCarousel, true);
            String treeNodeStr = treeNodes.toJSONString();
            //在进行查询离线或未上线的车辆时进行压缩处理
            if (Objects.equals(type, 7) || Objects.equals(type, 8)) {
                treeNodeStr = ZipUtil.compress(treeNodes.toJSONString());
            }
            return treeNodeStr;
        } catch (Exception e) {
            log.error("获取监控对象状态树异常", e);
        }
        return null;
    }

    /**
     * treeStateInfo
     * @param monitorTreeQuery 实时监控相关组织树查询
     * @return result (1120调整)
     */
    @RequestMapping(value = "/treeStateInfos", method = RequestMethod.POST)
    @ResponseBody
    public String stateInfo(MonitorTreeQuery monitorTreeQuery) {
        try {
            MonitorTreeReq monitorTreeReq = new MonitorTreeReq();
            monitorTreeReq.setNeedMonitorCount(false);
            monitorTreeReq.setQueryType(monitorTreeQuery.getQueryType());
            monitorTreeReq.setKeyword(monitorTreeQuery.getQueryParam());
            if (Objects.equals(monitorTreeQuery.getQueryType(), "vehType")) {
                monitorTreeReq.setVehicleTypeName(monitorTreeQuery.getQueryParam());
                monitorTreeReq.setKeyword(null);
                monitorTreeReq.setQueryType(null);
            }
            if (Objects.equals(monitorTreeQuery.getQueryType(), "monitor")) {
                monitorTreeReq.setQueryType("name");
            }
            Integer status = monitorTreeQuery.getType();
            monitorTreeReq.setStatus(status);
            monitorTreeReq.setDeviceTypes(monitorTreeReq.getDeviceTypes(status, monitorTreeQuery.getDevType()));
            monitorTreeReq.setWebType(monitorTreeQuery.getWebType());
            JSONArray treeNodes = monitorTreeService.getMonitorTreeFuzzy(monitorTreeReq);
            String treeNodeStr = treeNodes.toJSONString();
            //在进行查询离线或未上线的车辆时进行压缩处理
            if (Objects.equals(status, 7) || Objects.equals(status, 8)) {
                treeNodeStr = ZipUtil.compress(treeNodes.toJSONString());
            }
            return treeNodeStr;
        } catch (Exception e) {
            log.error("获取监控对象状态树异常", e);
        }
        return null;
    }

    /**
     * 查询品牌
     * @return result
     */
    @RequestMapping(value = "/findBrand", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findBrand() {
        try {
            Page<BrandInfo> brands = brandService.findBrandByPage(new BrandQuery());
            return new JsonResultBean(ImmutableMap.of("brandList", brands));
        } catch (Exception e) {
            log.error("查询品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * 根据品牌id查询机型
     * @return result
     */
    @RequestMapping(value = "/findBrandModelsByBrandId_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findBrandModelsByBrandId(@PathVariable final String id) {
        try {
            return new JsonResultBean(ImmutableMap.of("brandModelList", brandService.findBrandModelsByBrandId(id)));
        } catch (Exception e) {
            log.error("查询品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * 根据车牌号获取车辆的id和车牌号
     * @return
     */
    @RequestMapping(value = "/findTransportList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findTransportList(@RequestParam(value = "brand", required = false) String brand) {
        List<MonitorBaseDTO> vehicleList = vehicleService.getByCategoryName("危险品运输车");
        List<Map<String, String>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(vehicleList)) {
            for (MonitorBaseDTO vehicle : vehicleList) {
                result.add(ImmutableMap.of("id", vehicle.getId(), "brand", vehicle.getName()));
            }
        }
        return new JsonResultBean(result);
    }

    /**
     * 获取用户拥有权限且为已绑定的营运车辆数量和维修车辆的数量
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getOperatingAndRepairNum", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOperatingAndRepairNum() {
        return new JsonResultBean(vehicleService.getOperatingAndRepairNum());
    }

    /**
     * 获取所有类别为客车的车辆列表
     * @return 车辆ID和车牌号列表
     */
    @RequestMapping(value = { "/getAllBuses" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean queryVehicleTravel() {
        List<MonitorBaseDTO> vehicleList = vehicleService.getByCategoryName("客车");
        List<Map<String, String>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(vehicleList)) {
            for (MonitorBaseDTO vehicle : vehicleList) {
                result.add(ImmutableMap.of("id", vehicle.getId(), "brand", vehicle.getName()));
            }
        }
        return new JsonResultBean(result);
    }

    @RequestMapping(value = { "/maintained" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean maintainVehicle(@RequestParam(value = "vehicleType") String vehicleType,
        @RequestParam(value = "vid") String vid, @RequestParam(value = "execute") boolean execute) {
        return vehicleService.saveMaintained(vehicleType, vid, execute);
    }
}
