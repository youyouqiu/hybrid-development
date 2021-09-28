package com.zw.platform.service.core.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.CustomColumnConfigInfo;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.repository.core.CustomColumnDao;
import com.zw.platform.service.core.CustomColumnService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 自定义列service impl
 * @author zhouzongbo on 2019/3/11 15:25
 */
@Service
public class CustomColumnServiceImpl implements CustomColumnService {

    private static final Logger logger = LogManager.getLogger(CustomColumnServiceImpl.class);
    @Autowired
    private CustomColumnDao customColumnDao;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchService logSearchService;
    //多窗口定制列
    private static final String MULTI_WINDOW_REALTIME_DATA_LIST = "MULTI_WINDOW_REALTIME_DATA_LIST";
    private static final String MULTI_WINDOW_REALTIME_MONITORING = "MULTI_WINDOW_REALTIME_MONITORING";

    @Override
    public JsonResultBean findCustomColumnInfoByMark(String marks) throws Exception {
        String[] markArr = marks.split(",");
        Map<String, Object> resultMap = new HashMap<>(16);
        List<CustomColumnConfigInfo> bindList;
        for (String mark : markArr) {
            // 查询用户自定义的列, 如果不存在, 查询所有列
            bindList = new ArrayList<>();
            bindList = getCustomColumnConfigInfos(bindList, mark);
            resultMap.put(mark, bindList);
        }

        return new JsonResultBean(resultMap);
    }

    @Override
    @SuppressWarnings("unchekced")
    public List<CustomColumnConfigInfo> getCustomColumnConfigInfos(List<CustomColumnConfigInfo> bindList, String mark) {
        RedisKey redisKey = HistoryRedisKeyEnum.getCustomColumnEnum(mark);
        if (Objects.isNull(redisKey)) {
            return new ArrayList<>();
        }
        String userId = userService.getCurrentUserUuid();
        RedisKey userRedisKey = HistoryRedisKeyEnum.getUserCustomColumnEnum(mark).of(userId);
        if (RedisHelper.isContainsKey(userRedisKey)) {
            return RedisHelper.getList(userRedisKey, CustomColumnConfigInfo.class);
        }
        if (CollectionUtils.isEmpty(bindList)) {
            bindList = findDefaultCustomConfigByMark(redisKey);
        }
        return bindList;
    }

