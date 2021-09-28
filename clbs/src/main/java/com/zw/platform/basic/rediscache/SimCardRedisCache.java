package com.zw.platform.basic.rediscache;

import com.google.common.collect.Maps;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.domain.SimCardInfoDo;
import com.zw.platform.basic.domain.SimCardListDO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.util.FuzzySearchUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: zjc
 * @Description:sim卡redis缓存操作类
 * @Date: create in 2020/11/4 14:30
 */
public class SimCardRedisCache {
    private static final RedisKey FUZZY_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
    private static final RedisKey SIM_CARD_SORT = RedisKeyEnum.SIM_CARD_SORT_LIST.of();
    private static final String ALL = "*";

    public static void addSimCardCache(SimCardDTO simCardDTO) {
        String orgId = simCardDTO.getOrgId();
        String id = simCardDTO.getId();
        // 维护sim卡顺序
        RedisHelper.addToListTop(SIM_CARD_SORT, id);
        //维护企业sim卡缓存
        RedisHelper.addToSet(RedisKeyEnum.ORG_SIM_CARD.of(orgId), id);
        //维护企业下未绑定的sim卡缓存
        RedisHelper.addToSet(RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(orgId), id);
        //维护模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, FuzzySearchUtil.buildSimCard(simCardDTO.getSimcardNumber(), id));
    }

