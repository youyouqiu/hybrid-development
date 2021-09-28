package com.cb.platform.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.cb.platform.domain.MonthGroupOnlineTime;
import org.springframework.stereotype.Service;

import com.cb.platform.domain.MonthOnlineTime;
import com.cb.platform.domain.UserLogin;


@Service
public interface UserOnlineTimeService {

    /**
     * 道路运输企业月在线时间查询
     * @param groupId
     * @param day1
     * @param day2
     * @return
     */
    public List<MonthGroupOnlineTime> findMonthGroupOnlineTime(String groupId, String day1, String day2) throws Exception;

    /**
     * 导出道路运输企业月在线时长数据
     * @param title
     * @param type
     * @param response
     * @return
     * @throws Exception
     */
    public boolean exportMonthGroup(String title, int type, HttpServletResponse response,String groupId, String nowMonth, String nextMonth)
        throws Exception;

    /**
     * 道路运输企业用户月在线时间按查询
     * @param groupId
     * @param day1
     * @param day2
     * @return
     */
    public List<MonthOnlineTime> findMonthGroupUserOnlineTime(String groupId, String day1, String day2)
        throws Exception;

    /**
     * 导出道路运输企业用户月在线时长数据
     * @param title
     * @param type
     * @param response
     * @param resultData
     * @return
     * @throws Exception
     */
    public boolean exportMonthUser(String title, int type, HttpServletResponse response,String groupId, String nowMonth, String nextMonth)
        throws Exception;

    /**
     * 根据用户id查询用户上下线明细表
     * @param userIds
     * @param startTime
     * @param endTime
     * @return
     */
    public List<UserLogin> findUserOnlineOfflineDate(String userIds, String startTime, String endTime)
        throws Exception;

    /**
     * 导出用户登录明细表
     * @param title
     * @param type
     * @param response
     * @param resultData
     * @return
     */
    public boolean exportUserLoginDetail(String title, int type, HttpServletResponse response,
                                         List<UserLogin> resultData)
        throws Exception;
}
