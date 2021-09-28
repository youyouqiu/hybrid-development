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
     * 多边形
     */
    private static final String SHAPE_POLYGON = "zw_m_polygon";

    /**
     * 圆形
     */
    private static final String SHAPE_CIRCLE = "zw_m_circle";

    /**
     * 线
     */
    private static final String SHAPE_LINE = "zw_m_line";

    /**
     * 标注
     */
    private static final String SHAPE_MARKER = "zw_m_marker";

    /**
     * 行政区域
     */
    private static final String SHAPE_ADMINISTRATIVE = "zw_m_administration";

    /**
     * 绘制方式 -> 多边形
     */
    private static final String DRAW_SHAPE_POLYGON = "1";

    /**
     * 绘制方式 -> 圆
     */
    private static final String DRAW_SHAPE_CIRCLE = "2";

    /**
     * 绘制方式 -> 线
     */
    private static final String DRAW_SHAPE_LINE = "3";

    /**
     * 绘制方式 -> 标注
     */
    private static final String DRAW_SHAPE_MARKER = "4";

    /**
     * 绘制方式 -> 行政区域
     */
    private static final String DRAW_SHAPE_ADMINISTRATION = "5";

    /**
     * 围栏新增
     */
    private static final String ADD_FENCE = "0";

    /**
     * 围栏修改
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
     * 获得围栏种类列表
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean getFenceTypeList() {
        List<String> userOwnOrganizationIdList =
            userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return new JsonResultBean(fenceManagementDao.getFenceTypeList(userOwnOrganizationIdList));
    }

    /**
     * 获得围栏种类信息
     * @param fenceTypeId 围栏种类id
     * @return FenceTypeInfo
     */
    @Override
    public FenceTypeInfo getFenceTypeInfoById(String fenceTypeId) {
        FenceTypeInfo fenceTypeInfo = fenceManagementDao.getFenceTypeInfoByFenceTypeId(fenceTypeId);
        if (fenceTypeInfo == null) {
            return null;
        }
        // 围栏种类已经绘制的围栏类型
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
     * 新增围栏种类
     * @param fenceTypeFrom 围栏信息
     * @param ipAddress     ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addFenceType(FenceTypeFrom fenceTypeFrom, String ipAddress) {
        fenceTypeFrom.setCreateDataUsername(SystemHelper.getCurrentUsername());
        fenceTypeFrom.setGroupId(userService.getOrgUuidByUser());
        if (!fenceManagementDao.addFenceType(fenceTypeFrom)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：新增围栏种类（" + fenceTypeFrom.getFenceTypeName() + "）";
        logSearchService.addLog(ipAddress, msg, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 删除围栏种类
     * @param fenceTypeId 围栏种类id
     * @param ipAddress   ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean deleteFenceType(String fenceTypeId, String ipAddress) {
        if (CollectionUtils.isNotEmpty(fenceManagementDao.getAlreadyDrawFenceByFenceTypeId(fenceTypeId))) {
            return new JsonResultBean(JsonResultBean.FAULT, "该种类下已绘制了具体围栏，不能删除！");
        }
        FenceTypeInfo fenceTypeInfo = fenceManagementDao.getFenceTypeInfoByFenceTypeId(fenceTypeId);
        if (!fenceManagementDao.deleteFenceTypeByFenceTypeId(fenceTypeId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：删除围栏种类（" + fenceTypeInfo.getFenceTypeName() + "）";
        logSearchService.addLog(ipAddress, msg, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 修改围栏种类
     * @param fenceTypeFrom 围栏信息
     * @param ipAddress     ip地址
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
            msg = "修改围栏种类：" + newType;
        } else {
            msg = "修改围栏种类：" + oldType + "修改为" + newType;
        }

        logSearchService.addLog(ipAddress, msg, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 判断围栏种类名称是否能使用(判断是否已存在)
     * @param fenceTypeName 围栏种类名称
     * @param fenceTypeId   围栏种类id
     * @return true:可以使用; false: 不可以使用
     */
    @Override
    public boolean judgeFenceTypeNameIsCanBeUsed(String fenceTypeName, String fenceTypeId) {
        FenceTypeInfo fenceTypeInfo = fenceManagementDao.getFenceTypeInfoByFenceTypeName(fenceTypeName);
        return fenceTypeInfo == null || Objects.equals(fenceTypeId, fenceTypeInfo.getId());
    }

    /**
     * 判断围栏名称是否能使用(判断是否已存在)
     * @param fenceName   围栏名称
     * @param fenceId     围栏id
     * @param fenceTypeId 围栏种类id
     * @return true:可以使用; false: 不可以使用
     */
    @Override
    public boolean judgeFenceNameIsCanBeUsed(String fenceName, String fenceId, String fenceTypeId) {
        FenceInfo fenceInfo = fenceManagementDao.getFenceInfo(fenceTypeId, fenceName);
        return fenceInfo == null || Objects.equals(fenceId, fenceInfo.getShape());
    }

    /**
     * 获得围栏种类下的绘制方式
     * @param fenceTypeId 围栏种类id
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
     * 获得围栏种类下的围栏信息集合
     * @param fenceTypeId 围栏种类id
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
     * 新增或修改 线
     * @param lineForm  线 信息
     * @param ipAddress ip地址
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
     * 新增 线
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
        // 保存线路信息
        if (!lineDao.add(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 保存围栏表
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(lineFormId);
        fenceForm.setType(SHAPE_LINE);
        fenceForm.setTypeId(form.getTypeId());
        fenceForm.setArea(AreaCalculationUtil.getLineArea(form));
        if (!lineDao.fenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：新增线形电子围栏（" + form.getName() + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // 维护围栏内存数据
        ZMQFencePub.pubChangeFence("8");
        return new JsonResultBean(form);
    }

    /**
     * 修改 线
     */
    private JsonResultBean updateLine(LineForm form, String ipAddress) {
        String pointSeq = form.getPointSeqs();
        String longitudes = form.getLongitudes();
        String latitudes = form.getLatitudes();
        if (StringUtils.isBlank(pointSeq) || StringUtils.isBlank(longitudes) || StringUtils.isBlank(latitudes)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String lineFormId = form.getLineId();
        // step1:根据线路id删除线路点数据-修改线路时先删点再加点信息
        if (!lineDao.deleteLineContent(lineFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        // step2:然后将更新的线路点数据添加到数据库
        List<LineForm> lineFormList = assemblyNeedAddLineContent(pointSeq, longitudes, latitudes, username, lineFormId);
        if (!lineDao.addLineContentBatch(lineFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // step3:更新线路主表数据
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
            msg = "围栏管理：修改线形电子围栏（" + newName + "）";
        } else {
            msg = "围栏管理：修改线形电子围栏（" + newName + "），修改为（" + oldName + "）";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // 维护围栏内存数据
        ZMQFencePub.pubChangeFence("8");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 组装需要新增的线内容
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
     * 新增或修改 标注
     * @param markForm  标注 信息
     * @param ipAddress ip地址
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
     * 新增标注
     */
    private JsonResultBean addMarker(MarkForm form, String ipAddress) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setGroupId(userService.getOrgUuidByUser());
        if (!markDao.marker(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 保存围栏表
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(form.getId());
        fenceForm.setType(SHAPE_MARKER);
        fenceForm.setTypeId(form.getTypeId());
        if (!markDao.fenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：新增标注点围栏（" + form.getName() + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(form);
    }

    /**
     * 修改标注
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
            msg = "围栏管理：修改标注点（" + newName + "）";
        } else {
            msg = "围栏管理：修改标注点（" + oldName + "）,修改为（" + newName + "）";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 新增或修改 圆
     * @param circleForm 圆 信息
     * @param ipAddress  ip地址
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
     * 新增圆
     */
    private JsonResultBean addCircles(CircleForm form, String ipAddress) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setGroupId(userService.getOrgUuidByUser());
        if (!circleDao.circles(form)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 保存围栏表
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(form.getId());
        fenceForm.setArea(AreaCalculationUtil.getCircleArea(form));
        fenceForm.setType(SHAPE_CIRCLE);
        fenceForm.setTypeId(form.getTypeId());
        if (!circleDao.fenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：新增圆形电子围栏（" + form.getName() + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // 维护围栏内存数据
        ZMQFencePub.pubChangeFence("4");
        return new JsonResultBean(form);
    }

    /**
     * 修改圆
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
            msg = "修改围栏：（" + newName + "）";
        } else {
            msg = "修改围栏：（" + oldName + "），修改为（" + newName + "）";
        }
        logSearchService.addLog(ipAddress, msg, "3", ",", "-", "");
        // 维护围栏内存数据
        ZMQFencePub.pubChangeFence("4");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 新增或修改 多边形
     * @param polygonForm 多边形 信息
     * @param ipAddress   ip地址
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
     * 修改行政区划
     */
    private JsonResultBean updateAdministration(AdministrationForm form, String ipAddress) {
        String polygonFormId = form.getPolygonId();
        // step1:先根据多边形id删除当前多边形的所有点数据
        if (!administrationDao.deleteAdministrationContent(polygonFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //step2:添加新的经纬度信息
        if (!saveAdministrationContent(form.getAdministrativeLngLat(), polygonFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //step3:更新多边形主表数据
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
            msg = "围栏管理：修改行政区划电子围栏（" + newName + "）";
        } else {
            msg = "围栏管理：修改行政区划电子围栏（" + oldName + "），修改为（" + newName + "）";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // 维护围栏内存数据
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 新增行政区划
     */
    private JsonResultBean addAdministration(AdministrationForm form, String ipAddress) {
        List<Administration> administrationList = administrationDao.findAdministrationByName(form.getName());
        if (administrationList.size() == 0) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // 所属企业
            String orgId = userService.getOrgUuidByUser();
            form.setGroupId(orgId);
            String aministrationId = form.getId();
            boolean flag =
                saveAdministrationContent(form.getAdministrativeLngLat(), aministrationId) && administrationDao
                    .administration(form);
            if (flag) {
                // 保存围栏表
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
                String msg = "围栏管理：新增行政区划电子围栏（" + form.getName() + "）";
                logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
                // 维护围栏内存数据
                ZMQFencePub.pubChangeFence("6");
                return new JsonResultBean(form);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 保存行政区划经纬度信息
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
     * 新增多边形
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
        // 保存围栏表
        ManageFenceFrom fenceForm = new ManageFenceFrom();
        fenceForm.setShape(polygonFormId);
        fenceForm.setType(SHAPE_POLYGON);
        fenceForm.setTypeId(form.getTypeId());
        fenceForm.setArea(area);
        if (!polygonDao.addFenceInfo(fenceForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：新增多边形电子围栏（" + form.getName() + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // 维护围栏内存数据
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(form);
    }

    /**
     * 修改多边形
     */
    private JsonResultBean updatePolygon(PolygonForm form, String ipAddress) {
        String pointSeq = form.getPointSeqs();
        String longitudes = form.getLongitudes();
        String latitudes = form.getLatitudes();
        if (StringUtils.isBlank(pointSeq) || StringUtils.isBlank(longitudes) || StringUtils.isBlank(latitudes)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String polygonFormId = form.getPolygonId();
        // step1:先根据多边形id删除当前多边形的所有点数据
        if (!polygonDao.deletePolygonContent(polygonFormId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        // step2:然后将更新的多边形点数据添加到数据库
        List<PolygonForm> polygonFormList =
            assemblyNeedAddPolygonContent(longitudes, latitudes, pointSeq, username, polygonFormId);
        if (!polygonDao.addMoreContent(polygonFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //step3:更新多边形主表数据
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
            msg = "围栏管理：修改多边形电子围栏（" + newName + "）";
        } else {
            msg = "围栏管理：修改多边形电子围栏（" + oldName + "），修改为（" + newName + "）";
        }
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        // 维护围栏内存数据
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 组装需要新增的多边形内容
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
     * 获得围栏详情
     * @param fenceId 围栏id
     * @param type    围栏类型
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
     * 判断围栏是否可以删除(判断是否关联了排班或者任务,没有关联可以删除)
     * @param fenceId 围栏id
     * @return true:可以删除; false: 不可以删除
     */
    @Override
    public boolean judgeFenceCanBeDelete(String fenceId) {
        return CollectionUtils.isEmpty(fenceManagementDao.getFenceRelationSchedulingInfoList(fenceId))
            && CollectionUtils.isEmpty(fenceManagementDao.getFenceRelationTaskInfoList(fenceId));
    }

    /**
     * 判断围栏是否可以修改(判断是否关联了正在执行的排班或者任务)
     * @param fenceId 围栏id
     * @return true:可以修改; false: 不可以修改
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
     * 判断排班是否正执行
     * @return true:正在执行; false:没有执行
     */
    private boolean judgeSchedulingIsInExecution(List<SchedulingInfo> schedulingInfoList) {
        long todayDateLong = DateUtil.todayFirstDate().getTime();
        // 执行中的排班
        List<SchedulingInfo> inExecutionSchedulingList = schedulingInfoList.stream().filter(
            info -> info.getStartDate().getTime() <= todayDateLong && todayDateLong < (info.getEndDate().getTime()
                + 24 * 60 * 60 * 1000)).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(inExecutionSchedulingList);
    }

    /**
     * 判断指派是否正执行
     * @return true:正在执行; false:没有执行
     */
    private boolean judgeDesignateIsInExecution(List<DesignateInfo> designateInfoList) {
        long todayDateLong = DateUtil.todayFirstDate().getTime();
        // 执行中的指派
        List<DesignateInfo> inExecutionDesignateList = designateInfoList.stream().filter(
            info -> info.getStartDate().getTime() <= todayDateLong && todayDateLong < (info.getEndDate().getTime()
                + 24 * 60 * 60 * 1000)).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(inExecutionDesignateList);
    }

    /**
     * 删除围栏
     * @param fenceId   围栏id
     * @param type      围栏类型
     * @param ipAddress ip地址
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
     * 获得用户围栏显示设置
     * @return List<String>
     */
    @Override
    public List<String> getUserFenceDisplaySetting() {
        return fenceManagementDao.getUserFenceDisplaySetting(userService.getCurrentUserUuid());
    }

    /**
     * 获得围栏tree
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean getFenceTree() {
        return new JsonResultBean(getFenceTreeJsonArray().toJSONString());
    }

    /**
     * 获得围栏tree
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
     * 保存用户围栏显示设置
     * @param fenceIds  围栏id
     * @param ipAddress ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean saveUserFenceDisplaySet(String fenceIds, String ipAddress) {
        String userId = userService.getCurrentUserUuid();
        // 先删除之前的设置
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
        String msg = "用户：【" + SystemHelper.getCurrentUsername() + "】进行围栏显示设置";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 组装围栏种类tree
     * @param fenceTypeInfoList 围栏种类信息
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
     * 组装围栏tree
     * @param fenceInfoList 围栏信息
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
     * 删除标注
     */
    private JsonResultBean deleteMarker(String fenceId, String name, String ipAddress) {
        if (!markDao.deleteMarker(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：删除围栏（" + name + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 删除线
     */
    private JsonResultBean deleteLine(String fenceId, String name, String ipAddress) {
        if (!lineDao.deleteLine(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (!lineDao.deleteLineContent(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：删除围栏（" + name + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("8");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 删除圆
     */
    private JsonResultBean deleteCircle(String fenceId, String name, String ipAddress) {
        if (!circleDao.deleteCircle(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：删除围栏（" + name + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("4");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 删除多边形
     */
    private JsonResultBean deletePolygon(String fenceId, String name, String ipAddress) {
        if (!polygonDao.deletePolygon(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (!polygonDao.deletePolygonContent(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：删除围栏（" + name + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private JsonResultBean deleteAdministration(String fenceId, String name, String ipAddress) {
        //删除行政区划详情的围栏数据
        if (!administrationDao.deleteAdministration(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        //删除行政区划多边形的经纬度数据
        if (!administrationDao.deleteAdministrationContent(fenceId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String msg = "围栏管理：删除围栏（" + name + "）";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
        ZMQFencePub.pubChangeFence("6");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 获得详情 标注
     */
    private JsonResultBean getMarkerDetail(String fenceId, String type) {
        JSONObject result = new JSONObject();
        Mark marker = markDao.findMarkById(fenceId);
        result.put("fenceInfo", marker);
        result.put("type", type);
        return new JsonResultBean(result);
    }

    /**
     * 获得详情 线
     */
    private JsonResultBean getLineDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(lineDao.findLineContentById(fenceId), fenceId, type));
    }

    /**
     * 获得详情 圆
     */
    private JsonResultBean getCircleDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(circleDao.getCircleById(fenceId), fenceId, type));
    }

    /**
     * 获得详情 多边形
     */
    private JsonResultBean getPolygonDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(polygonDao.getPolygonById(fenceId), fenceId, type));
    }

    /**
     * 获得详情 行政区划
     */
    private JsonResultBean getAdministrationDetail(String fenceId, String type) {
        return new JsonResultBean(assemblyFenceDetail(administrationDao.getAdministrationById(fenceId), fenceId, type));
    }

    /**
     * 组装围栏详情
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
