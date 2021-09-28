package com.zw.lkyw.service.sendMessageReport.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.lkyw.domain.sendMessageReport.DetailQuery;
import com.zw.lkyw.domain.sendMessageReport.SendMessageDetail;
import com.zw.lkyw.domain.sendMessageReport.SendMessageReportData;
import com.zw.lkyw.service.sendMessageReport.SendMessageReportSevice;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author denghuabing on 2019/12/30 16:02
 */
@Service
public class SendMessageReportSeviceImpl implements SendMessageReportSevice {

    @Autowired
    TemplateExportExcel templateExportExcel;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    private static final String REPORT_TEMPLATE = "/file/cargoReport/sendMessageReport.xls";
    private static final String REPORT_DETAIL_TEMPLATE = "/file/cargoReport/sendMessageReportDetail.xls";

    @Override
    public JsonResultBean getList(String vehicleIds, String startTime, String endTime) {
        JSONObject resultObject = getReportList(vehicleIds, startTime, endTime);
        if (Objects.isNull(resultObject) || resultObject.getInteger("code").intValue() != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "数据查询异常");
        }
        List<SendMessageReportData> result =
            JSONObject.parseArray(resultObject.getString("data"), SendMessageReportData.class);
        if (CollectionUtils.isEmpty(result)) {
            return new JsonResultBean();
        }
        List<String> ids = result.stream().map(SendMessageReportData::getMonitorId).collect(Collectors.toList());
        Map<String, Map<String, String>> configLists =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(ids)).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(data -> setProperties(data, configLists));
        }
        return new JsonResultBean(result);
    }

    private void setProperties(SendMessageReportData data, Map<String, Map<String, String>> configLists) {
        Map<String, String> configList = configLists.get(data.getMonitorId());
        data.setPlateColor(PlateColor.getNameOrBlankByCode(configList.get("plateColor")));
        data.setGroupName(configList.get("orgName"));
        data.setMonitorName(configList.get("name"));
        data.setObjectType(cacheManger.getVehicleType(configList.get("vehicleType")).getType());
    }

    @Override
    public void export(String vehicleIds, String startTime, String endTime, HttpServletResponse response) {
        JSONObject resultObject = getReportList(vehicleIds, startTime, endTime);
        if (Objects.isNull(resultObject) || resultObject.getInteger("code") != 10000) {
            return;
        }
        List<SendMessageReportData> result =
            JSONObject.parseArray(resultObject.getString("data"), SendMessageReportData.class);

        if (CollectionUtils.isNotEmpty(result)) {
            List<String> ids = result.stream().map(SendMessageReportData::getMonitorId).collect(Collectors.toList());
            Map<String, Map<String, String>> configLists =
                RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(ids)).stream()
                    .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
            int i = 0;
            for (SendMessageReportData data : result) {
                setProperties(data, configLists);
                data.setNum(++i);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("msgData", result);
        String fileName = "下发消息统计表";
        templateExportExcel.templateExportExcel(REPORT_TEMPLATE, response, map, fileName);
    }

    private JSONObject getReportList(String vehicleIds, String startTime, String endTime) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("monitorIds", vehicleIds);
        queryParam.put("startTime", startTime);
        queryParam.put("endTime", endTime);
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.SEND_MESSAGE_REPORT_LIST_URL, queryParam);
        return JSON.parseObject(queryResult);
    }

    @Override
    public JsonResultBean getDetail(DetailQuery query, HttpServletResponse response) {
        JSONObject resultObject = getVehicleDetail(query);
        if (Objects.isNull(resultObject) || resultObject.getInteger("code").intValue() != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "数据查询异常");
        }
        List<SendMessageDetail> result = new ArrayList<>();
        JSONArray monitorInfos = resultObject.getJSONArray("data");
        if (CollectionUtils.isNotEmpty(monitorInfos)) {
            Map<String, String> configList = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(query.getMonitorId()));
            JSONObject data = monitorInfos.getJSONObject(0);
            JSONArray detailInfo = data.getJSONArray("detailInfo");
            detailInfo(data, detailInfo, result, configList);
        }

        return new JsonResultBean(result);
    }

    private JSONObject getVehicleDetail(DetailQuery query) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("monitorIds", query.getMonitorId());
        queryParam.put("startTime", query.getStartTime());
        queryParam.put("endTime", query.getEndTime());
        queryParam.put("msgContent", query.getMsgContent());
        queryParam.put("sendType", query.getSendType());
        queryParam.put("sendStatus", query.getSendStatus());
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.SEND_MESSAGE_REPORT_DETAILS_URL, queryParam);
        return JSON.parseObject(queryResult);
    }

    private String getPlayType(String type) {
        StringBuilder p = new StringBuilder();
        if (null == type) {
            return p.toString();
        }
        String[] types = type.split(",");
        for (String s : types) {
            switch (s) {
                case "0":
                    p.append("终端TTS读播，");
                    break;
                case "1":
                    p.append("终端显示器显示，");
                    break;
                case "2":
                    p.append("广告屏显示，");
                    break;
                default:
                    break;
            }
        }
        return p.delete(p.length() - 1, p.length()).toString();
    }

    @Override
    public void exportDetail(DetailQuery query, HttpServletResponse response) throws IOException {
        JSONObject resultObject = getVehicleDetail(query);
        if (Objects.isNull(resultObject) || resultObject.getInteger("code").intValue() != 10000) {
            return;
        }
        List<SendMessageDetail> result = new ArrayList<>();
        JSONArray monitorInfos = resultObject.getJSONArray("data");
        if (CollectionUtils.isNotEmpty(monitorInfos)) {
            Map<String, String> configList = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(query.getMonitorId()));
            JSONObject data = monitorInfos.getJSONObject(0);
            JSONArray detailInfo = data.getJSONArray("detailInfo");
            detailInfo(data, detailInfo, result, configList);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("msgData", result);
        String fileName = "下发消息(明细)统计表";
        templateExportExcel.templateExportExcel(REPORT_DETAIL_TEMPLATE, response, map, fileName);

    }

    private void detailInfo(JSONObject data, JSONArray detailInfo, List<SendMessageDetail> result,
        Map<String, String> configList) {
        int num = 0;
        for (int i = 0, len = detailInfo.size(); i < len; i++) {
            JSONObject info = detailInfo.getJSONObject(i);
            SendMessageDetail detail = new SendMessageDetail();
            detail.setMonitorId(data.getString("monitorId"));
            detail.setMonitorName(configList.get("name"));
            detail.setObjectType(cacheManger.getVehicleType(configList.get("vehicleType")).getType());
            detail.setSignColor(PlateColor.getNameOrBlankByCode(configList.get("plateColor")));
            detail.setGroupName(configList.get("orgName"));
            detail.setMsgContent(info.getString("msgContent"));
            detail.setPlayType(getPlayType(info.getString("playType")));
            detail.setSendTime(DateUtil.getLongToDateStr(info.getLong("sendTime"), null));
            detail.setSendType(Objects.equals(info.getInteger("sendType"), 0) ? "系统下发" : "人工下发");
            detail.setSendStatus(Objects.equals(info.getInteger("sendStatus"), 0) ? "下发成功" : "下发失败");
            detail.setSendUserName(info.getString("sendUserName"));
            detail.setNum(++num);
            result.add(detail);
        }

    }
}
