package com.zw.adas.service.riskEvidence.imp;

import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.query.PicProcessPageQuery;
import com.zw.adas.domain.riskManagement.show.AdasPicPostprocessResult;
import com.zw.adas.repository.mysql.riskEvidence.AdasPicPostprocessDao;
import com.zw.adas.service.riskEvidence.AdasPicPostprocessService;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdasPicPostprocessServiceImpl implements AdasPicPostprocessService {

    @Autowired
    private AdasPicPostprocessDao adasPicPostprocessDao;

    /**
     * 保证双写一致性，因为幂等，所以只需保证增和删不同时执行即可
     */
    private static final ReadWriteLock ADD_LOCK = new ReentrantReadWriteLock(true);
    private static final ReadWriteLock REMOVE_LOCK = new ReentrantReadWriteLock(true);

    @Override
    public int batchRemove(Set<String> monitorIds) {
        ADD_LOCK.writeLock().lock();
        try {
            REMOVE_LOCK.readLock().lock();
            try {
                int mysqlUpdated = adasPicPostprocessDao.unmarkPicPostprocess(monitorIds);
                int redisUpdated = (int) RedisHelper.delSetItem(RedisKeyEnum.ADAS_PIC_POSTPROCESS_ID.of(), monitorIds);
                this.sync(mysqlUpdated, redisUpdated);
                return redisUpdated;
            } finally {
                REMOVE_LOCK.readLock().unlock();
            }
        } finally {
            ADD_LOCK.writeLock().unlock();
        }
    }

    @Override
    public int add(Set<String> monitorIds) {
        REMOVE_LOCK.writeLock().lock();
        try {
            ADD_LOCK.readLock().lock();
            try {
                int mysqlUpdated = adasPicPostprocessDao.markPicPostprocess(monitorIds);
                int redisUpdated = (int) RedisHelper.addToSet(RedisKeyEnum.ADAS_PIC_POSTPROCESS_ID.of(), monitorIds);
                this.sync(mysqlUpdated, redisUpdated);
                return redisUpdated;
            } finally {
                ADD_LOCK.readLock().unlock();
            }
        } finally {
            REMOVE_LOCK.writeLock().unlock();
        }
    }

    private void sync(int mysqlUpdated, int redisUpdated) {
        if (mysqlUpdated != redisUpdated) {
            log.error("车辆处理列表，数据不一致[{}!={}]", mysqlUpdated, redisUpdated);
        }
    }

    @Override
    public Page<AdasPicPostprocessResult> page(PicProcessPageQuery query) {
        final Set<String> allSetMonitorIds = RedisHelper.getSet(RedisKeyEnum.ADAS_PIC_POSTPROCESS_ID.of());
        final Set<String> groupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(SystemHelper.getCurrentUsername()));
        final Set<String> userMonitorIds = RedisHelper.batchGetSet(RedisKeyEnum.GROUP_MONITOR.ofs(groupIds));

        allSetMonitorIds.retainAll(userMonitorIds);
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            allSetMonitorIds.retainAll(MonitorUtils.fuzzySearchBindMonitorIds(query.getSimpleQueryParam()));
        }

        final List<String> sortedMonitorIds = VehicleUtil.sortVehicles(allSetMonitorIds);
        final List<String> pageMonitorIds = RedisQueryUtil.getPageListIds(sortedMonitorIds, query);

        final List<String> fields = Arrays.asList("id", "name", "orgName", "groupName", "vehiclePurposeName",
                "deviceNumber", "simCardNumber", "terminalType");
        final Map<String, VehicleDTO> infoMap = VehicleUtil.batchGetVehicleInfosFromRedis(pageMonitorIds, fields);
        return pageMonitorIds.stream()
                .map(id -> infoMap.computeIfAbsent(id, k -> new VehicleDTO()))
                .map(info -> {
                    final AdasPicPostprocessResult resp = new AdasPicPostprocessResult();
                    resp.setMonitorId(info.getId());
                    resp.setMonitorName(info.getName());
                    resp.setOrgName(info.getOrgName());
                    resp.setGroupName(info.getGroupName());
                    resp.setVehiclePurposeCategory(info.getVehiclePurposeName());
                    resp.setDeviceNumber(info.getDeviceNumber());
                    resp.setSimcardNumber(info.getSimCardNumber());
                    resp.setDeviceModelNumber(info.getTerminalType());
                    return resp;
                }).collect(Collectors.toCollection(() -> {
                    final Page<AdasPicPostprocessResult> page = new Page<>();
                    page.setTotal(allSetMonitorIds.size());
                    return page;
                }));
    }

    @EventListener
    public void onUnbind(ConfigUnBindEvent event) {
        final Set<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(monitorIds)) {
            this.batchRemove(monitorIds);
        }
    }
}