    /**
     * 查询默认数据
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<CustomColumnConfigInfo> findDefaultCustomConfigByMark(RedisKey redisKey) {
        // 先从Redis中获取数据
        List<CustomColumnConfigInfo> list = RedisHelper.getList(redisKey, CustomColumnConfigInfo.class);
        if (CollectionUtils.isNotEmpty(list)) {
            return list;
        }
        // 如未获取到数据, 则从mysql中获取
        List<CustomColumnConfigInfo> customColumnConfigInfoList =
            customColumnDao.findDefaultCustomConfigByMark(redisKey.get(), CustomColumnConfigInfo.DEFAULT_COLUMN);
        if (CollectionUtils.isEmpty(customColumnConfigInfoList)) {
            return customColumnConfigInfoList;
        }
        // 如果Redis中存在默认数据的key, 先删除旧的垃圾数据, 再重新存入新的数据
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        // 存入Redis
        RedisHelper.addToList(redisKey, customColumnConfigInfoList);

        return customColumnConfigInfoList;
    }

    @Override
    public List<CustomColumnConfigInfo> findUserCustomColumnInfo(String columnModule) {
        String userUuid = userService.getCurrentUserUuid();
        return customColumnDao.findUserCustomColumnInfo(userUuid, columnModule);
    }

    @Override
    public List<CustomColumnConfigInfo> findCustomColumnConfigInfoByMark(String mark) {
        String userUuid = userService.getCurrentUserUuid();
        return customColumnDao.findCustomColumnConfigInfo(userUuid, mark);
    }

    @Override
    public JsonResultBean addCustomColumnConfig(String customColumnConfigJson, String title, String ipAddress)
        throws Exception {
        // 删除用户已经绑定的关系
        UserLdap user = SystemHelper.getCurrentUser();
        if (Objects.isNull(user)) {
            return new JsonResultBean(JsonResultBean.FAULT, "用户不存在!");
        }
        String userUuid = userService.getCurrentUserUuid();
        String username = user.getUsername();

        // 解析数据
        List<CustomColumnConfigInfo> customColumnConfigInfoList =
            JSON.parseObject(customColumnConfigJson, new TypeReference<List<CustomColumnConfigInfo>>() {
            });
        if (CollectionUtils.isNotEmpty(customColumnConfigInfoList)) {

            Map<String, List<CustomColumnConfigInfo>> addToRedisData = new HashMap<>(16);
            // 根据标识分组, 方便把结果存入Redis, 初始化时获取对应菜单的表头展示即可
            Boolean isSuccess = addCustomConfigToMysql(userUuid, username, customColumnConfigInfoList, addToRedisData);
            if (isSuccess) {
                // 维护绑定关系数据到Redis, 用于列表初始化
                addToRedisData.forEach((redisMark, customColumnConfigInfos) -> {
                    RedisKey redisKey = HistoryRedisKeyEnum.getUserCustomColumnEnum(redisMark).of(userUuid);
                    removeUserCuntomRedis(redisKey);
                    RedisHelper.addToList(redisKey, customColumnConfigInfos);
                });

                // 添加日志
                String message = String.format("用户：%s设置%s", username, title);
                logSearchService.addLog(ipAddress, message, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } else {
            if (MULTI_WINDOW_REALTIME_DATA_LIST.equals(title)) {
                //多窗口定制列可以全部取消
                List<CustomColumnConfigInfo> list = Lists.newArrayList();
                CustomColumnConfigInfo customColumnConfigInfo = new CustomColumnConfigInfo();
                customColumnConfigInfo.setMark("用户取消所有列");
                list.add(customColumnConfigInfo);
                RedisKey userRedisKey = HistoryRedisKeyEnum.USER_MULTI_WINDOW_REALTIME_DATA_LIST.of(userUuid);
                removeUserCuntomRedis(userRedisKey);
                RedisHelper.addToList(userRedisKey, list);
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 保存数据到mysql
     * @param userUUID                   userUUID
     * @param username                   username
     * @param customColumnConfigInfoList customColumnConfigInfoList
     * @param addToRedisData             addToRedisData
     * @return boolean true or false
     */
    private Boolean addCustomConfigToMysql(String userUUID, String username,
        List<CustomColumnConfigInfo> customColumnConfigInfoList,
        Map<String, List<CustomColumnConfigInfo>> addToRedisData) {
        // 根据标识分组, 方便把结果存入Redis, 初始化时获取对应菜单的表头展示即可
        Map<String, List<CustomColumnConfigInfo>> markCustomConfigList =
            customColumnConfigInfoList.stream().collect(Collectors.groupingBy(CustomColumnConfigInfo::getMark));
        // 存储到mysql数据集合
        List<CustomColumnConfigInfo> batchAddList = new ArrayList<>();

        for (Map.Entry<String, List<CustomColumnConfigInfo>> entryMark : markCustomConfigList.entrySet()) {
            String customMark = entryMark.getKey();
            // 增加数据
            List<CustomColumnConfigInfo> customColumnConfigInfos = entryMark.getValue();
            // 构建绑定数据
            customColumnConfigInfos.stream().map(configInfo -> {
                configInfo.setUserId(userUUID);
                configInfo.setCreateDataUsername(username);
                return configInfo;
            }).sorted(Comparator.comparing(CustomColumnConfigInfo::getSort)).collect(Collectors.toList());
            batchAddList.addAll(customColumnConfigInfos);
            addToRedisData.put(customMark, customColumnConfigInfos);
        }
        if (batchAddList.size() > 0) {
            customColumnDao.deleteAllCustomColumnConfigByMarks(userUUID, markCustomConfigList.keySet());
            return customColumnDao.addCustomColumnConfigList(batchAddList);
        }
        return false;
    }

    /**
     * 移除用户自定义数据
     */
    public void removeUserCuntomRedis(RedisKey redisKey) {
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
    }