    public static void updateSimCardCache(SimCardDTO simCardDTO, String bindMonitorId, SimCardInfoDo beforeSimCard) {
        // sim卡所属组织id
        String beforeOrgId = beforeSimCard.getOrgId();
        String nowOrgId = simCardDTO.getOrgId();
        String id = simCardDTO.getId();
        //企业变更
        if (!beforeOrgId.equals(nowOrgId)) {
            //在新企业下新增
            RedisHelper.addToSet(RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(nowOrgId), id);
            RedisHelper.addToSet(RedisKeyEnum.ORG_SIM_CARD.of(nowOrgId), id);
            //在老企业下删除
            RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(beforeOrgId), id);
            RedisHelper.delSetItem(RedisKeyEnum.ORG_SIM_CARD.of(beforeOrgId), id);
        }
        boolean isBind = Objects.nonNull(bindMonitorId);
        if (isBind) {
            //更新绑定信息缓存
            RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(bindMonitorId), getUpdateCacheInfo(simCardDTO));
        }

        //维护模糊搜索缓存，终端绑定后不允许修改sim卡号，所以不用考虑绑定的情况
        String simCardNum = simCardDTO.getSimcardNumber();
        String beforeSimCardNm = beforeSimCard.getSimCardNumber();
        if (!Objects.equals(simCardNum, beforeSimCardNm)) {
            String beforeFiled = FuzzySearchUtil.buildSimCardField(beforeSimCardNm);
            RedisHelper.hdel(FUZZY_KEY, beforeFiled);
            RedisHelper.addToHash(FUZZY_KEY, FuzzySearchUtil.buildSimCard(simCardNum, simCardDTO.getId()));
        }
    }

    private static Map<String, String> getUpdateCacheInfo(SimCardDTO simCardDTO) {
        Map<String, String> bindSimCardInfo = Maps.newHashMapWithExpectedSize(1);
        bindSimCardInfo.put("realSimCardNumber", simCardDTO.getRealId());
        bindSimCardInfo.put("simCardOrgId", simCardDTO.getOrgId());
        return bindSimCardInfo;
    }

    public static void deleteCache(String id, SimCardInfoDo simCardInfoDo) {
        //在老企业下删除
        RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(simCardInfoDo.getOrgId()), simCardInfoDo.getId());
        RedisHelper.delSetItem(RedisKeyEnum.ORG_SIM_CARD.of(simCardInfoDo.getOrgId()), simCardInfoDo.getId());
        // 维护sim卡顺序
        RedisHelper.delListItem(SIM_CARD_SORT, id);
        //删除模糊搜索的缓存
        RedisHelper.hdel(FUZZY_KEY, FuzzySearchUtil.buildSimCardField(simCardInfoDo.getSimCardNumber()));
    }

    public static void addImportCache(List<SimCardDO> list) {
        List<String> sortList = new ArrayList<>();
        Map<RedisKey, Collection<String>> orgSimCardSetMap = new HashMap<>();
        Map<RedisKey, Collection<String>> unbindSimCardOrgMapSet = new HashMap<>();
        Map<String, String> fuzzyMap = new HashMap<>();
        RedisKey orgIdSimCardKey;
        RedisKey unbindOrgSimCardKey;
        Collection<String> deviceSet;
        Collection<String> unbindSimCardSet;
        for (SimCardDO simCardDO : list) {
            orgIdSimCardKey = RedisKeyEnum.ORG_SIM_CARD.of(simCardDO.getOrgId());
            unbindOrgSimCardKey = RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(simCardDO.getOrgId());
            //初始化相关参数
            sortList.add(simCardDO.getId());
            deviceSet = Optional.ofNullable(orgSimCardSetMap.get(orgIdSimCardKey)).orElse(new HashSet<>());
            unbindSimCardSet =
                Optional.ofNullable(unbindSimCardOrgMapSet.get(unbindOrgSimCardKey)).orElse(new HashSet<>());
            deviceSet.add(simCardDO.getId());
            unbindSimCardSet.add(simCardDO.getId());
            //存储结果
            orgSimCardSetMap.put(orgIdSimCardKey, deviceSet);
            unbindSimCardOrgMapSet.put(unbindOrgSimCardKey, unbindSimCardSet);
            fuzzyMap.putAll(FuzzySearchUtil.buildSimCard(simCardDO.getSimcardNumber(), simCardDO.getId()));
        }
        // 维护sim卡顺序
        RedisHelper.addToListTop(SIM_CARD_SORT, sortList);
        //维护企业sim卡缓存
        RedisHelper.batchAddToSet(orgSimCardSetMap);
        //维护企业下未绑定的sim卡缓存
        RedisHelper.batchAddToSet(unbindSimCardOrgMapSet);
        //维护模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, fuzzyMap);
    }

    public static void deleteSimCardsCache(List<SimCardListDO> simCardList) {

        List<String> sortList = new ArrayList<>();
        Map<RedisKey, Collection<String>> orgSimCardSetMap = new HashMap<>();
        Map<RedisKey, Collection<String>> unbindSimCardOrgMapSet = new HashMap<>();
        Set<String> fuzzySet = new HashSet<>();
        RedisKey orgIdSimCardKey;
        RedisKey unbindOrgSimCardKey;
        Collection<String> simCardSet;
        Collection<String> unbindSimCardSet;
        for (SimCardListDO simCard : simCardList) {
            orgIdSimCardKey = RedisKeyEnum.ORG_SIM_CARD.of(simCard.getOrgId());
            unbindOrgSimCardKey = RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(simCard.getOrgId());
            //初始化相关参数
            sortList.add(simCard.getId());
            simCardSet = Optional.ofNullable(orgSimCardSetMap.get(orgIdSimCardKey)).orElse(new HashSet<>());
            unbindSimCardSet =
                Optional.ofNullable(unbindSimCardOrgMapSet.get(unbindOrgSimCardKey)).orElse(new HashSet<>());
            simCardSet.add(simCard.getId());
            unbindSimCardSet.add(simCard.getId());
            //存储结果
            orgSimCardSetMap.put(orgIdSimCardKey, simCardSet);
            unbindSimCardOrgMapSet.put(unbindOrgSimCardKey, unbindSimCardSet);
            fuzzySet.add(FuzzySearchUtil.buildSimCardField(simCard.getSimcardNumber()));
        }
        // 维护sim卡顺序
        RedisHelper.delListItem(SIM_CARD_SORT, sortList);
        //维护企业sim卡缓存
        RedisHelper.batchDelSet(orgSimCardSetMap);
        //维护企业下未绑定的sim卡缓存
        RedisHelper.batchDelSet(unbindSimCardOrgMapSet);
        //维护模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, fuzzySet);

    }

    /**
     * 清除仅跟SIM卡相关的缓存
     */
    public static void clearCache() {
        //清除sim卡的顺序缓存
        RedisHelper.delete(SIM_CARD_SORT);
        //清除企业与sim卡的关系缓存
        RedisHelper.delByPattern(RedisKeyEnum.ORG_SIM_CARD.of(ALL));
        //清除企业下未绑定sim卡
        RedisHelper.delByPattern(RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(ALL));
        //模糊搜索缓存清除 -- 在信息配置列表中缓存初始缓存中进行删除
    }

    public static void initCache(List<String> sortIdList, List<SimCardDTO> simCardList) {
        //维护顺序缓存
        RedisHelper.addToListTop(SIM_CARD_SORT, sortIdList);

        //企业下与SIM卡的缓存 企业ID-sim卡ID集合
        Map<RedisKey, Collection<String>> orgSimMap = new HashMap<>(16);
        //企业与未绑定SIM卡的缓存 企业ID-未绑定sim卡ID集合
        Map<RedisKey, Collection<String>> orgUnbindSimMap = new HashMap<>(16);
        //企业下未绑定sim卡的模糊搜索缓存维护
        Map<String, String> fuzzyMap = new HashMap<>(simCardList.size());
        for (SimCardDTO simCard : simCardList) {
            String orgId = simCard.getOrgId();
            String simId = simCard.getId();

            //企业与SIM卡的关系
            Collection<String> simSet = orgSimMap.getOrDefault(RedisKeyEnum.ORG_SIM_CARD.of(orgId), new HashSet<>());
            simSet.add(simId);
            orgSimMap.put(RedisKeyEnum.ORG_SIM_CARD.of(orgId), simSet);

            if (StringUtils.isNotBlank(simCard.getConfigId())) {
                continue;
            }
            //企业与未绑定sim卡
            Collection<String> unbindSet =
                orgUnbindSimMap.getOrDefault(RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(orgId), new HashSet<>());
            unbindSet.add(simId);
            orgUnbindSimMap.put(RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(orgId), unbindSet);

            //未绑定sim卡模糊搜索
            fuzzyMap.putAll(FuzzySearchUtil.buildSimCard(simCard.getSimcardNumber(), simCard.getId()));
        }
        //维护企业与SIM卡的缓存
        RedisHelper.batchAddToSet(orgSimMap);
        //维护企业与未绑定SIM卡的缓存
        RedisHelper.batchAddToSet(orgUnbindSimMap);
        //维护未绑定sim卡缓存的模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, fuzzyMap);
    }

    /**
     * 获取企业下未绑定的终端ID
     * @param orgIds     企业
     * @param keyword    关键字 可为空
     * @param isLimitNum 是否限制数量 true 返回100条内
     * @return 符合条件的有序终端ID
     */
    public static List<String> getUnbind(List<String> orgIds, String keyword, boolean isLimitNum) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }

        List<RedisKey> orgUnbindKey =
            orgIds.stream().map(RedisKeyEnum.ORG_UNBIND_SIM_CARD::of).collect(Collectors.toList());
        Set<String> ownIds = RedisHelper.batchGetSet(orgUnbindKey);

        List<String> sortIds = RedisHelper.getList(SIM_CARD_SORT);
        if (ownIds.isEmpty() || CollectionUtils.isEmpty(sortIds)) {
            return new ArrayList<>();
        }

        if (StringUtils.isNotBlank(keyword)) {
            Set<String> fuzzyIds = FuzzySearchUtil.scanUnbind(keyword, FuzzySearchUtil.SIM_TYPE);
            ownIds.retainAll(fuzzyIds);
        }

        //进行过滤和排序
        List<String> result = new ArrayList<>();
        for (String id : sortIds) {
            //限制下拉框返回数量
            if (isLimitNum && Objects.equals(result.size(), Vehicle.UNBIND_SELECT_SHOW_NUMBER)) {
                break;
            }
            if (ownIds.contains(id)) {
                result.add(id);
            }
        }

        return result;
    }
}
