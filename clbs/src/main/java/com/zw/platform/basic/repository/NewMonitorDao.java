package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.BaseKvDo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author wanxing
 * @Title: 监控对象Dao
 * @date 2020/11/615:29
 */
public interface NewMonitorDao {

    List<String> getMonitorIdByOrgId(String orgId);

    /**
     * 根据监控对象id查询监控对象id-name的映射关系
     * @param monitorIds
     * @param search
     * @return
     */
    @MapKey("keyName")
    Map<String, BaseKvDo<String, String>> getMonitorIdNameMap(@Param("monitorIds") Collection<String> monitorIds,
        @Param("search") String search);

    /**
     * 根据监控对象id查询下发的参数id集合
     * @param monitorId
     * @return
     */
    List<String> findSendParmId(String monitorId);
}
