package com.zw.talkback.service.baseinfo;

import com.zw.talkback.domain.intercom.info.OriginalModelInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 原始机型service
 */
public interface OriginalModelService {

    /**
     * 查询原始机型和对讲数据
     * @return list
     */
    List<OriginalModelInfo> findOriginalModelAndIntercomModel();

    /**
     * 新增原始机型
     */
    void addOriginalModelInfos(HttpServletRequest request);

    /**
     * 根据index查询原始机型
     */
    List<OriginalModelInfo> getOriginalModelByIndex(String index);

    /**
     * 获取所有的原始机型
     * @return
     */
    List<Map<Long, String>> getAllOriginalModel(String index);

    /**
     * 获取所有的原始机型
     * @return 原始机型
     */
    List<OriginalModelInfo> getAllOriginalModel();

}
