/*
 * Copyright (c) 2019 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.Converter;
import com.zw.ws.entity.t808.location.defence.CircleAreaItem;
import com.zw.ws.entity.defence.RegionAttributePositionDefinition;
import com.zw.ws.entity.t808.location.defence.PolygonNodeItem;
import com.zw.ws.entity.t808.location.defence.RectangleAreaItem;
import com.zw.ws.entity.t808.location.defence.T808Msg0x8604;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Log4j2
@Component
public class EntitiesMappingHelper {
    private static boolean isCheckMode = false;

    @Value("${mode.check}")
    private void setCheckMode(boolean isCheckMode) {
        EntitiesMappingHelper.isCheckMode = isCheckMode;
    }

    public static List<CircleAreaItem> mappingCircles(List<Map<String, Object>> circles) {

        Integer sendDownId;

        ArrayList<CircleAreaItem> circleAreaItems = new ArrayList<>();
        Circle circle;
        FenceConfig config;
        for (Map<String, Object> map : circles) {
            circle = (Circle) map.get("fence");
            config = (FenceConfig) map.get("config");
            if (circle == null || config == null) {
                continue;
            }
            CircleAreaItem circleAreaItem = new CircleAreaItem();
            double[] resultLocation = getCoordinators(circle.getLatitude(), circle.getLongitude());
            circleAreaItem.setCenterLatitude(resultLocation[0]);
            circleAreaItem.setCenterLongitude(resultLocation[1]);
            Integer contrastNumber = CommonUtil.abs(circle.getId().replaceAll("-", "").hashCode());
            sendDownId = config.getSendDownId();
            if (sendDownId != null && sendDownId != 0 && contrastNumber.equals(sendDownId)) {
                circleAreaItem.setCircleAreaId(sendDownId);
            } else {
                circleAreaItem.setCircleAreaId(1);
                log.error("????????????ID???1???sendDownId: {}, contrastNumber: {}", sendDownId, contrastNumber);
            }
            circleAreaItem.setCircleAreaName(circle.getName());
            circleAreaItem.setCircleAreaNameLength(circle.getName().length());
            circleAreaItem.setRadius(circle.getRadius());
            assembleCircleProperties(config, circleAreaItem, (boolean) map.get("isProtocol2019"));

            circleAreaItems.add(circleAreaItem);
        }
        return circleAreaItems;
    }

    /**
     * ????????????????????????
     *
     * @param config
     * @param circleAreaItem
     * @param isProtocol2019
     */
    private static void assembleCircleProperties(FenceConfig config, CircleAreaItem circleAreaItem,
                                                 boolean isProtocol2019) {
        long property = 0;
        property = assembleCirclePropFirstBit(property, config, circleAreaItem);
        property = assembleCirclePropSecondBit(property, config, circleAreaItem, isProtocol2019);
        property = assembleAreaProperty(property, config);
        circleAreaItem.setCircleAreaProperty(property);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param property       ?????????
     * @param config         ??????????????????
     * @param circleAreaItem ????????????
     * @return
     */
    private static long assembleCirclePropFirstBit(long property, FenceConfig config, CircleAreaItem circleAreaItem) {
        Date startTime = config.getAlarmStartTime();
        Date endTime = config.getAlarmEndTime();
        if (startTime != null && endTime != null) {
            circleAreaItem.setStartTime(Converter.getBcdDate(startTime));
            circleAreaItem.setEndTime(Converter.getBcdDate(endTime));
            property = getValueByPositionDefinition(property, RegionAttributePositionDefinition.ACCORDING_TIME);
        }
        return property;
    }



    /**
     * ??????????????????????????????????????????????????????????????????????????????
     *
     * @param property       ?????????
     * @param config         ??????????????????
     * @param circleAreaItem ????????????
     * @param isProtocol2019 ??????2019??????
     * @return
     */
    private static long assembleCirclePropSecondBit(long property, FenceConfig config, CircleAreaItem circleAreaItem,
                                                    boolean isProtocol2019) {
        if (isProtocol2019) {
            if (enableSpeedAndOverSpeedAndNightSpeed(config)) {
                circleAreaItem.setMaxSpeed(config.getSpeed());
                circleAreaItem.setOverSpeedLastTime(Optional.ofNullable(config.getOverSpeedLastTime()).orElse(10));
                //2019???????????????????????????
                circleAreaItem.setNightMaxSpeed(config.getNightMaxSpeed());
                property = getValueByPositionDefinition(property,
                    RegionAttributePositionDefinition.MAX_AND_NIGNT_SPEED_AND_OVER_SPEED_TIME);
            }
        } else {
            if (config.getSpeed() != null) {
                circleAreaItem.setMaxSpeed(config.getSpeed());
                circleAreaItem.setOverSpeedLastTime(Optional.ofNullable(config.getOverSpeedLastTime()).orElse(10));
                property = getValueByPositionDefinition(property, RegionAttributePositionDefinition.LIMIT_SPEED);
            }
        }
        return property;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     *
     * @param property          ?????????
     * @param config            ??????????????????
     * @param rectangleAreaItem ????????????
     * @param isProtocol2019    ??????2019??????
     * @return
     */
    private static long assembleRectanglePropSecondBit(long property, FenceConfig config,
                                                       RectangleAreaItem rectangleAreaItem,
                                                       boolean isProtocol2019) {
        if (isProtocol2019) {
            if (enableSpeedAndOverSpeedAndNightSpeed(config)) {
                rectangleAreaItem.setMaxSpeed(config.getSpeed());
                rectangleAreaItem.setOverSpeedLastTime(Optional.ofNullable(config.getOverSpeedLastTime()).orElse(10));
                //2019???????????????????????????
                rectangleAreaItem.setNightMaxSpeed(config.getNightMaxSpeed());
                property = getValueByPositionDefinition(property,
                    RegionAttributePositionDefinition.MAX_AND_NIGNT_SPEED_AND_OVER_SPEED_TIME);
            }
        } else {
            if (config.getSpeed() != null) {
                rectangleAreaItem.setMaxSpeed(config.getSpeed());
                rectangleAreaItem.setOverSpeedLastTime(Optional.ofNullable(config.getOverSpeedLastTime()).orElse(10));
                property = getValueByPositionDefinition(property, RegionAttributePositionDefinition.LIMIT_SPEED);
            }
        }
        return property;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????
     *
     * @param property       ?????????
     * @param config         ??????????????????
     * @param polygon        ???????????????
     * @param isProtocol2019 ??????2019??????
     * @return
     */
    public static long assemblePolygonPropSecondBit(long property, FenceConfig config,
                                                    T808Msg0x8604 polygon,
                                                    boolean isProtocol2019) {
        if (isProtocol2019) {
            if (enableSpeedAndOverSpeedAndNightSpeed(config)) {
                polygon.setMaxSpeed(config.getSpeed());
                polygon.setOverSpeedTime(Optional.ofNullable(config.getOverSpeedLastTime()).orElse(10));
                //2019???????????????????????????
                polygon.setNightMaxSpeed(config.getNightMaxSpeed());
                property = getValueByPositionDefinition(property,
                    RegionAttributePositionDefinition.MAX_AND_NIGNT_SPEED_AND_OVER_SPEED_TIME);
            }
        } else {
            if (config.getSpeed() != null) {
                polygon.setMaxSpeed(config.getSpeed());
                polygon.setOverSpeedTime(Optional.ofNullable(config.getOverSpeedLastTime()).orElse(10));
                property = getValueByPositionDefinition(property, RegionAttributePositionDefinition.LIMIT_SPEED);
            }
        }
        return property;
    }

    private static boolean enableSpeedAndOverSpeedAndNightSpeed(FenceConfig config) {
        return config.getSpeed() != null && config.getOverSpeedLastTime() != null
               && config.getNightMaxSpeed() != null;
    }

    public static List<RectangleAreaItem> mappingRectangles(List<Map<String, Object>> rectangles) {

        Integer sendDownId;

        Rectangle rectangle;
        FenceConfig config;
        ArrayList<RectangleAreaItem> rectangleAreaItems = new ArrayList<>();
        for (Map<String, Object> map : rectangles) {

            rectangle = (Rectangle) map.get("fence");
            config = (FenceConfig) map.get("config");

            if (rectangle == null || config == null) {
                continue;
            }
            RectangleAreaItem rectangleAreaItem = new RectangleAreaItem();

            Integer contrastNumber = CommonUtil.abs(rectangle.getId().replaceAll("-", "").hashCode());
            sendDownId = config.getSendDownId();
            if (sendDownId != null && sendDownId != 0 && contrastNumber.equals(sendDownId)) {
                rectangleAreaItem.setRectangleAreaId(sendDownId);
            } else {
                rectangleAreaItem.setRectangleAreaId(1);
                log.error("????????????ID???1???sendDownId: {}, contrastNumber: {}", sendDownId, contrastNumber);
            }
            // ??????????????????
            double[] leftLocation = getCoordinators(rectangle.getLeftLatitude(), rectangle.getLeftLongitude());
            rectangleAreaItem.setLeftTopLatitude(leftLocation[0]);
            rectangleAreaItem.setLeftTopLongitude(leftLocation[1]);
            // ??????????????????
            double[] rightLocation = getCoordinators(rectangle.getRightLatitude(), rectangle.getRightLongitude());
            rectangleAreaItem.setRightBottomLatitude(rightLocation[0]);
            rectangleAreaItem.setRightBottomLongitude(rightLocation[1]);

            rectangleAreaItem.setRectangleAreaName(rectangle.getName());
            rectangleAreaItem.setRectangleAreaNameLength(rectangle.getName().length());
            assembleRectangleProperties(config, rectangleAreaItem, (Boolean) map.get("isProtocol2019"));
            rectangleAreaItems.add(rectangleAreaItem);
        }
        return rectangleAreaItems;
    }

    /**
     * ??????????????????????????????
     *
     * @param config
     * @param rectangleAreaItem
     * @param isProtocol2019
     */
    private static void assembleRectangleProperties(FenceConfig config, RectangleAreaItem rectangleAreaItem,
                                                    boolean isProtocol2019) {
        long property = 0;
        property = assembleRectanglePropFirstBit(property, config, rectangleAreaItem);
        property = assembleRectanglePropSecondBit(property, config, rectangleAreaItem, isProtocol2019);
        property = assembleAreaProperty(property, config);
        rectangleAreaItem.setRectangleAreaProperty(property);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param property          ?????????
     * @param config            ????????????????????????
     * @param rectangleAreaItem ????????????
     * @return
     */
    private static long assembleRectanglePropFirstBit(long property, FenceConfig config,
                                                      RectangleAreaItem rectangleAreaItem) {
        Date startTime = config.getAlarmStartTime();
        Date endTime = config.getAlarmEndTime();
        if (startTime != null && endTime != null) {
            property = getValueByPositionDefinition(property, RegionAttributePositionDefinition.ACCORDING_TIME);
            rectangleAreaItem.setStartTime(Converter.getBcdDate(startTime));
            rectangleAreaItem.setEndTime(Converter.getBcdDate(endTime));
        }
        return property;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     *
     * @param property           ?????????
     * @param config             ????????????????????????
     * @param polygonDefenceInfo ???????????????
     * @return
     */
    public static long assemblePolygonPropFirstBit(FenceConfig config, long property,
                                                   T808Msg0x8604 polygonDefenceInfo) {
        Date startTime = config.getAlarmStartTime();
        Date endTime = config.getAlarmEndTime();
        if (startTime != null && endTime != null) {
            property = EntitiesMappingHelper.getValueByPositionDefinition(property,
                RegionAttributePositionDefinition.ACCORDING_TIME);
            polygonDefenceInfo.setStartTime(Converter.getBcdDate(startTime));
            polygonDefenceInfo.setEndTime(Converter.getBcdDate(endTime));
        }
        return property;
    }

    public static long assembleAreaProperty(long property, FenceConfig config) {
        Integer alarmIn = config.getAlarmInPlatform();
        Integer alarmOut = config.getAlarmOutPlatform();
        Integer alarmInDriver = config.getAlarmInDriver();
        Integer alarmOutDriver = config.getAlarmOutDriver();
        Integer openDoor = config.getOpenDoor();
        Integer communicationFlag = config.getCommunicationFlag();
        Integer gnssFlag = config.getGnssFlag();
        if (alarmIn != null && alarmIn == 1) {
            property = getValueByPositionDefinition(property,
                RegionAttributePositionDefinition.INTO_REGION_ALRAM_TO_PLATFORM);
        }
        if (alarmOut != null && alarmOut == 1) {
            property = getValueByPositionDefinition(property,
                RegionAttributePositionDefinition.OUT_REGION_ALARM_TO_PLATFORM);
        }
        if (alarmInDriver != null && alarmInDriver == 1) {
            property = getValueByPositionDefinition(property,
                RegionAttributePositionDefinition.INTO_REGION_ALRAM_TO_DRIVER);
        }
        if (alarmOutDriver != null && alarmOutDriver == 1) {
            property = getValueByPositionDefinition(property,
                RegionAttributePositionDefinition.OUT_REGION_ALARM_TO_DRIVER);
        }
        if (openDoor != null && openDoor == 1) {
            property = getValueByPositionDefinition(property, RegionAttributePositionDefinition.OPEN_DOOR);
        }
        if (communicationFlag != null && communicationFlag == 1) {
            property = getValueByPositionDefinition(property,
                RegionAttributePositionDefinition.IN_REGION_COMMUNICATION);
        }
        if (gnssFlag != null && gnssFlag == 1) {
            property = getValueByPositionDefinition(property,
                RegionAttributePositionDefinition.IN_REGION_NOT_COLLECT_GNSS);
        }
        return property;
    }

    public static List<PolygonNodeItem> mappingPolygons(List<Polygon> rectangles) {
        ArrayList<PolygonNodeItem> polygonAreaItems = new ArrayList<>();
        for (Polygon polygon : rectangles) {
            PolygonNodeItem polygonAreaItem = new PolygonNodeItem();
            // ??????
            double[] resultLocation = getCoordinators(polygon.getLatitude(), polygon.getLongitude());
            polygonAreaItem.setLatitude(resultLocation[0]);
            polygonAreaItem.setLongitude(resultLocation[1]);
            polygonAreaItems.add(polygonAreaItem);
        }
        return polygonAreaItems;
    }

    /**
     * ?????????????????????????????????????????????????????? <p> ???????????????????????????????????????????????????????????????????????????????????? ????????????2??????????????????????????????????????????
     *
     * @param seed:?????????
     * @author jiangxiaoqiang
     */
    public static long getValueByPositionDefinition(long seed, int position) {
        return seed | (long) Math.pow(2, position);
    }

    public String getJsonValue(String key, String json) {
        JSONObject parseObject = JSON.parseObject(json);
        return parseObject.get(key).toString();
    }

    public static double[] getCoordinators(double latitude, double longitude) {
        if (isCheckMode) {
            // ??????????????????????????????
            return new double[] {latitude, longitude};
        }
        // ??????
        return GpsDataTranslate.gcj_To_Gps84(latitude, longitude);
    }
}
