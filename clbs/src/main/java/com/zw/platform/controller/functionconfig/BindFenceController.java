package com.zw.platform.controller.functionconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.form.FenceConfigForm;
import com.zw.platform.domain.functionconfig.query.FenceConfigQuery;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.functionconfig.FenceService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.validator.FenceValidator;
import com.zw.ws.common.PublicVariable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p> Title:电子围栏Controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * 2016年8月4日下午6:40:28
 *
 * @version 1.0
 * @author wangying
 */
@Controller
@RequestMapping("/m/functionconfig/fence/bindfence")
public class BindFenceController {
    private static final Logger log = LogManager.getLogger(BindFenceController.class);

    @Value("${sys.error.msg}")
    private String sysError;

    @Value("${get.monitor.tree.error}")
    private String monitorTreeError;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    UserService userService;

    @Autowired
    FenceService fenceService;

    @Autowired
    FenceConfigService fenceConfigService;

    @Autowired
    LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private MonitorTreeService monitorTreeService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private HttpServletRequest request;

    private static final String LIST_PAGE = "modules/functionconfig/fence/bindfence/list";

    private static final String EDIT_PAGE = "vas/monitoring/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    // 前端需要显示的列
    private static final String COLUMN_STR = "id,type,name,vehicle_id,brand,dirStatus,send_fence_type,alarm_source,"
        + "alarm_in_platform,alarm_out_platform,alarm_in_driver,alarm_out_driver,"
        + "alarm_start_time,alarm_end_time,alarm_start_date,alarm_end_date,speed,"
        + "over_speed_last_time,travel_long_time,travel_small_time,"
        + "open_door,communication_flag,gnss_flag,paramId,fenceId" + ",monitorType,deviceType";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final FenceConfigQuery query) {
        try {
            if (query != null) {
                Page<Map<String, Object>> result = new Page<>();
                List<Map<String, Object>> dataMap = new ArrayList<>();
                if (query.getQueryFenceIdStr() != null && !"".equals(query.getQueryFenceIdStr())) {
                    // 获取用户权限下的监控对象集合
                    Set<String> vehicleIds = userService.getCurrentUserMonitorIds();
                    if (vehicleIds != null && vehicleIds.size() > 0) {
                        query.setVehicleIds(new ArrayList<>(vehicleIds));
                    }
                    String queryStr = query.getQueryFenceIdStr();
                    String[] queryFenceId = queryStr.split(",");
                    query.setQueryFenceId(Arrays.asList(queryFenceId));
                    result = fenceConfigService.findFenceConfigByPage(query);
                    dataMap = result.getResult();
                }

                if (dataMap != null && dataMap.size() > 0) {
                    // 遍历所有列名，若没有值，默认设置为""
                    String[] column = COLUMN_STR.split(",");
                    for (Map<String, Object> map : dataMap) {
                        for (String keyStr : column) {
                            if (!map.containsKey(keyStr)) {
                                map.put(keyStr, "");
                            }
                        }
                    }
                }
                return new PageGridBean(query, result, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("查询围栏信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/vehicelTree", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTree(String type, String queryParam) {
        try {
            MonitorTreeReq monitorTreeReq = new MonitorTreeReq();
            monitorTreeReq.setMonitorType("vehicle");
            monitorTreeReq.setType(type);
            monitorTreeReq.setKeyword(queryParam);
            return monitorTreeService.getMonitorTreeFuzzy(monitorTreeReq).toJSONString();
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    @RequestMapping(value = "/monitorTree", method = RequestMethod.POST)
    @ResponseBody
    public String getMonitorTree(String type, Integer webType, Integer isCarousel) {
        try {
            String result =
                monitorTreeService.getMonitorTree(type, webType, Objects.equals(isCarousel, 1), false).toJSONString();
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 新修改接口  监控对象数据添加监控对象状态与 acc状态
     * @param type       用于组织和分组的根节点是否可选
     * @param webType    1：实时监控 2：实时视频
     * @param isCarousel 1：获取轮播树的监控对象附加通道号相关信息  其他不获取
     * @return 组织+分组+监控对象树
     */
    @RequestMapping(value = "/new/monitorTree", method = RequestMethod.POST)
    @ResponseBody
    public String getMonitorTreeNew(String type, Integer webType, Integer isCarousel) {
        try {
            boolean needCarousel = Objects.equals(1, isCarousel);
            String result = monitorTreeService.getMonitorTree(type, webType, needCarousel, true).toJSONString();
            return ZipUtil.compress(result);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 超过5000的树结构
     * @return 组织+分组
     */
    @RequestMapping(value = "/bigDataMonitorTree", method = RequestMethod.POST)
    @ResponseBody
    public String getBigDataMonitorTree(String type, Integer showTreeCountFlag) {
        try {
            //是否需要计算分组的车总数和在线的车的数量 默认需要
            if (showTreeCountFlag == null) {
                showTreeCountFlag = 1;
            }
            boolean needOnLineNum = Objects.equals(1, showTreeCountFlag);
            JSONArray treeList = monitorTreeService.getGroupTree(type, needOnLineNum, needOnLineNum, true, true);
            return ZipUtil.compress(treeList.toJSONString());
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 根据分组id查询车辆（组装成树节点的格式）
     * @param assignmentId 分组ID
     * @return 分组下的树节点
     */
    @RequestMapping(value = "/putMonitorByAssign", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean putMonitorByAssign(String assignmentId, boolean isChecked, String monitorType,
        String deviceType, Integer webType, Integer status) {
        try {
            MonitorTreeReq treeReq = new MonitorTreeReq();
            List<String> deviceTypes = treeReq.getDeviceTypes(monitorType, deviceType, webType, false);
            treeReq.setDeviceTypes(deviceTypes);
            treeReq.setChecked(isChecked);
            treeReq.setWebType(webType);
            treeReq.setMonitorType(monitorType);
            treeReq.setStatus(status);
            treeReq.setNeedAccStatus(false);
            List<String> groupIds = Arrays.asList(assignmentId.split(","));
            JSONArray result = monitorTreeService.getByGroupId(groupIds, treeReq);
            // 压缩数据
            return new JsonResultBean(ZipUtil.compress(result.toJSONString()));
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 增加监控对象在线状态和acc状态
     * @param assignmentId 分组ID
     * @param isChecked    节点是否被勾选
     * @param monitorType  monitor：监控对象 vehicle：车辆
     * @param deviceType   协议类型
     * @param webType      1：实时监控 2：实时视频 10 两课一危
     * @return 分组下监控对象树节点
     */
    @RequestMapping(value = "/new/putMonitorByAssign", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean putMonitorByAssignNew(String assignmentId, boolean isChecked, String monitorType,
        String deviceType, Integer webType, Integer status) {
        try {
            MonitorTreeReq treeReq = new MonitorTreeReq();
            List<String> deviceTypes = treeReq.getDeviceTypes(monitorType, deviceType, webType, true);
            treeReq.setDeviceTypes(deviceTypes);
            treeReq.setChecked(isChecked);
            treeReq.setWebType(webType);
            treeReq.setMonitorType(monitorType);
            treeReq.setStatus(status);
            treeReq.setNeedAccStatus(true);
            List<String> groupIds = Arrays.asList(assignmentId.split(","));
            JSONArray result = monitorTreeService.getByGroupId(groupIds, treeReq);
            // 压缩数据
            String tree = ZipUtil.compress(result.toJSONString());
            return new JsonResultBean(tree);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 根据组织id查询车辆（组装成树节点的格式）
     * @param groupId 企业ID
     * @return 分组-分组下的监控对象树集合Map
     */
    @RequestMapping(value = "/putMonitorByGroup", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean putMonitorBygroup(String groupId, boolean isChecked, String monitorType, String deviceType,
        Integer status) {
        try {
            MonitorTreeReq treeReq = new MonitorTreeReq();
            treeReq.setChecked(isChecked);
            treeReq.setMonitorType(monitorType);
            if (StringUtils.isNotBlank(deviceType) && Objects.equals(monitorType, "vehicle")) {
                treeReq.setDeviceTypes(Collections.singletonList(deviceType));
            }
            treeReq.setStatus(status);
            JSONArray treeNodes = monitorTreeService.getByOrgDn(groupId, treeReq);
            return new JsonResultBean(getGroupMonitorTreeMap(treeNodes));
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 获取企业--分组--车的树结构
     */
    @RequestMapping(value = "/getOrgAssignmentVehicle", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOrgAssignmentVehicle(String groupId, boolean isChecked, String monitorType,
        String deviceType) {
        try {
            MonitorTypeEnum monitorTypeEnum = MonitorTypeEnum.getByType(monitorType);
            MonitorTreeReq treeReq = new MonitorTreeReq();
            treeReq.setChecked(isChecked);
            if (Objects.nonNull(monitorTypeEnum)) {
                treeReq.setMonitorType(monitorTypeEnum.getEnName());
            }
            //原逻辑的deviceType未使用上，这里也暂时保留原逻辑，deviceType不参与数据过滤
            JSONArray treeNodes = monitorTreeService.getByOrgDn(groupId, treeReq);
            Map<String, JSONArray> result = getGroupMonitorTreeMap(treeNodes);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    private Map<String, JSONArray> getGroupMonitorTreeMap(JSONArray monitorTreeNodes) {
        Map<String, JSONArray> result = new HashMap<>(16);
        for (Object item : monitorTreeNodes) {
            JSONObject treeNode = (JSONObject) item;
            String assignmentId = treeNode.getString("pId");
            JSONArray groupMonitors = result.getOrDefault(treeNode.getString("pId"), new JSONArray());
            groupMonitors.add(treeNode);
            result.put(assignmentId, groupMonitors);
        }
        return result;
    }

    /**
     * 模糊搜索，重组树结构
     * @param type       组织与分组的nocheck属性
     * @param queryParam 查询条件
     * @param queryType  查询类型
     * @return 组织+分组+监控对象树
     */
    @RequestMapping(value = "/monitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getMonitorTreeFuzzy(String type, String queryParam, String queryType, String deviceType,
        Integer webType, Integer isCarousel) {
        try {
            MonitorTreeReq query = new MonitorTreeReq();
            query.setQueryType(queryType);
            query.setType(type);
            query.setKeyword(queryParam);
            if (StringUtils.isNotBlank(deviceType)) {
                query.setDeviceTypes(Collections.singletonList(deviceType));
            }
            query.setWebType(webType);
            query.setNeedCarousel(Objects.equals(isCarousel, 1));
            query.setMonitorType("monitor");
            query.setNeedOnlineMonitorCount(true);

            String result = monitorTreeService.getMonitorTreeFuzzy(query).toJSONString();
            // 压缩数据
            return ZipUtil.compress(result);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 添加监控对象在线状态和acc状态
     * @param type       组织与分组的nocheck属性
     * @param queryParam 查询条件
     * @param queryType  查询类型
     * @param deviceType 协议类型
     * @param webType    1：实时监控 2：实时视频 10 两客一危
     * @param isCarousel 是否需要 是否是轮播页面的查询 1：需要
     * @return 组织+分组+监控对象树
     */
    @RequestMapping(value = "/new/monitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getMonitorTreeFuzzyNew(String type, String queryParam, String queryType, String deviceType,
        Integer webType, Integer isCarousel) {
        try {
            //构建树查询条件
            MonitorTreeReq query = new MonitorTreeReq();
            query.setQueryType(queryType);
            query.setType(type);
            query.setKeyword(queryParam);
            if (StringUtils.isNotBlank(deviceType)) {
                query.setDeviceTypes(Collections.singletonList(deviceType));
            }
            query.setWebType(webType);
            query.setNeedCarousel(Objects.equals(isCarousel, 1));
            query.setMonitorType("monitor");
            query.setChecked(false);
            query.setNeedAccStatus(true);
            query.setNeedQuitPeople(false);
            String result = monitorTreeService.getMonitorTreeFuzzy(query).toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 模糊搜索，重组树结构
     * @param type       组织与分组的nocheck属性
     * @param queryParam 查询条件
     * @param queryType  查询类型
     * @return 组织+分组+车辆树
     */
    @RequestMapping(value = "/vehicleTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTreeFuzzy(String type, String queryParam, String queryType) {
        try {
            MonitorTreeReq query = new MonitorTreeReq();
            query.setQueryType(queryType);
            query.setType(type);
            query.setKeyword(queryParam);
            query.setNeedCarousel(false);
            query.setMonitorType("vehicle");
            query.setChecked(false);
            query.setNeedAccStatus(false);
            query.setNeedQuitPeople(false);
            String result = monitorTreeService.getMonitorTreeFuzzy(query).toJSONString();
            // 压缩数据
            return ZipUtil.compress(result);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 获取用户权限内的监控对象数量
     */
    @RequestMapping(value = "/getAssignMonitorCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAssignMonitorCount() {
        int count = 0;
        Set<String> monitorIds = userService.getCurrentUserMonitorIds();
        if (monitorIds != null && !monitorIds.isEmpty()) {
            count = monitorIds.size();
        }
        return new JsonResultBean(count);
    }

    /**
     * 获取用户权限内的监控对象数量
     */
    @RequestMapping(value = "/getTreeByMonitorCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTreeByMonitorCount(String type) {
        String result;
        try {
            if (type.equals("resource")) {
                result = monitorTreeService.getMonitorTree(type, 2, false, false).toJSONString();
            } else {
                Set<String> monitorIds = userService.getCurrentUserMonitorIds();
                if (CollectionUtils.isNotEmpty(monitorIds) && monitorIds.size() > PublicVariable.MONITOR_COUNT) {
                    JSONObject resultObject = new JSONObject();
                    JSONArray tree = monitorTreeService.getGroupTree(type, true, true, true, true);
                    resultObject.put("tree", tree);
                    resultObject.put("size", monitorIds.size());
                    if ("single".equals(type)) {
                        result = tree.toJSONString();
                    } else {
                        result = resultObject.toJSONString();
                    }
                } else {
                    result = JSON.toJSONString(monitorTreeService.getMonitorTree(type, 1, false, false),
                        SerializerFeature.DisableCircularReferenceDetect);
                }
            }
            return new JsonResultBean(ZipUtil.compress(result));
        } catch (Exception e) {
            log.info("获取监控对象树异常");
            return new JsonResultBean(JsonResultBean.FAULT, monitorTreeError);
        }

    }

    @RequestMapping(value = "/alarmSearchTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmSearchTree(String type) {
        try {
            String result = monitorTreeService.getMonitorTreeByType(type, false).toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    @RequestMapping(value = "/fenceTree", method = RequestMethod.POST)
    @ResponseBody
    public String getFenceTree(String type) {
        return fenceConfigService.getFenceTree(type);
    }

    @RequestMapping(value = "/fenceTreeByVid", method = RequestMethod.POST)
    @ResponseBody
    public String getFenceTreeByVid(String vid) {
        String result = "";
        try {
            result = fenceConfigService.findFenceConfigByVid(vid);
        } catch (Exception e) {
            log.info("获取车辆绑定的围栏树异常！");
        }
        return result;
    }

    @RequestMapping(value = { "/getFenceDetails" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceDetails(String fenceNodes) {
        List<JSONObject> nodeList = JSON.parseArray(fenceNodes, JSONObject.class);
        JSONArray dataArr = new JSONArray();
        if (nodeList != null && nodeList.size() > 0) {
            for (JSONObject obj : nodeList) {
                if (Objects.isNull(obj)) {
                    continue;
                }
                String type = obj.getString("pId");
                String id = obj.getString("id");
                dataArr.add(fenceService.getFenceDetail(id, type));
            }
            return new JsonResultBean(dataArr);
        }

        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 保存绑定详情
     * @author wangying
     */
    @Transactional
    @RequestMapping(value = { "/saveBindFence" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveBindFence(String data) {
        try {
            if (data != null && !data.isEmpty()) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fenceConfigService.addBindFence(data, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("保存围栏绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 根据id下发围栏
     */
    @RequestMapping(value = "/sendFence", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendFence(String sendParam) {
        try {
            if (sendParam != null && !sendParam.isEmpty()) {
                List<JSONObject> paramList = JSON.parseArray(sendParam, JSONObject.class);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fenceConfigService.sendFenceData(paramList, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("下发围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 根据id删除 车辆
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fenceConfigService.unbindFence(id, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("解除围栏绑定异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 批量解除围栏绑定
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fenceConfigService.unbindFenceByBatch(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解除围栏绑定异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 删除关键点
     */
    @RequestMapping(value = "/deleteKeyPoint", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteKeyPoint(String kid) {
        try {
            if (fenceConfigService.deleteKeyPoint(kid)) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除关键点异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 修改围栏绑定
     */
    @RequestMapping(value = "/editById.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(String id, String vehicleId, String name, String type) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            final String plateNumber = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "name");
            FenceConfig config = fenceConfigService.queryFenceConfigById(id);
            mav.addObject("result", config);
            mav.addObject("vehicleName", plateNumber);
            mav.addObject("fenceName", name);
            mav.addObject("fenceType", type);
            return mav;
        } catch (Exception e) {
            log.error("修改围栏绑定异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FenceConfigForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (FenceValidator.isErrorDateParam(form)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "开始时间不能大于或等于结束时间");
                    }
                    // 获取客户端的IP地址
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    boolean flag = fenceConfigService.updateFenceConfig(form, ipAddress);
                    if (flag) {
                        // 清空下发状态
                        parameterDao.updateStatusByParameterName(6, form.getVehicleId(), form.getId(), "1");
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改围栏信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    @RequestMapping(value = "/getStatistical", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getStatistical(Integer webType) {
        Map<String, Object> re = new HashMap<>();
        try {
            re = fenceConfigService.getStatistical(webType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResultBean(re);
    }

    @RequestMapping(value = "/getRunAndStopMonitorNum", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRunAndStopMonitorNum(Boolean isNeedMonitorId, String userName) {
        Map<String, Object> result = new HashMap<>(16);
        try {
            result = fenceConfigService.getRunAndStopMonitorNum(isNeedMonitorId, userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResultBean(result);
    }

    /**
     * 根据传感器类型(仅限温、湿、正反转)查询绑定该类型传感器的监控对象
     * @return todo js调用该接口的方法没有被使用，应该是没用了，若确认没有使用，可以废弃掉
     */
    @RequestMapping(value = "/tempVehicleTree", method = RequestMethod.POST)
    @ResponseBody
    public String getTempSensorVehicleTree(String type, int sensorType) {
        try {
            String result = vehicleService.vehicleTempSensorPermissionTree(type, sensorType, true).toJSONString();
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            log.error("根据传感器类型(温、湿、正反转)获取车辆树异常", e);
            return null;
        }
    }

    /**
     * 后台订阅
     */
    @RequestMapping(value = "/subGroup", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean subBig(String pid, String type, Integer status) {
        Set<String> monitorSet = null;
        try {
            if (Objects.equals(type, "group")) {
                type = "org";
            }
            monitorSet = monitorService.getMonitorByGroupOrOrgDn(pid, type, status);
        } catch (Exception e) {
            log.error("后台订阅异常", e);
        }
        Map<String, Object> result = new HashMap<>();
        int num = CollectionUtils.size(monitorSet);
        result.put("num", num);
        if (num > 400) {
            return new JsonResultBean(result);
        } else {
            result.put("data", monitorSet);
            return new JsonResultBean(result);
        }
    }

    /**
     * 离线查询redis
     */
    @RequestMapping(value = "/getNodesList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getNodesList(String[] nodesList) {
        //获取存在未知信息的监控对象
        List<String> monitorIds = new ArrayList<>(Arrays.asList(nodesList));
        Set<String> filterMonitorIds = monitorService.getOnceOnLineIds(monitorIds);
        //没有位置信息的监控对象
        monitorIds.removeAll(filterMonitorIds);
        return new JsonResultBean(monitorIds);
    }
}
