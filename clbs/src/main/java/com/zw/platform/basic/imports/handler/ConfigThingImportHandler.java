package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.domain.ThingDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 信息配置物品导入
 * @author zhangjuan
 */
@Slf4j
public class ConfigThingImportHandler extends BaseImportHandler {
    private final ConfigImportHolder importHolder;
    private final ThingService thingService;
    /**
     * excel中是否存在需要新增的车辆
     */
    private int thingCount;

    private final List<ThingDTO> thingList;
    private final List<String> delThingIds;

    public ConfigThingImportHandler(ConfigImportHolder importHolder, ThingService thingService) {
        this.importHolder = importHolder;
        this.thingService = thingService;
        this.thingCount = 0;
        this.thingList = new ArrayList<>();
        this.delThingIds = new ArrayList<>();
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_THING_INFO };
    }

    @Override
    public boolean uniqueValid() {
        if (importHolder.getImportThingNum() == 0) {
            progressBar.setTotalProgress(1);
            return true;
        }
        Map<String, String> orgMap = importHolder.getOrgMap();
        Map<String, MonitorBaseDTO> thingMap =
            AssembleUtil.collectionToMap(importHolder.getExistThingList(), MonitorBaseDTO::getName);
        int errorCount = 0;
        for (BindDTO bindDTO : importHolder.getImportList()) {
            if (!Objects.equals(MonitorTypeEnum.THING.getType(), bindDTO.getMonitorType())) {
                continue;
            }
            bindDTO.setOrgId(orgMap.get(bindDTO.getOrgName()));
            MonitorBaseDTO baseDTO = thingMap.get(bindDTO.getName());
            if (Objects.isNull(baseDTO)) {
                thingCount++;
                continue;
            }

            //校验监控对象是否已经绑定  对讲信息导入时即当对讲绑定字段不为空时，可以是绑定状态
            if (Objects.isNull(bindDTO.getIntercomBindType()) && Objects
                .equals(Vehicle.BindType.HAS_BIND, baseDTO.getBindType())) {
                bindDTO.setErrorMsg("【物品: " + bindDTO.getName() + "】已绑定");
                errorCount++;
                continue;
            }

            //物品已经存在，未绑定，但用户没有权限操作
            if (StringUtils.isBlank(importHolder.getOrgIdNameMap().get(baseDTO.getOrgId()))) {
                errorCount++;
                bindDTO.setErrorMsg("物品已存在，不能重复导入");
                continue;
            }
            //校验db里的监控对象企业和导入的企业是否一致,若一致，赋值监控对象ID和监控对象的组织ID
            if (Objects.equals(bindDTO.getOrgId(), baseDTO.getOrgId())) {
                bindDTO.setId(baseDTO.getId());
                continue;
            }
            //若db里的监控对象企业和导入的企业不一致，则删除原来的物品，新增导入的物品
            delThingIds.add(baseDTO.getId());
            thingCount++;
        }
        // 一遍mysql，一遍redis，redis任务量视为减半
        progressBar.setTotalProgress(1 + thingCount * 3 / 2);
        importHolder.setExistThingList(null);

        return errorCount == 0;
    }

    @SneakyThrows
    private void deleteOldThing() {
        if (CollectionUtils.isNotEmpty(delThingIds)) {
            thingService.batchDel(delThingIds);
        }
    }

    @Override
    public boolean addMysql() {
        deleteOldThing();
        if (thingCount == 0) {
            return true;
        }
        //1. 封装物品信息对象
        final String username = SystemHelper.getCurrentUsername();
        final Date createDate = new Date();
        List<ThingDO> thingDOList = new ArrayList<>();
        for (BindDTO bindDTO : importHolder.getImportList()) {
            if (!Objects.equals(MonitorTypeEnum.THING.getType(), bindDTO.getMonitorType())) {
                continue;
            }

            if (StringUtils.isNotBlank(bindDTO.getId())) {
                continue;
            }
            ThingDTO thingDTO = build(bindDTO);
            ThingDO thingDO = new ThingDO(thingDTO);
            thingDO.setCreateDataUsername(username);
            thingDO.setCreateDataTime(createDate);
            bindDTO.setId(thingDO.getId());
            this.thingList.add(thingDTO);
            thingDOList.add(thingDO);
        }

        // 2.存储数据到mysql
        partition(thingDOList, thingService::addByBatch);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        thingService.addOrUpdateRedis(this.thingList, null);
    }

    private ThingDTO build(BindDTO bindDTO) {
        ThingDTO thing = new ThingDTO();
        thing.setName(bindDTO.getName());
        thing.setType("0");
        thing.setTypeName("其他物品");
        thing.setCategory("0");
        thing.setCategoryName("其他物品");
        thing.setOrgId(bindDTO.getOrgId());
        thing.setOrgName(bindDTO.getOrgName());
        thing.setMonitorType(MonitorTypeEnum.THING.getType());
        return thing;
    }
}
