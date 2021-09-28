package com.zw.platform.service.functionconfig.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cb.platform.domain.query.PointQuery;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.domain.functionconfig.form.FenceConfigForm;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.query.FenceConfigQuery;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.event.ConfigUnbindVehicleEvent;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.CircleDao;
import com.zw.platform.repository.modules.FenceConfigDao;
import com.zw.platform.repository.modules.LineDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.modules.PolygonDao;
import com.zw.platform.repository.modules.RectangleDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.functionconfig.FenceService;
import com.zw.platform.service.functionconfig.ManageFenceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.validator.FenceValidator;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import com.zw.ws.impl.WsElectronicDefenceService;
import joptsimple.internal.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FenceConfigServiceImpl implements FenceConfigService {
    private static Logger log = LogManager.getLogger(FenceConfigServiceImpl.class);

    @Autowired
    private FenceConfigDao fenceConfigDao;

    @Autowired
    private LineDao lineDao;

    @Autowired
    private RectangleDao rectangleDao;

    @Autowired
    private CircleDao circleDao;

    @Autowired
    private PolygonDao polygonDao;

    @Autowired
    private WsElectronicDefenceService wsElectronicDefenceService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    FenceService fenceService;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private MonitorService monitorService;

    private final ManageFenceService manageFenceService;

    private final LogSearchService logSearchService;

    @Value("${realTimeMonitoring.treeCountFlag}")
    private boolean treeCountFlag;

    @Autowired
    public FenceConfigServiceImpl(ManageFenceService manageFenceService, LogSearchService logSearchService) {
        this.manageFenceService = manageFenceService;
        this.logSearchService = logSearchService;
    }

    /**
     * 查询围栏绑定（分页）
     */
    @MethodLog(name = "分页查询围栏绑定", description = "分页查询围栏绑定")
    @Override
    public Page<Map<String, Object>> findFenceConfigByPage(FenceConfigQuery query) {
        Page<Map<String, Object>> list = PageHelperUtil.doSelect(query, () -> fenceConfigDao.findFenceConfig(query));
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> monitorIds =
            list.getResult().stream().map(o -> String.valueOf(o.get("vehicle_id"))).collect(Collectors.toList());
        //获取监控对象的协议类型
        Map<String, String> monitorDeviceTypeMap = MonitorUtils.getKeyValueMap(monitorIds, "id", "deviceType");
        for (Map<String, Object> map : list) {
            String monitorId = (String) map.get("vehicle_id");
            if (monitorId != null && map.get("id") != null) {
                // 获取终端协议类型
                if (monitorDeviceTypeMap.containsKey(monitorId)) {
                    map.put("deviceType", monitorDeviceTypeMap.get(monitorId));
                }

                // 下发状态 type=1:围栏
                List<Directive> paramList =
                    parameterDao.findParameterByType((String) map.get("vehicle_id"), (String) map.get("id"), "1");
                Directive param = null;
                if (paramList != null && !paramList.isEmpty()) {
                    param = paramList.get(0);
                }
                if (param != null) {
                    map.put("paramId", param.getId());
                    map.put("dirStatus", param.getStatus());
                }
            }
        }
        return list;
    }

    /**
     * 新增
     */
    @Override
    @MethodLog(name = "新增", description = "新增")
    public boolean addFenceConfig(FenceConfigForm fenceConfigForm) {
        fenceConfigForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        // 创建者
        fenceConfigForm.setCreateDataTime(new Date()); // 创建时间
        return fenceConfigDao.addFenceConfig(fenceConfigForm);
    }

    /**
     * 批量新增
     */
    @MethodLog(name = "批量新增", description = "批量新增")
    @Override
    public boolean addFenceConfigByBatch(List<FenceConfigForm> list) {
        for (FenceConfigForm form : list) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // 创建者
            form.setCreateDataTime(new Date()); // 创建时间
        }
        return fenceConfigDao.addFenceConfigByBatch(list);
    }

    @Override
    public Page<FenceConfig> findOrbitList(FenceConfigQuery query, String simpleQueryParam) {
        return PageHelperUtil.doSelect(query, () -> fenceConfigDao.findOrbitList(query, simpleQueryParam));
    }

    @Override
    public FenceConfig getFenceConfigById(String id) {
        return fenceConfigDao.getFenceConfigById(id);
    }

    @Override
    public void editFenceConfig(LineForm form) {
        String shapeId = form.getShapeId();
        // 给form里面保存线路主表时用到的字段赋值
        form.setName(Converter.toBlank(form.getLineName()));
        form.setDescription(Converter.toBlank(form.getLineDescription()));
        form.setType(Converter.toBlank(form.getLineType()));
        form.setWidth(Converter.toInteger(form.getLineWidth()));
        List<LineContent> lineContentList = lineDao.findLineContentById(Converter.toBlank(shapeId));
        if (CollectionUtils.isNotEmpty(lineContentList)) {
            for (LineContent lineContent : lineContentList) {
                form.setPointSeq(Converter.toBlank(lineContent.getSortOrder()));
                form.setLongitude(Converter.toBlank(lineContent.getLongitude()));
                form.setLatitude(Converter.toBlank(lineContent.getLatitude()));
                lineDao.addLineContent(form);
            }
        }
        // 保存线路主表信息
        lineDao.add(form);
        // 保存围栏表
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(form.getId());
        fenceForm.setType("zw_m_line");
        lineDao.fenceInfo(fenceForm);
    }

    /**
     * 解除围栏绑定
     */
    @Override
    public JsonResultBean unbindFence(String id, String ipAddress) throws Exception {
        FenceConfigForm form = new FenceConfigForm();
        form.setId(id);
        form.setFlag(0);
        // 下发解绑
        List<String> ids = new ArrayList<>();
        ids.add(id);
        // 围栏绑定表信息
        List<Map<String, Object>> configs = findFenceConfigByIds(ids);
        StringBuilder vehicleIds = new StringBuilder();
        if (CollectionUtils.isEmpty(configs)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        Map<String, Object> configInfo = configs.get(0);
        if (configInfo != null) {
            if (configInfo.containsKey("bindId") && configInfo.containsKey("vehicleId") && configInfo
                .containsKey("fenceId") && configInfo.containsKey("fence_id") && configInfo
                .containsKey("alarmSource")) {
                int alarmSource = (int) configs.get(0).get("alarmSource");
                if (alarmSource == 0) {
                    sendUnbindFence(configs);
                }
                String vehicleId = configs.get(0).get("vehicleId").toString();
                // 组装车辆id,用于通知软围栏数据变更
                vehicleIds.append(",").append(vehicleId);
                String fenceId = configs.get(0).get("fence_id").toString();
                // 根据车辆id和围栏id查询围栏的详细信息
                Map<String, Object> fenceConfig = findByVIdAndFId(vehicleId, fenceId);
                if (fenceConfig != null) {
                    String name = fenceConfig.get("name").toString();
                    String brand = fenceConfig.get("brand").toString();
                    String type = fenceConfig.get("type").toString();
                    String typeName = fenceTypeName(type);
                    String msg = "监控对象 : " + brand + " 解除和 " + typeName + " (" + name + ") 的绑定关系";
                    // 数据库删除绑定
                    boolean flag = fenceConfigDao.unbindFence(id);
                    if (flag) {
                        String[] vehicle = logSearchService.findCarMsg(vehicleId);
                        logSearchService.addLog(ipAddress, msg, "3", "", vehicle[0], vehicle[1]);
                        updateData(type, vehicleIds.toString());
                        // 维护下发到设备的围栏信息信息缓存
                        if (alarmSource == 0 && fenceConfig.get("send_down_id") != null) {
                            RedisKey redisKey =
                                HistoryRedisKeyEnum.FENCE_SEND.of(vehicleId, fenceConfig.get("send_down_id"));
                            if (RedisHelper.isContainsKey(redisKey)) {
                                RedisHelper.delete(redisKey);
                                ZMQFencePub.pubChangeFence("13");
                            }
                        }
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "绑定数据有误,解绑失败,请联系管理员");
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 批量解除绑定
     */
    @Override
    public JsonResultBean unbindFenceByBatch(List<String> ids, String ipAddress) throws Exception {
        // 下发解绑
        List<Map<String, Object>> configs = findFenceConfigByIds(ids);
        if (CollectionUtils.isEmpty(configs)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        StringBuilder message = new StringBuilder();
        for (int index = 0; index < configs.size(); index++) {
            Map<String, Object> configInfo = configs.get(index);
            if (configInfo != null && configInfo.containsKey("bindId") && configInfo.containsKey("vehicleId")
                && configInfo.containsKey("fenceId") && configInfo.containsKey("fence_id") && configInfo
                .containsKey("alarmSource")) {
                if (index == 0) {
                    String alarmSource = configInfo.get("alarmSource").toString();
                    if ("0".equals(alarmSource)) {
                        sendUnbindFence(configs);
                    }
                }
                String vehicleId = configInfo.get("vehicleId").toString();
                String fenceId = configInfo.get("fence_id").toString();
                // 根据车辆id和围栏id查询围栏的详细信息
                Map<String, Object> fenceConfig = findByVIdAndFId(vehicleId, fenceId);
                if (fenceConfig != null) {
                    String name = fenceConfig.get("name").toString();
                    String brand = fenceConfig.get("brand").toString();
                    String type = fenceConfig.get("type").toString();
                    String typeName = fenceTypeName(type);
                    message.append("监控对象 : ").append(brand).append("解除和").append(typeName).append(" (").append(name)
                        .append(" ) 的绑定关系").append("<br/>");
                }
            }
        }
        boolean flag = fenceConfigDao.unbindFenceByBatch(ids);
        if (flag) {
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量解除电子围栏绑定");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public void sendFenceByType(List<Map<String, Object>> listMap) {
        if (CollectionUtils.isEmpty(listMap)) {
            return;
        }
        List<DirectiveForm> directiveList = new ArrayList<>();
        for (Map<String, Object> map : listMap) {
            FenceInfo bindInfo = fenceConfigDao.findFenceConfigById((String) map.get("bindId"));
            // 绑定实体
            FenceConfig fenceConfig = fenceConfigDao.queryFenceConfigById((String) map.get("bindId"));
            if (bindInfo != null) {

                String vehicleId = (String) map.get("vehicleId");
                String sendType = (String) map.get("sendType");

                // 绑定表id
                String bindId = (String) map.get("bindId");
                List<String> bindIds = new ArrayList<>();
                bindIds.add(bindId);
                // 参数下发ids
                String paramId = (String) map.get("paramId");
                List<String> paramIds = new ArrayList<>();
                if (paramId != null && !"".equals(paramId)) {
                    paramIds.add(paramId);
                }
                //围栏下发到设备的hashCode值为空重新生成并赋值
                ifSendDownIdIsNullSetVal(bindInfo.getShape(), fenceConfig);
                //检查下发id
                checkSendDownId(bindInfo, fenceConfig);

                BindDTO monitor = MonitorUtils.getBindDTO(vehicleId);
                if (monitor == null) {
                    continue;
                }

                Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, monitor.getDeviceNumber());
                // 设备已经注册
                if (msgSN != null) {
                    // 围栏ids
                    String fenceId = bindInfo.getShape();
                    List<String> fenceIds = new ArrayList<>();
                    fenceIds.add(fenceId);
                    String type = bindInfo.getType();
                    VehicleInfo vehicle = getVehicleInfo(monitor);
                    // 已下发
                    int status = 4;
                    if ("zw_m_line".equals(type)) {
                        //下发线路
                        sendLine(fenceConfig, sendType, fenceIds, vehicle, msgSN);

                    } else if ("zw_m_rectangle".equals(type)) {
                        //下发矩形
                        sendRectangle(fenceConfig, sendType, fenceIds, vehicle, msgSN);

                    } else if ("zw_m_circle".equals(type)) {
                        //下发圆形
                        sendCircle(fenceConfig, sendType, fenceIds, vehicle, msgSN);

                    } else if ("zw_m_polygon".equals(type)) {
                        //下发多边形
                        sendPolygon(fenceConfig, sendType, fenceIds, vehicle, msgSN);

                    }
                    if (CollectionUtils.isNotEmpty(paramIds)) {
                        // 重新下发 ，修改流水号： 1 : 下发未回应
                        parameterDao.updateMsgSNById(paramIds, msgSN, status, 1);
                    } else {
                        // 新增下发参数数据
                        List<DirectiveForm> tempList =
                            generateDirective(bindIds, vehicleId, status, sendType, msgSN, directiveList, 1);
                        if (CollectionUtils.isNotEmpty(tempList)) {
                            // 批量新增
                            parameterDao.addDirectiveByBatch(tempList);
                        }
                        paramId = tempList.get(0).getId();
                    }
                    //异步检查和更新下发的状态
                    asyncCheckAndUpdateDownStatus(vehicleId, paramId, msgSN);
                    //下发后需要更新软围栏,调用通用应答模块的逻辑,推送用户订阅的电子围栏websocket接口
                    paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSN, vehicle.getSimcardNumber(),
                        SendTarget.getInstance(SendModule.ELECTRONIC_FENCE));
                } else {
                    dealDeviceUnRegister(vehicleId, sendType, bindIds, paramIds);

                }
            }
        }
    }

    private void asyncCheckAndUpdateDownStatus(String vehicleId, String paramId, Integer msgSN) {
        // 下发参数
        SendParam sendParam = new SendParam();
        // 流水号
        sendParam.setMsgSNACK(msgSN);
        sendParam.setVehicleId(vehicleId);
        sendParam.setParamId(paramId);
        f3SendStatusProcessService.updateSendParam(sendParam, 1);
    }

    private void checkSendDownId(FenceInfo bindInfo, FenceConfig fenceConfig) {
        // 根据围栏绑定表id和车辆id查询围栏信息
        Map<String, Object> fenceInfo =
            fenceConfigDao.findFenceInfoByVehicle(fenceConfig.getVehicleId(), fenceConfig.getId(), bindInfo.getShape());
        if (fenceInfo != null) {
            Integer sendDownId = fenceConfig.getSendDownId();
            if (sendDownId != null && sendDownId != 0) {
                ifKeyNotExistSetValAndSendMsgToFence(fenceConfig, fenceInfo, sendDownId);
            }
        }
    }

    private void dealDeviceUnRegister(String vehicleId, String sendType, List<String> bindIds, List<String> paramIds) {
        Integer msgSN;// 设备为注册
        int status = 5;
        msgSN = 0;
        if (CollectionUtils.isNotEmpty(paramIds)) {
            // 重新下发 ，修改流水号 1 : 下发未回应
            parameterDao.updateMsgSNById(paramIds, msgSN, status, 1);
        } else {
            // 新增下发参数数据
            List<DirectiveForm> tempList =
                generateDirective(bindIds, vehicleId, status, sendType, msgSN, new ArrayList<>(), 1);
            if (CollectionUtils.isNotEmpty(tempList)) {
                // 批量新增
                parameterDao.addDirectiveByBatch(tempList);
            }
        }
    }

    public static VehicleInfo getVehicleInfo(BindDTO monitor) {
        VehicleInfo vehicle = new VehicleInfo();
        vehicle.setId(monitor.getId());
        vehicle.setDeviceId(monitor.getDeviceId());
        vehicle.setSimcardNumber(monitor.getSimCardNumber());
        vehicle.setDeviceNumber(monitor.getDeviceNumber());
        //设置车辆设备的终端协议类型（方便后面按照协议下发参数组装）
        vehicle.setDeviceType(monitor.getDeviceType());
        //下发线路需要车牌号和车牌颜色
        vehicle.setBrand(monitor.getName());
        vehicle.setPlateColor(monitor.getPlateColor());
        vehicle.setGroupId(monitor.getOrgId());
        return vehicle;
    }

    private <T> List<Map<String, Object>> getFenceConfigMapList(List<T> fences, FenceConfig fenceConfig,
        String deviceType) {
        List<Map<String, Object>> fenceConfigMapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fences)) {
            Map<String, Object> fenceMap = new HashMap<>();
            fenceMap.put("fence", fences.get(0));
            fenceMap.put("config", fenceConfig);
            fenceMap.put("isProtocol2019", DeviceInfo.isProtocol2019(deviceType));
            fenceConfigMapList.add(fenceMap);
        }
        return fenceConfigMapList;
    }

    /**
     * 根据下发类型判断是否绑定操作
     * @param sendType
     * @return
     */
    private boolean isBind(String sendType) {
        return "1".equals(sendType);
    }

    /**
     * 根据下发类型判断师傅是解绑操作
     * @param sendType
     * @return
     */
    private boolean isUnbind(String sendType) {
        return "2".equals(sendType);
    }

    public List<Integer> getFenceSendDownIds(List<?> fences, Integer sendDownId) {
        List<Integer> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fences)) {
            ids.add(sendDownId);
        }
        return ids;
    }

    private void sendPolygon(FenceConfig fenceConfig, String sendType, List<String> fenceIds, VehicleInfo vehicle,
        Integer msgSN) {
        // 查询多边形的详情
        List<Polygon> fenceList = polygonDao.findPolygonByIds(fenceIds);
        // 下发消息
        if (isBind(sendType)) {
            List<Map<String, Object>> fenceConfigMapList =
                getFenceConfigMapList(fenceList, fenceConfig, vehicle.getDeviceType());
            wsElectronicDefenceService.sendPolygonInfoToDevice(fenceConfigMapList, vehicle, msgSN);
        } else if (isUnbind(sendType)) {
            List<Integer> sendDownIds = getFenceSendDownIds(fenceList, fenceConfig.getSendDownId());
            // 下发消息
            wsElectronicDefenceService
                .deleteDefenseInfo(ConstantUtil.T808_DELETE_POLYGON_AREA, vehicle, sendDownIds, msgSN);
        }
    }

    private void sendCircle(FenceConfig fenceConfig, String sendType, List<String> fenceIds, VehicleInfo vehicle,
        Integer msgSN) {
        // 查询圆形的详情
        List<Circle> fenceList = circleDao.getCircleByIds(fenceIds);
        // 下发消息
        if (isBind(sendType)) {
            List<Map<String, Object>> fenceConfigMapList =
                getFenceConfigMapList(fenceList, fenceConfig, vehicle.getDeviceType());
            wsElectronicDefenceService
                .sendCircleAreaInfoToDevice(fenceConfig.getSendFenceType(), fenceConfigMapList, vehicle, msgSN);
        } else if (isUnbind(sendType)) {
            List<Integer> sendDownIds = getFenceSendDownIds(fenceList, fenceConfig.getSendDownId());
            // 下发消息
            wsElectronicDefenceService
                .deleteDefenseInfo(ConstantUtil.T808_DELETE_ROUND_AREA, vehicle, sendDownIds, msgSN);
        }
    }

    private void sendRectangle(FenceConfig fenceConfig, String sendType, List<String> fenceIds, VehicleInfo vehicle,
        Integer msgSN) {
        // 查询矩形的详情
        List<Rectangle> fenceList = rectangleDao.getRectangleByIds(fenceIds);
        // 下发消息
        if (isBind(sendType)) {
            List<Map<String, Object>> fenceConfigMapList =
                getFenceConfigMapList(fenceList, fenceConfig, vehicle.getDeviceType());
            wsElectronicDefenceService
                .sendRectangleInfoToDevice(fenceConfig.getSendFenceType(), fenceConfigMapList, vehicle, msgSN);
        } else if (isUnbind(sendType)) {
            List<Integer> sendDownIds = getFenceSendDownIds(fenceList, fenceConfig.getSendDownId());
            // 下发消息
            wsElectronicDefenceService
                .deleteDefenseInfo(ConstantUtil.T808_DELETE_RECTANGLE_AREA, vehicle, sendDownIds, msgSN);
        }
    }

    private void sendLine(FenceConfig fenceConfig, String sendType, List<String> fenceIds, VehicleInfo vehicle,
        Integer msgSN) {
        // 查询线的详情
        List<Line> fenceList = lineDao.findLineByIds(fenceIds);
        // 下发消息
        if (isBind(sendType)) {
            List<Map<String, Object>> fenceConfigMapList =
                getFenceConfigMapList(fenceList, fenceConfig, vehicle.getDeviceType());
            wsElectronicDefenceService.sendSingleLineInfoToDevice(fenceConfigMapList, vehicle, msgSN);
        } else if (isUnbind(sendType)) {
            List<Integer> sendDownIds = getFenceSendDownIds(fenceList, fenceConfig.getSendDownId());
            wsElectronicDefenceService.deleteDefenseInfo(ConstantUtil.T808_DELETE_LINE, vehicle, sendDownIds, msgSN);
        }
    }

    private void ifKeyNotExistSetValAndSendMsgToFence(FenceConfig fenceConfig, Map<String, Object> fenceInfo,
        Integer sendDownId) {
        // 把hashcode值和车id作为键,存入缓存
        RedisKey redisKey = HistoryRedisKeyEnum.FENCE_SEND.of(fenceConfig.getVehicleId(), sendDownId);
        if (!RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.setString(redisKey, JSON.toJSONString(fenceInfo));
            ZMQFencePub.pubChangeFence("13");
        }
    }

    private void ifSendDownIdIsNullSetVal(String shape, FenceConfig fenceConfig) {
        if (fenceConfig.getSendDownId() == null || fenceConfig.getSendDownId() == 0) {
            int configIdHashCode = CommonUtil.abs(shape.replaceAll("-", "").hashCode());
            boolean result = fenceConfigDao.addHashCodeByConfigId(fenceConfig.getId(), configIdHashCode);
            if (result) {
                fenceConfig.setSendDownId(configIdHashCode);
            }
        }
    }

    /**
     * 生成参数下发数据
     */
    private List<DirectiveForm> generateDirective(List<String> ids, String vehicleId, int status, String directiveName,
        int msgSN, List<DirectiveForm> directiveList, int replyCode) {
        List<DirectiveForm> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            for (String id : ids) {
                DirectiveForm form = new DirectiveForm();
                form.setDirectiveName(directiveName);
                form.setDownTime(new Date());
                form.setMonitorObjectId(vehicleId);
                form.setParameterName(id);
                form.setParameterType("1");
                form.setStatus(status);
                form.setSwiftNumber(msgSN);
                form.setReplyCode(replyCode);
                list.add(form);
                directiveList.add(form);
            }
        }
        return list;
    }

    @Override
    public void sendUnbindFence(List<Map<String, Object>> configs) {
        if (CollectionUtils.isNotEmpty(configs)) {
            for (Map<String, Object> config : configs) {
                config.put("sendType", "2");
            }
            // 重组map,同一个车绑定相同类型的围栏，合并一起
            sendFenceByType(configs);
        }
    }

    @Override
    public List<Map<String, Object>> findFenceConfigByIds(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            return fenceConfigDao.findFenceConfigByIds(ids);
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> findByVIdAndFId(String vehicleId, String fenceId) {
        if (vehicleId != null && !"".equals(vehicleId) && fenceId != null && !"".equals(fenceId)) {
            return fenceConfigDao.findByVIdAndFId(vehicleId, fenceId);
        }
        return null;
    }

    @Override
    public boolean unbindFenceByVid(String vid, Integer type) {
        if (!"".equals(Converter.toBlank(vid))) {
            if (ConfigUnbindVehicleEvent.TYPE_SINGLE == type) {
                return fenceConfigDao.unbindFenceByVid(vid);
            } else {
                List<String> monitorIds = Arrays.asList(vid.split(","));
                return fenceConfigDao.unbindBatchFenceByVid(monitorIds);
            }
        }
        return false;
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        fenceConfigDao.unbindBatchFenceByVid(monitorIds);
    }

    @Override
    public boolean unbindFenceByConfigId(String configId) {
        if (!"".equals(Converter.toBlank(configId))) {
            return fenceConfigDao.unbindFenceByConfigId(configId);
        }
        return false;
    }

    @Override
    public FenceConfig queryFenceConfigById(String id) {
        if (StringUtils.isNotBlank(id)) {
            return fenceConfigDao.queryFenceConfigById(id);
        }
        return null;
    }

    @Override
    public boolean deleteKeyPoint(String id) {
        return fenceConfigDao.deleteKeyPoint(id);
    }

    /**
     * 修改围栏绑定
     * @param form
     * @return
     */
    @Override
    public boolean updateFenceConfig(FenceConfigForm form, String ipAddress) throws Exception {
        String vehicleId = form.getVehicleId();
        String fenceConfigId = form.getFenceId();
        Map<String, Object> fenceInfo = fenceConfigDao.findByVIdAndFId(vehicleId, fenceConfigId);
        if (fenceInfo != null) {
            boolean flag = fenceConfigDao.updateFenceConfig(form);
            if (flag) {
                String name = fenceInfo.get("name").toString();
                String brand = fenceInfo.get("brand").toString();
                String type = fenceInfo.get("type").toString();
                String typeName = fenceTypeName(type);
                String message = "监控对象 : " + brand + "修改" + typeName + " (" + name + ") 的绑定信息";
                updateData(type, "," + vehicleId);
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message, "3", "", vehicle[0], vehicle[1]);
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getRunAndStopMonitorNum(Boolean isNeedMonitorId, String userName) {
        Map<String, Object> result = new HashMap<>(16);
        JSONArray onlineArray = new JSONArray();
        JSONArray onlineParkArray = new JSONArray();
        JSONArray runArr = new JSONArray();
        Set<String> userAssignMonitorIds = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isNotEmpty(userAssignMonitorIds)) {
            Map<String, ClientVehicleInfo> vehicleStatusMap = monitorService.getMonitorStatus(userAssignMonitorIds);
            for (Map.Entry<String, ClientVehicleInfo> entry : vehicleStatusMap.entrySet()) {
                ClientVehicleInfo clientVehicleInfo = entry.getValue();

                Integer vehicleStatus = clientVehicleInfo.getVehicleStatus();
                String monitorId = clientVehicleInfo.getVehicleId();
                double speed = Double.parseDouble(clientVehicleInfo.getSpeed());
                onlineArray.add(monitorId);
                if (isPark(vehicleStatus, speed)) {
                    onlineParkArray.add(monitorId);
                } else if (isRun(vehicleStatus, speed)) {
                    runArr.add(monitorId);
                }
            }
        }
        result.put("onlineNum", onlineArray.size());
        result.put("onlineParkNum", onlineParkArray.size());
        result.put("runArrNum", runArr.size());
        result.put("allV", userAssignMonitorIds.size());
        if (isNeedMonitorId) {
            result.put("stopVidArray", onlineParkArray);
            result.put("runVidArray", runArr);
            result.put("vehicleIdArray", userAssignMonitorIds);
        }

        if (treeCountFlag) {
            //添加返回实时监控树的显示设置
            String treeShowSetting = RedisHelper
                .getString(HistoryRedisKeyEnum.USER_REALTIME_MONITORTREE_SET.of(SystemHelper.getCurrentUsername()));
            result.put("configTreeCountFlag", 1);
            if (treeShowSetting != null) {
                JSONObject re = JSONObject.parseObject(treeShowSetting, JSONObject.class);
                result.put("aliasesFlag", re.get("aliasesFlag") == null ? 0 : re.get("aliasesFlag"));
                result.put("showTreeCountFlag", re.get("showTreeCountFlag") == null ? 0 : re.get("showTreeCountFlag"));
            } else {
                result.put("aliasesFlag", userAssignMonitorIds.size() > 2000 ? 0 : 1);
                result.put("showTreeCountFlag", userAssignMonitorIds.size() > 2000 ? 0 : 1);
            }
        } else {
            result.put("aliasesFlag", 0);
            result.put("showTreeCountFlag", 0);
            result.put("configTreeCountFlag", 0);
        }
        return result;
    }

    @Override
    public String getPointFenceIdByGroupIds(PointQuery pointQuery) {
        final String fenceName = pointQuery.getSimpleQueryParam();
        final List<String> orgIds = Arrays.asList(pointQuery.getOrganizationIds().split(","));
        Set<String> fenceIds;
        if (StrUtil.isBlank(fenceName)) {
            // 未进行模糊匹配，查询所选企业下所有围栏ID
            fenceIds = fenceConfigDao.getPointFenceIdByGroupIds(orgIds, null);
        } else {
            // 进行模糊匹配，查询所选企业下满足模糊搜索的围栏ID
            fenceIds = fenceConfigDao.getPointFenceIdByGroupIds(orgIds, fenceName);
        }
        return Strings.join(fenceIds, ",");
    }

    @Override
    public String getPointFenceIdByMonitorIds(PointQuery pointQuery) {
        final String fenceName = pointQuery.getSimpleQueryParam();
        final List<String> monitorIds = Arrays.asList(pointQuery.getMonitorIds().split(","));
        Set<String> fenceIds;
        if (StrUtil.isBlank(fenceName)) {
            fenceIds = fenceConfigDao.getPointFenceIdByMonitorIds(monitorIds, null);
        } else {
            fenceIds = fenceConfigDao.getPointFenceIdByMonitorIds(monitorIds, fenceName);
        }
        return Strings.join(fenceIds, ",");
    }

    private boolean isRun(Integer vehicleStatus, double speed) {
        return vehicleStatus == 10 || vehicleStatus == 9 || (speed >= 1 && (vehicleStatus == 5 || vehicleStatus == 2
            || vehicleStatus == 11));
    }

    private boolean isPark(Integer vehicleStatus, double speed) {
        return vehicleStatus == 4 || (speed < 1 && (vehicleStatus == 5 || vehicleStatus == 2 || vehicleStatus == 11));
    }

    @Override
    public Map<String, Object> getStatistical(Integer webType) {
        Set<String> monitorIds = userService.getCurrentUserMonitorIds();
        if (webType != null && webType == 2) {
            monitorService.filterByDeviceType(Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE), monitorIds);
        }
        //获取监控对象的在离线状态
        Map<String, ClientVehicleInfo> monitorStatusMap = monitorService.getMonitorStatus(monitorIds);
        List<String> onlineIds = new ArrayList<>();
        List<String> onlineParkIds = new ArrayList<>();
        List<String> runIds = new ArrayList<>();
        for (Map.Entry<String, ClientVehicleInfo> entry : monitorStatusMap.entrySet()) {
            ClientVehicleInfo clientVehicle = entry.getValue();
            String protocolType = clientVehicle.getDeviceType();
            String monitorId = entry.getKey();
            if (webType == null || (webType == 2 && "1".equals(protocolType))) {
                onlineIds.add(monitorId);
                Integer status = clientVehicle.getVehicleStatus();
                Double speed = Double.parseDouble(clientVehicle.getSpeed());
                if (isPark(status, speed)) {
                    onlineParkIds.add(monitorId);
                } else if (isRun(status, speed)) {
                    runIds.add(monitorId);
                }
            }
        }

        Map<String, Object> result = new HashMap<>(16);
        result.put("onlineNum", onlineIds.size());
        result.put("onlineParkNum", onlineParkIds.size());
        result.put("runArrNum", runIds.size());
        result.put("allV", monitorIds.size());
        result.put("stopVidArray", onlineParkIds);
        result.put("runVidArray", runIds);
        result.put("vehicleIdArray", monitorIds);
        return result;
    }

    @Override
    public Map<String, Object> findFenceInfo(String vehicleId, int sendDownId) {
        if (vehicleId != null && !"".equals(vehicleId) && sendDownId != 0) {
            return fenceConfigDao.findFenceInfoByVehicleIdAndHashCode(sendDownId, vehicleId);
        }
        return null;
    }

    @Override
    public String findFenceConfigByVid(String vid) {
        JSONArray result = new JSONArray();
        // 生成围栏表头
        generateFenceTitle(result, "");
        if (StringUtils.isNotBlank(vid)) {
            List<String> vehicleIds = new ArrayList<>();
            FenceConfigQuery query = new FenceConfigQuery();
            List<OrganizationLdap> orgList =
                organizationService.getOrgParentAndChild(userService.getCurrentUserOrgDn());
            List<String> groupIds = orgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
            vehicleIds.add(vid);
            query.setVehicleIds(vehicleIds);
            query.setGroupIds(groupIds);
            List<Map<String, Object>> list = fenceConfigDao.findFenceConfigInfo(vid, groupIds);
            if (CollectionUtils.isNotEmpty(list)) {
                for (Map<String, Object> fence : list) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", fence.get("fenceId"));
                    obj.put("name", fence.get("name"));
                    obj.put("pId", fence.get("type"));
                    obj.put("type", "fence");
                    obj.put("iconSkin", fence.get("type") + "_skin");
                    obj.put("open", true);
                    obj.put("markIcon", fence.get("markIcon"));
                    result.add(obj);
                }
            }
        }
        return result.toJSONString();
    }

    @Override
    public String getFenceTree(String type) {
        JSONArray result = new JSONArray();
        // 生成围栏表头
        generateFenceTitle(result, type);
        // 组装标记
        generateFenceTree(result, "zw_m_marker");
        // 组装线
        generateFenceTree(result, "zw_m_line");
        // 组装矩形
        generateFenceTree(result, "zw_m_rectangle");
        // 组装圆形
        generateFenceTree(result, "zw_m_circle");
        // 组装多边形
        generateFenceTree(result, "zw_m_polygon");
        // 组装行政区划
        generateFenceTree(result, "zw_m_administration");
        // 组装行政区划
        generateFenceTree(result, "zw_m_travel_line");

        return result.toJSONString();
    }

    private void generateFenceTitle(JSONArray result, String type) {
        // 标注，线，矩形，圆形，多边形，行政区域
        if ("single".equals(type)) {
            result.add(JSON.parseObject("{\"id\":\"zw_m_marker\",\"pId\":\"\",\"name\":\"标注\""
                + ",\"nocheck\":true,\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_line\",\"pId\":\"\",\"name\":\"路线\","
                + "\"nocheck\":true,\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_rectangle\",\"pId\":\"\",\"name\":\"矩形\","
                + "\"nocheck\":true,\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_circle\",\"pId\":\"\",\"name\":\"圆形\","
                + "\"nocheck\":true,\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_polygon\",\"pId\":\"0\",\"name\":\"多边形\","
                + "\"nocheck\":true,\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_administration\",\"pId\":\"0\",\"name\":\"行政区划\","
                + "\"nocheck\":true,\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_travel_line\",\"pId\":\"0\",\"name\":\"导航线路\","
                + "\"nocheck\":true,\"open\":true,\"type\":\"fenceParent\"}"));
        } else {
            result.add(JSON.parseObject(
                "{\"id\":\"zw_m_marker\",\"pId\":\"\",\"name\":\"标注\"," + "\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject(
                "{\"id\":\"zw_m_line\",\"pId\":\"\",\"name\":\"路线\"," + "\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_rectangle\",\"pId\":\"\",\"name\":\"矩形\","
                + "\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject(
                "{\"id\":\"zw_m_circle\",\"pId\":\"\",\"name\":\"圆形\"," + "\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_polygon\",\"pId\":\"0\",\"name\":\"多边形\","
                + "\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_administration\",\"pId\":\"0\",\"name\":\"行政区划\","
                + "\"open\":true,\"type\":\"fenceParent\"}"));
            result.add(JSON.parseObject("{\"id\":\"zw_m_travel_line\",\"pId\":\"0\",\"name\":\"导航路线\","
                + "\"open\":true,\"type\":\"fenceParent\"}"));
        }
    }

    /**
     * 组装围栏树结构
     * @param result    树结构
     * @param fenceType 围栏形状表名
     * @return JSONArray
     * @author wangying
     */
    private JSONArray generateFenceTree(JSONArray result, String fenceType) {
        try {
            List<String> orgIds = userService.getCurrentUserOrgIds();
            List<Map<String, Object>> list = fenceService.findFenceByType(fenceType, orgIds);
            for (Map<String, Object> fence : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", fence.get("shape"));
                obj.put("name", fence.get("name"));
                obj.put("pId", fenceType);
                obj.put("type", "fence");
                obj.put("iconSkin", fenceType + "_skin");
                obj.put("fenceInfoId", fence.get("id"));
                obj.put("open", true);
                obj.put("markIcon", fence.get("mark_icon"));
                result.add(obj);
            }
            return result;
        } catch (Exception e) {
            log.error("组装围栏树结构异常", e);
            return result;
        }
    }

    /**
     * 围栏绑定
     * @param data 绑定详情的信息
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addBindFence(String data, String ipAddress) throws Exception {
        JSONObject msg = new JSONObject();

        List<FenceConfigForm> list = JSON.parseObject(data, new TypeReference<ArrayList<FenceConfigForm>>() {
        });
        // 获取围栏id
        String fenceId = list.get(0).getFenceId();
        // 获取围栏对应类型id
        String shape = manageFenceService.findFenceTypeId(fenceId);
        // 围栏类型及围栏名称
        String[] fenceTypes = manageFenceService.findType(shape);
        StringBuilder errMessage = new StringBuilder();
        // 说明有围栏的类型信息
        if (fenceTypes.length > 1) {
            boolean checkFlag = true;
            StringBuilder vehicleIds = new StringBuilder();
            String vehicleId = "";
            StringBuilder message = new StringBuilder();
            // 获取围栏类型
            String fenceType = fenceTypes[0];
            List<String> monitorIds = list.stream().map(FenceConfigForm::getVehicleId).collect(Collectors.toList());
            Map<String, BindDTO> monitorMap = MonitorUtils.getBindDTOMap(monitorIds);
            // 校验同一个围栏和同一个车辆是否已经绑定过
            for (FenceConfigForm config : list) {
                // 获取车辆id
                vehicleId = config.getVehicleId();
                //组装车辆id，用于通知软围栏数据变更
                vehicleIds.append(",").append(vehicleId);
                // 绑定围栏的监控对象
                BindDTO monitor = monitorMap.get(vehicleId);
                if (Objects.isNull(monitor)) {
                    continue;
                }
                //校验数据并进行相应错误日志的添加
                if (isCheckFailAndAppendErrorMsg(config, fenceType, errMessage, monitor, fenceId)) {
                    checkFlag = false;
                    continue;
                }
                // 存储围栏下发到设备的围栏id(圆形、矩形、多边形)  HashCode值
                config.setSendDownId(CommonUtil.abs(shape.replaceAll("-", "").hashCode()));
                //添加校验成功的相关绑定日志信息
                message.append(getFenceBindLog(fenceTypes, monitor.getName()));
            }

            if (checkFlag && addFenceConfigByBatch(list)) {
                updateData(fenceTypes[2], vehicleIds.toString());
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                if (vehicle != null) {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
                    msg.put("flag", 1);
                    msg.put("errMsg", "保存成功！");
                }
                return new JsonResultBean(msg);
            }
        } // 没有围栏的信息,说明数据库没有这个围栏,就无法绑定
        msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
        msg.put("errMsg", errMessage);
        return new JsonResultBean(msg);
    }

    private String getFenceBindLog(String[] fenceTypes, String monitorName) {
        StringBuffer sb = new StringBuffer();
        sb.append("监控对象 : ").append(monitorName).append(" 绑定 ").append(fenceTypeName(fenceTypes[2])).append(" (")
            .append(fenceTypes[1]).append(")").append("<br/>");
        return sb.toString();
    }

    private boolean isCheckFailAndAppendErrorMsg(FenceConfigForm config, String fenceType, StringBuilder errMessage,
        BindDTO bindDTO, String fenceId) {
        boolean isCheckFail = false;
        String vehicleId = config.getVehicleId();
        String deviceType = bindDTO.getDeviceType();
        Set<String> notSupportDeviceType = new HashSet<>(Arrays.asList("8", "9", "10"));
        boolean isErrorDeviceTypeWhenRiskPointer =
            "风控点".equals(fenceType) && !notSupportDeviceType.contains(deviceType);
        // 风控点 : 圆形电子围栏的一个区域类型
        String checkMonitor = bindDTO.getName();
        if (isErrorDeviceTypeWhenRiskPointer) {
            errMessage.append("监控对象\"").append(checkMonitor).append("\"绑定终端通讯类型不为超长待机设备，不能绑定该围栏<br/>");
            isCheckFail = true;
        }
        if (FenceValidator.isErrorDateParam(config)) {
            errMessage.append("监控对象\"").append(checkMonitor).append("\"开始时间不能大于或等于结束时间<br/>");
            isCheckFail = true;
        }

        // 根据监控对象id和围栏id查询围栏是否存在绑定关系
        Map<String, Object> fenceConfig = findByVIdAndFId(vehicleId, fenceId);
        // 已绑定
        if (fenceConfig != null) {
            errMessage.append("监控对象\"").append(fenceConfig.get("brand")).append("\"与围栏\"")
                .append(fenceConfig.get("name")).append("\"已存在绑定关系，不能重复绑定！<br/>");
            isCheckFail = true;
        }
        return isCheckFail;
    }

    private String fenceTypeName(String type) {
        if (type != null && !"".equals(type)) {
            String typeName = "";
            switch (type) {
                case "zw_m_circle":
                    typeName = "圆形电子围栏";
                    break;
                case "zw_m_rectangle":
                    typeName = "矩形电子围栏";
                    break;
                case "zw_m_polygon":
                    typeName = "多边形电子围栏";
                    break;
                case "zw_m_line":
                    typeName = "线段围栏";
                    break;
                case "zw_m_travel_line":
                    typeName = "导航路线";
                    break;
                case "zw_m_marker":
                    typeName = "标注点";
                    break;
                case "zw_m_administration":
                    typeName = "行政区划";
                    break;
                default:
                    break;
            }
            return typeName;
        }
        return "";
    }

    /**
     * 有围栏数据变更时调用
     * @param type       围栏类型
     * @param vehicleIds 车辆id
     */
    private void updateData(String type, String vehicleIds) {
        try {
            switch (type) {
                case "zw_m_circle":
                    ZMQFencePub.pubChangeFence("4");
                    break;
                case "zw_m_rectangle":
                    ZMQFencePub.pubChangeFence("5");
                    break;
                case "zw_m_polygon":
                    ZMQFencePub.pubChangeFence("6");
                    break;
                case "zw_m_administration":
                    ZMQFencePub.pubChangeFence("7" + vehicleIds);
                    break;
                case "zw_m_line":
                    ZMQFencePub.pubChangeFence("8");
                    break;
                case "zw_m_travel_line":
                    ZMQFencePub.pubChangeFence("9");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("围栏数据变更调用ZMQFencePub的方法出错", e);
        }

    }

    @Override
    public JsonResultBean sendFenceData(List<JSONObject> paramList, String ipAddress) throws Exception {
        List<Map<String, Object>> mapList = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        String vehicleId = "";
        for (JSONObject obj : paramList) {
            vehicleId = obj.get("vehicleId").toString();
            String fenceId = obj.get("fenceId").toString();
            Map<String, Object> fenceConfig = findByVIdAndFId(vehicleId, fenceId);
            int source = (int) fenceConfig.get("alarm_source");
            // 0 终端报警  1 平台报警  2 平台与终端报警
            boolean isTerminalAlarmType = (0 == source || 2 == source);
            if (isTerminalAlarmType) {
                Map<String, Object> map = getFenceSendMap(obj);
                mapList.add(map);
                msg.append(getTerminalAlarmSendLog(paramList, fenceConfig));
            }
        }
        //下发围栏
        sendFenceByType(mapList);
        boolean isSingleSend = mapList.size() == 1;
        addSendLog(ipAddress, msg, vehicleId, isSingleSend);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private Map<String, Object> getFenceSendMap(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        map.put("bindId", obj.get("fenceConfigId"));
        map.put("vehicleId", obj.get("vehicleId"));
        // 1:围栏绑定 2：解除绑定
        map.put("sendType", "1");
        if (obj.get("paramId") != null && !"".equals(obj.get("paramId"))) {
            map.put("paramId", obj.get("paramId"));
        }
        return map;
    }

    private void addSendLog(String ipAddress, StringBuilder msg, String vehicleId, boolean isSingleSend)
        throws Exception {
        if (isSingleSend) {
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            logSearchService.addLog(ipAddress, msg.toString(), "3", "", vehicle[0], vehicle[1]);
        } else {
            logSearchService.addLog(ipAddress, msg.toString(), "3", "batch", "批量下发电子围栏");
        }
    }

    private String getTerminalAlarmSendLog(List<JSONObject> paramList, Map<String, Object> fenceConfig) {
        StringBuffer sb = new StringBuffer();
        String name = fenceConfig.get("name").toString();
        String brand = fenceConfig.get("brand").toString();
        String type = fenceConfig.get("type").toString();
        String typeName = fenceTypeName(type);
        if (paramList.size() == 1) {
            sb.append("监控对象 : ").append(brand).append(" 下发").append(typeName).append(" (").append(name).append(")");
        } else {
            sb.append("监控对象 : ").append(brand).append(" 下发").append(typeName).append(" (").append(name).append(" )")
                .append("<br/>");
        }
        return sb.toString();
    }

}
