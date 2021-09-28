package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.other.protocol.jl.query.JiLinVehicleSetListQuery;
import com.zw.platform.domain.other.protocol.jl.resp.JiLinVehicleSetResp;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.other.protocol.jl.JiLinVehicleService;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.TreeUtils;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.RedisQueryUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 10:39
 */
@Service
public class JiLinVehicleServiceImpl implements JiLinVehicleService {

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;
    @Autowired
    private ConnectionParamsConfigDao connectionParamsConfigDao;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupDao groupDao;
    @Autowired
    private AssignmentService assignmentService;

    @Override
    public List<PlantParam> get809ConnectionParamSetsByProtocolType(Integer protocolType) {
        return connectionParamsSetDao.get809ConnectionParamSetsByProtocolType(protocolType);
    }

    @Override
    public JSONArray getTree(String id, String queryType, String queryParam) throws Exception {
        JSONArray resultJsonArr = new JSONArray();
        List<VehicleInfo> forwardVehiclesList = connectionParamsConfigDao.findForwardVehiclesBySettingId(id);
        if (CollectionUtils.isEmpty(forwardVehiclesList)) {
            return resultJsonArr;
        }
        // 用户uuid
        String uuid = userService.getCurrentUserUuid();
        // 用户所属组织及下级组织
        List<OrganizationLdap> userOrgList = userService.getCurrentUseOrgList();
        if (CollectionUtils.isEmpty(userOrgList)) {
            return resultJsonArr;
        }
        Set<OrganizationLdap> resultOrgSet = new HashSet<>(userOrgList);
        List<String> resultOrgIdList = userOrgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        if (Objects.equals(queryType, "group") && StringUtils.isNotBlank(queryParam)) {
            // 重新组装企业返回的企业
            resultOrgSet.clear();
            resultOrgIdList.clear();
            userOrgList.stream().filter(org -> org.getName().contains(queryParam)).forEach(org -> {
                resultOrgIdList.add(org.getUuid());
                TreeUtils.getLowerOrg(resultOrgSet, userOrgList, org);
            });
        }
        if (CollectionUtils.isEmpty(resultOrgSet)) {
            return resultJsonArr;
        }
        // 权限下满足条件的分组
        List<Assignment> assignmentList;
        if (Objects.equals(queryType, "assignment") && StringUtils.isNotBlank(queryParam)) {
            assignmentList =
                assignmentService.findUserAssignmentFuzzy(uuid, resultOrgIdList, StringUtil.fuzzyKeyword(queryParam));
        } else {
            assignmentList = assignmentService.findUserAssignment(uuid, resultOrgIdList);
        }
        if (CollectionUtils.isEmpty(assignmentList)) {
            return resultJsonArr;
        }
        // 满足条件的车辆
        List<String> resultVehicleIdList;
        if (Objects.equals(queryType, "vehicle") && StringUtils.isNotBlank(queryParam)) {
            resultVehicleIdList =
                forwardVehiclesList.stream().filter(vehicle -> vehicle.getBrand().contains(queryParam))
                    .map(VehicleInfo::getId).collect(Collectors.toList());
        } else {
            resultVehicleIdList = forwardVehiclesList.stream().map(VehicleInfo::getId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(resultVehicleIdList)) {
            return resultJsonArr;
        }
        // 权限下满足条件的分组id
        List<String> ownAssignmentIdList = assignmentList.stream().map(Assignment::getId).collect(Collectors.toList());
        // 分组下的车辆
        List<VehicleInfo> assignmentVehicleList = groupDao.findVehicleByAssignmentFuzzy(ownAssignmentIdList, null);
        // 分组权限下满足条件的车辆
        List<VehicleInfo> resultVehicleList = assignmentVehicleList.stream()
            .filter(assignmentVehicle -> resultVehicleIdList.contains(assignmentVehicle.getId()))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resultVehicleList)) {
            return resultJsonArr;
        }
        Set<String> resultAssignmentIdSet =
            resultVehicleList.stream().map(VehicleInfo::getAssignmentId).collect(Collectors.toSet());
        List<Assignment> resultAssignmentList =
            assignmentList.stream().filter(assignment -> resultAssignmentIdSet.contains(assignment.getId()))
                .collect(Collectors.toList());
        // 重新组装返回的企业
        resultOrgSet.clear();
        Set<String> groupIds = resultAssignmentList.stream().map(Assignment::getGroupId).collect(Collectors.toSet());
        userOrgList.stream().filter(org -> groupIds.contains(org.getUuid()))
            .forEach(org -> TreeUtils.getLowerOrg(resultOrgSet, userOrgList, org));
        // 组装分组树结构
        resultJsonArr.addAll(assembleOpenAssignmentNodes(resultAssignmentList, resultOrgSet));
        // 组装组织树结构
        resultJsonArr.addAll(JsonUtil.getOrgTree(new ArrayList<>(resultOrgSet), null));
        // 组装车辆树结构
        resultVehicleList.forEach(vehicleInfo -> resultJsonArr.add(putMonitorTree(vehicleInfo)));
        return resultJsonArr;
    }

    private JSONArray assembleOpenAssignmentNodes(List<Assignment> assignmentList,
        Collection<OrganizationLdap> organizations) {
        JSONArray assignmentNodeJsonArr = new JSONArray();
        Map<String, OrganizationLdap> orgIdAndOrgInfoMap =
            organizations.stream().collect(Collectors.toMap(OrganizationLdap::getUuid, org -> org));
        for (Assignment assignment : assignmentList) {
            String assignmentGroupId = assignment.getGroupId();
            OrganizationLdap assignmentOrgInfo = orgIdAndOrgInfoMap.get(assignmentGroupId);
            if (assignmentOrgInfo == null) {
                continue;
            }
            // 组装分组树
            JSONObject assignmentNode = new JSONObject();
            assignmentNode.put("id", assignment.getId());
            assignmentNode.put("pId", assignmentOrgInfo.getId().toString());
            assignmentNode.put("name", assignment.getName());
            assignmentNode.put("type", "assignment");
            assignmentNode.put("iconSkin", "assignmentSkin");
            assignmentNode.put("pName", assignmentOrgInfo.getName());
            assignmentNodeJsonArr.add(assignmentNode);
        }
        return assignmentNodeJsonArr;
    }

    private JSONObject putMonitorTree(VehicleInfo vehicle) {
        JSONObject vehicleNode = new JSONObject();
        vehicleNode.put("id", vehicle.getId());
        vehicleNode.put("type", "vehicle");
        vehicleNode.put("iconSkin", "vehicleSkin");
        vehicleNode.put("pId", vehicle.getAssignmentId());
        vehicleNode.put("name", vehicle.getBrand());
        return vehicleNode;
    }

    @Override
    public Page<JiLinVehicleSetResp> getVehicleSetList(JiLinVehicleSetListQuery query) {
        String vehicleIds = query.getVehicleIds();
        if (StringUtils.isBlank(vehicleIds)) {
            return new Page<>();
        }
        String queryType = query.getQueryType();
        String simpleQueryParam = query.getSimpleQueryParam();
        List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
        List<JiLinVehicleSetResp> resultList = new ArrayList<>();
        if (StringUtils.isBlank(simpleQueryParam)) {
            List<String> resultVehicleIdList =
                vehicleIdList.stream().skip(query.getStart()).limit(query.getLength()).collect(Collectors.toList());
            Map<String, BindDTO> configMap =
                RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(resultVehicleIdList)).stream()
                    .map(o -> MapUtil.mapToObj(o, BindDTO.class))
                    .collect(Collectors.toMap(BindDTO::getId, Function.identity()));
            resultVehicleIdList.forEach(vehicleId -> {
                BindDTO configInfo = configMap.get(vehicleId);
                JiLinVehicleSetResp jiLinVehicleSetResp = new JiLinVehicleSetResp();
                jiLinVehicleSetResp.setId(vehicleId);
                jiLinVehicleSetResp.setMonitorName(configInfo.getName());
                jiLinVehicleSetResp.setUnloadType(
                    Objects.equals(queryType, "1") ? "违规车辆" : Objects.equals(queryType, "2") ? "报警车辆" : "--");
                jiLinVehicleSetResp.setPlateColorStr(PlateColor.getNameOrBlankByCode(configInfo.getPlateColor()));
                jiLinVehicleSetResp.setAssignmentNames(configInfo.getGroupName());
                jiLinVehicleSetResp.setGroupName(configInfo.getOrgName());
                resultList.add(jiLinVehicleSetResp);
            });
            return RedisQueryUtil.getListToPage(resultList, query, vehicleIdList.size());
        }
        List<BindDTO> configList = RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleIdList)).stream()
            .map(o -> MapUtil.mapToObj(o, BindDTO.class)).collect(Collectors.toList());
        final List<BindDTO> meetConditionConfigList =
            configList.stream().filter(configInfo -> configInfo.getName().contains(simpleQueryParam))
                .collect(Collectors.toList());
        meetConditionConfigList.stream().skip(query.getStart()).limit(query.getLength()).forEachOrdered(configInfo -> {
            JiLinVehicleSetResp jiLinVehicleSetResp = new JiLinVehicleSetResp();
            jiLinVehicleSetResp.setId(configInfo.getId());
            jiLinVehicleSetResp.setMonitorName(configInfo.getName());
            jiLinVehicleSetResp.setUnloadType(
                Objects.equals(queryType, "1") ? "违规车辆" : Objects.equals(queryType, "2") ? "报警车辆" : "--");
            jiLinVehicleSetResp.setPlateColorStr(PlateColor.getNameOrBlankByCode(configInfo.getPlateColor()));
            jiLinVehicleSetResp.setAssignmentNames(configInfo.getGroupName());
            jiLinVehicleSetResp.setGroupName(configInfo.getOrgName());
            resultList.add(jiLinVehicleSetResp);
        });
        return RedisQueryUtil.getListToPage(resultList, query, meetConditionConfigList.size());
    }

}
