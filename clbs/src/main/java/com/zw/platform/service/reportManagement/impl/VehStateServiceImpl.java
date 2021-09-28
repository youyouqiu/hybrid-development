package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.domain.reportManagement.VehPrintDTO;
import com.zw.platform.domain.reportManagement.VehStateContainerDTO;
import com.zw.platform.domain.reportManagement.VehStateExportDTO;
import com.zw.platform.domain.reportManagement.VehStateListDTO;
import com.zw.platform.domain.reportManagement.form.VehBasicDO;
import com.zw.platform.domain.reportManagement.form.VehStateDO;
import com.zw.platform.domain.reportManagement.query.VehStateQuery;
import com.zw.platform.repository.modules.VehStateDao;
import com.zw.platform.service.reportManagement.VehStateService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.TemplateExportExcel;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/13 9:35
 */
@Service
public class VehStateServiceImpl implements VehStateService {
    @Autowired
    private VehStateDao vehStateDao;

    @Autowired
    private OrganizationService organizationService;

    private static Logger logger = LogManager.getLogger(VehStateServiceImpl.class);

    @Autowired
    private TemplateExportExcel templateExportExcel;

    private static final long DAY_SECOND = 86400;

    private static final String format = "yyyyMMddHHmmss";

    private static final String EXPORT_TEMPLATE = "/file/cargoReport/vehStateTemplate.xls";

    private static final String EXPORT_FILE_NAME = "【%s】%s公司 道路运输车辆动态监控记录表";
    private static final String EXPORT_ZIP_NAME = "【%s】道路运输车辆动态监控记录表";

    @Override
    public VehStateListDTO getData(VehStateQuery query) {
        VehStateListDTO vehStateListDTO = new VehStateListDTO();
        vehStateListDTO.setOrgId(query.getOrgId());
        query.init();

        //绑定车辆的基本信息
        Map<String, VehBasicDO> bindVehicleMap = vehStateDao.getBindVehicleMap(ImmutableList.of(query.getOrgId()));
        Set<String> orgVidSet = bindVehicleMap.keySet();

        //企业名称
        vehStateListDTO.setOrgName(organizationService.getOrgNameByUuid(query.getOrgId()));
        //车辆总数
        vehStateListDTO.setTotal(orgVidSet.size());
        Map<String, LocationInfo> lastLocationMap = MonitorUtils.getLastLocationMap(orgVidSet);
        //计算停运数量
        calculateOutOfService(vehStateListDTO, bindVehicleMap);
        //离线24小时逻辑
        if (query.isContainsToday()) {
            //离线24小时车辆数(包含今天直接使用最后一条位置信息计算)
            calOffline(query, vehStateListDTO, lastLocationMap, orgVidSet);
        } else {
            //不包含今天的情况
            dealNoContainsToday(query, vehStateListDTO, orgVidSet);
        }

        VehStateContainerDTO container;
        List<VehStateDO> orgVehStateList = vehStateDao.getSingleOrgVehStateDO(query);

        //计算车辆各个报警以及在线车辆数情况(包含今天的情况)
        if (query.isContainsToday()) {
            //在线数（先通过最后一条位置信息计算出一批车辆id）
            Set<String> onlineVehIdSet = calculateTodayOnline(query.getStartDateSecond(), lastLocationMap);
            container = VehStateContainerDTO.calculateVehNum(orgVehStateList, orgVidSet, onlineVehIdSet);
        } else {
            //完全不包含今天
            container = VehStateContainerDTO.getNoContainsTodayVehNum(orgVehStateList, orgVidSet);
        }
        vehStateListDTO.init(container, bindVehicleMap);
        //存储前端打印需要的非列表字段到redis中，并设置过期时间为30分钟，方便后续前端打印取值，调用接口获取数据
        if (!query.isExport()) {
            initAndSetBrandToRedis(vehStateListDTO, query);
            vehStateListDTO.clearNotListField();
        }
        return vehStateListDTO;
    }

    @Override
    public VehPrintDTO getPrintInfo(String id) {

        String s = RedisHelper.getString(HistoryRedisKeyEnum.VEH_STATE_REPORT_BRAND_KEY.of(id));
        if (StringUtil.isNullOrBlank(s)) {
            return new VehPrintDTO();
        }
        return JSONObject.parseObject(s, VehPrintDTO.class);
    }

    private void initAndSetBrandToRedis(VehStateListDTO vehStateListDTO, VehStateQuery query) {
        VehPrintDTO instance = VehPrintDTO.getInstance(vehStateListDTO, query);
        RedisHelper.setString(HistoryRedisKeyEnum.VEH_STATE_REPORT_BRAND_KEY.of(vehStateListDTO.getId()),
            JSONObject.toJSONString(instance), 1800);
    }

    private void dealNoContainsToday(VehStateQuery query, VehStateListDTO vehStateListDTO, Set<String> orgVidSet) {
        List<VehStateDO> dayOrgVehStateDos =
            vehStateDao.getDayOrgVehStateDo(query.getOrgId(), query.getEndDateStartSecond());
        //在线数计算
        vehStateListDTO.setOnline(dayOrgVehStateDos.size());
        //24小时在线车辆
        int online24 = 0;
        //离线24小时车辆数计算(这两天之内，没有离线24小时的车)
        for (VehStateDO vehStateDO : dayOrgVehStateDos) {
            if (orgVidSet.contains(vehStateDO.getVid())) {
                online24++;

            }
        }
        //离线24小时车辆数(全部车辆减去24小时在线车辆)
        vehStateListDTO.setOffLine(orgVidSet.size() - online24);
    }

