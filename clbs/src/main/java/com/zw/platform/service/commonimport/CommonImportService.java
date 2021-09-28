package com.zw.platform.service.commonimport;

import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.dto.ProgressDTO;

import java.util.List;

/**
 * 公共导入服务
 *
 * @author Zhang Yanhui
 * @since 2020/9/15 15:55
 */

public interface CommonImportService {

    /**
     * 查询导入进度
     *
     * @param module 模块
     * @param username 用户名
     * @return 各阶段进度列表
     */
    List<ProgressDTO> getImportProgress(ImportModule module, String username);
}
