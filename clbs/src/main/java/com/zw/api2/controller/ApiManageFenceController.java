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
@Api(tags = { "????????????_dev" }, description = "??????????????????api??????")
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
     * ????????????????????????
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final ManageFenceQuery query, String simpleQueryParam) {
        try {
            Page<ManageFenceInfo> result = manageFenceService.findByPage(query, simpleQueryParam);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("???????????????????????????findByPage?????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ????????????????????????(2018-2-26 ??????????????????????????????????????????)
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean deleteMore(
        @ApiParam(value = "????????????ID,????????????????????????", required = true) @PathVariable("delItems") String delItems) {
        try {
            if (StringUtils.isNotEmpty(delItems)) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                manageFenceService.delete(delItems, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    /**
     * ??????
     */
    @ApiIgnore
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * ?????????????????????
     * @param form
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean add(@ModelAttribute("form") final LineForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                if (Converter.toBlank(form.getAddOrUpdateLineFlag()).equals("0")) { // ??????
                    List<LineForm> list = manageFenceService.findLineByName(form.getName());
                    if (list.size() == 0) {
                        return manageFenceService.add(form, ip);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                } else if (Converter.toBlank(form.getAddOrUpdateLineFlag()).equals("1")) {
                    return manageFenceService.updateLine(form, ip);// ??????
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     * @param name ??????????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = "/addLine", method = RequestMethod.POST)
    @ApiOperation(value = "???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean addLine(@ApiParam(value = "????????????", required = true) String name) {
        try {
            List<LineForm> list = manageFenceService.findLineByName(name);
            if (list.size() == 0) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????(??????????????????)
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/resetSegment", method = RequestMethod.POST)
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean resetSegment(@ApiParam(value = "??????ID", required = true) String lineId) {
        try {
            manageFenceService.resetSegment(lineId);
            manageFenceService.unbundleSegment(lineId);
            //????????????????????????????????????????????????
            ZMQFencePub.pubChangeFence("14");
        } catch (Exception e) {
            log.error("resetSegment??????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = "/unbundleSegment", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean unbundleSegment(@ApiParam(value = "??????ID", required = true) String lineId) {
        try {
            manageFenceService.unbundleSegment(lineId);
        } catch (Exception e) {
            log.error("unbundleSegment??????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ???????????????
     * @param form
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @RequestMapping(value = "/addMonitoringTag", method = RequestMethod.POST)
    @ApiOperation(value = "?????????????????????", authorizations = {
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
            log.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ?????????????????????
     * @param form
     * @return JsonResultBean
     * @Title: marker
     * @author Liubangquan
     */
    @RequestMapping(value = "/marker", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean marker(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final MarkForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                if (Converter.toBlank(form.getAddOrUpdateMarkerFlag()).equals("0")) { // ??????
                    return manageFenceService.addMarker(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateMarkerFlag()).equals("1")) { // ??????
                    return manageFenceService.updateMarker(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @param form
     * @return JsonResultBean
     * @Title: circles
     * @author Liubangquan
     */
    @RequestMapping(value = "/circles", method = RequestMethod.POST)
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean circles(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final CircleForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                ZMQFencePub.pubChangeFence("4");
                if (Converter.toBlank(form.getAddOrUpdateCircleFlag()).equals("0")) { // ??????
                    return manageFenceService.addCircles(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateCircleFlag()).equals("1")) { // ??????
                    return manageFenceService.updateCircle(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ?????????????????????
     * @param form
     * @return JsonResultBean
     * @Title: rectangles
     * @author Liubangquan
     */
    @RequestMapping(value = "/rectangles", method = RequestMethod.POST)
    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean rectangles(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final RectangleForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                if (Converter.toBlank(form.getAddOrUpdateRectangleFlag()).equals("0")) { // ??????
                    return manageFenceService.addRectangles(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateRectangleFlag()).equals("1")) { // ??????
                    return manageFenceService.updateRectangle(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     * @param form
     * @return JsonResultBean
     * @Title: polygons
     * @author Liubangquan
     */
    @RequestMapping(value = "/polygons", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean polygons(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final PolygonForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("0")) { // ??????
                    return manageFenceService.addPolygons(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("1")) { // ??????
                    return manageFenceService.updatePolygon(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     * @param query
     * @param simpleQueryParam
     * @return PageGridBean
     * @Title: getOrbitsList
     * @author Liubangquan
     */
    @RequestMapping(value = { "/orbitList" }, method = RequestMethod.POST)
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public PageGridBean getOrbitsList(final FenceConfigQuery query, String simpleQueryParam) {
        try {
            // ???????????????????????????id
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
            log.error("????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * ??????id?????????????????????????????????
     * @param id
     * @return JsonResultBean
     * @Title: editFenceConfigPage
     * @author Liubangquan
     */
    @RequestMapping(value = "/editFenceConfig_{id}", method = RequestMethod.POST)
    @ApiOperation(value = "??????id?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean editFenceConfigPage(@ApiParam(value = "??????ID", required = true) String id) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("fenceConfig", fenceConfigService.getFenceConfigById(id));
            return new JsonResultBean(jsonObj);
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @param form
     * @return JsonResultBean
     * @Title: editFenceConfig
     * @author Liubangquan
     */
    @RequestMapping(value = "/orbitAdd", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean editFenceConfig(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final LineForm form) {
        try {
            // ??????????????????
            String createDataUsername = SystemHelper.getCurrentUsername();
            form.setCreateDataUsername(createDataUsername);
            fenceConfigService.editFenceConfig(form);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     * @param fenceIdShape
     * @return JsonResultBean
     * @Title: getFenceDetail
     * @author Liubangquan
     */
    @RequestMapping(value = "/previewFence", method = RequestMethod.POST)
    @ApiOperation(value = "????????????,????????????ID#????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean getFenceDetail(@ApiParam(value = "??????ID#????????????", required = true) String fenceIdShape) {
        try {
            if (fenceIdShape != null) {
                String[] strs = fenceIdShape.split("#");
                String fenceId = strs[0];
                String shape = strs[1];
                JSONArray dataArr = new JSONArray();
                JSONObject msg = new JSONObject();
                if ("zw_m_marker".equals(shape)) { // ??????????????????
                    Mark mark = markService.findMarkById(fenceId);
                    if (mark != null) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", mark);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_line".equals(shape)) { // ??????????????????
                    List<LineContent> lineList = lineService.findLineContentById(fenceId);
                    Line line = lineService.findLineById(fenceId);
                    if (lineList != null && lineList.size() != 0) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", lineList);
                        msg.put("line", line);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_rectangle".equals(shape)) { // ?????????????????????
                    Rectangle rectangle = rectangleService.getRectangleByID(fenceId);
                    if (rectangle != null) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", rectangle);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_circle".equals(shape)) { // ?????????????????????
                    Circle circle = circleService.getCircleByID(fenceId);
                    if (circle != null) {
                        msg.put("fenceType", shape);
                        msg.put("fenceData", circle);
                        // return new JsonResultBean(msg);
                    }
                } else if ("zw_m_polygon".equals(shape)) { // ????????????????????????
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
                } else if ("zw_m_travel_line".equals(shape)) { // ???????????????????????????
                    // ??????id????????????????????????
                    List<LinePassPoint> passPointList = travelLineService.getPassPointById(fenceId);
                    // ??????id????????????????????????
                    TravelLine travelLine = travelLineService.getTravelLineById(fenceId);
                    // ??????id?????????????????????
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @param form
     * @return JsonResultBean
     */
    @RequestMapping(value = "/travelLine", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean travelLine(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final GpsLine form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                if (Converter.toBlank(form.getAddOrUpdateTravelFlag()).equals("0")) { // ??????
                    return manageFenceService.addTravelLine(form, ip);
                } else if (Converter.toBlank(form.getAddOrUpdateTravelFlag()).equals("1")) { // ??????
                    return manageFenceService.updateTravelLine(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @return JsonResultBean
     * @Title: editFenceConfigPage
     * @author yangyi
     */
    @RequestMapping(value = "/addAdministration", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean addAdministration(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final AdministrationForm form) {
        try {
            if (form != null) {
                form.setAddOrUpdatePolygonFlag("0");
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("0")) { // ??????
                    return manageFenceService.addAdministration(form, ip);
                } /*else if (Converter.toBlank(form.getAddOrUpdatePolygonFlag()).equals("1")) {

              }*/
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
