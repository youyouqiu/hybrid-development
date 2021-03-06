package com.zw.platform.service.functionconfig.impl;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.functionconfig.Administration;
import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.ManageFenceInfo;
import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.domain.functionconfig.TravelLine;
import com.zw.platform.domain.functionconfig.form.AdministrationForm;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.FenceConfigForm;
import com.zw.platform.domain.functionconfig.form.GpsLine;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.LinePassPointForm;
import com.zw.platform.domain.functionconfig.form.LineSegmentContentForm;
import com.zw.platform.domain.functionconfig.form.LineSegmentForm;
import com.zw.platform.domain.functionconfig.form.LineSpotForm;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.functionconfig.form.RectangleForm;
import com.zw.platform.domain.functionconfig.form.SegmentContentForm;
import com.zw.platform.domain.functionconfig.form.TravelLineForm;
import com.zw.platform.domain.functionconfig.query.ManageFenceQuery;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.AdministrationDao;
import com.zw.platform.repository.modules.CircleDao;
import com.zw.platform.repository.modules.FenceConfigDao;
import com.zw.platform.repository.modules.FenceDao;
import com.zw.platform.repository.modules.LineDao;
import com.zw.platform.repository.modules.ManageFenceDao;
import com.zw.platform.repository.modules.MarkDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.modules.PolygonDao;
import com.zw.platform.repository.modules.RectangleDao;
import com.zw.platform.repository.modules.TravelLineDao;
import com.zw.platform.service.functionconfig.AdministrationService;
import com.zw.platform.service.functionconfig.ManageFenceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.common.GpsDataTranslate;
import com.zw.ws.entity.defence.RouteAttributeDefinition;
import com.zw.ws.entity.line.LinePoints;
import com.zw.ws.entity.t808.location.defence.T8080x8606;
import com.zw.ws.entity.t808.parameter.T808MessageType;
import com.zw.ws.impl.WsElectronicDefenceService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Administrator on 2016/8/4.
 */
@Service
public class ManageFenceServiceImpl implements ManageFenceService {

    private static final Logger log = LogManager.getLogger(ManageFenceServiceImpl.class);

    @Autowired
    private LineDao lineDao;

    @Autowired
    private MarkDao markDao;

    @Autowired
    private CircleDao circleDao;

    @Autowired
    private RectangleDao rectangleDao;

    @Autowired
    private AdministrationDao administrationDao;

    @Autowired
    private PolygonDao polygonDao;

    @Autowired
    private ManageFenceDao manageFenceDao;

    @Autowired
    private TravelLineDao travelDao;

    @Autowired
    private FenceConfigDao fenceConfigDao;

    @Autowired
    private FenceDao fenceDao;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private WsElectronicDefenceService wsElectronicDefenceService;

    @Autowired
    private UserService userService;

    @Resource
    private LogSearchService logSearchService;

    @Resource
    private AdministrationService administrationService;

    @Value("${imp.coordinate.format.error}")
    private String impCoordinateFormatError;

    @Value("${fence.relieve}")
    private String fenceRelieve;

    @Override
    public JsonResultBean add(LineForm form, String ipAddress) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ????????????
        // ????????????
        String orgId = userService.getCurrentUserOrg().getUuid(); // ?????????????????????id
        form.setGroupId(orgId);

