package com.zw.platform.util;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class VehicleUtils {

    /**
     * 模糊搜索监控对象(不区分大小写)
     * @param isOther 为true表示要支持监控对象/SIM卡/终端编号的模糊搜索
     */
    public static List<String> fuzzQueryMonitors(String fuzzyParam, boolean isOther) {
        List<String> fuzzyIds = new ArrayList<>();
        if (StringUtils.isNotBlank(fuzzyParam)) {
            final RedisKey key = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
            if (fuzzyParam.matches(".*[a-zA-z].*")) { // 包含英文
                // 将英文转换为大写
                fuzzyParam = fuzzyParam.toUpperCase();

                Map<String, String> allList = RedisHelper.hgetAll(key);
                if (allList != null && allList.size() > 0) {
                    for (Map.Entry<String, String> entry : allList.entrySet()) {
                        if (entry.getKey().contains("&")) {
                            String[] keys = entry.getKey().split("&"); // 监控对象模糊搜索key
                            if (keys.length == 3) {
                                String monitor = keys[0]; // 监控对象编号
                                String device = keys[1]; // 终端编号
                                String simCard = keys[2]; // sim卡号
                                if (StringUtils.isNotBlank(monitor) && StringUtils.isNotBlank(device) && StringUtils
                                    .isNotBlank(simCard)) {
                                    monitor = monitor.toUpperCase();
                                    device = device.toUpperCase();
                                    simCard = simCard.toUpperCase();
                                    // 如果车牌号包含英文,则将英文转换为大写
                                    if ((!isOther && monitor.contains(fuzzyParam)) || (isOther && (
                                        monitor.contains(fuzzyParam) || device.contains(fuzzyParam) || simCard
                                            .contains(fuzzyParam)))) {
                                        String value = entry.getValue();
                                        if (value.contains("vehicle") && value.contains("simcard") && value
                                            .contains("device")) {
                                            fuzzyIds.add(value.split("vehicle")[1].split("&")[1]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else { // 不包含英文(值包含汉字、数字等合法字符)
                List<Map.Entry<String, String>> fuzzyMonitor;
                // 获取监控对象信息
                if (isOther) { // 为true表示要支持监控对象/SIM卡/终端编号的模糊搜索
                    fuzzyMonitor = RedisHelper.hscan(key, "*" + fuzzyParam + "*");
                } else {
                    fuzzyMonitor = RedisHelper.hscan(key, "*" + fuzzyParam + "*&*&*");
                }
                // 获取监控对象id
                if (fuzzyMonitor.size() > 0) {
                    for (Map.Entry<String, String> entry : fuzzyMonitor) {
                        String value = entry.getValue();
                        if (value.contains("vehicle") && value.contains("simcard") && value.contains("device")) {
                            fuzzyIds.add(value.split("vehicle")[1].split("&")[1]);
                        }
                    }
                }
            }
        }
        return fuzzyIds;
    }

}
