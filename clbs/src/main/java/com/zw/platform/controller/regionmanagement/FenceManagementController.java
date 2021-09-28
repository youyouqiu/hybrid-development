package com.zw.platform.controller.regionmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.functionconfig.form.AdministrationForm;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.regionmanagement.FenceTypeFrom;
import com.zw.platform.service.regionmanagement.FenceManagementService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 区域管理-围栏管理
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 10:31
 */
@Controller
@RequestMapping("/m/regionManagement/fenceManagement")
public class FenceManagementController {
    private static final Logger logger = LogManager.getLogger(FenceManagementController.class);

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 围栏管理页面
     */
    private static final String LIST_PAGE = "modules/regionManagement/fenceManagement/list";

    /**
     * 围栏种类新增页面
     */
    private static final String FENCE_TYPE_ADD_PAGE = "modules/regionManagement/fenceManagement/fenceTypeAddPage";

    /**
     * 围栏种类修改页面
     */
    private static final String FENCE_TYPE_UPDATE_PAGE = "modules/regionManagement/fenceManagement/fenceTypeUpdatePage";

    /**
     * 围栏种类详情页面
     */
    private static final String FENCE_TYPE_DETAIL_PAGE = "modules/regionManagement/fenceManagement/fenceTypeDetailPage";

    /**
     * 用户围栏显示设置页面
     */
    private static final String USER_FENCE_DISPLAY_SET_PAGE =
        "modules/regionManagement/fenceManagement/userFenceShowSetPage";

