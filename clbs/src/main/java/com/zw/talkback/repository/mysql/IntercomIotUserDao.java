package com.zw.talkback.repository.mysql;

import com.zw.talkback.domain.intercom.form.IntercomIotUserForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/***
 @Author zhengjc
 @Date 2019/8/20 15:10
 @Description 云调度员
 @version 1.0
 **/
public interface IntercomIotUserDao {

    void updateIntercomIotUsers(@Param("iotUserFormList") List<IntercomIotUserForm> iotUserFormList);

    void updateIntercomIotUser(@Param("iotUserForm") IntercomIotUserForm iotUserForm);

    List<IntercomIotUserForm> getIntercomIotUsersByUserNames(@Param("userNames") List<String> userNames);

    IntercomIotUserForm getIntercomIotUsersByUserName(@Param("userName") String userName);
}
