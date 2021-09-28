package com.zw.api2.controller.alarm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerAlarmSearchQuery;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.switching.SwitchTypeService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import jodd.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @version 1.0
 * @Author gfw
 * @Date 2018/12/11 13:47
 * @Description 报警查询相关API
 */
@Controller
@RequestMapping("api/a/search")
@Api(tags = { "报警查询_dev" }, description = "报警查询相关api")
public class ApiAlarmSearchController {
    private static final String LIST_PAGE = "vas/alarm/alarmSearch/alarmSearch";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static Logger logger = LogManager.getLogger(ApiAlarmSearchController.class);

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private AssignmentService assignmentService;

    @Resource
    private SwitchTypeService switchTypeService;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    boolean layerClose = false;

    /**
     * 将车辆id存入session中
     * @param vehicleId
     * @param session
     * @return
     */
    @ApiOperation(value = "将车辆id存入session中", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "session", value = "http的session", paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/addSession" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean addSession(String vehicleId, HttpSession session) {
        if (StringUtils.isNotBlank(vehicleId)) {
            try {
                // 获取前端页面vid传入session,
                session.setAttribute("vehicleIdAlram", vehicleId);
                return true;
            } catch (Exception e) {
                logger.error("addSession方法异常", e);
            }
        }
        return false;
    }

