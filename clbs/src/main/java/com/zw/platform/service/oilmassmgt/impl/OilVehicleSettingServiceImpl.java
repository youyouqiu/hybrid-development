/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.service.oilmassmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.share.ShapeUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilVehicleSettingForm;
import com.zw.platform.domain.vas.oilmassmgt.query.OilVehicleSettingQuery;
import com.zw.platform.event.ConfigUnbindVehicleEvent;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.modules.RodSensorDao;
import com.zw.platform.repository.vas.FuelTankManageDao;
import com.zw.platform.repository.vas.OilVehicleSettingDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.MonitorTypeUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.impl.WsOilSensorCommandService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ??????????????????Service????????? <p>Title: OilVehicleSettingServiceImpl.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016???10???24?????????4:32:08
 */
@Service
public class OilVehicleSettingServiceImpl implements OilVehicleSettingService {
    private static Logger log = LogManager.getLogger(OilVehicleSettingServiceImpl.class);

    @Autowired
    private OilVehicleSettingDao oilVehicleSettingDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private FuelTankManageService fuelTankServive;

    @Autowired
    private WsOilSensorCommandService wsOilSensorCommandService;

    @Autowired
    private RodSensorDao rodSensorDao;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private FuelTankManageDao fuelTankManageDao;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private MonitorTypeUtil monitorTypeUtil;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Autowired
    private void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    private static DecimalFormat dfInt = new DecimalFormat("#"); // ??????

    @Override
    public Page<OilVehicleSetting> findOilVehicleList(OilVehicleSettingQuery query) {
        Page<OilVehicleSetting> page;
        try {
            page = getOilVehicleFromCache(query);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            page = getOilVehicleFromDB(query);
        } finally {
            PageHelper.clearPage();
        }
        if (page == null || page.isEmpty()) {
            page = new Page<>();
            return page;
        }
        // ??????result??????groupId?????????groupName???result??????????????????
        for (OilVehicleSetting parameter : page) {
            boolean isBind =
                StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils.isNotBlank(parameter.getId());
            // ????????????
            if (isBind) { // ?????????
                setIssuedStatus(parameter);
            }
        }
        return page;
    }

