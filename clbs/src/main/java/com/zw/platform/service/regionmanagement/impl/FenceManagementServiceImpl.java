package com.zw.platform.service.regionmanagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.functionconfig.Administration;
import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.domain.functionconfig.form.AdministrationForm;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.regionmanagement.FenceTypeFrom;
import com.zw.platform.domain.regionmanagement.FenceTypeInfo;
import com.zw.platform.domain.regionmanagement.UserFenceDisplaySet;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.domain.taskmanagement.DesignateInfo;
import com.zw.platform.domain.taskmanagement.TaskInfo;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.AdministrationDao;
import com.zw.platform.repository.modules.CircleDao;
import com.zw.platform.repository.modules.LineDao;
import com.zw.platform.repository.modules.ManageFenceDao;
import com.zw.platform.repository.modules.MarkDao;
import com.zw.platform.repository.modules.PolygonDao;
import com.zw.platform.repository.modules.FenceManagementDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.regionmanagement.FenceManagementService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AreaCalculationUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 10:52
 */
@Service
public class FenceManagementServiceImpl implements FenceManagementService {

    /**
     * ?????????
     */
    private static final String SHAPE_POLYGON = "zw_m_polygon";

    /**
     * ??????
     */
    private static final String SHAPE_CIRCLE = "zw_m_circle";

    /**
     * ???
     */
    private static final String SHAPE_LINE = "zw_m_line";

    /**
     * ??????
     */
    private static final String SHAPE_MARKER = "zw_m_marker";

    /**
     * ????????????
     */
    private static final String SHAPE_ADMINISTRATIVE = "zw_m_administration";

    /**
     * ???????????? -> ?????????
     */
    private static final String DRAW_SHAPE_POLYGON = "1";

    /**
     * ???????????? -> ???
     */
    private static final String DRAW_SHAPE_CIRCLE = "2";

    /**
     * ???????????? -> ???
     */
    private static final String DRAW_SHAPE_LINE = "3";

    /**
     * ???????????? -> ??????
     */
    private static final String DRAW_SHAPE_MARKER = "4";

    /**
     * ???????????? -> ????????????
     */
    private static final String DRAW_SHAPE_ADMINISTRATION = "5";

    /**
     * ????????????
     */
    private static final String ADD_FENCE = "0";

    /**
     * ????????????
     */
    private static final String UPDATE_FENCE = "1";

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private FenceManagementDao fenceManagementDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ManageFenceDao manageFenceDao;

    @Autowired
    private LineDao lineDao;

    @Autowired
    private MarkDao markDao;

    @Autowired
    private CircleDao circleDao;

    @Autowired
    private PolygonDao polygonDao;

    @Autowired
    private AdministrationDao administrationDao;

    /**
     * ????????????????????????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean getFenceTypeList() {
        List<String> userOwnOrganizationIdList =
            userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return new JsonResultBean(fenceManagementDao.getFenceTypeList(userOwnOrganizationIdList));
    }

    /**
     * ????????????????????????
     * @param fenceTypeId ????????????id
     * @return FenceTypeInfo
     */
    @Override
    public FenceTypeInfo getFenceTypeInfoById(String fenceTypeId) {
        FenceTypeInfo fenceTypeInfo = fenceManagementDao.getFenceTypeInfoByFenceTypeId(fenceTypeId);
        if (fenceTypeInfo == null) {
            return null;
        }
        // ???????????????????????????????????????
        String alreadyDrawFence = fenceTypeInfo.getAlreadyDrawFence();
        if (StringUtils.isNotBlank(alreadyDrawFence)) {
            alreadyDrawFence =
                alreadyDrawFence.replace(SHAPE_POLYGON, DRAW_SHAPE_POLYGON).replace(SHAPE_CIRCLE, DRAW_SHAPE_CIRCLE)
                    .replace(SHAPE_LINE, DRAW_SHAPE_LINE).replace(SHAPE_MARKER, DRAW_SHAPE_MARKER)
                    .replace(SHAPE_ADMINISTRATIVE, DRAW_SHAPE_ADMINISTRATION);
            fenceTypeInfo.setAlreadyDrawFence(alreadyDrawFence);
        }
        return fenceTypeInfo;
    }

