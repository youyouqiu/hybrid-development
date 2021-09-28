package com.zw.platform.service.switching.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.switching.IoVehicleConfig;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.vas.IoVehicleConfigDao;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.switching.IoVehicleConfigService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangsq
 * @date 2018/6/28 10:19
 */
@Service
public class IoVehicleConfigServiceImpl implements IoVehicleConfigService {

    private static Logger log = LogManager.getLogger(IoVehicleConfigService.class);

    @Autowired
    private IoVehicleConfigDao ioVehicleConfigDao;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchService logSearchService;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private OrganizationService organizationService;

    @Value("${vehicle.set.null}")
    private String vehicleSetNull;

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    @Override
    public JsonResultBean addIoConfigs(List<IoVehicleConfig> ioVehicleConfigs, String ipAddress) throws Exception {
        List<AlarmParameter> alarmParameters = new ArrayList<>();
        for (IoVehicleConfig ioVehicleConfig : ioVehicleConfigs) {
            AlarmParameter alarmParameter = new AlarmParameter();
            alarmParameter.setParamCode("param1");
            String ioSite = ioVehicleConfig.getIoSite().toString();
            String pos = "";
            String name = "I/O" + ioSite;
            pos = getPos(ioVehicleConfig, ioSite, pos);
            alarmParameter.setAlarmTypeId(ioVehicleConfigDao.getAlarmTypeByPosAndName(name, pos));
            alarmParameter.setIoMonitorId(ioVehicleConfig.getVehicleId());
            alarmParameters.add(alarmParameter);
            ioVehicleConfig.setCreateDataTime(new Date());
            ioVehicleConfig.setCreateDataUsername(SystemHelper.getCurrentUsername());
        }
        boolean flag = ioVehicleConfigDao.addBatch(ioVehicleConfigs);
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            ioVehicleConfigDao.addAlarmParameterBatch(alarmParameters);
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(ioVehicleConfigs.get(0).getVehicleId());
            if (bindInfo != null) {
                String number = bindInfo.getName();
                String plateColor = Converter.toBlank(bindInfo.getPlateColor());
                String orgName = bindInfo.getOrgName();
                String msg = "监控对象 : " + number + " ( @" + orgName + ")  绑定io信号位";
                logSearchService.addLog(ipAddress, msg, "3", "io信号位管理", number, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Page<SwitchingSignal> findByPage(SensorConfigQuery query) throws Exception {

        Page<SwitchingSignal> list = new Page<>();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("groupId", query.getGroupId());
            map.put("assignmentId", query.getAssignmentId());
            map.put("query", query.getSimpleQueryParam());

            List<String> vehicleList = redisVehicleService.getUserVehicles(map, null, query.getProtocol());
            if (CollectionUtils.isEmpty(vehicleList)) {
                throw new RedisException(">=======redis 缓存出错了===========<");
            }
            int listSize = vehicleList.size();
            int curPage = query.getPage().intValue();// 当前页
            int pageSize = query.getLimit().intValue(); // 每页条数
            int lst = (curPage - 1) * pageSize;// 遍历开始条数
            int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// 遍历条数

            List<String> vehicles = new ArrayList<String>();

            for (int i = 0; i < vehicleList.size(); i++) {
                if (i >= lst && i < ps) {
                    vehicles.add(vehicleList.get(i));
                }
            }
            List<SwitchingSignal> switchList = ioVehicleConfigDao.findByPageRedis(vehicles);
            VehicleUtil.sort(switchList, vehicles);
            list = RedisQueryUtil.getListToPage(switchList, query, listSize);
        } catch (Exception e) {
            if (e instanceof RedisException) {
                // redis 如果报错，从数据库中获取
                String userId = userService.getCurrentUserUuid();
                // 获取当前用户所属组织及下级组织
                List<String> orgList = userService.getCurrentUserOrgIds();
                if (userId != null && userId != "" && orgList != null && orgList.size() > 0) {
                    list = PageHelperUtil
                        .doSelect(query, () -> ioVehicleConfigDao.findByPage(query, userId, orgList));
                }
            } else {
                log.error("应用管理--->开关信号设置分页查询失败", e);
            }

        }
        if (null != list && list.size() > 0) {
            Set<String> vids = list.stream().map(SwitchingSignal::getVehicleId).collect(Collectors.toSet());
            Map<String, BindDTO> bindInfos = VehicleUtil.batchGetBindInfosByRedis(vids);
            for (SwitchingSignal parameter : list) {
                // 将groupId对应的groupName
                BindDTO bindDTO = bindInfos.get(parameter.getVehicleId());
                if (bindDTO != null) {
                    parameter.setGroups(bindDTO.getOrgName());
                }
            }
        }
        return list;
    }

    @Override
    public Boolean deleteById(String vehicleId, String ipAddress) throws Exception {
        String[] vehicle = logSearchService.findCarMsg(vehicleId);
        if (vehicle != null && vehicle.length > 0) {
            boolean flag = ioVehicleConfigDao.deleteByVehicleId(vehicleId);
            if (flag) {
                List<String> alarmIds = ioVehicleConfigDao.findAlarmParameterIdsByVehicleId(vehicleId);
                //删除车辆io报警参数
                if (alarmIds != null && alarmIds.size() > 0) {
                    ioVehicleConfigDao.delAlarmParaByIds(alarmIds);
                }
                //删除车辆io报警参数设置redis缓存
                RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_IO_ALARM_SETTING.of(vehicleId));
                //删除已设置好的报警参数
                if (alarmIds != null && alarmIds.size() > 0) {
                    ioVehicleConfigDao.delAlarmParaSettingByIds(alarmIds, vehicleId);
                }
                ZMQFencePub.pubChangeFence("18");
                String msg = "监控对象 : " + vehicle[0] + " 删除开关信号设置参数";
                logSearchService.addLog(ipAddress, msg, "3", "开关信号管理", vehicle[0], vehicle[1]);
                return true;
            }
        }
        return false;
    }

    @Override
    public JsonResultBean deleteBatchByIds(List<String> ids, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        StringBuilder brand = new StringBuilder();
        for (String id : ids) {
            String[] vehicle = logSearchService.findCarMsg(id);
            if (vehicle == null || vehicle.length == 0) {
                continue;
            }
            brand.append(vehicle[0]).append(",");
            message.append("监控对象 : ").append(vehicle[0]).append(" 删除开关信号设置参数 <br/>");
        }
        if ("".equals(brand.toString())) {
            return new JsonResultBean(JsonResultBean.FAULT, vehicleSetNull);
        }
        // todo 改为循环外操作MySQL Redis
        for (String vehicleId : ids) {
            List<String> alarmIds = ioVehicleConfigDao.findAlarmParameterIdsByVehicleId(vehicleId);
            //删除车辆io报警参数
            if (alarmIds != null && alarmIds.size() > 0) {
                ioVehicleConfigDao.delAlarmParaByIds(alarmIds);
            }
            //删除车辆io报警参数设置redis缓存
            RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_IO_ALARM_SETTING.of(vehicleId));
            //删除已设置好的报警参数
            if (alarmIds != null && alarmIds.size() > 0) {
                ioVehicleConfigDao.delAlarmParaSettingByIds(alarmIds, vehicleId);
            }
        }
        boolean flag = ioVehicleConfigDao.deleteByVehicleIds(ids);
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除开关信号设置参数");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public List<Map> getVehicleBindIos(String vehicleId, Integer ioType) {
        return ioVehicleConfigDao.getVehicleBindIos(vehicleId, ioType);
    }

