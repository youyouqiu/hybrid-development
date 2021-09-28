package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.query.OfflineExportQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 离线报表导出dao层
 * @author XK
 */
public interface OfflineExportDao {
    /**
     * 根据digestId查询 用户名。
     * @param digestId 摘要id
     * @return list 用户名
     */
    List<String> getUserNamesByDigestId(String digestId);

    /**
     * 根据digestId查询离线报表导出详情
     * @param digestId 摘要
     * @return OfflineExportInfo
     */
    OfflineExportInfo getOfflineExportByDigestId(String digestId);

    /**
     * 查询离线报表导出列表
     * @param query 查询条件
     * @return list
     */
    Page<OfflineExportInfo> getOfflineExportBySimpleQuery(@Param("query") OfflineExportQuery query);

    /**
     * 新增离线离线导出逻辑
     * @param offlineExportInfo
     */
    void addOfflineExportInfo(OfflineExportInfo offlineExportInfo);

    void deleteOfflineExport(String deleteTime);

    Set<String> selectExportRealPath(String deleteTime);

    void updateExportStatus(@Param("updateTime") String updateTime, @Param("status") int status);
}
