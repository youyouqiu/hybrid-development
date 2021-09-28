package com.zw.platform.push.handler.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.ws.common.MessageEncapsulationHelper;
import com.zw.ws.entity.MessageType;
import com.zw.ws.entity.common.ClientWebSocketRequest;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jiangxiaoqiang on 2016/10/27.
 */
@Component
@Log4j2
public class CommonDataOperation {
    /**
     * 获取缓存状态(重置版)
     */
    public String getCacheStatusInfo(String requestContent) {

        ClientWebSocketRequest<List<ClientVehicleInfo>> clientWebSocketRequest =
            JSON.parseObject(requestContent, new TypeReference<ClientWebSocketRequest<List<ClientVehicleInfo>>>() {
            });
        List<ClientVehicleInfo> clientVehicleList = clientWebSocketRequest.getData();
        List<String> monitorIds =
            clientVehicleList.stream().map(ClientVehicleInfo::getVehicleId).collect(Collectors.toList());

        List<RedisKey> statusRedisKeys = HistoryRedisKeyEnum.MONITOR_STATUS.ofs(monitorIds);
        List<String> statusList = com.zw.platform.basic.core.RedisHelper.batchGetString(statusRedisKeys);

        List<ClientVehicleInfo> result = new ArrayList<>();
        for (String statusStr : statusList) {
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(statusStr, ClientVehicleInfo.class);
            if (clientVehicleInfo != null && result.indexOf(clientVehicleInfo) == -1) {
                result.add(clientVehicleInfo);
            }
        }
        return MessageEncapsulationHelper
            .webSocketMessageEncapsulation(result, MessageType.BS_CLIENT_REQUEST_VEHICLE_CACHE_STATUS_INTO);
    }

}
