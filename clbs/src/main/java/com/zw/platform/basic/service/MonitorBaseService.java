package com.zw.platform.basic.service;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 监控对象公共管理接口
 * @author zhangjuan
 * @date 2020/9/25
 */
public interface MonitorBaseService<D extends BindDTO> {

    /**
     * 校验编号是否存在 车辆校验车牌号
     * @param id     监控对象ID 新增时为空
     * @param number 人员编号/物品标号/车牌号
     * @return true 已经存在 false 不存在
     */
    boolean isExistNumber(String id, String number);

    /**
     * 校验监控对象是否进行了绑定
     * @param number 人员编号/物品标号/车牌号
     * @return true 绑定 false 未绑定
     */
    boolean isBind(String number);

    /**
     * 添加监控对象
     * @param d 监控对象实体
     * @return 是否添加成功
     */
    boolean add(D d);

    /**
     * 修改监控对象
     * @param d monitorDTO
     * @return 是否更新成功
     */
    boolean update(D d);

    /**
     * 根据监控对象ID更新监控对象名称
     * 该场景在绑定的时候使用 --暂不用推送监控对象修改事件
     * @param id   id
     * @param name name
     * @return 是否更新成功
     */
    boolean update(String id, String name);

    /**
     * 获取用户权限下监控对象的ID 包含绑定和未绑定的
     * @param keyword 监控对象名称关键字，为空未所有
     * @param orgIds  若企业ID集合为空，默认为用户拥有权限下的所有组织下未绑定的监控对象
     * @return 监控对象ID集合
     */
    List<String> getUserOwnIds(String keyword, List<String> orgIds);

    /**
     * 用户权限下绑定的监控对象ID
     * @param keyword 监控对象名称关键字
     * @return 监控对象ID集合
     */
    List<String> getUserOwnBindIds(String keyword);

    /**
     * 根据ID获取监控对象
     * @param id id
     * @return 监控对象详情
     */
    D getById(String id);

    /**
     * 批量获取监控对象
     * @param ids 监控对象Id
     * @return 监控对象集合
     */
    List<D> getByIds(Collection<String> ids);

    /**
     * 根据监控对象名称
     * @param monitorName 控对象名称
     * @return 监控对象
     */
    D getByName(String monitorName);

    /**
     * 单个删除
     * @param id id
     * @return true 删除成功、删除失败
     * @throws BusinessException 异常
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 获取用户权限下未绑定对象列表
     * @return 未绑定对象列表
     */
    List<Map<String, Object>> getUbBindSelectList();

    /**
     * 模糊搜索用户权限下未绑定对象列表
     * @param keyword 关键字
     * @return 未绑定对象列表
     */
    List<Map<String, Object>> getUbBindSelectList(String keyword);

    /**
     * 批量新增或更新监控对象缓存
     * @param monitorList 监控对象列表
     * @param updateIds   属于修改的监控对象
     */
    void addOrUpdateRedis(List<D> monitorList, Set<String> updateIds);

    /**
     * 构建默认信息
     * @param bindDTO 绑定信息
     * @return 对应的监控对象
     */
    D getDefaultInfo(ConfigDTO bindDTO);

    /**
     * 获取下发到F3的监控对象基础信息封装
     * @param id id
     * @return 下发到F3的监控对象信息
     */
    MonitorInfo getF3Data(String id);

    /**
     * 根据名称获取监控对象
     * @param monitorNames 监控对象名称集合 当为空时返回全部的监控对象
     * @return 监控对象的基础信息
     */
    List<MonitorBaseDTO> getByNames(Collection<String> monitorNames);

    /**
     * 获取扫描生成的监控对象名称
     * @param afterName 监控对象名称后缀
     * @return 监控对象名称
     */
    List<String> getScanByName(String afterName);

    /**
     * 更新监控对象的个性化图标
     * @param ids      监控对象id集合
     * @param iconId   图标ID
     * @param iconName 图标名称
     * @return 是否成功
     */
    boolean updateIcon(Collection<String> ids, String iconId, String iconName);

    /**
     * 删除监控对象的个性化图标
     * @param ids 监控对象id集合
     * @return 是否成功
     */
    boolean deleteIcon(Collection<String> ids);

    /**
     * 初始化监控对象图标
     */
    void initIconCache();