    @Autowired
    private FenceManagementService fenceManagementService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 围栏管理页面
     */
    @Auth
    @RequestMapping(value = "/listPage", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 获取围栏种类新增页面
     */
    @RequestMapping(value = "/getFenceTypeAddPage", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String getFenceTypeAddPage() {
        return FENCE_TYPE_ADD_PAGE;
    }

    /**
     * 新增围栏种类
     */
    @RequestMapping(value = "/addFenceType", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addFenceType(FenceTypeFrom fenceTypeFrom, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.addFenceType(fenceTypeFrom, ipAddress);
        } catch (Exception e) {
            logger.error("新增围栏种类异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除围栏种类
     */
    @RequestMapping(value = "/deleteFenceType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteFenceType(String fenceTypeId, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.deleteFenceType(fenceTypeId, ipAddress);
        } catch (Exception e) {
            logger.error("删除围栏种类异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取围栏种类修改页面
     * @param fenceTypeId 围栏种类id
     */
    @RequestMapping(value = "/getFenceTypeUpdatePage/{fenceTypeId}", method = RequestMethod.GET)
    public ModelAndView getFenceTypeUpdatePage(@PathVariable("fenceTypeId") String fenceTypeId) {
        try {
            return new ModelAndView(FENCE_TYPE_UPDATE_PAGE)
                .addObject("fenceTypeInfo", fenceManagementService.getFenceTypeInfoById(fenceTypeId));
        } catch (Exception e) {
            logger.error("获取修改围栏种类页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取围栏种类详情页面
     * @param fenceTypeId 围栏种类id
     */
    @RequestMapping(value = "/getFenceTypeDetailPage/{fenceTypeId}", method = RequestMethod.GET)
    public ModelAndView getFenceTypeDetailPage(@PathVariable("fenceTypeId") String fenceTypeId) {
        try {
            return new ModelAndView(FENCE_TYPE_DETAIL_PAGE)
                .addObject("fenceTypeInfo", fenceManagementService.getFenceTypeInfoById(fenceTypeId));
        } catch (Exception e) {
            logger.error("获取围栏种类详情页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改围栏种类
     */
    @RequestMapping(value = "/updateFenceType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateFenceType(FenceTypeFrom fenceTypeFrom, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.updateFenceType(fenceTypeFrom, ipAddress);
        } catch (Exception e) {
            logger.error("修改围栏种类异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得围栏种类列表
     */
    @RequestMapping(value = "/getFenceTypeList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceTypeList() {
        try {
            return fenceManagementService.getFenceTypeList();
        } catch (Exception e) {
            logger.error("获得围栏种类列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 判断围栏种类名称是否能使用(判断是否已存在)
     * @param fenceTypeName 围栏种类名称
     * @param fenceTypeId   围栏种类id
     * @return true:可以使用; false: 不可以使用
     */
    @RequestMapping(value = "/judgeFenceTypeNameIsCanBeUsed", method = RequestMethod.POST)
    @ResponseBody
    public boolean judgeFenceTypeNameIsCanBeUsed(String fenceTypeName, String fenceTypeId) {
        try {
            return fenceManagementService.judgeFenceTypeNameIsCanBeUsed(fenceTypeName, fenceTypeId);
        } catch (Exception e) {
            logger.error("判断围栏种类名称是否能使用异常", e);
            return false;
        }
    }

    /**
     * 获得围栏种类的绘制方式
     */
    @RequestMapping(value = "/getFenceTypeDrawType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceTypeDrawType(String fenceTypeId) {
        try {
            return fenceManagementService.getFenceTypeDrawType(fenceTypeId);
        } catch (Exception e) {
            logger.error("获得围栏种类下的围栏信息集合异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得围栏种类下的围栏信息集合
     */
    @RequestMapping(value = "/getFenceInfoListByFenceTypeId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceInfoListByFenceTypeId(String fenceTypeId) {
        try {
            return fenceManagementService.getFenceInfoListByFenceTypeId(fenceTypeId);
        } catch (Exception e) {
            logger.error("获得围栏种类下的围栏信息集合异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 判断围栏名称是否能使用(判断是否已存在)
     * @param fenceName   围栏名称
     * @param fenceId     围栏id
     * @param fenceTypeId 围栏种类id
     * @return true:可以使用; false: 不可以使用
     */
    @RequestMapping(value = "/judgeFenceNameIsCanBeUsed", method = RequestMethod.POST)
    @ResponseBody
    public boolean judgeFenceNameIsCanBeUsed(String fenceName, String fenceId, String fenceTypeId) {
        try {
            return fenceManagementService.judgeFenceNameIsCanBeUsed(fenceName, fenceId, fenceTypeId);
        } catch (Exception e) {
            logger.error("判断围栏名称是否能使用异常", e);
            return false;
        }
    }

    /**
     * 新增/修改线
     */
    @RequestMapping(value = "/addOrUpdateLine", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOrUpdateLine(LineForm form, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.addOrUpdateLine(form, ipAddress);
        } catch (Exception e) {
            logger.error("线围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增/修改标注
     */
    @RequestMapping(value = "/addOrUpdateMarker", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOrUpdateMarker(MarkForm form, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.addOrUpdateMarker(form, ipAddress);
        } catch (Exception e) {
            logger.error("标注围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增/修改圆
     */
    @RequestMapping(value = "/addOrUpdateCircle", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOrUpdateCircle(CircleForm form, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.addOrUpdateCircle(form, ipAddress);
        } catch (Exception e) {
            logger.error("圆形围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增/修改多边形
     */
    @RequestMapping(value = "/addOrUpdatePolygon", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOrUpdatePolygon(PolygonForm form, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.addOrUpdatePolygon(form, ipAddress);
        } catch (Exception e) {
            logger.error("多边形围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增/修改行政区划
     */
    @RequestMapping(value = "/addOrUpdateAdministration", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOrUpdateAdministration(AdministrationForm form, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.addOrUpdateAdministration(form, ipAddress);
        } catch (Exception e) {
            logger.error("行政区划围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 判断围栏是否可以修改(判断是否关联了正在执行的排班或者任务)
     * @param fenceId 围栏id
     * @return true:可以修改; false: 不可以修改
     */
    @RequestMapping(value = "/judgeFenceCanBeUpdate", method = RequestMethod.POST)
    @ResponseBody
    public boolean judgeFenceCanBeUpdate(String fenceId) {
        try {
            return fenceManagementService.judgeFenceCanBeUpdate(fenceId);
        } catch (Exception e) {
            logger.error("判断围栏是否可以修改异常", e);
            return false;
        }
    }

    /**
     * 判断围栏是否可以删除(判断是否关联了排班或者任务)
     * @param fenceId 围栏id
     * @return true:可以删除; false: 不可以删除
     */
    @RequestMapping(value = "/judgeFenceCanBeDelete", method = RequestMethod.POST)
    @ResponseBody
    public boolean judgeFenceCanBeDelete(String fenceId) {
        try {
            return fenceManagementService.judgeFenceCanBeDelete(fenceId);
        } catch (Exception e) {
            logger.error("判断围栏是否可以删除异常", e);
            return false;
        }
    }

    /**
     * 删除围栏
     * @param fenceId 围栏id
     * @param type    围栏类型
     */
    @RequestMapping(value = "/deleteFence", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteFence(String fenceId, String type, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.deleteFence(fenceId, type, ipAddress);
        } catch (Exception e) {
            logger.error("删除围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得围栏详情
     * @param fenceId 围栏id
     * @param type    围栏类型
     */
    @RequestMapping(value = "/getFenceDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceDetail(String fenceId, String type) {
        try {
            return fenceManagementService.getFenceDetail(fenceId, type);
        } catch (Exception e) {
            logger.error("获得围栏详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 用户围栏显示设置页面
     */
    @RequestMapping(value = "/getUserFenceDisplaySetPage", method = RequestMethod.GET)
    public ModelAndView getUserFenceDisplaySetPage() {
        try {
            return new ModelAndView(USER_FENCE_DISPLAY_SET_PAGE)
                .addObject("userFenceDisplaySetting", fenceManagementService.getUserFenceDisplaySetting());
        } catch (Exception e) {
            logger.error("获取用户围栏显示设置页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获得围栏tree
     */
    @RequestMapping(value = "/getFenceTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceTree() {
        try {
            return fenceManagementService.getFenceTree();
        } catch (Exception e) {
            logger.error("获得围栏tree异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 保存用户围栏显示设置
     */
    @RequestMapping(value = "/saveUserFenceDisplaySet", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveUserFenceDisplaySet(String fenceIds, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fenceManagementService.saveUserFenceDisplaySet(fenceIds, ipAddress);
        } catch (Exception e) {
            logger.error("保存用户围栏显示设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
