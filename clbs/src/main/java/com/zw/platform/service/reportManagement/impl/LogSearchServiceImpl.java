package com.zw.platform.service.reportManagement.impl;

import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.reportManagement.LogSearch;
import com.zw.platform.domain.reportManagement.VideoLog;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.domain.reportManagement.query.LogSearchQuery;
import com.zw.platform.repository.sharding.LogSearchDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.HttpServletRequestUtil;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.BigDataExportExcelParam;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class LogSearchServiceImpl implements LogSearchService {
    @Autowired
    private LogSearchDao logSearchDao;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private HttpServletRequestUtil httpUtils;

    @Autowired
    private DataSourceTransactionManager txManager;

    private static final String REALTIME_MONITOR_MODULE = "MONITORING";

    private static final String REALTIME_VIDEO_MODULE = "REALTIMEVIDEO";
    /**
     * 日志导出message最大限制长度
     */
    private static final int MAXIMUM_NUMBER_OF_MESSAGE = 15000;
    /**
     * 日志导出Excel 切换为07版阈值
     */
    private static final int MAXIMUM_NUMBER_OF_THRESHOLD = 50000;
    /**
     * 截取超过最大限制长度 特殊字符标示
     */
    private static final String SPECIAL_INTERCEPT_CHARACTER = "<br/>";

    private static final Logger logger = LogManager.getLogger(LogSearchServiceImpl.class);

    private static final Lock MIGRATION_LOCK = new ReentrantLock();

    @Override
    public List<LogSearch> findLog(LogSearchQuery query, boolean doPage) {
        List<String> userOrgListId = userService.getCurrentUserOrgIds();
        query.setGroupIds(userOrgListId);
        return doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> logSearchDao.findLog(query))
                : logSearchDao.findLog(query);
    }

    @Override
    public List<VideoLog> findVideoLog(LogSearchQuery query, boolean doPage) {
        List<String> userOrgListId = userService.getCurrentUserOrgIds();
        query.setGroupIds(userOrgListId);
        query.setModule(REALTIME_VIDEO_MODULE);
        String orgId = userService.getOrgIdExceptAdmin();
        String name = organizationService.getOrganizationByUuid(orgId).getName();

        List<VideoLog> logSearchList = doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> logSearchDao.findVideoLogDetail(query))
                : logSearchDao.findVideoLogDetail(query);
        final Set<String> usernames = logSearchList.stream().map(VideoLog::getUsername).collect(Collectors.toSet());
        if (usernames.size() < 100) {
            query.setUsernames(usernames);
        }
        final Map<String, Long> countMap = logSearchDao.findVideoLogCount(query).stream()
                .collect(Collectors.toMap(VideoLog::getUsername, VideoLog::getUserCount, (o, p) -> o));
        int max = 800;
        int min = 200;
        Random random = new Random();
        for (VideoLog videoLog : logSearchList) {
            videoLog.setGroupName(name);
            int s = random.nextInt(max) % (max - min + 1) + min;
            videoLog.setLogFlow(s / 100.0);
            videoLog.setUserCount(countMap.getOrDefault(videoLog.getUsername(), 0L));
        }
        return logSearchList;
    }

    @Override
    public boolean log(String message, String source, String module) {
        request = httpUtils.getRequest();
        String ip = IPAddrUtil.getClientIp(request);
        return addLog(ip, message, source, module);
    }

    @Override
    public boolean log(String message, String source, String module, String monitoringOperation) {
        request = httpUtils.getRequest();
        String ip = IPAddrUtil.getClientIp(request);
        return addLog(ip, message, source, module, monitoringOperation);
    }

    @Override
    public boolean log(List<String> messages, String source, String module, String monitoringOperation) {
        request = httpUtils.getRequest();
        String ip = IPAddrUtil.getClientIp(request);
        return addLog(ip, messages, source, module, monitoringOperation);
    }

    @Override
    public boolean log(String message, String source, String module, String brand, String plateColor) {
        request = httpUtils.getRequest();
        String ip = IPAddrUtil.getClientIp(request);
        return addLog(ip, message, source, module, brand, plateColor);
    }

    @Override
    public boolean log(String message, String source, String module, String userName, String brand, String plateColor) {
        request = httpUtils.getRequest();
        String ip = IPAddrUtil.getClientIp(request);
        return addLogByUserName(ip, message, source, module, userName, brand, plateColor);
    }

    @Override
    public boolean addLog(String ipAddress, String message, String logSource, String module) {
        LogSearchForm logForm = new LogSearchForm();
        // 当前用户的所属企业的id
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        logForm.setEventDate(new Date());
        // 获取到当前用户的用户名
        logForm.setUsername(SystemHelper.getCurrentUsername());
        logForm.setIpAddress(ipAddress);
        logForm.setMessage(message);
        logForm.setLogSource(logSource);
        logForm.setModule(module);
        logForm.setGroupId(currentUserOrg.getUuid());
        return logSearchDao.addLog(logForm);
    }

    @Override
    public boolean addLog(String ipAddress, String message, String logSource, String module, String brand,
        String palateColor) {
        LogSearchForm logForm = new LogSearchForm();
        // 当前用户的所属企业的id
        logForm.setEventDate(new Date());
        // 获取到当前用户的用户名
        Optional<String> userName = Optional.ofNullable(SystemHelper.getCurrentUsername());
        logForm.setUsername(userName.orElse("联动策略"));
        logForm.setIpAddress(ipAddress);
        logForm.setMessage(message);
        logForm.setLogSource(logSource);
        logForm.setModule(module);
        logForm.setGroupId(userService.getCurrentUserOrg().getUuid());
        logForm.setBrand(brand);
        Integer color;
        if (StringUtils.isEmpty(palateColor) || Objects.equals(palateColor, "null")) {
            color = null;
        } else {
            color = Integer.valueOf(palateColor);
        }
        logForm.setPlateColor(color);
        return logSearchDao.addLog(logForm);
    }

    @Override
    public boolean addLogWithBrandAndColor(String ipAddress, String message, String logSource, String module,
                                           String brand, String palateColor, String userName, String orgId) {
        LogSearchForm logForm = new LogSearchForm();
        logForm.setEventDate(new Date());
        logForm.setUsername(null != userName ? userName : "联动策略");
        logForm.setIpAddress(ipAddress);
        logForm.setMessage(message);
        logForm.setLogSource(logSource);
        logForm.setModule(module);
        logForm.setGroupId(orgId);
        logForm.setBrand(brand);
        Integer color;
        if (StringUtils.isEmpty(palateColor) || Objects.equals(palateColor, "null")) {
            color = null;
        } else {
            color = Integer.valueOf(palateColor);
        }
        logForm.setPlateColor(color);
        return logSearchDao.addLog(logForm);
    }

    @Override
    public boolean addLogByUserName(String ipAddress, String message, String logSource, String module, String userName,
        String brand, String palateColor) {
        LogSearchForm logForm = new LogSearchForm();
        if (StringUtils.isEmpty(userName)) {
            // 用户名若为空，则去Context获取
            Optional<String> getUserName = Optional.ofNullable(SystemHelper.getCurrentUsername());
            if (getUserName.isPresent()) {
                logForm.setUsername(getUserName.get());
                // 当前用户的所属企业的id
                logForm.setGroupId(userService.getCurrentUserOrg().getUuid());
            } else {
                logForm.setUsername("");
                logForm.setGroupId("");
            }
        } else {
            logForm.setUsername(userName);
            // 当前用户的所属企业的id
            UserDTO user = userService.getUserByUsername(userName);
            if (null != user) {
                OrganizationLdap ol =
                    organizationService.getOrgByEntryDn(userService.getUserOrgDnByDn(user.getId().toString()));
                logForm.setGroupId(ol.getUuid());
            }
        }
        logForm.setEventDate(new Date());
        logForm.setIpAddress(ipAddress);
        logForm.setMessage(message);
        logForm.setLogSource(logSource);
        logForm.setModule(module);
        logForm.setBrand(brand);
        Integer color;
        if (palateColor != null && !"".equals(palateColor) && !"null".equals(palateColor)) {
            color = Integer.valueOf(palateColor);
        } else {
            color = null;
        }
        logForm.setPlateColor(color);
        return logSearchDao.addLog(logForm);
    }

    @Override
    public List<LogSearch> findLogByModule(String eventDate, Integer webType) {
        List<LogSearch> logSearches;
        final String now = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_SHORT);
        logSearches = null != webType && 2 == webType
                ? logSearchDao.findLogByModule(eventDate, now, REALTIME_VIDEO_MODULE)
                : logSearchDao.findLogByModule(eventDate, now, REALTIME_MONITOR_MODULE);
        for (LogSearch log : logSearches) {
            String color =
                VehicleUtil.getPlateColorStr(log.getPlateColor() == null ? "" : log.getPlateColor().toString());
            log.setPlateColorStr(color);
        }
        return logSearches;
    }

    @Override
    public String[] findCarMsg(String vehicleId) {
        String brand = "-";
        // 默认颜色为9，其它
        String plateColor = "9";
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "name", "plateColor");
        if (Objects.nonNull(bindDTO) && StringUtils.isNotBlank(bindDTO.getName())) {
            brand = bindDTO.getName();
            plateColor = Objects.isNull(bindDTO.getPlateColor()) ? null : String.valueOf(bindDTO.getPlateColor());
        }
        return new String[] { brand, plateColor };
    }

    @Override
    public boolean addLogBean(LogSearchForm form) {
        if (form != null && form.getEventDate() == null) {
            form.setEventDate(new Date());
        }
        return form != null && logSearchDao.addLog(form);
    }

    @Override
    public boolean addLog(String ipAddress, String message, String logSource, String module,
        String monitoringOperation) {
        LogSearchForm logForm = new LogSearchForm();
        logForm.setEventDate(new Date());
        // 获取到当前用户的用户名
        logForm.setUsername(SystemHelper.getCurrentUsername());
        logForm.setIpAddress(ipAddress != null ? ipAddress : "");
        logForm.setMessage(message);
        logForm.setLogSource(logSource);
        logForm.setModule(module);
        logForm.setGroupId(userService.getCurrentUserOrg().getUuid());
        logForm.setMonitoringOperation(monitoringOperation);
        return logSearchDao.addLog(logForm);
    }

    @Override
    public boolean addLogByUserNameAndOrgId(String ipAddress, String message, String logSource, String module,
                                            String monitoringOperation, String userName, String orgId) {
        LogSearchForm logForm = new LogSearchForm();
        logForm.setEventDate(new Date());
        logForm.setUsername(userName);
        logForm.setIpAddress(ipAddress != null ? ipAddress : "");
        logForm.setMessage(message);
        logForm.setLogSource(logSource);
        logForm.setModule(module);
        logForm.setGroupId(orgId);
        logForm.setMonitoringOperation(monitoringOperation);
        return logSearchDao.addLog(logForm);
    }

    @Override
    public boolean addMoreLog(String ipAddress, String message, String logSource, String brand,
        String monitoringOperation) {
        LogSearchForm logForm = new LogSearchForm();
        logForm.setEventDate(new Date());
        // 获取到当前用户的用户名
        logForm.setUsername(SystemHelper.getCurrentUsername());
        logForm.setIpAddress(ipAddress != null ? ipAddress : "");
        logForm.setMessage(message);
        logForm.setLogSource(logSource);
        logForm.setBrand(brand);
        logForm.setModule("more");
        logForm.setGroupId(userService.getCurrentUserOrg().getUuid());
        logForm.setMonitoringOperation(monitoringOperation);
        return logSearchDao.addLog(logForm);
    }

    @Override
    public boolean addLog(String ipAddress, List<String> message, String logSource, String module,
        String monitoringOperation) {
        LogSearchForm logForm = new LogSearchForm();
        logForm.setEventDate(new Date());
        // 获取到当前用户的用户名
        logForm.setUsername(SystemHelper.getCurrentUsername());
        logForm.setIpAddress(ipAddress != null ? ipAddress : "");
        logForm.setLogSource(logSource);
        logForm.setModule(module);
        logForm.setGroupId(userService.getCurrentUserOrg().getUuid());
        logForm.setMonitoringOperation(monitoringOperation);
        for (String mes : message) {
            logForm.setMessage(mes);
            logSearchDao.addLog(logForm);
        }
        return true;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res) throws Exception {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<LogSearch> logSearch =
                RedisHelper.getList(HistoryRedisKeyEnum.EXPORT_LOG_FIND_INFORMATION.of(userId),
                        LogSearch.class);
        //日志不存在的情况
        if (logSearch == null) {
            return false;
        }
        // 逆地址编码
        for (LogSearch log : logSearch) {
            String color =
                VehicleUtil.getPlateColorStr(log.getPlateColor() == null ? "" : log.getPlateColor().toString());
            if ("".equals(color)) {
                color = "-";
            }
            if (log.getMessage() != null && log.getMessage().length() > MAXIMUM_NUMBER_OF_MESSAGE) {
                String message = log.getMessage().substring(0, MAXIMUM_NUMBER_OF_MESSAGE);
                if (message.contains(SPECIAL_INTERCEPT_CHARACTER)) {
                    log.setMessage(message.substring(0,
                        message.lastIndexOf(SPECIAL_INTERCEPT_CHARACTER) + SPECIAL_INTERCEPT_CHARACTER.length()));
                }
            }
            log.setPlateColorStr(color);
            log.setLogSource(convertLogSource(log.getLogSource()));
        }
        if (logSearch.size() > MAXIMUM_NUMBER_OF_THRESHOLD) {
            BigDataExportExcelParam param =
                new BigDataExportExcelParam(100, title, type, logSearch, LogSearch.class, null, res.getOutputStream());
            return ExportExcelUtil.bigDataExport(param);
        }
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, logSearch, LogSearch.class, null, res.getOutputStream()));
    }

    @Override
    public boolean videoExport(String title, int type, HttpServletResponse res)
        throws Exception {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<VideoLog> logSearch = RedisHelper.getList(HistoryRedisKeyEnum.EXPORT_VIDEO_LOG_INFORMATION.of(userId),
                VideoLog.class);
        // 逆地址编码
        if (CollectionUtils.isNotEmpty(logSearch)) {
            for (VideoLog log : logSearch) {
                String color =
                    VehicleUtil.getPlateColorStr(log.getPlateColor() == null ? "" : log.getPlateColor().toString());
                if ("".equals(color)) {
                    color = "-";
                }
                log.setPlateColorStr(color);
                log.setLogSource(convertLogSource(log.getLogSource()));
                if (Objects.nonNull(log.getUserCount())) {
                    log.setUserCountStr(String.valueOf(log.getUserCount()));
                }
                log.setLogFlowStr(String.valueOf(log.getLogFlow()));
            }
        }
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, logSearch, VideoLog.class, null, res.getOutputStream()));
    }

    @Override
    public void addLog(String vehicleId, int i, String ip) {
        // 根据id查询组织架构实体
        try {
            final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
            final Map<String, String> monitor = RedisHelper.getHashMap(key, "name", "plateColor", "orgName");
            if (monitor != null) {
                String brand = monitor.get("name");
                String plateColor = monitor.get("plateColor");
                String orgName = monitor.get("orgName");
                String msg = "监控对象:(" + brand + "@(" + orgName + ")" + ")查看历史轨迹记录";
                addLog(ip, msg, "3", "MONITORING", brand, plateColor);
            }
        } catch (Exception e) {
            logger.error("轨迹回放，日志管理异常" + e);
        }
    }

    @Override
    public void addSearchAlarmLog(String vehicleId, String ip) {
        // 根据id查询组织架构实体
        try {
            final Map<String, String> monitor = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(vehicleId, "name", "plateColor", "orgName"));
            if (monitor != null) {
                String brand = monitor.get("name");
                String plateColor = monitor.get("plateColor");
                String orgName = monitor.get("orgName");
                String msg = "监控对象:(" + brand + "@(" + orgName + ")" + ")查询报警记录";
                addLog(ip, msg, "3", "MONITORING", brand, plateColor);
            }
        } catch (Exception e) {
            logger.error("轨迹回放，日志管理异常" + e);
        }
    }

    @Override
    public void addSingleVehicleLog(String vehicleId, String ip, String message) {
        // 根据id查询组织架构实体
        try {
            final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
            final Map<String, String> monitor = RedisHelper.getHashMap(key, "name", "plateColor", "orgName");
            if (monitor != null) {
                String brand = monitor.get("name");
                String plateColor = monitor.get("plateColor");
                String orgName = monitor.get("orgName");
                String msg = "监控对象:(" + brand + "@(" + orgName + ")" + ")" + message;
                addLog(ip, msg, "5", "MONITORING", brand, plateColor);
            }
        } catch (Exception e) {
            logger.error("单车登录小程序，日志管理异常" + e);
        }
    }

    @Override
    public List<LogSearch> getByTime(Long startTime, Long endTime, Set<String> orgIds) {
        String startTimeStr = DateUtil.getLongToDateStr(startTime * 1000, null);
        String endTimeStr = DateUtil.getLongToDateStr(endTime * 1000, null);
        return logSearchDao.getByTime(startTimeStr, endTimeStr, orgIds);
    }

    private static String convertLogSource(String logSource) {
        switch (logSource) {
            case "1":
                return "终端上传";
            case "2":
                return "平台下发";
            case "3":
                return "平台操作";
            case "4":
                return "APP操作";
            case "5":
                return "单车登录小程序";
            default:
                return logSource;
        }
    }

    @Override
    public String migrateLog(YearMonth month) {
        if (MIGRATION_LOCK.tryLock()) {
            try {
                final long begin = System.currentTimeMillis();

                final Date monthBeginTime =
                        Date.from(month.atDay(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                final Date endTime = YearMonth.now().equals(month)
                        ? new Date()
                        : Date.from(month.atEndOfMonth().atTime(LocalTime.MAX)
                                .atZone(ZoneId.systemDefault()).toInstant());
                String id = null;
                final int batchSize = 1000;

                final double totalSec =
                        Duration.between(monthBeginTime.toInstant(), endTime.toInstant()).get(ChronoUnit.SECONDS);
                Triple<Integer, Integer, Optional<LogSearch>> progress;
                Optional<LogSearch> example;
                int total = 0;
                int wrote = 0;
                Date beginTime = monthBeginTime;
                do {
                    progress = this.doMigrateLog(beginTime, endTime, id, batchSize);
                    // 记录条数、进度
                    total += progress.getLeft();
                    wrote += progress.getMiddle();
                    example = progress.getRight();
                    final String progressText = example.map(LogSearch::getEventDate).map(date -> {
                        final long processed =
                                Duration.between(monthBeginTime.toInstant(), date.toInstant()).get(ChronoUnit.SECONDS);
                        final double percent = processed * 100 / totalSec;
                        final String dateStr = DateUtil.formatDate(date, DateUtil.DATE_FORMAT_SHORT);
                        return String.format("%.2f%%[%s]", percent, dateStr);
                    }).orElse("-");
                    logger.info("zw_c_log日志迁移中，当前累计读取：[{}]条，实际写入[{}]条，进度：{}", total, wrote, progressText);
                    // 设置增量
                    if (example.isPresent()) {
                        beginTime = example.get().getEventDate();
                        id = example.get().getId();
                    }
                } while (progress.getLeft() == batchSize && example.isPresent());
                final long end = System.currentTimeMillis();
                final String result = String.format("迁移结果：查询出[%s]的数据共[%d]条，实际写入[%d]条，耗时%.2fs",
                        month, total, wrote, (end - begin) / 1000f);
                logger.info(result);
                return result;
            } finally {
                MIGRATION_LOCK.unlock();
            }
        } else {
            return "迁移中，请稍候……";
        }
    }

    /**
     * 复制数据 zw_c_log -> zw_log_xxx
     * <p>每批次提交一次事务，开始前取第一条数据作为该批次重复插入的判定，重复时直接跳过
     * <p>btw, sharding jdbc 不支持insert into select
     *
     * @param beginTime 开始时间
     * @param endTime   截止时间
     * @param id        增量查询id（不含）
     * @param batchSize 至多复制条数
     * @return 实际读取条数、实际写入条数、最后一条数据
     */
    private Triple<Integer, Integer, Optional<LogSearch>> doMigrateLog(
            Date beginTime, Date endTime, String id, int batchSize) {
        final List<LogSearch> records = logSearchDao.listTopRecordsByTime(beginTime, endTime, id, batchSize);
        if (records.isEmpty()) {
            return Triple.of(0, 0, Optional.empty());
        }
        final LogSearch example = records.get(records.size() - 1);
        if (Boolean.TRUE.equals(logSearchDao.checkIfExists(example.getId(), example.getEventDate()))) {
            return Triple.of(records.size(), 0, Optional.of(example));
        }
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        final TransactionStatus transaction = txManager.getTransaction(def);
        try {
            final int wrote = logSearchDao.batchInsert(records);
            txManager.commit(transaction);
            return Triple.of(records.size(), wrote, Optional.of(example));
        } catch (Exception e) {
            txManager.rollback(transaction);
            throw e;
        }
    }

}
