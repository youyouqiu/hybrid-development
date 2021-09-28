package com.zw.platform.util;

import com.zw.adas.repository.mysql.riskdisposerecord.AdasAlarmJingParamSettingDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasCommonParamSettingDao;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.repository.core.CustomColumnDao;
import com.zw.platform.service.basicinfo.InitRedisCacheService;
import com.zw.platform.service.schedulingcenter.SchedulingManagementService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 初始化数据到redis
 * @author hjj
 */
@Controller
@RequestMapping("/u/redis")
public class StartDataToRedis {
    private static Logger logger = LogManager.getLogger(StartDataToRedis.class);
    @Autowired
    CustomColumnDao customColumnDao;
    @Autowired
    AdasCommonParamSettingDao commonParamSettingDao;
    @Autowired
    AdasAlarmJingParamSettingDao jingParamSettingDao;

    @Autowired
    private UserService userService;

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SchedulingManagementService schedulingManagementService;
    @Autowired
    private InitRedisCacheService initRedisCacheService;
    /**
     * 山东货运数据报表
     */
    @Value("${cargo.report.switch:false}")
    private boolean cargoReportSwitch;

    /**
     * 初始化行政区划代码
     */
    @ResponseBody
    @RequestMapping(value = { "/initDivisionCode" })
    public JsonResultBean initDivisionCode() {
        logger.info("最新版本缓存里面没有该字段需要改成通过数据库查询方式，进行组装数据！");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping("/init")
    @ResponseBody
    public JsonResultBean configInit() {
        if (!userService.isAdminRole()) {
            return new JsonResultBean(new Exception("权限不足"));
        }

        try {
            initRedisCacheService.addCacheToRedis();
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("初始化redis缓存异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    @RequestMapping(value = { "/initSensor" })
    @ResponseBody
    public JsonResultBean initSensorCache() {
        initRedisCacheService.initBindingSensor();
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @ResponseBody
    @RequestMapping("/initCargoGroupVids")
    public JsonResultBean initCargoGroupVids() {
        try {
            initRedisCacheService.initCargoGroupVids();
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 初始化 redis中默认风险参数定义设置
     * @return
     */
    @ResponseBody
    @RequestMapping("/initRiskEventSetting")
    public JsonResultBean initRiskEventSetting() {
        initRedisCacheService.initRiskEventSetting();
        return new JsonResultBean();
    }

    /**
     * 初始化需要计算离线报表的排班id到redis
     */
    @ResponseBody
    @RequestMapping(value = "/initNeedCalculateOfflineReportScheduledIdToRedis")
    public JsonResultBean initNeedCalculateOfflineReportScheduledIdToRedis() {
        try {
            logger.info("初始化需要计算离线报表的排班id到redis.............start；");
            schedulingManagementService.saveNeedCalculateOfflineReportScheduledIdToRedis();
            logger.info("初始化需要计算离线报表的排班id到redis.............end；");
        } catch (Exception e) {
            logger.error("初始化需要计算离线报表的排班id到redis异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 清除平台从未上过的线的车的垃圾数据
     */
    @ResponseBody
    @RequestMapping(value = "/clearNeverOnlineVehicle")
    public JsonResultBean clearNeverOnlineVehicle() {
        try {
            vehicleService.deleteNeverOnlineVehicle();
        } catch (Exception e) {
            logger.error("删除平台从未上过线的车辆数据失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 用于4.2.3更新redis4分区vc缓存。
     * @return JsonResultBean j
     */
    @ResponseBody
    @RequestMapping(value = "/initVC")
    public JsonResultBean initVCCache() {
        try {
            List<String> sortVid = new ArrayList<>();
            Map<String, String> proNameAndPid = new HashMap<>();
            if (RedisHelper.isContainsKey(RedisKeyEnum.VEHICLE_SORT_LIST.of())) {

                sortVid = RedisHelper.getList(RedisKeyEnum.VEHICLE_SORT_LIST.of());
            }

            //获取key 为cardNumber_proName   value 为 vid 的map
            Map<String, String> proNameAndVid = getVcCacheByVid(sortVid);
            if (MapUtils.isEmpty(proNameAndVid)) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            List<ProfessionalDTO> allProfessionals = newProfessionalsDao.findAllProfessionals();

            if (allProfessionals.size() > 0) {
                for (ProfessionalDTO professionalDTO : allProfessionals) {
                    String identity = professionalDTO.getIdentity();
                    String proName = professionalDTO.getName();
                    String name = identity + "_" + proName;
                    if (StringUtils.isEmpty(proName)) {
                        continue;
                    }
                    if (proNameAndVid.get(name) != null) {
                        proNameAndPid.put(name, professionalDTO.getId());
                    }
                }
            }
            Map<RedisKey, String> vcPid = initVcCahce(proNameAndPid, proNameAndVid);
            RedisHelper.setStringMap(vcPid);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("初始化redis四分区VC缓存异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获取map key为 cardNumber_proName   value 为 vid
     * @param sortVid 所有车辆
     * @return map
     */
    private Map<String, String> getVcCacheByVid(List<String> sortVid) {
        Map<String, String> proNameAndVid = new HashMap<>();
        Map<String, RedisKey> cardNumKeys = sortVid.stream()
                .collect(Collectors.toMap(Function.identity(), HistoryRedisKeyEnum.CARD_NUM_PREFIX::of));
        Map<String, String> cardNumValues = RedisHelper.batchGetStringMap(cardNumKeys);

        if (MapUtils.isEmpty(cardNumValues)) {
            return proNameAndVid;
        }
        try {

            for (Map.Entry<String, String> entry : cardNumValues.entrySet()) {
                String value = entry.getValue();
                if (StringUtils.isNotBlank(value)) {
                    String cardNumberName = value.split(",")[0];
                    //key为 cardNumber_proName   value 为 vid
                    proNameAndVid.put(cardNumberName, entry.getKey());
                }
            }
            return proNameAndVid;
        } catch (Exception e) {
            logger.error("初始化redis四分区VC缓存异常", e);
            return null;
        }
    }

    /**
     * vc_从业人员id换成vp从业人员id
     * 获取存入redis的map  vp_pid
     * @param proNameAndPid key为 cardNumber_proName   value 为 pid
     * @param proNameAndVid key为 cardNumber_proName   value 为 vid
     * @return map
     */
    private Map<RedisKey, String> initVcCahce(Map<String, String> proNameAndPid, Map<String, String> proNameAndVid) {
        Map<RedisKey, String> vcPid = new HashMap<>();
        //要删除的插卡从业人员无效key
        Set<RedisKey> deleteErrorVehicleProfessionalKey = new HashSet<>();
        Map<String, RedisKey> professionalKeys = new HashMap<>();
        try {

            for (Map.Entry<String, String> entry : proNameAndVid.entrySet()) {
                String pid = proNameAndPid.get(entry.getKey());
                if (pid == null) {
                    continue;
                }
                RedisKey key = HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(pid);
                professionalKeys.put(entry.getKey(), key);
                deleteErrorVehicleProfessionalKey.add(key);
            }
            //key为cardNumber_proName，value为55613519463400389595_驾驶员249,1607940016000
            Map<String, String> professionalValueMap = RedisHelper.batchGetStringMap(professionalKeys);
            for (Map.Entry<String, String> entry : professionalValueMap.entrySet()) {
                String value = entry.getValue();
                String cardNumberName = entry.getKey();
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                value = value + "," + proNameAndVid.get(cardNumberName);
                RedisKey redisKey =
                    HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(proNameAndPid.get(cardNumberName));
                vcPid.put(redisKey, value);

            }
            return vcPid;
        } catch (Exception e) {
            logger.error("初始化redis四分区VC缓存异常", e);
            return new HashMap<>();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/clearAbandonedRedis")
    public JsonResultBean clearAbandonedKey() {
        if (!userService.isAdminRole()) {
            return new JsonResultBean(new Exception("权限不足"));
        }
        initRedisCacheService.clearAbandonedRedis();
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }
}