    /**
     * ??????????????????
     * @param fenceTypeFrom ????????????
     * @param ipAddress     ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addFenceType(FenceTypeFrom fenceTypeFrom, String ipAddress) {
        fenceTypeFrom.setCreateDataUsername(SystemHelper.getCurrentUsername());
        fenceTypeFrom.setGroupId(userService.getOrgUuidByUser());
        if (!fenceManagementDao.addFenceType(fenceTypeFrom)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "????????????????????????????????????" + fenceTypeFrom.getFenceTypeName() + "???";
        logSearchService.addLog(ipAddress, msg, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????
     * @param fenceTypeId ????????????id
     * @param ipAddress   ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean deleteFenceType(String fenceTypeId, String ipAddress) {
        if (CollectionUtils.isNotEmpty(fenceManagementDao.getAlreadyDrawFenceByFenceTypeId(fenceTypeId))) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????????????????");
        }
        FenceTypeInfo fenceTypeInfo = fenceManagementDao.getFenceTypeInfoByFenceTypeId(fenceTypeId);
        if (!fenceManagementDao.deleteFenceTypeByFenceTypeId(fenceTypeId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "????????????????????????????????????" + fenceTypeInfo.getFenceTypeName() + "???";
        logSearchService.addLog(ipAddress, msg, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????
     * @param fenceTypeFrom ????????????
     * @param ipAddress     ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean updateFenceType(FenceTypeFrom fenceTypeFrom, String ipAddress) {
        fenceTypeFrom.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        FenceTypeInfo oldFenceType = fenceManagementDao.getFenceTypeInfoByFenceTypeId(fenceTypeFrom.getId());
        if (oldFenceType == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (!fenceManagementDao.updateFenceType(fenceTypeFrom)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String newType = fenceTypeFrom.getFenceTypeName();
        String oldType = oldFenceType.getFenceTypeName();
        String msg;
        if (Objects.equals(newType, oldType)) {
            msg = "?????????????????????" + newType;
        } else {
            msg = "?????????????????????" + oldType + "?????????" + newType;
        }

        logSearchService.addLog(ipAddress, msg, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ???????????????????????????????????????(?????????????????????)
     * @param fenceTypeName ??????????????????
     * @param fenceTypeId   ????????????id
     * @return true:????????????; false: ???????????????
     */
    @Override
    public boolean judgeFenceTypeNameIsCanBeUsed(String fenceTypeName, String fenceTypeId) {
        FenceTypeInfo fenceTypeInfo = fenceManagementDao.getFenceTypeInfoByFenceTypeName(fenceTypeName);
        return fenceTypeInfo == null || Objects.equals(fenceTypeId, fenceTypeInfo.getId());
    }

    /**
     * ?????????????????????????????????(?????????????????????)
     * @param fenceName   ????????????
     * @param fenceId     ??????id
     * @param fenceTypeId ????????????id
     * @return true:????????????; false: ???????????????
     */
    @Override
    public boolean judgeFenceNameIsCanBeUsed(String fenceName, String fenceId, String fenceTypeId) {
        FenceInfo fenceInfo = fenceManagementDao.getFenceInfo(fenceTypeId, fenceName);
        return fenceInfo == null || Objects.equals(fenceId, fenceInfo.getShape());
    }

