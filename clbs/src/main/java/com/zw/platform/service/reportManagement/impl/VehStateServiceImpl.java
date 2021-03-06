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

    private static final String EXPORT_FILE_NAME = "???%s???%s?????? ???????????????????????????????????????";
    private static final String EXPORT_ZIP_NAME = "???%s??????????????????????????????????????????";

    @Override
    public VehStateListDTO getData(VehStateQuery query) {
        VehStateListDTO vehStateListDTO = new VehStateListDTO();
        vehStateListDTO.setOrgId(query.getOrgId());
        query.init();

        //???????????????????????????
        Map<String, VehBasicDO> bindVehicleMap = vehStateDao.getBindVehicleMap(ImmutableList.of(query.getOrgId()));
        Set<String> orgVidSet = bindVehicleMap.keySet();

        //????????????
        vehStateListDTO.setOrgName(organizationService.getOrgNameByUuid(query.getOrgId()));
        //????????????
        vehStateListDTO.setTotal(orgVidSet.size());
        Map<String, LocationInfo> lastLocationMap = MonitorUtils.getLastLocationMap(orgVidSet);
        //??????????????????
        calculateOutOfService(vehStateListDTO, bindVehicleMap);
        //??????24????????????
        if (query.isContainsToday()) {
            //??????24???????????????(??????????????????????????????????????????????????????)
            calOffline(query, vehStateListDTO, lastLocationMap, orgVidSet);
        } else {
            //????????????????????????
            dealNoContainsToday(query, vehStateListDTO, orgVidSet);
        }

        VehStateContainerDTO container;
        List<VehStateDO> orgVehStateList = vehStateDao.getSingleOrgVehStateDO(query);

        //???????????????????????????????????????????????????(?????????????????????)
        if (query.isContainsToday()) {
            //??????????????????????????????????????????????????????????????????id???
            Set<String> onlineVehIdSet = calculateTodayOnline(query.getStartDateSecond(), lastLocationMap);
            container = VehStateContainerDTO.calculateVehNum(orgVehStateList, orgVidSet, onlineVehIdSet);
        } else {
            //?????????????????????
            container = VehStateContainerDTO.getNoContainsTodayVehNum(orgVehStateList, orgVidSet);
        }
        vehStateListDTO.init(container, bindVehicleMap);
        //?????????????????????????????????????????????redis??????????????????????????????30??????????????????????????????????????????????????????????????????
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
        //???????????????
        vehStateListDTO.setOnline(dayOrgVehStateDos.size());
        //24??????????????????
        int online24 = 0;
        //??????24?????????????????????(??????????????????????????????24????????????)
        for (VehStateDO vehStateDO : dayOrgVehStateDos) {
            if (orgVidSet.contains(vehStateDO.getVid())) {
                online24++;

            }
        }
        //??????24???????????????(??????????????????24??????????????????)
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
     * ???????????????
     * @param todayStartSecond
     * @param todayEndSecond
     * @param lastLocationSecond
     * @return
     */
    private boolean inToday(long todayStartSecond, long todayEndSecond, long lastLocationSecond) {
        return todayStartSecond <= lastLocationSecond && lastLocationSecond <= todayEndSecond;
    }

    /**
     * ????????????24???????????????
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
            //???????????????????????????????????????????????????????????????????????????
            if (locationInfo == null) {
                VehStateDO vehStateDO = beforeDayVehStateMap.get(getKey(vid, query.getOrgId()));
                //?????????????????????????????????????????????????????????????????????????????????24??????
                if (vehStateDO != null && !overOneDay(query, vehStateDO.getPosLast())) {
                    continue;
                }
                vehStateListDTO.incrOffLine();
            } else {
                long lastLocationSecond = locationInfo.getLastLocationSecond();
                //???????????????????????????
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
     * ????????????
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
        //???????????????????????????
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
        //????????????excel
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
        //??????????????????1????????????
        LocalDateTime deleteDateTime = LocalDateTime.now().minusMonths(4);
        deleteDateTime = deleteDateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long second = deleteDateTime.toEpochSecond(ZoneOffset.of("+8"));
        logger.info(String.format("???????????????????????????%s?????????????????????%s??????????????????", Date8Utils.getValToDay(deleteDateTime), second));
        List<VehStateDO> dayBeforeData = vehStateDao.getDayBeforeData(second);
        logger.info(String.format("??????????????????????????????????????????%s", dayBeforeData.size()));
        //????????????
        List<List<VehStateDO>> partitions = Lists.partition(dayBeforeData, 1000);
        for (List<VehStateDO> partition : partitions) {
            vehStateDao.deleteExpireData(partition);
        }
        logger.info("??????????????????????????????");

    }
}
