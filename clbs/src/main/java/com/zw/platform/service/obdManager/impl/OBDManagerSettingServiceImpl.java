package com.zw.platform.service.obdManager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.OBDManagerSettingForm;
import com.zw.platform.domain.basicinfo.form.OBDVehicleDataInfo;
import com.zw.platform.domain.basicinfo.form.OBDVehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.OBDManagerSettingQuery;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.platform.domain.sendTxt.OBDFault;
import com.zw.platform.domain.sendTxt.SetStreamObd;
import com.zw.platform.domain.statistic.info.FaultCodeInfo;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.OBDManagerSettingDao;
import com.zw.platform.repository.modules.OBDVehicleTypeDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.obdManager.OBDManagerSettingService;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import com.zw.ws.impl.WsOilSensorCommandService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OBDManagerSettingServiceImpl implements OBDManagerSettingService, IpAddressService {

    @Autowired
    private OBDManagerSettingDao obdManagerSettingDao;


    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;

    @Autowired
    private OBDVehicleTypeDao obdVehicleTypeDao;

    @Autowired
    private WsOilSensorCommandService wsOilSensorCommandService;

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Autowired
    private NewConfigDao newConfigDao;

    @Override
    public Page<OBDManagerSettingForm> findList(OBDManagerSettingQuery query) {
        String protocol = query.getProtocol();
        String assignmentId = query.getAssignmentId();
        String orgId = query.getGroupId();
        String simpleQueryParam = query.getSimpleQueryParam();
        List<String> validVehicleIds = userService.getValidVehicleId(orgId, assignmentId, protocol, null, null, true);
        if (CollectionUtils.isEmpty(validVehicleIds)) {
            return new Page<>();
        }
        Map<String, OBDVehicleTypeForm> obdVehicleTypeMap = obdVehicleTypeDao.findAll()
            .stream()
            .collect(Collectors.toMap(OBDVehicleTypeForm::getId, Function.identity()));

        if (StringUtils.isNotBlank(simpleQueryParam)) {
            String finalSimpleQueryParam = simpleQueryParam;
            Set<String> filterObdVehicleTypeIds = obdVehicleTypeMap.values()
                .stream()
                .filter(obj -> obj.getName().contains(finalSimpleQueryParam))
                .map(OBDVehicleTypeForm::getId)
                .collect(Collectors.toSet());
            Set<String> filterObdSettingMoIds = CollectionUtils.isEmpty(filterObdVehicleTypeIds) ? new HashSet<>() :
                obdManagerSettingDao.getByObdVehicleTypeIds(filterObdVehicleTypeIds)
                    .stream()
                    .map(OBDManagerSettingForm::getVehicleId)
                    .collect(Collectors.toSet());
            simpleQueryParam = com.zw.platform.util.StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam);
            // 模糊搜索监控对象
            Set<String> filterMoIds = newConfigDao.getMoIdsByFuzzyMoName(simpleQueryParam);
            filterMoIds.addAll(filterObdSettingMoIds);
            validVehicleIds.retainAll(filterMoIds);
            if (CollectionUtils.isEmpty(validVehicleIds)) {
                return new Page<>();
            }
        }
        int total = validVehicleIds.size();
        // 当前页
        int curPage = query.getPage().intValue();
        // 每页条数
        int pageSize = query.getLimit().intValue();
        // 遍历开始条数
        int start = (curPage - 1) * pageSize;
        // 遍历结束条数
        int end = pageSize > (total - start) ? total : (pageSize * curPage);
        List<String> pageVehicleIdList = validVehicleIds.subList(start, end);
        Map<String, OBDManagerSettingForm> vehicleObdSetMap = obdManagerSettingDao.getListByMoIds(pageVehicleIdList)
            .stream()
            .collect(Collectors.toMap(OBDManagerSettingForm::getVehicleId, Function.identity()));
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(pageVehicleIdList);
        List<OBDManagerSettingForm> resultList = new ArrayList<>();
        for (String moId : pageVehicleIdList) {
            OBDManagerSettingForm obdManagerSettingForm = vehicleObdSetMap.get(moId);
            if (obdManagerSettingForm == null) {
                obdManagerSettingForm = new OBDManagerSettingForm();
            }
            obdManagerSettingForm.setVehicleId(moId);
            BindDTO bindDTO = bindInfoMap.get(moId);
            if (bindDTO != null) {
                obdManagerSettingForm.setBrand(bindDTO.getName());
                obdManagerSettingForm.setGroupId(bindDTO.getOrgId());
                obdManagerSettingForm.setGroupName(bindDTO.getOrgName());
                String monitorType = bindDTO.getMonitorType();
                obdManagerSettingForm
                    .setMonitorType(StringUtils.isNotBlank(monitorType) ? Integer.parseInt(monitorType) : null);
            }
            resultList.add(obdManagerSettingForm);

        }
        Page<OBDManagerSettingForm> page = RedisQueryUtil.getListToPage(resultList, query, total);
        //赋值
        setStatus(page, pageVehicleIdList);
        return page;
    }

    private void setStatus(List<OBDManagerSettingForm> resultList, List<String> pageVehicleIdList) {
        if (CollectionUtils.isEmpty(resultList) || CollectionUtils.isEmpty(pageVehicleIdList)) {
            return;
        }
        String paramType = "F3-8900-obd";
        Map<String, List<Directive>> monitorDirectiveListMap =
            parameterDao.findDirectiveByMoIdAndType(pageVehicleIdList, paramType).stream()
                .collect(Collectors.groupingBy(Directive::getMonitorObjectId));
        for (OBDManagerSettingForm obdSet : resultList) {
            String vehicleId = obdSet.getVehicleId();
            String obdSetId = obdSet.getId();
            if (StringUtils.isBlank(vehicleId) || StringUtils.isBlank(obdSetId)) {
                continue;
            }

            List<Directive> directiveList = monitorDirectiveListMap.get(vehicleId);
            Directive param = null;
            if (CollectionUtils.isNotEmpty(directiveList)) {
                String parameterName = obdSetId + paramType;
                param = directiveList
                    .stream()
                    .filter(obj -> Objects.equals(obj.getParameterName(), parameterName))
                    .max(Comparator.comparing(Directive::getCreateDataTime))
                    .orElse(null);
            }
            if (param != null) {
                obdSet.setStatus(param.getStatus());
                obdSet.setParamId(param.getId());
            }
        }
    }

    @Override
    public OBDManagerSettingForm findByVid(String vehicleId) {
        if (StringUtils.isEmpty(vehicleId)) {
            return null;
        }
        OBDManagerSettingQuery query = new OBDManagerSettingQuery();
        query.setVehicleId(vehicleId);
        String userUuid = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
        List<String> userOrgIds = userService.getCurrentUserOrgIds();
        Page<OBDManagerSettingForm> obdManagerSettingForms =
            obdManagerSettingDao.findOBDSetting(query, userUuid, userOrgIds);

        if (CollectionUtils.isEmpty(obdManagerSettingForms)) {
            return null;
        }
        OBDManagerSettingForm obdManagerSettingForm = obdManagerSettingForms.get(0);
        String obdSetId = obdManagerSettingForm.getId();
        String paramType = "F3-8900-obd";
        String parameterName = obdSetId + paramType;
        List<Directive> directiveList = parameterDao.findParameterByType(vehicleId, parameterName, paramType);
        Directive param = null;
        if (CollectionUtils.isNotEmpty(directiveList)) {
            param = directiveList.get(0);
        }
        if (param != null) {
            obdManagerSettingForm.setStatus(param.getStatus());
            obdManagerSettingForm.setParamId(param.getId());
        }
        return obdManagerSettingForm;
    }

    @Override
    public JsonResultBean addObdManagerSetting(OBDManagerSettingForm form) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = obdManagerSettingDao.addOBDManagerSetting(form);
        if (!flag) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //缓存
        setObdRedis(form);
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        String brand = "";
        String plateColor = "";
        if (bindDTO != null) {
            brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            plateColor = plateColorInt == null ? "" : plateColorInt.toString();
        }
        logSearchService.addLog(getIpAddress(), "监控对象【" + brand + "】 绑定OBD管理设置", "3", "", brand, plateColor);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 设置OBD缓存
     */
    private void setObdRedis(OBDManagerSettingForm form) {
        String typeId = form.getObdVehicleTypeId();
        OBDVehicleTypeForm obdVehicleTypeForm = obdVehicleTypeDao.findById(typeId);
        String value = obdVehicleTypeForm.getName() + RedisKeys.SEPARATOR + form.getId();
        com.zw.platform.basic.core.RedisHelper.addToHash(
                RedisKeyEnum.OBD_SETTING_MONITORY_LIST.of(), form.getVehicleId(), value);
    }

    @Override
    public OBDManagerSettingForm findObdSettingById(String id) {
        return obdManagerSettingDao.findOBDSettingById(id);
    }

    @Override
    public List<OBDManagerSettingForm> findObdSettingByVid(String vid) {
        return obdManagerSettingDao.findOBDSettingByVid(vid);
    }

    @Override
    public List<OBDManagerSettingForm> getReferentInfo(String vid, List<Integer> protocols) {
        // 获取当前用户所属组织及下级组织
        List<String> userOrgIds = userService.getCurrentUserOrgIds();
        if (CollectionUtils.isEmpty(userOrgIds)) {
            return  new ArrayList<>();
        }
        String userUuid = userService.getCurrentUserUuid();
        List<OBDManagerSettingForm> list = obdManagerSettingDao.getReferentInfo(userUuid, userOrgIds, protocols);
        if (CollectionUtils.isNotEmpty(list)) {
            //参考对象 排除自己
            list = list.stream().filter(form -> !vid.equals(form.getVehicleId())).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    public JsonResultBean updateObdManagerSetting(OBDManagerSettingForm form) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = obdManagerSettingDao.updateOBDManagerSetting(form);
        if (!flag) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        clearStatus(form);
        //缓存
        setObdRedis(form);
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        String brand = "";
        String plateColor = "";
        if (bindDTO != null) {
            brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            plateColor = plateColorInt == null ? "" : plateColorInt.toString();
        }
        logSearchService.addLog(getIpAddress(), "监控对象【" + brand + "】 修改OBD管理设置", "3", "", brand, plateColor);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 清空下发状态
     */
    private void clearStatus(OBDManagerSettingForm form) {
        String paramType = "F3-8900-obd";
        sendHelper.deleteByVehicleIdParameterName(form.getVehicleId(), form.getId() + paramType, paramType);
    }

    @Override
    public JsonResultBean deleteObdManagerSetting(String id) {
        List<String> ids = Arrays.stream(id.split(",")).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        String brand = "";
        String color = "";
        Set<String> moIds;
        if (ids.size() > 1) {
            List<OBDManagerSettingForm> obdManagerSettingFormList = obdManagerSettingDao.getListByIds(ids);
            moIds = obdManagerSettingFormList
                .stream()
                .map(OBDManagerSettingForm::getVehicleId)
                .collect(Collectors.toSet());
            Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds, Lists.newArrayList("name"));
            for (OBDManagerSettingForm obdManagerSettingForm : obdManagerSettingFormList) {
                BindDTO bindDTO = bindInfoMap.get(obdManagerSettingForm.getVehicleId());
                if (bindDTO != null) {
                    brand = bindDTO.getName();
                }
                sb.append("监控对象【").append(brand).append("】 解除OBD管理设置绑定</br>");
            }
        } else {
            OBDManagerSettingForm form = obdManagerSettingDao.findOBDSettingById(ids.get(0));
            moIds = Sets.newHashSet(form.getVehicleId());
            BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
            if (bindDTO != null) {
                brand = bindDTO.getName();
                Integer plateColorInt = bindDTO.getPlateColor();
                color = plateColorInt == null ? "" : plateColorInt.toString();
            }
            sb.append("监控对象【").append(brand).append("】 解除OBD管理设置绑定");
        }
        boolean flag = obdManagerSettingDao.deleteOBDManagerSetting(ids);
        if (!flag) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //删除缓存
        com.zw.platform.basic.core.RedisHelper.hdel(HistoryRedisKeyEnum.OBD_SETTING_MONITORY_LIST.of(), moIds);
        String ip = getIpAddress();
        if (ids.size() > 1) {
            logSearchService.addLog(ip, sb.toString(), "3", "batch", "批量解除OBD管理设置绑定");
        } else {
            logSearchService.addLog(ip, sb.toString(), "3", "", brand, color);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public void sendObdParam(List<JSONObject> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        StringBuilder msg = new StringBuilder();
        String username = SystemHelper.getCurrentUsername();
        Set<String> moIds = list
            .stream()
            .map(jsonObj -> jsonObj.getString("vehicleId"))
            .collect(Collectors.toSet());
        Set<String> obdSetIds = list
            .stream()
            .map(jsonObj -> jsonObj.getString("id"))
            .collect(Collectors.toSet());
        Map<String, OBDManagerSettingForm> obdSetMap = obdManagerSettingDao.getListByIds(obdSetIds)
            .stream()
            .collect(Collectors.toMap(OBDManagerSettingForm::getId, Function.identity()));
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        for (JSONObject object : list) {
            String vehicleId = object.getString("vehicleId");
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                continue;
            }
            String paramId = object.getString("paramId");
            String id = object.getString("id");

            String deviceNumber = bindDTO.getDeviceNumber();
            // 序列号
            Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
            String paramType = "F3-8900-obd";
            // 设备已经注册
            if (msgSno != null) {
                // 已下发
                int status = 4;
                // 下发参数
                // 先更新下发状态再下发: 避免车辆第一次下发不存在paramId的情况
                paramId =
                    sendHelper.updateParameterStatus(paramId, msgSno, status, vehicleId, paramType, id + paramType);
                SendParam sendParam = new SendParam();
                sendParam.setMsgSNACK(msgSno);
                sendParam.setParamId(paramId);
                sendParam.setVehicleId(vehicleId);
                f3SendStatusProcessService.updateSendParam(sendParam, 1);
                OBDManagerSettingForm obdManagerSettingForm = obdSetMap.get(id);
                OrderForm orderForm = new OrderForm();
                orderForm.setVid(obdManagerSettingForm.getVehicleId());
                orderForm.setVehicleTypeId(Long.parseLong(obdManagerSettingForm.getCode().substring(2), 16));
                orderForm.setUploadTime(obdManagerSettingForm.getTime());
                msg.append(sendObd(bindDTO, orderForm, msgSno, username));
            } else { // 设备未注册
                int status = 5;
                msgSno = 0;
                // 工时绑定下发
                sendHelper.updateParameterStatus(paramId, msgSno, status, vehicleId, paramType, id + paramType);
            }
        }
        if (msg.length() <= 0) {
            return;
        }
        String ip = getIpAddress();
        if (list.size() > 1) {
            logSearchService.addLog(ip, msg.toString(), "3", "batch", "批量下发OBD参数");
        } else {
            String firstMoId = list.get(0).getString("vehicleId");
            BindDTO bindDTO = bindInfoMap.get(firstMoId);
            String brand = "";
            String plateColor = "";
            if (bindDTO != null) {
                brand = bindDTO.getName();
                Integer plateColorInt = bindDTO.getPlateColor();
                plateColor = plateColorInt == null ? "" : plateColorInt.toString();
            }
            logSearchService.addLog(ip, msg.toString().replace("</br>", ""), "3", "", brand, plateColor);
        }
    }

    @Override
    public JsonResultBean sendObdInfo(String vid, Integer commandType) {
        // 获取车辆及设备信息
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vid);
        if (bindDTO == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象不存在");
        }
        String name = bindDTO.getName();
        String orgName = bindDTO.getOrgName();
        Integer plateColorInt = bindDTO.getPlateColor();
        String plateColor = plateColorInt == null ? "" : plateColorInt.toString();
        String deviceId = bindDTO.getDeviceId();
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceType = bindDTO.getDeviceType();
        String simCardNumber = bindDTO.getSimCardNumber();
        // 序列号
        Integer msgSno = DeviceHelper.getRegisterDevice(vid, deviceNumber);
        String paramType = "F3-8900-obd-" + commandType;
        String paramId = "";
        // 设备已经注册
        if (msgSno != null) {
            // 已下发
            int status = 4;
            // 下发参数
            // 先更新下发状态再下发: 避免车辆第一次下发不存在paramId的情况
            paramId = sendHelper.updateParameterStatus(paramId, msgSno, status, vid, paramType, vid + paramType);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSno);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vid);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            if (commandType == 1) {
                //查看故障码
                OBDFault fault = new OBDFault();
                JSONArray obdArr = new JSONArray();
                obdArr.add(fault);
                T808_0x8900<Object> t8080x8900 = new T808_0x8900<>();
                t8080x8900.setType(0xF1);
                t8080x8900.setSum(1);
                t8080x8900.setSensorDatas(obdArr);

                //下发8900
                T808Message message = MsgUtil
                    .get808Message(simCardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSno, t8080x8900, deviceType);
                WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
            } else if (commandType == 2) {
                //查看OBD数据  下发点名 订阅0201
                wsOilSensorCommandService.vehicleLocationQuery(msgSno, bindDTO);
            }
        } else { // 设备未注册
            int status = 5;
            msgSno = 0;
            // 工时绑定下发
            sendHelper.updateParameterStatus(paramId, msgSno, status, vid, paramType, vid + paramType);
            return new JsonResultBean(JsonResultBean.FAULT, "终端离线");
        }
        String module = "";
        switch (commandType) {
            case 1:
                module = "查看故障码";
                break;
            case 2:
                module = "查看OBD数据";
                break;
            default:
                break;
        }
        String logMsg = "监控对象：" + name + "( @" + orgName + ")" + module;
        logSearchService.addLog(getIpAddress(), logMsg, "3", "", name, plateColor);
        String username = SystemHelper.getCurrentUsername();
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSno));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
    }

    @Override
    public FaultCodeInfo findFaultCodeByVid(String vid) {
        return obdVehicleTypeDao.findFaultCodeByVid(vid);

    }

    @Override
    public boolean findIsBandObdSensor(String monitorId) {
        Integer count = obdManagerSettingDao.findIsBandObdSensor(monitorId);
        return count != null && count > 0;
    }

    @Override
    public JsonResultBean getCacheObd(String vehicleId) {
        if (StringUtils.isEmpty(vehicleId)) {
            return new JsonResultBean(false);
        }
        String cacheLocationInfo = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleId));
        if (StringUtils.isBlank(cacheLocationInfo)) {
            return new JsonResultBean(JsonResultBean.FAULT, "未查询到OBD参数");
        }
        Message message = JSON.parseObject(cacheLocationInfo, Message.class);
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
        OBDVehicleDataInfo obdInfo = obdVehicleTypeService.convertStreamToObdInfo(info);
        info.setObdObjStr(JSON.toJSONString(obdInfo));
        t808Message.setMsgBody(info);
        message.setData(t808Message);
        return new JsonResultBean(message);
    }

    private String sendObd(BindDTO bindDTO, OrderForm orderForm, Integer msgSno, String username) {
        if (bindDTO == null) {
            return "";
        }
        SetStreamObd setStreamObd = new SetStreamObd();
        setStreamObd.setVehicleTypeId(orderForm.getVehicleTypeId());
        if (orderForm.getUploadTime() == null) {
            setStreamObd.setUploadTime(0xFFFFFFFF);
        } else {
            // 单位 : ms
            setStreamObd.setUploadTime(orderForm.getUploadTime() * 1000);
        }
        T808_0x8103 benchmark = new T808_0x8103();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamLength(64);
        paramItem.setParamId(0xF3E5);
        paramItem.setParamValue(setStreamObd);
        List<ParamItem> paramItems = new ArrayList<>();
        paramItems.add(paramItem);
        benchmark.setParamItems(paramItems);
        benchmark.setParametersCount(1);

        String name = bindDTO.getName();
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        String simCardNumber = bindDTO.getSimCardNumber();
        // 订阅
        SubscibeInfo info = new SubscibeInfo(username, deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK, 1);
        SubscibeInfoCache.getInstance().putTable(info);
        SubscibeInfo info1 = new SubscibeInfo(username, deviceId, msgSno, ConstantUtil.T808_DATA_PERMEANCE_REPORT);
        SubscibeInfoCache.getInstance().putTable(info1);

        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_PARAM, msgSno, benchmark, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
        paramSendingCache.put(username, msgSno, simCardNumber, SendTarget.getInstance(SendModule.OBD));
        return "监控对象【" + name + "】 下发OBD设置参数</br>";
    }
}
