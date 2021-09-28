package com.zw.platform.push.controller;

import com.github.pagehelper.Page;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.result.UserMenuDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.VehicleInsuranceInfo;
import com.zw.platform.domain.basicinfo.query.VehicleInsuranceQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.modules.VehicleInsuranceDao;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.ws.common.PublicVariable;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID;
import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.EXPIRE_DRIVING_LICENSE;
import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.EXPIRE_INSURANCE_ID;
import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.EXPIRE_MAINTENANCE;
import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.EXPIRE_ROAD_TRANSPORT;
import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.LIFECYCLE_EXPIRE_LIST;
import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.UNHANDLED_VEHICLE;
import static com.zw.platform.util.MenuConstants.MENU_CONNECTION_PARAMS;
import static com.zw.platform.util.MenuConstants.MENU_INSURANCE_MANAGEMENT;
import static com.zw.platform.util.MenuConstants.MENU_LIFECYCLE_STATISTIC;
import static com.zw.platform.util.MenuConstants.MENU_VEHICLE_MANAGEMENT;

@Controller
@Log4j2
public class WsControlController {

    public static String userName = "";

    private static final String LIST_PAGE = "test/list";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private OfflineExportService offlineExportService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private VehicleInsuranceDao vehicleInsuranceDao;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    private RoleService roleService;

    @RequestMapping(value = { "add/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        if (!userService.isAdminRole()) {
            throw new BusinessException("权限不足");
        }
        return LIST_PAGE;
    }