    /**
     * 实时监控双击报警信息跳转到报警查询页面
     * @param avid
     * @param atype
     * @param atime
     * @return 页面跳转
     */
    @ApiIgnore
    @ApiOperation(value = "实时监控双击报警信息跳转到报警查询页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "avid", value = "报警车辆id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "atype", value = "2为点击全局报警,0为实时监控点击进入此页面", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "atime", value = "最早报警开始时间", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "session", value = "http的session", paramType = "query", dataType = "string") })
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage(String avid, String atype, String atime, HttpSession session) {
        try {
            // atyp:2为点击全局报警,0为实时监控点击进入此页面
            ModelAndView mv = new ModelAndView(LIST_PAGE);
            List<AlarmType> alarm = alarmSearchService.getAlarmType();// 查询所需的报警类型
            List<SwitchType> ioSwitchType = switchTypeService.getIoSwitchType();
            JSONObject alarmTypeName = installIoAlarmTypeTree(ioSwitchType);
            mv.addObject("alarmTypeName", alarmTypeName);
            mv.addObject("type", JSON.toJSONString(alarm));
            out:
            if (StringUtil.isNotBlank(atype)) {
                if ("2".equals(atype)) {
                    // 获取session
                    Object vehicleIdAlram = session.getAttribute("vehicleIdAlram");
                    if (vehicleIdAlram != null && StringUtils.isNotBlank(vehicleIdAlram.toString())) {
                        avid = vehicleIdAlram.toString();
                        String startTime = alarmSearchService.getAlarmTime(avid);
                        atime = startTime;
                    } else {
                        break out;
                    }
                }
                // 如果是实时监控、实时视频跳转, 则查询开始时间为atime(最早报警开始时间)
                // 通过vid查询车辆树组织ID
                String assignIds = assignmentService.getAssignsByMonitorId(avid);
                mv.addObject("assignIds", assignIds);
                mv.addObject("atype", atype);
                mv.addObject("avid", avid);
                mv.addObject("atime", atime);
            }
            return mv;
        } catch (Exception e) {
            logger.error("实时监控双击报警信息跳转到报警查询页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 分页查询,String[] vehicleList
     */
    @ApiOperation(value = "查询报警列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "alarmSource", value = "报警来源", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "alarmType", value = "通讯类型", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "status", value = "处理状态", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "alarmStartTime", value = "报警开始时间", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "alarmEndTime", value = "报警结束时间", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "vehicleList", value = "报警车辆id集合", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "pushType", value = "全局报警状态", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "response", value = "http响应", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "session", value = "http的Session", paramType = "query", dataType = "int") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getListPage(int alarmSource, String alarmType, int status, String alarmStartTime,
        String alarmEndTime, String vehicleList, int pushType, HttpServletResponse response, HttpSession session) {
        JSONObject msg = new JSONObject();
        try {
            /* List<AlarmHandle> result = new ArrayList<>();
            if (!"".equals(vehicleList)) {
                List<byte[]> vehicleIds = UuidUtils.filterVid(vehicleList);
                AlarmSearchQuery query = new AlarmSearchQuery();
                if (StringUtils.isNotBlank(alarmEndTime)) {
                    query.setAlarmEndTime(DateUtils.parseDate(alarmEndTime, DATE_FORMAT).getTime());
                }
                if (StringUtils.isNotBlank(alarmStartTime)) {
                    query.setAlarmStartTime(DateUtils.parseDate(alarmStartTime, DATE_FORMAT).getTime());
                }
                query.setStatus(status);
                query.setPushType(pushType);
                query.setType(alarmType);
                query.setAlarmSource(alarmSource);
                // 进入habase查询数据
                result = alarmSearchService.getAlarmHandle(vehicleIds, query);
            }

            String userId = SystemHelper.getCurrentUser().getId().toString();
            String key = RedisHelper.buildKey(userId, "alarmDeal", "list");
            RedisHelper.del(key, PublicVariable.REDIS_ELEVEN_DATABASE); // 再次查询前删除 key
            // 获取组装数据存入redis管道
            RedisHelper.rpushPipeline(key, result, PublicVariable.REDIS_ELEVEN_DATABASE);*/
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("分页查询（getAlarmHandle）异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "分页查询报警列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "start", value = "启始页", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "length", value = "每页查询数", paramType = "query", dataType = "int") })
    @RequestMapping(value = { "/getAlarmList" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAlarmList(
        @ModelAttribute("swaggerAlarmSearchQuery") SwaggerAlarmSearchQuery swaggerAlarmSearchQuery) {
        AlarmSearchQuery query = new AlarmSearchQuery();
        BeanUtils.copyProperties(swaggerAlarmSearchQuery, query);
        Page<AlarmHandle> results = new Page<>();
        try {
            // redis分页查询
            /*    String userId = SystemHelper.getCurrentUser().getId().toString();
            String key = RedisHelper.buildKey(userId, "alarmDeal", "list");
            List<AlarmHandle> result = RedisHelper
                .lrange(key, (query.getStart() + 1), (query.getStart() + query.getLimit()),
                    PublicVariable.REDIS_ELEVEN_DATABASE);
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    AlarmHandle alarm = JSON.parseObject(JSON.toJSONString(result.get(i)), AlarmHandle.class);
                    //重写了开始时间和结束时间的get方法,get时候已经转了毫秒值
                    if (alarm.getAlarmStartTime() != null) {
                        String starTime = DateFormatUtils.format(alarm.getAlarmStartTime(), DATE_FORMAT);
                        alarm.setStartTime(starTime);
                    }
                    if (alarm.getAlarmEndTime() != null && alarm.getAlarmEndTime() != 0) {
                        String endTime = DateFormatUtils.format(alarm.getAlarmEndTime(), DATE_FORMAT);
                        alarm.setEndTime(endTime);
                    }
                    if (alarm.getHandleTime() != null) {
                        String handleTime =
                            DateFormatUtils.format(Long.parseLong(alarm.getHandleTime() + "000"), DATE_FORMAT);
                        alarm.setHandleTime(handleTime);
                    }
                    String redisColor = alarmSearchService.getPlateColorByVehicleId(alarm.getVehicleId());
                    alarm.setPlateColor(redisColor);
                    String plateColor = VehicleUtil.getPlateColorStr(redisColor);
                    alarm.setPlateColorString(plateColor);
                    AlarmSearchServiceImpl impl = (AlarmSearchServiceImpl) alarmSearchService;
                    if (null != alarm.getAlarmStartLocation()) {
                        String address1 = impl.getLocation(alarm.getAlarmStartLocation());
                        alarm.setAlarmStartSpecificLocation(address1);
                    }
                    if (null != alarm.getAlarmEndLocation()) {
                        String address2 = impl.getLocation(alarm.getAlarmEndLocation());
                        alarm.setAlarmEndSpecificLocation(address2);
                    }
                    if (alarm.getAlarmStartLocation() != null && !alarm.getAlarmStartLocation().equals("")) {
                        String[] address = alarm.getAlarmStartLocation().split(",");
                        alarm.setLongtitude(address[0]);
                        alarm.setLatitude(address[1]);
                    }
                    String alarmType = AlarmTypeUtil.getAlarmType(String.valueOf(alarm.getAlarmType()));
                    alarm.setDescription(alarmType);
                    result.set(i, alarm);
                }
                results = RedisUtil.queryPageList(result, query, key, PublicVariable.REDIS_ELEVEN_DATABASE);
                return new PageGridBean(query, results, true);
            } else {
                return new PageGridBean(query, results, true);
            }*/
            return null;
        } catch (Exception e) {
            logger.error("分页查询车辆信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        } finally {
            results.close();
        }
    }

    /**
     * 导出(生成excel文件)
     * 忽略导出
     * @param response
     */
    @ApiIgnore
    @ApiOperation(value = "导出报警列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export2(HttpServletResponse response) {
        try {
          /*  ExportExcelUtil.setResponseHead(response, "报警列表");
            String userId = SystemHelper.getCurrentUser().getId().toString();
            String key = RedisHelper.buildKey(userId, "alarmDeal", "list");
            List<AlarmHandle> result = RedisHelper.lrangePipeline(key, 1, -1, PublicVariable.REDIS_ELEVEN_DATABASE);*/
            // alarmSearchService.export(null, 1, response, result); 报警导出已经改为离线导出
        } catch (Exception e) {
            logger.error("导出excel文件界面弹出异常", e);
        } finally {
            layerClose = true;
        }
    }

    @ApiIgnore
    @RequestMapping(value = { "/export" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export1() {
        if (layerClose == true) {
            layerClose = false;
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改单条状态
     */
    @ApiIgnore
    @RequestMapping(value = { "/findEndTime" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findEndTime(String vehicleId, String type, String startTime) {
        try {
            String[] alarm = type.split(",");
            if (alarm.length != 1) {
                return new JsonResultBean(JsonResultBean.SUCCESS, "0");
            }
            long time = DateUtils.parseDate(startTime, DATE_FORMAT).getTime();
            String endTime = alarmSearchService.getLatestAlarmHandle(vehicleId, Integer.parseInt(alarm[0]), time);
            if ("0".equals(endTime)) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("修改单条状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "报警处理页面弹出", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车辆id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "类型",
            defaultValue = "0", paramType = "query", dataType = "string", required = false) })
    @RequestMapping(value = { "/alarmDeal" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean alarmDeal(String vid, String type) {
        try {
            JSONObject msg = new JSONObject();
            // String sim = "";
            // String device = "";
            // String deviceType = "";
            // Map vehicleInfo = VehicleUtil.findVehicleConfigInfo(vid);
            // if (vehicleInfo != null) {
            //     sim = vehicleInfo.get("simcardNumber").toString();
            //     device = vehicleInfo.get("deviceNumber").toString();
            //     deviceType = vehicleInfo.get("deviceType").toString();
            // }
            // msg.put("sim", sim);
            // msg.put("device", device);
            // msg.put("type", deviceType);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("报警处理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 查询车辆颜色
     * 没有进行调用
     * @param brand
     * @return
     */
    @ApiIgnore
    @ApiOperation(value = "查询车辆颜色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "brand", value = "车牌号", paramType = "query", dataType = "string")
    @RequestMapping(value = { "/findColor" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findColor(String brand) {
        try {
            JSONObject msg = new JSONObject();
            String[] str = brand.split(",");
            String color = null;
            String plateColor = null;
            for (int i = 0; i < str.length; i++) {
                color = vehicleService.findColorByBrand(brand);
                plateColor = VehicleUtil.getPlateColorStr(color);
            }
            msg.put("msg", plateColor);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("查询车辆颜色异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 模糊搜索，重组树结构
     * @param type
     * @param queryParam 查询条件
     * @param deviceType 查询类型
     * @return
     */
    @ApiOperation(value = "模糊搜索，重组树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "组织id或是分组id",
            paramType = "query", dataType = "string", required = true),
        @ApiImplicitParam(name = "queryParam", value = "查询条件",
            paramType = "query", dataType = "string", required = true),
        @ApiImplicitParam(name = "webType", value = "当前所属界面",
            paramType = "query", dataType = "int", defaultValue = "1", required = true) })
    @RequestMapping(value = "/monitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTreeFuzzy(String type, String queryParam, String deviceType, Integer webType) {
        try {
            String result = vehicleService.monitorTreeFuzzy(type, queryParam, "name", "monitor", deviceType, webType,
                null, null)
                .toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("模糊搜索车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 模糊搜索，返回监控对象数量
     * @param type
     * @param queryParam 查询条件
     * @param deviceType 查询类型
     * @return
     */
    @ApiOperation(value = "模糊搜索，返回监控对象数量", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "组织id或是分组id",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "queryParam", value = "查询条件",
            paramType = "query", dataType = "string", required = true),
        @ApiImplicitParam(name = "deviceType", value = "设备类型",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "webType", value = "当前所属界面",
            paramType = "query", dataType = "int", defaultValue = "1", required = true) })
    @RequestMapping(value = "/monitorTreeFuzzyCount", method = RequestMethod.POST)
    @ResponseBody
    public int getMonitorTreeFuzzyCount(String type, String queryParam, String queryType, String deviceType) {
        try {
            if (StringUtils.isBlank(queryType)) {
                queryType = "name";
            }
            int monitorCount = vehicleService.monitorTreeFuzzyCount(type, queryParam, queryType, "monitor", deviceType);
            return monitorCount;
        } catch (Exception e) {
            logger.error("模糊搜索车辆树信息异常", e);
            return 0;
        }
    }

    /**
     * 组装报警类型名称树
     * @param ioSwitchType
     * @return
     */
    private JSONObject installIoAlarmTypeTree(List<SwitchType> ioSwitchType) {
        JSONObject alarmTypeName = new JSONObject();
        JSONArray tree = new JSONArray();
        if (CollectionUtils.isNotEmpty(ioSwitchType)) {
            for (int i = 0; i < ioSwitchType.size(); i++) {
                SwitchType switchType = ioSwitchType.get(i);
                String identify = switchType.getIdentify();
                JSONObject parentNode = new JSONObject();
                parentNode.put("isParent", true);
                parentNode.put("name", switchType.getName());
                parentNode.put("id", identify);
                parentNode.put("pId", 0);
                parentNode.put("isCondition", false);
                JSONObject childNodeOne = new JSONObject();
                childNodeOne.put("isParent", false);
                childNodeOne.put("name", switchType.getStateOne());
                childNodeOne.put("id", identify + "1");
                childNodeOne.put("pId", identify);
                childNodeOne.put("isCondition", true);
                JSONObject childNodeOTwo = new JSONObject();
                childNodeOTwo.put("isParent", false);
                childNodeOTwo.put("name", switchType.getStateTwo());
                childNodeOTwo.put("id", identify + "2");
                childNodeOTwo.put("pId", identify);
                childNodeOTwo.put("isCondition", true);
                JSONObject abnormalIo = new JSONObject();
                abnormalIo.put("isParent", false);
                abnormalIo.put("name", switchType.getName() + "异常");
                abnormalIo.put("id", identify + "3");
                abnormalIo.put("pId", identify);
                abnormalIo.put("isCondition", true);
                tree.add(parentNode);
                tree.add(childNodeOne);
                tree.add(childNodeOTwo);
                tree.add(abnormalIo);
            }
        }
        for (int i = 90; i <= 92; i++) {
            JSONObject sensor = new JSONObject();
            sensor.put("isParent", false);
            sensor.put("name", i == 90 ? "终端I/O异常" : (i == 91 ? "I/O采集1异常" : "I/O采集2异常"));
            sensor.put("id", "0x" + i);
            sensor.put("pId", 0);
            sensor.put("isCondition", true);
            tree.add(sensor);
        }
        alarmTypeName.put("tree", tree);
        return alarmTypeName;
    }
}
