package com.zw.platform.service.generalCargoReport.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.generalCargoReport.CargoSearchQuery;
import com.zw.platform.domain.generalCargoReport.CargoSpotCheckForm;
import com.zw.platform.service.generalCargoReport.SpotCheckCargoReortService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.CargoCommonUtils;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SpotCheckCargoReortServiceImpl implements SpotCheckCargoReortService {

    private final Logger logger = LogManager.getLogger(SpotCheckCargoReortServiceImpl.class);

    @Autowired
    UserService userService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    PositionalService positionalService;

    public static String DATE_FORMAT_MIN = "yyyy-MM-dd HH:mm";

    private static int dayLongTime = 24 * 60 * 60;

    private static long FIVE_MIN = 5 * 60;

    @Override
    public Boolean batchDeal(String dealMeasure, String dealResult) throws Exception {
        UserDTO userInfo = userService.getCurrentUserInfo();
        RedisKey redisKey = HistoryRedisKeyEnum.CARGO_SPOT_CHECK_INFORMATION.of(userInfo.getUsername());
        if (!StringUtils.isNotBlank(dealMeasure) && !StringUtils.isNotBlank(dealResult)) {
            RedisHelper.delete(redisKey);
            return true;
        }

        UserDTO userDTO = userService.getUserByUsername(userInfo.getUsername());
        String realName = userDTO.getFullName();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("realName", realName);
        jsonObject.put("dealMeasure", dealMeasure);
        jsonObject.put("dealResult", dealResult);
        RedisHelper.setStringEx(redisKey, jsonObject.toJSONString(), dayLongTime);
        return true;
    }

    @Override
    public Page<CargoSpotCheckForm> searchSpotCheck(CargoSearchQuery cargoSpotCheckQuery) throws Exception {
        Page<CargoSpotCheckForm> result =
            new Page<>(cargoSpotCheckQuery.getPage().intValue(), cargoSpotCheckQuery.getLimit().intValue(), false);
        List<CargoSpotCheckForm> cargoSpotCheckForms = new ArrayList<>();
        long endTime = DateUtil.getStringToLong(cargoSpotCheckQuery.getTime(), DATE_FORMAT_MIN);
        if (System.currentTimeMillis() < endTime) {
            return result;
        }
        long dayStartTime = DateUtil.getStringToLong(cargoSpotCheckQuery.getTime().substring(0, 11) + "00:00:00", null);
        long dayEndTime = dayStartTime + dayLongTime * 1000;
        Set<String> groupIds = new HashSet<>(Arrays.asList(cargoSpotCheckQuery.getGroupIds().split(",")));
        Set<String> allVids = CargoCommonUtils.getGroupCargoVids(groupIds.toArray(new String[] {}));
        List<String> searchGroup = setSearchGroupIds(groupIds, cargoSpotCheckQuery);
        Set<String> vids = CargoCommonUtils.getGroupCargoVids(groupIds.toArray(new String[] {}));
        setSearchVehicleIds(vids, cargoSpotCheckQuery, searchGroup, allVids);
        List<String> pageList = new ArrayList<>(vids);
        try {
            int listSize = pageList.size();
            //当前页
            int curPage = cargoSpotCheckQuery.getPage().intValue();
            //每页条数
            int pageSize = cargoSpotCheckQuery.getLimit().intValue();
            // 遍历开始条数
            int lst = (curPage - 1) * pageSize;
            // 遍历条数
            int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
            VehicleDTO vehicleDTO;
            List<Integer> alarms;

            for (int i = lst; i < ps; i++) {
                try {
                    String vt = pageList.get(i);
                    vehicleDTO = MonitorUtils.getVehicle(vt);
                    String orgId = vehicleDTO.getOrgId();
                    CargoSpotCheckForm cargoSpotCheckForm = getSpotCheckOnline(endTime / 1000L - FIVE_MIN, endTime, vt);
                    if (cargoSpotCheckForm == null) {
                        cargoSpotCheckForm = new CargoSpotCheckForm();
                    } else {
                        cargoSpotCheckForm.setOnlineFlag("1");
                        cargoSpotCheckForm.setAddress(positionalService
                            .getAddress(cargoSpotCheckForm.getLongtitude(), cargoSpotCheckForm.getLatitude()));
                    }
                    String time = cargoSpotCheckQuery.getTime().substring(11, 16).replaceAll(":", "时") + "分";
                    cargoSpotCheckForm.setTime(time);
                    cargoSpotCheckForm.setVehicleId(vt);
                    cargoSpotCheckForm.setBrand(vehicleDTO.getName());
                    cargoSpotCheckForm.setGroupId(orgId);
                    alarms = getDayAlarm(dayStartTime, dayEndTime, vt);
                    setAlarmInfo(alarms, cargoSpotCheckForm);
                    setDealInfo(cargoSpotCheckForm);
                    RedisKey orgKey = RedisKeyEnum.ORGANIZATION_INFO.of(orgId);
                    String orgName = RedisHelper.hget(orgKey, "name");
                    cargoSpotCheckForm.setGroupName(orgName);
                    cargoSpotCheckForms.add(cargoSpotCheckForm);
                } catch (Exception e) {
                    logger.error("查询普货抽查表车" + pageList.get(i) + "数据异常", e);
                }
            }
            result.addAll(cargoSpotCheckForms);
            // 设置总数
            result.setTotal((long) listSize);
        } catch (Exception e) {
            logger.error("查询普货抽查表数据异常", e);
            return result;
        }
        return result;
    }

    private CargoSpotCheckForm getSpotCheckOnline(long startTime, long endTime, String vehicleId) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_SPOT_CHECK_ONLINE, params);
        return PaasCloudUrlUtil.getResultData(str, CargoSpotCheckForm.class);
    }

    private List<Integer> getDayAlarm(long dayStartTime, long dayEndTime, String vehicleId) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", vehicleId);
        params.put("startTime", String.valueOf(dayStartTime));
        params.put("endTime", String.valueOf(dayEndTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_DAY_ALARM, params);
        return PaasCloudUrlUtil.getResultListData(str, Integer.class);
    }

    /**
     * 组装分组名称
     * @param allGroup
     * @throws Exception
     */
    private void installGroupName(List<CargoSpotCheckForm> cargoSpotCheckForms, List<OrganizationLdap> allGroup)
        throws Exception {
        for (CargoSpotCheckForm cargoSpotCheckForm : cargoSpotCheckForms) {
            // 从Ldap中查询出组织名称
            if (allGroup != null && !allGroup.isEmpty() && !Converter.toBlank(cargoSpotCheckForm.getGroupId())
                .equals("")) {
                String groupId = cargoSpotCheckForm.getGroupId();
                String groupName = "";
                for (OrganizationLdap org : allGroup) {
                    if (groupId.equals(org.getUuid())) {
                        groupName = org.getName();
                        break;
                    }
                }
                cargoSpotCheckForm.setGroupName(groupName);
            }
        }
    }

    /**
     * 筛选出模糊搜索的企业id
     */
    private List<String> setSearchGroupIds(Set<String> groupIds, CargoSearchQuery cargoSpotCheckQuery) {
        List<String> searchId = null;
        if (StringUtils.isNotBlank(cargoSpotCheckQuery.getSearch())) {
            searchId = organizationService.getOrgIdsByOrgName(cargoSpotCheckQuery.getSearch(), null);
            if (searchId.size() == 0) {
                return searchId;
            }
            groupIds.retainAll(searchId);
        }
        return searchId;
    }

    /**
     * 筛选出模糊查询的车的id
     */
    private void setSearchVehicleIds(Set<String> vehicleIds, CargoSearchQuery cargoSpotCheckQuery,
        List<String> searchGroup, Set<String> allVids) {
        String monitorName = cargoSpotCheckQuery.getSearch();
        if (StringUtils.isNotBlank(monitorName)) {
            Set<String> queryKeys = MonitorUtils.fuzzySearchAllMonitorIds(monitorName);
            if (searchGroup == null || searchGroup.size() == 0) {
                vehicleIds.retainAll(queryKeys);
            } else {
                allVids.retainAll(queryKeys);
                vehicleIds.addAll(allVids);
            }
        }
    }

    /**
     * 组装超速以及疲劳报警数据
     */
    private void setAlarmInfo(List<Integer> alarms, CargoSpotCheckForm cargoSpotCheckForm) {
        int otherAlarmCount = 0;
        String otherAlarm = "";
        for (Integer alarmType : alarms) {
            if (alarmType == 1) {
                cargoSpotCheckForm.setSpeedFlag("1");
                continue;
            }
            if (alarmType == 2) {
                cargoSpotCheckForm.setFatigueFlag("1");
                continue;
            }
            if (otherAlarmCount < 2) {
                String alarm = AlarmTypeUtil.alarmMap.get(alarmType + "");
                if (alarm != null) {
                    otherAlarm = otherAlarm + AlarmTypeUtil.alarmMap.get(alarmType + "") + ",";
                    otherAlarmCount = otherAlarmCount + 1;
                }
            }
        }
        cargoSpotCheckForm
            .setOtherAlarm(otherAlarm.length() > 0 ? otherAlarm.substring(0, otherAlarm.length() - 1) : otherAlarm);
    }

    /**
     * 设置处理结果
     */
    private void setDealInfo(CargoSpotCheckForm cargoSpotCheckForm) {
        String username = userService.getCurrentUserInfo().getUsername();
        String re =
            RedisHelper.getString(HistoryRedisKeyEnum.CARGO_SPOT_CHECK_INFORMATION.of(username));
        if (re != null) {
            JSONObject jsonObject = JSON.parseObject(re);
            cargoSpotCheckForm.setDealTime(cargoSpotCheckForm.getTime());
            cargoSpotCheckForm.setFeedbackTime(cargoSpotCheckForm.getTime());
            cargoSpotCheckForm.setDealer(jsonObject.getString("realName"));
            cargoSpotCheckForm.setDealMeasure(jsonObject.getString("dealMeasure"));
            cargoSpotCheckForm.setDealResult(jsonObject.getString("dealResult"));
            cargoSpotCheckForm.setDealMin(cargoSpotCheckForm.getTimeMin());
            cargoSpotCheckForm.setDealSec(cargoSpotCheckForm.getTimeSec());
            cargoSpotCheckForm.setFeedbackMin(cargoSpotCheckForm.getTimeMin());
            cargoSpotCheckForm.setFeedbackSec(cargoSpotCheckForm.getTimeSec());
        }
    }

    @Override
    public List<CargoSpotCheckForm> exportSearchSpotCheck(CargoSearchQuery cargoSpotCheckQuery) throws Exception {
        List<CargoSpotCheckForm> cargoSpotCheckForms = new ArrayList<>();
        long endTime = DateUtil.getStringToLong(cargoSpotCheckQuery.getTime(), DATE_FORMAT_MIN);
        long dayStartTime = DateUtil.getStringToLong(cargoSpotCheckQuery.getTime().substring(0, 11) + "00:00:00", null);
        long dayEndTime = dayStartTime + dayLongTime * 1000;
        Set<String> groupIds = new HashSet<>(Arrays.asList(cargoSpotCheckQuery.getGroupIds().split(",")));
        Set<String> allVids = CargoCommonUtils.getGroupCargoVids(groupIds.toArray(new String[] {}));
        List<String> searchGroup = setSearchGroupIds(groupIds, cargoSpotCheckQuery);
        Set<String> vids = CargoCommonUtils.getGroupCargoVids(groupIds.toArray(new String[] {}));
        setSearchVehicleIds(vids, cargoSpotCheckQuery, searchGroup, allVids);
        List<String> pageList = new ArrayList<>(vids);
        try {
            VehicleDTO vehicleDTO;
            List<Integer> alarms;
            for (String vehicleId : pageList) {
                try {
                    vehicleDTO = MonitorUtils.getVehicle(vehicleId);
                    String orgId = vehicleDTO.getOrgId();
                    final long startTime = endTime / 1000L - FIVE_MIN;
                    CargoSpotCheckForm cargoSpotCheckForm = getSpotCheckOnline(startTime, endTime, vehicleId);
                    if (cargoSpotCheckForm == null) {
                        cargoSpotCheckForm = new CargoSpotCheckForm();
                    } else {
                        cargoSpotCheckForm.setOnlineFlag("1");
                        cargoSpotCheckForm.setAddress(positionalService
                                .getAddress(cargoSpotCheckForm.getLongtitude(), cargoSpotCheckForm.getLatitude()));
                    }
                    String[] time = cargoSpotCheckQuery.getTime().substring(11, 16).split(":");
                    cargoSpotCheckForm.setTimeMin(time[0]);
                    cargoSpotCheckForm.setTimeSec(time[1]);
                    cargoSpotCheckForm.setVehicleId(vehicleId);
                    cargoSpotCheckForm.setBrand(vehicleDTO.getName());
                    cargoSpotCheckForm.setGroupId(orgId);
                    alarms = getDayAlarm(dayStartTime, dayEndTime, vehicleId);
                    setAlarmInfo(alarms, cargoSpotCheckForm);
                    setDealInfo(cargoSpotCheckForm);

                    RedisKey orgKey = RedisKeyEnum.ORGANIZATION_INFO.of(orgId);
                    String orgName = RedisHelper.hget(orgKey, "name");
                    cargoSpotCheckForm.setGroupName(orgName);
                    cargoSpotCheckForms.add(cargoSpotCheckForm);
                } catch (Exception e) {
                    logger.error("查询普货抽查表车" + vehicleId + "数据异常", e);
                }
            }
        } catch (Exception e) {
            logger.error("查询普货抽查表导出数据异常", e);
            return cargoSpotCheckForms;
        }
        return cargoSpotCheckForms;
    }

    /**
     * 将用户车id转为byte[]
     */
    private List<byte[]> getByteVids(Set<String> vids) {
        List<byte[]> v = new ArrayList<>();
        for (String vid : vids) {
            v.add(UuidUtils.getBytesFromUUID(UUID.fromString(vid)));
        }
        return v;
    }
}
