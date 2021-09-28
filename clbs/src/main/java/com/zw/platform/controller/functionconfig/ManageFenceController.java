package com.zw.platform.controller.functionconfig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.ManageFenceInfo;
import com.zw.platform.domain.functionconfig.form.AdministrationForm;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.GpsLine;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.LineSegmentContentForm;
import com.zw.platform.domain.functionconfig.form.LineSpotForm;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.functionconfig.form.RectangleForm;
import com.zw.platform.domain.functionconfig.query.FenceConfigQuery;
import com.zw.platform.domain.functionconfig.query.ManageFenceQuery;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.functionconfig.FenceService;
import com.zw.platform.service.functionconfig.ManageFenceService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Administrator on 2016/8/3.
 */
@Controller
@RequestMapping("/m/functionconfig/fence/managefence")
public class ManageFenceController {
    private static Logger log = LogManager.getLogger(ManageFenceController.class);

    private static final String LIST_PAGE = "modules/functionconfig/fence/managefence/list";

    private static final String ADD_PAGE = "modules/functionconfig/fence/managefence/add";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private ManageFenceService manageFenceService;

    @Autowired
    private FenceConfigService fenceConfigService;

    @Autowired
    FenceService fenceService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询围栏信息
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final ManageFenceQuery query, String simpleQueryParam) {
        try {
            Page<ManageFenceInfo> result = manageFenceService.findByPage(query, simpleQueryParam);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询围栏信息（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 批量删除电子围栏(2018-2-26 平台暂无批量删除电子围栏功能)
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return manageFenceService.delete(items, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除 ManageFence
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return manageFenceService.delete(id, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除电子围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增或修改线路
     * @param form
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final LineForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);
                //新增
                if (Objects.equals(form.getAddOrUpdateLineFlag(), "0")) {
                    List<LineForm> list = manageFenceService.findLineByName(form.getName());
                    if (list.size() == 0) {
                        return manageFenceService.add(form, ip);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                } else if (Objects.equals(form.getAddOrUpdateLineFlag(), "1")) {
                    //进行修改
                    return manageFenceService.updateLine(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("电子围栏新增线路异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 将历史轨迹添加为线围栏时判断线路名称是否存在
     * @param name 新增围栏名称
     * @return JsonResultBean
     */
    @RequestMapping(value = "/addLine", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean isExistLineFence(String name) {
        List<LineForm> list = manageFenceService.findLineByName(name);
        return new JsonResultBean(list.size() == 0);
    }

    /**
     * 新增路段(线形围栏分段)
     * @param form
     * @return JsonResultBean
     */
    @RequestMapping(value = "/addSegment", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public JsonResultBean addSegment(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final LineSegmentContentForm form) {
        if (form != null) {
            return manageFenceService.addSegment(form);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @RequestMapping(value = "/resetSegment", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean resetSegment(String lineId) {
        try {
            manageFenceService.resetSegment(lineId);
            manageFenceService.unbundleSegment(lineId);
            //向软围栏发送路线分段信息改变消息
            ZMQFencePub.pubChangeFence("14");
        } catch (Exception e) {
            log.error("resetSegment异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = "/unbundleSegment", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean unbundleSegment(String lineId) {
        try {
            manageFenceService.unbundleSegment(lineId);
        } catch (Exception e) {
            log.error("unbundleSegment异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 新增关键点
     * @param form
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @RequestMapping(value = "/addMonitoringTag", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addMonitoringTag(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final LineSpotForm form) {
        try {
            if (form.getLongitude() != null && form.getLatitude() != null) {
                manageFenceService.addMonitoringTag(form);
                ZMQFencePub.pubChangeFence("3");
            }
        } catch (Exception e) {
            log.error("新增或修改线路异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 新增或修改标注
     * @param form
     * @return JsonResultBean
     * @Title: marker
     * @author Liubangquan
     */
    @RequestMapping(value = "/marker", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean marker(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final MarkForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                if (Converter.toBlank(form.getAddOrUpdateMarkerFlag()).equals("0")) { // 新增
                    return manageFenceService.addMarker(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateMarkerFlag()).equals("1")) { // 修改
                    return manageFenceService.updateMarker(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增或修改标注异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增或修改圆
     * @param form
     * @return JsonResultBean
     * @Title: circles
     * @author Liubangquan
     */
    @RequestMapping(value = "/circles", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean circles(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final CircleForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                ZMQFencePub.pubChangeFence("4");
                if (Converter.toBlank(form.getAddOrUpdateCircleFlag()).equals("0")) { // 新增
                    return manageFenceService.addCircles(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateCircleFlag()).equals("1")) { // 修改
                    return manageFenceService.updateCircle(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增或修改圆异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增或修改矩形
     * @param form
     * @return JsonResultBean
     * @Title: rectangles
     * @author Liubangquan
     */
    @RequestMapping(value = "/rectangles", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean rectangles(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final RectangleForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                if (Converter.toBlank(form.getAddOrUpdateRectangleFlag()).equals("0")) { // 新增
                    return manageFenceService.addRectangles(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateRectangleFlag()).equals("1")) { // 修改
                    return manageFenceService.updateRectangle(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增或修改矩形异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增或修改多边形
     * @param form
     * @return JsonResultBean
     * @Title: polygons
     * @author Liubangquan
     */
    @RequestMapping(value = "/polygons", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean polygons(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final PolygonForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("0")) { // 新增
                    return manageFenceService.addPolygons(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("1")) { // 修改
                    return manageFenceService.updatePolygon(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增或修改多边形围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹列表
     * @param query
     * @param simpleQueryParam
     * @return PageGridBean
     * @Title: getOrbitsList
     * @author Liubangquan
     */
    @RequestMapping(value = { "/orbitList" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOrbitsList(final FenceConfigQuery query, String simpleQueryParam) {
        try {
            // 获取用户权限下的监控对象ID
            Set<String> monitorIds = userService.getCurrentUserMonitorIds();
            if (CollectionUtils.isNotEmpty(monitorIds)) {
                query.setVehicleIds(new ArrayList<>(monitorIds));
            }
            Page<FenceConfig> result = fenceConfigService.findOrbitList(query, simpleQueryParam);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("获取轨迹列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 根据id获取围栏车辆关联表信息
     * @param id
     * @return JsonResultBean
     * @Title: editFenceConfigPage
     * @author Liubangquan
     */
    @RequestMapping(value = "/editFenceConfig_{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editFenceConfigPage(String id) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("fenceConfig", fenceConfigService.getFenceConfigById(id));
            return new JsonResultBean(jsonObj);
        } catch (Exception e) {
            log.error("获取围栏车辆关联表信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 保存轨迹信息
     * @param form
     * @return JsonResultBean
     * @Title: editFenceConfig
     * @author Liubangquan
     */
    @RequestMapping(value = "/orbitAdd", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editFenceConfig(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final LineForm form) {
        try {
            // 获取当前用户
            String createDataUsername = SystemHelper.getCurrentUsername();
            form.setCreateDataUsername(createDataUsername);
            fenceConfigService.editFenceConfig(form);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("保存轨迹信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 围栏预览
     * @param fenceIdShape
     * @return JsonResultBean
     * @Title: getFenceDetail
     * @author Liubangquan
     */
    @RequestMapping(value = "/previewFence", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceDetail(String fenceIdShape) {
        try {
            if (fenceIdShape != null) {
                String[] strs = fenceIdShape.split("#");
                String fenceId = strs[0];
                String shape = strs[1];
                JSONArray dataArr = new JSONArray();
                dataArr.add(fenceService.previewFence(fenceId, shape));
                return new JsonResultBean(dataArr);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("围栏预览异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增或者修改导航路线
     * @param form
     * @return JsonResultBean
     */
    @RequestMapping(value = "/travelLine", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean travelLine(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final GpsLine form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                if (Converter.toBlank(form.getAddOrUpdateTravelFlag()).equals("0")) { // 新增
                    return manageFenceService.addTravelLine(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateTravelFlag()).equals("1")) { // 修改
                    return manageFenceService.updateTravelLine(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增或者修改导航路线异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增行政区域
     * @return JsonResultBean
     * @Title: editFenceConfigPage
     * @author yangyi
     */
    @RequestMapping(value = "/addAdministration", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addAdministration(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final AdministrationForm form) {
        try {
            if (form != null) {
                form.setAddOrUpdatePolygonFlag("0");
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("0")) { // 新增
                    return manageFenceService.addAdministration(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增行政区域", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
