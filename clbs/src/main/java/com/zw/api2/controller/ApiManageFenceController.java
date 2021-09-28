package com.zw.api2.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.controller.functionconfig.ManageFenceController;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.functionconfig.Administration;
import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LinePassPoint;
import com.zw.platform.domain.functionconfig.ManageFenceInfo;
import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.domain.functionconfig.TravelLine;
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
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.functionconfig.AdministrationService;
import com.zw.platform.service.functionconfig.CircleService;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.service.functionconfig.ManageFenceService;
import com.zw.platform.service.functionconfig.MarkService;
import com.zw.platform.service.functionconfig.PolygonService;
import com.zw.platform.service.functionconfig.RectangleService;
import com.zw.platform.service.functionconfig.TravelLineService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/v/fence/managefence")
@Api(tags = { "围栏管理_dev" }, description = "围栏管理相关api接口")
public class ApiManageFenceController {
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
    private MarkService markService;

    @Autowired
    private LineService lineService;

    @Autowired
    private RectangleService rectangleService;

    @Autowired
    private CircleService circleService;

    @Autowired
    private PolygonService polygonService;

    @Autowired
    private AdministrationService administrationService;

    @Autowired
    private TravelLineService travelLineService;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @ApiIgnore
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
    @ApiOperation(value = "删除围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean deleteMore(
        @ApiParam(value = "电子围栏ID,多个使用逗号隔开", required = true) @PathVariable("delItems") String delItems) {
        try {
            if (StringUtils.isNotEmpty(delItems)) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                manageFenceService.delete(delItems, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除围栏异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    /**
     * 新增
     */
    @ApiIgnore
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
    @ApiOperation(value = "新增或修改线路", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean add(@ModelAttribute("form") final LineForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                if (Converter.toBlank(form.getAddOrUpdateLineFlag()).equals("0")) { // 新增
                    List<LineForm> list = manageFenceService.findLineByName(form.getName());
                    if (list.size() == 0) {
                        return manageFenceService.add(form, ip);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                } else if (Converter.toBlank(form.getAddOrUpdateLineFlag()).equals("1")) {
                    return manageFenceService.updateLine(form, ip);// 修改
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
    @ApiOperation(value = "增加线围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean addLine(@ApiParam(value = "围栏名称", required = true) String name) {
        try {
            List<LineForm> list = manageFenceService.findLineByName(name);
            if (list.size() == 0) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("线路名称判断异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
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
        try {
            if (form != null) {
                return manageFenceService.addSegment(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增路段异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/resetSegment", method = RequestMethod.POST)
    @ApiOperation(value = "重新设置线形围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean resetSegment(@ApiParam(value = "围栏ID", required = true) String lineId) {
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
    @ApiOperation(value = "解绑线形围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean unbundleSegment(@ApiParam(value = "围栏ID", required = true) String lineId) {
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
    @ApiOperation(value = "新增关键点围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
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
    @ApiOperation(value = "新增或者修改标注围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean marker(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final MarkForm form) {
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
    @ApiOperation(value = "新增或修改圆围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean circles(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final CircleForm form) {
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
    @ApiOperation(value = "新增或修改矩形围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
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
    @ApiOperation(value = "新增或修改多边形围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean polygons(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final PolygonForm form) {
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
    @ApiOperation(value = "轨迹列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public PageGridBean getOrbitsList(final FenceConfigQuery query, String simpleQueryParam) {
        try {
            // 根据用户名获取用户id
            String userId = SystemHelper.getCurrentUser().getId().toString();
            List<VehicleInfo> vehicleList =
                vehicleService.findVehicleByUserAndGroup(userId, userService.getOrgUuidsByUser(userId), true);
            List<String> vehicleIds = new ArrayList<>();
            if (vehicleList != null && vehicleList.size() > 0) {
                for (VehicleInfo vehicle : vehicleList) {
                    vehicleIds.add(vehicle.getId());
                }
                query.setVehicleIds(vehicleIds);
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
    @ApiOperation(value = "根据id获取围栏车辆关联表信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean editFenceConfigPage(@ApiParam(value = "围栏ID", required = true) String id) {
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
    @ApiOperation(value = "保存轨迹信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
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
    @ApiOperation(value = "围栏预览,传递围栏ID#行政类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean getFenceDetail(@ApiParam(value = "围栏ID#围栏类型", required = true) String fenceIdShape) {
        try {
            if (fenceIdShape != null) {
                String[] strs = fenceIdShape.split("#");
                String fenceId = strs[0];
                String shape = strs[1];
                JSONArray dataArr = new JSONArray();
                JSONObject msg = new JSONObject();
                if ("zw_m_marker".equals(shape)) { // 查询标注详情
                    Mark mark = markService.findMarkById(fenceId);
                    if (mark != null) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", mark);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_line".equals(shape)) { // 查询线的详情
                    List<LineContent> lineList = lineService.findLineContentById(fenceId);
                    Line line = lineService.findLineById(fenceId);
                    if (lineList != null && lineList.size() != 0) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", lineList);
                        msg.put("line", line);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_rectangle".equals(shape)) { // 查询矩形的详情
                    Rectangle rectangle = rectangleService.getRectangleByID(fenceId);
                    if (rectangle != null) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", rectangle);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_circle".equals(shape)) { // 查询圆形的详情
                    Circle circle = circleService.getCircleByID(fenceId);
                    if (circle != null) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", circle);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_polygon".equals(shape)) { // 查询多边形的详情
                    List<Polygon> polygonList = polygonService.getPolygonByID(fenceId);
                    Polygon polygon = polygonService.findPolygonById(fenceId);
                    if (polygonList != null && polygonList.size() != 0) {
                        msg.put("fenceType", shape);
                        polygonList.get(0).setDescription(polygon.getDescription());
                        msg.put("fenceData", polygonList);
                        msg.put("polygon", polygon);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_administration".equals(shape)) {
                    Administration administration = administrationService.findAdministrationById(fenceId);
                    if (administration != null) {
                        msg.put("fenceType", shape);
                        msg.put("administration", administration);
                    }
                } else if ("zw_m_travel_line".equals(shape)) { // 查询导航线路的详情
                    // 根据id查询途经点的信息
                    List<LinePassPoint> passPointList = travelLineService.getPassPointById(fenceId);
                    // 根据id查询导航路线信息
                    TravelLine travelLine = travelLineService.getTravelLineById(fenceId);
                    // 根据id查询所有点信息
                    List<LineContent> allPoinsList = travelLineService.getAllPointsById(fenceId);
                    if (passPointList != null && passPointList.size() != 0) {
                        msg.put("fenceType", shape);
                        msg.put("passPointData", passPointList);
                        msg.put("travelLine", travelLine);
                        msg.put("allPoinsData", allPoinsList);
                    } else {
                        msg.put("fenceType", shape);
                        msg.put("travelLine", travelLine);
                        msg.put("allPoinsData", allPoinsList);
                    }
                }
                dataArr.add(msg);
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
    @ApiOperation(value = "新增或者修改导航路线", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean travelLine(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final GpsLine form) {
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
    @ApiOperation(value = "新增行政区域", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean addAdministration(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final AdministrationForm form) {
        try {
            if (form != null) {
                form.setAddOrUpdatePolygonFlag("0");
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("0")) { // 新增
                    return manageFenceService.addAdministration(form, ip);
                } /*else if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("1")) {

              }*/
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增行政区域", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
