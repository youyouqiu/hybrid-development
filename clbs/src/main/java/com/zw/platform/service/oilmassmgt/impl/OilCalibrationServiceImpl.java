package com.zw.platform.service.oilmassmgt.impl;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.LastOilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.OilCalibrationDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.oilmassmgt.OilCalibrationService;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.impl.WsOilSensorCommandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 油量标定实现类 <p>Title: OilCalibrationServiceImpl.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author Liubangquan
 * @date 2016年12月14日下午3:26:00
 */
@Service
public class OilCalibrationServiceImpl implements OilCalibrationService {

    @Autowired
    private UserService userService;

    @Autowired
    private OilCalibrationDao oilCalibrationDao;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private WsOilSensorCommandService wsOilSensorCommandService;

    @Override
    public List<OilVehicleSetting> getVehicleList() {
        List<OilVehicleSetting> list = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        if (userId != null && !"".equals(userId) && orgList != null && orgList.size() > 0) {
            list = oilCalibrationDao.getVehicleList(userService.getUserUuidById(userId), orgList);
        }
        // 获取油箱、标定、通讯参数的下发id
        if (null != list && list.size() > 0) {
            for (OilVehicleSetting parameter : list) {
                // 下发状态
                if (StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils
                    .isNotBlank(parameter.getId())) { // 已绑定
                    List<Directive> paramlist1 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), "4"); // 4: 油箱下发
                    List<Directive> paramlist2 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), "5"); // 5: 标定下发
                    List<Directive> paramlist3 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), "7"); // 5: 通讯参数设置
                    Directive param1 = null;
                    Directive param2 = null;
                    Directive param3 = null;
                    if (paramlist1 != null && paramlist1.size() > 0) {
                        param1 = paramlist1.get(0);
                    }
                    if (paramlist2 != null && paramlist2.size() > 0) {
                        param2 = paramlist2.get(0);
                    }
                    if (paramlist3 != null && paramlist3.size() > 0) {
                        param3 = paramlist3.get(0);
                    }
                    if (param1 != null && param2 != null && param3 != null) {
                        parameter.setSettingParamId(param1.getId());
                        parameter.setCalibrationParamId(param2.getId());
                        parameter.setTransmissionParamId(param3.getId());
                        if (param1.getStatus().equals(param2.getStatus()) && param1.getStatus()
                            .equals(param3.getStatus())) {
                            parameter.setStatus(param1.getStatus());
                        } else if (param1.getStatus() == 4 || param2.getStatus() == 4
                            || param3.getStatus() == 4) { // 有一个没有收到回应，则状态为已下发
                            parameter.setStatus(4);
                        } else {
                            parameter.setStatus(1);
                        }
                    }
                }
            }
        }

        return list;
    }

    @Override
    public Map<String, List<OilCalibrationForm>> getOilCalibrationByVid(String vehicleId) {
        Map<String, List<OilCalibrationForm>> map = new HashMap<>();
        List<OilCalibrationForm> ocList = oilCalibrationDao.getOilCalibrationByVid(vehicleId);
        // 解决bug:查询标定时，数据库返回page型数据，只有10条
        // if (!ocList.isEmpty() && (ocList instanceof Page<?>)){
        // ocList = oilCalibrationDao.getOilCalibrationByVid(vehicleId);
        // }
        String oilBoxType = "1"; // 油箱1
        String oilBoxType2 = "2"; // 油箱2
        if (null != ocList && ocList.size() > 0) {
            List<OilCalibrationForm> list1 = new ArrayList<>(); // 油箱1标定数据
            List<OilCalibrationForm> list2 = new ArrayList<>(); // 油箱2标定数据
            for (OilCalibrationForm form : ocList) {
                if (oilBoxType.equals(form.getOilBoxType())) {
                    list1.add(form);
                } else if (oilBoxType2.equals(form.getOilBoxType())) {
                    list2.add(form);
                }
            }
            map.put(oilBoxType, list1);
            map.put(oilBoxType2, list2);
        }
        return map;
    }

    @Override
    public boolean updateOilCalibration(String vehicleId, String oilBoxVehicleIds, String oilBoxVehicleIds2,
        String oilLevelHeights, String oilLevelHeights2, String oilValues, String oilValues2) {
        // 油箱1
        if (StringUtils.isNotBlank(oilBoxVehicleIds) && StringUtils.isNotBlank(oilLevelHeights) && StringUtils
            .isNotBlank(oilValues)) {
            String[] oilLevelHeight = oilLevelHeights.split(",");
            String[] oilValue = oilValues.split(",");
            oilCalibrationDao.deleteOilCalibrationByOilBoxVehicleId(oilBoxVehicleIds);
            for (int i = 0; i < oilLevelHeight.length; i++) {
                OilCalibrationForm form = new OilCalibrationForm();
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                form.setOilBoxVehicleId(oilBoxVehicleIds);
                form.setOilLevelHeight(oilLevelHeight[i]);
                form.setOilValue(oilValue[i]);
                oilCalibrationDao.addOilCalibration(form);
            }
        }
        // 油箱2
        if (StringUtils.isNotBlank(oilBoxVehicleIds2) && StringUtils.isNotBlank(oilLevelHeights2) && StringUtils
            .isNotBlank(oilValues2)) {
            String[] oilLevelHeight = oilLevelHeights2.split(",");
            String[] oilValue = oilValues2.split(",");
            oilCalibrationDao.deleteOilCalibrationByOilBoxVehicleId(oilBoxVehicleIds2);
            for (int i = 0; i < oilLevelHeight.length; i++) {
                OilCalibrationForm form = new OilCalibrationForm();
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                form.setOilBoxVehicleId(oilBoxVehicleIds2);
                form.setOilLevelHeight(oilLevelHeight[i]);
                form.setOilValue(oilValue[i]);
                oilCalibrationDao.addOilCalibration(form);
            }
        }
        return true;
    }

    @Override
    public String getLatestPositional(String vehicleId) {
        // 获取车辆及设备信息
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO == null) {
            return "";
        }
        String deviceNumber = bindDTO.getDeviceNumber();
        // 序列号
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // 设备已经注册
        if (msgSno != null) {
            // 下发参数
            wsOilSensorCommandService.vehicleLocationQuery(msgSno, bindDTO);
        }

        return Converter.toBlank(msgSno);
    }

    @Override
    public String getCalibrationStatusByVid(String vehicleId) {
        List<String> list = oilCalibrationDao.getCalibrationStatusByVid(vehicleId);
        String result = "";
        if (null != list && list.size() > 0) {
            result = list.get(0);
        }
        return result;
    }

    @Override
    public boolean updateCalibrationStatusByVid(String vehicleId, String calibrationStatus) {
        String updateTime = Converter.toString(new Date(), "yyyy-MM-dd HH:mm:ss");
        String updateUserName = Converter.toBlank(SystemHelper.getCurrentUsername());
        return oilCalibrationDao.updateCalibrationStatusByVid(vehicleId, calibrationStatus, updateTime, updateUserName);
    }

    @Override
    public boolean findIsBondOilBox(String vehicleId) {
        int count = oilCalibrationDao.checkIsBondOilBox(vehicleId);
        return count > 0;
    }

    @Override
    public void saveLastCalibration(LastOilCalibrationForm form) {
        oilCalibrationDao.saveLastCalibration(form);
    }

    @Override
    public void deleteLastCalibration(String vehicleId) {
        oilCalibrationDao.deleteLastCalibration(vehicleId);
    }

    @Override
    public List<LastOilCalibrationForm> getLastCalibration(String vehicleId) {
        return oilCalibrationDao.getLastCalibration(vehicleId);
    }

    @Override
    public String getCalibrationUpdateTimeByVid(String vehicleId) {
        List<String> list = oilCalibrationDao.getCalibrationUpdateTimeByVid(vehicleId);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return "";
    }

}
