package com.zw.platform.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.domain.GroupMonitorBindDO;
import com.zw.platform.basic.domain.GroupMonitorCountDo;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import org.apache.commons.collections.CollectionUtils;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工具类-》IO处理工具类-》 json 操作工具类 <p> [依赖 fastJson.jar] </p>
 */
public class JsonUtil {

    /**
     * 将JSON解析成对象list
     */
    public static <T> List<T> json2List(final String json, final Class<T> c) {
        String jsonStr = json;
        if (!StringUtil.isNullOrBlank(json)) {
            if (!json.startsWith("[")) {
                jsonStr = "[" + json + "]";
            }
            return JSON.parseArray(jsonStr, c);
        } else {
            return new ArrayList<T>();
        }
    }


    /**
     * 将对象转换成json
     */
    public static String object2Json(final Object entity) {
        return JSON.toJSONString(entity);
    }

    /**
     * 将分组对象转化为JSONObject，并添加到指定的JSONArray里，用于树的显示
     * @param assignment   要添加的分组对象
     * @param organization 该Assignment对象在要显示的树中的父结点
     * @param treeType     树的类型，single为单选，multiple为多选
     * @param array        用于树显示的JSONArray
     */
    public static void addAssignmentObj(final Assignment assignment, final OrganizationLdap organization,
        final String treeType, JSONArray array) {
        JSONObject assignmentObj = new JSONObject();
        assignmentObj.put("id", assignment.getId());
        assignmentObj.put("pId", organization == null ? "" : organization.getId().toString());
        assignmentObj.put("name", assignment.getName());
        assignmentObj.put("type", "assignment");
        assignmentObj.put("iconSkin", "assignmentSkin");
        assignmentObj.put("open", true);
        if ("single".equals(treeType)) { // 根节点是否可选
            assignmentObj.put("nocheck", true);
        }
        assignmentObj.put("isParent", true);
        array.add(assignmentObj);
    }

    public static void addAssignmentObjNum(final Assignment assignment, final OrganizationLdap organization,
        final String treeType, JSONArray array) {
        JSONObject assignmentObj = new JSONObject();
        assignmentObj.put("id", assignment.getId());
        assignmentObj.put("pId", organization == null ? "" : organization.getId().toString());
        assignmentObj.put("name", assignment.getName());
        assignmentObj.put("count", assignment.getMNum() == null ? 0 : assignment.getMNum());
        assignmentObj.put("type", "assignment");
        assignmentObj.put("iconSkin", "assignmentSkin");
        //assignmentObj.put("open", true);
        if ("single".equals(treeType)) { // 根节点是否可选
            assignmentObj.put("nocheck", true);
        }
        if (assignment.getMNum() > 0) {
            assignmentObj.put("isParent", true);
        }
        array.add(assignmentObj);
    }

    /**
     * 构建分组节点树
     */
    public static void addGroupJsonObj(final GroupDTO groupDTO, final OrganizationLdap organization,
        final String treeType, JSONArray array) {
        JSONObject groupJsonObj = new JSONObject();
        groupJsonObj.put("id", groupDTO.getId());
        groupJsonObj.put("pId", organization == null ? "" : organization.getId().toString());
        groupJsonObj.put("name", groupDTO.getName());
        groupJsonObj.put("count", groupDTO.getMonitorCount() == null ? 0 : groupDTO.getMonitorCount());
        groupJsonObj.put("type", "assignment");
        groupJsonObj.put("iconSkin", "assignmentSkin");
        // 根节点是否可选
        if ("single".equals(treeType)) {
            groupJsonObj.put("nocheck", true);
        }
        if (groupDTO.getMonitorCount() > 0) {
            groupJsonObj.put("isParent", true);
        }
        array.add(groupJsonObj);
    }

    public static JSONObject assembleVehicleObject(VehicleInfo vehicle) {
        return assembleMonitorBase(vehicle.getId(), vehicle.getBrand(), vehicle.getAssignmentId(),
            vehicle.getMonitorType());
    }

    public static JSONObject assembleVehicleObject(GroupMonitorDTO groupMonitorDTO) {
        return assembleMonitorBase(groupMonitorDTO.getMonitorId(), groupMonitorDTO.getBrand(),
            groupMonitorDTO.getGroupId(), groupMonitorDTO.getMonitorType());
    }

    private static JSONObject assembleMonitorBase(String id, String brand, String groupId, String monitorType) {
        JSONObject vehicleObj = new JSONObject();
        vehicleObj.put("id", id);
        vehicleObj.put("name", brand);
        vehicleObj.put("pId", groupId);
        if ("0".equals(monitorType)) {
            vehicleObj.put("type", "vehicle");
            vehicleObj.put("monitorType", "0");
            vehicleObj.put("iconSkin", "vehicleSkin");
        } else if ("1".equals(monitorType)) {
            vehicleObj.put("type", "people");
            vehicleObj.put("monitorType", "1");
            vehicleObj.put("iconSkin", "peopleSkin");
        } else if ("2".equals(monitorType)) {
            vehicleObj.put("type", "thing");
            vehicleObj.put("monitorType", "2");
            vehicleObj.put("iconSkin", "thingSkin");
        }
        return vehicleObj;
    }

    /**
     * 组装组织树结构
     * @author wangying
     */
    public static JSONArray getOrgTree(Collection<OrganizationLdap> orgList, String type) {
        // 组装组织树
        if (CollectionUtils.isEmpty(orgList)) {
            return new JSONArray();
        }
        JSONArray array = new JSONArray();
        for (OrganizationLdap group : orgList) {
            JSONObject obj = new JSONObject();
            // 组装group数结构
            obj.put("id", group.getCid());
            obj.put("pId", group.getPid());
            obj.put("name", group.getName());
            obj.put("iconSkin", "groupSkin");
            obj.put("type", "group");
            obj.put("open", true);
            obj.put("uuid", group.getUuid());
            // 根节点是否可选
            if ("single".equals(type)) {
                obj.put("nocheck", true);
            }
            if ("ou=organization".equals(group.getCid())) {
                obj.put("pId", "0");
            }
            array.add(obj);
        }
        return array;
    }

