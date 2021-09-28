package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.functionconfig.Administration;
import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LinePassPoint;
import com.zw.platform.domain.functionconfig.ManageFenceInfo;
import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.domain.functionconfig.TravelLine;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.functionconfig.form.RectangleForm;
import com.zw.platform.domain.functionconfig.query.ManageFenceQuery;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.service.functionconfig.AdministrationService;
import com.zw.platform.service.functionconfig.CircleService;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.service.functionconfig.ManageFenceService;
import com.zw.platform.service.functionconfig.MarkService;
import com.zw.platform.service.functionconfig.PolygonService;
import com.zw.platform.service.functionconfig.RectangleService;
import com.zw.platform.service.functionconfig.TravelLineService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Administrator on 2016/8/3.
 */
@Controller
@RequestMapping("/swagger/m/managefence")
@Api(tags = { "围栏管理" }, description = "围栏管理相关api接口")
public class SwaggerManageFenceController {
    private static Logger log = LogManager.getLogger(SwaggerManageFenceController.class);

    @Autowired
    private ManageFenceService manageFenceService;

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
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 分页查询用户
     */
    @Auth
    @ApiOperation(value = "分页查询围栏列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", required = false, paramType = "query",
            dataType = "string") })
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
     * 批量删除
     */
    @ApiOperation(value = "根据围栏id集合批量删除围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "围栏id集合串（用逗号隔开多个id）", required = true, paramType = "query",
        dataType = "string")
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
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 ManageFence
     */
    @ApiOperation(value = "根据围栏id删除围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
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
            log.error("删除围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增或修改线路
     * @param form
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @ApiOperation(value = "新增或者修改线路", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "线路名称,长度不超过20", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "pointSeqs", value = "点序号集合（用逗号隔开）。例:0,1", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "longitudes", value = "经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)。例：106.33,106.15",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "latitudes", value = "纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "线路类型(类型包括：国道，省道，县道，高速，高架立交，其他小路)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "width", value = "偏移量，数字型", required = true, paramType = "query", dataType = "String"),
        @ApiImplicitParam(name = "addOrUpdateLineFlag", value = "新增或者修改的标识，0-新增；1-修改", required = true,
            paramType = "query", dataType = "string", defaultValue = "0"),
        @ApiImplicitParam(name = "lineId", value = "线路id，若为修改线路时，必填（该字段是分页查询围栏接口所返回结果中的id值）", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述，长度不超过100", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final LineForm form) {
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
     * 新增或修改标注
     * @param form
     * @return JsonResultBean
     * @Title: marker
     * @author Liubangquan
     */
    @ApiOperation(value = "新增或者修改标注", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "标注名称，长度小于50", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "longitude", value = "经度", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "latitude", value = "纬度", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOrUpdateMarkerFlag", value = "新增或者修改的标识，0-新增；1-修改", required = true,
            paramType = "query", dataType = "string", defaultValue = "0"),
        @ApiImplicitParam(name = "type", value = "标注类型(类型包括：普通标记，线路点)", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "markerId", value = "围栏id，若为修改标注时，必填（该字段是分页查询围栏接口所返回结果中的id值）", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述，长度不超过100", required = false, paramType = "query",
            dataType = "string") })
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
    @ApiOperation(value = "新增或者修改圆", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "区域名称，长度不超过20", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "longitude", value = "中心点的经度", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "latitude", value = "中心点的纬度", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "radius", value = "半径，double型", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "addOrUpdateCircleFlag", value = "新增或者修改的标识，0-新增；1-修改", required = true,
            paramType = "query", dataType = "string", defaultValue = "0"),
        @ApiImplicitParam(name = "type", value = "区域类型(类型包括：危险区域，违规区域，休息区，接驳点，普通区域)，长度不超过20", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "circleId", value = "圆形id，若为修改圆时，必填（该字段是分页查询围栏接口所返回结果中的id值）", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述，长度不超过100", required = false, paramType = "query",
            dataType = "string") })
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
    @ApiOperation(value = "新增或者修改矩形", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "矩形名称,长度小于20", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "pointSeqs", value = "点序号集合（用逗号隔开）。例:0,1", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "longitudes", value = "经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)。例：29.539577,29.540921",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "latitudes", value = "纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOrUpdateRectangleFlag", value = "新增或者修改的标识，0-新增；1-修改", required = true,
            paramType = "query", dataType = "string", defaultValue = "0"),
        @ApiImplicitParam(name = "type", value = "区域类型(类型包括：危险区域，违规区域，休息区，接驳点，普通区域)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "rectangleId", value = "矩形id，若为修改矩形时，必填（该字段是分页查询围栏接口所返回结果中的id值）", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述，长度不超过100", required = false, paramType = "query",
            dataType = "string") })
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
    @ApiOperation(value = "新增或者修改多边形", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "多边形名称，长度不超过20", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "pointSeqs", value = "点序号集合", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "longitudes", value = "经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "latitudes", value = "纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOrUpdatePolygonFlag", value = "新增或者修改的标识，0-新增；1-修改", required = true,
            paramType = "query", dataType = "string", defaultValue = "0"),
        @ApiImplicitParam(name = "type", value = "区域类型(类型包括：危险区域，违规区域，休息区，接驳点，普通区域)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "polygonId", value = "多边形id，若为修改多边形时，必填（该字段是分页查询围栏接口所返回结果中的id值）", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述，长度不超过100", required = false, paramType = "query",
            dataType = "string") })
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
     * 围栏预览
     * @param fenceIdShape
     * @return JsonResultBean
     * @throws BusinessException
     * @Title: getFenceDetail
     * @author Liubangquan
     */
    @ApiOperation(value = "根据围栏id和类型查询围栏详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "fenceId_shape",
        value = "围栏id和类型的字符串,用#隔开（围栏id#围栏类型）。" + "围栏类型：zw_m_marker：标注；zw_m_line：线；zw_m_rectangle：矩形；"
            + "zw_m_circle：圆；zw_m_polygon：多边形", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/previewFence", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceDetail(String fenceIdShape) {
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

}