    private Set<String> calculateTodayOnline(long startTimeSecond, Map<String, LocationInfo> lastLocationMap) {
        Set<String> onlineVidSet = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        long todayStartSecond = startTimeSecond;
        long todayEndSecond = now.withHour(23).withMinute(59).withSecond(59).toEpochSecond(ZoneOffset.of("+8"));
        for (LocationInfo locationInfo : lastLocationMap.values()) {
            long lastLocationSecond = locationInfo.getLastLocationSecond();
            if (inToday(todayStartSecond, todayEndSecond, lastLocationSecond)) {
                onlineVidSet.add(locationInfo.getMonitorInfo().getMonitorId());
            }
        }
        return onlineVidSet;
    }

    private void calculateOutOfService(VehStateListDTO vehStateListDTO, Map<String, VehBasicDO> bindVehicleMap) {
        for (VehBasicDO vehBasicDO : bindVehicleMap.values()) {
            if (vehBasicDO.isOutOfService()) {
                vehStateListDTO.incrOutOfService();
            }
        }
    }

    /**
     * 在今天之内
     * @param todayStartSecond
     * @param todayEndSecond
     * @param lastLocationSecond
     * @return
     */
    private boolean inToday(long todayStartSecond, long todayEndSecond, long lastLocationSecond) {
        return todayStartSecond <= lastLocationSecond && lastLocationSecond <= todayEndSecond;
    }

    /**
     * 计算离线24小时车辆数
     * @param query
     * @param vehStateListDTO
     * @param lastLocationMap
     * @param orgVidSet
     */
    private void calOffline(VehStateQuery query, VehStateListDTO vehStateListDTO,
        Map<String, LocationInfo> lastLocationMap, Set<String> orgVidSet) {
        for (String vid : orgVidSet) {

            Map<String, VehStateDO> beforeDayVehStateMap = getBeforeDayVehStateDOMap(query);
            LocationInfo locationInfo = lastLocationMap.get(vid);
            //最后一条位置信息为空的情况，需要再判断前一天的数据
            if (locationInfo == null) {
                VehStateDO vehStateDO = beforeDayVehStateMap.get(getKey(vid, query.getOrgId()));
                //判断前一个点和我们结束时间和我们查询的截止时间是否超过24小时
                if (vehStateDO != null && !overOneDay(query, vehStateDO.getPosLast())) {
                    continue;
                }
                vehStateListDTO.incrOffLine();
            } else {
                long lastLocationSecond = locationInfo.getLastLocationSecond();
                //是否超过二十四小时
                if (!overOneDay(query, lastLocationSecond)) {
                    continue;
                }
                vehStateListDTO.incrOffLine();
            }
        }

    }

    private Map<String, VehStateDO> getBeforeDayVehStateDOMap(VehStateQuery query) {
        Map<String, VehStateDO> beforeDayVehStateMap = new HashMap<>();
        List<VehStateDO> beforeDayVehStateList =
            vehStateDao.getDayOrgVehStateDo(query.getOrgId(), query.getBeforeDaySecond());
        for (VehStateDO vs : beforeDayVehStateList) {
            beforeDayVehStateMap.put(getKey(vs.getVid(), vs.getOid()), vs);
        }
        return beforeDayVehStateMap;
    }

    /**
     * 唯一标识
     * @return
     */
    public String getKey(String vid, String orgId) {
        return vid + "_" + orgId;
    }

    private boolean overOneDay(VehStateQuery query, long timeSecond) {
        return query.getEndDateSecond() - timeSecond > DAY_SECOND;
    }

    @Override
    public void export(VehStateQuery query) {
        //初始化导出相关参数
        query.initExport();

        List<VehStateExportDTO> exports = new ArrayList<>();
        List<String> orgIdList = query.getOrgIdList();
        for (String orgId : orgIdList) {
            query.setOrgId(orgId);
            exports.add(VehStateExportDTO.getInstance(getData(query), query));
        }
        List<Map<String, Object>> exportDataList = new ArrayList<>();
        long exportDate = Date8Utils.getValToDay(LocalDateTime.now());
        String zipName = String.format(EXPORT_ZIP_NAME, exportDate);
        Map<String, Integer> exportNameMap = new HashMap<>();
        //开始导出excel
        for (VehStateExportDTO export : exports) {
            String exportName = String.format(EXPORT_FILE_NAME, exportDate, export.getOrgName());
            Map<String, Object> data = new HashMap<>();
            Integer num = exportNameMap.get(exportName);
            if (num != null) {
                data.put("templateSingleFileName", exportName + num);
                num += 1;
            } else {
                data.put("templateSingleFileName", exportName);
                num = 1;

            }
            exportNameMap.put(exportName, num);
            data.put("vs", export);
            exportDataList.add(data);
        }
        templateExportExcel.templateExportExcels(EXPORT_TEMPLATE, getResponse(), exportDataList, zipName);
    }

    @Override
    public void deleteData() {
        //获取四个月前1号的秒值
        LocalDateTime deleteDateTime = LocalDateTime.now().minusMonths(4);
        deleteDateTime = deleteDateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long second = deleteDateTime.toEpochSecond(ZoneOffset.of("+8"));
        logger.info(String.format("车辆状态报表将删除%s（对应的秒值为%s）之前的数据", Date8Utils.getValToDay(deleteDateTime), second));
        List<VehStateDO> dayBeforeData = vehStateDao.getDayBeforeData(second);
        logger.info(String.format("车辆状态报表删除数据的条数为%s", dayBeforeData.size()));
        //分批删除
        List<List<VehStateDO>> partitions = Lists.partition(dayBeforeData, 1000);
        for (List<VehStateDO> partition : partitions) {
            vehStateDao.deleteExpireData(partition);
        }
        logger.info("车辆状态报表删除结束");

    }
}
