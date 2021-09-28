package com.zw.platform.repository.modules;

import com.zw.adas.domain.report.inspectuser.InspectUserDTO;
import com.zw.adas.domain.report.inspectuser.InspectUserQuery;
import com.zw.platform.domain.reportManagement.SuperPlatformMsg;
import com.zw.platform.domain.reportManagement.Zw809MessageDO;
import com.zw.platform.dto.platformInspection.Zw809MessageDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface Zw809MessageDao {
    /**
     * 上级平台消息存储
     */
    void insert(Zw809MessageDO zw809MessageDO);

    /**
     * 更新处理结果
     */
    void updateMsgStatus(Zw809MessageDO superPlatformMsg);

    /**
     * 根据id查询处理状态
     */
    Integer getMsgStatus(String id);

    /**
     * 查询所有未处理的上级平台消息
     */
    List<Zw809MessageDO> getAllUntreatedMsg();

    /**
     * 获得上级平台消息
     * @param id id
     * @return Zw809MessageDO
     */
    Zw809MessageDO getMsgById(String id);

    /**
     * 修改所有未处理的上级平台消息数量
     */
    List<String> getAllUntreatedMsgIds(@Param("time") String time);

    /**
     * 更新已过期数据的状态
     */
    void updatePastData(@Param("msgIds") List<String> msgIds);

    List<Zw809MessageDO> listByTime(@Param("startTime") String startTime,
                                    @Param("endTime") String endTime,
                                    @Param("groupId") String groupId);

    List<Zw809MessageDO> getTheDayAllMsgByUser(@Param("startTime") String startTime,
                                               @Param("endTime") String endTime,
                                               @Param("groupId") String groupId,
                                               @Param("msgType") Integer msgType,
                                               @Param("status") Integer status);

    List<Zw809MessageDTO> getTheDayAllMsgByGroup(@Param("startTime") String startTime,
                                                 @Param("endTime") String endTime,
                                                 @Param("groupId") String[] groupId,
                                                 @Param("msgType") Integer msgType,
                                                 @Param("status") Integer status);

    /**
     * 数据迁移专用
     */
    List<SuperPlatformMsg> listByTimeLaterThan(@Param("startTime") String startTime,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

    /**
     * 数据迁移专用
     */
    int batchInsert(List<Zw809MessageDO> newMessages);

    /**
     * 定时任务专用
     */
    int deleteByTimeEarlierThan(String earliestTime);

    /**
     * 获取巡检用户
     * @param query
     * @return
     */
    List<InspectUserDTO> getListByKeyWord(InspectUserQuery query);

    /**
     * 更新
     * @param zw809MessageDO
     */
    void update(Zw809MessageDO zw809MessageDO);
}
