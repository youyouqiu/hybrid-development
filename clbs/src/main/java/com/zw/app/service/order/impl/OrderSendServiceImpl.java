package com.zw.app.service.order.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.service.order.OrderSendService;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.service.oilmassmgt.OilCalibrationService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.MonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderSendServiceImpl implements OrderSendService {

    @Autowired
    private OilCalibrationService oilCalibrationService;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public String checkServerUnobstructed() throws Exception {
        return "Communication is normal";
    }

    /**
     * 下发8201 位置信息查询指令
     * @param monitorId 监控对象id
     * @return
     */
    @Override
    public JSONObject send0x8201(String monitorId, HttpServletRequest request) throws Exception {
        if (AppParamCheckUtil.check64String(monitorId)) {
            /** 判断改设备是否为808设备，若不是则不予下发参数 */
            BindDTO bindDTO = MonitorUtils.getBindDTO(monitorId, "name", "plateColor", "deviceType");
            if (bindDTO != null) {
                String deviceType = bindDTO.getDeviceType();
                JSONObject msg = new JSONObject();
                List<Integer> protocols = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808);
                if ("0".equals(deviceType) || protocols.contains(Integer.parseInt(deviceType))) {
                    String msgSN = oilCalibrationService.getLatestPositional(monitorId); // 流水号
                    msg.put("msgSN", msgSN);
                    /** 记录点名日志 */
                    String ip = new GetIpAddr().getIpAddr(request);
                    String brand = bindDTO.getName();
                    String logMsg = "监控对象：" + brand + " 车辆点名";
                    String plateColor = bindDTO.getPlateColor() == null
                        ? "" : bindDTO.getPlateColor().toString();
                    logSearchService.addLog(ip, logMsg, "4", "MONITORING", brand, plateColor);
                    return msg;
                } else {
                    msg.put("exceptionDetailMsg", "该设备不为808设备，不能点名");
                }
            }
        }
        return null;
    }
}
