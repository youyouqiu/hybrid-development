/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.query.LineSegmentInfo;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.repository.modules.LineDao;
import com.zw.platform.repository.modules.PolygonDao;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.Converter;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.common.EntitiesMappingHelper;
import com.zw.ws.entity.defence.RouteAttributeDefinition;
import com.zw.ws.entity.defence.RoutePointAttributeDefinition;
import com.zw.ws.entity.line.LinePoints;
import com.zw.ws.entity.t808.location.defence.CircleAreaItem;
import com.zw.ws.entity.t808.location.defence.PolygonNodeItem;
import com.zw.ws.entity.t808.location.defence.RectangleAreaItem;
import com.zw.ws.entity.t808.location.defence.T8080x8602;
import com.zw.ws.entity.t808.location.defence.T8080x8606;
import com.zw.ws.entity.t808.location.defence.T808Msg0x8600;
import com.zw.ws.entity.t808.location.defence.T808Msg0x8604;
import com.zw.ws.entity.t808.location.defence.T808_0x8607;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class WsElectronicDefenceService {
    private static final Logger logger = LogManager.getLogger(WsElectronicDefenceService.class);

    @Autowired
    private LineDao lineDao;

    @Autowired
    private PolygonDao polygonDao;

    @Autowired
    LineService lineService;

    public void deleteDefenseInfo(int t808MessageType, VehicleInfo vehicleInfo, List<Integer> ids, Integer transNo) {
        try {
            deleteDefenceInfo(t808MessageType, vehicleInfo, ids, transNo);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????messageType???" + t808MessageType + ",???????????????" + JSON.toJSONString(vehicleInfo));
        }
    }

    public void sendPolygonInfoToDevice(List<Map<String, Object>> polygons, VehicleInfo vehicleInfo, Integer transNo) {
        try {
            sendPolygonAreaDefenceCommand(polygons, vehicleInfo, transNo);
        } catch (Exception e) {
            logger.error("????????????????????????????????????line:" + JSON.toJSONString(polygons) + ",????????????;" + JSON.toJSONString(vehicleInfo),
                e);
        }
    }

    public void sendRectangleInfoToDevice(int settingType, List<Map<String, Object>> rectangles,
        VehicleInfo vehicleInfo, Integer transNo) {
        try {
            List<RectangleAreaItem> rectangleAreaItem = EntitiesMappingHelper.mappingRectangles(rectangles);
            sendRectangleAreaDefenceCommand(settingType, rectangleAreaItem, vehicleInfo, transNo);
        } catch (Exception e) {
            logger.error("?????????????????????????????????line:" + JSON.toJSONString(rectangles) + ",????????????;" + JSON.toJSONString(vehicleInfo),
                e);
        }
    }

    public void sendCircleAreaInfoToDevice(int settingType, List<Map<String, Object>> circles, VehicleInfo vehicleInfo,
        Integer transNo) {
        try {
            List<CircleAreaItem> circleAreaItems = EntitiesMappingHelper.mappingCircles(circles);
            sendCircleAreaDefenceCommand(settingType, circleAreaItems, vehicleInfo, transNo);
        } catch (Exception e) {
            logger
                .error("?????????????????????????????????line:" + JSON.toJSONString(circles) + ",????????????;" + JSON.toJSONString(vehicleInfo), e);
        }
    }

    public void sendSingleLineInfoToDevice(List<Map<String, Object>> lines, VehicleInfo vehicleInfo, Integer transNo) {
        try {
            findPointAndSendCommand(lines, vehicleInfo, transNo);
        } catch (Exception e) {
            logger.error("?????????????????????????????????line:" + JSON.toJSONString(lines) + ",????????????;" + JSON.toJSONString(vehicleInfo), e);
        }
    }

    /**
     * ????????????????????? ????????????
     */
    private void sendPolygonAreaDefenceCommand(List<Map<String, Object>> polygons, VehicleInfo vehicleInfo,
        Integer transNo) {
        Polygon polygon;
        FenceConfig config;

        Integer sendDownId;

        for (Map<String, Object> map : polygons) {
            polygon = (Polygon) map.get("fence");
            config = (FenceConfig) map.get("config");
            if (polygon == null || config == null) {
                continue;
            }
            // settingType:????????????,0?????????????????? 1?????????????????? 2???????????????
            T808Msg0x8604 polygonDefenceInfo = new T808Msg0x8604();

            sendDownId = config.getSendDownId();
            if (sendDownId != null && sendDownId != 0) {
                polygonDefenceInfo.setAreaId(sendDownId);
            } else {
                polygonDefenceInfo.setAreaId(Integer.MAX_VALUE);
            }
            polygonDefenceInfo.setPolygonAreaName(polygon.getName());
            List<Polygon> polygonPoints = polygonDao.getPolygonById(polygon.getId());
            if (CollectionUtils.isNotEmpty(polygonPoints)) {
                List<PolygonNodeItem> polygonNodeItems = EntitiesMappingHelper.mappingPolygons(polygonPoints);
                polygonDefenceInfo.setTopSum(polygonNodeItems.size());
                // ????????????
                polygonDefenceInfo.setPackageVertexCount(polygonNodeItems.size());
                polygonDefenceInfo.setMaxSpeed(config.getSpeed());
                if (config.getSpeed() != null) {
                    polygonDefenceInfo.setMaxSpeed(config.getSpeed());
                    config.setOverSpeedLastTime(Optional.ofNullable(config.getOverSpeedLastTime()).orElse(10));
                }

                polygonDefenceInfo.setAreaParam(
                    assemblePolygonProperties(config, polygonDefenceInfo, (Boolean) map.get("isProtocol2019")));
                polygonDefenceInfo.getTopItems().addAll(polygonNodeItems);
                //??????????????????????????????????????????
                sendPolygon(vehicleInfo, transNo, polygonDefenceInfo);

            }
        }
    }

    private void sendPolygon(VehicleInfo vehicleInfo, Integer transNo, T808Msg0x8604 polygonDefenceInfo) {
        String deviceId = vehicleInfo.getDeviceId();
        String deviceTypeStr = vehicleInfo.getDeviceType();
        int deviceType = getDeviceType(deviceTypeStr);
        if (DeviceInfo.DEVICE_TYPE_2019 == deviceType) {
            polygonDefenceInfo.initParam2019();
        }
        // ??????????????????
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimcardNumber(), ConstantUtil.T808_SET_POLYGON_AREA, transNo,
                polygonDefenceInfo, deviceTypeStr);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_POLYGON_AREA, deviceId);
    }

    private long assemblePolygonProperties(FenceConfig config, T808Msg0x8604 polygon, boolean isProtocol2019) {
        long property = 0;
        property = EntitiesMappingHelper.assemblePolygonPropFirstBit(config, property, polygon);
        property = EntitiesMappingHelper.assemblePolygonPropSecondBit(property, config, polygon, isProtocol2019);
        property = EntitiesMappingHelper.assembleAreaProperty(property, config);
        return property;
    }

    /**
     * ????????????????????????
     */
    private void sendRectangleAreaDefenceCommand(int settingType, List<RectangleAreaItem> rectangleAreaItems,
        VehicleInfo vehicleInfo, Integer transNo) {
        // settingType:????????????,0?????????????????? 1?????????????????? 2???????????????
        T8080x8602 rectangleDefenceInfo = new T8080x8602();
        rectangleDefenceInfo.setSetParam(settingType);
        // ????????????
        rectangleDefenceInfo.setAreaSum(rectangleAreaItems.size());
        rectangleDefenceInfo.setAreaParams(rectangleAreaItems);
        String deviceId = vehicleInfo.getDeviceId();
        for (RectangleAreaItem areaItem : rectangleAreaItems) {
            areaItem.initParam2019();
        }
        // ??????????????????
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimcardNumber(), ConstantUtil.T808_SET_RECTANGLE_AREA, transNo,
                rectangleDefenceInfo, vehicleInfo.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_RECTANGLE_AREA, deviceId);
    }

    /**
     * ????????????????????????
     */
    private void sendCircleAreaDefenceCommand(int settingType, List<CircleAreaItem> circleAreaItems,
        VehicleInfo vehicleInfo, Integer transNo) {
        // settingType:????????????,0?????????????????? 1?????????????????? 2???????????????
        T808Msg0x8600 circleDefenceInfo = new T808Msg0x8600();
        circleDefenceInfo.setSetParam(settingType);
        // ??????????????????
        circleDefenceInfo.setAreaSum(circleAreaItems.size());
        circleDefenceInfo.getAreaParams().addAll(circleAreaItems);
        String deviceId = vehicleInfo.getDeviceId();
        for (CircleAreaItem circleAreaItem : circleAreaItems) {
            circleAreaItem.initParam2019();
        }
        // ??????????????????
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimcardNumber(), ConstantUtil.T808_SET_CIRCULAR_AREA, transNo,
                circleDefenceInfo, vehicleInfo.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_CIRCULAR_AREA, deviceId);
    }

    /**
     * ????????????????????????(?????????????????????0?????????ID????????????)
     */
    private void findPointAndSendCommand(List<Map<String, Object>> lines, VehicleInfo vehicleInfo, Integer transNo) {
        long property = 0;
        FenceConfig config;
        Line line;
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }
        for (Map<String, Object> map : lines) {
            line = (Line) map.get("fence");
            config = (FenceConfig) map.get("config");
            if (line == null || config == null) {
                continue;
            }
            int lineId;
            if (config.getSendDownId() != Integer.MIN_VALUE) {
                lineId = config.getSendDownId();
            } else {
                lineId = Math.abs(Integer.MAX_VALUE);
            }
            T8080x8606 defence = new T8080x8606();
            defence.setLineID(Math.abs(lineId));
            defence.setRouteName(line.getName());
            property = assembleLineProperties(property, config, defence);

            List<LineSegmentInfo> lineSegmentInfos = lineService.findSegmentContentByLid(line.getId());
            if (!lineSegmentInfos.isEmpty()) {
                //??????????????????
                assembleLinePoints(defence, lineSegmentInfos);
            } else {
                // ?????????????????????????????????
                assembleWholeLine(config, line, defence);
            }
            defence.setLineParam(property);
            //???????????????f3??????????????????
            sendLineParam(vehicleInfo, transNo, defence);
        }
    }

    private void sendLineParam(VehicleInfo vehicleInfo, Integer transNo, T8080x8606 defence) {
        String deviceId = vehicleInfo.getDeviceId();
        String deviceType = vehicleInfo.getDeviceType();

        // ??????????????????
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimcardNumber(), ConstantUtil.T808_SET_LINE, transNo, defence, deviceType);
        if (DeviceInfo.isProtocol2019(deviceType)) {
            defence.initParam2019();
            //vehicleInfo.setT809PlatId(connectionParamsConfigDao.getTransPlatIdByVehicleId(vehicleInfo.getId()));
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_LINE, vehicleInfo);
        } else {
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_LINE, deviceId);
        }

    }

    private int getDeviceType(String deviceType) {
        int deviceTypeVal = DeviceInfo.DEVICE_TYPE_2013;
        //?????????????????????11????????????2019???????????????
        if ("11".equals(deviceType)) {
            deviceTypeVal = DeviceInfo.DEVICE_TYPE_2019;
        }
        return deviceTypeVal;
    }

    private void assembleWholeLine(FenceConfig config, Line line, T8080x8606 defence) {
        List<LineContent> lineContentList = getLineContents(line);
        defence.setSwervePortSum(lineContentList.size());
        // ????????????
        defence.setPackagePointsCount(lineContentList.size());
        int pointCount = 0;
        for (LineContent lineContent : lineContentList) {
            pointCount++;
            long linePointsProperty = 0;
            LinePoints linePoints = new LinePoints();
            linePoints.setLineId(1);
            linePoints.setLinePointId(pointCount);
            linePoints.setWidth(line.getWidth());
            // ??????
            double[] resultLocation =
                EntitiesMappingHelper.getCoordinators(lineContent.getLatitude(), lineContent.getLongitude());
            linePoints.setLongitude(resultLocation[1]);
            linePoints.setLatitude(resultLocation[0]);
            if (config.getSpeed() != null) {
                linePoints.setMaxSpeed(config.getSpeed());
                if (config.getOverSpeedLastTime() != null) {
                    linePoints.setOverSpeedLastTime(config.getOverSpeedLastTime());
                } else {
                    linePoints.setOverSpeedLastTime(10);
                }
                linePointsProperty = EntitiesMappingHelper
                    .getValueByPositionDefinition(linePointsProperty, RoutePointAttributeDefinition.SPEED_LIMIT);
            }
            // ????????????
            if (config.getTravelLongTime() != null && config.getTravelSmallTime() != null) {
                linePoints.setRunTimeMax(config.getTravelLongTime());
                linePoints.setRunTimeMin(config.getTravelSmallTime());
                linePointsProperty = EntitiesMappingHelper
                    .getValueByPositionDefinition(linePointsProperty, RoutePointAttributeDefinition.TRAVEL_TIME);
            }
            // ???????????????????????????km/h?????????????????????1??????0??????????????????
            linePoints.setAttribute(linePointsProperty);
            //2019????????????
            linePoints.setNightMaxSpeed(config.getNightMaxSpeed());
            defence.getSwervePortParams().add(linePoints);
        }
    }

    private List<LineContent> getLineContents(Line line) {
        List<LineContent> lineContentList = Collections.emptyList();
        try {
            lineContentList = lineDao.findLineContentById(line.getId());
        } catch (Exception e) {
            logger.error("findPonitAndSendCommand??????" + e);
        }
        return lineContentList;
    }

    private void assembleLinePoints(T8080x8606 defence, List<LineSegmentInfo> lineSegmentInfos) {
        int rcount = 0;
        int segmentId = 0;
        // ???????????????????????????
        for (LineSegmentInfo segmentInfo : lineSegmentInfos) {
            String[] segmentLons = segmentInfo.getLongitude().split(",");
            String[] segmentLats = segmentInfo.getLatitude().split(",");
            segmentId++;
            for (int i = 0; i < segmentLons.length; i++) {
                rcount++;
                long linePointsProperty = 0;
                LinePoints linePoints = new LinePoints();
                // ??????
                double[] resultLocation = EntitiesMappingHelper
                    .getCoordinators(Double.parseDouble(segmentLats[i]), Double.parseDouble(segmentLons[i]));
                linePoints.setLongitude(resultLocation[1]);
                linePoints.setLatitude(resultLocation[0]);
                linePoints.setLineId(segmentId);
                linePoints.setLinePointId(rcount);
                linePoints.setWidth(Integer.valueOf(segmentInfo.getOffset()));

                if (segmentInfo.getMaximumSpeed() != null) {
                    linePoints.setMaxSpeed(Integer.valueOf(segmentInfo.getMaximumSpeed()));
                    if (segmentInfo.getOverspeedTime() != null) {
                        linePoints.setOverSpeedLastTime(Integer.valueOf(segmentInfo.getOverspeedTime()));
                    } else {
                        linePoints.setOverSpeedLastTime(10);
                    }
                    linePointsProperty = EntitiesMappingHelper
                        .getValueByPositionDefinition(linePointsProperty, RoutePointAttributeDefinition.SPEED_LIMIT);
                }
                // ????????????
                if (StringUtils.isNotBlank(segmentInfo.getOverlengthThreshold()) && StringUtils
                    .isNotBlank(segmentInfo.getShortageThreshold()) && !Objects
                    .equals("0", segmentInfo.getOverlengthThreshold()) && !Objects
                    .equals("0", segmentInfo.getShortageThreshold())) {
                    linePoints.setRunTimeMax(Integer.valueOf(segmentInfo.getOverlengthThreshold()));
                    linePoints.setRunTimeMin(Integer.valueOf(segmentInfo.getShortageThreshold()));
                    linePointsProperty = EntitiesMappingHelper
                        .getValueByPositionDefinition(linePointsProperty, RoutePointAttributeDefinition.TRAVEL_TIME);
                }
                //???????????????????????????km/h?????????????????????1??????0??????????????????
                linePoints.setAttribute(linePointsProperty);
                //2019??????????????????????????????
                linePoints.setNightMaxSpeed(segmentInfo.getNightMaxSpeed());
                defence.getSwervePortParams().add(linePoints);
            }
        }
        defence.setSwervePortSum(rcount);
        // ????????????
        defence.setPackagePointsCount(rcount);
    }

    private long assembleLineProperties(long property, FenceConfig config, T8080x8606 defence) {
        Date startTime = config.getAlarmStartTime();
        Date endTime = config.getAlarmEndTime();
        Integer alarmIn = config.getAlarmInPlatform();
        Integer alarmOut = config.getAlarmOutPlatform();
        Integer alarmInDriver = config.getAlarmInDriver();
        Integer alarmOutDriver = config.getAlarmOutDriver();
        if (startTime != null && endTime != null) {
            property =
                EntitiesMappingHelper.getValueByPositionDefinition(property, RouteAttributeDefinition.ACCORD_TIME);
            defence.setStartTime(Converter.getBcdDate(startTime));
            defence.setEndTime(Converter.getBcdDate(endTime));
        }
        if (alarmIn != null && alarmIn == 1) {
            property = EntitiesMappingHelper
                .getValueByPositionDefinition(property, RouteAttributeDefinition.IN_ROUTE_ALARM_TO_PLATFORM);
        }
        if (alarmOut != null && alarmOut == 1) {
            property = EntitiesMappingHelper
                .getValueByPositionDefinition(property, RouteAttributeDefinition.OUT_ROUTE_ALARM_TO_PLATFORM);
        }
        if (alarmInDriver != null && alarmInDriver == 1) {
            property = EntitiesMappingHelper
                .getValueByPositionDefinition(property, RouteAttributeDefinition.IN_ROUTE_ALARM_TO_DRIVER);
        }
        if (alarmOutDriver != null && alarmOutDriver == 1) {
            property = EntitiesMappingHelper
                .getValueByPositionDefinition(property, RouteAttributeDefinition.OUT_ROUTE_ALARM_TO_DRIVER);
        }
        return property;
    }

    /**
     * ??????????????????
     */
    private void deleteDefenceInfo(int t808MessageType, VehicleInfo vehicleInfo, List<Integer> ids, Integer transNo) {
        T808_0x8607 deleteDefenceCommand = new T808_0x8607();
        deleteDefenceCommand.setAreaSum(ids.size());
        List<String> sendDownIds = deleteDefenceCommand.getAreaIDs();
        for (Integer sourceId : ids) {
            if (sourceId != null) {
                sendDownIds.add(sourceId.toString());
            }
        }
        String deviceId = vehicleInfo.getDeviceId();
        // ??????????????????
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimcardNumber(), t808MessageType, transNo, deleteDefenceCommand, vehicleInfo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, t808MessageType, deviceId);
    }
}