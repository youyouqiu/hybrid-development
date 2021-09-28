package com.zw.platform.basic.imports.handler;

import com.google.common.collect.Maps;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.domain.UserGroupDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.dto.UserGroupDTO;
import com.zw.platform.basic.dto.imports.OrgGroupNumberDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zw.platform.dto.constant.VehicleConstant.ASSIGNMENT_MAX_COUNT;
import static com.zw.platform.util.imports.lock.ImportTable.ZW_M_ASSIGNMENT;
import static com.zw.platform.util.imports.lock.ImportTable.ZW_M_ASSIGNMENT_USER;

/**
 * 信息配置分组导入
 * 1.企业下的分组数量不能大于100
 * 2.如果分组存在, 需要判断分组下的监控对象数量不能大于500
 * 3.如果分组不存在, 则需要新增分组
 * @author zhangjuan
 */
@Slf4j
public class ConfigGroupImportHandler extends BaseImportHandler {
    private final ConfigImportHolder holder;
    private final GroupService groupService;
    private final GroupMonitorService groupMonitorService;
    private final UserGroupService userGroupService;

    /**
     * 企业下的分组信息 orgId -> 分组名称 -> 分组ID (excel中存在的企业下)
     */
    private Map<String, Map<String, String>> orgGroupMap;

    /**
     * 分组id和名称的映射关系
     */
    private Map<String, String> groupIdNameMap;

    /**
     * 分组下监控对象的数量 分组ID -- 监控对象数量
     */
    private Map<String, Integer> groupMonitorNum;

    private int errorRow;

    /**
     * 新增分组列表
     */
    private List<GroupDO> newGroupList;
    /**
     * 新增分组与监控对象绑定列表
     */
    private List<GroupMonitorDO> newGroupMonitorList;

    /**
     * 组织下分组的编号
     */
    private Map<String, OrgGroupNumberDTO> orgGroupNumberMap;

    /**
     * 新增分组赋权限的用户与分组关系列表
     */
    private List<UserGroupDTO> userGroupList;

    public ConfigGroupImportHandler(ConfigImportHolder holder, GroupService groupService,
        GroupMonitorService groupMonitorService, UserGroupService userGroupService) {
        this.holder = holder;
        this.groupService = groupService;
        this.groupMonitorService = groupMonitorService;
        this.userGroupService = userGroupService;
        this.errorRow = 0;

    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public int stage() {
        return 3;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ZW_M_ASSIGNMENT, ZW_M_ASSIGNMENT_USER };
    }

    @Override
    public boolean uniqueValid() {
        prepareInitData();
        return valid();
    }

    @Override
    public boolean addMysql() {
        if (CollectionUtils.isEmpty(this.newGroupList)) {
            return true;
        }
        partition(this.newGroupList, groupService::addByBatch);

        //默认分配分组权限给当前用户及其所属当前企业及上级企业下管理员
        this.userGroupList = groupService.getNewGroupOwnUser(this.newGroupList);
        List<UserGroupDO> tempList = userGroupList.stream().map(UserGroupDO::new).collect(Collectors.toList());
        partition(tempList, userGroupService::batchAddToDb);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        if (CollectionUtils.isEmpty(this.newGroupList)) {
            return;
        }
        groupService.addToRedis(this.newGroupList);
        userGroupService.batchAddToRedis(this.userGroupList);
    }

    private boolean valid() {
        this.newGroupList = new ArrayList<>();
        this.newGroupMonitorList = new ArrayList<>();
        final Map<String, String> orgNameIdMap = holder.getOrgMap();
        for (BindDTO config : holder.getImportList()) {
            final String orgId = orgNameIdMap.get(config.getOrgName());
            final Map<String, String> groupNameIdMap = orgGroupMap.getOrDefault(orgId, Collections.emptyMap());
            //导入的数据的分组名称不为空的情况
            if (StringUtils.isNotBlank(config.getGroupName())) {
                handleGroupNotBlank(config, groupNameIdMap);
            } else {
                handleGroupIsBank(config, orgId, groupNameIdMap);
            }

        }
        holder.setNewGroupMonitorList(this.newGroupMonitorList);
        //新增分组 分组插入数据库 给相关用户赋权限
        progressBar.setTotalProgress(newGroupList.size() * 3 + 1);
        return errorRow == 0;
    }

