package com.zw.platform.service.netaccessproveforward.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.netaccessproveforward.NetAccessProveForwardVehicleDo;
import com.zw.platform.dto.netaccessproveforward.NetAccessProveForwardVehicleDto;
import com.zw.platform.dto.netaccessproveforward.NetAccessProveForwardVehicleQuery;
import com.zw.platform.repository.modules.NetAccessProveForwardDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.netaccessproveforward.ZhejiangNetAccessProveForwardService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/7/20 9:08
 */
@Service
public class ZhejiangNetAccessProveForwardServiceImpl implements ZhejiangNetAccessProveForwardService {

    @Value("${zhejiang.netAccessProveForward.ip:121.196.205.11}")
    private String netAccessProveForwardIp;
    @Value("${zhejiang.netAccessProveForward.port:10009}")
    private String netAccessProveForwardPort;
    @Value("${zhejiang.netAccessProveForward.path:/dtas-server/api/service/push}")
    private String netAccessProveForwardPath;
    @Value("${zhejiang.netAccessProveForward.token:4669ea87-4bc4-8dfd-e5be-401fd0a91941}")
    private String netAccessProveForwardToken;

    @Autowired
    private NetAccessProveForwardDao netAccessProveForwardDao;
    @Autowired
    private LogSearchService logSearchService;
    @Autowired
    private UserService userService;

    @Override
    public JsonResultBean addNetAccessProveForward(String vehicleIds, String ipAddress) throws Exception {
        if (StringUtils.isBlank(vehicleIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆id为空");
        }
        List<String> vehicleIdList = new ArrayList<>(Arrays.asList(vehicleIds.split(",")));
        // 已经转发的车辆
        List<String> existingVehicleIds = netAccessProveForwardDao.getAllVehicleIds();
        vehicleIdList.removeAll(existingVehicleIds);
        if (CollectionUtils.isEmpty(vehicleIdList)) {
            return new JsonResultBean();
        }
        List<RedisKey> monitorRedisKeys = RedisKeyEnum.MONITOR_INFO.ofs(vehicleIdList);
        List<BindDTO> configList = RedisHelper.batchGetHashMap(monitorRedisKeys).stream()
            .filter(o -> Objects.equals(o.get("bindType"), Vehicle.BindType.HAS_BIND))
            .map(o -> MapUtil.mapToObj(o, BindDTO.class)).collect(Collectors.toList());
        Date createDataTime = new Date();
        String createDataUsername = SystemHelper.getCurrentUsername();
        List<NetAccessProveForwardVehicleDo> list = configList.stream()
            .map(o -> new NetAccessProveForwardVehicleDo(o.getId(), createDataTime, createDataUsername))
            .collect(Collectors.toList());
        boolean flag = netAccessProveForwardDao.addNetAccessProveForward(list);
        if (!flag) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String url = "http://" + netAccessProveForwardIp + ":" + netAccessProveForwardPort + netAccessProveForwardPath;
        String resultStr =
            HttpClientUtil.sendPostByJson(url, JSON.toJSONString(assembleRequestParamMap(configList)));
        JSONObject resultJsonObj = JSON.parseObject(resultStr);
        if (resultJsonObj == null || !Objects.equals(resultJsonObj.getInteger("result"), 0)) {
            String errorMsg =
                "新增浙江入网证明转发异常：" + (resultJsonObj == null ? "推送数据异常" : resultJsonObj.getString("resultMsg"));
            throw new Exception(errorMsg);
        }
        StringBuilder logMessageBuilder = new StringBuilder();
        for (BindDTO configInfo : configList) {
            logMessageBuilder.append("监控对象：").append(configInfo.getName()).append(" 新增浙江入网证明转发 </br>");
        }
        logSearchService.addLog(ipAddress, logMessageBuilder.toString(), "3", "batch", "新增浙江入网证明转发");
        return new JsonResultBean();
    }

    @Override
    public PageGridBean getList(NetAccessProveForwardVehicleQuery query) {
        query.setUserId(userService.getUserUuidById(SystemHelper.getCurrentUId()));
        Page<NetAccessProveForwardVehicleDto> pageList =
            PageHelperUtil.doSelect(query, () -> netAccessProveForwardDao.listByPage(query));
        for (NetAccessProveForwardVehicleDto netAccessProveForwardVehicle : pageList) {
            netAccessProveForwardVehicle.setPlantFormName("浙江入网转发平台");
            netAccessProveForwardVehicle.setPlantFormIp(netAccessProveForwardIp);
            netAccessProveForwardVehicle.setPlantFormPort(netAccessProveForwardPort);
        }
        return new PageGridBean(query, pageList, true);
    }

    @Override
    public JsonResultBean deleteNetAccessProveForward(String vehicleIds, String ipAddress) {
        if (StringUtils.isBlank(vehicleIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆id为空");
        }
        List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
        netAccessProveForwardDao.deleteNetAccessProveForward(vehicleIdList);
        List<RedisKey> monitorRedisKeys = RedisKeyEnum.MONITOR_INFO.ofs(vehicleIdList);
        List<Map<String, String>> monitorData = RedisHelper.batchGetHashMap(monitorRedisKeys);
        StringBuilder logMessageBuilder = new StringBuilder();
        for (Map<String, String> monitor : monitorData) {
            logMessageBuilder.append("监控对象：").append(monitor.get("name")).append(" 解除浙江入网证明转发 </br>");
        }
        logSearchService.addLog(ipAddress, logMessageBuilder.toString(), "3", "batch", "解除浙江入网证明转发");
        return new JsonResultBean();
    }

    private Map<String, Object> assembleRequestParamMap(List<BindDTO> configList) {
        Map<String, Object> requestParamMap = new HashMap<>(16);
        requestParamMap.put("serviceName", "SC_VEHICLE_GPS_IN");
        requestParamMap.put("token", netAccessProveForwardToken);
        requestParamMap.put("updateNull", "true");
        JSONArray params = new JSONArray();
        for (BindDTO entry : configList) {
            String vehicleId = entry.getId();
            JSONObject paramJsonObj = new JSONObject();
            paramJsonObj.put("PLATE_NUMBER", entry.getName());
            paramJsonObj.put("PLATE_COLOR", entry.getPlateColor());
            String expireDateStr = entry.getExpireDate();
            if (StringUtils.isNotBlank(expireDateStr)) {
                paramJsonObj.put("GPS_END_DATE", expireDateStr);
            }
            paramJsonObj.put("EQUIPMENT_TYPE", entry.getTerminalType());
            paramJsonObj.put("SERIAL_NUMBER", entry.getDeviceNumber());
            paramJsonObj.put("SIM_NO", entry.getSimCardNumber());
            // 对接人给出的参数 写死
            paramJsonObj.put("PLATFORM_NAME", "北斗星云监控服务平台");
            paramJsonObj.put("PLATFORM_ID", 52147);
            paramJsonObj.put("ID", vehicleId);
            String createDateTime = entry.getBindDate();
            if (StringUtils.isNotBlank(createDateTime)) {
                paramJsonObj.put("RECEIVE_TIME", createDateTime);
                paramJsonObj.put("INSTALLATION_PROOF_DATE", createDateTime);
            }
            params.add(paramJsonObj);
        }
        requestParamMap.put("param", params);
        return requestParamMap;
    }
}
