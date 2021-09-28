package com.zw.platform.util;

import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrackBackUtil {


    /**
     * 行驶
     */
    public static final String RUNNING_STATE = "1";

    /**
     * 停止
     */
    public static final String STOP_STATE = "2";

    /**
     * Remove Redis Data
     *
     * @param redisKey redisKey
     */
    public static void removeRedisData(RedisKey redisKey) {
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
    }


    /**
     * 获取超待类型
     */
    public static String getfunctionalType(String vehicleId) {
        String functionalType = "";
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindInfo != null) {
            functionalType = bindInfo.getDeviceType();
        }
        if (functionalType.equals("10") || functionalType.equals("9")) {
            functionalType = "standby";
        }
        return functionalType;
    }


    /**
     * 存储查询结果到Redis
     *
     * @param redisKey       redisKey
     * @param redisData      需要存储到Redis中的数据
     */
    public static void addResultToRedis(RedisKey redisKey, String redisData) {
        RedisHelper.setString(redisKey, redisData, 2 * 60 * 60);
    }

    /**
     * 初始行驶状态
     *
     * @param flogKey    是否绑定里程传感器
     * @param positional 位置信息
     */
    public static void initDrivingState(boolean flogKey, Positional positional) {
        double speedDouble = flogKey ? (null != positional.getMileageSpeed() ? positional.getMileageSpeed() : 0.0) :
            (null != positional.getSpeed() ? Double.parseDouble(positional.getSpeed()) : 0.0);
        if (speedDouble > 5) {
            positional.setDrivingState(RUNNING_STATE);
        } else {
            positional.setDrivingState(STOP_STATE);
        }
    }

    /**
     * status解析
     * acc,0：熄火,1：点火
     */
    public static String getAccAndStatus(String status, int index) {
        if (status != null && !"null".equals(status)) {
            long statusValue = Long.parseLong(status);
            long l = statusValue & index;
            if (l > 0) {
                return "1";
            }
            return "0";
        }
        return "";
    }

    /**
     * 计算行驶状态
     */
    public static void calculateDrivingStatus(Positional positional, List<Positional> list,
        boolean flogKey, int listSize, int i) {
        String drivingState = positional.getDrivingState();
        List<Positional> subList = new ArrayList<>();
        String afterDrivingState = null;
        if (RUNNING_STATE.equals(drivingState)) {
            //判断后5个是否超出长度
            if (i + 1 < listSize) {
                //如果当前点是行驶状态，从左往右判断，如果后继连续5个点的速度都小于5km/h，
                //则这5个点对应的状态都调整成停止，如果没出现，则都直接设置为行驶状态。
                subList = list.stream().skip(i + 1).limit(5).collect(Collectors.toList());
                if (subList.size() >= 5) {
                    double maxSpeedDouble = subList.stream().mapToDouble(
                        info -> flogKey ? (null != info.getMileageSpeed() ? info.getMileageSpeed() : 0.0) :
                            (null != info.getSpeed() ? Double.parseDouble(info.getSpeed()) : 0.0)).max().getAsDouble();
                    //最大值都小于5,那么所有值都小于5;
                    if (maxSpeedDouble <= 5) {
                        afterDrivingState = STOP_STATE;
                    } else {
                        afterDrivingState = RUNNING_STATE;
                    }
                } else {
                    afterDrivingState = RUNNING_STATE;
                }
            }
        } else {
            //判断后3个是否超出长度
            if (i + 1 < listSize) {
                //如果第一个点是停止状态，从左往右判断，如果出现连续3个点的速度都大于5km/h，
                //则这3个点对应的状态都调整成行驶，如果没出现，则都直接设置为停止状态
                subList = list.stream().skip(i + 1).limit(3).collect(Collectors.toList());
                if (subList.size() >= 3) {
                    double minSpeedDouble = subList.stream().mapToDouble(
                        info -> flogKey ? (null != info.getMileageSpeed() ? info.getMileageSpeed() : 0.0) :
                            (null != info.getSpeed() ? Double.parseDouble(info.getSpeed()) : 0.0)).min().getAsDouble();
                    //最小值都大于5,那么所有的都大于5;
                    if (minSpeedDouble > 5) {
                        afterDrivingState = RUNNING_STATE;
                    } else {
                        afterDrivingState = STOP_STATE;
                    }
                } else {
                    afterDrivingState = STOP_STATE;
                }
            }
        }
        if (StringUtils.isNotBlank(afterDrivingState)) {
            String finalAfterDrivingState = afterDrivingState;
            subList.forEach(info -> info.setDrivingState(finalAfterDrivingState));
        }
    }
}