    /**
     * 订阅当前用户状态
     */
    @RequestMapping(value = { "/subscribe/global" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean subscribeGlobal(String userName) {
        if (userName == null) {
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        Set<String> vehicles = userService.getMonitorIdsByUser(userName);
        List<String> result = new ArrayList<>();
        Set<String> sets = new HashSet<>();
        if (CollectionUtils.isNotEmpty(vehicles)) {
            this.addSubscribeVehicles(vehicles, sets);
            final Set<String> todayUnhandledVehicles = RedisHelper.getSet(UNHANDLED_VEHICLE.of());
            if (CollectionUtils.isNotEmpty(todayUnhandledVehicles)) {
                vehicles.retainAll(todayUnhandledVehicles);
                result.addAll(vehicles);
            }
        }

        final UserMenuDTO userMenuDTO = userService.loadUserPermission(userName);
        boolean careMaintenanceExpire = userMenuDTO.getMenuIds().contains(MENU_VEHICLE_MANAGEMENT);
        boolean careInsuranceExpire = userMenuDTO.getMenuIds().contains(MENU_INSURANCE_MANAGEMENT);
        boolean careServiceExpire = userMenuDTO.getMenuIds().contains(MENU_LIFECYCLE_STATISTIC);
        boolean care809Broke = userMenuDTO.getMenuIds().contains(MENU_CONNECTION_PARAMS);

        Map<String, Object> resultMap = new HashMap<>(16);
        String userDn = null;
        final UserDTO user = userMenuDTO.getUserInfo();
        if (user != null) {
            userDn = user.getId().toString();
        }

        if (userDn != null) {
            countVehicleExpireRemind(resultMap, vehicles, careMaintenanceExpire);
            countVehicleInsuranceExpireRemind(resultMap, user, careInsuranceExpire);
            countLifecycleExpireRemind(resultMap, userName, careServiceExpire);

            setT809OfflineReconnectMsg(resultMap, userDn, care809Broke);

        }
        resultMap.put("result", result);
        return new JsonResultBean(resultMap);
    }

    /**
     * 离线报表导出查询
     */
    @RequestMapping(value = { "/vehicle/offlineExport" }, method = RequestMethod.POST)
    @ResponseBody
    public void pushOfflineExport(String id) {
        List<String> userNames = offlineExportService.findUserNameByDigestId(id);
        OfflineExportInfo info = offlineExportService.getInfoByDigestId(id);
        Map<String, String> sendMap = new HashMap<>();
        if (info != null) {
            if (sslEnabled) {
                info.setAssemblePath("/" + info.getAssemblePath());
            } else {
                info.setAssemblePath(webServerUrl + info.getAssemblePath());
            }
            sendMap.put("id", id);
            sendMap.put("url", info.getAssemblePath());
        }
        if (CollectionUtils.isNotEmpty(userNames)) {
            for (String userName : userNames) {
                simpMessagingTemplate.convertAndSendToUser(userName, ConstantUtil.WEB_SOCKET_OFFLINE_EXPORT, sendMap);
            }
        }
    }

    /**
     * 根据redis中key搜索匹配的缓存值
     * @param assignedVehicles 当前用户有权限的车辆id列表
     * @param result           满足条件的车辆id列表
     */
    private void addSubscribeVehicles(Set<String> assignedVehicles, Collection<String> result) {
        Pattern splitter = Pattern.compile(ALARM_PUSH_SET_MONITOR_ID.of("").get());
        List<String> vehicleIdList = RedisHelper.scanKeys(ALARM_PUSH_SET_MONITOR_ID.of("*"));
        for (String alarmVehicle : vehicleIdList) {
            String vehicleId = splitter.split(alarmVehicle)[1];
            if (assignedVehicles.contains(vehicleId)) {
                result.add(vehicleId);
            }
        }
    }

    /**
     * 统计车辆到期的提醒
     * 行驶证到期车辆数、 运输证到期车辆数、 车辆保养到期车辆数
     */
    private void countVehicleExpireRemind(Map<String, Object> resultMap, Set<String> userOwnVehicleIdList,
                                          boolean careMaintenanceExpire) {
        // 车辆到期数量
        long drivingLicenseExpireNum = 0;
        long roadTransportExpireNum = 0;
        long maintenanceExpireNum = 0;
        if (CollectionUtils.isNotEmpty(userOwnVehicleIdList)) {
            // 行驶证即将到期的车辆
            List<String> expireDrivingLicenseList = RedisHelper.getSetFromString(EXPIRE_DRIVING_LICENSE.of());
            drivingLicenseExpireNum = filterVehicleByOwnVehicleIds(userOwnVehicleIdList, expireDrivingLicenseList);
            // 运输证即将到期的车辆
            List<String> expireRoadTransportList = RedisHelper.getSetFromString(EXPIRE_ROAD_TRANSPORT.of());
            roadTransportExpireNum = filterVehicleByOwnVehicleIds(userOwnVehicleIdList, expireRoadTransportList);
            // 车辆保养到期的车辆
            if (careMaintenanceExpire) {
                List<String> expireMaintenanceList = RedisHelper.getSetFromString(EXPIRE_MAINTENANCE.of());
                maintenanceExpireNum = filterVehicleByOwnVehicleIds(userOwnVehicleIdList, expireMaintenanceList);
            }
        }
        resultMap.put("expireDrivingLicenseList", drivingLicenseExpireNum);
        resultMap.put("expireRoadTransportList", roadTransportExpireNum);
        resultMap.put("expireMaintenanceList", maintenanceExpireNum);
    }

    /**
     * 计算当前用户下的即将过期的车辆保险数据
     * @param resultMap resultMap
     * @param user      用户信息
     */
    private void countVehicleInsuranceExpireRemind(Map<String, Object> resultMap, UserDTO user,
                                                   boolean careInsuranceExpire) {
        try {
            int size = 0;
            if (careInsuranceExpire) {
                List<String> insuranceList = RedisHelper.getList(EXPIRE_INSURANCE_ID.of());
                if (CollectionUtils.isNotEmpty(insuranceList)) {
                    VehicleInsuranceQuery query = new VehicleInsuranceQuery();
                    query.setInsuranceList(insuranceList);
                    query.setUserUUID(user.getUuid());
                    List<String> orgIdList = organizationService.getOrgUuidsByUser(user.getId().toString());
                    query.setGroupList(orgIdList);
                    Page<VehicleInsuranceInfo> vehicleInsuranceList =
                            vehicleInsuranceDao.findVehicleInsuranceList(query);
                    size = vehicleInsuranceList.size();
                }
            }
            resultMap.put("expireInsuranceIdList", size);
        } catch (Exception e) {
            log.error("获取车辆保险即将到期数据异常", e);
        }
    }

    private void countLifecycleExpireRemind(Map<String, Object> resultMap, String username, boolean careServiceExpire) {
        resultMap.put("lifecycleExpireNumber", 0);
        if (!careServiceExpire) {
            return;
        }
        boolean containsKey = RedisHelper.isContainsKey(LIFECYCLE_EXPIRE_LIST.of());
        if (containsKey) {
            List<String> expireMonitorIds = null;
            try {
                expireMonitorIds = RedisHelper.getList(LIFECYCLE_EXPIRE_LIST.of(), 0, -1);
            } catch (Exception e) {
                log.error("查询用户权限下的即将到期监控对象异常", e);
            }
            if (CollectionUtils.isEmpty(expireMonitorIds)) {
                return;
            }
            // 当前用户权限下的车
            final Set<String> userVehicles = userService.getMonitorIdsByUser(username);
            if (!userVehicles.isEmpty()) {
                userVehicles.retainAll(new HashSet<>(expireMonitorIds));
                resultMap.put("lifecycleExpireNumber", userVehicles.size());
            }
        }
    }

    private void setT809OfflineReconnectMsg(Map<String, Object> resultMap, String userDn, boolean care809Broke) {
        try {
            String t809OfflineReconnectStatus = "true";
            if (care809Broke) {
                OrganizationLdap orgByEntryDn =
                        organizationService.getOrgByEntryDn(userService.getUserOrgDnByDn(userDn));
                String orgUuid = orgByEntryDn.getUuid();
                if (StrUtil.isNotBlank(orgUuid)) {
                    t809OfflineReconnectStatus = String.valueOf(PublicVariable.checkConnectionStatus(
                            connectionParamsSetDao.getT809ConnectionStatusByGroupId(orgUuid)));
                }
            }
            resultMap.put("t809OfflineReconnectStatus", t809OfflineReconnectStatus);
        } catch (Exception e) {
            log.error("出现异常了", e);
        }
    }

    /**
     * 根据用户拥有的车辆过滤
     */
    private long filterVehicleByOwnVehicleIds(Set<String> owned, List<String> filtering) {
        return filtering.stream().filter(owned::contains).count();
    }
}