        //???????????????
        if (addLineContent(form)) {
            // ??????????????????
            boolean flag = lineDao.add(form);
            if (flag) {
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType("zw_m_line");
                boolean fenceFlag = lineDao.fenceInfo(fenceForm);
                if (fenceFlag) {
                    String msg = "???????????????????????? : " + form.getName() + "(" + form.getType() + ")";
                    logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean updateLine(LineForm form, String ipAddress) {
        String afterType = form.getType();// ??????????????????
        String afterName = form.getName();// ??????????????????
        LineForm lineForm = getLineForm(form.getLineId());
        List<LineForm> list = findLineByName(form.getName()); // ??????????????????????????????
        String[] before = findType(form.getLineId());// ?????????????????????????????????
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        // ????????????id?????????????????????-???????????????????????????????????????
        boolean flag = lineDao.deleteLineContent(form.getLineId());
        // step2:???????????????????????????????????????????????????
        form.setId(form.getLineId());
        boolean flag1 = addLineContent(form);
        // step3:????????????????????????
        boolean flag2 = lineDao.updateLine(form);
        if (flag && flag1 && flag2) {
            String msg = "";
            if (lineForm.getName().equals(form.getName())) { // ???????????????????????????(?????????????????????)
                String beforeType = before[0];// ??????????????????
                String beforeName = before[1];// ??????????????????
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg = "???????????? : " + afterName + "(" + afterType + ")";
                } else {
                    msg =
                        "???????????? :  " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType + ")";
                }
            } else if (list.size() == 0) {
                String beforeType = before[0];// ??????????????????
                String beforeName = before[1];// ??????????????????
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg += "???????????? : " + afterName + "(" + afterType + ")";
                } else {
                    msg +=
                        "???????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType + ")";
                }
            }
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            ZMQFencePub.pubChangeFence("8");// ????????????????????????
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private boolean addLineContent(LineForm form) {
        String lineSport = form.getPointSeqs(); // ???
        String longitudes = form.getLongitudes(); // ??????
        String latitudes = form.getLatitudes(); // ??????
        if (lineSport == null || longitudes == null || latitudes == null) {
            return false;
        }
        List<LineForm> lineContentList = new ArrayList<>();
        if (lineSport != null && longitudes != null && latitudes != null) {
            String[] pointSeqArray = lineSport.split(",");
            String[] longitudeArray = longitudes.split(",");
            String[] latitudeArray = latitudes.split(",");
            if (pointSeqArray.length > 0 && longitudeArray.length > 0 && latitudeArray.length > 0) {
                for (int i = 0; i < pointSeqArray.length; i++) {
                    LineForm lineForm = new LineForm();
                    lineForm.setLineId(form.getId());
                    lineForm.setPointType(form.getPointType());
                    lineForm.setPointSeq(pointSeqArray[i]);
                    lineForm.setLongitude(longitudeArray[i]);
                    lineForm.setLatitude(latitudeArray[i]);
                    lineForm.setSegmentId(i);
                    lineForm.setCreateDataUsername(form.getCreateDataUsername());
                    lineContentList.add(lineForm);
                }
            }
        }
        return lineDao.addLineContentBatch(lineContentList);
    }

    @Override
    public JsonResultBean addMarker(MarkForm form, String ipAddress) {
        List<MarkForm> list = findMarkByName(form.getName());
        if (list.size() == 0) {
            String userName = SystemHelper.getCurrentUsername();
            form.setCreateDataUsername(userName);
            // ????????????
            String orgId = userService.getCurrentUserOrg().getUuid();
            form.setGroupId(orgId);
            boolean flag = markDao.marker(form);
            if (flag) {
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType("zw_m_marker");
                markDao.fenceInfo(fenceForm);
                String afterName = form.getName();
                String afterType = form.getType();
                String msg = "??????????????? : " + afterName + "(" + afterType + ")";
                logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    @Override
    public JsonResultBean updateMarker(MarkForm form, String ipAddress) {
        MarkForm markForm = getMarkForm(form.getMarkerId());
        List<MarkForm> list = findMarkByName(form.getName()); // ????????????????????????????????????
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
        boolean flag = markDao.updateMarker(form);
        if (flag) {
            String[] before = findType(form.getMarkerId()); // ?????????????????????????????????
            String afterType = form.getType(); // ??????????????????
            String afterName = form.getName(); // ??????????????????
            String beforeType = before[0];// ??????????????????
            String beforeName = before[1];// ??????????????????
            String msg = "";
            if (markForm.getName().equals(form.getName())) {
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg = "??????????????? : " + afterName + "(" + afterType + ")";
                } else {
                    msg =
                        "??????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType + ")";
                }
            } else if (list.size() == 0) {
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg = "??????????????? : " + afterName + "(" + afterType + ")";
                } else {
                    msg =
                        "??????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType + ")";
                }
            }
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);

    }

    private boolean isTypeAndNameChanged(String afterType, String afterName, String beforeType, String beforeName) {
        return beforeType.equals(afterType) && beforeName.equals(afterName);
    }

    @Override
    public JsonResultBean addCircles(CircleForm form, String ipAddress) {
        List<CircleForm> list = findCircleByName(form.getName());
        if (list.size() == 0) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // ????????????
            String orgId = userService.getCurrentUserOrg().getUuid();
            form.setGroupId(orgId);
            boolean flag = circleDao.circles(form);
            if (flag) {
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType("zw_m_circle");
                circleDao.fenceInfo(fenceForm);
                String afterName = form.getName();
                String afterType = form.getType();
                String msg = "???????????????????????? : " + afterName + "(" + afterType + ")";
                logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean updateCircle(CircleForm form, String ipAddress) {
        CircleForm circleForm = getCircleForm(form.getCircleId());
        List<CircleForm> list = findCircleByName(form.getName());
        String[] before = findType(form.getCircleId());// ?????????????????????????????????
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = circleDao.updateCircle(form);
        if (flag) {
            String beforeType = before[0];// ??????????????????
            String beforeName = before[1];// ??????????????????
            String afterType = form.getType();
            String afterName = form.getName();
            String msg = "";
            if (circleForm.getName().equals(form.getName())) {
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg += "???????????????????????? : " + afterName + "(" + afterType + ")";
                } else {
                    msg += "???????????????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType
                        + ")";
                }
            } else if (list.size() == 0) {
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg += "???????????????????????? :" + afterName + "(" + afterType + ")";
                } else {
                    msg += "???????????????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType
                        + ")";
                }
            }
            logSearchService.addLog(ipAddress, msg, "3", ",", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);

    }

    @Override
    public JsonResultBean addRectangles(RectangleForm form, String ipAddress) {
        List<RectangleForm> list = findRectangleByName(form.getName());
        if (list.size() == 0) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // ????????????
            String orgId = userService.getCurrentUserOrg().getUuid();
            form.setGroupId(orgId);
            String pointCoordinate = form.getPointSeqs(); // ?????????????????????????????????????????????
            String longitudes = form.getLongitudes(); // ??????
            String latitudes = form.getLatitudes(); // ??????
            boolean flag = false;
            if (pointCoordinate != null && longitudes != null && latitudes != null) {
                String[] pointSeqsArray = pointCoordinate.split(",");
                String[] longitudesArray = longitudes.split(",");
                String[] latitudesArray = latitudes.split(",");
                if (pointSeqsArray.length > 0 && longitudesArray.length > 0 && latitudesArray.length > 0) {
                    form.setLeftLongitude(Double.parseDouble(longitudesArray[0]));
                    form.setLeftLatitude(Double.parseDouble(latitudesArray[0]));
                    form.setRightLongitude(Double.parseDouble(longitudesArray[2]));
                    form.setRightLatitude(Double.parseDouble(latitudesArray[2]));
                    // ??????????????????
                    flag = rectangleDao.rectangles(form);
                }
            }
            if (flag) { // ??????????????????????????????????????????????????????????????????
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType("zw_m_rectangle");
                boolean infoFlag = rectangleDao.fenceInfo(fenceForm);
                if (infoFlag) {
                    String msg = "???????????????????????? : " + form.getName() + "(" + form.getType() + ")";
                    logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);

    }

    @Override
    public JsonResultBean updateRectangle(RectangleForm form, String ipAddress) {
        RectangleForm rectangleForm = getRectangleForm(form.getRectangleId()); // ??????id??????????????????????????????
        List<RectangleForm> list = findRectangleByName(form.getName());
        String[] before = findType(form.getRectangleId());// ?????????????????????????????????
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        String[] pointSeqs = form.getPointSeqs().split(",");
        String[] longitudes = form.getLongitudes().split(",");
        String[] latitudes = form.getLatitudes().split(",");
        if (pointSeqs.length > 0 && longitudes.length > 0 && latitudes.length > 0) {
            form.setLeftLongitude(Double.parseDouble(longitudes[0]));
            form.setLeftLatitude(Double.parseDouble(latitudes[0]));
            form.setRightLongitude(Double.parseDouble(longitudes[2]));
            form.setRightLatitude(Double.parseDouble(latitudes[2]));
        }
        boolean flag = rectangleDao.updateRectangle(form);
        if (flag) {
            ZMQFencePub.pubChangeFence("5");
            String beforeType = before[0];// ??????????????????
            String beforeName = before[1];// ??????????????????
            String afterType = form.getType();// ??????????????????
            String afterName = form.getName();// ??????????????????
            String msg = "";
            if (rectangleForm.getName().equals(form.getName())) {
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg = "???????????????????????? : " + afterName + "(" + afterType + ")";
                } else {
                    msg += "???????????????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType
                        + ")";
                }
            } else if (list.size() == 0) {
                if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                    msg += "???????????????????????? : " + afterName + "(" + afterType + ")";
                } else {
                    msg += "???????????????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType
                        + ")";
                }
            }
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ???????????????
     */
    @Override
    public JsonResultBean addPolygons(PolygonForm form, String ipAddress) {
        List<PolygonForm> list = findPolygonByName(form.getName());
        if (list.size() == 0) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // ????????????
            String orgId = userService.getCurrentUserOrg().getUuid();
            form.setGroupId(orgId);
            String pointSeqs = form.getPointSeqs();
            String longitudes = form.getLongitudes();
            String latitudes = form.getLatitudes();
            boolean flag = false;
            if (pointSeqs != null && longitudes != null && latitudes != null) {
                String[] pointSeqsArray = pointSeqs.split(",");
                String[] longitudesArray = longitudes.split(",");
                String[] latitudesArray = latitudes.split(",");
                if (pointSeqsArray.length > 0 && longitudesArray.length > 0 && latitudesArray.length > 0) {
                    for (int i = 0; i < pointSeqsArray.length; i++) {
                        form.setPointSeq(pointSeqsArray[i]);
                        form.setLongitude(longitudesArray[i]);
                        form.setLatitude(latitudesArray[i]);
                        flag = polygonDao.addPolygonsContent(form); // ???????????????
                    }
                }
            }
            if (flag) {
                // ??????????????????
                polygonDao.addPolygons(form);
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType("zw_m_polygon");
                boolean fenceFlag = polygonDao.addFenceInfo(fenceForm);
                if (fenceFlag) {
                    String msg = "??????????????????????????????" + form.getName() + "(" + form.getType() + ")";
                    logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ???????????????????????????
     */
    @Override
    public JsonResultBean updatePolygon(PolygonForm form, String ipAddress) {
        PolygonForm polygonForm = getPolygonForm(form.getPolygonId());
        List<PolygonForm> list = findPolygonByName(form.getName());
        String[] before = findType(form.getPolygonId());// ?????????????????????????????????
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        // step1:??????????????????id???????????????????????????????????????
        boolean deFlag = polygonDao.deletePolygonContent(form.getPolygonId());
        boolean addFlag = false;
        if (deFlag) {
            String pointSeqs = form.getPointSeqs();
            String longitudes = form.getLongitudes();
            String latitudes = form.getLatitudes();
            if (pointSeqs != null && longitudes != null && latitudes != null) {
                // step2:??????????????????????????????????????????????????????
                String[] pointSeqsArray = form.getPointSeqs().split(",");
                String[] longitudesArray = form.getLongitudes().split(",");
                String[] latitudesArray = form.getLatitudes().split(",");
                if (pointSeqsArray.length > 0 && longitudesArray.length > 0 && latitudesArray.length > 0) {
                    for (int i = 0; i < pointSeqsArray.length; i++) {
                        form.setPointSeq(pointSeqsArray[i]);
                        form.setLongitude(longitudesArray[i]);
                        form.setLatitude(latitudesArray[i]);
                        addFlag = polygonDao.updatePolygonContent(form);
                    }
                }
            }
        }
        if (addFlag) {
            // step3:???????????????????????????
            boolean infoFlag = polygonDao.updatePolygon(form);
            if (infoFlag) {
                String beforeType = before[0]; // ??????????????????
                String beforeName = before[1]; // ??????????????????
                String afterType = form.getType(); // ??????????????????
                String afterName = form.getName(); // ??????????????????
                String msg = "";
                if (polygonForm.getName().equals(form.getName())) {
                    if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                        msg = "??????????????????????????? : " + afterName + "(" + afterType + ")";
                    } else {
                        msg =
                            "??????????????????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType
                                + ")";
                    }

                } else if (list.size() == 0) {
                    if (isTypeAndNameChanged(afterType, afterName, beforeType, beforeName)) {
                        msg = "??????????????????????????? : " + afterName + "(" + afterType + ")";
                    } else {
                        msg =
                            "??????????????????????????? : " + beforeName + "(" + beforeType + ")" + " ????????? " + afterName + "(" + afterType
                                + ")";
                    }
                }
                logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                ZMQFencePub.pubChangeFence("6");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);

    }

    @MethodLog(name = "???????????? User", description = "???????????? User")
    @Override
    public Page<ManageFenceInfo> findByPage(ManageFenceQuery query, String simpleQueryParam) {
        return PageHelperUtil.doSelect(query, () -> manageFenceDao.find(query, simpleQueryParam));
    }

    @Override
    public JsonResultBean delete(String id, String ipAddress) {
        String[] item = id.split(",");
        StringBuilder message = new StringBuilder();
        for (String shapeId : item) {
            if (select(shapeId) == 0) { // ???????????????????????????
                ManageFenceInfo manageFenceInfo = manageFenceDao.gettable(id); // ??????????????????
                String tableName = manageFenceInfo.getType();
                String[] fence = findType(id); // ????????????
                boolean flag = manageFenceDao.delete(id, tableName); // ????????????????????????
                if (flag) {
                    switch (tableName) {
                        case "zw_m_line": // ???
                            manageFenceDao.deleteSpotbyline(id);
                            message.append("???????????????????????? : ").append(fence[1]).append(" (").append(fence[0]).append(")");
                            break;
                        case "zw_m_administration": // ????????????
                            administrationDao.deleteAdministrationContent(id);
                            message.append("?????????????????? : ").append(fence[1]).append(" (").append(fence[0]).append(")");
                            break;
                        case "zw_m_travel_line": // ????????????
                            travelDao.deleteTravelLineById(id); // ??????????????????
                            travelDao.deletePassPointById(id); // ?????????????????????
                            travelDao.deleteLineContentById(id); // ??????????????????????????????
                            message.append("?????????????????? : ").append(fence[1]).append(" (").append(fence[0]).append(")");
                            break;
                        case "zw_m_rectangle": // ??????
                            message.append("???????????????????????? : ").append(fence[1]).append(" (").append(fence[0]).append(")");
                            break;
                        case "zw_m_marker": // ?????????
                            message.append("???????????? : ").append(fence[1]).append(" (").append(fence[0]).append(")");
                            break;
                        case "zw_m_polygon": // ?????????
                            message.append("??????????????????????????? : ").append(fence[1]).append(" (").append(fence[0]).append(")");
                            break;
                        case "zw_m_circle": // ??????
                            message.append("???????????????????????? : ").append(fence[1]).append(" (").append(fence[0]).append(")");
                            break;
                        default:
                            break;
                    }
                }
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, fenceRelieve);
            }
        }
        if (!message.toString().isEmpty()) {
            if (item.length == 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "????????????????????????");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public int select(String id) {
        return manageFenceDao.select(id);
    }

    @Override
    public void addMonitoringTag(LineSpotForm form) {
        lineDao.addMonitoringTag(form);
    }

    @Override
    public String[] findType(String id) {
        String fenceType = manageFenceDao.findType(id);
        // ??????
        if ("zw_m_circle".equals(fenceType)) {
            Circle circle = circleDao.getCircleById(id);
            if (circle != null) {
                return new String[] { circle.getType(), circle.getName(), fenceType };
            }
        }
        // ??????
        if ("zw_m_rectangle".equals(fenceType)) {
            Rectangle rectangle = rectangleDao.getRectangleByID(id);
            if (rectangle != null) {
                return new String[] { rectangle.getType(), rectangle.getName(), fenceType };
            }
        }
        // ?????????
        if ("zw_m_polygon".equals(fenceType)) {
            Polygon polygon = polygonDao.findPolygonById(id);
            if (polygon != null) {
                return new String[] { polygon.getType(), polygon.getName(), fenceType };
            }
        }
        // ??????
        if ("zw_m_line".equals(fenceType)) {
            Line line = lineDao.findLineById(id);
            if (line != null) {
                return new String[] { line.getType(), line.getName(), fenceType };
            }
        }
        // ??????
        if ("zw_m_marker".equals(fenceType)) {
            Mark mark = markDao.findMarkById(id);
            if (mark != null) {
                return new String[] { mark.getType(), mark.getName(), fenceType };
            }
        }
        // ????????????
        if ("zw_m_travel_line".equals(fenceType)) {
            TravelLine travel = travelDao.findTravelLineById(id);
            if (travel != null) {
                return new String[] { travel.getLineType(), travel.getName(), fenceType };
            }
        }
        if ("zw_m_administration".equals(fenceType)) {
            Administration area = administrationDao.findAdministrationByIds(id);
            if (area != null) {
                return new String[] { area.getProvince() + "," + area.getCity() + "," + area.getDistrict(),
                    area.getName(), fenceType };
            }
        }
        return new String[] { "" };
    }

    @Override
    public JsonResultBean addSegment(LineSegmentContentForm form) {
        List<LineSegmentForm> lineSegmentForms = new ArrayList<>();
        List<SegmentContentForm> segmentContentForms = new ArrayList<>();
        assemblePersistData(form, lineSegmentForms, segmentContentForms);
        boolean flag1 = manageFenceDao.addSegment(lineSegmentForms);
        boolean flag2 = manageFenceDao.addSegmentContent(segmentContentForms);
        if (flag1 && flag2) {
            //????????????????????????????????????????????????
            ZMQFencePub.pubChangeFence("14");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private void assemblePersistData(LineSegmentContentForm form, List<LineSegmentForm> lineSegmentForms,
        List<SegmentContentForm> segmentContentForms) {
        assembelSegment(form, lineSegmentForms);
        assembleSegmentContent(form, lineSegmentForms, segmentContentForms);
    }

    private void assembleSegmentContent(LineSegmentContentForm form, List<LineSegmentForm> lineSegmentForms,
        List<SegmentContentForm> segmentContentForms) {
        String[] pos = form.getLongitude().substring(0, form.getLongitude().length() - 1).split("],");
        JSONArray jsonArray = new JSONArray();
        for (String po : pos) {
            jsonArray.add(po.split(","));
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            for (int j = 0; j < jsonArray.getJSONArray(i).size(); j++) {
                String[] posx = jsonArray.getJSONArray(i).getString(j).split(";");
                SegmentContentForm segmentContentForm = new SegmentContentForm();
                segmentContentForm.setLongitude(posx[0]);
                segmentContentForm.setLatitude(posx[1]);
                segmentContentForm.setLineSegmentId(lineSegmentForms.get(i).getId());
                segmentContentForm.setSortOrder(j);
                segmentContentForms.add(segmentContentForm);
            }
        }
    }

    private void assembelSegment(LineSegmentContentForm form, List<LineSegmentForm> lineSegmentForms) {
        String[] maxSpeeds = form.getMaximumSpeed().split(",");
        String[] overTimes = form.getOverspeedTime().split(",");
        String[] offsets = form.getOffset().split(",");
        String[] overlengthThresholds = form.getOverlengthThreshold().split(",");
        String[] shortageThresholds = form.getShortageThreshold().split(",");

        //3658??????
        String[] nightTopSpeeds = form.getNightMaxSpeed().split(",", -1);
        String[] nightLimitTimes = form.getNightLimitTime().split(",", -1);

        for (int i = 0; i < form.getSumn(); i++) {
            LineSegmentForm lineSegmentForm = new LineSegmentForm();
            lineSegmentForm.setLineId(form.getLineId());
            lineSegmentForm.setMaximumSpeed(Double.valueOf(maxSpeeds[i]));
            lineSegmentForm.setOverspeedTime(Integer.valueOf(overTimes[i]));
            lineSegmentForm.setOffset(Integer.valueOf(offsets[i]));
            lineSegmentForm.setSegmentSort(i);
            lineSegmentForm.setOverlengthThreshold(
                StringUtil.isNull(overlengthThresholds[i]) ? 0 : Integer.valueOf(overlengthThresholds[i]));
            lineSegmentForm.setShortageThreshold(
                StringUtil.isNull(shortageThresholds[i]) ? 0 : Integer.valueOf(shortageThresholds[i]));
            lineSegmentForm.setNightMaxSpeed(Integer.valueOf(nightTopSpeeds[i]));
            lineSegmentForm.setNightLimitTime(nightLimitTimes[i]);
            lineSegmentForms.add(lineSegmentForm);

        }
    }

    @Override
    public boolean addSegmentContent(List<SegmentContentForm> segmentContentForms) {
        return manageFenceDao.addSegmentContent(segmentContentForms);
    }

    @Override
    public boolean resetSegment(String lineId) {
        return manageFenceDao.resetSegment(lineId);
    }

    @Override
    public boolean unbundleSegment(String lineId) throws Exception {
        List<Map<String, Object>> maps = lineDao.findBindInfoByLid(lineId);
        for (Map<String, Object> map : maps) {
            String deviceNumber = (String) map.get("device_number");
            VehicleInfo vehicle = new VehicleInfo();
            ConvertUtils.register(vehicle, Date.class);
            BeanUtils.populate(vehicle, map); // map???bean
            List<Integer> ids = new ArrayList<>();
            ids.add(CommonUtil.abs(lineId.replaceAll("-", "").hashCode()));
            Integer msgSN = DeviceHelper.getRegisterDevice(vehicle.getId(), deviceNumber);
            if (msgSN != null) {
                wsElectronicDefenceService
                    .deleteDefenseInfo(T808MessageType.DELETE_ELECTRONIC_LINE_COMMAND, vehicle, ids, msgSN);
            }
        }
        return true;
    }

    @Override
    public List<MarkForm> findMarkByName(String name) {
        return manageFenceDao.findMarkByName(name);
    }

    @Override
    public List<LineForm> findLineByName(String name) {
        return manageFenceDao.findLineByName(name);
    }

    @Override
    public List<RectangleForm> findRectangleByName(String name) {
        return manageFenceDao.findRectangleByName(name);
    }

    @Override
    public List<CircleForm> findCircleByName(String name) {
        return manageFenceDao.findCircleByName(name);
    }

    @Override
    public List<PolygonForm> findPolygonByName(String name) {
        return manageFenceDao.findPolygonByName(name);
    }

    @Override
    public MarkForm getMarkForm(String id) {
        return manageFenceDao.getMarkForm(id);
    }

    @Override
    public LineForm getLineForm(String id) {
        return manageFenceDao.getLineForm(id);
    }

    @Override
    public RectangleForm getRectangleForm(String id) {
        return manageFenceDao.getRectangleForm(id);
    }

    @Override
    public CircleForm getCircleForm(String id) {
        return manageFenceDao.getCircleForm(id);
    }

    @Override
    public PolygonForm getPolygonForm(String id) {
        return manageFenceDao.getPolygonForm(id);
    }

    /**
     * ??????????????????
     */
    public JsonResultBean addAdministration(AdministrationForm form, String ipAddress) {
        List<Administration> administrationList = administrationService.findAdministrationByName(form.getName());
        if (administrationList.size() == 0) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // ????????????
            String orgId = userService.getCurrentUserOrg().getUuid();
            form.setGroupId(orgId);
            boolean flag = saveAdministration(form);
            if (flag) {
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType("zw_m_administration");
                boolean infoFlag = administrationDao.fenceInfo(fenceForm);
                if (infoFlag) {
                    // ??????????????????
                    String info = form.getProvince() + "," + form.getCity() + "," + form.getDistrict();
                    String msg = "?????????????????? : " + form.getName() + "(" + info + ")";
                    logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ????????????????????????????????????????????????
     */
    private boolean saveAdministration(AdministrationForm form) {
        if (form.getAdministrativeLngLat() == null) {
            return false;
        }
        String[] pointSeqs = form.getAdministrativeLngLat().split("-");
        List<AdministrationForm> list = new ArrayList<>();
        for (int i = 0; i < pointSeqs.length; i++) {
            String[] regionCount;
            regionCount = pointSeqs[i].split(",");
            for (int j = 0; j < regionCount.length; j++) {
                AdministrationForm administration = new AdministrationForm();
                administration.setId(form.getId());
                administration.setRegionCount(i);
                administration.setSortOrder(j);
                administration.setLongitude(regionCount[j]);
                administration.setLatitude(regionCount[j + 1]);
                list.add(administration);
                j = j + 1;
            }
        }
        boolean contentFlag = administrationDao.administrationContent(list);
        // ??????????????????
        boolean infoFlag = administrationDao.administration(form);
        return contentFlag && infoFlag;
    }

    @MethodLog(name = "????????????", description = "????????????")
    public Map addCoordinates(MultipartFile multipartFile, String name, String type, String excursion)
        throws Exception {
        int width = 1;// ?????????
        if (excursion != null) {
            width = Integer.parseInt(excursion);
        }
        Map<String, Object> resultMap = new HashMap<>();
        InputStream file = multipartFile.getInputStream();
        String coordinates = convertStreamToString(file);
        String[] fence;
        if (coordinates.equals("false")) {
            resultMap.put("flag", 0);
            resultMap.put("resultInfo", "");
            resultMap.put("errorMsg", impCoordinateFormatError);
        } else {
            fence = coordinates.split(",");
            if (type.equals("zw_m_line")) {
                LineForm lineform = new LineForm();
                lineform.setCreateDataUsername(SystemHelper.getCurrentUsername());
                lineform.setCreateDataTime(new Date());
                lineform.setType("??????");
                lineform.setName(name);
                lineform.setWidth(width);
                // ????????????
                String orgId = userService.getCurrentUserOrg().getUuid();
                lineform.setGroupId(orgId);
                for (int i = 0; i < fence.length; i++) {
                    lineform.setPointSeq(String.valueOf(i));
                    lineform.setLongitude(fence[i]);
                    lineform.setLatitude(fence[i + 1]);
                    lineDao.addLineContent(lineform);
                    i = i + 1;
                }
                // ??????????????????
                lineDao.add(lineform);
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(lineform.getId());
                fenceForm.setType("zw_m_line");
                lineDao.fenceInfo(fenceForm);
            } else if (type.equals("zw_m_polygon")) {
                PolygonForm polygonForm = new PolygonForm();
                polygonForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                polygonForm.setCreateDataTime(new Date());
                polygonForm.setCreateDataTime(new Date());
                polygonForm.setType("????????????");
                polygonForm.setName(name);
                // ????????????
                String orgId = userService.getCurrentUserOrg().getUuid();
                polygonForm.setGroupId(orgId);
                List<PolygonForm> list = new ArrayList<>();
                for (int i = 0; i < fence.length; i++) {
                    PolygonForm form = new PolygonForm();
                    form.setId(polygonForm.getId());
                    form.setSortOrder(i);
                    form.setLongitude(fence[i]);
                    form.setLatitude(fence[i + 1]);
                    i = i + 1;
                    list.add(form);
                }
                polygonDao.addMoreContent(list);
                polygonDao.addPolygons(polygonForm);
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(polygonForm.getId());
                fenceForm.setType("zw_m_polygon");
                polygonDao.addFenceInfo(fenceForm);
            }
            resultMap.put("flag", 1);
            resultMap.put("resultInfo", "????????????");
            resultMap.put("errorMsg", "");
        }
        return resultMap;

    }

    /**
     * ??????txt?????????????????????
     * @author yangyi
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        String reg = "([-+])?(180\\.0{4,7}|(\\d{1,2}|1([0-7]\\d))\\.\\d{4,20})"
            + "(,)([-+])?(90\\.0{4,8}|(\\d|[1-8]\\d)\\.\\d{4,20})";
        boolean flag = false;
        try {
            while ((line = reader.readLine()) != null) {
                flag = line.matches(reg);
                if (flag) {
                    String[] point = line.split(",");
                    double longitude = Double.valueOf(point[0]);
                    double latitude = Double.valueOf(point[1]);
                    double[] resultLocation = GpsDataTranslate.transform(latitude, longitude);

                    String str = resultLocation[1] + "," + resultLocation[0];
                    sb.append(str).append(",");
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (flag) {
            return sb.toString();
        } else {
            return "false";
        }
    }

    @Override
    public List<TravelLineForm> findTravelLineByName(String name) {
        return manageFenceDao.findTravelLineByName(name);
    }

    @Override
    public JsonResultBean addTravelLine(GpsLine form, String ipAddress) {
        List<TravelLineForm> travelLineFormList = findTravelLineByName(form.getName());
        if (travelLineFormList.size() == 0) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // ????????????
            String orgId = userService.getCurrentUserOrg().getUuid();
            form.setGroupId(orgId);
            // ???????????????????????????????????????
            String[] startToEndLng = form.getStartToEndLng().split(";");
            String[] startToEndLat = form.getStartToEndLat().split(";");
            form.setStartLongitude(Double.parseDouble(startToEndLng[0]));
            form.setStartLatitude(Double.parseDouble(startToEndLat[0]));
            form.setEndLongitude(Double.parseDouble(startToEndLng[1]));
            form.setEndLatitude(Double.parseDouble(startToEndLat[1]));
            boolean addFlag = travelDao.saveStartAndEndPoint(form); // ??????????????????
            boolean pointFlag = savePassDot(form); // ?????????
            boolean allFlag = saveAllPoint(form);
            if (addFlag && allFlag && pointFlag) {
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType("zw_m_travel_line");
                fenceForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                boolean infoFlag = travelDao.fenceInfo(fenceForm);
                if (infoFlag) {
                    String msg = "?????????????????? : " + form.getName() + "(" + form.getType() + ")";
                    logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ?????????????????????????????????
     * @return boolean
     */
    private boolean savePassDot(GpsLine form) {
        if (form.getWayPointLng() != null && form.getWayPointLat() != null && !form.getWayPointLat().isEmpty() && !form
            .getWayPointLng().isEmpty()) {
            // ?????????????????????
            String[] longitudes = form.getWayPointLng().split(";");
            String[] latitudes = form.getWayPointLat().split(";");
            List<LinePassPointForm> list = new ArrayList<>();
            if (!longitudes[0].equals("") && !latitudes[0].equals("")) {
                for (int i = 0; i < longitudes.length; i++) {
                    LinePassPointForm lineForm = new LinePassPointForm();
                    lineForm.setLineId(form.getId());
                    lineForm.setSortOrder(i);
                    lineForm.setLongitude(Double.parseDouble(longitudes[i]));
                    lineForm.setLatitude(Double.parseDouble(latitudes[i]));
                    lineForm.setCreateDataTime(form.getCreateDataTime());
                    lineForm.setCreateDataUsername(form.getCreateDataUsername());
                    list.add(lineForm);
                }
                return travelDao.savePassPoint(list);
            }
        }
        return true;
    }

    /**
     * ?????????????????????????????????
     */
    private boolean saveAllPoint(GpsLine form) {
        if (form.getAllPointLat() != null && form.getAllPointLng() != null) {
            // ?????????????????????
            String[] allLongitudes = form.getAllPointLng().split(";");
            String[] allLatitudes = form.getAllPointLat().split(";");
            List<LineContent> lineContent = new ArrayList<>();
            if (allLongitudes.length > 0 && allLatitudes.length > 0) {
                for (int i = 0; i < allLongitudes.length; i++) {
                    LineContent content = new LineContent();
                    content.setLineId(form.getId());
                    content.setSortOrder(i);
                    content.setLongitude(Double.parseDouble(allLongitudes[i]));
                    content.setLatitude(Double.parseDouble(allLatitudes[i]));
                    content.setCreateDataTime(new Date());
                    content.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    lineContent.add(content);
                }
                return travelDao.saveAllPoint(lineContent);
            }
        }
        return false;
    }

    @Override
    public TravelLineForm getTravelLineForm(String id) {
        return manageFenceDao.getTravelLineForm(id);
    }

    @Override
    public JsonResultBean updateTravelLine(GpsLine form, String ipAddress) {
        List<TravelLineForm> travelLineFormList = findTravelLineByName(form.getName()); // ??????????????????????????????
        String[] before = findType(form.getTravelLineId()); // ????????????????????????
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        // ??????id??????????????????
        TravelLine travel = travelDao.findTravelLineById(form.getTravelLineId());
        // ??????????????????????????????????????????
        if (travel != null) {
            // ???????????????????????????????????????
            String[] startToEndLng = form.getStartToEndLng().split(";");
            String[] startToEndLat = form.getStartToEndLat().split(";");
            form.setStartLongitude(Double.parseDouble(startToEndLng[0]));
            form.setStartLatitude(Double.parseDouble(startToEndLat[0]));
            form.setEndLongitude(Double.parseDouble(startToEndLng[1]));
            form.setEndLatitude(Double.parseDouble(startToEndLat[1]));
            form.setUpdateDataTime(new Date());
            form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            // ??????id????????????????????????????????????????????????
            travelDao.deletePassPoint(form);
            // ??????id??????????????????????????????????????????
            travelDao.deleteLineContent(form);
            boolean pointFlag = travelDao.updateStartAndEndPoint(form);
            boolean dotFlag = true;
            if (form.getWayPointLng() != null && form.getWayPointLat() != null && !form.getWayPointLng().isEmpty()
                && !form.getWayPointLat().isEmpty()) {
                dotFlag = updatePassDot(form); // ???????????????????????????????????????
            }
            boolean allFlag = updateAllPoint(form); // ???????????????????????????????????????
            if (pointFlag && dotFlag && allFlag) {
                String beforeLineType = before[0]; // ???????????????????????????
                String beforeName = before[1]; // ???????????????????????????
                String afterLineType = form.getType(); // ??????????????????????????????
                String afterName = form.getName(); // ??????????????????????????????
                String msg = "";
                if (travel.getName().equals(form.getName())) { // ???????????????
                    if (isTypeAndNameChanged(afterLineType, afterName, beforeLineType, beforeName)) {
                        msg = "?????????????????? " + afterName + "(????????????)";
                    } else {
                        msg = "??????????????????" + beforeName + "(????????????)" + afterName + "(????????????)";
                    }
                } else if (travelLineFormList.size() == 0) {
                    if (isTypeAndNameChanged(afterLineType, afterName, beforeLineType, beforeName)) {
                        msg += "?????????????????? " + afterName + "(????????????)";
                    } else {
                        msg += "??????????????????" + beforeName + "(????????????)" + afterName + "(????????????)";
                    }
                }
                logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                ZMQFencePub.pubChangeFence("10");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ?????????????????????????????????
     */
    private boolean updateAllPoint(GpsLine form) {
        if (form.getAllPointLat() != null && form.getAllPointLng() != null) {
            // ?????????????????????
            String[] allLongitudes = form.getAllPointLng().split(";");
            String[] allLatitudes = form.getAllPointLat().split(";");
            List<LineContent> lineContent = new ArrayList<>();
            if (allLongitudes.length > 0 && allLatitudes.length > 0) {
                for (int i = 0; i < allLongitudes.length; i++) {
                    LineContent content = new LineContent();
                    content.setLineId(form.getTravelLineId());
                    content.setSortOrder(i);
                    content.setLongitude(Double.parseDouble(allLongitudes[i]));
                    content.setLatitude(Double.parseDouble(allLatitudes[i]));
                    content.setUpdateDataTime(new Date());
                    content.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                    lineContent.add(content);
                }
                return travelDao.updateAllPoint(lineContent);
            }
        }
        return false;
    }

    /**
     * ?????????????????????????????????
     * @return boolean
     */
    private boolean updatePassDot(GpsLine form) {
        if (form.getWayPointLng() != null && form.getWayPointLat() != null && !form.getWayPointLat().isEmpty() && !form
            .getWayPointLng().isEmpty()) {
            // ?????????????????????
            String[] longitudes = form.getWayPointLng().split(";");
            String[] latitudes = form.getWayPointLat().split(";");
            List<LinePassPointForm> list = new ArrayList<>();
            if (!longitudes[0].equals("") && !latitudes[0].equals("")) {
                for (int i = 0; i < longitudes.length; i++) {
                    LinePassPointForm lineForm = new LinePassPointForm();
                    lineForm.setLineId(form.getTravelLineId());
                    lineForm.setSortOrder(i);
                    lineForm.setLongitude(Double.parseDouble(longitudes[i]));
                    lineForm.setLatitude(Double.parseDouble(latitudes[i]));
                    lineForm.setUpdateDataTime(new Date());
                    lineForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                    list.add(lineForm);
                }
                return travelDao.updatePassPoint(list);
            }
        }
        return true;
    }

    @Override
    public String findFenceTypeId(String fenceId) {
        return manageFenceDao.findFenceTypeId(fenceId);
    }

    /**
     * ??????809???????????????????????????
     * @param fence
     * @param lineId
     */
    @Override
    public String add809FenceLineInfo(T8080x8606 fence, String lineId, String vehicleId) {
        // ???????????????
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(lineId);
        fenceForm.setType("zw_m_line");
        lineDao.fenceInfo(fenceForm);
        Long lineParam = fence.getLineParam();
        //??????????????????
        FenceConfigForm fenceConfigForm = new FenceConfigForm();
        fenceConfigForm.setFenceId(fenceForm.getId());
        fenceConfigForm.setSendDownId(fence.getLineID());
        fenceConfigForm.setAlarmSource(2);
        fenceConfigForm.setSendFenceType(0);
        fenceConfigForm
            .setAlarmInPlatform((short) (lineParam & (1 << RouteAttributeDefinition.IN_ROUTE_ALARM_TO_PLATFORM)));
        fenceConfigForm
            .setAlarmOutPlatform((short) (lineParam & (1 << RouteAttributeDefinition.OUT_ROUTE_ALARM_TO_PLATFORM)));
        fenceConfigForm.setAlarmInDriver((int) (lineParam & (1 << RouteAttributeDefinition.IN_ROUTE_ALARM_TO_DRIVER)));
        fenceConfigForm
            .setAlarmOutDriver((int) (lineParam & (1 << RouteAttributeDefinition.OUT_ROUTE_ALARM_TO_DRIVER)));
        fenceConfigForm.setOpenDoor(2);
        fenceConfigForm.setCommunicationFlag(2);
        fenceConfigForm.setGnssFlag(2);
        fenceConfigForm.setAlarmStartTime(
            Optional.ofNullable(fence.getStartTime()).map(o -> DateUtil.getStringToDate(o, DateUtil.DATE_YYMMDDHHMMSS))
                .orElse(null));
        fenceConfigForm.setAlarmEndTime(
            Optional.ofNullable(fence.getEndTime()).map(o -> DateUtil.getStringToDate(o, DateUtil.DATE_YYMMDDHHMMSS))
                .orElse(null));
        fenceConfigForm.setVehicleId(vehicleId);
        fenceConfigDao.addFenceConfig(fenceConfigForm);
        return fenceConfigForm.getFenceId();
    }

    @Override
    public String update809FenceLineInfo(T8080x8606 fence, String lineId, String vehicleId) {
        String fenceId = lineDao.getFenceIdByLineId(lineId);
        Long lineParam = fence.getLineParam();
        //??????????????????
        FenceConfigForm fenceConfigForm = new FenceConfigForm();
        fenceConfigForm.setId(fenceId);
        fenceConfigForm.setFenceId(lineId);
        fenceConfigForm.setSendDownId(fence.getLineID());
        fenceConfigForm.setAlarmSource(2);
        fenceConfigForm.setSendFenceType(0);
        fenceConfigForm
            .setAlarmInPlatform((short) (lineParam & (1 << RouteAttributeDefinition.IN_ROUTE_ALARM_TO_PLATFORM)));
        fenceConfigForm
            .setAlarmOutPlatform((short) (lineParam & (1 << RouteAttributeDefinition.OUT_ROUTE_ALARM_TO_PLATFORM)));
        fenceConfigForm.setAlarmInDriver((int) (lineParam & (1 << RouteAttributeDefinition.IN_ROUTE_ALARM_TO_DRIVER)));
        fenceConfigForm
            .setAlarmOutDriver((int) (lineParam & (1 << RouteAttributeDefinition.OUT_ROUTE_ALARM_TO_DRIVER)));
        fenceConfigForm.setOpenDoor(2);
        fenceConfigForm.setCommunicationFlag(2);
        fenceConfigForm.setGnssFlag(2);
        fenceConfigForm.setAlarmStartTime(
            Optional.ofNullable(fence.getStartTime()).map(o -> DateUtil.getStringToDate(o, DateUtil.DATE_YYMMDDHHMMSS))
                .orElse(null));
        fenceConfigForm.setAlarmEndTime(
            Optional.ofNullable(fence.getEndTime()).map(o -> DateUtil.getStringToDate(o, DateUtil.DATE_YYMMDDHHMMSS))
                .orElse(null));
        fenceConfigForm.setVehicleId(vehicleId);
        fenceConfigDao.updateFenceConfig(fenceConfigForm);
        return fenceId;

    }

    public void add809Line(T8080x8606 fence, VehicleInfo vehicleInfo, String lineUuid) {
        List<LineForm> lineForms = new ArrayList<>();
        ArrayList<LinePoints> swervePortParams = fence.getSwervePortParams();
        swervePortParams.stream().forEach(o -> {
            LineForm lineForm = new LineForm();
            lineForm.setLineId(lineUuid);
            lineForm.setPointSeq((o.getLinePointId() - 1) + "");
            lineForm.setLongitude(o.getLongitude().toString());
            lineForm.setLatitude(o.getLatitude().toString());
            lineForms.add(lineForm);
        });
        lineDao.addLineContentBatch(lineForms);

        LineForm lineForm = new LineForm();
        lineForm.setId(lineUuid);
        lineForm.setName(fence.getName());
        lineForm.setWidth(swervePortParams.get(0).getWidth());
        lineForm.setGroupId(vehicleInfo.getGroupId());
        lineForm.setType("??????");
        lineForm.setCreateDataUsername("admin");
        // ??????????????????
        lineDao.add(lineForm);
    }

    /**
     * ??????809??????????????????
     * @param fence
     * @param vehicleInfo
     */
    @Override
    public String update809FenceLineInfo(T8080x8606 fence, VehicleInfo vehicleInfo, String fenceId) {
        FenceInfo fenceInfo = fenceDao.findFenceInfoById(fenceId);
        String lineUuid = fenceInfo.getShape();
        update809Line(fence, lineUuid);
        return lineUuid;
    }

    public void update809Line(T8080x8606 fence, String lineUuid) {
        ArrayList<LinePoints> swervePortParams = fence.getSwervePortParams();
        Line line = lineDao.findLineById(lineUuid);
        if (!Objects.equals(line.getName(), fence.getRouteName()) || !Objects
            .equals(line.getWidth(), swervePortParams.get(0).getWidth())) {
            LineForm lineForm = new LineForm();
            lineForm.setLineId(lineUuid);
            lineForm.setName(fence.getRouteName());
            lineForm.setWidth(swervePortParams.get(0).getWidth());
            lineForm.setUpdateDataTime(new Date());
            lineDao.updateLine(lineForm);
        }

        List<LineForm> lineForms = new ArrayList<>();
        for (int i = 0; i < swervePortParams.size(); i++) {
            LinePoints linePoints = swervePortParams.get(i);
            LineForm lineForm = new LineForm();
            lineForm.setLineId(lineUuid);
            lineForm.setPointSeq((linePoints.getLinePointId() - 1) + "");
            lineForm.setLongitude(linePoints.getLongitude().toString());
            lineForm.setLatitude(linePoints.getLatitude().toString());
            lineForms.add(lineForm);

        }

        lineDao.deleteLineContent(lineUuid);
        lineDao.addLineContentBatch(lineForms);

    }

    /**
     * ??????????????????????????????????????????
     * @param fence
     * @param vehicleInfo
     */
    @Override
    public Integer sendLineToVehicle(T8080x8606 fence, VehicleInfo vehicleInfo, String t809PlatId,
        String fenceConfigId) {
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleInfo.getId(), vehicleInfo.getDeviceNumber());
        DirectiveForm form = new DirectiveForm();
        form.setDirectiveName(null);
        form.setDownTime(new Date());
        form.setMonitorObjectId(vehicleInfo.getId());
        form.setParameterName(fenceConfigId);
        form.setParameterType("1");
        form.setStatus(5);//?????????5 msgSn
        form.setSwiftNumber(msgSN == null ? 0 : msgSN);
        form.setReplyCode(1);// ?????????
        if (null != msgSN) {
            try {
                String deviceId = vehicleInfo.getDeviceId();
                String deviceType = vehicleInfo.getDeviceType();
                // ??????????????????
                SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                    ConstantUtil.T808_DEVICE_GE_ACK);
                SubscibeInfoCache.getInstance().putTable(info);
                T808Message message = MsgUtil
                    .get808Message(vehicleInfo.getSimcardNumber(), ConstantUtil.T808_SET_LINE, msgSN, fence,
                        deviceType);
                if (DeviceInfo.isProtocol2019(deviceType)) {
                    fence.initParam2019();
                    vehicleInfo.setT809PlatId(t809PlatId);
                    WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_LINE, vehicleInfo);
                } else {
                    WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_LINE, deviceId);
                }
                form.setStatus(4);
            } catch (Exception e) {
                log.error("9211???????????????????????????", e);
            }
        }
        //??????????????????
        parameterDao.addDirective(form);

        return msgSN;
    }
}
