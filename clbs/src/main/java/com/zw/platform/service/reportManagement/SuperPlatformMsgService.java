package com.zw.platform.service.reportManagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.reportManagement.SuperPlatformMsg;
import com.zw.platform.domain.reportManagement.Zw809MessageDO;
import com.zw.platform.dto.platformInspection.Zw809MessageDTO;

import java.util.List;


/**
 * 上级平台消息处理service
 */
public interface SuperPlatformMsgService {

    /**
     * 存储809上级平台消息
     */
    void saveSuperPlatformMsg(SuperPlatformMsg superPlatformMsg);

    /**
     * 批量存储809上级平台消息
     * @param superPlatformMsgList
     */
    void batchSaveSuperPlatformMsg(List<SuperPlatformMsg> superPlatformMsgList);

    /**
     * 更新上级平台消息为已处理
     */
    void updateSuperPlatformMsg(String id, Integer result, String ackContent);

    /**
     * 根据id查询消息的处理状态
     * @param id
     * @return
     */
    Integer getMsgStatus(String id);


    /**
     * 更新已过期的数据
     */
    void updatePastData();

    /**
     *  查询用户权限下所有未处理的上级平台消息数量
     */
    JSONObject getTheDayPlatformMsg();

    /**
     * 查询用户权限下当天所有的上级平台消息
     * @return
     */
    List<Zw809MessageDTO> getTheDayAllMsgByUser(String type, String startTime, String endTime, int status);

    /**
     * 查询809消息
     * @param id id
     * @return Zw809MessageDO
     */
    Zw809MessageDO get809Message(String id);

    /**
     * 迁移数据
     *
     * @return 迁移结果摘要
     */
    String migrate809Message();

    /**
     * 删除历史数据
     */
    void deleteOldMessages();
}
