package com.cb.platform.repository.mysqlDao;


import com.cb.platform.domain.UserLogin;
import com.cb.platform.domain.UserOnline;
import com.cb.platform.domain.UserOnlineTime;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface OnlineTimeDao {

    /**
     * 添加用户上下线记录
     * @return
     */
    public boolean addUserTopAndBottomLine(UserOnline info);

    /**
     * 根据记录id查询用户上下线记录
     * @param id
     * @return
     */
    public UserOnline findOnlineTimeById(String id);

    /**
     * 根据记录id更新用户上下线记录的下线时间和下线时长
     * @param userOnline
     * @return
     */
    public boolean updateOffLineTime(UserOnline userOnline);

    /**
     * 根据用户id和时间查询用户当天的上线时间
     * @param userId
     * @param onlineDate
     * @return
     */
    public String findOnlineDuration(@Param("userId") String userId, @Param("onlineDate") String onlineDate);

    /**
     * 添加用户在线时长记录
     * @param userOnlineTime
     * @return
     */
    public boolean addUserOnlineTime(UserOnlineTime userOnlineTime);

    /**
     * 更新用户在线时长
     * @param userOnlineTime
     * @return
     */
    public boolean updateUserOnlineTime(UserOnlineTime userOnlineTime);

    /**
     * 根据道路运输企业和月份查询道路运输企业月在线时长
     * @param groupIds
     *            组织id
     * @param day1
     *            当月第一天
     * @param day2
     *            当月第二天
     * @return List<UserOnlineTime>
     */
    public List<UserOnlineTime> getMonthGroupOnlineTime(@Param("groupId") String groupIds,
                                                        @Param("day1") String day1, @Param("day2") String day2);

    /**
     * 根据道路运输企业和月份查询道路运输企业下用户当月在线时长
     * @param groupIds
     *            组织id
     * @param day1
     *            当月第一天
     * @param day2
     *            当月第二天
     * @return List<UserOnlineTime>
     */
    public List<UserOnlineTime> getMonthGroupUserOnlineTime(@Param("userIds") List<String> userIds,
                                                            @Param("day1") String day1, @Param("day2") String day2);

    /**
     * 根据用户id查询用户上下线明细
     * @param userIds
     *            用户id
     * @param startTime
     *            查询开始时间
     * @param endTime
     *            查询结束时间
     * @return
     */
    public List<UserLogin> getOnlineDataByIds(@Param("userIds") List<String> userIds,
                                              @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 根据组织id和时间查询企业下用户登录明细
     * @param startTime
     * @param endTime
     * @return
     */
    public List<UserLogin> getGroupUserOnlineData(@Param("groupIds") List<String> groupIds,
                                                       @Param("startTime") String startTime,
                                                       @Param("endTime") String endTime);

    /**
     * 根据用户id和时间查询用户登录明细
     */
    List<UserLogin> getUserOnlineData(@Param("userIds") List<String> userIds,
        @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 查询下线时间为null的数据
     */
    public List<UserOnline> findOfflineIsNullData();

    /**
     * 用户抽查车辆数量及百分比统计报表
     * 获取用户在岗时段
     * @param userIdList
     * @param startTime
     * @param endTime
     * @return
     */
    List<UserOnline> getUserOnlineTimeRange(@Param("userIdList") List<String> userIdList,
        @Param("startTime") String startTime, @Param("endTime") String endTime);
}
