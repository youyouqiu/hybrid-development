package com.zw.platform.service.basicinfo;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/9/16 9:21
 */
public interface InitRedisCacheService {
    /**
     * 初始化缓存的总方法
     */
    void addCacheToRedis();

    /**
     * 初始化绑定的传感器信息
     */
    void initBindingSensor();

    /**
     * 初始化货运数据缓存
     */
    void initCargoGroupVids();

    /**
     * 初始化 redis中默认风险参数定义设置
     */
    void initRiskEventSetting();

    /**
     * 清空废弃的redis缓存key
     */
    void clearAbandonedRedis();

}
