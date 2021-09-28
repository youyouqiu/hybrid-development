package com.zw.platform.domain.basicinfo;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Data;

/**
 * @author Chen Feng
 * @version 1.0 2018/2/27
 */
@Data
public class AssignCache {
    private List<String> assignIds;
    private List<String> assignNames;
    private Map<String, String> assignGroupMap;
    private Map<String, String> assignNameMap;

    public AssignCache() {
        assignIds = Lists.newArrayList();
        assignNames = Lists.newArrayList();
        assignGroupMap = Maps.newHashMap();
        assignNameMap = Maps.newHashMap();
    }

    public void addId(String id) {
        assignIds.add(id);
    }

    public void addName(String name) {
        assignNames.add(name);
    }

    public void addGroup(String id, String group) {
        assignGroupMap.put(id, group);
    }
    
    public void addNameMap(String id, String name) {
        assignNameMap.put(id, name);
    }
}
