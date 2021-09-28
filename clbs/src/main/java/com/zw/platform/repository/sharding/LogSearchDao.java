package com.zw.platform.repository.sharding;

import com.zw.platform.domain.reportManagement.LogSearch;
import com.zw.platform.domain.reportManagement.VideoLog;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.domain.reportManagement.query.LogSearchQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Title: 日志Dao
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: wangying
 * @date 2017年4月7日上午9:07:35
 */
public interface LogSearchDao {

    /**
     * 查询日志
     */
    List<LogSearch> findLog(LogSearchQuery query);

    /**
     * 查询音视频日志
     */
    List<VideoLog> findVideoLogCount(LogSearchQuery query);

    /**
     * 查询音视频日志
     */
    List<VideoLog> findVideoLogDetail(LogSearchQuery query);

    /**
     * 添加日志
     */
    boolean addLog(LogSearchForm form);

    /**
     * 查询实时监控日志
     */
    List<LogSearch> findLogByModule(@Param("startTime") String startTime,
                                    @Param("endTime") String endTime,
                                    @Param("module") String module);

    /**
     * 通过时间查询日志
     */
    List<LogSearch> getByTime(@Param("startTime") String startTime,
                              @Param("endTime") String endTime,
                              @Param("orgIds") Set<String> orgIds);

    /**
     * 查询旧表数据，数据迁移专用
     *
     * @param id 可选，用于增量查询
     * @return 数据列表，排序方式为 时间+id
     */
    List<LogSearch> listTopRecordsByTime(@Param("beginTime") Date beginTime,
                                         @Param("endTime") Date endTime,
                                         @Param("id") String id,
                                         @Param("batchSize") int batchSize);

    /**
     * 检查是否存在
     */
    Boolean checkIfExists(@Param("id") String id, @Param("eventDate") Date eventDate);

    /**
     * 批量写入
     */
    int batchInsert(Collection<LogSearch> records);
}
