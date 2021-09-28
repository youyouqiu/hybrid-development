package com.zw.platform.service.protocol.impl;

import com.zw.platform.domain.basicinfo.ProtocolInfo;
import com.zw.platform.repository.modules.ProtocolUtilDao;
import com.zw.platform.service.protocol.ProtocolUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 用于填加协议 相关操作
 * @Author zhangqiang
 * @Date 2020/5/14 15:30
 */
@Service
public class ProtocolUtilServiceImpl implements ProtocolUtilService {

    private static final Integer PROTOCOL_809_TYPE = 809;

    private static final Integer PROTOCOL_808_TYPE = 808;

    @Autowired
    private ProtocolUtilDao protocolUtilDao;

    @Override
    public List<Map<String, String>> findProtocolListByType(Integer type) {
        if (PROTOCOL_809_TYPE.equals(type) || PROTOCOL_808_TYPE.equals(type)) {
            List<ProtocolInfo> protocolByType = protocolUtilDao.findProtocolByType(type);
            List<Map<String, String>> result = new ArrayList<>();
            protocolByType.forEach(protocolInfo -> {
                Map<String, String> map = new HashMap<>();
                map.put("protocolName", protocolInfo.getProtocolName());
                map.put("protocolCode", protocolInfo.getProtocolCode());
                result.add(map);
            });
            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, String>> findActiveSafetyProtocolList() {
        return protocolUtilDao.findActiveSafetyProtocol();
    }

}
