package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.monitoring.RefrigeratorForm;
import com.zw.platform.service.monitoring.HumitureService;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HumitureServiceImpl implements HumitureService {


    @Autowired
    private UserService userService;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<RefrigeratorForm> getTempDtaAndHumData(String vehicleId, String starTime, String endTime)
        throws Exception {
        long starTimes = DateUtils.parseDate(starTime, DATE_FORMAT).getTime() / 1000;
        long endTimes = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        List<Positional> data = getTempDataAndHumidityData(vehicleId, starTimes, endTimes);
        List<RefrigeratorForm> result = new ArrayList<>();
        if (data != null) {
            for (Positional positional : data) {
                RefrigeratorForm refrigeratorForm = new RefrigeratorForm();
                if (positional.getVehicleId() != null) {
                    refrigeratorForm
                        .setVehcileId(String.valueOf(UuidUtils.getUUIDFromBytes(positional.getVehicleId())));// 车辆id
                }
                if (positional.getVtime() != 0) {
                    refrigeratorForm.setVTime(positional.getVtime());// 上传时间
                }
                if (positional.getPlateNumber() != null) {
                    refrigeratorForm.setPlateNumber(positional.getPlateNumber());// 车牌号
                }
                if (positional.getWetnessValueOne() != null) {
                    if (positional.getWetnessValueOne() >= 0 && positional.getWetnessValueOne() <= 100) {
                        // 一号湿度传感器湿度
                        refrigeratorForm
                            .setWetnessValueOne(Double.parseDouble(positional.getWetnessValueOne().toString()));
                    }
                }
                if (positional.getWetnessValueTwo() != null) {
                    if (positional.getWetnessValueTwo() >= 0 && positional.getWetnessValueTwo() <= 100) {
                        // 二号湿度传感器湿度
                        refrigeratorForm
                            .setWetnessValueTwo(Double.parseDouble(positional.getWetnessValueTwo().toString()));
                    }
                }
                if (positional.getWetnessValueThree() != null) {
                    if (positional.getWetnessValueThree() >= 0 && positional.getWetnessValueThree() <= 100) {
                        // 三号湿度传感器湿度
                        refrigeratorForm
                            .setWetnessValueThree(Double.parseDouble(positional.getWetnessValueThree().toString()));
                    }
                }
                if (positional.getWetnessValueFour() != null) {
                    if (positional.getWetnessValueFour() >= 0 && positional.getWetnessValueFour() <= 100) {
                        // 四号温度传感器温度
                        refrigeratorForm
                            .setWetnessValueFour(Double.parseDouble(positional.getWetnessValueFour().toString()));
                    }
                }
                // 温度数据正常的范围在 -55°C～+125°C之间,超过的数据就过滤
                if (positional.getTempValueOne() != null && positional.getTempValueOne() / 10 <= 125
                    && positional.getTempValueOne() / 10 >= -55) {
                    refrigeratorForm.setTempValueOne((positional.getTempValueOne()) / 10.0);// 一号温度传感器温度
                }
                if (positional.getTempValueTwo() != null && positional.getTempValueTwo() / 10 <= 125
                    && positional.getTempValueTwo() / 10 >= -55) {
                    refrigeratorForm.setTempValueTwo((positional.getTempValueTwo()) / 10.0);// 二号温度传感器温度
                }
                if (positional.getTempValueThree() != null && positional.getTempValueThree() / 10 <= 125
                    && positional.getTempValueThree() / 10 >= -55) {
                    refrigeratorForm.setTempValueThree((positional.getTempValueThree()) / 10.0);// 三号温度传感器温度
                }
                if (positional.getTempValueFour() != null && positional.getTempValueFour() / 10 <= 125
                    && positional.getTempValueFour() / 10 >= -55) {
                    refrigeratorForm.setTempValueFour((positional.getTempValueFour()) / 10.0);// 四号温度传感器温度
                }
                if (positional.getTempValueFive() != null && positional.getTempValueFive() / 10 <= 125
                    && positional.getTempValueFive() / 10 >= -55) {
                    refrigeratorForm.setTempValueFive((positional.getTempValueFive()) / 10.0);// 五号温度传感器温度
                }
                result.add(refrigeratorForm);
            }
        }
        return result;
    }

    private List<Positional> getTempDataAndHumidityData(String vehicleId, long starTimes, long endTimes) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(starTimes));
        params.put("endTime", String.valueOf(endTimes));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_TEMP_DATA_AND_HUMIDITY_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JSONObject getMonitorLastLocationById(String vehicleId) throws Exception {
        Set<String> onlyId;
        // 如果传过来的id为空,就获取用户权限下的车辆且不处于离线状态的车辆id
        if (StringUtils.isBlank(vehicleId)) {
            onlyId = userService.getCurrentUserMonitorIds();
        } else {
            onlyId = new HashSet<>(Arrays.asList(vehicleId.split(",")));
        }
        List<String> monitorIds = new ArrayList<>(onlyId);
        onlyId.clear();
        List<Message> monitorLocation = getLocationInfo(monitorIds);
        // prepareBackData(monitorTempData, monitorIds);
        return getTemAndHumDataByLocation(monitorLocation);
    }

    /**
     * 获取监控对象最后一条位置信息
     */
    private List<Message> getLocationInfo(List<String> vehicleId) {
        List<Message> messages = new ArrayList<>();
        if (CollectionUtils.isEmpty(vehicleId)) {
            return messages;
        }

        List<RedisKey> statusKeys = HistoryRedisKeyEnum.MONITOR_STATUS.ofs(vehicleId);
        List<String> monitorStatusInfos = com.zw.platform.basic.core.RedisHelper.batchGetString(statusKeys);

        if (CollectionUtils.isEmpty(monitorStatusInfos)) {
            return messages;
        }
        // 在线车辆的id
        Set<String> onlineStatusMonitorId = new HashSet<>();
        for (String statusInfo : monitorStatusInfos) {
            if (StringUtils.isBlank(statusInfo)) {
                continue;
            }
            JSONObject jsonObject = JSONObject.parseObject(statusInfo);
            if (jsonObject == null) {
                continue;
            }
            Integer monitorStatus = jsonObject.getInteger("vehicleStatus");
            if (monitorStatus == 3) { // 离线
                continue;
            }
            String id = jsonObject.getString("vehicleId");
            onlineStatusMonitorId.add(id);
        }
        if (CollectionUtils.isEmpty(onlineStatusMonitorId)) {
            return messages;
        }

        List<RedisKey> locationKeys = HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(onlineStatusMonitorId);
        List<String> resList = com.zw.platform.basic.core.RedisHelper.batchGetString(locationKeys);
        for (String location : resList) {
            if (StringUtils.isNotBlank(location)) {
                Message result = JSONObject.parseObject(location, Message.class);
                messages.add(result);
            }
        }
        return messages;
    }

    /**
     * 从位置信息中解析出监控对象所绑定的温湿度传感器中的第一个传感器的数据
     */
    private JSONObject getTemAndHumDataByLocation(List<Message> monitorLocation) {
        JSONObject msg = new JSONObject();
        if (CollectionUtils.isEmpty(monitorLocation)) {
            return msg;
        }
        List<String> monitorTempValue = new ArrayList<>();
        List<String> haveTempMonitorId = new ArrayList<>();
        monitorLocation.forEach(location -> {
            T808Message t808Message = JSON.parseObject(location.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            String monitorId = info.getMonitorInfo().getMonitorId(); // 监控对象id
            haveTempMonitorId.add(monitorId);
            if (info.getTemperatureSensor() == null || info.getTemperatureSensor().size() <= 0) {
                monitorTempValue.add("");
            } else {
                JSONArray temperatureSensor = info.getTemperatureSensor(); // 温度传感器数据
                JSONObject minTempSensor = (JSONObject) temperatureSensor.get(0);
                String minTempSensorData = minTempSensor.getString("temperature");
                monitorTempValue.add(minTempSensorData);
            }
        });
        msg.put("monitorId", haveTempMonitorId);
        msg.put("value", monitorTempValue);
        return msg;
    }

}
