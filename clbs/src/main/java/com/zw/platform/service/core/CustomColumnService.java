package com.zw.platform.service.core;

import com.zw.platform.domain.core.CustomColumnConfigInfo;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Map;

/**
 * 定制列services
 * @author zhouzongbo on 2019/3/11 15:24
 */
public interface CustomColumnService {

    JsonResultBean findCustomColumnInfoByMark(String marks) throws Exception;

    List<CustomColumnConfigInfo> findCustomColumnConfigInfoByMark(String mark);

    /**
     * 添加用户自定义列表绑定关系
     * @param customColumnConfigJson customColumnConfigJson
     * @param title                  title
     * @param ipAddress              ipAddress
     * @return
     * @throws Exception ex
     */
    JsonResultBean addCustomColumnConfig(String customColumnConfigJson, String title, String ipAddress)
        throws Exception;

    /**
     * 获取当前用户自定义列数据
     * @param columnModule columnModule
     * @return
     */
    List<CustomColumnConfigInfo> findUserCustomColumnInfo(String columnModule);

    /**
     * 获取用户自定列
     * @param bindList bindList
     * @param mark     mark
     * @return list
     */
    List<CustomColumnConfigInfo> getCustomColumnConfigInfos(List<CustomColumnConfigInfo> bindList, String mark);

    List<String> findCustomColumnTitleList(String mark);

    List<Map<String, Object>> findCustomColumnModule(String columnModule);

    /**
     * 删除用户定制列
     * @param columnId
     */
    void deleteUserMarkColumn(String columnId, String mark);

}
