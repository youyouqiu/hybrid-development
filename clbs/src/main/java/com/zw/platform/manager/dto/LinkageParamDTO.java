package com.zw.platform.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 报警联动数据同步
 * @author create by zhouzongbo on 2020/10/9.
 */
@Data
@AllArgsConstructor
public class LinkageParamDTO implements Serializable {
    private static final long serialVersionUID = 6554734235074960358L;

    /**
     * 需要删除的监控对象报警类型信息
     * key: 监控对象ID, value: 报警类型集合
     */
    private Map<String, Set<String>> deleteMonitorAlarmTypeCache;

    /**
     * 需要新增的监控对象报警类型信息
     * key: 监控对象ID, value: 报警类型集合
     */
    private Map<String, Set<String>> addMonitorAlarmTypeCache;
}