    /**
     * 组装分组树结构
     * @param groupMonitorCountList 分组信息及分组下的监控对象数量
     * @param orgMap                企业信息map
     * @param type                  用于组织和分组的根节点是否可选 single：nocheck=true multiple 不组装 nocheck
     */
    public static JSONArray assembleGroupTree(Collection<GroupMonitorCountDo> groupMonitorCountList,
        Map<String, OrganizationLdap> orgMap, String type) {
        if (CollectionUtils.isEmpty(groupMonitorCountList)) {
            return new JSONArray();
        }
        JSONArray treeJsonArr = new JSONArray();
        for (GroupMonitorCountDo groupMonitorCountDo : groupMonitorCountList) {
            String orgId = groupMonitorCountDo.getOrgId();
            OrganizationLdap organizationLdap = orgMap.get(orgId);
            // 组装分组树
            JSONObject assignmentObj = new JSONObject();
            assignmentObj.put("count", groupMonitorCountDo.getMonitorCount());
            assignmentObj.put("id", groupMonitorCountDo.getId());
            assignmentObj.put("pId", organizationLdap.getId().toString());
            assignmentObj.put("name", groupMonitorCountDo.getName());
            assignmentObj.put("type", "assignment");
            assignmentObj.put("iconSkin", "assignmentSkin");
            // 根节点是否可选
            if ("single".equals(type)) {
                assignmentObj.put("nocheck", true);
            }
            // 有子节点
            assignmentObj.put("isParent", true);
            treeJsonArr.add(assignmentObj);
        }
        return treeJsonArr;
    }

    /**
     * 组装监控对象树节点
     * @param groupMonitorBindDO 绑定信息
     * @param isChecked          是否选择
     */
    public static JSONObject assembleMonitorObjectTreeNode(GroupMonitorBindDO groupMonitorBindDO, boolean isChecked) {
        JSONObject monitorObjectTreeNode = new JSONObject();
        monitorObjectTreeNode.put("id", groupMonitorBindDO.getMoId());
        String monitorType = groupMonitorBindDO.getMonitorType();
        if (MonitorTypeEnum.VEHICLE.getType().equals(monitorType)) {
            monitorObjectTreeNode.put("type", "vehicle");
            monitorObjectTreeNode.put("iconSkin", "vehicleSkin");
        } else if (MonitorTypeEnum.PEOPLE.getType().equals(monitorType)) {
            monitorObjectTreeNode.put("type", "people");
            monitorObjectTreeNode.put("iconSkin", "peopleSkin");
        } else if (MonitorTypeEnum.THING.getType().equals(monitorType)) {
            monitorObjectTreeNode.put("type", "thing");
            monitorObjectTreeNode.put("iconSkin", "thingSkin");
        }
        monitorObjectTreeNode.put("pId", groupMonitorBindDO.getGroupId());
        monitorObjectTreeNode.put("name", groupMonitorBindDO.getMoName());
        monitorObjectTreeNode.put("deviceNumber", groupMonitorBindDO.getDeviceNumber());
        monitorObjectTreeNode.put("deviceType", groupMonitorBindDO.getDeviceType());
        monitorObjectTreeNode.put("plateColor", groupMonitorBindDO.getPlateColor());
        monitorObjectTreeNode.put("simcardNumber", groupMonitorBindDO.getSimCardNumber());
        monitorObjectTreeNode.put("assignName", groupMonitorBindDO.getGroupName());
        monitorObjectTreeNode.put("aliases", groupMonitorBindDO.getAliases());
        if (isChecked) {
            monitorObjectTreeNode.put("checked", true);
        }
        return monitorObjectTreeNode;
    }

    public static JSONArray getUserTree(Map<String, List<UserDTO>> map, Set<Name> roleMembers,
        LdapName currentUserName) {
        JSONArray array = new JSONArray();
        for (String pid : map.keySet()) {
            List<UserDTO> userBeanList = map.get(pid);
            for (UserDTO userDTO : userBeanList) {
                JSONObject obj = new JSONObject();
                obj.put("pId", pid);
                obj.put("uuid", userDTO.getUuid());
                obj.put("id", userDTO.getId().toString());
                obj.put("type", "user");
                obj.put("iconSkin", "userSkin");
                obj.put("name", userDTO.getUsername());
                if (roleMembers.contains(userDTO.getMember())) {
                    obj.put("checked", true);
                    if (currentUserName.equals(userDTO.getMember())) {
                        obj.put("chkDisabled", true);
                    }
                } else {
                    obj.put("checked", false);
                }

                array.add(obj);
            }
        }
        return array;
    }

    public static JSONArray getVehicleTree(List<VehicleInfo> vehicleList) {
        JSONArray result = new JSONArray();
        if (vehicleList != null && vehicleList.size() > 0) {
            for (VehicleInfo vehicle : vehicleList) {
                JSONObject vehicleObj = new JSONObject();
                vehicleObj.put("id", vehicle.getId());
                vehicleObj.put("type", "vehicle");
                vehicleObj.put("iconSkin", "vehicleSkin");
                vehicleObj.put("pId", vehicle.getAssignmentId());
                vehicleObj.put("name", vehicle.getBrand());
                vehicleObj.put("deviceNumber", vehicle.getDeviceNumber());
                vehicleObj.put("isVideo", vehicle.getIsVideo());
                result.add(vehicleObj);
            }
        }
        return result;
    }
}
