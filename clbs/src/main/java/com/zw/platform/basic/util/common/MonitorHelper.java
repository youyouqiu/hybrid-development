package com.zw.platform.basic.util.common;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.MonitorBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zjc
 * @Description:监控对象通用工具类（为了避免在替换修改的时候出现重复类名以及统一管理）
 * @Date: create in 2021/1/29 14:02
 */
@Component
public class MonitorHelper {
    private Map<MonitorTypeEnum, MonitorBaseService> monitorBaseServiceMap = new HashMap<>();

    @Autowired
    public void initService(List<MonitorBaseService> monitorBaseServices) {
        for (MonitorBaseService monitorBaseService : monitorBaseServices) {
            monitorBaseServiceMap.put(monitorBaseService.getMonitorEnum(), monitorBaseService);
        }
    }

    public BindDTO getBindDTO(String monitorId, MonitorTypeEnum monitorTypeEnum) {
        return monitorBaseServiceMap.get(monitorTypeEnum).getById(monitorId);
    }

}
