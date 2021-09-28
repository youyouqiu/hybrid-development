package com.zw.lkyw.domain;

import com.zw.platform.repository.core.ResourceDao;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 @Author zhengjc
 @Date 2019/12/30 10:55
 @Description 实时监控报表常量信息
 @version 1.0
 **/
public class ReportConstant {
    private static Map<String, List<ReportMenu>> reports = new HashMap<>();

    private static Set<String> reportIds = new HashSet<>();

    private static Map<String, String> reportMaps = new HashMap<>();

    public static final String SERVICE_REPORT = "SERVICE_REPORT";
    public static final String ALARM_REPORT = "ALARM_REPORT";
    public static final String MEDIA_REPORT = "MEDIA_REPORT";
    public static final String POSITIONAL_REPORT = "POSITIONAL_REPORT";
    public static final String OTHER_REPORT = "OTHER_REPORT";

    static {
        reports.put(SERVICE_REPORT, getServiceReports());
        reports.put(ALARM_REPORT, getAlarmReports());
        reports.put(MEDIA_REPORT, getMediaReports());
        reports.put(POSITIONAL_REPORT, getPositionalReports());
        reports.put(OTHER_REPORT, getOtherReports());
    }

    private static List<String> getAllReportName() {
        List<String> allReportName = new LinkedList<>();
        for (List<ReportMenu> reportList : reports.values()) {
            for (ReportMenu menu : reportList) {
                allReportName.add(menu.getName());
            }
        }
        return allReportName;
    }

    public static void init(ResourceDao resourceDao) {
        List<ReportMenu> reportMenus = resourceDao.getReportMenuByNames(getAllReportName());
        if (CollectionUtils.isEmpty(reportMenus)) {
            return;
        }
        for (ReportMenu menu : reportMenus) {
            reportMaps.put(menu.getName(), menu.getUrl());
            reportIds.add(menu.getId());
        }
        List<ReportMenu> sortReportMenus;
        for (Map.Entry<String, List<ReportMenu>> entry : reports.entrySet()) {
            sortReportMenus = entry.getValue();
            for (ReportMenu sortMenu : sortReportMenus) {
                sortMenu.setUrl(reportMaps.get(sortMenu.getName()));
            }
        }

    }

    public static Set<String> getReportIdSet() {
        return reportIds;
    }

    public static Map<String, List<ReportMenu>> getUserReportMenu(Set<String> menuNames) {
        Map<String, List<ReportMenu>> result = new HashMap<>();

        List<ReportMenu> sortReportMenus;
        String sortReportKey;
        for (Map.Entry<String, List<ReportMenu>> entry : reports.entrySet()) {
            List<ReportMenu> reportMenus = new ArrayList<>();
            sortReportMenus = entry.getValue();
            sortReportKey = entry.getKey();
            for (ReportMenu sortMenu : sortReportMenus) {
                if (!menuNames.contains(sortMenu.getName())) {
                    continue;
                }
                reportMenus.add(sortMenu);
            }
            result.put(sortReportKey, reportMenus);

        }
        return result;
    }

    private static List<ReportMenu> getOtherReports() {
        List<ReportMenu> otherReports = new ArrayList<>();
        otherReports.add(ReportMenu.getInstance("下发消息统计"));
        otherReports.add(ReportMenu.getInstance("809转发报警查询"));
        otherReports.add(ReportMenu.getInstance("终端里程报表"));
        otherReports.add(ReportMenu.getInstance("行驶报表"));
        otherReports.add(ReportMenu.getInstance("停止报表"));
        otherReports.add(ReportMenu.getInstance("连续性分析报表"));
        otherReports.add(ReportMenu.getInstance("出区划累计时长统计"));
        otherReports.add(ReportMenu.getInstance("客流量报表"));
        otherReports.add(ReportMenu.getInstance("809查岗督办报表"));
        otherReports.add(ReportMenu.getInstance("行驶里程报表"));
        otherReports.add(ReportMenu.getInstance("用户在线时长统计"));
        otherReports.add(ReportMenu.getInstance("车辆运营状态报表"));
        otherReports.add(ReportMenu.getInstance("车辆在线率统计"));
        return otherReports;
    }

    private static List<ReportMenu> getPositionalReports() {
        List<ReportMenu> positionalReports = new ArrayList<>();
        positionalReports.add(ReportMenu.getInstance("离线查询报表"));
        positionalReports.add(ReportMenu.getInstance("上线率报表"));
        positionalReports.add(ReportMenu.getInstance("定位数据合格率"));
        positionalReports.add(ReportMenu.getInstance("漂移数据报表"));
        positionalReports.add(ReportMenu.getInstance("车辆定位统计"));
        positionalReports.add(ReportMenu.getInstance("凌晨2-5点运行报表"));
        positionalReports.add(ReportMenu.getInstance("异常轨迹报表"));
        positionalReports.add(ReportMenu.getInstance("轨迹有效性报表"));
        positionalReports.add(ReportMenu.getInstance("车辆异动统计"));
        return positionalReports;
    }

    private static List<ReportMenu> getMediaReports() {
        List<ReportMenu> mediaReports = new ArrayList<>();
        mediaReports.add(ReportMenu.getInstance("多媒体管理"));
        mediaReports.add(ReportMenu.getInstance("音视频日志查询"));
        mediaReports.add(ReportMenu.getInstance("音视频流量报表"));
        mediaReports.add(ReportMenu.getInstance("视频巡检统计"));
        return mediaReports;
    }

    private static List<ReportMenu> getAlarmReports() {
        List<ReportMenu> alarmReports = new ArrayList<>();
        alarmReports.add(ReportMenu.getInstance("报警查询"));
        alarmReports.add(ReportMenu.getInstance("风险证据库"));
        alarmReports.add(ReportMenu.getInstance("报警信息报表"));
        alarmReports.add(ReportMenu.getInstance("主动安全处置报表"));
        alarmReports.add(ReportMenu.getInstance("报警排行统计报表"));
        alarmReports.add(ReportMenu.getInstance("超速报表"));
        alarmReports.add(ReportMenu.getInstance("超速报警报表"));
        alarmReports.add(ReportMenu.getInstance("持续超速统计"));
        alarmReports.add(ReportMenu.getInstance("报警信息统计"));
        alarmReports.add(ReportMenu.getInstance("疲劳驾驶报警明细"));
        return alarmReports;
    }

    private static List<ReportMenu> getServiceReports() {
        List<ReportMenu> serviceReports = new ArrayList<>();
        serviceReports.add(ReportMenu.getInstance("引导页"));
        serviceReports.add(ReportMenu.getInstance("领导看板"));
        serviceReports.add(ReportMenu.getInstance("安全看板"));
        serviceReports.add(ReportMenu.getInstance("运营看板"));
        serviceReports.add(ReportMenu.getInstance("大数据报表"));
        serviceReports.add(ReportMenu.getInstance("服务器监控报表"));
        serviceReports.add(ReportMenu.getInstance("服务到期报表"));
        serviceReports.add(ReportMenu.getInstance("驾驶员统计"));
        serviceReports.add(ReportMenu.getInstance("驾驶员评分"));
        serviceReports.add(ReportMenu.getInstance("监控对象评分"));
        return serviceReports;
    }

}
