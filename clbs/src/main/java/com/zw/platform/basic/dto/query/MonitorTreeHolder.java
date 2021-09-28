package com.zw.platform.basic.dto.query;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.domain.basicinfo.MonitorAccStatus;
import com.zw.platform.domain.core.OrganizationLdap;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 组织+分组+监控对象树获取的中间临时变量--存放中途获取的常用数据
 * --避免重复从db、redis等重复查询
 * --避免方法参数太多，降低了代码可读性
 * @author zhangjuan
 */
@Data
public class MonitorTreeHolder {
    /**
     * 返回的树节点集合
     */
    private JSONArray treeNodes;
    /**
     * 树形结构中，最终的分组信息
     */
    private List<GroupDTO> groupList;

    /**
     * 树形结构中，最终的组织信息
     */
    private List<OrganizationLdap> orgList;
    /**
     * 分组-监控对象集合Map
     */
    private Map<String, Set<String>> groupMonitorSetMap;

    /**
     * 在线监控对象集合
     */
    private Set<String> onlineIds;

    /**
     * 符合条件的监控对象集合
     */
    private Set<String> monitorSet;

    /**
     * 监控对象和监控对象状态及ACC状态映射
     */
    private Map<String, MonitorAccStatus> accAndStatusMap;
    
    /**
     * 是否需要获取ACC状态 true 需要
     */
    private boolean needAccStatus = false;

    public Set<String> getMonitorSet() {
        if (monitorSet != null) {
            return monitorSet;
        }
        if (Objects.isNull(groupMonitorSetMap)) {
            return new HashSet<>();
        }
        monitorSet = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : groupMonitorSetMap.entrySet()) {
            monitorSet.addAll(entry.getValue());
        }
        return monitorSet;
    }

}
