package com.zw.talkback.service.redis.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.basicinfo.VehicleCacheItem;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.StringUtil;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.AssignmentVehicleForm;
import com.zw.talkback.domain.basicinfo.form.PersonnelForm;
import com.zw.talkback.repository.mysql.ClusterDao;
import com.zw.talkback.service.redis.RedisClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedisClusterServiceImpl implements RedisClusterService {

    private static final String ASSIGNMENT_MONITOR_KEY = "assignment_monitor";

    private static final String GROUP_ASSIGN = "group_assign";

    private UserService userService;

    private ClusterDao clusterDao;

    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setClusterDao(ClusterDao clusterDao) {
        this.clusterDao = clusterDao;
    }

    @Autowired
    public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * 添加用户分组权限
     * @param userList   用户id列表
     * @param assignList 分组id列表
     * @param session    session
     */
    @Override
    public void addUserAssignments(List<String> userList, List<String> assignList, HttpSession session) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     // 获取所有组织
        //     List<OrganizationLdap> organizations = userService.getOrgChild("ou=organization");
        //     List<Cluster> clusters = clusterDao.findAssignmentByBatch(assignList);
        //     Map<String, OrganizationLdap> orgMap = getOrganizationMap(organizations);
        //     String userKey;
        //     JSONArray array;
        //     int index = 0;
        //     JSONArray assignArray = initAssignMonitorCache(clusters, orgMap, jedisGet, pipeline);
        //     if (session != null) {
        //         session.setAttribute("CONFIG_IMPORT_PROGRESS", 80);
        //     }
        //     for (String user : userList) {
        //         userKey = RedisHelper.buildKey(user, "zw", "list");
        //         array = JSON.parseArray(jedisGet.get(userKey));
        //         if (array == null) {
        //
        //             //根据名字查询数据库所有数据
        //
        //             array = new JSONArray();
        //             pipeline.set(userKey, array.toJSONString());
        //             continue;
        //         }
        //         array.addAll(assignArray);
        //         pipeline.set(userKey, array.toJSONString());
        //         if (session != null) {
        //             session.setAttribute("CONFIG_IMPORT_PROGRESS", index++ * 10 / userList.size() + 80);
        //         }
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    private JSONArray initAssignMonitorCache(List<Cluster> clusters, Map<String, OrganizationLdap> orgMap,
        Jedis jedisGet, Pipeline pipeline) {
        // String assignKey;
        JSONArray assignArray = new JSONArray();
        // Map<String, List<String>> groupAndAssignIdMap = new HashMap<>(16);
        // for (Cluster assign : clusters) {
        //     OrganizationLdap org = orgMap.get(assign.getGroupId());
        //     if (org == null) {
        //         continue;
        //     }
        //     addAssignToGroup(assign.getId(), org.getUuid(), jedisGet, groupAndAssignIdMap);
        //     JSONObject object = assembleAssignJSONObject(assign, org);
        //     assignArray.add(object);
        //
        //     assignKey = RedisHelper.buildKey(assign.getId(), ASSIGNMENT_MONITOR_KEY, "list");
        //     pipeline.set(assignKey, "[]");
        // }
        // groupAndAssignIdMap.forEach((key, value) -> {
        //     String groupAssignKey = RedisHelper.buildKey(key, GROUP_ASSIGN, "list");
        //     pipeline.set(groupAssignKey, JSON.toJSONString(value));
        // });
        return assignArray;
    }

    private Map<String, OrganizationLdap> getOrganizationMap(List<OrganizationLdap> organizations) {
        Map<String, OrganizationLdap> map = new HashMap<>();
        for (OrganizationLdap organization : organizations) {
            map.put(organization.getUuid(), organization);
        }
        return map;
    }

    /**
     * 添加分组到组织-分组缓存
     * @param assignId          分组id
     * @param orgId             分组所属组织id
     * @param jedisGet          读redis的客户端
     * @param groupAndAssignMap
     */
    private void addAssignToGroup(String assignId, String orgId, Jedis jedisGet,
        Map<String, List<String>> groupAndAssignMap) {
        // List<String> assignIdList;
        // if (groupAndAssignMap.containsKey(orgId)) {
        //     assignIdList = groupAndAssignMap.get(orgId);
        // } else {
        //     String key = RedisHelper.buildKey(orgId, GROUP_ASSIGN, "list");
        //     assignIdList = JSON.parseArray(jedisGet.get(key), String.class);
        //     if (assignIdList == null) {
        //         assignIdList = new ArrayList<>();
        //     }
        // }
        // assignIdList.add(assignId);
        // groupAndAssignMap.put(orgId, assignIdList);
    }

    /**
     * 组装群组信息JSON对象
     * @param assign 分组信息
     * @param org    分组所属组织
     * @return 分组信息JSON对象
     */
    private JSONObject assembleAssignJSONObject(Cluster assign, OrganizationLdap org) {
        JSONObject object = new JSONObject();
        object.put("id", assign.getId());
        object.put("name", assign.getName());
        object.put("pId", org.getId().toString());
        object.put("groupId", assign.getGroupId());
        object.put("groupName", org.getName());
        object.put("type", "assignment");
        object.put("iconSkin", "assignmentSkin");
        object.put("types", "cluster");
        return object;
    }

    /**
     * 删除分组相关缓存
     * @param clusters 分组列表
     */
    @Override
    public void deleteAssignmentsCache(List<Cluster> clusters) {

        taskExecutor.execute(() -> deleteUserAssigns(clusters));

        taskExecutor.execute(() -> deleteConfigAssigns(clusters));

        taskExecutor.execute(() -> deleteGroupAssigns(clusters));

        taskExecutor.execute(() -> deleteVehicleAssigns(clusters));

        deleteAssignMonitor(clusters);
    }

    /**
     * 删除用户分组缓存中的分组列表
     * @param assignList 待删除的分组列表
     */
    private void deleteUserAssigns(List<Cluster> assignList) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     List<UserBean> userList = userService.findAllUserUUID();
        //     String key;
        //     for (UserBean user : userList) {
        //         key = RedisHelper.buildKey(user.getUsername(), "zw", "list");
        //         JSONArray array = JSON.parseArray(jedisGet.get(key));
        //         if (array == null) {
        //             continue;
        //         }
        //         for (Cluster assign : assignList) {
        //             array.removeIf(p -> ((JSONObject) p).get("id").equals(assign.getId()));
        //         }
        //         pipeline.set(key, array.toJSONString());
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    /**
     * 删除绑定关系缓存中的分组列表
     * @param assignList 待删除的分组列表
     */
    private void deleteConfigAssigns(List<Cluster> assignList) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     String key;
        //     String vehicleKey;
        //     for (Cluster assign : assignList) {
        //         key = RedisHelper.buildKey(assign.getId(), ASSIGNMENT_MONITOR_KEY, "list");
        //         JSONArray vehicles = JSON.parseArray(jedisGet.get(key));
        //         if (vehicles == null) {
        //             continue;
        //         }
        //         for (Object obj : vehicles) {
        //             String vehicleId = (String) obj;
        //             vehicleKey = RedisHelper.buildKey(vehicleId, "config", "list");
        //             ConfigList config = JSON.parseObject(jedisGet.get(vehicleKey), ConfigList.class);
        //             if (config == null) {
        //                 // 车辆未绑定
        //                 continue;
        //             }
        //             removeAssignFromConfig(assign, config);
        //             pipeline.set(vehicleKey, JSON.toJSONString(config));
        //         }
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    /**
     * 删除组织分组缓存中的分组列表
     * @param assignList 待删除的分组列表
     */
    private void deleteGroupAssigns(List<Cluster> assignList) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     List<OrganizationLdap> orgList = userService.getAllOrganization();
        //     String key;
        //     for (OrganizationLdap org : orgList) {
        //         key = RedisHelper.buildKey(org.getUuid(), GROUP_ASSIGN, "list");
        //         List<String> assignIdList = JSON.parseArray(jedisGet.get(key), String.class);
        //         if (assignIdList == null) {
        //             continue;
        //         }
        //         for (Cluster cluster : assignList) {
        //             assignIdList.remove(cluster.getId());
        //         }
        //         pipeline.set(key, JSON.toJSONString(assignIdList));
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    /**
     * 删除车辆详情缓存中的分组列表
     * @param assignList 待删除的分组列表
     */
    private void deleteVehicleAssigns(List<Cluster> assignList) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     String key;
        //     String vehicleKey;
        //     for (Cluster assign : assignList) {
        //         key = RedisHelper.buildKey(assign.getId(), ASSIGNMENT_MONITOR_KEY, "list");
        //         JSONArray vehicles = JSON.parseArray(jedisGet.get(key));
        //         if (vehicles == null) {
        //             continue;
        //         }
        //         for (Object obj : vehicles) {
        //             String vehicleId = (String) obj;
        //             vehicleKey = RedisHelper.buildKey(vehicleId, "vehicle", "list");
        //             String vehicleCache = jedisGet.get(vehicleKey);
        //             if (vehicleCache == null || vehicleCache.equals("{}")) {
        //                 pipeline.set(vehicleKey, "{}");
        //                 continue;
        //             }
        //             VehicleCacheItem vehicleDetail = JSON.parseObject(vehicleCache, VehicleCacheItem.class);
        //             removeAssignFromVehicle(vehicleDetail, assign, "0");
        //             pipeline.set(vehicleKey, JSON.toJSONString(vehicleDetail));
        //         }
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    /**
     * 删除分组-监控对象缓存
     * @param clusters 分组列表
     */
    private void deleteAssignMonitor(List<Cluster> clusters) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     String key;
        //     for (Cluster assign : clusters) {
        //         key = RedisHelper.buildKey(assign.getId(), ASSIGNMENT_MONITOR_KEY, "list");
        //         if (jedisGet.exists(key)) {
        //             pipeline.del(key);
        //         }
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    /**
     * 更新分组名称
     * @param assignId 分组id
     * @param oldName  分组原名称
     * @param newName  分组新名称
     */
    @Override
    public void updateAssignment(String assignId, String oldName, String newName) {

        taskExecutor.execute(() -> updateUserAssign(assignId, newName));

        taskExecutor.execute(() -> updateConfigAssign(assignId, oldName, newName));

        //taskExecutor.execute(() -> updateVehicleAssign(assignId, oldName, newName));
    }

    /**
     * 在用户-分组缓存中更新分组名称
     * @param id      分组id
     * @param newName 分组新名称
     */
    private void updateUserAssign(String id, String newName) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     List<UserBean> userList = userService.findAllUserUUID();
        //     String key;
        //     for (UserBean user : userList) {
        //         key = RedisHelper.buildKey(user.getUsername(), "zw", "list");
        //         JSONArray assignList = JSON.parseArray(jedisGet.get(key));
        //         if (assignList == null) {
        //             assignList = new JSONArray();
        //         }
        //         for (Object obj : assignList) {
        //             JSONObject assign = (JSONObject) obj;
        //             if (Objects.equals(id, assign.getString("id"))) {
        //                 assign.put("name", newName);
        //                 break;
        //             }
        //         }
        //         pipeline.set(key, assignList.toJSONString());
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    /**
     * 在绑定关系缓存中更新分组名称
     * @param id      分组id
     * @param oldName 分组原名称
     * @param newName 分组新名称
     */
    private void updateConfigAssign(String id, String oldName, String newName) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     String vehicleKey;
        //     String configKey;
        //     String key = RedisHelper.buildKey(id, ASSIGNMENT_MONITOR_KEY, "list");
        //     JSONArray vehicles = JSON.parseArray(jedisGet.get(key));
        //     if (vehicles == null) {
        //         return;
        //     }
        //     for (Object obj : vehicles) {
        //         String vehicleId = (String) obj;
        //         configKey = RedisHelper.buildKey(vehicleId, "config", "list");
        //         ConfigList config = JSON.parseObject(jedisGet.get(configKey), ConfigList.class);
        //         if (config == null) {
        //             // 车辆未绑定
        //             return;
        //         }
        //         String monitorType = config.getMonitorType();
        //         if ("0".equals(monitorType)) {
        //             vehicleKey = RedisHelper.buildKey(vehicleId, "vehicle", "list");
        //             String vehicleCache = jedisGet.get(vehicleKey);
        //             if (vehicleCache != null || !"{}".equals(vehicleCache)) {
        //                 VehicleCacheItem vehicleDetail = JSON.parseObject(vehicleCache, VehicleCacheItem.class);
        //
        //                 Cluster oldAssign = new Cluster();
        //                 oldAssign.setId(id);
        //                 oldAssign.setName(oldName);
        //
        //                 Cluster newAssign = new Cluster();
        //                 newAssign.setId(id);
        //                 newAssign.setName(newName);
        //                 // 在车辆详情中更新分组信息
        //                 updateAssignInVehicle(vehicleDetail, oldAssign, newAssign, monitorType);
        //
        //                 pipeline.set(vehicleKey, JSON.toJSONString(vehicleDetail));
        //             }
        //         } else if ("1".equals(monitorType)) {
        //             vehicleKey = RedisHelper.buildKey(vehicleId, "people", "list");
        //             String vehicleCache = jedisGet.get(vehicleKey);
        //             if (vehicleCache != null || !"{}".equals(vehicleCache)) {
        //                 PersonnelForm personnelForm = JSON.parseObject(vehicleCache, PersonnelForm.class);
        //
        //                 Cluster oldAssign = new Cluster();
        //                 oldAssign.setId(id);
        //                 oldAssign.setName(oldName);
        //
        //                 Cluster newAssign = new Cluster();
        //                 newAssign.setId(id);
        //                 newAssign.setName(newName);
        //                 // 在车辆详情中更新分组信息
        //                 updateAssignInVehicle(personnelForm, oldAssign, newAssign, monitorType);
        //
        //                 pipeline.set(vehicleKey, JSON.toJSONString(personnelForm));
        //             }
        //         } else if ("2".equals(monitorType)) {
        //             vehicleKey = RedisHelper.buildKey(vehicleId, "thing", "list");
        //             String vehicleCache = jedisGet.get(vehicleKey);
        //             if (vehicleCache != null || !"{}".equals(vehicleCache)) {
        //                 ThingInfoForm thingInfoForm = JSON.parseObject(vehicleCache, ThingInfoForm.class);
        //
        //                 Cluster oldAssign = new Cluster();
        //                 oldAssign.setId(id);
        //                 oldAssign.setName(oldName);
        //
        //                 Cluster newAssign = new Cluster();
        //                 newAssign.setId(id);
        //                 newAssign.setName(newName);
        //                 // 在车辆详情中更新分组信息
        //                 updateAssignInVehicle(thingInfoForm, oldAssign, newAssign, monitorType);
        //
        //                 pipeline.set(vehicleKey, JSON.toJSONString(thingInfoForm));
        //             }
        //         }
        //         Cluster oldAssign = new Cluster();
        //         oldAssign.setId(id);
        //         oldAssign.setName(oldName);
        //         Cluster newAssign = new Cluster();
        //         newAssign.setId(id);
        //         newAssign.setName(newName);
        //         // 在config中更新分组信息
        //         updateAssignFromConfig(config, oldAssign, newAssign);
        //         pipeline.set(configKey, JSON.toJSONString(config));
        //     }
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet);
        // }
    }

    /**
     * 获取用户的分组信息
     * @param userID 用户ID
     */
    @Override
    public JSONArray getAssignmentByUserID(String userID) {
        return null;
        // // 获取指定的用户与组的关联关系的缓存数据
        // UserBean user = userService.findUser(userID);
        // String key = RedisHelper.buildKey(user.getUsername(), "zw", "list");
        // String targetUserJsonString = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);
        //
        // JSONArray array;
        // if (targetUserJsonString == null) {
        //     return new JSONArray();
        // }
        // array = JSON.parseArray(targetUserJsonString);
        // array.forEach(p -> ((JSONObject) p).put("checked", "true"));
        // // 获取当前的用户与组的关联关系的缓存数据
        // UserBean curUser = userService.findUser(SystemHelper.getCurrentUser().getId().toString());
        // if (Objects.equals(user.getId().toString(), curUser.getId().toString())) {
        //     return array;
        // }
        // key = RedisHelper.buildKey(curUser.getUsername(), "zw", "list");
        // String curUserJsonString = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);
        //
        // if (curUserJsonString == null) {
        //     return array;
        // }
        // JSONArray curUserAssignments = JSONArray.parseArray(curUserJsonString);
        //
        // // 查询指定用户的组织及下级组织
        // List<String> targetUserGroupList = userService.getOrgUuidsByUser(userID);
        // if (curUserAssignments == null) {
        //     return array;
        // }
        // for (Object curUserAssignment : curUserAssignments) {
        //     JSONObject obj = (JSONObject) curUserAssignment;
        //     if (targetUserGroupList.contains(obj.getString("groupId"))) {
        //         array.add(obj);
        //     }
        // }
        //
        // // 去除重复的项
        // HashSet<String> hashSet = new HashSet<>();
        // array.removeIf(p -> !hashSet.add(((JSONObject) p).getString("id")));
        //
        // return array;
    }

    /**
     * 更新指定的用户的分组授权
     * @param userID  用户ID
     * @param delList 待删除的分组ID列表
     * @param addList 待添加的分组ID列表
     */
    @Override
    public void updateAssignmentsByUserID(String userID, List<String> delList, List<String> addList) throws Exception {
        // // 如果待添加和删除的列表都为空则直接返回
        // if (delList.isEmpty() && addList.isEmpty()) {
        //     return;
        // }
        //
        // UserBean user = userService.findUser(userID);
        // // 获取用户与分组的关联关系的缓存数据
        // String key = RedisHelper.buildKey(user.getUsername(), "zw", "list");
        // String jsonString = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);
        //
        // if (jsonString != null) {
        //     // 根据分组ID删除缓存中的分组数据
        //     List<Map<String, String>> userAssignments;
        //     userAssignments = JSON.parseObject(jsonString, new TypeReference<List<Map<String, String>>>() {
        //     });
        //     if (userAssignments != null && !delList.isEmpty()) {
        //         userAssignments.removeIf(p -> delList.contains(p.get("id")));
        //     }
        //
        //     // 添加分组数据
        //     if (userAssignments != null && !addList.isEmpty()) {
        //         List<Cluster> clusters = clusterDao.findAssignmentByBatch(addList);
        //         clusters.forEach(assignment -> {
        //             OrganizationLdap group = userService.getOrgByUuid(assignment.getGroupId());
        //             Map<String, String> map = new HashMap<>();
        //             map.put("id", assignment.getId());
        //             map.put("name", assignment.getName());
        //             map.put("groupId", assignment.getGroupId());
        //             map.put("pId", group.getId().toString());
        //             map.put("groupName", group.getName());
        //             map.put("type", "assignment");
        //             map.put("iconSkin", "assignmentSkin");
        //             map.put("types", "cluster");
        //             userAssignments.add(map);
        //         });
        //     }
        //
        //     // 更新缓存数据
        //     Map<String, List<Map<String, String>>> userMap = new HashMap<>();
        //     userMap.put(key, userAssignments);
        //     RedisHelper.setMapValue(userMap, PublicVariable.REDIS_TEN_DATABASE);
        // }
    }

    private void removeAssignmentByUserId(String assignId, String userName) throws Exception {
        // // 获取指定的用户与分组的关联关系的缓存数据
        // String key = RedisHelper.buildKey(userName, "zw", "list");
        // String jsonString = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);
        // if (jsonString == null) {
        //     return;
        // }
        // // 根据分组ID删除缓存中的分组数据
        // List<Map<String, String>> userAssignments;
        // userAssignments = JSON.parseObject(jsonString, new TypeReference<List<Map<String, String>>>() {
        // });
        // if (userAssignments == null) {
        //     return;
        // }
        // userAssignments.removeIf(p -> assignId.equals(p.get("id")));
        // // 更新缓存数据
        // Map<String, List<Map<String, String>>> userMap = new HashMap<>();
        // userMap.put(key, userAssignments);
        // RedisHelper.setMapValue(userMap, PublicVariable.REDIS_TEN_DATABASE);
    }

    private void addAssignmentByUserId(String assignId, String userName) {
        // // 获取指定的用户与分组的关联关系的缓存数据
        // String key = RedisHelper.buildKey(userName, "zw", "list");
        // String jsonString = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);
        // if (jsonString == null) {
        //     return;
        // }
        // // 根据分组ID添加缓存中的分组数据
        // List<Map<String, String>> userAssignments;
        // userAssignments = JSON.parseObject(jsonString, new TypeReference<List<Map<String, String>>>() {
        // });
        // if (userAssignments == null) {
        //     return;
        // }
        //
        // Cluster cluster = clusterDao.findAssignmentById(assignId);
        // OrganizationLdap org = userService.getOrgByUuid(cluster.getGroupId());
        // if (org != null) {
        //     cluster.setGroupName(org.getName());
        // }
        //
        // Map<String, String> map = assembleAssignmentCache(cluster);
        // userAssignments.add(map);
        // // 更新缓存数据
        // Map<String, List<Map<String, String>>> userMap = new HashMap<>();
        // userMap.put(key, userAssignments);
        // RedisHelper.setMapValue(userMap, PublicVariable.REDIS_TEN_DATABASE);
    }

    private Map<String, String> assembleAssignmentCache(Cluster cluster) {
        Map<String, String> map = new HashMap<>();
        map.put("id", cluster.getId());
        map.put("name", cluster.getName());
        map.put("groupId", cluster.getGroupId());
        map.put("pId", userService.getOrgByUuid(cluster.getGroupId()).getId().toString());
        map.put("groupName", cluster.getGroupName());
        map.put("type", "assignment");
        map.put("iconSkin", "assignmentSkin");
        map.put("types", "cluster");
        return map;
    }

    /**
     * 更新分组的监控对象信息
     * @param delList 待删除的监控对象ID列表
     * @param addList 待添加的监控对象ID列表
     */
    @Override
    public void updateVehiclesCache(List<AssignmentVehicleForm> delList, List<AssignmentVehicleForm> addList) {
        // 如果待添加和删除的列表都为空则直接返回
        if ((delList == null || delList.isEmpty()) && (addList == null || addList.isEmpty())) {
            return;
        }

        // 更新分组-车辆缓存
        taskExecutor.execute(() -> updateAssignmentCache(delList, addList));

        // 更新config缓存
        taskExecutor.execute(() -> updateConfigCache(delList, addList));

        // 更新车辆信息缓存
        taskExecutor.execute(() -> updateVehicleCache(delList, addList));
    }

    /**
     * 更新分组-监控对象缓存
     * @param delList 待删除的分组列表
     * @param addList 待添加的分组列表
     */
    private void updateAssignmentCache(List<AssignmentVehicleForm> delList, List<AssignmentVehicleForm> addList) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     Pipeline pipeline = jedisSet.pipelined();
        //     removeAssignCache(delList, jedisGet, pipeline);
        //     addAssignCache(addList, jedisGet, pipeline);
        //     pipeline.sync();
        // } finally {
        //     RedisHelper.returnResource(jedisGet);
        //     RedisHelper.returnResource(jedisSet);
        // }
    }

    /**
     * 从分组-监控对象缓存中删除监控对象列表
     * @param delList  待删除的监控对象列表
     * @param jedisGet 读redis的客户端
     * @param pipeline 写redis的客户端
     */
    private void removeAssignCache(List<AssignmentVehicleForm> delList, Jedis jedisGet, Pipeline pipeline) {
        // String key;
        // Map<String, JSONArray> map = new HashMap<>();
        // for (AssignmentVehicleForm form : delList) {
        //     key = RedisHelper.buildKey(form.getAssignmentId(), ASSIGNMENT_MONITOR_KEY, "list");
        //     // 获取缓存中指定分组下的所有车辆
        //     JSONArray assigns = map.computeIfAbsent(key, k -> JSON.parseArray(jedisGet.get(k)));
        //     if (assigns == null) {
        //         continue;
        //     }
        //     // 删除车辆
        //     assigns.remove(form.getVehicleId());
        // }
        //
        // for (Map.Entry<String, JSONArray> entry : map.entrySet()) {
        //     pipeline.set(entry.getKey(), JSON.toJSONString(entry.getValue()));
        // }
    }

    /**
     * 向分组-监控对象缓存中添加分组列表
     * @param addList  待添加的分组列表
     * @param jedisGet 读redis的客户端
     * @param pipeline 写redis的客户端
     */
    private void addAssignCache(List<AssignmentVehicleForm> addList, Jedis jedisGet, Pipeline pipeline) {
        // String key;
        // Map<String, JSONArray> map = new HashMap<>();
        // for (AssignmentVehicleForm form : addList) {
        //     key = RedisHelper.buildKey(form.getAssignmentId(), ASSIGNMENT_MONITOR_KEY, "list");
        //     // 获取缓存中指定分组下的所有车辆
        //     JSONArray assigns = map.computeIfAbsent(key, k -> JSON.parseArray(jedisGet.get(k)));
        //     if (assigns == null) {
        //         assigns = new JSONArray();
        //     }
        //     // 添加车辆
        //     if (assigns.contains(form.getVehicleId())) {
        //         // 已经存在，不重复添加
        //         continue;
        //     }
        //     assigns.add(form.getVehicleId());
        //     map.put(key, assigns);
        // }
        // for (Map.Entry<String, JSONArray> entry : map.entrySet()) {
        //     pipeline.set(entry.getKey(), JSON.toJSONString(entry.getValue()));
        // }
    }

    /**
     * 更新绑定信息缓存
     * @param delList 待删除分组信息列表
     * @param addList 待添加分组信息列表
     */
    private void updateConfigCache(List<AssignmentVehicleForm> delList, List<AssignmentVehicleForm> addList) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // Jedis jedisGet1 = null;
        // Jedis jedisSet1 = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisGet1 = RedisHelper.getJedis(PublicVariable.REDIS_NINE_DATABASE);
        //     jedisSet1 = RedisHelper.getJedis(PublicVariable.REDIS_NINE_DATABASE);
        //     removeAssignListFromConfig(delList, jedisGet, jedisSet, jedisGet1, jedisSet1);
        //     addAssignListToConfig(addList, jedisGet, jedisSet, jedisGet1, jedisSet1);
        // } finally {
        //     RedisHelper.returnResource(jedisGet);
        //     RedisHelper.returnResource(jedisSet);
        //     RedisHelper.returnResource(jedisGet1);
        //     RedisHelper.returnResource(jedisSet1);
        // }
    }

    /**
     * 从车辆详情缓存中删除分组列表，如果在添加分组列表存在，则替换
     * @param delList  待删除的分组列表
     * @param jedisGet 读redis的客户端
     * @param jedisSet 写redis的客户端
     */
    private void removeAssignListFromConfig(List<AssignmentVehicleForm> delList, Jedis jedisGet, Jedis jedisSet,
        Jedis jedisGet1, Jedis jedisSet1) {
        // String key;
        // String key1;
        // for (AssignmentVehicleForm form : delList) {
        //     key = RedisHelper.buildKey(form.getVehicleId(), "config", "list");
        //     key1 = RedisHelper.buildKey(form.getVehicleId(), "intercom", "list");
        //     // 获取config缓存对象
        //     ConfigList configList = JSON.parseObject(jedisGet.get(key), ConfigList.class);
        //     IntercomObjectInfo configList1 = JSON.parseObject(jedisGet1.get(key1), IntercomObjectInfo.class);
        //     if (configList == null) {
        //         // 车辆未绑定
        //         continue;
        //     }
        //
        //     Cluster assign = new Cluster();
        //     assign.setId(form.getAssignmentId());
        //     assign.setName(form.getAssignmentName());
        //     removeAssignFromConfig(assign, configList);
        //     removeAssignFromConfig1(assign, configList1);
        //
        //     jedisSet.set(key, JSON.toJSONString(configList));
        //     jedisSet1.set(key1, JSON.toJSONString(configList1));
        // }
    }

    /**
     * 从绑定信息缓存中删除分组信息
     * @param assign     待删除的分组信息
     * @param configList 绑定信息缓存
     */
    private void removeAssignFromConfig(Cluster assign, ConfigList configList) {
        String assignIds = configList.getAssignmentId();
        String assignNames = configList.getAssignmentName();
        if (assignIds.contains(assign.getId())) {
            assignIds = assignIds.replaceAll(assign.getId() + ",?|,?" + assign.getId(), "");
            assignNames = assignNames.replaceFirst(assign.getName() + ",?|,?" + assign.getName(), "");
            configList.setAssignmentId(assignIds);
            configList.setAssignmentName(assignNames);
        }
    }

    private void removeAssignFromConfig1(Cluster assign, IntercomObjectInfo configList1) {
        String assignIds = configList1.getAssignmentId();
        String assignNames = configList1.getAssignmentName();
        if (assignIds.contains(assign.getId())) {
            assignIds = assignIds.replaceAll(assign.getId() + ",?|,?" + assign.getId(), "");
            assignNames = assignNames.replaceFirst(assign.getName() + ",?|,?" + assign.getName(), "");
            configList1.setAssignmentId(assignIds);
            configList1.setAssignmentName(assignNames);
            configList1.setCurrentGroupNum(configList1.getCurrentGroupNum() - 1);
        }
    }

    /**
     * 在绑定信息缓存中添加分组信息列表
     * @param addList  待添加的分组列表
     * @param jedisGet 读redis的客户端
     * @param jedisSet 写redis的客户端
     */
    private void addAssignListToConfig(List<AssignmentVehicleForm> addList, Jedis jedisGet, Jedis jedisSet,
        Jedis jedisGet1, Jedis jedisSet1) {
        // String key;
        // String key1;
        // for (AssignmentVehicleForm form : addList) {
        //     key = RedisHelper.buildKey(form.getVehicleId(), "config", "list");
        //     key1 = RedisHelper.buildKey(form.getVehicleId(), "intercom", "list");
        //     // 获取config缓存对象
        //     ConfigList configList = JSON.parseObject(jedisGet.get(key), ConfigList.class);
        //     IntercomObjectInfo configList1 = JSON.parseObject(jedisGet1.get(key1), IntercomObjectInfo.class);
        //     if (configList == null) {
        //         // 车辆未绑定
        //         continue;
        //     }
        //
        //     Cluster assign = new Cluster();
        //     assign.setId(form.getAssignmentId());
        //     assign.setName(form.getAssignmentName());
        //     addAssignInConfig(assign, configList);
        //     addAssignInConfig1(assign, configList1);
        //
        //     jedisSet.set(key, JSON.toJSONString(configList));
        //     jedisSet1.set(key1, JSON.toJSONString(configList1));
        // }
    }

    /**
     * 在绑定信息缓存中更新分组信息
     * @param configList 绑定信息缓存
     * @param oldAssign  更新前的分组信息
     * @param newAssign  更新后的分组信息
     */
    private void updateAssignFromConfig(ConfigList configList, Cluster oldAssign, Cluster newAssign) {
        String assignIds = configList.getAssignmentId();
        String assignNames = configList.getAssignmentName();
        if (assignIds.contains(oldAssign.getId())) {
            assignIds = StringUtil.replaceAssignName(assignIds, oldAssign.getId(), newAssign.getId());
            assignNames = StringUtil.replaceAssignName(assignNames, oldAssign.getName(), newAssign.getName());
            configList.setAssignmentId(assignIds);
            configList.setAssignmentName(assignNames);
        }
    }

    /**
     * 在绑定信息缓存中添加分组信息
     * @param assign     待添加的分组信息
     * @param configList 绑定信息缓存
     */
    private void addAssignInConfig(Cluster assign, ConfigList configList) {
        String assignIds = configList.getAssignmentId();
        String assignNames = configList.getAssignmentName();
        if (!assignIds.contains(assign.getId())) {
            assignIds = assignIds + (assignIds.isEmpty() ? assign.getId() : ("," + assign.getId()));
            assignNames = assignNames + (assignNames.isEmpty() ? assign.getName() : ("," + assign.getName()));
            configList.setAssignmentId(assignIds);
            configList.setAssignmentName(assignNames);
        }
    }

    private void addAssignInConfig1(Cluster assign, IntercomObjectInfo configList1) {
        String assignIds = configList1.getAssignmentId();
        String assignNames = configList1.getAssignmentName();
        if (!assignIds.contains(assign.getId())) {
            assignIds = assignIds + (assignIds.isEmpty() ? assign.getId() : ("," + assign.getId()));
            assignNames = assignNames + (assignNames.isEmpty() ? assign.getName() : ("," + assign.getName()));
            configList1.setAssignmentId(assignIds);
            configList1.setAssignmentName(assignNames);
            String[] split = assignNames.split(",");
            configList1.setCurrentGroupNum(configList1.getCurrentGroupNum() + 1);
        }
    }

    /**
     * 更新车辆详情缓存
     * @param delList 待删除分组信息列表
     * @param addList 待添加分组信息列表
     */
    private void updateVehicleCache(List<AssignmentVehicleForm> delList, List<AssignmentVehicleForm> addList) {
        // Jedis jedisGet = null;
        // Jedis jedisSet = null;
        // try {
        //     jedisGet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     jedisSet = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     removeAssignListFromVehicle(delList, jedisGet, jedisSet);
        //     addAssignListInVehicle(addList, jedisGet, jedisSet);
        // } finally {
        //     RedisHelper.returnResource(jedisGet);
        //     RedisHelper.returnResource(jedisSet);
        // }
    }

    private void removeAssignListFromVehicle(List<AssignmentVehicleForm> delList, Jedis jedisGet, Jedis jedisSet) {
        // String key = "";
        // for (AssignmentVehicleForm form : delList) {
        //     if ("0".equals(form.getMonitorType())) {
        //         key = RedisHelper.buildKey(form.getVehicleId(), "vehicle", "list");
        //         String vehicleInfo = jedisGet.get(key);
        //         VehicleCacheItem vehicleDetail = JSONObject.parseObject(vehicleInfo, VehicleCacheItem.class);
        //         if (vehicleDetail == null) {
        //             continue;
        //         }
        //         Cluster assign = new Cluster();
        //         assign.setId(form.getAssignmentId());
        //         assign.setName(form.getAssignmentName());
        //         removeAssignFromVehicle(vehicleDetail, assign, form.getMonitorType());
        //         jedisSet.set(key, JSON.toJSONString(vehicleDetail));
        //     } else if ("1".equals(form.getMonitorType())) {
        //         key = RedisHelper.buildKey(form.getVehicleId(), "people", "list");
        //         String peopleInfo = jedisGet.get(key);
        //         PersonnelForm personnelForm = JSONObject.parseObject(peopleInfo, PersonnelForm.class);
        //         if (personnelForm == null) {
        //             continue;
        //         }
        //         Cluster assign = new Cluster();
        //         assign.setId(form.getAssignmentId());
        //         assign.setName(form.getAssignmentName());
        //         removeAssignFromVehicle(personnelForm, assign, form.getMonitorType());
        //         jedisSet.set(key, JSON.toJSONString(personnelForm));
        //     } else if ("2".equals(form.getMonitorType())) {
        //         key = RedisHelper.buildKey(form.getVehicleId(), "thing", "list");
        //         String thingInfo = jedisGet.get(key);
        //         ThingInfoForm thingInfoForm = JSONObject.parseObject(thingInfo, ThingInfoForm.class);
        //         if (thingInfoForm == null) {
        //             continue;
        //         }
        //         Cluster assign = new Cluster();
        //         assign.setId(form.getAssignmentId());
        //         assign.setName(form.getAssignmentName());
        //         removeAssignFromVehicle(thingInfoForm, assign, form.getMonitorType());
        //         jedisSet.set(key, JSON.toJSONString(thingInfoForm));
        //     }
        // }
    }

    private void addAssignListInVehicle(List<AssignmentVehicleForm> addList, Jedis jedisGet, Jedis jedisSet) {
        // String key = "";
        // for (AssignmentVehicleForm form : addList) {
        //     if ("0".equals(form.getMonitorType())) {
        //         key = RedisHelper.buildKey(form.getVehicleId(), "vehicle", "list");
        //         String vehicleInfo = jedisGet.get(key);
        //         VehicleCacheItem vehicleDetail = JSONObject.parseObject(vehicleInfo, VehicleCacheItem.class);
        //         if (vehicleDetail == null) {
        //             continue;
        //         }
        //         Cluster assign = new Cluster();
        //         assign.setId(form.getAssignmentId());
        //         assign.setName(form.getAssignmentName());
        //         addAssignInVehicle(vehicleDetail, assign, form.getMonitorType());
        //         jedisSet.set(key, JSON.toJSONString(vehicleDetail));
        //     } else if ("1".equals(form.getMonitorType())) {
        //         key = RedisHelper.buildKey(form.getVehicleId(), "people", "list");
        //         String peopleInfo = jedisGet.get(key);
        //         PersonnelForm personnelForm = JSONObject.parseObject(peopleInfo, PersonnelForm.class);
        //         if (personnelForm == null) {
        //             continue;
        //         }
        //         Cluster assign = new Cluster();
        //         assign.setId(form.getAssignmentId());
        //         assign.setName(form.getAssignmentName());
        //         addAssignInVehicle(personnelForm, assign, form.getMonitorType());
        //         jedisSet.set(key, JSON.toJSONString(personnelForm));
        //     } else if ("2".equals(form.getMonitorType())) {
        //         key = RedisHelper.buildKey(form.getVehicleId(), "thing", "list");
        //         String thingInfo = jedisGet.get(key);
        //         ThingInfoForm thingInfoForm = JSONObject.parseObject(thingInfo, ThingInfoForm.class);
        //         if (thingInfoForm == null) {
        //             continue;
        //         }
        //         Cluster assign = new Cluster();
        //         assign.setId(form.getAssignmentId());
        //         assign.setName(form.getAssignmentName());
        //         addAssignInVehicle(thingInfoForm, assign, form.getMonitorType());
        //         jedisSet.set(key, JSON.toJSONString(thingInfoForm));
        //     }
        // }
    }

    /**
     * 从车辆详情缓存中删除分组
     * @param assign      待删除的分组信息
     * @param monitorType
     */
    private void removeAssignFromVehicle(Object obj, Cluster assign, String monitorType) {
        String assignIds = "";
        String assignNames = "";
        if ("0".equals(monitorType)) {
            VehicleCacheItem vehicleDetail = (VehicleCacheItem) obj;
            assignIds = vehicleDetail.getAssignId();
            assignNames = vehicleDetail.getAssign();
            if (assignIds.contains(assign.getId())) {
                assignIds = assignIds.replaceAll(assign.getId() + ",?|,?" + assign.getId(), "");
                assignNames = assignNames.replaceFirst(assign.getName() + ",?|,?" + assign.getName(), "");
                vehicleDetail.setAssignId(assignIds);
                vehicleDetail.setAssign(assignNames);
            }
        } else if ("1".equals(monitorType)) {
            PersonnelForm personnelForm = (PersonnelForm) obj;
            assignIds = personnelForm.getAssignId();
            assignNames = personnelForm.getAssign();
            if (assignIds.contains(assign.getId())) {
                assignIds = assignIds.replaceAll(assign.getId() + ",?|,?" + assign.getId(), "");
                assignNames = assignNames.replaceFirst(assign.getName() + ",?|,?" + assign.getName(), "");
                personnelForm.setAssignId(assignIds);
                personnelForm.setAssign(assignNames);
            }
        } else if ("2".equals(monitorType)) {
            ThingInfoForm thingInfoForm = (ThingInfoForm) obj;
            assignIds = thingInfoForm.getAssignId();
            assignNames = thingInfoForm.getAssign();
            if (assignIds.contains(assign.getId())) {
                assignIds = assignIds.replaceAll(assign.getId() + ",?|,?" + assign.getId(), "");
                assignNames = assignNames.replaceFirst(assign.getName() + ",?|,?" + assign.getName(), "");
                thingInfoForm.setAssignId(assignIds);
                thingInfoForm.setAssign(assignNames);
            }
        }
    }

    /**
     * 在车辆详情缓存中更新分组信息
     * @param obj         车辆详情缓存
     * @param assign      待更新的分组信息
     * @param monitorType
     */
    private void addAssignInVehicle(Object obj, Cluster assign, String monitorType) {
        String assignIds = "";
        String assignNames = "";
        if ("0".equals(monitorType)) {
            VehicleCacheItem vehicleDetail = (VehicleCacheItem) obj;
            assignIds = vehicleDetail.getAssignId();
            assignNames = vehicleDetail.getAssign();
            if (!assignIds.contains(assign.getId())) {
                assignIds = assignIds + (assignIds.isEmpty() ? assign.getId() : ("," + assign.getId()));
                assignNames = assignNames + (assignNames.isEmpty() ? assign.getName() : ("," + assign.getName()));
                vehicleDetail.setAssignId(assignIds);
                vehicleDetail.setAssign(assignNames);
            }
        } else if ("1".equals(monitorType)) {
            PersonnelForm personnelForm = (PersonnelForm) obj;
            assignIds = personnelForm.getAssignId();
            assignNames = personnelForm.getAssign();
            if (!assignIds.contains(assign.getId())) {
                assignIds = assignIds + (assignIds.isEmpty() ? assign.getId() : ("," + assign.getId()));
                assignNames = assignNames + (assignNames.isEmpty() ? assign.getName() : ("," + assign.getName()));
                personnelForm.setAssignId(assignIds);
                personnelForm.setAssign(assignNames);
            }
        } else if ("2".equals(monitorType)) {
            ThingInfoForm thingInfoForm = (ThingInfoForm) obj;
            assignIds = thingInfoForm.getAssignId();
            assignNames = thingInfoForm.getAssign();
            if (!assignIds.contains(assign.getId())) {
                assignIds = assignIds + (assignIds.isEmpty() ? assign.getId() : ("," + assign.getId()));
                assignNames = assignNames + (assignNames.isEmpty() ? assign.getName() : ("," + assign.getName()));
                thingInfoForm.setAssignId(assignIds);
                thingInfoForm.setAssign(assignNames);
            }
        }
    }

    /**
     * 在车辆详情中更新分组信息
     * @param obj         车辆详情
     * @param oldAssign   原分组信息
     * @param newAssign   新分组信息
     * @param monitorType
     */
    private void updateAssignInVehicle(Object obj, Cluster oldAssign, Cluster newAssign, String monitorType) {
        String assignIds = "";
        String assignNames = "";
        if ("0".equals(monitorType)) {
            VehicleCacheItem vehicleDetail = (VehicleCacheItem) obj;
            assignIds = vehicleDetail.getAssignId();
            assignNames = vehicleDetail.getAssign();
            if (assignIds.contains(oldAssign.getId())) {
                assignIds = StringUtil.replaceAssignName(assignIds, oldAssign.getId(), newAssign.getId());
                assignNames = StringUtil.replaceAssignName(assignNames, oldAssign.getName(), newAssign.getName());
                vehicleDetail.setAssignId(assignIds);
                vehicleDetail.setAssign(assignNames);
            }
        } else if ("1".equals(monitorType)) {
            PersonnelForm personnelForm = (PersonnelForm) obj;
            assignIds = personnelForm.getAssignId();
            assignNames = personnelForm.getAssign();
            if (assignIds.contains(oldAssign.getId())) {
                assignIds = StringUtil.replaceAssignName(assignIds, oldAssign.getId(), newAssign.getId());
                assignNames = StringUtil.replaceAssignName(assignNames, oldAssign.getName(), newAssign.getName());
                personnelForm.setAssignId(assignIds);
                personnelForm.setAssign(assignNames);
            }
        } else if ("2".equals(monitorType)) {
            ThingInfoForm thingInfoForm = (ThingInfoForm) obj;
            assignIds = thingInfoForm.getAssignId();
            assignNames = thingInfoForm.getAssign();
            if (assignIds.contains(oldAssign.getId())) {
                assignIds = StringUtil.replaceAssignName(assignIds, oldAssign.getId(), newAssign.getId());
                assignNames = StringUtil.replaceAssignName(assignNames, oldAssign.getName(), newAssign.getName());
                thingInfoForm.setAssignId(assignIds);
                thingInfoForm.setAssign(assignNames);
            }
        }
    }

}
