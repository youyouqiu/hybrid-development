package com.zw.adas.service.driverScore.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.zw.adas.domain.driverScore.show.AdasDriverGroupGeneralScoreListExport;
import com.zw.adas.domain.driverScore.show.AdasDriverGroupGeneralScoreListShow;
import com.zw.adas.domain.driverScore.show.AdasDriverGroupGeneralScoreShow;
import com.zw.adas.domain.driverScore.show.AdasDriverScoreEventShow;
import com.zw.adas.domain.driverScore.show.AdasDriverScoreProfessionalInfoExport;
import com.zw.adas.domain.driverScore.show.AdasDriverScoreProfessionalInfoShow;
import com.zw.adas.domain.driverScore.show.query.AdasDriverScoreQuery;
import com.zw.adas.domain.driverStatistics.show.AdasProfessionalShow;
import com.zw.adas.repository.mysql.driverscore.AdasDriverScoreDao;
import com.zw.adas.service.driverScore.AdasDriverScoreService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ProfessionalShowDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.query.IcCardDriverQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.excel.TemplateExportExcel;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/***
 @Author zhengjc
 @Date 2019/10/14 19:45
 @Description 司机评分service具体事项
 @version 1.0
 **/

@Service
public class AdasDriverScoreServiceImpl implements AdasDriverScoreService {

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AdasDriverScoreDao adasDriverScoreDao;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @Autowired
    private ProfessionalService professionalService;

    @Override
    public AdasDriverGroupGeneralScoreShow getGroupDriverGeneralScoreInfos(String groupId, long time) {
        AdasDriverGroupGeneralScoreShow result = adasDriverScoreDao.getGroupDriverGeneralScoreInfos(groupId, time);
        Integer scoreRange = adasDriverScoreDao.getMaxRangeByGroupIdsAndTime(groupId, time);
        if (result != null) {
            result.setMaxScoreRange(scoreRange);
        }
        return result;
    }

    @Override
    public List<AdasDriverGroupGeneralScoreListShow> getGroupDriverGeneralScoreInfoList(AdasDriverScoreQuery query) {
        List<AdasDriverGroupGeneralScoreListShow> results =
            adasDriverScoreDao.getGroupDriverGeneralScoreInfoList(query);
        assembleData(results);

        return results;
    }

    /**
     * 查询从业人员企业
     */
    private void assembleData(List<AdasDriverGroupGeneralScoreListShow> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }

        List<IcCardDriverQuery> list = results.stream()
                .map(e -> IcCardDriverQuery.getInstance(e.getDriverNameCardNumber()))
                .collect(Collectors.toList());
        Map<String, AdasProfessionalShow> driverInfoMap = getAdasProfessionalShowMap(list);