    /**
     * 更新图标缓存
     * @param ids      监控对象id集合
     * @param iconName 图标名称
     */
    default void updateIconCache(Collection<String> ids, String iconName) {
        Map<String, String> monitorIconMap = new HashMap<>(CommonUtil.ofMapCapacity(ids.size()));
        for (String id : ids) {
            monitorIconMap.put(id, iconName);
        }
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_ICON.of(), monitorIconMap);
    }

    /**
     * id排序和过滤
     * @param ids          获取到权限下的监控对象id集合
     * @param sortRedisKey 排序redisKey的枚举类型
     * @return 过滤后的顺序ID
     */
    default List<String> sortList(Set<String> ids, RedisKeyEnum sortRedisKey) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<String> sortList = RedisHelper.getList(sortRedisKey.of());
        if (CollectionUtils.isEmpty(sortList)) {
            return new ArrayList<>();
        }
        List<String> filterIds = new ArrayList<>();
        for (String id : sortList) {
            if (ids.contains(id)) {
                filterIds.add(id);
            }
        }
        return filterIds;
    }

    /**
     * 根据关键字模糊搜索
     * @param keyword     关键字
     * @param userOwnIds  拥有权限的监控对象
     * @param monitorType 监控对象类型
     * @return 搜索出的结果
     */
    default Set<String> fuzzyKeyword(String keyword, Set<String> userOwnIds, MonitorTypeEnum monitorType) {
        if (StringUtils.isBlank(keyword) || CollectionUtils.isEmpty(userOwnIds)) {
            return userOwnIds;
        }
        RedisKey fuzzyRedisKey = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
        String pattern = FuzzySearchUtil.buildFuzzySearchAllMonitorKey(keyword, monitorType);
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(fuzzyRedisKey, pattern);
        if (fuzzyList == null || fuzzyList.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> ids = new HashSet<>();
        for (Map.Entry<String, String> entry : fuzzyList) {
            String monitorId = entry.getValue().split("&")[1];
            if (userOwnIds.contains(monitorId)) {
                ids.add(monitorId);
            }
        }
        return ids;
    }

    /**
     * 构建模糊搜索的filed字段
     * @param monitorDTO 监控对象信息
     * @return filed
     */
    default String buildFuzzyField(D monitorDTO) {
        String filed;
        String type = FuzzySearchUtil.getMonitorType(monitorDTO.getMonitorType());
        if (Objects.equals(monitorDTO.getBindType(), Vehicle.BindType.UNBIND)) {
            filed = type + monitorDTO.getName();
        } else {
            filed = type + monitorDTO.getName() + "&" + FuzzySearchUtil.DEVICE_TYPE + monitorDTO.getDeviceNumber() + "&"
                + FuzzySearchUtil.SIM_TYPE + monitorDTO.getSimCardNumber();
        }
        return filed;
    }

    /**
     * 构建模糊搜索的value值
     * @param monitorDTO monitorDTO
     * @return 模糊搜索的value值
     */
    default String buildFuzzyValue(D monitorDTO) {
        String value;
        if (Objects.equals(monitorDTO.getBindType(), Vehicle.BindType.UNBIND)) {
            value = "vehicle&" + monitorDTO.getId();
        } else {
            value = "vehicle&" + monitorDTO.getId() + "&device&" + monitorDTO.getDeviceId() + "&simcard&" + monitorDTO
                .getSimCardId();
        }
        return value;
    }

    /**
     * 过滤分组和封装分组名  -- 后面考虑放到分组模块中去
     * @param groupIdStr 分组ID
     * @param groupMap   用户权限下的监控对象
     * @return 过滤后的分组名和分组名称
     */
    default Map<String, String> filterGroup(String groupIdStr, Map<String, String> groupMap) {
        if (StringUtils.isBlank(groupIdStr)) {
            return new HashMap<>(16);
        }

        String[] groupIds = groupIdStr.split(",");
        List<String> groupNames = new ArrayList<>();
        List<String> groupList = new ArrayList<>();
        for (String groupId : groupIds) {
            if (groupMap.containsKey(Converter.toBlank(groupId))) {
                groupList.add(groupId);
                groupNames.add(groupMap.get(groupId));
            }
        }
        Map<String, String> result = new HashMap<>(16);
        result.put("groupId", StringUtils.join(groupList, ","));
        result.put("groupName", StringUtils.join(groupNames, ","));
        return result;
    }

    /**
     * 获取数组第一个值
     * @param selectValues selectValues
     * @param defaultValue defaultValue
     * @return 值
     */
    default String getArrFirstValue(String[] selectValues, String defaultValue) {
        return selectValues == null || selectValues.length == 0 ? defaultValue : selectValues[0];
    }

    /**
     * 进行标记，用于区分人车物
     * @return
     */
    MonitorTypeEnum getMonitorEnum();
}
