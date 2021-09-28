package com.zw.platform.service.reportManagement.impl;

import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.connectionparamsset_809.OrgInspectionExtraUserDO;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.dto.platformInspection.Zw809MessageDTO;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.modules.OrgInspectionExtraUserDAO;
import com.zw.platform.repository.modules.Zw809MessageDao;
import com.zw.platform.service.reportManagement.InspectionAndSupervisionService;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InspectionAndSupervisionServiceImpl implements InspectionAndSupervisionService {

    @Autowired
    private Zw809MessageDao zw809MessageDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrgInspectionExtraUserDAO orgInspectionExtraUserDAO;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;

    @Override
    public List<Zw809MessageDTO> getList(String groupIds, String type, String startTime, String endTime,
                                         Integer status) {
        RedisKey key = HistoryRedisKeyEnum.INSPECTION_SUPERVISION_LIST.of(SystemHelper.getCurrentUsername());
        RedisHelper.delete(key);
        String[] groupIdList = groupIds.split(",");
        Integer msgType;
        // 应该是magic
        switch (type) {
            case "11":
                msgType = 0;
                break;
            case "21":
                msgType = 1;
                break;
            case "12":
                msgType = 2;
                break;
            case "22":
                msgType = 3;
                break;
            default:
                msgType = null;
        }
        Integer msgStatus;
        switch (status) {
            // 未处理
            case 1:
                msgStatus = 0;
                break;
            // 已处理
            case 2:
                msgStatus = 1;
                break;
            // 已过期
            case 3:
                msgStatus = 2;
                break;
            default:
                msgStatus = null;
        }
        List<Zw809MessageDTO> theDayAllMsgByGroup =
            zw809MessageDao.getTheDayAllMsgByGroup(startTime, endTime, groupIdList, msgType, msgStatus);
        if (CollectionUtils.isNotEmpty(theDayAllMsgByGroup)) {
            // 组织名称
            Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(Sets.newHashSet(groupIdList));
            // 平台名称
            final Set<String> platformIds =
                    theDayAllMsgByGroup.stream().map(Zw809MessageDTO::getPlatformId).collect(Collectors.toSet());
            final Map<String, String> platformMap = connectionParamsSetDao.listPlatformNameByIdIn(platformIds).stream()
                    .collect(Collectors.toMap(BaseFormBean::getId, PlantParam::getPlatformName, (o, p) -> o));

            for (Zw809MessageDTO zw809Message : theDayAllMsgByGroup) {
                final String orgName = Optional.ofNullable(orgMap.get(zw809Message.getGroupId()))
                        .map(OrganizationLdap::getName)
                        .orElse("");
                zw809Message.setGroupName(orgName);
                final String platformName = platformMap.getOrDefault(zw809Message.getPlatformId(), "");
                zw809Message.setPlatformName(platformName);
            }
            RedisHelper.addToList(key, theDayAllMsgByGroup);
            RedisHelper.expireKey(key, 60 * 60);
        }

        return theDayAllMsgByGroup;
    }

    @Override
    public void exportList(String title, HttpServletResponse res) throws Exception {
        RedisKey key = HistoryRedisKeyEnum.INSPECTION_SUPERVISION_LIST.of(SystemHelper.getCurrentUsername());
        List<Zw809MessageDTO> zw809MessageList = RedisHelper.getList(key, Zw809MessageDTO.class);
        if (CollectionUtils.isEmpty(zw809MessageList)) {
            return;
        }

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Zw809MessageDTO zw809Message : zw809MessageList) {
            Integer type = zw809Message.getType();
            if (type != null) {
                switch (type) {
                    case 0:
                        zw809Message.setTypeStr("JTT平台查岗");
                        break;
                    case 1:
                        zw809Message.setTypeStr("JTT报警督办");
                        break;
                    case 2:
                        zw809Message.setTypeStr("西藏企业查岗");
                        break;
                    case 3:
                        zw809Message.setTypeStr("西藏企业督办");
                        break;
                    default:
                        break;
                }
            }
            Date time = zw809Message.getTime();
            if (time != null) {
                zw809Message.setTimeStr(simpleDateFormat.format(time));
            }
            Integer result = zw809Message.getResult();
            if (result != null) {
                switch (result) {
                    case 0:
                        zw809Message.setResultStr("未处理");
                        break;
                    case 1:
                        zw809Message.setResultStr("已处理");
                        break;
                    case 2:
                        zw809Message.setResultStr("已过期");
                        break;
                    default:
                        break;
                }

            }
            Date ackTime = zw809Message.getAckTime();
            if (ackTime != null) {
                zw809Message.setAckTimeStr(simpleDateFormat.format(ackTime));
            }
            String infoContent = zw809Message.getInfoContent();
            String warnType = zw809Message.getWarnType();
            String alarmType = "";
            if (warnType != null) {
                alarmType = getAlarmType(Integer.valueOf(warnType));
            }
            zw809Message.setInfoContent(StringUtils.isNotBlank(infoContent) ? infoContent : alarmType);
        }
        ExportExcelUtil.export(
            new ExportExcelParam(title, 1, zw809MessageList, Zw809MessageDTO.class, null, res.getOutputStream()));
    }

    @Override
    public List<String> getExtraInspectionReceivers(OrganizationLdap org) {
        final List<UserDTO> orgUsers = userService.getUserByOrgDn(org.getId().toString(), SearchScope.ONELEVEL);
        final Set<String> usernames = orgInspectionExtraUserDAO.listUsernameByOrgId(org.getUuid());
        return orgUsers.stream()
                .map(UserDTO::getUsername)
                .filter(usernames::contains)
                .collect(Collectors.toList());
    }

    @Override
    public void setExtraInspectionReceivers(OrganizationLdap org, Collection<String> usernames, OpType opType) {
        final String orgId = org.getUuid();
        final String parentDn = org.getPid();
        switch (opType) {
            case INSERT:
                batchCopySuperiorReceivers(Collections.singletonList(orgId), parentDn);
                addAsChild(usernames, orgId, parentDn);
                break;
            case ADD:
                addAsChild(usernames, orgId, parentDn);
                break;
            case UPDATE:
                // 计算增量
                calcAndChange(usernames, orgId);
                break;
            case DELETE:
                // 当前企业无用户，且其他企业相对位置不变，所以仅按orgId删除即可
                orgInspectionExtraUserDAO.deleteByOrgId(orgId);
                break;
            default:
        }
    }

    private void addAsChild(Collection<String> usernames, String orgId, String parentDn) {
        batchCopySubordinateReceivers(Collections.singletonList(orgId), parentDn);
        if (CollectionUtils.isEmpty(usernames)) {
            return;
        }
        final String me = SystemHelper.getCurrentUsername();
        final List<OrgInspectionExtraUserDO> entitiesToAdd = usernames.stream()
                .map(username -> new OrgInspectionExtraUserDO(username, orgId, me))
                .collect(Collectors.toList());
        orgInspectionExtraUserDAO.saveAll(entitiesToAdd);
    }

    private void calcAndChange(Collection<String> usernames, String orgId) {
        final Set<String> present = orgInspectionExtraUserDAO.listUsernameByOrgId(orgId);
        final Set<String> toDelete = new HashSet<>(present);
        toDelete.removeAll(usernames);
        if (CollectionUtils.isNotEmpty(toDelete)) {
            orgInspectionExtraUserDAO.deleteByUsernameIn(toDelete);
        }
        final Set<String> toAdd = new HashSet<>(usernames);
        toAdd.removeAll(present);
        if (CollectionUtils.isNotEmpty(toAdd)) {
            final String me = SystemHelper.getCurrentUsername();
            final List<String> orgIds = organizationService.getChildOrgIdByUuid(orgId);
            List<OrgInspectionExtraUserDO> entitiesToAdd = new ArrayList<>(orgIds.size() * toAdd.size());
            for (String orgIdToAdd : orgIds) {
                for (String username : toAdd) {
                    entitiesToAdd.add(new OrgInspectionExtraUserDO(username, orgIdToAdd, me));
                }
            }
            orgInspectionExtraUserDAO.saveAll(entitiesToAdd);
        }
    }

    @Override
    public void batchCopySuperiorReceivers(List<String> newOrgIds, String sourceOrgDn) {
        final String sourceOrgId = organizationService.getOrgByEntryDn(sourceOrgDn).getUuid();
        final Set<String> usernames = orgInspectionExtraUserDAO.listUsernameByOrgId(sourceOrgId);
        if (usernames.isEmpty()) {
            return;
        }
        final String me = SystemHelper.getCurrentUsername();
        List<OrgInspectionExtraUserDO> copied = new ArrayList<>(newOrgIds.size() * usernames.size());
        for (String newOrgId : newOrgIds) {
            for (String username : usernames) {
                copied.add(new OrgInspectionExtraUserDO(username, newOrgId, me));
            }
        }
        if (CollectionUtils.isNotEmpty(copied)) {
            orgInspectionExtraUserDAO.saveAll(copied);
        }
    }

    @Override
    public void batchCopySubordinateReceivers(List<String> newOrgIds, String sourceOrgDn) {
        final List<UserDTO> orgUsers = userService.getUserByOrgDn(sourceOrgDn, SearchScope.ONELEVEL);
        final List<String> usernames = orgUsers.stream().map(UserDTO::getUsername).collect(Collectors.toList());
        if (usernames.isEmpty()) {
            return;
        }
        final List<OrgInspectionExtraUserDO> sourceList = orgInspectionExtraUserDAO.listByUsernameIn(usernames);
        final String me = SystemHelper.getCurrentUsername();
        List<OrgInspectionExtraUserDO> copied = new ArrayList<>(newOrgIds.size() * sourceList.size());
        for (String newOrgId : newOrgIds) {
            for (OrgInspectionExtraUserDO source : sourceList) {
                copied.add(new OrgInspectionExtraUserDO(source.getUsername(), newOrgId, me));
            }
        }
        if (CollectionUtils.isNotEmpty(copied)) {
            orgInspectionExtraUserDAO.saveAll(copied);
        }
    }

    @Override
    public void deleteByUsername(Collection<String> usernames) {
        orgInspectionExtraUserDAO.deleteByUsernameIn(usernames);
    }

    private String getAlarmType(Integer type) {
        String alarmType;
        switch (type) {
            case 1:
                alarmType = "超速报警";
                break;
            case 2:
                alarmType = "疲劳驾驶报警";
                break;
            case 3:
                alarmType = "紧急报警";
                break;
            case 4:
                alarmType = "进入指定区域报警";
                break;
            case 5:
                alarmType = "离开指定区域报警";
                break;
            case 6:
                alarmType = "路段堵塞报警";
                break;
            case 7:
                alarmType = "危险路段报警";
                break;
            case 8:
                alarmType = "越界报警";
                break;
            case 9:
                alarmType = "盗警";
                break;
            case 10:
                alarmType = "劫警";
                break;
            case 11:
                alarmType = "偏离路线报警";
                break;
            case 12:
                alarmType = "车辆移动报警";
                break;
            case 13:
                alarmType = "超时驾驶报警";
                break;
            default:
                alarmType = "其他报警";
        }
        return alarmType;
    }
}