    /**
     * 导入的数据中分组信息不为空时分组的处理逻辑
     * @param config         导入数据
     * @param groupNameIdMap 分组名称与id的映射关系（同一企业下的）
     */
    private void handleGroupNotBlank(BindDTO config, Map<String, String> groupNameIdMap) {
        final Set<String> groupNameArr = new HashSet<>(Arrays.asList(config.getGroupName().split(",")));
        for (String groupName : groupNameArr) {
            final String groupId = groupNameIdMap.get(groupName);
            if (StringUtils.isBlank(groupId)) {
                config.setErrorMsg("【分组名称: " + groupName + " 】不存在.");
                errorRow++;
                continue;
            }

            int monitorCount = groupMonitorNum.getOrDefault(groupId, 0);
            // 校验分组下的监控对象数量已经达到分组的上限
            if (monitorCount >= holder.getGroupMaxMonitorNum()) {
                groupMonitorNum.remove(groupId);
                config.setErrorMsg("【分组 : " + groupName + "】下的监控对象数大于" + holder.getGroupMaxMonitorNum());
                errorRow++;
                continue;
            }
            groupMonitorNum.put(groupId, monitorCount + 1);
            addGroupMonitor(config, groupId);
        }
    }

    private void addGroupMonitor(BindDTO config, String groupId) {
        GroupMonitorDO groupMonitorDO = new GroupMonitorDO(config.getId(), config.getMonitorType(), groupId);
        this.newGroupMonitorList.add(groupMonitorDO);
        String groupIds = config.getGroupId();
        if (StringUtils.isNotBlank(groupIds)) {
            groupIds += "," + groupId;
        } else {
            groupIds = groupId;
        }
        config.setGroupId(groupIds);
    }

    private void handleGroupIsBank(BindDTO config, String orgId, Map<String, String> groupNameIdMap) {
        final OrgGroupNumberDTO numberDTO = this.orgGroupNumberMap.getOrDefault(orgId, new OrgGroupNumberDTO());
        //检查优先从企业下获取可用的分组
        String groupId = getUsableGroup(orgId, config.getOrgName(), numberDTO);
        if (StringUtils.isNotBlank(groupId)) {
            final Integer monitorCount = this.groupMonitorNum.getOrDefault(groupId, 0);
            this.groupMonitorNum.put(groupId, monitorCount + 1);
            addGroupMonitor(config, groupId);
            //回写分组名称
            config.setGroupName(this.groupIdNameMap.get(groupId));
            return;
        }

        // 企业下的分组数大于100, 并且没有可用的分组
        if (groupNameIdMap.size() >= ASSIGNMENT_MAX_COUNT) {
            config.setErrorMsg("【企业: " + config.getOrgName() + "】下的分组数超过" + ASSIGNMENT_MAX_COUNT);
            errorRow++;
            return;
        }

        //生成企业下新的分组编号，若小于0，则代表分组已经使用完
        final int newGroupNumber = numberDTO.nextAssignmentNumber();
        if (newGroupNumber < 0) {
            config.setErrorMsg("【企业: " + config.getOrgName() + "】下的分组数超过" + ASSIGNMENT_MAX_COUNT);
            errorRow++;
            return;
        }

        final String newGroupName = config.getOrgName() + newGroupNumber;
        config.setGroupName(newGroupName);
        // 新增分组
        final GroupDO groupDO = buildGroupDO(newGroupName, orgId);
        groupId = groupDO.getId();
        this.newGroupList.add(groupDO);

        //维护企业下新的分组编号
        this.orgGroupNumberMap.putIfAbsent(orgId, numberDTO);
        //更新分组数量关系
        this.groupMonitorNum.put(groupId, 1);
        //更新企业下的分组的映射关系
        this.orgGroupMap.computeIfAbsent(orgId, k -> new HashMap<>(16)).put(newGroupName, groupId);
        //更新分组id和名称映射关系
        this.groupIdNameMap.put(groupId, newGroupName);
        addGroupMonitor(config, groupId);
    }

