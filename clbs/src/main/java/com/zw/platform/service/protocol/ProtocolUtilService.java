package com.zw.platform.service.protocol;

import java.util.List;
import java.util.Map;

/**
 *
 * @Author zhangqiang
 * @Date 2020/5/14 15:30
 */
public interface ProtocolUtilService {
    List<Map<String, String>> findProtocolListByType(Integer type);

    List<Map<String, String>> findActiveSafetyProtocolList();
}
