package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.LogSearch;
import com.zw.platform.domain.reportManagement.VideoLog;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.domain.reportManagement.query.LogSearchQuery;

import javax.servlet.http.HttpServletResponse;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

/**
 * @author wangying
 */
public interface LogSearchService {

    List<LogSearch> findLog(LogSearchQuery query, boolean doPage) throws Exception;

    List<VideoLog> findVideoLog(LogSearchQuery query, boolean doPage) throws Exception;

    boolean log(String message, String source, String module);

    boolean log(String message, String source, String module, String monitoringOperation);

    boolean log(List<String> messages, String source, String module, String monitoringOperation);

    boolean log(String message, String source, String module, String brand, String plateColor);

    boolean log(String message, String source, String module, String userName, String brand, String plateColor);

    boolean addLog(String ipAddress, String message, String logSource, String module);

    boolean addLog(String ipAddress, String message, String logSource, String module, String brand, String palateColor);

    /**
     * 添加日志
     * @param ipAddress IP
     * @param message 日志内容
     * @param logSource 日志类型
     * @param module 日志模块
     * @param brand 车牌号
     * @param palateColor 车牌颜色
     * @param userName 用户名
     * @param orgId 企业ID
     * @return boolean
     */
    boolean addLogWithBrandAndColor(String ipAddress, String message, String logSource, String module, String brand,
                     String palateColor,
                   String userName, String orgId);

    /**
     * 添加日志（操作人为传入的用户名）
     *
     * @param ipAddress ip地址
     * @param message   日志语句
     * @param logSource 日志类型
     * @param module    日志模块
     * @param userName  用户名
     * @author hujun
     */
    boolean addLogByUserName(String ipAddress, String message, String logSource, String module, String userName,
        String brand, String palateColor);

    /**
     * 添加日志
     * @author fanlu
     */
    boolean addLog(String ipAddress, String message, String logSource, String module, String monitoringOperation);

    /**
     * 添加日志
     * @param ipAddress ip
     * @param message 日志语句
     * @param logSource 日志类型
     * @param module 日志模块
     * @param monitoringOperation 监控对象操作
     * @param userName 用户名
     * @param orgId 企业ID
     * @return boolean
     */
    boolean addLogByUserNameAndOrgId(String ipAddress, String message, String logSource, String module,
                                     String monitoringOperation, String userName, String orgId);

    boolean addMoreLog(String ipAddress, String message, String logSource, String brand, String monitoringOperation);

    boolean addLogBean(LogSearchForm form);

    /**
     * 根据不同界面类型和时间查询相应操作日志
     */
    List<LogSearch> findLogByModule(String eventDate, Integer webType) throws Exception;

    String[] findCarMsg(String vehicleId);

    boolean addLog(String ipAddress, List<String> message, String logSource, String module, String monitoringOperation)
        throws Exception;

    boolean export(String title, int type, HttpServletResponse res) throws Exception;

    boolean videoExport(String title, int type, HttpServletResponse res) throws Exception;

    void addLog(String vehicleId, int i, String ip);

    void addSearchAlarmLog(String vehicleId, String ip);

    /**
     * 添加小程序日志
     * @param vehicleId 车辆id
     * @param ip ip地址
     * @param message 日志消息
     */
    void addSingleVehicleLog(String vehicleId, String ip, String message);

    /**
     * 根据时间进行查询
     * @param startTime
     * @param endTime
     * @return
     */
    List<LogSearch> getByTime(Long startTime, Long endTime, Set<String> orgIds);

    /**
     * 数据迁移 zw_c_log -> zw_log_yyyyMM
     * @param month 月份
     * @return succeed or not
     */
    String migrateLog(YearMonth month);
}
