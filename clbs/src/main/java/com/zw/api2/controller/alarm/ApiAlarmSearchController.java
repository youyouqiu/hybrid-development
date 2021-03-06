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
 * @Description ??????????????????API
 */
@Controller
@RequestMapping("api/a/search")
@Api(tags = { "????????????_dev" }, description = "??????????????????api")
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
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    boolean layerClose = false;

    /**
     * ?????????id??????session???
     * @param vehicleId
     * @param session
     * @return
     */
    @ApiOperation(value = "?????????id??????session???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "??????id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "session", value = "http???session", paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/addSession" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean addSession(String vehicleId, HttpSession session) {
        if (StringUtils.isNotBlank(vehicleId)) {
            try {
                // ??????????????????vid??????session,
                session.setAttribute("vehicleIdAlram", vehicleId);
                return true;
            } catch (Exception e) {
                logger.error("addSession????????????", e);
            }
        }
        return false;
    }

    /**
     * ?????????????????????????????????????????????????????????
     * @param avid
     * @param atype
     * @param atime
     * @return ????????????
     */
    @ApiIgnore
    @ApiOperation(value = "?????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "avid", value = "????????????id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "atype", value = "2?????????????????????,0????????????????????????????????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "atime", value = "????????????????????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "session", value = "http???session", paramType = "query", dataType = "string") })
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage(String avid, String atype, String atime, HttpSession session) {
        try {
            // atyp:2?????????????????????,0????????????????????????????????????
            ModelAndView mv = new ModelAndView(LIST_PAGE);
            List<AlarmType> alarm = alarmSearchService.getAlarmType();// ???????????????????????????
            List<SwitchType> ioSwitchType = switchTypeService.getIoSwitchType();
            JSONObject alarmTypeName = installIoAlarmTypeTree(ioSwitchType);
            mv.addObject("alarmTypeName", alarmTypeName);
            mv.addObject("type", JSON.toJSONString(alarm));
            out:
            if (StringUtil.isNotBlank(atype)) {
                if ("2".equals(atype)) {
                    // ??????session
                    Object vehicleIdAlram = session.getAttribute("vehicleIdAlram");
                    if (vehicleIdAlram != null && StringUtils.isNotBlank(vehicleIdAlram.toString())) {
                        avid = vehicleIdAlram.toString();
                        String startTime = alarmSearchService.getAlarmTime(avid);
                        atime = startTime;
                    } else {
                        break out;
                    }
                }
                // ??????????????????????????????????????????, ????????????????????????atime(????????????????????????)
                // ??????vid?????????????????????ID
                String assignIds = assignmentService.getAssignsByMonitorId(avid);
                mv.addObject("assignIds", assignIds);
                mv.addObject("atype", atype);
                mv.addObject("avid", avid);
                mv.addObject("atime", atime);
            }
            return mv;
        } catch (Exception e) {
            logger.error("???????????????????????????????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????,String[] vehicleList
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "alarmSource", value = "????????????", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "alarmType", value = "????????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "status", value = "????????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "alarmStartTime", value = "??????????????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "alarmEndTime", value = "??????????????????", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "vehicleList", value = "????????????id??????", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "pushType", value = "??????????????????", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "response", value = "http??????", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "session", value = "http???Session", paramType = "query", dataType = "int") })
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
                // ??????habase????????????
                result = alarmSearchService.getAlarmHandle(vehicleIds, query);
            }

            String userId = SystemHelper.getCurrentUser().getId().toString();
            String key = RedisHelper.buildKey(userId, "alarmDeal", "list");
            RedisHelper.del(key, PublicVariable.REDIS_ELEVEN_DATABASE); // ????????????????????? key
            // ????????????????????????redis??????
            RedisHelper.rpushPipeline(key, result, PublicVariable.REDIS_ELEVEN_DATABASE);*/
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("???????????????getAlarmHandle?????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "start", value = "?????????", paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "length", value = "???????????????", paramType = "query", dataType = "int") })
    @RequestMapping(value = { "/getAlarmList" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAlarmList(
        @ModelAttribute("swaggerAlarmSearchQuery") SwaggerAlarmSearchQuery swaggerAlarmSearchQuery) {
        AlarmSearchQuery query = new AlarmSearchQuery();
        BeanUtils.copyProperties(swaggerAlarmSearchQuery, query);
        Page<AlarmHandle> results = new Page<>();
        try {
            // redis????????????
            /*    String userId = SystemHelper.getCurrentUser().getId().toString();
            String key = RedisHelper.buildKey(userId, "alarmDeal", "list");
            List<AlarmHandle> result = RedisHelper
                .lrange(key, (query.getStart() + 1), (query.getStart() + query.getLimit()),
                    PublicVariable.REDIS_ELEVEN_DATABASE);
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    AlarmHandle alarm = JSON.parseObject(JSON.toJSONString(result.get(i)), AlarmHandle.class);
                    //???????????????????????????????????????get??????,get???????????????????????????
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
            logger.error("??????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        } finally {
            results.close();
        }
    }

    /**
     * ??????(??????excel??????)
     * ????????????
     * @param response
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export2(HttpServletResponse response) {
        try {
          /*  ExportExcelUtil.setResponseHead(response, "????????????");
            String userId = SystemHelper.getCurrentUser().getId().toString();
            String key = RedisHelper.buildKey(userId, "alarmDeal", "list");
            List<AlarmHandle> result = RedisHelper.lrangePipeline(key, 1, -1, PublicVariable.REDIS_ELEVEN_DATABASE);*/
            // alarmSearchService.export(null, 1, response, result); ????????????????????????????????????
        } catch (Exception e) {
            logger.error("??????excel????????????????????????", e);
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
     * ??????????????????
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
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "??????id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "??????",
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
            logger.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ??????????????????
     * ??????????????????
     * @param brand
     * @return
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "brand", value = "?????????", paramType = "query", dataType = "string")
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
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????????????????
     * @param type
     * @param queryParam ????????????
     * @param deviceType ????????????
     * @return
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "??????id????????????id",
            paramType = "query", dataType = "string", required = true),
        @ApiImplicitParam(name = "queryParam", value = "????????????",
            paramType = "query", dataType = "string", required = true),
        @ApiImplicitParam(name = "webType", value = "??????????????????",
            paramType = "query", dataType = "int", defaultValue = "1", required = true) })
    @RequestMapping(value = "/monitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTreeFuzzy(String type, String queryParam, String deviceType, Integer webType) {
        try {
            String result = vehicleService.monitorTreeFuzzy(type, queryParam, "name", "monitor", deviceType, webType,
                null, null)
                .toJSONString();
            // ????????????
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
            return null;
        }
    }

    /**
     * ???????????????????????????????????????
     * @param type
     * @param queryParam ????????????
     * @param deviceType ????????????
     * @return
     */
    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "??????id????????????id",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "queryParam", value = "????????????",
            paramType = "query", dataType = "string", required = true),
        @ApiImplicitParam(name = "deviceType", value = "????????????",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "webType", value = "??????????????????",
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
            logger.error("?????????????????????????????????", e);
            return 0;
        }
    }

    /**
     * ???????????????????????????
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
                abnormalIo.put("name", switchType.getName() + "??????");
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
            sensor.put("name", i == 90 ? "??????I/O??????" : (i == 91 ? "I/O??????1??????" : "I/O??????2??????"));
            sensor.put("id", "0x" + i);
            sensor.put("pId", 0);
            sensor.put("isCondition", true);
            tree.add(sensor);
        }
        alarmTypeName.put("tree", tree);
        return alarmTypeName;
    }
}
