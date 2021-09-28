package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.reportManagement.SuperPlatformMsg;
import com.zw.platform.domain.reportManagement.Zw809MessageDO;
import com.zw.platform.dto.platformInspection.Zw809MessageDTO;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.modules.Zw809MessageDao;
import com.zw.platform.service.reportManagement.SuperPlatformMsgService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SuperPlatformMsgServiceImpl implements SuperPlatformMsgService {

    /**
     * 标准809查岗
     */
    private static final int STANDARD_809_INSPECTION = 0;

    /**
     * 标准809督办
     */
    private static final int STANDARD_809_SUPERVISION = 1;

    /**
     * 西藏809查岗
     */
    private static final int TIBET_809_INSPECTION = 2;

    /**
     * 西藏809督办
     */
    private static final int TIBET_809_SUPERVISION = 3;

    /**
     * 监管平台巡检监控人员
     */
    private static final int INSPECT_USER_809_SUPERVISION = 4;

    @Autowired
    private Zw809MessageDao zw809MessageDao;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    private UserService userService;

    @Autowired
    private DataSourceTransactionManager txManager;

    @Value("${supervision-message.keep-months:6}")
    private int messageKeepMonth;

    @Override
    public void saveSuperPlatformMsg(SuperPlatformMsg superPlatformMsg) {
        if (superPlatformMsg != null) {
            final Zw809MessageDO zw809MessageDO = convertToDO(superPlatformMsg);
            zw809MessageDao.insert(zw809MessageDO);
        }
    }

    @Override
    public void batchSaveSuperPlatformMsg(List<SuperPlatformMsg> superPlatformMsgList) {
        if (CollectionUtils.isEmpty(superPlatformMsgList)) {
            return;
        }
        List<Zw809MessageDO> list = Lists.newArrayList();
        for (SuperPlatformMsg superPlatformMsg : superPlatformMsgList) {
            list.add(convertToDO(superPlatformMsg));
        }
        zw809MessageDao.batchInsert(list);
    }

    /**
     * 拷贝属性
     * @param superPlatformMsg superPlatformMsg
     * @return zw809MessageDO
     */
    private static Zw809MessageDO convertToDO(SuperPlatformMsg superPlatformMsg) {
        Zw809MessageDO zw809MessageDO = new Zw809MessageDO();
        BeanUtils.copyProperties(superPlatformMsg, zw809MessageDO);

        final JSONObject msg = JSONObject.parseObject(superPlatformMsg.getMsg());
        final JSONObject message808 = msg.getJSONObject("data");
        final JSONObject msgHead = message808.getJSONObject("msgHead");
        final JSONObject msgBody = message808.getJSONObject("msgBody");
        final JSONObject data = msgBody.getJSONObject("data");
        switch (superPlatformMsg.getType()) {
            case STANDARD_809_INSPECTION:
                zw809MessageDO.setInfoContent(data.getString("infoContent"));
                break;
            case STANDARD_809_SUPERVISION:
                zw809MessageDO.setBrand(msgBody.getString("vehicleNo"));
                zw809MessageDO.setPlateColor(msgBody.getInteger("vehicleColor"));
                zw809MessageDO.setWarnType(data.getString("warnType"));
                zw809MessageDO.setWarnTime(data.getLong("warnTime"));
                zw809MessageDO.setAlarmType(data.getString("alarmType"));
                zw809MessageDO.setWarnSrc(data.getInteger("warnSrc"));
                zw809MessageDO.setSupervisionLevel(data.getInteger("supervisionLevel"));
                zw809MessageDO.setSupervisor(data.getString("supervisor"));
                zw809MessageDO.setSupervisionTel(data.getString("supervisionTel"));
                zw809MessageDO.setSupervisionEmail(data.getString("supervisionEmal"));
                break;
            case TIBET_809_INSPECTION:
                zw809MessageDO.setInfoContent(data.getString("infoContent"));
                zw809MessageDO.setEnterprise(data.getString("enterprise"));
                zw809MessageDO.setSupervisor(data.getString("gangman"));
                break;
            case TIBET_809_SUPERVISION:
                zw809MessageDO.setInfoContent(data.getString("infoContent"));
                zw809MessageDO.setEnterprise(data.getString("enterprise"));
                zw809MessageDO.setBrand(msgBody.getString("vehicleNo"));
                zw809MessageDO.setPlateColor(msgBody.getInteger("vehicleColor"));
                zw809MessageDO.setWarnType(data.getString("warnType"));
                zw809MessageDO.setWarnTime(data.getLong("warnTime"));
                zw809MessageDO.setAlarmType(data.getString("alarmType"));
                zw809MessageDO.setWarnSrc(data.getInteger("warnSrc"));
                zw809MessageDO.setSupervisionLevel(data.getInteger("level"));
                zw809MessageDO.setSupervisor(data.getString("gangman"));
                zw809MessageDO.setSupervisionTel(data.getString("tel"));
                zw809MessageDO.setSupervisionEmail(data.getString("email"));
                zw809MessageDO.setExpireTime(new Date(data.getLong("deadTime") * 1000L));
                break;
            default:
        }

        zw809MessageDO.setAlarmStartTime(data.getLong("alarmStartTime"));
        zw809MessageDO.setSourceMsgSn(msgHead.getInteger("msgSn"));
        zw809MessageDO.setSourceDataType(msgBody.getInteger("dataType"));
        zw809MessageDO.setEventId(data.getString("eventId"));
        zw809MessageDO.setInfoId(data.getInteger("infoId"));
        zw809MessageDO.setMonitorId(data.getString("monitorId"));
        zw809MessageDO.setObjectId(data.getString("objectId"));
        zw809MessageDO.setObjectType(data.getInteger("objectType"));
        zw809MessageDO.setSupervisionId(data.getInteger("supervisionId"));
        zw809MessageDO.setDataType(msgBody.getInteger("dataType"));
        zw809MessageDO.setHandleId(msgHead.getString("handleId"));
        zw809MessageDO.setMsgGnssCenterId(msgHead.getInteger("msgGNSSCenterId"));
        zw809MessageDO.setMsgId(msgHead.getInteger("msgID"));
        zw809MessageDO.setMsgSn(data.getInteger("msgSn"));
        zw809MessageDO.setProtocolType(msgHead.getInteger("protocolType"));
        zw809MessageDO.setServerIp(msgHead.getString("serverIp"));
        zw809MessageDO.setAnswerTime(superPlatformMsg.getAnswerTime());

        return zw809MessageDO;
    }

    @Override
    public void updateSuperPlatformMsg(String id, Integer result, String ackContent) {
        Zw809MessageDO message = new Zw809MessageDO();
        message.setId(id);
        message.setAckTime(new Date());
        message.setResult(result);
        message.setAckContent(ackContent);
        String user = SystemHelper.getCurrentUsername();
        message.setDealer(user == null ? "admin" : user);
        zw809MessageDao.updateMsgStatus(message);
    }

    @Override
    public Integer getMsgStatus(String id) {
        return zw809MessageDao.getMsgStatus(id);
    }

    @Override
    public void updatePastData() {
        final String now = DateUtil.YMD_HMS.format(LocalDateTime.now())
                .orElseThrow(RuntimeException::new);
        List<String> msgIds = zw809MessageDao.getAllUntreatedMsgIds(now);
        final int batchSize = 500;
        if (msgIds.size() > batchSize) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            Lists.partition(msgIds, batchSize).forEach(part -> {
                final TransactionStatus transaction = txManager.getTransaction(def);
                try {
                    zw809MessageDao.updatePastData(part);
                    txManager.commit(transaction);
                } catch (Exception e) {
                    txManager.rollback(transaction);
                    throw e;
                }
                log.info("已删除{}条过期未处理的809数据", part.size());
            });
        } else if (!msgIds.isEmpty()) {
            zw809MessageDao.updatePastData(msgIds);
            log.info("已删除{}条过期未处理的809数据", msgIds.size());
        }
    }

    /**
     * 查询当天所有未处理的上级平台消息
     */
    @Override
    public JSONObject getTheDayPlatformMsg() {
        JSONObject msg = new JSONObject();
        List<Zw809MessageDO> messages = listMsgToday();
        int standard809GangNum = 0;
        int standard809AlarmNum = 0;
        int extend809GangNum = 0;
        int extend809AlarmNum = 0;
        int inspectUserNum = 0;
        if (messages.size() > 0) {
            for (Zw809MessageDO message : messages) {
                int type = message.getType();
                switch (type) {
                    case STANDARD_809_INSPECTION:
                        standard809GangNum++;
                        break;
                    case STANDARD_809_SUPERVISION:
                        standard809AlarmNum++;
                        break;
                    case TIBET_809_INSPECTION:
                        extend809GangNum++;
                        break;
                    case TIBET_809_SUPERVISION:
                        extend809AlarmNum++;
                        break;
                    case INSPECT_USER_809_SUPERVISION:
                        inspectUserNum++;
                        break;
                    default:
                        break;
                }
            }
        }
        msg.put("standard809GangNum", standard809GangNum);
        msg.put("standard809AlarmNum", standard809AlarmNum);
        msg.put("extend809GangNum", extend809GangNum);
        msg.put("extend809AlarmNum", extend809AlarmNum);
        msg.put("inspectUserNum", inspectUserNum);
        return msg;
    }

    /**
     * 获取用户所属企业当天未处理的上级平台消息
     */
    private List<Zw809MessageDO> listMsgToday() {
        final LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        String startTime = DateUtil.YMD_HMS.format(startOfToday)
                .orElseThrow(RuntimeException::new);
        String endTime = DateUtil.YMD_HMS.format(startOfToday.plusDays(1).minusSeconds(1))
                .orElseThrow(RuntimeException::new);
        List<Zw809MessageDO> msgData = new ArrayList<>();
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        if (currentUserOrg != null) {
            // 获取用户的所属企业
            msgData = zw809MessageDao.listByTime(startTime, endTime, currentUserOrg.getUuid());
        }
        return msgData;
    }

    @Override
    public List<Zw809MessageDTO> getTheDayAllMsgByUser(String type, String startTime, String endTime, int status) {
        List<Zw809MessageDTO> msgData = new ArrayList<>();
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
            if (currentUserOrg != null) {
                Integer msgType = null;
                switch (type) {
                    case "11":
                        msgType = STANDARD_809_INSPECTION;
                        break;
                    case "21":
                        msgType = STANDARD_809_SUPERVISION;
                        break;
                    case "12":
                        msgType = TIBET_809_INSPECTION;
                        break;
                    case "22":
                        msgType = TIBET_809_SUPERVISION;
                        break;
                    default:
                        break;
                }
                Integer msgStatus = null;
                switch (status) {
                    case 1: // 未处理
                        msgStatus = 0;
                        break;
                    case 2:  // 已处理
                        msgStatus = 1;
                        break;
                    case 3: // 已过期
                        msgStatus = 2;
                        break;
                    default:
                        break;
                }
                final String groupId = currentUserOrg.getUuid();
                final List<Zw809MessageDO> entities = zw809MessageDao.getTheDayAllMsgByUser(startTime,
                        endTime, groupId, msgType, msgStatus);
                final Set<String> platformIds =
                        entities.stream().map(Zw809MessageDO::getPlatformId).collect(Collectors.toSet());
                final Map<String, String> platformIdNameMap = AssembleUtil.convertToMap(
                        platformIds, connectionParamsSetDao::listPlatformNameByIdIn,
                        PlantParam::getId, PlantParam::getPlatformName);
                entities.forEach(e -> {
                    final Zw809MessageDTO dto = new Zw809MessageDTO();
                    BeanUtils.copyProperties(e, dto);
                    dto.setPlatformName(platformIdNameMap.getOrDefault(e.getPlatformId(), ""));
                    msgData.add(dto);
                });
            }
        }
        return msgData;
    }

    @Override
    public Zw809MessageDO get809Message(String id) {
        return zw809MessageDao.getMsgById(id);
    }

    @Override
    public String migrate809Message() {
        final long begin = System.currentTimeMillis();
        final String startTime = DateUtil.YMD_HMS.format(YearMonth.now()
                // 当前月白送
                .minusMonths(messageKeepMonth)
                .atDay(1)
                .atStartOfDay()
        ).orElseThrow(RuntimeException::new);
        final int batchSize = 1000;
        int offset = 0;
        int added = 0;
        for (int currentPageSize = batchSize; currentPageSize == batchSize; ) {
            List<SuperPlatformMsg> oldMessages = zw809MessageDao.listByTimeLaterThan(startTime, offset, batchSize);
            currentPageSize = oldMessages.size();
            log.info("迁移809查岗督办消息-正在迁移第{}条至第{}条", offset + 1, offset + currentPageSize);
            offset += currentPageSize;
            if (CollectionUtils.isNotEmpty(oldMessages)) {
                final List<Zw809MessageDO> newMessages =
                        oldMessages.stream().map(SuperPlatformMsgServiceImpl::convertToDO).collect(Collectors.toList());
                added += zw809MessageDao.batchInsert(newMessages);
                log.info("迁移809查岗督办消息-写入{}条", added);
            }
        }
        final long end = System.currentTimeMillis();
        final String result = String.format("迁移结果：查询出本月及最近%d个月的数据共%d条，写入%d条，耗时%.2fs",
                messageKeepMonth, offset, added, (end - begin) / 1000f);
        log.info(result);
        return result;
    }

    @Override
    public void deleteOldMessages() {
        final long begin = System.currentTimeMillis();
        // 早于此时间的数据全部删除
        final String earliestTime = DateUtil.YMD_HMS.format(YearMonth.now()
                // 当前月白送
                .minusMonths(messageKeepMonth)
                .atDay(1)
                .atStartOfDay()
        ).orElseThrow(RuntimeException::new);
        final int deleted = zw809MessageDao.deleteByTimeEarlierThan(earliestTime);
        final long end = System.currentTimeMillis();
        log.info("移除过久的809查岗/督办消息-已删除{}条数据，耗时{}ms", deleted, end - begin);
    }

}
