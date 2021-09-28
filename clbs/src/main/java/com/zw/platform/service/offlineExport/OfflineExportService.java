package com.zw.platform.service.offlineExport;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.query.OfflineExportQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OfflineExportService {
    /**
     * 通过摘要id查找用户名
     * @param digestId 摘要
     * @return list 用户名
     */
    List<String> findUserNameByDigestId(String digestId);

    /**
     * 分页查询
     * @param query 查询条件
     * @return page
     */
    Page<OfflineExportInfo> getPageOfflineExport(OfflineExportQuery query);

    /**
     * 新增一个离线导出报表,如果文件已经导出完成，则需要返回推送给前端的map对象
     * @param offlineExportInfo
     */
    Map<String, String> addOfflineExport(OfflineExportInfo offlineExportInfo);

    /**
     * 删除此时间之前生成的导出数据
     * @param deleteTime
     */
    void deleteOfflineExport(String deleteTime);

    /**
     * 查询需要从fastDfs删除的文件地址
     * @param deleteTime
     * @return
     */
    Set<String> selectExportRealPath(String deleteTime);

    void updateExportStatus(String updateTime, int status);

    /**
     * 推送下载结果给用户
     */
    void senExportResultMsg(Map<String, String> sendMap);

    OfflineExportInfo getInfoByDigestId(String id);
}