    @Override
    public JsonResultBean updateIoConfigs(List<IoVehicleConfig> ioVehicleConfigs, String delIds, String ipAddress,
        String vehicleId) throws Exception {
        // 查询该车辆绑定的io剔除掉新增的,修改删除绑定
        List<String> deleteOldIoIds = new ArrayList<>();
        if (StringUtils.isNotEmpty(vehicleId)) {
            deleteOldIoIds = ioVehicleConfigDao.findIoConfigByVehicleId(vehicleId);
        }
        //新加的报警参数
        List<AlarmParameter> alarmParameters = new ArrayList<>();
        //新加的io参数配置
        List<IoVehicleConfig> newIos = new ArrayList<>();
        //修改的io参数配置
        List<IoVehicleConfig> editIos = new ArrayList<>();
        for (IoVehicleConfig ioVehicleConfig : ioVehicleConfigs) {
            String ioConfigId = ioVehicleConfig.getId();
            IoVehicleConfig io = ioVehicleConfigDao.findById(ioConfigId);
            String vehicleId1 = ioVehicleConfig.getVehicleId();
            if (io != null) {
                ioVehicleConfig.setUpdateDataTime(new Date());
                ioVehicleConfig.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                editIos.add(ioVehicleConfig);
                // 存在该io,则移除,剩下的则需要删除
                deleteOldIoIds.remove(ioConfigId);
            } else {
                ioVehicleConfig.setCreateDataTime(new Date());
                ioVehicleConfig.setCreateDataUsername(SystemHelper.getCurrentUsername());
                newIos.add(ioVehicleConfig);

                AlarmParameter alarmParameter = new AlarmParameter();
                alarmParameter.setParamCode("param1");
                String alarmTypeId = getAlarmTypeId(ioVehicleConfig);
                alarmParameter.setAlarmTypeId(alarmTypeId);
                alarmParameter.setIoMonitorId(vehicleId1);
                alarmParameters.add(alarmParameter);
            }
        }
        //删除报警参数
        if (StringUtils.isNotEmpty(delIds)) {
            List<String> delIdList = Arrays.asList(delIds.split(","));
            delAlarmParameter(delIdList, ioVehicleConfigs.get(0).getVehicleId());
        }
        boolean flag = false;
        if (newIos.size() > 0) {
            flag = ioVehicleConfigDao.addBatch(newIos);
        }

        for (IoVehicleConfig ioVehicleConfig : editIos) {
            flag = ioVehicleConfigDao.updateIoConfig(ioVehicleConfig);
        }
        if (CollectionUtils.isNotEmpty(deleteOldIoIds)) {
            this.delAlarmParameter(deleteOldIoIds, vehicleId);
        }
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            if (alarmParameters.size() > 0) {
                ioVehicleConfigDao.addAlarmParameterBatch(alarmParameters);
            }
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(ioVehicleConfigs.get(0).getVehicleId());
            if (bindInfo != null) {
                String number = bindInfo.getName();
                String orgName = bindInfo.getOrgName();
                String msg = "监控对象 : " + number + " ( @" + orgName + ")  绑定io信号位";
                logSearchService.addLog(ipAddress, msg, "3",
                    "io信号位管理", number, bindInfo.getPlateColor() + "");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private String getAlarmTypeId(IoVehicleConfig ioVehicleConfig) {
        String ioSite = ioVehicleConfig.getIoSite().toString();
        String pos = "";
        String name = "I/O" + ioSite;
        pos = getPos(ioVehicleConfig, ioSite, pos);
        return ioVehicleConfigDao.getAlarmTypeByPosAndName(name, pos);
    }

    private String getPos(IoVehicleConfig ioVehicleConfig, String ioSite, String pos) {
        if (ioVehicleConfig.getIoType() == 1) {
            //终端io
            pos = "140" + (ioSite.length() == 1 ? "0" + ioSite : ioSite);
        } else if (ioVehicleConfig.getIoType() == 2) {
            //io采集1
            pos = "141" + (ioSite.length() == 1 ? "0" + ioSite : ioSite);
        } else if (ioVehicleConfig.getIoType() == 3) {
            //io采集2
            pos = "142" + (ioSite.length() == 1 ? "0" + ioSite : ioSite);
        }
        return pos;
    }

    private void delAlarmParameter(List<String> delIdList, String vehicleId) {
        List<String> delParaIds = new ArrayList<>();
        List<String> delPoss = new ArrayList<>();
        if (delIdList.size() > 0) {
            List<IoVehicleConfig> ioVehicleConfigs = ioVehicleConfigDao.findByIds(delIdList, vehicleId);
            if (ioVehicleConfigs.size() > 0) {
                for (IoVehicleConfig ioVehicleConfig : ioVehicleConfigs) {
                    String alarmTypeId = getAlarmTypeId(ioVehicleConfig);
                    String pos = getPos(ioVehicleConfig, ioVehicleConfig.getIoSite().toString(), "");
                    if (StringUtils.isNotEmpty(alarmTypeId)) {
                        //获取参数报警ID
                        List<String> paraIds = ioVehicleConfigDao
                            .findParaIdByAlarmTypeIdAndIoMonitorId(alarmTypeId, ioVehicleConfig.getVehicleId());
                        if (paraIds != null && paraIds.size() > 0) {
                            delParaIds.addAll(paraIds);
                            delPoss.add(pos);
                        }
                    }
                }
            }
        }
        if (delParaIds.size() > 0) {
            final RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_IO_ALARM_SETTING.of(vehicleId);
            String ioAlarmSetting = RedisHelper.getString(redisKey);
            if (ioAlarmSetting != null) {
                JSONObject jsonObject = JSON.parseObject(ioAlarmSetting);
                if (CollectionUtils.isNotEmpty(delPoss)) {
                    for (String pos : delPoss) {
                        jsonObject.remove(pos);
                    }
                    RedisHelper.setString(redisKey, jsonObject.toJSONString());
                }
            }
            //删除IO参数配置
            ioVehicleConfigDao.delVehicleConfigByIds(delIdList, vehicleId);
            //删除车辆io报警参数
            ioVehicleConfigDao.delAlarmParaByIds(delParaIds);
            //删除已设置好的报警参数
            ioVehicleConfigDao.delAlarmParaSettingByIds(delParaIds, vehicleId);
        }

    }

}
