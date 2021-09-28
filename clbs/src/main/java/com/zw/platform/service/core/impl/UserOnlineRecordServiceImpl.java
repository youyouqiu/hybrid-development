package com.zw.platform.service.core.impl;

import com.cb.platform.domain.UserOnline;
import com.cb.platform.domain.UserOnlineTime;
import com.cb.platform.repository.mysqlDao.OnlineTimeDao;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.core.UserOnlineRecordService;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.common.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author zhengjc
 * @since 2019/11/21 17:34
 * @version 1.0
 **/
@Service
@Slf4j
public class UserOnlineRecordServiceImpl implements UserOnlineRecordService {
    private static final long DAY_SECOND = 86400;

    @Autowired
    UserService userService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    private OnlineTimeDao onlineTimeDao;

    /**
     * 增加用户上线记录
     */
    @Override
    public String addUserOnlineRecord(String userName) {
        try {
            // 添加用户上线记录
            UserOnline userOnline = new UserOnline();
            UserDTO userDto = userService.getUserByUsername(userName); // 根据用户名查询用户信息
            if (userDto == null) {
                return null;
            }
            // 用户所属组织的uuid
            OrganizationLdap orgByEntryDn =
                organizationService.getOrgByEntryDn(userService.getUserOrgDnByDn(userDto.getId().toString()));
            String orgUuid = orgByEntryDn.getUuid();
            // 根据userName获取userId
            String userUuid = userDto.getUuid();
            userOnline.setGroupId(StringUtils.isNotEmpty(orgUuid) ? orgUuid : "");
            userOnline.setUserId(StringUtils.isNotEmpty(userUuid) ? userUuid : "");
            Date onlineTime = DateUtils.round(new Date(), Calendar.SECOND); // 将时间精度转换为秒
            userOnline.setOnlineTime(onlineTime);
            userOnline.setCreateDataUsername(userName);
            onlineTimeDao.addUserTopAndBottomLine(userOnline);
            return userOnline.getId();
        } catch (Exception e) {
            log.error("添加用户上线记录出错", e);
            return null;
        }
    }

    /**
     * 用户下线处理
     */
    @Override
    public void addUserOffline(String userName, String onlineRecordId) {
        try {
            log.info("用户下线处理 : " + userName);
            UserOnline userOnline = onlineTimeDao.findOnlineTimeById(onlineRecordId); // 用户上下线记录
            if (userOnline != null) { // 用户有上线记录
                Date onlineTime = userOnline.getOnlineTime(); // 上线时间
                Date nowDate = new Date(); // 下线时间(当前时间)
                Date offerOnlineTime = DateUtils.round(nowDate, Calendar.SECOND); // 将时间精度转换为秒
                if (offerOnlineTime.getTime() < onlineTime.getTime()) { // 下线时间大于等于上线时间
                    offerOnlineTime = onlineTime;
                }
                updateOfflineTime(onlineTime, offerOnlineTime, userOnline, userName); // 更新下线时间
                countUserDayOnlineTime(onlineTime, offerOnlineTime, userName);
            }
        } catch (Exception e) {
            log.error("用户下线处理异常", e);
        }
    }

    /**
     * 更新下线时间
     */
    private void updateOfflineTime(Date onlineTime, Date offlineTime, UserOnline userOnline, String userName) {
        try {
            long onlineDuration = (offlineTime.getTime() - onlineTime.getTime()) / 1000; // 在线时长
            userOnline.setOfflineTime(offlineTime);
            userOnline.setOnlineDuration(onlineDuration); // 在线时长
            userOnline.setUpdateDataUsername(userName);
            onlineTimeDao.updateOffLineTime(userOnline);
        } catch (Exception e) {
            log.error("更新用户下线时间异常");
        }
    }

    /**
     * 计算用户的天在线时长
     * @param onlineTime      上线时间
     * @param offlineTime 下线时间
     */
    private void countUserDayOnlineTime(Date onlineTime, Date offlineTime, String userName) {
        try {
            // 记录用户当日在线时间
            Calendar calOnline = Calendar.getInstance();
            calOnline.setTime(onlineTime);
            int onYear = calOnline.get(Calendar.YEAR); // 上线时间的年
            int onMonth = calOnline.get(Calendar.MONTH) + 1; // 上线时间的月
            int onDay = calOnline.get(Calendar.DAY_OF_MONTH); // 上线时间的天
            calOnline.setTime(offlineTime);
            int upYear = calOnline.get(Calendar.YEAR); // 下线时间的年
            int upDay = calOnline.get(Calendar.DAY_OF_MONTH); // 下线时间的天
            int upMonth = calOnline.get(Calendar.MONTH) + 1; // 下线时间的月
            String formatDay = DateUtil.getDateToString(onlineTime, "yyyy-MM-dd");
            Date date = DateUtil.getStringToDate(formatDay, "yyyy-MM-dd");
            if (onYear == upYear && onMonth == upMonth) { // 上线时间和下线时间在本年本月的情况
                if (onDay == upDay) { // 上线时间和下线时间为同一天
                    long onlineDuration = (offlineTime.getTime() - onlineTime.getTime()) / 1000; // 本次上线在线时长
                    theSameDay(date, formatDay, userName, onlineDuration);
                } else if (onDay < upDay) { // 跨天(长时间登录的情况)
                    cmccOnline(onlineTime, offlineTime, userName);
                }
            } else { // 跨月登录的情况和跨年登录的情况
                cmccOnline(onlineTime, offlineTime, userName);
            }
        } catch (Exception e) {
            log.error("记录用户在线时长出错", e);
        }

    }

