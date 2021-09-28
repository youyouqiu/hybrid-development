package com.zw.talkback.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.util.StringUtil;
import com.zw.talkback.domain.basicinfo.Cluster;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工具类-》IO处理工具类-》 json 操作工具类 <p> [依赖 fastJson.jar] </p>
 */
public class JsonUtil {

    /**
     * 将JSON解析成map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> json2Map(final String json) {
        return (Map<String, Object>) JSON.parse(json);
    }

    /**
     * 将JSON解析成对象
     */
    public static <T> T json2Object(final String json, final Class<T> c) {
        return JSON.parseObject(json, c);
    }

    /**
     * 将字符串包装成json数组
     */
    public static String warpJson2ListJson(final String json) {
        String jsonStr = json;
        if (!StringUtil.isNullOrBlank(json)) {
            if (!json.startsWith("[")) {
                jsonStr = "[" + json + "]";
            }
        }
        return jsonStr;
    }

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
            return new ArrayList<>();
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
     * @param cluster   要添加的分组对象
     * @param organization 该Assignment对象在要显示的树中的父结点
     * @param treeType     树的类型，single为单选，multiple为多选
     * @param array        用于树显示的JSONArray
     */
    public static void addAssignmentObj(final Cluster cluster, final OrganizationLdap organization,
        final String treeType, JSONArray array) {
        JSONObject assignmentObj = new JSONObject();
        assignmentObj.put("id", cluster.getId());
        assignmentObj.put("pId", organization == null ? "" : organization.getId().toString());
        assignmentObj.put("name", cluster.getName());
        assignmentObj.put("type", "assignment");
        assignmentObj.put("iconSkin", "assignmentSkin");
        assignmentObj.put("open", true);
        if ("single".equals(treeType)) { // 根节点是否可选
            assignmentObj.put("nocheck", true);
        }
        assignmentObj.put("isParent", true);
        array.add(assignmentObj);
    }

    public static void addAssignmentObjNum(final Cluster cluster, final OrganizationLdap organization,
        final String treeType, JSONArray array) {
        JSONObject assignmentObj = new JSONObject();
        assignmentObj.put("id", cluster.getId());
        assignmentObj.put("pId", organization == null ? "" : organization.getId().toString());
        assignmentObj.put("name", cluster.getName());
        assignmentObj.put("count", cluster.getMNum() == null ? 0 : cluster.getMNum());
        assignmentObj.put("type", "assignment");
        assignmentObj.put("iconSkin", "assignmentSkin");
        //assignmentObj.put("open", true);
        if ("single".equals(treeType)) { // 根节点是否可选
            assignmentObj.put("nocheck", true);
        }
        /* if (cluster.getMNum() > 0) {
            assignmentObj.put("isParent", true);
        }*/
        array.add(assignmentObj);
    }