    private GroupDO buildGroupDO(String newGroupName, String orgId) {
        GroupDO groupDO = new GroupDO();
        groupDO.setId(UUID.randomUUID().toString());
        groupDO.setName(newGroupName);
        groupDO.setOrgId(orgId);
        groupDO.setCreateDataTime(new Date());
        groupDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        groupDO.setFlag(1);
        groupDO.setTypes("0");
        return groupDO;
    }

    private String getUsableGroup(String orgId, String orgName, OrgGroupNumberDTO numberDTO) {
        int minNumber = numberDTO.getMinNumber();
        while (minNumber <= numberDTO.getMaxNumber()) {
            final String groupName = orgName + minNumber;
            final String groupId = this.orgGroupMap.getOrDefault(orgId, Maps.newHashMap()).get(groupName);
            if (StringUtils.isBlank(groupId)) {
                return null;
            }
            //监控数量未达上限直接用该分组
            final Integer monitorCount = this.groupMonitorNum.get(groupId);
            if (Objects.isNull(monitorCount) || monitorCount < holder.getGroupMaxMonitorNum()) {
                return groupId;
            }

            //监控数量已达上限,继续寻找下一个分组
            minNumber++;
        }
        return null;
    }

    private void prepareInitData() {
        final List<BindDTO> importList = holder.getImportList();
        final Map<String, String> orgNameIdMap = holder.getOrgMap();
        //获取到企业ID的集合
        final Set<String> orgIdSet =
            importList.stream().map(o -> orgNameIdMap.get(o.getOrgName())).collect(Collectors.toSet());

        //获取企业下所有的分组
        final List<GroupDTO> groupList = groupService.getGroupsByOrgIds(orgIdSet);
        this.orgGroupMap = groupList.stream()
            .collect(Collectors.groupingBy(GroupDTO::getOrgId, Collectors.toMap(GroupDTO::getName, GroupDTO::getId)));
        this.groupIdNameMap = AssembleUtil.collectionToMap(groupList, GroupDTO::getId, GroupDTO::getName);

        //获取所有的分组ID
        final Set<String> groupIds = groupIdNameMap.keySet();
        if (CollectionUtils.isEmpty(groupIds)) {
            this.groupMonitorNum = new HashMap<>(16);
            this.orgGroupNumberMap = new HashMap<>(16);
            return;
        }

        //获取分组下的监控对象
        List<GroupMonitorDTO> list = groupMonitorService.getByGroupIds(groupIds);
        this.groupMonitorNum =
            list.stream().collect(Collectors.groupingBy(GroupMonitorDTO::getGroupId, Collectors.summingInt(x -> 1)));

        this.orgGroupNumberMap = getOrgGroupNumbers();
    }

    private Map<String, OrgGroupNumberDTO> getOrgGroupNumbers() {
        final Map<String, String> orgIdNameMap = holder.getOrgIdNameMap();
        Map<String, OrgGroupNumberDTO> orgGroupNumberMap = new HashMap<>(16);
        orgGroupMap.forEach((orgId, groupMap) -> {
            int[] groupNumArr = new int[ASSIGNMENT_MAX_COUNT];
            // 初始化最大编号下标为最小编号小标
            int maxIndex = Integer.MIN_VALUE;
            final String orgName = orgIdNameMap.get(orgId);
            for (String groupName : groupMap.keySet()) {
                if (!groupName.startsWith(orgName)) {
                    continue;
                }
                final String groupNumberStr = groupName.substring(orgName.length());

                try {
                    // 这里的分组编号为0~99999, 但是限制平台现在最多只有100个分组, 所以用一个数组来记录即可
                    int groupNumber = Integer.parseInt(groupNumberStr);
                    if (groupNumber < ASSIGNMENT_MAX_COUNT) {
                        maxIndex = Math.max(groupNumber, maxIndex);
                        groupNumArr[groupNumber] = 1;
                    }
                } catch (Exception e) {
                    // 不做任何处理
                }
                if (maxIndex == Integer.MIN_VALUE) {
                    maxIndex = 0;
                }
                orgGroupNumberMap.put(orgId, new OrgGroupNumberDTO(groupNumArr, maxIndex, 0));
            }
        });
        return orgGroupNumberMap;
    }
}
