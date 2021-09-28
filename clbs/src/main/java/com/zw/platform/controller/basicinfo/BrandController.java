package com.zw.platform.controller.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.BrandInfo;
import com.zw.platform.domain.basicinfo.BrandModelsInfo;
import com.zw.platform.domain.basicinfo.form.BrandForm;
import com.zw.platform.domain.basicinfo.form.BrandModelsForm;
import com.zw.platform.domain.basicinfo.query.BrandModelsQuery;
import com.zw.platform.domain.basicinfo.query.BrandQuery;
import com.zw.platform.service.basicinfo.BrandService;
import com.zw.platform.service.basicinfo.VehicleService;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p> Title: 品牌机型管理Form </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 *
 * @version 1.0
 * @author: penghujie
 * @date 2018年4月17日下午4:00:00
 */
@Controller
@RequestMapping("/m/basicinfo/enterprise/brand")
public class BrandController {
    private static Logger log = LogManager.getLogger(BrandController.class);

    @Autowired
    private BrandService brandService;

    @Autowired
    private VehicleService vehicleService;

    @Resource
    private HttpServletRequest request;

    //品牌机型页
    private static final String LIST_PAGE = "modules/basicinfo/enterprise/brand/list";
    //品牌添加页
    private static final String ADD_BRAND_PAGE = "modules/basicinfo/enterprise/brand/addBrand";
    //机型添加页
    private static final String ADD_BRANDMODELS_PAGE = "modules/basicinfo/enterprise/brand/addBrandModels";
    //品牌编辑页
    private static final String EDIT_BRAND_PAGE = "modules/basicinfo/enterprise/brand/editBrand";
    //机型编辑页
    private static final String EDIT_BRANDMODELS_PAGE = "modules/basicinfo/enterprise/brand/editBrandModels";
    //品牌导入页
    private static final String IMPORT_BRAND_PAGE = "modules/basicinfo/enterprise/brand/importBrand";
    //机型导入页
    private static final String IMPORT_BRANDMODELS_PAGE = "modules/basicinfo/enterprise/brand/importBrandModels";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 品牌机型列表页
     *
     * @return
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询品牌
     */
    @ResponseBody
    @RequestMapping(value = "/listBrand", method = RequestMethod.POST)
    public PageGridBean listBrand(final BrandQuery query) {
        try {
            Page<BrandInfo> result = brandService.findBrandByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询品牌异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 分页查询机型
     */
    @ResponseBody
    @RequestMapping(value = "/listBrandModels", method = RequestMethod.POST)
    public PageGridBean listBrandModels(final BrandModelsQuery query) {
        try {
            Page<BrandModelsInfo> result = brandService.findBrandModelsByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询机型异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 生成表单的tokey数据
     */
    @RequestMapping("/generateFormToken")
    @AvoidRepeatSubmitToken(setToken = true)
    @ResponseBody
    public JsonResultBean generateFormToken(HttpServletRequest request) {
        String formKey = (String) request.getSession().getAttribute("avoidRepeatSubmitToken");
        return new JsonResultBean(true, formKey);
    }

    /**
     * 新增品牌页
     * @return
     */
    @RequestMapping(value = {"/addBrand"}, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView addBrandPage() {
        try {
            ModelAndView mav = new ModelAndView(ADD_BRAND_PAGE);
            return mav;
        } catch (Exception e) {
            log.error("新增品牌界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         *
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @Title: 新增品牌
     */
    @RequestMapping(value = {"/addBrand"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addBrand(@Validated({ValidGroupAdd.class}) @ModelAttribute("form") final BrandForm form,
                                   final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                            SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    boolean flag = brandService.addBrand(form);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }

                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改品牌页面
     */
    @RequestMapping(value = "/editBrand_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editBrandPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_BRAND_PAGE);
            BrandInfo brandInfo = brandService.getBrand(id);
            mav.addObject("result", brandInfo);
            return mav;
        } catch (Exception e) {
            log.error("修改品牌界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改品牌
     *
     * @param form
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/editBrand.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editBrand(
            @Validated({ValidGroupUpdate.class}) @ModelAttribute("form") final BrandForm form,
            final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                            SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    return brandService.updateBrand(form);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除品牌
     */
    @RequestMapping(value = "/deleteBrand_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteBrand(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                int isBandModel;
                // 查询品牌是否绑定机型
                isBandModel = brandService.getIsBandModel(id);
                // 判断是否存在绑定关系
                if (isBandModel > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该品牌已经绑定机型，不能删除！请先解绑");
                }
                return brandService.deleteBrandById(id);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除品牌
     */
    @RequestMapping(value = "/deleteBrandMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteBrandMore(final String ids) {
        try {
            if (ids != null) {
                List<String> brandIds = Arrays.asList(ids.split(","));
                List<String> list = new ArrayList<String>();
                if (brandIds.size() > 0) {
                    int before = brandIds.size();
                    for (String brandId : brandIds) {
                        int isBandOrg;
                        isBandOrg = brandService.getIsBandModel(brandId);
                        if (isBandOrg == 0) {
                            list.add(brandId);
                        }
                    }
                    if (list.size() == before) {
                        return brandService.deleteBrandByBatch(list);
                    } else {
                        if (list.size() != 0) {
                            brandService.deleteBrandByBatch(list);
                            return new JsonResultBean(JsonResultBean.FAULT, "品牌中已经绑定了机型的，不能删除！未绑定机型的已经删除.");
                        } else {
                            return new JsonResultBean(JsonResultBean.FAULT, "品牌中已经绑定了机型的，不能删除！");
                        }
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 新增时校验品牌
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/repetitionAddBrandName", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionAddBrandName(@RequestParam("name") String name) {
        try {
            BrandInfo brand = brandService.findBrandByName(name);
            if (brand == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据名字校验品牌存在异常", e);
            return false;
        }
    }

    /**
     * 修改时校验品牌
     *
     * @param id
     * @param name
     * @return
     */
    @RequestMapping(value = "/repetitionEditBrandName", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionEditBrandName(String id, String name) {
        try {
            BrandInfo brand = brandService.findBrandByName(name);
            if (brand != null) {
                if (brand.getId().equals(id)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }

        } catch (Exception e) {
            log.error("根据名字和id校验品牌存在异常", e);
            return false;
        }
    }

    /**
     * 导出品牌excel表
     */
    @RequestMapping(value = "/exportBrand", method = RequestMethod.GET)
    @ResponseBody
    public void exportBrand(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "品牌列表");
            brandService.exportBrand(null, 1, response);
        } catch (Exception e) {
            log.error("导出岗位类型列表异常", e);
        }
    }

    /**
     * 导入品牌页面
     *
     * @return
     */
    @RequestMapping(value = {"/importBrandPage"}, method = RequestMethod.GET)
    public String importBrandPage() {
        return IMPORT_BRAND_PAGE;
    }

    /**
     * 下载品牌模板
     */
    @RequestMapping(value = "/downloadBrand", method = RequestMethod.GET)
    public void downloadBrand(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "品牌模板");
            brandService.generateTemplateBrand(response);
        } catch (Exception e) {
            log.error("下载品牌模板异常", e);
        }
    }

    /**
     * 导入品牌
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/importBrand", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importBrand(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Map resultMap = brandService.importBrand(file);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入品牌信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 新增机型页
     * @return
     */
    @RequestMapping(value = {"/addBrandModels"}, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView addBrandModelsPage() {
        try {
            return new ModelAndView(ADD_BRANDMODELS_PAGE);
        } catch (Exception e) {
            log.error("新增品牌机型界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         *
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @Title: 新增机型
     */
    @RequestMapping(value = {"/addBrandModels"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addBrandModels(
            @Validated({ValidGroupAdd.class}) @ModelAttribute("form") final BrandModelsForm form,
            final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                            SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (form.getBrandId() != null) {
                        BrandInfo brandInfo = brandService.getBrand(form.getBrandId());
                        if (brandInfo == null) {
                            return new JsonResultBean(JsonResultBean.FAULT, "品牌不存在");
                        }
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT, "无品牌id");
                    }

                    boolean flag = brandService.addBrandModels(form);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }

                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增机型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改机型页面
     */
    @RequestMapping(value = "/editBrandModels_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editBrandModelsPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_BRANDMODELS_PAGE);
            BrandModelsInfo brandModelsInfo = brandService.getBrandModels(id);
            mav.addObject("result", brandModelsInfo);
            return mav;
        } catch (Exception e) {
            log.error("修改机型界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改机型
     *
     * @param form
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/editBrandModels.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editBrandModels(
            @Validated({ValidGroupUpdate.class}) @ModelAttribute("form") final BrandModelsForm form,
            final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                            SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (form.getBrandId() == null || "".equals(form.getBrandId())) {
                        return new JsonResultBean(JsonResultBean.FAULT, "品牌未选择");
                    }
                    return brandService.updateBrandModels(form);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改机型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除机型
     */
    @RequestMapping(value = "/deleteBrandModels_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteBrandModels(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                int isBandVehicle;
                // 查询机型是否绑定车辆
                isBandVehicle = vehicleService.getIsBandVehicleByBrandModelsId(id);
                // 判断是否存在绑定关系
                if (isBandVehicle > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该机型已经绑定车辆，不能删除！请先解绑");
                }
                return brandService.deleteBrandModelsById(id);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除机型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除机型
     */
    @RequestMapping(value = "/deleteBrandModelsMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteBrandModelsMore(final String ids) {
        try {
            if (ids != null) {
                List<String> modelIds = Arrays.asList(ids.split(","));
                List<String> list = new ArrayList<String>();
                if (modelIds.size() > 0) {
                    int before = modelIds.size();
                    for (String modelId : modelIds) {
                        int isBandOrg;
                        isBandOrg = vehicleService.getIsBandVehicleByBrandModelsId(modelId);
                        if (isBandOrg == 0) {
                            list.add(modelId);
                        }
                    }
                    if (list.size() == before) {
                        return brandService.deleteBrandModelsByBatch(list);
                    } else {
                        if (list.size() != 0) {
                            brandService.deleteBrandModelsByBatch(list);
                            return new JsonResultBean(JsonResultBean.FAULT, "机型中绑定了车辆的，不能删除！未绑定车辆的已经删除.");
                        } else {
                            return new JsonResultBean(JsonResultBean.FAULT, "机型中绑定了车辆的，不能删除！");
                        }

                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除机型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 新增时校验机型
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/repetitionAddModelName", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionAddModelName(String brandId, String name) {
        try {
            List<BrandModelsInfo> brandModelsInfo = brandService.findBrandModelsByName(name);
            if (brandModelsInfo == null || brandModelsInfo.size() == 0) {
                return true;
            } else {
                for (BrandModelsInfo modelsInfo : brandModelsInfo) {
                    if (modelsInfo.getBrandId().equals(brandId)) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            log.error("根据名字校验机型存在异常", e);
            return false;
        }
    }

    /**
     * 修改时校验机型
     *
     * @param id
     * @param modelName
     * @return
     */
    @RequestMapping(value = "/repetitionEditModelName", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionEditModelName(String id, String name, String brandId) {
        try {
            List<BrandModelsInfo> brandModelsInfo = brandService.findBrandModelsByName(name);
            if (brandModelsInfo == null || brandModelsInfo.size() == 0) {
                return true;
            } else {
                for (BrandModelsInfo modelsInfo : brandModelsInfo) {
                    if (modelsInfo.getId().equals(id)) {
                        continue;
                    } else {
                        if (modelsInfo.getBrandId().equals(brandId)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            log.error("根据名字和id校验机型存在异常", e);
            return false;
        }
    }

    /**
     * 导出机型excel表
     */
    @RequestMapping(value = "/exportBrandModels", method = RequestMethod.GET)
    @ResponseBody
    public void exportBrandModels(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "机型列表");
            brandService.exportBrandModels(null, 1, response);
        } catch (Exception e) {
            log.error("导出岗位类型列表异常", e);
        }
    }

    /**
     * 导入机型页面
     *
     * @return
     */
    @RequestMapping(value = {"/importBrandModelsPage"}, method = RequestMethod.GET)
    public String importBrandModelsPage() {
        return IMPORT_BRANDMODELS_PAGE;
    }

    /**
     * 下载机型模板
     */
    @RequestMapping(value = "/downloadBrandModels", method = RequestMethod.GET)
    public void downloadBrandModels(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "机型模板");
            brandService.generateTemplateBrandModels(response);
        } catch (Exception e) {
            log.error("下载机型模板异常", e);
        }
    }

    /**
     * 导入机型
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/importBrandModels", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importBrandModels(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Map<String, Object> resultMap = brandService.importBrandModels(file);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入机型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}