    @Override
    public List<String> findCustomColumnTitleList(String mark) {
        List<CustomColumnConfigInfo> customColumnList = getCustomColumnConfigInfos(Lists.newArrayList(), mark);
        if (CollectionUtils.isEmpty(customColumnList)) {
            return null;
        }

        return customColumnList.stream().map(CustomColumnConfigInfo::getTitle).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> findCustomColumnModule(String columnModule) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<CustomColumnConfigInfo> customColumnConfigInfoList = findUserCustomColumnInfo(columnModule);
        if (CollectionUtils.isNotEmpty(customColumnConfigInfoList)) {
            // 根据标识进行分组
            Map<String, List<CustomColumnConfigInfo>> markList =
                customColumnConfigInfoList.stream().collect(Collectors.groupingBy(CustomColumnConfigInfo::getMark));

            for (Map.Entry<String, List<CustomColumnConfigInfo>> customMark : markList.entrySet()) {
                Map<String, Object> data = new HashMap<>(16);
                List<CustomColumnConfigInfo> customConfigInfoList = customMark.getValue();
                Integer fixSize =
                    customConfigInfoList.stream().filter(configInfo -> Objects.nonNull(configInfo.getIsFix()))
                        .mapToInt(CustomColumnConfigInfo::getIsFix).sum();

                CustomColumnConfigInfo customColumnConfigInfo = customConfigInfoList.get(0);

                // 用于判断用户是否自定义了列
                String userId = customColumnConfigInfo.getUserId();
                if (StringUtils.isNotEmpty(userId)) {
                    customConfigInfoList = customConfigInfoList.stream().peek(info -> {
                        if (StringUtils.isNotEmpty(info.getUserId())) {
                            // 前端需要勾选字段
                            info.setStatus(CustomColumnConfigInfo.DEFAULT_COLUMN);
                        } else {
                            // 不需要勾选字段
                            info.setStatus(CustomColumnConfigInfo.NOT_DEFAULT_COLUMN);
                        }
                    }).collect(Collectors.toList());
                }
                dealMultiWindowRealtimeMonitoring(columnModule, customConfigInfoList);
                data.put("fixSize", fixSize);
                data.put("dataList", customConfigInfoList);
                data.put("mark", customMark.getKey());
                resultList.add(data);
            }
        }
        return resultList;
    }

    @Override
    public void deleteUserMarkColumn(String columnId, String mark) {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.getUserCustomColumnEnum(mark).of(userId);
        customColumnDao.deleteUserCustomColumn(userId, columnId);
        List<String> list = RedisHelper.getList(redisKey);
        if (CollectionUtils.isNotEmpty(list)) {
            List<CustomColumnConfigInfo> customColumns =
                list.stream().map(o -> JSONObject.parseObject(o, CustomColumnConfigInfo.class))
                    .collect(Collectors.toList());
            for (int i = 0, len = customColumns.size(); i < len; i++) {
                if (customColumns.get(i).getColumnId().equals(columnId)) {
                    customColumns.remove(i);
                    break;
                }
            }
            list = customColumns.stream().map(JSONObject::toJSONString).collect(Collectors.toList());
        }
        removeUserCuntomRedis(redisKey);
        RedisHelper.addToList(redisKey, list);
    }

    private void dealMultiWindowRealtimeMonitoring(String columnModule,
        List<CustomColumnConfigInfo> customConfigInfoList) {
        if (MULTI_WINDOW_REALTIME_MONITORING.equals(columnModule)) {
            //多窗口定制列,处理用户取消全部列
            String userUuid = userService.getCurrentUserUuid();
            RedisKey redisKey = HistoryRedisKeyEnum.USER_MULTI_WINDOW_REALTIME_DATA_LIST.of(userUuid);
            List<CustomColumnConfigInfo> allDatas = RedisHelper.getList(redisKey, CustomColumnConfigInfo.class);
            if (allDatas != null && allDatas.size() == 1 && "用户取消所有列".equals(allDatas.get(0).getMark())) {
                //用户将所有的列都清空设置了，那么就直接返回
                for (CustomColumnConfigInfo columnConfigInfo : customConfigInfoList) {
                    columnConfigInfo.setStatus(CustomColumnConfigInfo.NOT_DEFAULT_COLUMN);
                }
            }
        }
    }
}