    public static JSONObject assembleVehicleObject(VehicleInfo vehicle) {
        JSONObject vehicleObj = new JSONObject();
        vehicleObj.put("id", vehicle.getId());
        vehicleObj.put("name", vehicle.getBrand());
        vehicleObj.put("pId", vehicle.getAssignmentId());
        if ("0".equals(vehicle.getMonitorType())) {
            vehicleObj.put("type", "vehicle");
            vehicleObj.put("monitorType", "0");
            vehicleObj.put("iconSkin", "vehicleSkin");
        } else if ("1".equals(vehicle.getMonitorType())) {
            vehicleObj.put("type", "people");
            vehicleObj.put("monitorType", "1");
            vehicleObj.put("iconSkin", "peopleSkin");
        } else if ("2".equals(vehicle.getMonitorType())) {
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
    public static JSONArray getGroupTree(List<OrganizationLdap> orgs, String type, Boolean isNoCheck,
        Set<String> hasUseGroups) {
        JSONArray array = new JSONArray();
        // 组装组织树
        if (orgs != null && !orgs.isEmpty()) {
            for (OrganizationLdap group : orgs) {
                String cid = group.getCid();
                if (!hasUseGroups.contains(cid)) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                // 组装group数结构
                obj.put("id", cid);
                obj.put("pId", group.getPid());
                obj.put("name", group.getName());
                obj.put("iconSkin", "groupSkin");
                obj.put("type", "group");
                obj.put("open", false);
                obj.put("uuid", group.getUuid());
                if ("single".equals(type)) { // 根节点是否可选
                    obj.put("nocheck", true);
                }
                if ("ou=organization".equals(cid)) {
                    obj.put("pId", "0");
                }
                // 节点是否可选
                obj.put("nocheck", isNoCheck);
                array.add(obj);
            }
        }
        return array;
    }

    /**
     * 组装组织树结构
     *
     * @author wangying
     */

    public static JSONArray getGroupTree(List<OrganizationLdap> orgs, String type) {
        JSONArray array = new JSONArray();
        // 组装组织树
        if (orgs != null && !orgs.isEmpty()) {
            for (OrganizationLdap group : orgs) {
                JSONObject obj = new JSONObject();
                // 组装group数结构
                obj.put("id", group.getCid());
                obj.put("pId", group.getPid());
                obj.put("name", group.getName());
                obj.put("iconSkin", "groupSkin");
                obj.put("type", "group");
                obj.put("open", true);
                obj.put("uuid", group.getUuid());
                if ("single".equals(type)) { // 根节点是否可选
                    obj.put("nocheck", true);
                }
                if ("ou=organization".equals(group.getCid())) {
                    obj.put("pId", "0");
                }
                array.add(obj);
            }
        }
        return array;
    }

    public static JSONArray getGroupTree(List<OrganizationLdap> orgs, String type, Boolean isNoCheck) {
        JSONArray array = new JSONArray();
        // 组装组织树
        if (orgs != null && !orgs.isEmpty()) {
            for (OrganizationLdap group : orgs) {
                JSONObject obj = new JSONObject();
                // 组装group数结构
                obj.put("id", group.getCid());
                obj.put("pId", group.getPid());
                obj.put("name", group.getName());
                obj.put("iconSkin", "groupSkin");
                obj.put("type", "group");
                obj.put("open", true);
                obj.put("uuid", group.getUuid());
                if ("single".equals(type)) { // 根节点是否可选
                    obj.put("nocheck", true);
                }
                if ("ou=organization".equals(group.getCid())) {
                    obj.put("pId", "0");
                }
                // 节点是否可选
                obj.put("nocheck", isNoCheck);
                array.add(obj);
            }
        }
        return array;
    }

    public static JSONArray getUserTree(Map<String, List<UserBean>> map, Set<Name> roleMembers,
        LdapName currentUserName) {
        JSONArray array = new JSONArray();
        for (String pid : map.keySet()) {
            List<UserBean> userBeanList = map.get(pid);
            for (UserBean userBean : userBeanList) {
                JSONObject obj = new JSONObject();
                obj.put("pId", pid);
                obj.put("uuid", userBean.getUuid());
                obj.put("id", userBean.getId().toString());
                obj.put("type", "user");
                obj.put("iconSkin", "userSkin");
                obj.put("name", userBean.getUsername());
                if (roleMembers.contains(userBean.getMember())) {
                    obj.put("checked", true);
                    if (currentUserName.equals(userBean.getMember())) {
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
    // ////////////////////////jackson///////////////////////////////////////////////////
    // ObjectMapper 线程安全具有缓存机制，重用可显著提高效率，实际使用中可设为全局公用
    // @Getter
    // private static ObjectMapper mapper = new ObjectMapper();
    //
    // /**
    // * 将JSON解析成map
    // */
    // @SuppressWarnings("unchecked")
    // public static Map<String, Object> json2Map(final String json) {
    // Map<String, Object> map = null;
    // try {
    // map = mapper.readValue(json, Map.class);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return map;
    // }
    //
    // /**
    // * 将JSON解析成对象
    // */
    // public final Object json2Object(final String json, final Class<T> c) {
    // Object obj = null;
    // try {
    // obj = mapper.readValue(json, c);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return obj;
    // }
    //
    // /**
    // * 将字符串包装成json数组
    // */
    // public static String warpJson2ListJson(final String json) {
    // String jsonStr = "";
    // if (!Strings.isNullOrEmpty(json)) {
    // if (!json.startsWith("[")) {
    // jsonStr = "[" + json + "]";
    // }
    // }
    // return jsonStr;
    // }
    //
    // /**
    // * 将JSON解析成对象list
    // */
    // public final List<T> json2List(final String json) {
    // String jsonStr = "";
    // if (!Strings.isNullOrEmpty(json)) {
    // if (!json.startsWith("[")) {
    // jsonStr = "[" + json + "]";
    // }
    // List<T> list = null;
    // try {
    // list = mapper.readValue(jsonStr, new TypeReference<List<T>>() {});
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return list;
    // } else {
    // return null;
    // }
    // }

    // public static void main(String[] args) {
    // String s = "[{\"taskItemId\":\"\",\"naming\":\"\",\"startTime\":\"\",\"endTime\":\"\",\"weight\":\"\"}]";
    // IoJsonUtil<ProjectPlanDetailForm> u = new IoJsonUtil<ProjectPlanDetailForm>();
    // List<ProjectPlanDetailForm> m = u.json2List(s);
    //
    // }
}