    /**
     * 计算用户上下线时间为当天的情况的在线时长
     */
    private void theSameDay(Date date, String formatDay, String userName, long onlineDuration) {
        UserDTO userDTO = userService.getUserByUsername(userName); // 根据用户名查询用户信息
        if (userDTO != null) {
            // 用户所属组织的uuid
            Optional<String> groupUuid =
                Optional.ofNullable(organizationService.getOrgUuidByUserId(userDTO.getId().toString()));
            // 根据userName获取userId
            Optional<String> userUuid = Optional.ofNullable(userDTO.getUuid());
            String userId = userUuid.orElse("");
            String groupId = groupUuid.orElse("");
            String dayOnlineDuration = onlineTimeDao.findOnlineDuration(userId, formatDay);
            UserOnlineTime userOnlineTime = new UserOnlineTime();
            if (dayOnlineDuration == null) { // 用户当天第一次上线
                addUserOnlineTime(userId, groupId, date, onlineDuration, userName);
            } else { // 用户第N次上线
                long onlineTime = onlineDuration + Long.parseLong(dayOnlineDuration);
                if (onlineTime > DAY_SECOND) {
                    onlineTime = DAY_SECOND;
                }
                userOnlineTime.setUserId(userId); // 用户id
                userOnlineTime.setOnlineDate(date); // 上线日期
                userOnlineTime.setOnlineDuration(onlineTime); // 当天在线时长
                userOnlineTime.setUpdateDataUsername(userName);
                onlineTimeDao.updateUserOnlineTime(userOnlineTime);
            }
        }
    }

    /**
     * 处理跨天,跨月,跨年登录的情况
     * @param onlineTime      上线时间
     * @param offerOnlineTime 下线时间
     */
    private void cmccOnline(Date onlineTime, Date offerOnlineTime, String onlineUserName) {
        UserDTO userDTO = userService.getUserByUsername(onlineUserName); // 根据用户名查询用户信息
        if (userDTO != null) {
            // 用户所属组织的uuid
            Optional<String> groupUuid =
                Optional.ofNullable(organizationService.getOrgUuidByUserId(userDTO.getId().toString()));
            // 根据userName获取userId
            Optional<String> userUuid = Optional.ofNullable(userDTO.getUuid());
            String userId = userUuid.orElse("");
            String groupId = groupUuid.orElse("");
            List<String> everyDayTime =
                BigDataQueryUtil.getTwoTimeMiddleEveryDayTime(onlineTime.getTime(), offerOnlineTime.getTime());
            if (CollectionUtils.isNotEmpty(everyDayTime)) {
                String strOnlineTime = DateFormatUtils.format(onlineTime, "yyyyMMdd");
                String strOfferTime = DateFormatUtils.format(offerOnlineTime, "yyyyMMdd");
                everyDayTime.forEach(every -> {
                    if (strOnlineTime.equals(every)) { // 上线的天
                        // 计算在线时长
                        Long distanceEndTime = DateUtil.getDistanceEndTime(onlineTime);
                        addOrUpdateDayOnline(userId, groupId, every, distanceEndTime, onlineUserName);
                    } else if (strOfferTime.equals(every)) { // 下线的天
                        Long distanceStartTime = DateUtil.getDistanceStartTime(offerOnlineTime);
                        addOrUpdateDayOnline(userId, groupId, every, distanceStartTime, onlineUserName);
                    } else { // 中间的天
                        addOrUpdateDayOnline(userId, groupId, every, DAY_SECOND, onlineUserName);
                    }
                });
            }
        }
    }

    /**
     * 增加用户在线时长数据
     */
    private void addUserOnlineTime(String userId, String groupId, Date date, long onlineTime, String userName) {
        try {
            if (onlineTime > DAY_SECOND) {
                onlineTime = DAY_SECOND;
            }
            UserOnlineTime userOnlineTime = new UserOnlineTime();
            userOnlineTime.setUserId(userId);
            userOnlineTime.setGroupId(groupId);
            userOnlineTime.setOnlineDate(date);
            userOnlineTime.setOnlineDuration(onlineTime); // 当天在线时长
            userOnlineTime.setCreateDataUsername(userName);
            onlineTimeDao.addUserOnlineTime(userOnlineTime);
        } catch (Exception e) {
            log.error("添加用户在线时长数据异常", e);
        }
    }

    private void addOrUpdateDayOnline(String userId, String groupId, String every, long totalTime, String userName) {
        try {
            String dayOnlineDuration = onlineTimeDao.findOnlineDuration(userId, every);
            Date online = DateUtil.getStringToDate(every, "yyyyMMdd");
            if (StringUtils.isBlank(dayOnlineDuration)) {
                addUserOnlineTime(userId, groupId, online, totalTime, userName);
            } else { // 用户有上线
                long onlineTime = totalTime + Long.parseLong(dayOnlineDuration);
                if (onlineTime > DAY_SECOND) {
                    onlineTime = DAY_SECOND;
                }
                UserOnlineTime userOnlineTime = new UserOnlineTime();
                userOnlineTime.setUserId(userId); // 用户id
                userOnlineTime.setOnlineDate(online); // 上线日期
                userOnlineTime.setOnlineDuration(onlineTime); // 当天在线时长
                userOnlineTime.setUpdateDataUsername(userName);
                onlineTimeDao.updateUserOnlineTime(userOnlineTime);
            }
        } catch (Exception e) {
            log.error("添加/更新用户在线时长记录出错", e);
        }
    }
}
