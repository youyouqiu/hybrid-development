package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.ProtocolInfo;

import java.util.List;
import java.util.Map;

/**
 *
 * @Author zhangqiang
 * @Date 2020/5/14 15:40
 */
public interface ProtocolUtilDao {
    /**
     * 通过协议类型获取协议
     * @param type  808 OR 809
     * @return map
     */
    List<ProtocolInfo> findProtocolByType(Integer type);

    /**
     * 获取主动安全参数设置协议类型
     * @return map
     */
    List<Map<String, String>> findActiveSafetyProtocol();
}
