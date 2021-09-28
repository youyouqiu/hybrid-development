package com.zw.platform.domain.reportManagement.query;

import com.zw.platform.util.common.DateUtil;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: zjc
 * @Description:车辆状态表查询
 * @Date: create in 2020/11/12 17:49
 */
@Data
public class VehStateQuery {
    /**
     * 开始日期的秒值
     */
    private String startDate;
    /**
     * 结束日期的秒值
     */
    private String endDate;
    /**
     * 企业id
     */
    private String orgId;
    /**
     * 多个企业id按照逗号隔开，用户导出使用
     */
    private String orgIds;

    /**--------------------前端参数分界线-------------------**/

    /**
     * 开始时间秒值
     */
    private long startDateSecond;
    /**
     * 结束时间秒值
     */
    private long endDateSecond;

    private List<String> orgIdList;

    /**
     * 是否包含当天
     */
    private boolean containsToday;

    /**
     * 前一天零点的秒值
     */
    private long beforeDaySecond;

    /**
     * 结束日期当天的开始时间（只会用于查询不包含今天的情况，用来组装查询结束时间当天的条件）
     */
    private long endDateStartSecond;

    /**
     * 是否是导出，这里会用到过滤掉非列表参数的清空（导出不清空，查询需要清空）
     */
    private boolean export;

    /**
     * 导出使用的时间格式（2020年10月29日16时50分 ）
     */
    private String exportStartDate;

    /**
     * 导出使用的时间格式（2020年10月29日16时50分 ）
     */
    private String exportEndDate;

    /**
     * 初始化查询相关参数
     */
    public void init() {
        String format = DateUtil.DATE_FORMAT_SHORT;
        //查询开始的时间戳
        Date startDateTime = DateUtil.getStringToDate(startDate + " 00:00:00", format);
        //查询结束的时间戳
        Date endDateTime;

        Date today = new Date();
        containsToday = endDate.contains(DateUtil.getDayStr(today));
        //如果包含当天的情况
        if (containsToday) {
            endDateTime = today;
        } else {
            endDateTime = DateUtil.getStringToDate(endDate + " 23:59:59", format);
        }
        //初始化查询的条件时间信息
        startDateSecond = getSecond(startDateTime);
        endDateSecond = getSecond(endDateTime);

        //组装
        //如果包含当天的情况
        if (containsToday) {
            //如果是今天的话，就需要查前一天的零点（用开始时间减去一天即可）
            beforeDaySecond = startDateSecond - 86400;
        } else {
            endDateStartSecond = endDateSecond - 86399;
            //不包含今天的时候（23:59:59）需要减去减去86399，然后再减去一天86400
            beforeDaySecond = endDateStartSecond - 86400;
        }

        exportStartDate = DateUtil.formatDate(startDateTime, DateUtil.DATE_FORMAT_SHORT_MINUTE_CN);
        exportEndDate = DateUtil.formatDate(endDateTime, DateUtil.DATE_FORMAT_SHORT_MINUTE_CN);

    }

    private long getSecond(Date date) {
        return date.getTime() / 1000;
    }

    /**
     * 初始化导出相关参数
     */
    public void initExport() {
        export = true;
        orgIdList = Arrays.asList(orgIds.split(","));
        init();

    }

    public String getExportName() {
        return String.format("【%s-%s】道路运输车辆动态监控记录表.zip", startDate, endDate);
    }

}
