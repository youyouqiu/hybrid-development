package com.zw.platform.controller.carbonmgt;


import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.carbonmgt.form.BasicManagementForm;
import com.zw.platform.domain.vas.carbonmgt.query.BasicManagementQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.basicinfo.VehicleTypeService;
import com.zw.platform.service.carbonmgt.BasicManagementService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;


/**
 * Created by Administrator on 2017/2/22.
 */
@Controller
@RequestMapping("/v/carbonmgt/mobileSourceBaseInfo")
public class MobileSourceController {
    private static Logger log = LogManager.getLogger(MobileSourceController.class);

    private static final String LIST_PAGE = "vas/carbonmgt/basicManagement/mobileSource";

    private static final String ADD_PAGE = "vas/carbonmgt/basicManagement/add";

    private static final String EDIT_PAGE = "vas/carbonmgt/basicManagement/edit";

    private static final String DETAILS_PAGE = "vas/carbonmgt/basicManagement/details";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private BasicManagementService basicManagementService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleTypeService vehicleTypeService;

    private static Logger logger = LogManager.getLogger(MobileSourceController.class);

    /**
     * 进入移动源基础信息列表界面
     *
     * @return String
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: basicInfoList
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage()
            throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 查询移动源基础信息
     *
     * @param query
     * @return PageGridBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: list
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final BasicManagementQuery query) {
        try {
            Page<BasicManagementForm> result = basicManagementService.find(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            logger.error("分页查询移动源信息(find)异常 ", e);
            return new PageGridBean(false);
        }

    }

    /**
     * 移动源基础信息设置界面
     *
     * @return String
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: add
     */
    @RequestMapping(value = {"/add_{vehicleId}.gsp"}, method = RequestMethod.GET)
    public ModelAndView add(@PathVariable("vehicleId") String vehicleId) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            VehicleInfo vi = vehicleService.findVehicleById(vehicleId);
            mav.addObject("result", vi);
            return mav;
        } catch (Exception e) {
            log.error("移动源基础信息设置界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 移动源基础信息设置
     *
     * @param form
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: addFuelTank
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addMobileSourceBaseInfo(@ModelAttribute("form") BasicManagementForm form) {
        try {
            boolean flag = basicManagementService.addMobileSourceBaseInfo(form);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("设置移动源基础信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 移动源基础信息修改界面
     *
     * @param vehicleId
     * @return ModelAndView
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: editMobileSourceBaseInfo
     */
    @RequestMapping(value = {"/edit_{vehicleId}"}, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("vehicleId") String vehicleId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            BasicManagementForm form = basicManagementService.getMobileSourceBaseinfoByVid(vehicleId);
            VehicleInfo vi = vehicleService.findVehicleById(vehicleId);
            if (null != vi) {
                form.setVehicleInfo(vi);
            }
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("移动源基础信息修改界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改移动源基础信息
     *
     * @param form
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: edit
     */
    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@ModelAttribute("form") BasicManagementForm form) {
        try {
            boolean flag = basicManagementService.editMobileSourceBaseinfo(form);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("修改移动源基础信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 移动源基础信息详情界面
     *
     * @return String
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: detailsPage
     */
    @Auth
    @RequestMapping(value = {"/details_{vehicleId}"}, method = RequestMethod.GET)
    public ModelAndView detailsPage(@PathVariable("vehicleId") String vehicleId) {
        try {
            ModelAndView mav = new ModelAndView(DETAILS_PAGE);
            BasicManagementForm form = basicManagementService.getMobileSourceBaseinfoByVid(vehicleId);
            VehicleInfo vi = vehicleService.findVehicleById(vehicleId);
            if (null == form) {
                form = new BasicManagementForm();
            }
            form.setVehicleInfo(vi);
            form.setPlateColorStr(
                    basicManagementService.getPlateColorByPlateColorId(Converter.toBlank(vi.getPlateColor())));
            form.setVehicleType(vehicleTypeService.get(vi.getVehicleType()).getType());
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("移动源基础信息详情界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 移动源基础信息-删除功能
     *
     * @param vehicleId
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: delete
     */
    @RequestMapping(value = {"/delete_{vehicleId}"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("vehicleId") String vehicleId) {
        try {
            boolean flag = basicManagementService.deleteMobileSourceBaseinfo(vehicleId);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("删除移动源基础信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     *
     * @param request
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: deleteMore
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            for (int i = 0; i < item.length; i++) {
                basicManagementService.deleteMobileSourceBaseinfo(item[i]);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("批量删除移动源基础信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}
