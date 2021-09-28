package com.zw.platform.service.reportManagement.impl;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.dto.reportManagement.AccStatisticDTO;
import com.zw.platform.dto.reportManagement.AccStatisticDetailDTO;
import com.zw.platform.dto.reportManagement.AccStatisticsDetailQuery;
import com.zw.platform.dto.reportManagement.AccStatisticsQuery;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.service.reportManagement.AccStatisticsService;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AccStatisticsServiceImpl implements AccStatisticsService {

    @Autowired
    private OfflineExportService exportService;

    private static final Logger log = LogManager.getLogger(AccStatisticsServiceImpl.class);


    @Override
    public PageGridBean getAccStatisticsInfo(AccStatisticsQuery query) throws Exception {
        Map<String, String> params = new HashMap<>();
        //组装paas-cloud api入参
        params.put("startDate", query.getStartDate());
        params.put("endDate", query.getEndDate());
        params.put("monitorIds", query.getMonitorIds());
        params.put("page", query.getPage().toString());
        params.put("pageSize", query.getLimit().toString());

        //调用paas api 获取分页数据
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.ACC_STATISTIC_LIST_URL, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<AccStatisticDTO>> resultData =
                PaasCloudUrlUtil.pageResult(paasResult, AccStatisticDTO.class);
        List<AccStatisticDTO> items = resultData.getData().getItems();
        if (CollectionUtils.isEmpty(items)) {
            return new PageGridBean();
        }
        return new PageGridBean(items, resultData.getData().getPageInfo());
    }

    @Override
    public JsonResultBean exportAccStatisticsInfo(AccStatisticsQuery query) {
        String monitorIds = query.getMonitorIds();
        String startDate = query.getStartDate();
        String endDate = query.getEndDate();
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        String fileName = "ACC统计表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("ACC统计报表", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", monitorIds);
        params.put("startDate", startDate);
        params.put("endDate",  endDate);
        instance.assembleCondition(params, OffLineExportBusinessId.ACC_STATISTICS_LIST);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public PageGridBean getAccStatisticsDetailInfo(AccStatisticsDetailQuery query) throws Exception {
        Map<String, String> params = new HashMap<>();
        //组装paas-cloud api入参
        params.put("startTime", query.getStartTime());
        params.put("endTime", query.getEndTime());
        params.put("monitorId", query.getMonitorId());
        params.put("page", query.getPage().toString());
        params.put("pageSize", query.getLimit().toString());

        //调用paas api 获取分页数据
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.ACC_STATISTIC_DETAIL_LIST_URL, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<AccStatisticDetailDTO>> resultData =
                PaasCloudUrlUtil.pageResult(paasResult, AccStatisticDetailDTO.class);
        List<AccStatisticDetailDTO> items = resultData.getData().getItems();
        if (CollectionUtils.isEmpty(items)) {
            return new PageGridBean();
        }
        return new PageGridBean(items, resultData.getData().getPageInfo());
    }

}
