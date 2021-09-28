package com.zw.platform.util.alarm;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/12/13 11:21
 */
@Component
@Getter
public class PassCloudAlarmUrlUtil {

    @Value("${db.host}")
    private String passIp;

    @Value("${f3.pass.port}")
    private String passPort;

    public static final Integer SUCCESS_CODE = 10000;

    public static final String RETURN_RESULT_CODE_KEY = "code";

    private String reportQueryAddress;

    /**
     * 报表管理 报警报表 报警查询
     * 查询报警信息
     */
    private String queryAlarmInfo;

    /**
     * 查询当天时间范围外的报警
     */
    private String queryTodayOutOfTimeAlarm;

    /**
     * 报表管理 报警报表 io报警查询
     * 查询IO报警信息
     */
    private String queryIoAlarmInfo;

    /**
     * 报表管理 报警报表 809转发报警查询
     * 查询809转发报警
     */
    private String query809ForwardAlarmInfo;

    /**
     * 查询同一时间的报警
     */
    private String queryTheSameTimeAlarmInfo;

    /**
     * 查询全局报警数量
     */
    private String queryGlobalAlarmNumber;

    /**
     * 查询全局报警最早开始时间
     */
    private String queryGlobalAlarmEarliestStartTime;

    /**
     * 查询报警次数(总的和已处理的)统计
     */
    private String queryAlarmNumberCount;

    /**
     * 报警处理(同一报警类型之前的报警全部处理)
     */
    private String handleAlarmBatch;

    /**
     * 报警处理 只处理一条报警记录
     */
    private String handleAlarmSingle;

    /**
     * 报警处理 处理指定的多条报警记录
     */
    private String handleAlarmMulti;

    @PostConstruct
    private void init() {
        reportQueryAddress = "http://" + passIp + ":" + passPort + "/api";
        queryAlarmInfo = assembleUrl("/alarm/list");
        queryTodayOutOfTimeAlarm = assembleUrl("/alarm/sichuan/besides_time/list");
        queryIoAlarmInfo = assembleUrl("/alarm/io/list");
        query809ForwardAlarmInfo = assembleUrl("/alarm/809/list");
        queryTheSameTimeAlarmInfo = assembleUrl("/alarm/time/list");
        queryGlobalAlarmNumber = assembleUrl("/alarm/global/num");
        queryGlobalAlarmEarliestStartTime = assembleUrl("/alarm/global/min_time");
        handleAlarmBatch = assembleUrl("/alarm/status/batch");
        handleAlarmSingle = assembleUrl("/alarm/status");
        handleAlarmMulti = assembleUrl("/alarm/handle/batch");
        queryAlarmNumberCount = assembleUrl("/alarm/count/type");
    }

    private String assembleUrl(String url) {
        return reportQueryAddress + url;
    }
}
