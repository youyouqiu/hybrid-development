package com.zw.talkback.util.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.ConcurrentHashSet;
import com.zw.platform.util.common.ZipUtil;
import com.zw.talkback.util.TalkCallUtil;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import com.zw.ws.entity.vehicle.VehiclePositionalInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InitializationData implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LogManager.getLogger(ApplicationListener.class);

    /**
     * spring容器初始化后加载一些数据
     */

    public static Set<VehiclePositionalInfo> vehiclePositionalInfo = new ConcurrentHashSet<>();

    public static Map<String, Set<String>> orgUUidMap = new ConcurrentHashMap<>();

    public static Set<String> onlineIds = new ConcurrentHashSet<>();

    @Autowired
    private TalkCallUtil talkCallUtils;

    @Value("${supervise.es.prefix:#{\"\"}}")
    public String supervisePrefix;

    public static String prefix;

    @Value("${module.talk.enable:false}")
    private boolean enabled;

    @Autowired
    private UserService userService;

    @PostConstruct
    private void initPrefix() {
        if (Objects.equals(supervisePrefix, "default")) {
            prefix = "";
            return;
        }
        if (!StringUtils.isEmpty(supervisePrefix) && !supervisePrefix.contains("_")) {
            prefix = supervisePrefix + "_";
            return;
        }
        prefix = supervisePrefix;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!enabled) {
            return;
        }
        //只初始化一次(因为spring的listener父容器会初始化一次，spring-servlet也会初始化一次，这个对我们业务逻辑会造成重复数据)
        if (event.getApplicationContext().getParent() == null) {
            // 初始化车辆部分位置信息到内存中
            try {
                initVehiclePositionalInfo();
                initOrgMap();
                initOnline();
                //系统启动登录使用一级客户账号登录到对讲平台
                talkCallUtils.getNewFirstCustomerPid();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initOnline() {
        try {
            final List<String> vehicleStatuses =
                    RedisHelper.getStringByPattern(HistoryRedisKeyEnum.FUZZY_VEHICLE_STATUS.of());
            for (String vehicleStatus : vehicleStatuses) {
                ClientVehicleInfo clientVehicleInfo = JSON.parseObject(vehicleStatus, ClientVehicleInfo.class);
                //3离线11心跳，在林业巡检均代表离线
                if (clientVehicleInfo.getVehicleStatus() != 3 && clientVehicleInfo.getVehicleStatus() != 11) {
                    onlineIds.add(clientVehicleInfo.getVehicleId());
                }
            }
        } catch (Exception e) {
            log.error("初始化在线监控对象ids异常", e);
        }
    }

    public void initOrgMap() {
        List<OrganizationLdap> allOrganization = userService.getAllOrganization();
        orgUUidMap.clear();
        for (OrganizationLdap organizationLdap : allOrganization) {
            List<OrganizationLdap> orgChild = userService.getOrgChild(organizationLdap.getId().toString());
            if (orgChild != null) {
                orgUUidMap.put(organizationLdap.getUuid(), new HashSet<>());
                for (OrganizationLdap org : orgChild) {
                    orgUUidMap.get(organizationLdap.getUuid()).add(org.getUuid());
                }
            }
        }
    }

    private void initVehiclePositionalInfo() {
        String vehiclePositional =
                RedisHelper.getString(HistoryRedisKeyEnum.ALL_MONITOR_POSITION_DATA_ZIP.of());
        if (!StringUtils.isEmpty(vehiclePositional)) {
            // 进行解压
            String str = ZipUtil.gunzip(vehiclePositional);
            // 使用json进行转换
            List<VehiclePositionalInfo> list = JSONObject.parseArray(str, VehiclePositionalInfo.class);
            vehiclePositionalInfo.addAll(list);
        }

    }

    /**
     * 接收到车辆位置信息后更新
     * @param vehiclePositional
     */
    public void addVehiclePositionalInfo(VehiclePositionalInfo vehiclePositional) {
        boolean success = vehiclePositionalInfo.add(vehiclePositional);
        if (!success) {
            vehiclePositionalInfo.remove(vehiclePositional);
            vehiclePositionalInfo.add(vehiclePositional);
        }
    }

}