        AdasProfessionalShow driver;
        for (AdasDriverGroupGeneralScoreListShow result : results) {
            driver = driverInfoMap.get(result.getDriverNameCardNumber());
            if (driver != null) {
                result.setDriverNameCardNumberVal(driver.getCardNumber() + "_" + driver.getName());
                result.setGroupName(driver.getGroupName());
                result.setDriverGroupId(driver.getGroupId());
            }
        }
    }

    private Map<String, AdasProfessionalShow> getAdasProfessionalShowMap(
        List<IcCardDriverQuery> icCardDriverQueryList) {
        Map<String, ProfessionalShowDTO> professionalShowMap =
            professionalService.getProfessionalShowMaps(icCardDriverQueryList);
        return AdasProfessionalShow.convertProfessionalMaps(professionalShowMap);
    }

    @Override
    public AdasDriverScoreProfessionalInfoShow getDriverScoreProfessionalInfo(AdasDriverScoreQuery query) {
        query.initParam();
        AdasDriverScoreProfessionalInfoShow show = adasDriverScoreDao.getDriverScoreProfessionalInfo(query);
        if (show != null) {
            Map<String, AdasProfessionalShow> professionalShowMap =
                getAdasProfessionalShowMap(Arrays.asList(IcCardDriverQuery.getInstance(query.getCardNumberName())));
            show.setAdasProfessionalShow(professionalShowMap.get(query.getCardNumberName()));
        } else {
            show = new AdasDriverScoreProfessionalInfoShow();
            show.setAdasProfessionalShow(new AdasProfessionalShow());
            return show;
        }
        List<Map<String, String>> cls = new ArrayList<>();
        show.setEventInfos(JSONObject.parseObject(show.getEventInfoStr(), cls.getClass()));
        show.getEventInfos()
            .sort((o1, o2) -> Integer.valueOf(o2.get("value")).compareTo(Integer.valueOf(o1.get("value"))));

        return show;
    }

    @Override
    public List<AdasDriverScoreEventShow> getIcCardDriverEvents(AdasDriverScoreQuery query) {

        query.setGroupIds(organizationService.getChildOrgIdByUuid(query.getGroupId()));
        List<String> ids = adasElasticSearchUtil.getDriverDetailResponse(query);

        return getDriverEventFromPass(ids);
    }

    private List<AdasDriverScoreEventShow> getDriverEventFromPass(List<String> eventIds) {
        Map<String, String> param = new HashMap<>();
        param.put("eventIdStr", JSONObject.toJSONString(eventIds));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_IC_CARD_DRIVER_EVENT_LIST, param);
        List<AdasDriverScoreEventShow> listResult =
            PassCloudResultUtil.getListResult(sendResult, AdasDriverScoreEventShow.class);
        for (AdasDriverScoreEventShow show : listResult) {
            if (StrUtil.isBlank(show.getMonitorName())) {
                continue;
            }
            String riskType = adasCommonHelper.geRiskTypeName(show.getEvent());
            String riskEvent = adasCommonHelper.geEventName(show.getEvent());
            show.setEvent(riskEvent + "(" + riskType + ")");
            show.setRiskLevel(adasCommonHelper.geRiskLevel(show.getRiskLevel()));
        }
        return listResult;
    }

    @Override
    public Map<String, String> getGroupIdMap() {
        Map<String, String> groupIdsMap = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        RedisKey groupChildKey = HistoryRedisKeyEnum.GROUP_CHILD_IDS.of(Date8Utils.getValToMonth(now));
        List<OrganizationLdap> allOrganization = organizationService.getAllOrganization();
        for (OrganizationLdap organization : allOrganization) {
            List<String> orgChildByOrgUuid = organizationService.getChildOrgIdByUuid(organization.getUuid());
            groupIdsMap.put(organization.getUuid(), JSON.toJSONString(orgChildByOrgUuid));
        }
        RedisHelper.addToHash(groupChildKey, groupIdsMap);
        return groupIdsMap;
    }

    @Override
    public void exportIcCardDriverInfoList(AdasDriverScoreQuery adasDriverScoreQuery, HttpServletResponse response) {
        List<AdasDriverGroupGeneralScoreListShow> groupDriverScoreList =
            getGroupDriverGeneralScoreInfoList(adasDriverScoreQuery);
        if (CollectionUtils.isEmpty(groupDriverScoreList)) {
            return;
        }
        List<AdasDriverGroupGeneralScoreListExport> exports = new ArrayList<>();
        int number = 0;
        for (AdasDriverGroupGeneralScoreListShow groupDriverScore : groupDriverScoreList) {
            exports.add(AdasDriverGroupGeneralScoreListExport.getInstance(groupDriverScore, ++number));
        }
        Map<String, Object> exportData = new HashMap<>();

        exportData.put("datas", exports);
        exportData.put("startDay", Date8Utils.getFirstMonthDateTime(adasDriverScoreQuery.getTime(), "YYYY-MM-dd"));
        exportData.put("endDay", Date8Utils.getLastMonthDateTime(adasDriverScoreQuery.getTime(), "YYYY-MM-dd"));

        templateExportExcel
            .templateExportExcel("/file/cargoReport/driver_score_list.xls", response, exportData, "驾驶员评分统计报表");
    }

    @Override
    public void exportDriverScoreProfessionalDetail(AdasDriverScoreQuery query, HttpServletResponse response) {
        AdasDriverScoreProfessionalInfoShow driverScoreProfessionalInfo = getDriverScoreProfessionalInfo(query);

        List<AdasDriverScoreEventShow> events = getIcCardDriverEvents(query);

        AdasDriverScoreProfessionalInfoExport export =
            AdasDriverScoreProfessionalInfoExport.getInstance(events, driverScoreProfessionalInfo, adasCommonHelper);
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("data", export);
        String photo = driverScoreProfessionalInfo.getAdasProfessionalShow().getPhotograph();

        exportData.put("img", configHelper.getFileFromFtp(photo));

        String exportName = "驾驶员评分明细报表" + query.getDriverName() + "(" + query.getCardNumber() + ")";
        templateExportExcel
            .templateExportExcel("/file/cargoReport/driver_score_detail.xls", response, exportData, exportName);
    }

    @Override
    public void exportDriverScoreProfessionalDetails(AdasDriverScoreQuery query, HttpServletResponse response) {
        query.initParam();
        List<AdasDriverScoreProfessionalInfoShow> driverScoreProfessionalInfos =
            adasDriverScoreDao.getCurrentPageDriverScoreProfessionalInfos(query);
        if (CollectionUtils.isNotEmpty(driverScoreProfessionalInfos)) {
            List<Map<String, Object>> param = new ArrayList<>();
            AdasProfessionalShow adasProfessionalShow;
            List<Map<String, String>> cls = new ArrayList<>();
            String[] cardNumberAndName;
            String exportName;
            Integer count = 0;
            List<IcCardDriverQuery> icCardDriverQueryList = driverScoreProfessionalInfos.stream()
                    .map(e -> IcCardDriverQuery.getInstance(e.getCardNumberName()))
                    .collect(Collectors.toList());
            Map<String, AdasProfessionalShow> driverInfoMaps = getAdasProfessionalShowMap(icCardDriverQueryList);
            for (AdasDriverScoreProfessionalInfoShow show : driverScoreProfessionalInfos) {
                show.setEventInfos(JSONObject.parseObject(show.getEventInfoStr(), cls.getClass()));
                //重新初始化条件
                query.setGroupId(show.getGroupId());
                query.setCardNumberName(show.getCardNumberName());
                cardNumberAndName = query.getCardNumberName().split("_");
                query.setDriverName(cardNumberAndName[1]);
                query.setCardNumber(cardNumberAndName[0]);

                adasProfessionalShow = Optional.ofNullable(driverInfoMaps.get(show.getCardNumberName()))
                    .orElse(new AdasProfessionalShow());
                show.setAdasProfessionalShow(adasProfessionalShow);
                //生成导出的数据
                List<AdasDriverScoreEventShow> events = getIcCardDriverEvents(query);
                AdasDriverScoreProfessionalInfoExport export =
                    AdasDriverScoreProfessionalInfoExport.getInstance(events, show, adasCommonHelper);
                Map<String, Object> exportData = new HashMap<>();
                if (StrUtil.isBlank(adasProfessionalShow.getCardNumber())) {
                    ++count;
                    exportName = "驾驶员评分明细报表未知" + count;
                } else {
                    exportName =
                        "驾驶员评分明细报表" + adasProfessionalShow.getName() + "(" + adasProfessionalShow.getCardNumber() + ")";
                }
                exportData.put("templateSingleFileName", exportName);

                exportData.put("img", configHelper.getFileFromFtp(adasProfessionalShow.getPhotograph()));
                exportData.put("data", export);
                param.add(exportData);
            }
            templateExportExcel
                .templateExportExcels("/file/cargoReport/driver_score_detail.xls", response, param, "驾驶员评分明细报表");
        }
    }

    @Override
    public Map<String, Object> selectIcCardDriverEvents(AdasDriverScoreQuery query) {
        Map<String, Object> resultMap = new HashMap<>();
        query.setGroupIds(organizationService.getChildOrgIdByUuid(query.getGroupId()));
        AdasElasticSearchUtil.IcCardDriverEventInfo info = adasElasticSearchUtil.selectIcCardDriverEvents(query);
        List<AdasDriverScoreEventShow> result = getDriverEventFromPass(info.getIds());
        resultMap.put("searchAfter", info.getSearchAfter());
        resultMap.put("result", result);
        return resultMap;
    }

}
