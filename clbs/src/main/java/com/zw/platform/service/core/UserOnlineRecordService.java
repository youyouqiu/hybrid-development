package com.zw.platform.service.core;

/**
 * 实例化service
 *
 * @author zhengjc
 * @since 2019/11/21 17:32
 * @version 1.0
 **/
public interface UserOnlineRecordService {
    /**
     * 添加用户上线记录
     * @param userName 上线用户名称
     * @return 用户上线记录id
     */
    String addUserOnlineRecord(String userName);

    void addUserOffline(String userName, String onlineRecordId);
}