    private Page<OilVehicleSetting> getOilVehicleFromCache(OilVehicleSettingQuery query) throws InterruptedException {
        RedisSensorQuery redisQuery =
            new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                Integer.valueOf(query.getProtocol()));
        List<String> cacheIdList =
            redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_OIL_BOX_MONITOR);
        final Set<String> cacheIdSet = new HashSet<>(cacheIdList);
        if (!StringUtils.isEmpty(query.getSimpleQueryParam())) { // ????????????
            List<String> oilSensorVehicle = redisVehicleService.getOilSensorVehicle(redisQuery);
            for (String vid : oilSensorVehicle) {
                if (cacheIdSet.add(vid)) {
                    cacheIdList.add(vid);
                }
            }
        }
        int total = cacheIdList.size();
        int curPage = query.getPage().intValue();// ?????????
        int pageSize = query.getLimit().intValue(); // ????????????
        int start = (curPage - 1) * pageSize;// ??????????????????
        int end = pageSize > (total - start) ? total : (pageSize * curPage);// ??????????????????
        List<String> list = cacheIdList.subList(start, end);
        List<OilVehicleSetting> vehicles = new LinkedList<>();
        if (list.size() > 0) {
            List<String> vehicleIds = new LinkedList<>();
            List<String> tankIds = new LinkedList<>();
            for (String item : list) {
                String[] info = item.split("#@!@#");
                vehicleIds.add(info[0]);
                if (info.length == 2) {
                    tankIds.add(info[1]);
                }
            }
            //????????????????????????????????????????????????
            vehicles = oilVehicleSettingDao.listOilVehicleByIds(vehicleIds, tankIds);
            if (vehicles != null && vehicles.size() > 0) {
                String monitorType = null;
                Set<String> vids = vehicles.stream().map(OilVehicleSetting::getVId).collect(Collectors.toSet());
                Map<String, Map<String, String>> vehicleMap =
                    RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vids)).stream()
                        .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
                for (OilVehicleSetting oilVehicleSetting : vehicles) {
                    //??????ID??????????????????
                    Map<String, String> map = vehicleMap.get(oilVehicleSetting.getVId());
                    if (map != null) {
                        oilVehicleSetting.setGroups(Optional.ofNullable(map.get("orgName")).orElse(""));
                        //????????????????????????
                        monitorType = Optional.ofNullable(map.get("monitorType")).orElse("");
                        //???
                        if ("0".equals(monitorType)) {
                            oilVehicleSetting.setVehicleType(map.get("vehicleType") == null ? "" :
                                cacheManger.getVehicleType(map.get("vehicleType")).getType());
                        } else if ("2".equals(monitorType)) { //????????????
                            oilVehicleSetting.setVehicleType("????????????");
                        }
                    }
                }
            }
            //?????????????????????????????????????????????????????????????????????
            VehicleUtil.sort(vehicles, vehicleIds);
        }
        return RedisQueryUtil.getListToPage(vehicles, query, total);
    }

    private Page<OilVehicleSetting> getOilVehicleFromDB(OilVehicleSettingQuery query) {
        Page<OilVehicleSetting> page = null;

        String userUuid = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (userUuid != null && !Objects.equals(userUuid, "") && orgList != null && orgList.size() > 0) {
            page = oilVehicleSettingDao.findOilVehicleList(query, userUuid, orgList);
        }
        if (null != page) {
            Set<String> vids = page.stream().map(OilVehicleSetting::getVId).collect(Collectors.toSet());
            Map<String, BindDTO> bindInfos;
            try {
                bindInfos = VehicleUtil.batchGetBindInfosByRedis(vids);
            } catch (Exception e) {
                bindInfos = Collections.emptyMap();
            }
            for (OilVehicleSetting parameter : page) {
                // ??????groupId?????????groupName
                BindDTO bindDTO = bindInfos.get(parameter.getVId());
                if (bindDTO != null) {
                    parameter.setGroups(bindDTO.getOrgName());
                }
            }
        }
        return page;
    }

    private void setIssuedStatus(OilVehicleSetting parameter) {
        String paramType = "F3-8103-4-" + parameter.getOilBoxType(); // ??????
        String calibrationParamType = "F3-8103-5-" + parameter.getOilBoxType(); // ????????????

        List<Directive> paramList1 =
            parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), paramType); // 4: ????????????
        List<Directive> paramList2 = parameterDao
            .findParameterByType(parameter.getVehicleId(), parameter.getId(), calibrationParamType); // 5: ????????????
        Directive param1 = null;
        Directive param2 = null;
        if (paramList1 != null && paramList1.size() > 0) {
            param1 = paramList1.get(0);
        }
        if (paramList2 != null && paramList2.size() > 0) {
            param2 = paramList2.get(0);
        }
        if (param1 != null && param2 != null) {
            parameter.setSettingParamId(param1.getId());
            parameter.setCalibrationParamId(param2.getId());

            if (param1.getStatus().equals(param2.getStatus())) {
                parameter.setStatus(param1.getStatus());
            } else if (param1.getStatus() == 4 || param2.getStatus() == 4) { // ???????????????????????????????????????????????????
                parameter.setStatus(4);
            } else if (param1.getStatus() == 7 || param2.getStatus() == 7) { // ???????????????????????????????????????????????????
                parameter.setStatus(7);
            } else {
                parameter.setStatus(1);
            }
        }
    }

    /**
     * ???????????????????????????????????????
     * @param vehicleId
     * @return
     * @throws Exception
     */
    @Override
    public OilVehicleSetting findOilVehicleByVid(String vehicleId) throws Exception {
        if (StringUtils.isEmpty(vehicleId)) {
            return null;
        }

        List<OilVehicleSetting> oilVehicleSettings = new ArrayList<>();
        OilVehicleSettingQuery query = new OilVehicleSettingQuery();
        query.setVehicleId(vehicleId);

        String userUuid = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (userUuid != null && !Objects.equals(userUuid, "") && orgList != null && orgList.size() > 0) {
            oilVehicleSettings = oilVehicleSettingDao.findOilVehicleList(query, userUuid, orgList);
        }

        if (oilVehicleSettings.size() == 0) {
            return null;
        }

        OilVehicleSetting oilVehicleSetting = oilVehicleSettings.get(0);

        boolean isBind = StringUtils.isNotBlank(oilVehicleSetting.getVehicleId()) && StringUtils
            .isNotBlank(oilVehicleSetting.getId());
        // ????????????
        if (isBind) { // ?????????
            setIssuedStatus(oilVehicleSetting);
        }
        return oilVehicleSetting;
    }

    @Override
    public List<DoubleOilVehicleSetting> findReferenceVehicle() throws Exception {
        // ?????????????????????????????????
        Map<String, String> vehicleIds =
            redisVehicleService.getVehicleBindByType(RedisKeys.SensorType.SENSOR_OIL_BOX_MONITOR);
        Set<String> ids = vehicleIds.keySet(); // ???????????????key(????????????id)
        Set<String> assignmentList = userService.getCurrentUserGroupIds(); // ???????????????????????????id
        List<DoubleOilVehicleSetting> doubleList = new ArrayList<>();
        Map<String, BindDTO> vehicleInfos = VehicleUtil.batchGetBindInfosByRedis(ids);// ????????????????????????????????????
        ids.forEach(e -> { // ????????????,????????????
            if (!e.isEmpty()) {
                BindDTO vehicleInfo = vehicleInfos.get(e);
                if (vehicleInfo != null) {
                    String deviceType = vehicleInfo.getDeviceType();// ??????????????????
                    if ("1".equals(deviceType)) {
                        String assignmentId = vehicleInfo.getGroupId(); // ??????id
                        List<String> assigments = new ArrayList<>(Arrays.asList(assignmentId.split(",")));
                        assigments.retainAll(assignmentList);
                        if (CollectionUtils.isNotEmpty(assigments)) {
                            String brand = vehicleInfo.getName(); // ?????????
                            DoubleOilVehicleSetting doubleOil = new DoubleOilVehicleSetting();
                            doubleOil.setVehicleId(e);
                            doubleOil.setBrand(brand);
                            doubleList.add(doubleOil);
                        }
                    }
                }
            }
        });
        return doubleList;
        // List<DoubleOilVehicleSetting> doubleList = new ArrayList<>();
        // List<OilVehicleSetting> list;
        // String userId = SystemHelper.getCurrentUser().getId().toString();
        // // ?????????????????????????????????????????????
        // List<String> orgList = userService.getOrgUuidsByUser(userId);
        // if (userId == null || userId.isEmpty() || orgList == null || orgList.isEmpty()) {
        // return doubleList;
        // }
        // list = oilVehicleSettingDao.findOilBoxVehicle(userService.getUserUuidById(userId), orgList);
        // if (list == null || list.size() == 0) {
        // return doubleList;
        // }
        // for (int i = 0; i < list.size(); i++) {
        // OilVehicleSetting oilVehicle = list.get(i);
        // DoubleOilVehicleSetting doubleOil = new DoubleOilVehicleSetting();
        // boolean doubleFlag = false; // ??????????????????
        // if (oilVehicle.getCheckFlag() == null) {
        // continue; // ???checkFlag = true,?????????????????????????????????
        // }
        // for (int j = i + 1; j < list.size(); j++) {
        // OilVehicleSetting oilVehicle2 = list.get(j);
        // // ??????????????? ?????????
        // if (oilVehicle2.getCheckFlag() != null
        // && oilVehicle.getVehicleId().equals(oilVehicle2.getVehicleId())) {
        // if ("1".equals(oilVehicle.getOilBoxType()) && "2".equals(oilVehicle2.getOilBoxType())) {
        // pushDoubleOil1(oilVehicle, doubleOil); // ????????????????????????
        // pushDoubleOil2(oilVehicle2, doubleOil); // ????????????????????????
        // } else if ("2".equals(oilVehicle.getOilBoxType()) && "1".equals(oilVehicle2.getOilBoxType())) {
        // pushDoubleOil1(oilVehicle2, doubleOil);
        // pushDoubleOil2(oilVehicle, doubleOil);
        // }
        // oilVehicle2.setCheckFlag(null); // ???????????????????????????checkFlag??????null
        // doubleFlag = true; // ???????????????????????????oilFlag??????true,?????????????????????
        // }
        // }
        // if (!doubleFlag) { // ?????????
        // pushDoubleOil1(oilVehicle, doubleOil);
        // }
        // if (!Converter.toBlank(doubleOil.getId()).equals("")) {
        // doubleList.add(doubleOil);
        // }
        // }

    }

    /**
     * TODO ????????????1??????
     * @param oilVehicle
     * @param doubleOil
     * @return void
     * @throws @Title: pushDoubleOil1
     * @author wangying
     */
    public void pushDoubleOil1(OilVehicleSetting oilVehicle, DoubleOilVehicleSetting doubleOil) throws Exception {
        if (oilVehicle != null) {
            doubleOil = doubleOil == null ? new DoubleOilVehicleSetting() : doubleOil;
            doubleOil.assembleMainTank(oilVehicle);
            List<OilCalibrationForm> list = fuelTankManageDao.getOilCalibrationList(oilVehicle.getId());
            if (null != list && list.size() > 0) {
                setOilCalibration(list, doubleOil, "1");
            }
        }
    }

    /**
     * TODO ????????????2??????
     * @param oilVehicle
     * @param doubleOil
     * @return void
     * @throws @Title: pushDoubleOil2
     * @author wangying
     */
    public void pushDoubleOil2(OilVehicleSetting oilVehicle, DoubleOilVehicleSetting doubleOil) throws Exception {
        if (oilVehicle != null) {
            doubleOil = doubleOil == null ? new DoubleOilVehicleSetting() : doubleOil;
            doubleOil.assembleAuxiliaryTank(oilVehicle);
            List<OilCalibrationForm> list = fuelTankManageDao.getOilCalibrationList(oilVehicle.getId());
            if (null != list && list.size() > 0) {
                setOilCalibration(list, doubleOil, "2");
            }
        }
    }

    /**
     * ?????????????????????????????????????????????
     * @param list
     * @param doubleOil
     * @param boxSeq
     * @return void
     * @throws @Title: setOilCalibration
     * @author Liubangquan
     */
    private void setOilCalibration(List<OilCalibrationForm> list, DoubleOilVehicleSetting doubleOil, String boxSeq) {
        StringBuilder oilLevelHeights = new StringBuilder();
        StringBuilder oilValues = new StringBuilder();
        for (OilCalibrationForm form : list) {
            oilLevelHeights.append(form.getOilLevelHeight()).append(",");
            oilValues.append(form.getOilValue()).append(",");
        }
        if ("1".equals(boxSeq)) {
            doubleOil.setOilLevelHeights(Converter.removeStringLastChar(oilLevelHeights.toString()));
            doubleOil.setOilValues(Converter.removeStringLastChar(oilValues.toString()));
        } else if ("2".equals(boxSeq)) {
            doubleOil.setOilLevelHeights2(Converter.removeStringLastChar(oilLevelHeights.toString()));
            doubleOil.setOilValues2(Converter.removeStringLastChar(oilValues.toString()));
        }
    }

    @Override
    public List<OilVehicleSetting> findReferenceBrand() throws Exception {
        List<OilVehicleSetting> list = new ArrayList<>();
        
        list = oilVehicleSettingDao.findVehicleSetting(userService.getCurrentUserUuid());
        List<OilVehicleSetting> referList = new ArrayList<>();
        Set<String> vidSet = new HashSet<>();
        for (OilVehicleSetting setting : list) {
            // ??????????????????
            if (vidSet.add(setting.getVehicleId())) {
                referList.add(setting);
            }
        }
        return referList;
    }

    @Override
    public List<FuelTank> findFuelTankList() throws Exception {
        return oilVehicleSettingDao.findFuelTankList();
    }

    @Override
    public List<RodSensor> findRodSensorList() throws Exception {
        return rodSensorDao.findAllow();
    }

    @Override
    public JsonResultBean addFuelTankBind(DoubleOilVehicleSetting bean, String ipAddress) throws Exception {
        //????????????????????????????????????????????????
        BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(bean.getVehicleId()); // ?????????????????????????????????
        String brand = vehicleInfo.getName();
        String groupName = vehicleInfo.getOrgName();

        boolean isSingleTank = StringUtils.isNotBlank(bean.getOilBoxId()) && StringUtils.isBlank(bean.getOilBoxId2());
        boolean isBinaryTank =
            StringUtils.isNotBlank(bean.getOilBoxId()) && StringUtils.isNotBlank(bean.getOilBoxId2());
        String message = "";
        if (isSingleTank) { // ?????????
            OilVehicleSettingForm oilSettingForm1 = new OilVehicleSettingForm();
            copyoilSettingForm1(bean, oilSettingForm1);
            oilSettingForm1.setId(bean.getId());
            oilSettingForm1.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm1.setCreateDataTime(new Date()); // ????????????
            // ???????????????????????????????????????????????????????????????????????????????????????
            addCalibration(bean, "1");
            boolean flag = oilVehicleSettingDao.addOilSetting(oilSettingForm1);
            if (flag) {
                message = "???????????????" + brand + "( @" + groupName + ")" + "??????????????????";
            }
        } else if (isBinaryTank) { // ?????????
            OilVehicleSettingForm oilSettingForm1 = new OilVehicleSettingForm();
            copyoilSettingForm1(bean, oilSettingForm1);
            oilSettingForm1.setId(bean.getId());
            oilSettingForm1.setSensorType(bean.getSensorType());
            oilSettingForm1.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm1.setCreateDataTime(new Date()); // ????????????

            OilVehicleSettingForm oilSettingForm2 = new OilVehicleSettingForm();
            copyoilSettingForm2(bean, oilSettingForm2);
            oilSettingForm2.setId(bean.getId2());
            oilSettingForm2.setSensorType(bean.getSensorType2());
            oilSettingForm2.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm2.setCreateDataTime(new Date()); // ????????????

            boolean flag = oilVehicleSettingDao.addOilSetting(oilSettingForm1);
            boolean flag1 = oilVehicleSettingDao.addOilSetting(oilSettingForm2);
            if (flag && flag1) {
                message = "???????????????" + brand + "( @" + groupName + ")" + "??????????????????";

            }
            addCalibration(bean, "1");
            addCalibration(bean, "2");
        }
        List<OilVehicleSetting> tanks = oilVehicleSettingDao.findOilBoxVehicleByVid(bean.getVehicleId());
        String value;
        String sensorValue;
        if (tanks.size() == 1) {
            value = getTankBindInfo(tanks.get(0));
            sensorValue = getSensorBindInfo(tanks.get(0));
        } else {
            value = getTankBindInfo(tanks.get(0)) + "," + getTankBindInfo(tanks.get(1));
            sensorValue = getSensorBindInfo(tanks.get(0)) + "," + getSensorBindInfo(tanks.get(1));
        }
        RedisHelper.addToHash(RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST.of(), bean.getVehicleId(), value);
        RedisHelper.addToHash(RedisKeyEnum.VEHICLE_OIL_SENSOR_LIST.of(), bean.getVehicleId(), sensorValue);
        if (!"".equals(message)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private String getTankBindInfo(OilVehicleSetting setting) {
        return setting.getType() + RedisKeys.SEPARATOR + setting.getId();
    }

    private String getSensorBindInfo(OilVehicleSetting setting) {
        return setting.getSensorNumber() + RedisKeys.SEPARATOR + setting.getId();
    }

    /**
     * ?????????????????????????????????
     * @param bean
     * @param type ???????????????????????????1-????????????2-?????????
     * @return void
     * @throws BusinessException
     * @throws @Title:           addCalibration
     * @author Liubangquan
     */
    private void addCalibration(DoubleOilVehicleSetting bean, String type) throws Exception {
        if ("1".equals(type)) {
            /*
             * if (Converter.toBlank(form.getOilBoxId()).equals("")) { form.setOilBoxId(form.getId()); }
             */
            if (Converter.toBlank(bean.getOilLevelHeights()).equals("") && Converter.toBlank(bean.getOilValues())
                .equals("") && !Converter.toBlank(bean.getCalibrationSets()).equals("")) {

                OilVehicleSetting oilVehicleSetting = new OilVehicleSetting();
                oilVehicleSetting.setId(bean.getId()); // ?????????id
                oilVehicleSetting.setBoxLength(bean.getBoxLength()); // ????????????
                oilVehicleSetting.setWidth(bean.getWidth()); // ????????????
                oilVehicleSetting.setHeight(bean.getHeight()); // ????????????
                oilVehicleSetting.setThickness(bean.getThickness()); // ??????
                oilVehicleSetting.setButtomRadius(bean.getButtomRadius()); // ???????????????
                oilVehicleSetting.setTopRadius(bean.getTopRadius()); // ???????????????
                oilVehicleSetting.setRealVolume(bean.getRealVolume()); // ????????????
                oilVehicleSetting.setCalibrationSets(bean.getCalibrationSets()); // ????????????
                oilVehicleSetting.setTheoryVolume(bean.getTheoryVolume()); // ????????????
                oilVehicleSetting.setShape(bean.getShape()); // ????????????
                fuelTankManageService.addOilCalibration(oilVehicleSetting); // ??????????????????????????????
            } else if (!Converter.toBlank(bean.getOilLevelHeights()).equals("") && !Converter
                .toBlank(bean.getOilValues()).equals("")) {
                fuelTankManageDao.deleteOilCalibration(bean.getId());
                String[] heights = bean.getOilLevelHeights().split(",");
                String[] vals = bean.getOilValues().split(",");
                if (heights.length > 0 && vals.length > 0) {
                    for (int i = 0; i < heights.length; i++) {
                        OilCalibrationForm of = new OilCalibrationForm();
                        of.setOilBoxVehicleId(bean.getId());
                        of.setCreateDataUsername(SystemHelper.getCurrentUsername());
                        of.setOilLevelHeight(heights[i]);
                        of.setOilValue(vals[i]);
                        fuelTankManageDao.addOilCalibration(of);
                    }
                }
            }
        }
        if ("2".equals(type)) { // ???????????????
            /*
             * if (Converter.toBlank(form.getOilBoxId()).equals("")) { form.setOilBoxId(form.getId()); }
             */
            if (Converter.toBlank(bean.getOilLevelHeights2()).equals("") && Converter.toBlank(bean.getOilValues2())
                .equals("") && !Converter.toBlank(bean.getCalibrationSets2()).equals("")) {
                OilVehicleSetting oilVehicleSetting = new OilVehicleSetting();
                oilVehicleSetting.setId(bean.getId2()); // ?????????id
                oilVehicleSetting.setBoxLength(bean.getBoxLength2()); // ????????????
                oilVehicleSetting.setWidth(bean.getWidth2()); // ????????????
                oilVehicleSetting.setHeight(bean.getHeight2()); // ????????????
                oilVehicleSetting.setThickness(bean.getThickness2()); // ??????
                oilVehicleSetting.setButtomRadius(bean.getButtomRadius2()); // ???????????????
                oilVehicleSetting.setTopRadius(bean.getTopRadius2()); // ???????????????
                oilVehicleSetting.setRealVolume(bean.getRealVolume2()); // ????????????
                oilVehicleSetting.setCalibrationSets(bean.getCalibrationSets2()); // ????????????
                oilVehicleSetting.setTheoryVolume(bean.getTheoryVolume2()); // ????????????
                oilVehicleSetting.setShape(bean.getShape2()); // ????????????
                fuelTankManageService.addOilCalibration(oilVehicleSetting); // ??????????????????????????????
            } else if (!Converter.toBlank(bean.getOilLevelHeights2()).equals("") && !Converter
                .toBlank(bean.getOilValues2()).equals("")) {
                fuelTankManageDao.deleteOilCalibration(bean.getId2());
                String[] heights = bean.getOilLevelHeights2().split(",");
                String[] vals = bean.getOilValues2().split(",");
                if (null != heights && heights.length > 0 && vals != null && vals.length > 0) {
                    for (int i = 0; i < heights.length; i++) {
                        OilCalibrationForm of = new OilCalibrationForm();
                        of.setOilBoxVehicleId(bean.getId2());
                        of.setCreateDataUsername(SystemHelper.getCurrentUsername());
                        of.setOilLevelHeight(heights[i]);
                        of.setOilValue(vals[i]);
                        fuelTankManageDao.addOilCalibration(of);
                    }
                }
            }
        }
    }

    /**
     * TODO ??????1
     * @param bean
     * @param form
     * @return void
     * @throws @Title: copyoilSettingForm2
     * @author wangying
     */
    public void copyoilSettingForm1(DoubleOilVehicleSetting bean, OilVehicleSettingForm form) {
        form.setOilBoxId(bean.getOilBoxId());
        form.setOilBoxType("1");
        form.setSensorType(bean.getSensorType());
        form.setVehicleId(bean.getVehicleId());
        form.setAutomaticUploadTime(bean.getAutomaticUploadTime());
        form.setOutputCorrectionCoefficientK(bean.getOutputCorrectionCoefficientK());
        form.setOutputCorrectionCoefficientB(bean.getOutputCorrectionCoefficientB());
        form.setAddOilTimeThreshold(bean.getAddOilTimeThreshold());
        form.setAddOilAmountThreshol(bean.getAddOilAmountThreshol());
        form.setSeepOilTimeThreshold(bean.getSeepOilTimeThreshold());
        form.setSeepOilAmountThreshol(bean.getSeepOilAmountThreshol());
        form.setCalibrationSets(bean.getCalibrationSets());
    }

    /**
     * TODO ??????2
     * @param bean
     * @param form
     * @return void
     * @throws @Title: copyoilSettingForm2
     * @author wangying
     */
    public void copyoilSettingForm2(DoubleOilVehicleSetting bean, OilVehicleSettingForm form) {
        form.setOilBoxId(bean.getOilBoxId2());
        form.setOilBoxType("2");
        form.setSensorType(bean.getSensorType());
        form.setVehicleId(bean.getVehicleId());
        form.setAutomaticUploadTime(bean.getAutomaticUploadTime2());
        form.setOutputCorrectionCoefficientK(bean.getOutputCorrectionCoefficientK2());
        form.setOutputCorrectionCoefficientB(bean.getOutputCorrectionCoefficientB2());
        form.setAddOilTimeThreshold(bean.getAddOilTimeThreshold2());
        form.setAddOilAmountThreshol(bean.getAddOilAmountThreshol2());
        form.setSeepOilTimeThreshold(bean.getSeepOilTimeThreshold2());
        form.setSeepOilAmountThreshol(bean.getSeepOilAmountThreshol2());
        form.setCalibrationSets(bean.getCalibrationSets2());
    }

    @Override
    public JsonResultBean deleteFuelTankBindById(String items, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        if (StringUtils.isNotBlank(items)) {
            String[] ids = items.split(",");
            for (int i = 0; i < ids.length; i++) {
                OilVehicleSetting oilVehicle = findOilBoxVehicleByBindId(ids[i]);
                if (oilVehicle == null) {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
                String brand = oilVehicle.getBrand();
                vehicleId = oilVehicle.getVehicleId();
                String type = oilVehicle.getType();
                String numbers = oilVehicle.getSensorNumber();
                if ("2".equals(oilVehicle.getOilBoxType())) { // ????????????2???????????????
                    List<OilVehicleSetting> tanks = findOilBoxVehicleByVid(oilVehicle.getVehicleId());
                    boolean flag = oilVehicleSettingDao.deleteFuelTankBindById(ids[i]);
                    if (flag) {
                        // ????????????
                        message.append("???????????? ??? ").append(brand).append("??????").append(numbers).append(" (???????????????)??? ")
                            .append(type).append(" (??????) <br/>");
                        // ????????????2???????????????????????????
                        fuelTankManageDao.deleteOilCalibration(ids[i]);
                        // ????????????, tanks.get(0)???????????????
                        redisVehicleService
                            .delVehicleTankBind(oilVehicle.getVehicleId(), oilVehicle.getId(), tanks.get(0).getType());
                        redisVehicleService.delVehicleOilSensorBind(oilVehicle.getVehicleId(), oilVehicle.getId(),
                            tanks.get(0).getSensorNumber());
                    }
                }
                // ????????????1
                // ??????????????????????????????
                List<OilVehicleSetting> list = findOilBoxVehicleByVid(oilVehicle.getVehicleId());
                if (list != null && list.size() > 0) {
                    String number = "";
                    String sensorNumber = "";
                    boolean flag = false;
                    boolean flag1 = false;
                    if (list.size() == 1) { // ????????????????????????
                        flag = oilVehicleSettingDao.deleteFuelTankBindById(ids[i]);
                        // ????????????1???????????????????????????
                        fuelTankManageDao.deleteOilCalibration(ids[i]);
                        // ????????????
                        number = list.get(0).getType();
                        sensorNumber = list.get(0).getSensorNumber();
                    } else if (list.size() == 2) { // ????????????????????????1????????????2???type????????????1
                        flag1 = oilVehicleSettingDao.deleteFuelTankBindById(ids[i]);
                        fuelTankManageDao.deleteOilCalibration(ids[i]);
                        for (OilVehicleSetting oil : list) {
                            if (StringUtils.isNotBlank(oil.getId()) && !ids[i].equals(oil.getId())) { // ??????2
                                // ????????????2 type ?????????1
                                oilVehicleSettingDao.updateOil2ToOil1(oil.getId());
                                continue;
                            }
                            number = oil.getType();
                            sensorNumber = oil.getSensorNumber();
                        }
                    }
                    if (flag || flag1) {
                        message.append("???????????? ??? ").append(brand).append("??????").append(numbers).append(" (???????????????)??? ")
                            .append(type).append(" (??????) <br/>");
                    }
                    // ?????????????????????????????????
                    redisVehicleService.delVehicleTankBind(oilVehicle.getVehicleId(), oilVehicle.getId(), number);
                    // ????????????????????????????????????????????????
                    redisVehicleService
                        .delVehicleOilSensorBind(oilVehicle.getVehicleId(), oilVehicle.getId(), sensorNumber);
                }
            }
            if (!"".equals(message.toString())) { // ?????????????????????????????????,?????????????????????
                if (ids.length == 1) {
                    String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleId);
                    logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
                } else {
                    logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "????????????????????????????????????");
                }
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }

        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public DoubleOilVehicleSetting findOilVehicleSettingByVid(String vehicleId) throws Exception {
        if (StringUtils.isNotBlank(vehicleId)) {
            DoubleOilVehicleSetting doubleOil = new DoubleOilVehicleSetting();
            List<OilVehicleSetting> list = oilVehicleSettingDao.findOilBoxVehicleByVid(vehicleId);
            if (list != null && list.size() > 0) {
                if (list.size() == 1) { // ?????????
                    OilVehicleSetting setting = list.get(0);
                    if ("1".equals(setting.getOilBoxType())) { // ??????1
                        pushDoubleOil1(setting, doubleOil); // ????????????????????????
                    } else if ("2".equals(setting.getOilBoxType())) { // ????????????2?????????1
                        oilVehicleSettingDao.updateOil2ToOil1(setting.getOilBoxId());
                        pushDoubleOil1(setting, doubleOil); // ????????????????????????
                    }
                } else if (list.size() == 2) { // ?????????
                    OilVehicleSetting setting1 = list.get(0);
                    OilVehicleSetting setting2 = list.get(1);
                    if ("1".equals(setting1.getOilBoxType()) && "2".equals(setting2.getOilBoxType())) {
                        pushDoubleOil1(setting1, doubleOil); // ????????????1?????????
                        pushDoubleOil2(setting2, doubleOil); // ????????????2?????????
                    } else if ("2".equals(setting1.getOilBoxType()) && "1".equals(setting2.getOilBoxType())) {
                        pushDoubleOil1(setting2, doubleOil); // ????????????1?????????
                        pushDoubleOil2(setting1, doubleOil); // ????????????2?????????
                    }
                }
            }
            return doubleOil;
        }
        return null;
    }

    @Override
    public JsonResultBean updateOilVehicleSetting(DoubleOilVehicleSetting bean, String ipAddress) throws Exception {
        String messae = "";
        // ??????????????????
        String value = null;
        String sensorValue = null;
        String brand = "";
        String plateColor = "";
        String groupName = "";
        BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(bean.getVehicleId());
        if (vehicleInfo != null) {
            brand = vehicleInfo.getName();
            plateColor = String.valueOf(vehicleInfo.getPlateColor());
            groupName = vehicleInfo.getOrgName();
        }
        DoubleOilVehicleSetting beforeSetting = findOilVehicleSettingByVid(bean.getVehicleId());
        if (StringUtils.isNotBlank(beforeSetting.getId()) && StringUtils.isNotBlank(beforeSetting.getId2())
            && StringUtils.isNotBlank(bean.getId()) && StringUtils.isNotBlank(bean.getOilBoxId2()) && StringUtils
            .isNotBlank(bean.getType2())) { // ??????????????????????????????
            OilVehicleSettingForm oilSettingForm1 = new OilVehicleSettingForm();
            OilVehicleSettingForm oilSettingForm2 = new OilVehicleSettingForm();
            copyoilSettingForm1(bean, oilSettingForm1);
            oilSettingForm1.setId(bean.getId());
            oilSettingForm1.setSensorType(bean.getSensorType());
            oilSettingForm1.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm1.setUpdateDataTime(new Date()); // ????????????
            copyoilSettingForm2(bean, oilSettingForm2);
            oilSettingForm2.setId(beforeSetting.getId2());
            oilSettingForm2.setSensorType(bean.getSensorType2());
            oilSettingForm2.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm2.setUpdateDataTime(new Date()); // ????????????
            boolean flag = oilVehicleSettingDao.updateOilSetting(oilSettingForm1);
            boolean flag2 = oilVehicleSettingDao.updateOilSetting(oilSettingForm2);
            addCalibration(bean, "1");
            addCalibration(bean, "2");
            if (flag && flag2) {
                messae = "???????????? ???" + brand + "( @" + groupName + ")" + "????????????????????????(?????????)";
            }
            value = getDoubleTankBindInfo(bean); // ??????
            sensorValue = getDoubleOilSensor(bean); // ???????????????
            // ???????????????????????????
            clearSendStatus(bean.getVehicleId(), bean.getId(), bean.getId2());
        } else if (StringUtils.isNotBlank(beforeSetting.getId()) && StringUtils.isBlank(beforeSetting.getId2())
            && StringUtils.isNotBlank(bean.getId()) && StringUtils.isNotBlank(bean.getOilBoxId2())) {
            // ?????????????????????????????????????????????2???
            OilVehicleSettingForm oilSettingForm1 = new OilVehicleSettingForm();
            OilVehicleSettingForm oilSettingForm2 = new OilVehicleSettingForm();
            copyoilSettingForm1(bean, oilSettingForm1);
            oilSettingForm1.setId(bean.getId());
            oilSettingForm1.setSensorType(bean.getSensorType());
            oilSettingForm1.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm1.setUpdateDataTime(new Date()); // ????????????
            // ????????????2
            copyoilSettingForm2(bean, oilSettingForm2);
            oilSettingForm2.setId(bean.getNewId2()); // ??????id
            oilSettingForm2.setSensorType(bean.getSensorType2());
            oilSettingForm2.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm2.setCreateDataTime(new Date()); // ????????????
            boolean flag = oilVehicleSettingDao.updateOilSetting(oilSettingForm1);
            boolean flag1 = oilVehicleSettingDao.addOilSetting(oilSettingForm2);
            if (flag && flag1) {
                messae = "???????????????" + brand + "( @" + groupName + ")" + "????????????????????????(???????????????)";
            }
            bean.setId2(bean.getNewId2());
            addCalibration(bean, "1");
            addCalibration(bean, "2");
            value = getDoubleTankBindInfo(bean);
            sensorValue = getDoubleOilSensor(bean);
            // ???????????????????????????
            clearSendStatus(bean.getVehicleId(), bean.getId(), bean.getId2());
        } else if (StringUtils.isNotBlank(beforeSetting.getId()) && StringUtils.isBlank(beforeSetting.getId2())
            && StringUtils.isNotBlank(bean.getId()) && StringUtils.isBlank(bean.getOilBoxId2())) {
            // ?????????????????????????????????
            OilVehicleSettingForm oilSettingForm1 = new OilVehicleSettingForm();
            copyoilSettingForm1(bean, oilSettingForm1);
            oilSettingForm1.setId(bean.getId());
            oilSettingForm1.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm1.setUpdateDataTime(new Date()); // ????????????
            boolean flag = oilVehicleSettingDao.updateOilSetting(oilSettingForm1);
            if (flag) {
                messae = "???????????????" + brand + "( @" + groupName + ")" + "????????????????????????(?????????)";
            }
            addCalibration(bean, "1");
            value = getSingleTankBindInfo(bean);
            sensorValue = getSingleOilsensor(bean);
            // ???????????????????????????
            clearSendStatus(bean.getVehicleId(), bean.getId(), bean.getId2());
        } else if (StringUtils.isNotBlank(bean.getType()) && StringUtils.isBlank(bean.getType2())) {
            // ????????????????????????????????????????????????2???
            OilVehicleSettingForm oilSettingForm1 = new OilVehicleSettingForm();
            copyoilSettingForm1(bean, oilSettingForm1);
            oilSettingForm1.setId(bean.getId());
            oilSettingForm1.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
            oilSettingForm1.setUpdateDataTime(new Date()); // ????????????
            boolean flag = oilVehicleSettingDao.updateOilSetting(oilSettingForm1);
            addCalibration(bean, "1");
            // ????????????2
            boolean flag1 = oilVehicleSettingDao.deleteFuelTankBindById(bean.getId2());
            // ????????????2???????????????
            boolean flag2 = fuelTankManageDao.deleteOilCalibration(bean.getId2());
            if (flag && flag1 && flag2) {
                messae = "???????????????" + brand + "( @" + groupName + ")" + "????????????????????????(???????????????)";
            }
            value = getSingleTankBindInfo(bean); // ??????
            sensorValue = getSingleOilsensor(bean); // ???????????????
            // ???????????????????????????
            clearSendStatus(bean.getVehicleId(), bean.getId(), bean.getId2());
        }
        RedisHelper.addToHash(RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST.of(), bean.getVehicleId(), value);
        RedisHelper.addToHash(RedisKeyEnum.VEHICLE_OIL_SENSOR_LIST.of(), bean.getVehicleId(), sensorValue);
        if (!"".equals(messae)) {
            logSearchServiceImpl.addLog(ipAddress, messae, "3", "", brand, plateColor);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private String getDoubleTankBindInfo(DoubleOilVehicleSetting setting) {
        StringBuilder builder = new StringBuilder();
        builder.append(setting.getType()).append(RedisKeys.SEPARATOR).append(setting.getId());
        builder.append(",").append(setting.getType2()).append(RedisKeys.SEPARATOR).append(setting.getId2());
        return builder.toString();
    }

    private String getDoubleOilSensor(DoubleOilVehicleSetting setting) {
        StringBuilder message = new StringBuilder();
        message.append(setting.getSensorNumber()).append(RedisKeys.SEPARATOR).append(setting.getId());
        message.append(",").append(setting.getSensorNumber2()).append(RedisKeys.SEPARATOR).append(setting.getId2());
        return message.toString();
    }

    private String getSingleOilsensor(DoubleOilVehicleSetting setting) { // ??????????????????
        StringBuilder message = new StringBuilder();
        if (StringUtils.isNotBlank(setting.getType()) && StringUtils.isNotBlank(setting.getId())) {
            message.append(setting.getSensorNumber()).append(RedisKeys.SEPARATOR).append(setting.getId());

        }
        if (StringUtils.isNotBlank(setting.getType2()) && StringUtils.isNotBlank(setting.getId2())) {
            message.append(",").append(setting.getSensorNumber2()).append(RedisKeys.SEPARATOR).append(setting.getId2());
        }
        return message.toString();
    }

    private String getSingleTankBindInfo(DoubleOilVehicleSetting setting) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(setting.getType()) && StringUtils.isNotBlank(setting.getId())) {
            builder.append(setting.getType()).append(RedisKeys.SEPARATOR).append(setting.getId());

        }
        if (StringUtils.isNotBlank(setting.getType2()) && StringUtils.isNotBlank(setting.getId2())) {
            builder.append(",").append(setting.getType2()).append(RedisKeys.SEPARATOR).append(setting.getId2());
        }
        return builder.toString();
    }

    /**
     * ???????????????????????????
     * @return
     */
    private void clearSendStatus(String vechiclid, String boxoneId, String boxTwoId) throws Exception {

        String paramType = "F3-8103-4-1"; // ??????
        String calibrationParamType = "F3-8103-5-1"; // ????????????

        if (boxoneId != null && !"".equals(boxoneId)) {
            sendHelper.deleteByVehicleIdParameterName(vechiclid, boxoneId, paramType);
            sendHelper.deleteByVehicleIdParameterName(vechiclid, boxoneId, calibrationParamType);
        }
        if (boxTwoId != null && !"".equals(boxTwoId)) {
            paramType = "F3-8103-4-2"; // ??????
            calibrationParamType = "F3-8103-5-2"; // ????????????
            sendHelper.deleteByVehicleIdParameterName(vechiclid, boxTwoId, paramType);
            sendHelper.deleteByVehicleIdParameterName(vechiclid, boxTwoId, calibrationParamType);
        }
    }

    @Override
    public OilVehicleSetting selectOilVehicleById(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            return oilVehicleSettingDao.selectOilVehicleById(id);
        }
        return null;
    }

    @Override
    public List<OilVehicleSetting> findOilBoxVehicleByVid(String vehicleId) throws Exception {
        if (StringUtils.isNotBlank(vehicleId)) {
            List<OilVehicleSetting> list = oilVehicleSettingDao.findOilBoxVehicleByVid(vehicleId);
            return list;
        }
        return null;
    }

    @Override
    public OilVehicleSetting findOilBoxVehicleByBindId(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            OilVehicleSetting list = oilVehicleSettingDao.findOilBoxVehicleByBindId(id);
            if (list == null) {
                return null;
            }
            list.setBaudRateStr(BaudRateUtil.getBaudRateVal(Integer.valueOf(list.getBaudRate())));
            list.setCompensationCanMakeStr(CompEnUtil.getCompEnVal(list.getCompensationCanMake()));
            list.setFilteringFactorStr(FilterFactorUtil.getFilterFactorVal(Integer.valueOf(list.getFilteringFactor())));
            list.setOddEvenCheckStr(ParityCheckUtil.getParityCheckVal(list.getOddEvenCheck()));
            list.setAutomaticUploadTimeStr(
                UploadTimeUtil.getUploadTimeVal(Integer.parseInt(list.getAutomaticUploadTime())));
            list.setShapeStr(ShapeUtil.getShapeVal(Integer.parseInt(list.getShape())));
            return list;
        }
        return null;
    }

    @Override
    public boolean deleteOilSettingByVid(String vehicleId, Integer type) {
        if (StringUtils.isNotBlank(vehicleId)) {
            if (ConfigUnbindVehicleEvent.TYPE_SINGLE == type) {
                return oilVehicleSettingDao.deleteOilSettingByVid(vehicleId);
            } else {
                List<String> monitorIds = Arrays.asList(vehicleId.split(","));
                return oilVehicleSettingDao.deleteBatchOilSettingByVid(monitorIds);
            }

        }
        return false;
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        oilVehicleSettingDao.deleteBatchOilSettingByVid(monitorIds);
    }

    @Override
    public String sendOil(ArrayList<JSONObject> paramList, String ipAddress) throws Exception {
        StringBuilder msgSN = new StringBuilder();
        StringBuilder msg = new StringBuilder();
        String vehicleId = "";
        if (paramList != null && paramList.size() > 0) {
            for (int i = 0; i < paramList.size(); i++) {
                JSONObject obj = paramList.get(i);
                String parameterName = "";
                String paramId = "";
                String calibrationParamId = "";
                if (obj.get("oilVehicleId") != null) {
                    parameterName = obj.get("oilVehicleId").toString();
                }
                if (obj.get("vehicleId") != null) {
                    vehicleId = obj.get("vehicleId").toString();
                }
                if (obj.get("settingParamId") != null) {
                    paramId = obj.get("settingParamId").toString(); // ????????????
                }
                if (obj.get("calibrationParamId") != null) {
                    calibrationParamId = obj.get("calibrationParamId").toString(); // ????????????
                }
                // ??????
                OilVehicleSetting oilVehicle = findOilBoxVehicleByBindId(parameterName);
                if (oilVehicle != null) {
                    // ????????????
                    String paramType = "F3-8103-4-" + oilVehicle.getOilBoxType(); // ??????
                    String calibrationParamType = "F3-8103-5-" + oilVehicle.getOilBoxType(); // ????????????
                    sendOilVehicleList(oilVehicle); // ??????????????????
                    List<FuelTankForm> calibrationList;
                    // ??????
                    calibrationList = fuelTankServive.getOilCalibrationByBindId(parameterName);
                    String reCalibrationFlag = "";
                    if (obj.get("reCalibrationFlag") != null && !"".equals(obj.get("reCalibrationFlag"))) {
                        reCalibrationFlag = obj.get("reCalibrationFlag").toString();
                    }
                    if (!reCalibrationFlag.isEmpty() && Converter.toBlank(reCalibrationFlag).equals("1") || Converter
                        .toBlank(reCalibrationFlag).equals("2")) { // ?????????????????????????????????????????????
                        // ????????????
                        msgSN.append(sendCalibration(calibrationParamId, vehicleId, calibrationParamType, parameterName,
                            calibrationList)).append(",");
                        if (!oilVehicle.getBrand().isEmpty()) {
                            String message = "";
                            if (Converter.toBlank(reCalibrationFlag).equals("1")) {
                                message = "???????????? :" + oilVehicle.getBrand() + " ????????????1????????????";
                            } else {
                                message = "???????????? :" + oilVehicle.getBrand() + " ????????????2????????????";
                            }
                            String[] vehicle = logSearchServiceImpl.findCarMsg(oilVehicle.getVehicleId());

                            logSearchServiceImpl.addLog(ipAddress, message, "2", "", vehicle[0], vehicle[1]);
                        }
                    } else {
                        // ????????????
                        sendOilTank(paramId, vehicleId, paramType, parameterName, oilVehicle);
                        // ????????????
                        sendCalibration(calibrationParamId, vehicleId, calibrationParamType, parameterName,
                            calibrationList);
                        if (!oilVehicle.getBrand().isEmpty()) {
                            msg.append("???????????? : ").append(oilVehicle.getBrand()).append(" ?????????????????????????????? <br/>");
                        }
                    }
                }
            }
            if (!msg.toString().isEmpty()) {
                if (paramList.size() == 1) { // ????????????
                    String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleId);
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "", vehicle[0], vehicle[1]);
                } else { // ????????????
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "????????????????????????????????????");
                }
            }
        }
        return Converter.removeStringLastChar(msgSN.toString());
    }

    /**
     * ??????????????????
     * @param paramId       ??????id
     * @param vehicleId     ???id
     * @param paramType     ????????????
     * @param parameterName ????????????
     * @return
     * @throws @Title: sendOilTank
     * @author wangying
     */
    public String sendOilTank(String paramId, String vehicleId, String paramType, String parameterName,
        OilVehicleSetting oilVehicle) throws Exception {
        // ???????????????????????????
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        vehicle.setId(vehicleId);
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // ??????????????????
            // ????????????
            int status = 4; // ?????????
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
            // ??????????????????
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);

            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            wsOilSensorCommandService.oilRodSensorCompose(oilVehicle, msgSN, vehicle);
        } else { // ???????????????
            int status = 5; // ???????????????
            msgSN = 0;
            // ??????????????????
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        }
        return Converter.toBlank(msgSN.toString());
    }

    /**
     * ??????????????????
     * @param paramId         ????????????id
     * @param vehicleId       ???id
     * @param paramType       ????????????
     * @param calibrationList ????????????
     * @return String ??????msgSN ???????????????????????????????????????????????????????????????????????????
     * @throws @Title: sendCalibration
     * @author wangying
     */
    public String sendCalibration(String paramId, String vehicleId, String paramType, String parameterName,
        List<FuelTankForm> calibrationList) throws Exception {
        // ???????????????????????????
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        vehicle.setId(vehicleId);
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // ??????????????????
            // msgSN = JTBDeviceManager.getDevice(deveceNumber).getMsgSn();
            // ????????????
            int status = 4; // ?????????
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // ????????????
            wsOilSensorCommandService.markDataCompose(msgSN, vehicle, calibrationList);
            //            updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        } else { // ???????????????
            int status = 5; // ???????????????
            msgSN = 0;
            // ????????????
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        }
        return Converter.toBlank(msgSN);
    }

    /**
     * ????????????????????????
     *
     * @param parameterName ??????id
     * @param replyCode     ????????????code???0 ????????????1 ?????????
     * @return void
     */
    public DirectiveForm generateDirective(String vehicleId, int status, String parameterType, int msgSN,
        String parameterName, int replyCode) {
        DirectiveForm form = new DirectiveForm();
        form.setDownTime(new Date());
        form.setMonitorObjectId(vehicleId);
        form.setStatus(status);
        form.setParameterType(parameterType);
        form.setParameterName(parameterName);
        form.setSwiftNumber(msgSN);
        form.setReplyCode(replyCode);
        return form;
    }

    public void sendOilVehicleList(OilVehicleSetting setting) throws Exception {
        if (setting != null) {
            // ??????????????????
            String fuelType = setting.getFuelOil();
            if ("0#??????".equals(fuelType) || "-10#??????".equals(fuelType) || "-20#??????".equals(fuelType) || "-30#??????"
                .equals(fuelType) || "-50#??????".equals(fuelType)) {
                setting.setFuelOil("01");
            } else if ("89#??????".equals(fuelType) || "90#??????".equals(fuelType) || "92#??????".equals(fuelType) || "93#??????"
                .equals(fuelType) || "95#??????".equals(fuelType) || "97#??????".equals(fuelType) || "98#??????".equals(fuelType)) {
                setting.setFuelOil("02");
            } else if ("LNG".equals(fuelType)) {
                setting.setFuelOil("03");
            } else if ("CNG".equals(fuelType)) {
                setting.setFuelOil("04");
            } else {
                setting.setFuelOil("01");
            }

            // ???????????????????????????????????????0.1?????????
            String addOilAmountThreshol = setting.getAddOilAmountThreshol();
            String seepOilAmountThreshol = setting.getSeepOilAmountThreshol();
            if (StringUtils.isNotBlank(addOilAmountThreshol)) {
                setting.setAddOilAmountThreshol(dfInt.format(Converter.toDouble(addOilAmountThreshol) * 10).toString());
            }
            if (StringUtils.isNotBlank(seepOilAmountThreshol)) {
                setting
                    .setSeepOilAmountThreshol(dfInt.format(Converter.toDouble(seepOilAmountThreshol) * 10).toString());
            }
        }
    }

    @Override
    public List<DoubleOilVehicleSetting> findReferenceVehicleByProtocols(List<Integer> protocols) throws Exception {
        // ?????????????????????????????????
        Map<String, String> vehicleIds =
            redisVehicleService.getVehicleBindByType(RedisKeys.SensorType.SENSOR_OIL_BOX_MONITOR);
        Set<String> ids = vehicleIds.keySet();
        // ???????????????????????????id
        Set<String> userGroupIds = userService.getCurrentUserGroupIds();
        List<DoubleOilVehicleSetting> doubleList = new ArrayList<>();
        ids.forEach(vehicleId -> {
            if (!vehicleId.isEmpty()) {
                final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
                final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "deviceType", "groupId", "name");
                if (vehicleInfo != null) {
                    String deviceType = vehicleInfo.get("deviceType");
                    if (StringUtils.isNotBlank(deviceType) && protocols.contains(Integer.valueOf(deviceType))) {
                        String groupId = vehicleInfo.get("groupId");
                        List<String> groupIds = new ArrayList<>(StringUtils.isBlank(groupId)
                            ? Collections.emptyList() : Arrays.asList(groupId.split(",")));
                        groupIds.retainAll(userGroupIds);
                        if (CollectionUtils.isNotEmpty(groupIds)) {
                            String brand = vehicleInfo.get("name");
                            DoubleOilVehicleSetting doubleOil = new DoubleOilVehicleSetting();
                            doubleOil.setVehicleId(vehicleId);
                            doubleOil.setBrand(brand);
                            doubleList.add(doubleOil);
                        }
                    }
                }
            }
        });
        return doubleList;
    }
}
