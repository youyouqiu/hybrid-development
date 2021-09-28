package com.zw.adas.service.riskStatistics.impl;

import com.alibaba.fastjson.JSON;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventAlarmForm;
import com.zw.adas.domain.riskStatistics.bean.AdasStatisticsListBean;
import com.zw.adas.domain.riskStatistics.bean.AdasStatisticsReportBean;
import com.zw.adas.domain.riskStatistics.query.EventStatisticsRecordQuery;
import com.zw.adas.domain.riskStatistics.query.RiskStatisticsRecordQuery;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskStatisticsRecordDao;
import com.zw.adas.service.riskStatistics.AdasRiskStatisticsRecordService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import com.zw.talkback.util.excel.ExportExcelParam;
import com.zw.talkback.util.excel.ExportExcelUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @Author zhangqiang
 * @Date 2020/6/18 9:49
 */
@Service
public class AdasRiskStatisticsRecordServiceImpl implements AdasRiskStatisticsRecordService {
    @Autowired
    private AdasRiskStatisticsRecordDao adasRiskStatisticsRecordDao;
    @Autowired
    private AdasCommonHelper adasCommonHelper;
    @Autowired
    private AdasElasticSearchUtil elasticSearchUtil;
    @Autowired
    private VehicleService vehicleService;

    @Override
    public List<AdasStatisticsListBean> getListData(EventStatisticsRecordQuery query) {
        if (StrUtil.isBlank(query.getVehicleIds())) {
            return new Page<>();
        }
        query.init();
        Map<String, BindDTO> bindDTOMap =
            MonitorUtils.getBindDTOMap(query.getVehicleIdSet(), "name", "orgName", "plateColor");
        List<AdasStatisticsListBean> result = adasRiskStatisticsRecordDao.getListData(query);
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(e -> e.initData(bindDTOMap.get(e.getVehicleId())));
        }
        return result;
    }

    @Override
    public void exportData(EventStatisticsRecordQuery query, HttpServletResponse response) throws Exception {
        ExportExcelUtil
            .export(new ExportExcelParam(getListData(query), AdasStatisticsListBean.class, response.getOutputStream()));
    }

    @Override
    public Map<String, Object> searchReportInfo(RiskStatisticsRecordQuery query, boolean exportFlag)
        throws IOException {
        List<Integer> functionIdList = adasCommonHelper.getAllEventByCommonField(query.getCommonField());
        String commonName = adasCommonHelper.getEventFieldAndCommonNameMap().get(query.getCommonField());
        VehicleDTO vehicleDTO = vehicleService.getById(query.getVehicleId());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("commonName", commonName);

        if (functionIdList != null && functionIdList.size() > 0) {
            Map<String, Object> esResultMap =
                elasticSearchUtil.searchAlarmInfoByFunctionIdList(query, functionIdList, commonName, exportFlag);
            if (esResultMap != null) {
                resultMap.putAll(esResultMap);
                //导出时不需要知道是否有报警证据
                if (!exportFlag) {
                    //1.设置是否有报警图片和报警视频
                    setPicAndVideoFlag(resultMap);
                }
                //2.设置车辆颜色
                resultMap.put("plateColor", PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
                resultMap.put("groupName", Optional.ofNullable(vehicleDTO.getOrgName()).orElse("未知企业"));
                resultMap.put("brand", Optional.ofNullable(vehicleDTO.getName()).orElse(""));
                //4.删除hBaseIds
                resultMap.remove("ids");
            }
        }
        return resultMap;
    }

    private void setPicAndVideoFlag(Map<String, Object> resultMap) {
        List<byte[]> eventIds = (List<byte[]>) resultMap.get("ids");
        Map<String, AdasRiskEventAlarmForm> alarmFormMap = getAlarmFormMap(eventIds);
        List<AdasStatisticsReportBean> dataList = (List<AdasStatisticsReportBean>) resultMap.get("data");
        for (AdasStatisticsReportBean bean : dataList) {
            AdasRiskEventAlarmForm alarmForm = alarmFormMap.get(bean.getRiskEventId());
            if (alarmForm != null) {
                bean.setHasPic(alarmForm.getHasPic());
                bean.setHasVideo(alarmForm.getHasVideo());
            }
        }
    }

    private Map<String, AdasRiskEventAlarmForm> getAlarmFormMap(List<byte[]> eventIds) {
        Map<String, AdasRiskEventAlarmForm> alarmFormMap = new HashMap<>();
        List<AdasRiskEventAlarmForm> riskEventAlarmForms = getRiskEventAlarmsByIds(eventIds);
        for (AdasRiskEventAlarmForm eventAlarmForm : riskEventAlarmForms) {
            eventAlarmForm.transFormData(adasCommonHelper);
            alarmFormMap.put(eventAlarmForm.getId(), eventAlarmForm);
        }
        return alarmFormMap;
    }

    private List<AdasRiskEventAlarmForm> getRiskEventAlarmsByIds(List<byte[]> eventIds) {
        Map<String, String> param = new HashMap<>();
        param.put("eventIds", JSON.toJSONString(eventIds));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.ADAS_REPORT_EVENT_LIST, param);
        return PassCloudResultUtil.getListResult(sendResult, AdasRiskEventAlarmForm.class);
    }

}