    /**
     * ????????????????????????????????????
     * @param fenceTypeId ????????????id
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean getFenceTypeDrawType(String fenceTypeId) {
        FenceTypeInfo fenceTypeInfo = fenceManagementDao.getFenceTypeInfoByFenceTypeId(fenceTypeId);
        if (fenceTypeInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean((Object) fenceTypeInfo.getDrawWay());
    }

    /**
     * ??????????????????????????????????????????
     * @param fenceTypeId ????????????id
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean getFenceInfoListByFenceTypeId(String fenceTypeId) {
        List<String> userOwnOrganizationIdList =
            userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        List<FenceInfo> fenceInfoList = fenceManagementDao.getFenceInfoList(fenceTypeId, userOwnOrganizationIdList);
        return new JsonResultBean(fenceInfoList);
    }

    /**
     * ??????????????? ???
     * @param lineForm  ??? ??????
     * @param ipAddress ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addOrUpdateLine(LineForm lineForm, String ipAddress) {
        if (lineForm == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String addOrUpdateLineFlag = lineForm.getAddOrUpdateLineFlag();
        if (Objects.equals(addOrUpdateLineFlag, ADD_FENCE)) {
            return addLine(lineForm, ipAddress);
        }
        if (Objects.equals(addOrUpdateLineFlag, UPDATE_FENCE)) {
            return updateLine(lineForm, ipAddress);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ?????? ???
     */
    private JsonResultBean addLine(LineForm form, String ipAddress) {
        String pointSeq = form.getPointSeqs();
        String longitudes = form.getLongitudes();
        String latitudes = form.getLatitudes();
        if (StringUtils.isBlank(pointSeq) || StringUtils.isBlank(longitudes) || StringUtils.isBlank(latitudes)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        form.setLineId(form.getId());
        String lineFormId = form.getLineId();
        List<LineForm> lineFormList = assemblyNeedAddLineContent(pointSeq, longitudes, latitudes, username, lineFormId);
        if (!lineDao.addLineContentBatch(lineFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        form.setGroupId(userService.getOrgUuidByUser());
        form.setCreateDataUsername(username);
        // ??????????????????
        if (!lineDao.add(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // ???????????????
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(lineFormId);
        fenceForm.setType(SHAPE_LINE);
        fenceForm.setTypeId(form.getTypeId());
        fenceForm.setArea(AreaCalculationUtil.getLineArea(form));
        if (!lineDao.fenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "??????????????????????????????????????????" + form.getName() + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // ????????????????????????
        ZMQFencePub.pubChangeFence("8");
        return new JsonResultBean(form);
    }

    /**
     * ?????? ???
     */
    private JsonResultBean updateLine(LineForm form, String ipAddress) {
        String pointSeq = form.getPointSeqs();
        String longitudes = form.getLongitudes();
        String latitudes = form.getLatitudes();
        if (StringUtils.isBlank(pointSeq) || StringUtils.isBlank(longitudes) || StringUtils.isBlank(latitudes)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String lineFormId = form.getLineId();
        // step1:????????????id?????????????????????-???????????????????????????????????????
        if (!lineDao.deleteLineContent(lineFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        // step2:???????????????????????????????????????????????????
        List<LineForm> lineFormList = assemblyNeedAddLineContent(pointSeq, longitudes, latitudes, username, lineFormId);
        if (!lineDao.addLineContentBatch(lineFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // step3:????????????????????????
        form.setUpdateDataUsername(username);
        if (!lineDao.updateLine(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setArea(AreaCalculationUtil.getLineArea(form));
        fenceForm.setShape(lineFormId);
        if (!fenceManagementDao.updateFenceArea(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        LineForm lineForm = manageFenceDao.getLineForm(lineFormId);
        String newName = form.getName();
        String oldName = lineForm.getName();
        String msg;
        if (Objects.equals(newName, oldName)) {
            msg = "??????????????????????????????????????????" + newName + "???";
        } else {
            msg = "??????????????????????????????????????????" + newName + "??????????????????" + oldName + "???";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // ????????????????????????
        ZMQFencePub.pubChangeFence("8");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????????????????
     */
    private List<LineForm> assemblyNeedAddLineContent(String pointSeq, String longitudes, String latitudes,
        String username, String lineFormId) {
        List<LineForm> lineFormList = new ArrayList<>();
        String[] pointSeqArray = pointSeq.split(",");
        String[] longitudeArray = longitudes.split(",");
        String[] latitudeArray = latitudes.split(",");
        for (int i = 0; i < pointSeqArray.length; i++) {
            LineForm line = new LineForm();
            line.setLineId(lineFormId);
            line.setPointSeq(pointSeqArray[i]);
            line.setLongitude(longitudeArray[i]);
            line.setLatitude(latitudeArray[i]);
            line.setCreateDataUsername(username);
            lineFormList.add(line);
        }
        return lineFormList;
    }

    /**
     * ??????????????? ??????
     * @param markForm  ?????? ??????
     * @param ipAddress ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addOrUpdateMarker(MarkForm markForm, String ipAddress) {
        if (markForm == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String addOrUpdateMarkerFlag = markForm.getAddOrUpdateMarkerFlag();
        if (Objects.equals(addOrUpdateMarkerFlag, ADD_FENCE)) {
            return addMarker(markForm, ipAddress);
        }
        if (Objects.equals(addOrUpdateMarkerFlag, UPDATE_FENCE)) {
            return updateMarker(markForm, ipAddress);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ????????????
     */
    private JsonResultBean addMarker(MarkForm form, String ipAddress) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setGroupId(userService.getOrgUuidByUser());
        if (!markDao.marker(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // ???????????????
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(form.getId());
        fenceForm.setType(SHAPE_MARKER);
        fenceForm.setTypeId(form.getTypeId());
        if (!markDao.fenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "???????????????????????????????????????" + form.getName() + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(form);
    }

    /**
     * ????????????
     */
    private JsonResultBean updateMarker(MarkForm form, String ipAddress) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        if (!markDao.updateMarker(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        MarkForm markForm = manageFenceDao.getMarkForm(form.getMarkerId());
        String newName = form.getName();
        String oldName = markForm.getName();
        String msg;
        if (Objects.equals(newName, oldName)) {
            msg = "?????????????????????????????????" + newName + "???";
        } else {
            msg = "?????????????????????????????????" + oldName + "???,????????????" + newName + "???";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????? ???
     * @param circleForm ??? ??????
     * @param ipAddress  ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addOrUpdateCircle(CircleForm circleForm, String ipAddress) {
        if (circleForm == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String addOrUpdateCircleFlag = circleForm.getAddOrUpdateCircleFlag();
        if (Objects.equals(addOrUpdateCircleFlag, ADD_FENCE)) {
            return addCircles(circleForm, ipAddress);
        }
        if (Objects.equals(addOrUpdateCircleFlag, UPDATE_FENCE)) {
            return updateCircles(circleForm, ipAddress);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ?????????
     */
    private JsonResultBean addCircles(CircleForm form, String ipAddress) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setGroupId(userService.getOrgUuidByUser());
        if (!circleDao.circles(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // ???????????????
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(form.getId());
        fenceForm.setArea(AreaCalculationUtil.getCircleArea(form));
        fenceForm.setType(SHAPE_CIRCLE);
        fenceForm.setTypeId(form.getTypeId());
        if (!circleDao.fenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "??????????????????????????????????????????" + form.getName() + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // ????????????????????????
        ZMQFencePub.pubChangeFence("4");
        return new JsonResultBean(form);
    }

    /**
     * ?????????
     */
    private JsonResultBean updateCircles(CircleForm form, String ipAddress) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        CircleForm circleForm = manageFenceDao.getCircleForm(form.getCircleId());
        if (!circleDao.updateCircle(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setArea(AreaCalculationUtil.getCircleArea(form));
        fenceForm.setShape(form.getCircleId());
        if (!fenceManagementDao.updateFenceArea(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String newName = form.getName();
        String oldName = circleForm.getName();
        String msg;
        if (Objects.equals(newName, oldName)) {
            msg = "??????????????????" + newName + "???";
        } else {
            msg = "??????????????????" + oldName + "??????????????????" + newName + "???";
        }
        logSearchService.addLog(ipAddress, msg, "3", ",", "-", "");
        // ????????????????????????
        ZMQFencePub.pubChangeFence("4");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????? ?????????
     * @param polygonForm ????????? ??????
     * @param ipAddress   ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addOrUpdatePolygon(PolygonForm polygonForm, String ipAddress) {
        if (polygonForm == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String addOrUpdatePolygonFlag = polygonForm.getAddOrUpdatePolygonFlag();
        if (Objects.equals(addOrUpdatePolygonFlag, ADD_FENCE)) {
            return addPolygon(polygonForm, ipAddress);
        }
        if (Objects.equals(addOrUpdatePolygonFlag, UPDATE_FENCE)) {
            return updatePolygon(polygonForm, ipAddress);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean addOrUpdateAdministration(AdministrationForm administrationForm, String ipAddress) {
        if (administrationForm == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String addOrUpdateFlag = administrationForm.getAddOrUpdatePolygonFlag();
        if (Objects.equals(addOrUpdateFlag, ADD_FENCE)) {
            return addAdministration(administrationForm, ipAddress);
        }

        if (Objects.equals(addOrUpdateFlag, UPDATE_FENCE)) {
            return updateAdministration(administrationForm, ipAddress);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ??????????????????
     */
    private JsonResultBean updateAdministration(AdministrationForm form, String ipAddress) {
        String polygonFormId = form.getPolygonId();
        // step1:??????????????????id???????????????????????????????????????
        if (!administrationDao.deleteAdministrationContent(polygonFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //step2:???????????????????????????
        if (!saveAdministrationContent(form.getAdministrativeLngLat(), polygonFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //step3:???????????????????????????
        String username = SystemHelper.getCurrentUsername();
        form.setUpdateDataUsername(username);
        if (!administrationDao.updateAdministration(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(polygonFormId);
        double area = Double.parseDouble(new DecimalFormat("#.0").format(Double.parseDouble(form.getArea()) / 1000000));
        fenceForm.setArea(area);
        if (!fenceManagementDao.updateFenceArea(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        AdministrationForm administrationForm = manageFenceDao.getAdministrationForm(polygonFormId);
        String newName = form.getName();
        String oldName = administrationForm.getName();
        String msg;
        if (Objects.equals(newName, oldName)) {
            msg = "????????????????????????????????????????????????" + newName + "???";
        } else {
            msg = "????????????????????????????????????????????????" + oldName + "??????????????????" + newName + "???";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // ????????????????????????
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????
     */
    private JsonResultBean addAdministration(AdministrationForm form, String ipAddress) {
        List<Administration> administrationList = administrationDao.findAdministrationByName(form.getName());
        if (administrationList.size() == 0) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // ????????????
            String orgId = userService.getOrgUuidByUser();
            form.setGroupId(orgId);
            String aministrationId = form.getId();
            boolean flag =
                saveAdministrationContent(form.getAdministrativeLngLat(), aministrationId) && administrationDao
                    .administration(form);
            if (flag) {
                // ???????????????
                ManageFenceFrom fenceForm = new ManageFenceFrom();
                fenceForm.setShape(form.getId());
                fenceForm.setType(SHAPE_ADMINISTRATIVE);
                fenceForm.setTypeId(form.getTypeId());
                if (!StringUtils.isBlank(form.getArea())) {
                    double area = Double
                        .parseDouble(new DecimalFormat("#.0").format(Double.parseDouble(form.getArea()) / 1000000));
                    fenceForm.setArea(area);
                }
                if (!polygonDao.addFenceInfo(fenceForm)) {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
                String msg = "????????????????????????????????????????????????" + form.getName() + "???";
                logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                // ????????????????????????
                ZMQFencePub.pubChangeFence("6");
                return new JsonResultBean(form);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ?????????????????????????????????
     */
    private boolean saveAdministrationContent(String administrativeLngLat, String aministrationId) {
        if (administrativeLngLat == null) {
            return false;
        }
        String[] pointSeqs = administrativeLngLat.split("-");
        List<AdministrationForm> list = new ArrayList<>();
        for (int i = 0; i < pointSeqs.length; i++) {
            String[] regionCount;
            regionCount = pointSeqs[i].split(",");
            for (int j = 0; j < regionCount.length; j++) {
                AdministrationForm administration = new AdministrationForm();
                administration.setId(aministrationId);
                administration.setRegionCount(i);
                administration.setSortOrder(j);
                administration.setLongitude(regionCount[j]);
                administration.setLatitude(regionCount[j + 1]);
                administration.setCreateDataUsername(SystemHelper.getCurrentUsername());
                list.add(administration);
                j = j + 1;
            }
        }
        boolean contentFlag = administrationDao.administrationContent(list);
        return contentFlag;
    }

    /**
     * ???????????????
     */
    private JsonResultBean addPolygon(PolygonForm form, String ipAddress) {
        String longitudes = form.getLongitudes();
        String latitudes = form.getLatitudes();
        String pointSeq = form.getPointSeqs();
        if (StringUtils.isBlank(pointSeq) || StringUtils.isBlank(longitudes) || StringUtils.isBlank(latitudes)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        String polygonFormId = form.getId();
        List<PolygonForm> polygonFormList =
            assemblyNeedAddPolygonContent(longitudes, latitudes, pointSeq, username, polygonFormId);
        if (!polygonDao.addMoreContent(polygonFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        form.setCreateDataUsername(username);
        form.setGroupId(userService.getOrgUuidByUser());
        if (!polygonDao.addPolygons(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        double area = Double.parseDouble(new DecimalFormat("#.0").format(Double.parseDouble(form.getArea()) / 1000000));
        // ???????????????
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(polygonFormId);
        fenceForm.setType(SHAPE_POLYGON);
        fenceForm.setTypeId(form.getTypeId());
        fenceForm.setArea(area);
        if (!polygonDao.addFenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "?????????????????????????????????????????????" + form.getName() + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // ????????????????????????
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(form);
    }

    /**
     * ???????????????
     */
    private JsonResultBean updatePolygon(PolygonForm form, String ipAddress) {
        String pointSeq = form.getPointSeqs();
        String longitudes = form.getLongitudes();
        String latitudes = form.getLatitudes();
        if (StringUtils.isBlank(pointSeq) || StringUtils.isBlank(longitudes) || StringUtils.isBlank(latitudes)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String polygonFormId = form.getPolygonId();
        // step1:??????????????????id???????????????????????????????????????
        if (!polygonDao.deletePolygonContent(polygonFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        // step2:??????????????????????????????????????????????????????
        List<PolygonForm> polygonFormList =
            assemblyNeedAddPolygonContent(longitudes, latitudes, pointSeq, username, polygonFormId);
        if (!polygonDao.addMoreContent(polygonFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //step3:???????????????????????????
        form.setUpdateDataUsername(username);
        if (!polygonDao.updatePolygon(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(polygonFormId);
        double area = Double.parseDouble(new DecimalFormat("#.0").format(Double.parseDouble(form.getArea()) / 1000000));
        fenceForm.setArea(area);
        if (!fenceManagementDao.updateFenceArea(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        PolygonForm polygonForm = manageFenceDao.getPolygonForm(polygonFormId);
        String newName = form.getName();
        String oldName = polygonForm.getName();
        String msg;
        if (Objects.equals(newName, oldName)) {
            msg = "?????????????????????????????????????????????" + newName + "???";
        } else {
            msg = "?????????????????????????????????????????????" + oldName + "??????????????????" + newName + "???";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // ????????????????????????
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ????????????????????????????????????
     */
    private List<PolygonForm> assemblyNeedAddPolygonContent(String longitudes, String latitudes, String pointSeq,
        String username, String polygonFormId) {
        String[] pointSeqArray = pointSeq.split(",");
        String[] longitudesArray = longitudes.split(",");
        String[] latitudesArray = latitudes.split(",");
        List<PolygonForm> polygonFormList = new ArrayList<>();
        for (int i = 0; i < pointSeqArray.length; i++) {
            PolygonForm polygonForm = new PolygonForm();
            polygonForm.setId(polygonFormId);
            polygonForm.setPointSeq(pointSeqArray[i]);
            polygonForm.setLongitude(longitudesArray[i]);
            polygonForm.setLatitude(latitudesArray[i]);
            polygonForm.setCreateDataUsername(username);
            polygonFormList.add(polygonForm);
        }
        return polygonFormList;
    }

    /**
     * ??????????????????
     * @param fenceId ??????id
     * @param type    ????????????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean getFenceDetail(String fenceId, String type) {
        if (Objects.equals(SHAPE_MARKER, type)) {
            return getMarkerDetail(fenceId, type);
        }
        if (Objects.equals(SHAPE_LINE, type)) {
            return getLineDetail(fenceId, type);
        }
        if (Objects.equals(SHAPE_CIRCLE, type)) {
            return getCircleDetail(fenceId, type);
        }
        if (Objects.equals(SHAPE_POLYGON, type)) {
            return getPolygonDetail(fenceId, type);
        }

        if (Objects.equals(SHAPE_ADMINISTRATIVE, type)) {
            return getAdministrationDetail(fenceId, type);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ??????????????????????????????(???????????????????????????????????????,????????????????????????)
     * @param fenceId ??????id
     * @return true:????????????; false: ???????????????
     */
    @Override
    public boolean judgeFenceCanBeDelete(String fenceId) {
        return CollectionUtils.isEmpty(fenceManagementDao.getFenceRelationSchedulingInfoList(fenceId))
            && CollectionUtils.isEmpty(fenceManagementDao.getFenceRelationTaskInfoList(fenceId));
    }

    /**
     * ??????????????????????????????(??????????????????????????????????????????????????????)
     * @param fenceId ??????id
     * @return true:????????????; false: ???????????????
     */
    @Override
    public boolean judgeFenceCanBeUpdate(String fenceId) {
        List<SchedulingInfo> relationSchedulingInfoList =
            fenceManagementDao.getFenceRelationSchedulingInfoList(fenceId);
        if (CollectionUtils.isNotEmpty(relationSchedulingInfoList)) {
            if (judgeSchedulingIsInExecution(relationSchedulingInfoList)) {
                return false;
            }
        }
        List<DesignateInfo> relationDesignateInfoList = fenceManagementDao.getFenceRelationDesignateInfoList(fenceId);
        if (CollectionUtils.isNotEmpty(relationDesignateInfoList)) {
            return !judgeDesignateIsInExecution(relationDesignateInfoList);
        }
        return true;
    }

    /**
     * ???????????????????????????
     * @return true:????????????; false:????????????
     */
    private boolean judgeSchedulingIsInExecution(List<SchedulingInfo> schedulingInfoList) {
        long todayDateLong = DateUtil.todayFirstDate().getTime();
        // ??????????????????
        List<SchedulingInfo> inExecutionSchedulingList = schedulingInfoList.stream().filter(
            info -> info.getStartDate().getTime() <= todayDateLong && todayDateLong < (info.getEndDate().getTime()
                + 24 * 60 * 60 * 1000)).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(inExecutionSchedulingList);
    }

    /**
     * ???????????????????????????
     * @return true:????????????; false:????????????
     */
    private boolean judgeDesignateIsInExecution(List<DesignateInfo> designateInfoList) {
        long todayDateLong = DateUtil.todayFirstDate().getTime();
        // ??????????????????
        List<DesignateInfo> inExecutionDesignateList = designateInfoList.stream().filter(
            info -> info.getStartDate().getTime() <= todayDateLong && todayDateLong < (info.getEndDate().getTime()
                + 24 * 60 * 60 * 1000)).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(inExecutionDesignateList);
    }

    /**
     * ????????????
     * @param fenceId   ??????id
     * @param type      ????????????
     * @param ipAddress ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean deleteFence(String fenceId, String type, String ipAddress) {
        FenceInfo fenceInfo = fenceManagementDao.getFenceInfoByFenceId(fenceId);
        if (fenceInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        fenceManagementDao.deleteFenceInfo(fenceId);
        String fenceName = fenceInfo.getFenceName();
        if (Objects.equals(SHAPE_MARKER, type)) {
            return deleteMarker(fenceId, fenceName, ipAddress);
        }
        if (Objects.equals(SHAPE_LINE, type)) {
            return deleteLine(fenceId, fenceName, ipAddress);
        }
        if (Objects.equals(SHAPE_CIRCLE, type)) {
            return deleteCircle(fenceId, fenceName, ipAddress);
        }
        if (Objects.equals(SHAPE_POLYGON, type)) {
            return deletePolygon(fenceId, fenceName, ipAddress);
        }
        if (Objects.equals(SHAPE_ADMINISTRATIVE, type)) {
            return deleteAdministration(fenceId, fenceName, ipAddress);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ??????????????????????????????
     * @return List<String>
     */
    @Override
    public List<String> getUserFenceDisplaySetting() {
        return fenceManagementDao.getUserFenceDisplaySetting(userService.getCurrentUserUuid());
    }

    /**
     * ????????????tree
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean getFenceTree() {
        return new JsonResultBean(getFenceTreeJsonArray().toJSONString());
    }

    /**
     * ????????????tree
     * @return JSONArray
     */
    @Override
    public JSONArray getFenceTreeJsonArray() {
        JSONArray result = new JSONArray();
        List<FenceTypeInfo> allFenceType = fenceManagementDao.getAllFenceType();
        result.addAll(assemblyFenceTypeNodeTree(allFenceType));
        List<String> userOwnOrganizationIdList =
            userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        List<FenceInfo> fenceInfoList = fenceManagementDao.getFenceInfoList(null, userOwnOrganizationIdList);
        result.addAll(assemblyFenceNodeTree(fenceInfoList));
        return result;
    }

    /**
     * ??????????????????????????????
     * @param fenceIds  ??????id
     * @param ipAddress ip??????
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean saveUserFenceDisplaySet(String fenceIds, String ipAddress) {
        String userId = userService.getCurrentUserUuid();
        // ????????????????????????
        fenceManagementDao.deleteUserFenceDisplaySet(userId);
        if (StringUtils.isBlank(fenceIds)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        String[] fenceIdArr = fenceIds.split(",");
        List<UserFenceDisplaySet> userFenceDisplaySetList = new ArrayList<>();
        for (String id : fenceIdArr) {
            UserFenceDisplaySet userFenceDisplaySet = new UserFenceDisplaySet();
            userFenceDisplaySet.setRelationId(id);
            userFenceDisplaySet.setUserId(userId);
            userFenceDisplaySet.setCreateDataUsername(SystemHelper.getCurrentUsername());
            userFenceDisplaySetList.add(userFenceDisplaySet);
        }
        if (!fenceManagementDao.saveUserFenceDisplaySet(userFenceDisplaySetList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "????????????" + SystemHelper.getCurrentUsername() + "???????????????????????????";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????tree
     * @param fenceTypeInfoList ??????????????????
     * @return JSONArray
     */
    private JSONArray assemblyFenceTypeNodeTree(List<FenceTypeInfo> fenceTypeInfoList) {
        JSONArray fenceTypeNodeTree = new JSONArray();
        for (FenceTypeInfo fenceType : fenceTypeInfoList) {
            JSONObject fenceTypeNode = new JSONObject();
            fenceTypeNode.put("id", fenceType.getId());
            fenceTypeNode.put("pId", "0");
            fenceTypeNode.put("name", fenceType.getFenceTypeName());
            fenceTypeNode.put("type", "fenceParent");
            fenceTypeNodeTree.add(fenceTypeNode);
        }
        return fenceTypeNodeTree;
    }

    /**
     * ????????????tree
     * @param fenceInfoList ????????????
     * @return JSONArray
     */
    private JSONArray assemblyFenceNodeTree(List<FenceInfo> fenceInfoList) {
        JSONArray fenceNodeTree = new JSONArray();
        for (FenceInfo fenceInfo : fenceInfoList) {
            JSONObject fenceNode = new JSONObject();
            fenceNode.put("id", fenceInfo.getShape());
            fenceNode.put("pId", fenceInfo.getFenceTypeId());
            fenceNode.put("name", fenceInfo.getFenceName());
            fenceNode.put("type", "fence");
            String type = fenceInfo.getType();
            fenceNode.put("fenceType", type);
            fenceNode.put("iconSkin", type + "_skin");
            fenceNodeTree.add(fenceNode);
        }
        return fenceNodeTree;
    }

    /**
     * ????????????
     */
    private JsonResultBean deleteMarker(String fenceId, String name, String ipAddress) {
        if (!markDao.deleteMarker(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "??????????????????????????????" + name + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ?????????
     */
    private JsonResultBean deleteLine(String fenceId, String name, String ipAddress) {
        if (!lineDao.deleteLine(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (!lineDao.deleteLineContent(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "??????????????????????????????" + name + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("8");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ?????????
     */
    private JsonResultBean deleteCircle(String fenceId, String name, String ipAddress) {
        if (!circleDao.deleteCircle(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "??????????????????????????????" + name + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("4");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ???????????????
     */
    private JsonResultBean deletePolygon(String fenceId, String name, String ipAddress) {
        if (!polygonDao.deletePolygon(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (!polygonDao.deletePolygonContent(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "??????????????????????????????" + name + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private JsonResultBean deleteAdministration(String fenceId, String name, String ipAddress) {
        //???????????????????????????????????????
        if (!administrationDao.deleteAdministration(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        //?????????????????????????????????????????????
        if (!administrationDao.deleteAdministrationContent(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "??????????????????????????????" + name + "???";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ???????????? ??????
     */
    private JsonResultBean getMarkerDetail(String fenceId, String type) {
        JSONObject result = new JSONObject();
        Mark marker = markDao.findMarkById(fenceId);
        result.put("fenceInfo", marker);
        result.put("type", type);
        return new JsonResultBean(result);
    }

    /**
     * ???????????? ???
     */
    private JsonResultBean getLineDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(lineDao.findLineContentById(fenceId), fenceId, type));
    }

    /**
     * ???????????? ???
     */
    private JsonResultBean getCircleDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(circleDao.getCircleById(fenceId), fenceId, type));
    }

    /**
     * ???????????? ?????????
     */
    private JsonResultBean getPolygonDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(polygonDao.getPolygonById(fenceId), fenceId, type));
    }

    /**
     * ???????????? ????????????
     */
    private JsonResultBean getAdministrationDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(administrationDao.getAdministrationById(fenceId), fenceId, type));
    }

    /**
     * ??????????????????
     */
    private JSONObject assemblyFenceDetail(Object fenceDetail, String fenceId, String type) {
        JSONObject result = new JSONObject();
        result.put("fenceInfo", fenceDetail);
        result.put("type", type);
        result.put("relationSchedulingNameList", fenceManagementDao.getFenceRelationSchedulingInfoList(fenceId).stream()
            .map(SchedulingInfo::getScheduledName).collect(Collectors.toList()));
        result.put("relationTaskNameList",
            fenceManagementDao.getFenceRelationTaskInfoList(fenceId).stream().map(TaskInfo::getTaskName)
                .collect(Collectors.toList()));
        return result;
    }
}
