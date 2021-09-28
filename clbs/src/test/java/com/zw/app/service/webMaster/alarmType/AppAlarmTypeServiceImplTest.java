package com.zw.app.service.webMaster.alarmType;

import com.alibaba.fastjson.JSONArray;
import com.zw.app.domain.webMaster.alarmType.AppAlarmConfigInfo;
import com.zw.app.repository.mysql.webMaster.alarmType.AppAlarmTypeDao;
import com.zw.app.service.webMaster.alarmType.impl.WebMasterAlarmTypeServiceImpl;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.service.core.UserService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.naming.CompositeName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AppAlarmTypeServiceImplTest {
 /*   @InjectMocks
    private static UserService userService;
    @InjectMocks
    private static AppAlarmTypeDao appAlarmTypeDao;
    @InjectMocks
    private static MockHttpServletRequest mockHttpServletRequest;
    private static WebMasterAlarmTypeServiceImpl webMasterAlarmTypeService;
    @BeforeClass
    public static void setUp() {
        //模拟写入用户信息到session
        HttpSession session = new MockHttpSession();
        mockHttpServletRequest = new MockHttpServletRequest();
        session.setAttribute("admin", getCurrentUser());
        mockHttpServletRequest.setSession(session);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes((HttpServletRequest) mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        userService = Mockito.mock(UserService.class);
        appAlarmTypeDao = Mockito.mock(AppAlarmTypeDao.class);
        Mockito.when(userService.getOrgByUuid("1cc669e4-2972-1038-8414-657df3054443")).thenReturn(getOrganizationLdap());
        Mockito.when(userService.getOrgUuidByUser()).thenReturn("1cc669e4-2972-1038-8414-657df3054443");
        Mockito.when(userService.getOrgUuidsByUser("")).thenReturn(getGroupIds());
        Mockito.when(appAlarmTypeDao.getAlarmType("1cc669e4-2972-1038-8414-657df3054443",0)).thenReturn(getAppAlarmConfigInfos());
        Mockito.when(appAlarmTypeDao.getAlarmType("1cc669e4-2972-1038-8414-657df3054443",1)).thenReturn(getAppAlarmConfigInfos());
        Mockito.when(appAlarmTypeDao.getAlarmType("f09cef2c-2a71-1038-8430-657df3054443",0)).thenReturn(null);
        Mockito.when(appAlarmTypeDao.getAlarmType("f09cef2c-2a71-1038-8430-657df3054443",1)).thenReturn(null);
        Mockito.when(appAlarmTypeDao.getGroupName("1cc669e4-2972-1038-8414-657df3054443")).thenReturn("测试企业（test）");
        Mockito.when(appAlarmTypeDao.getGroupName("f09cef2c-2a71-1038-8430-657df3054443")).thenReturn("test2018");
        Mockito.when(appAlarmTypeDao.addGroupAlarmType(getAppAlarmConfigInfos())).thenReturn(true);
        Mockito.when(appAlarmTypeDao.deleteGroupAlarmType("1cc669e4-2972-1038-8414-657df3054443",0)).thenReturn(true);
        Mockito.when(appAlarmTypeDao.deleteGroupAlarmType("1cc669e4-2972-1038-8414-657df3054443",1)).thenReturn(true);
        webMasterAlarmTypeService = new WebMasterAlarmTypeServiceImpl();
        webMasterAlarmTypeService.setAppAlarmTypeDao(appAlarmTypeDao);
        webMasterAlarmTypeService.setUserService(userService);
    }

    *//**
     * 模拟用户的信息
     *//*
    private static UserLdap getCurrentUser() {
        Collection collection = new ArrayList();
        UserLdap userLdap = new UserLdap("lijie1", "1", true, true,
                true, true, collection, new CompositeName(), "1", "1",
                "1", "1", "1", "1");
        return userLdap;
    }
    *//**
     * 模拟用户的组织信息
     *//*
    private static OrganizationLdap getOrganizationLdap(){
        OrganizationLdap organizationLdap = new OrganizationLdap();
        organizationLdap.setName("测试企业（test）");
        return  organizationLdap;
    }
    *//**
     * 模拟用户所在组织的下级组织集合
     *//*
    private static List<String> getGroupIds(){
        List<String> groups = new ArrayList<>();
        groups.add("f09cef2c-2a71-1038-8430-657df3054443");
        groups.add("269d63e0-3ac1-1038-8475-657df3054443");
        return groups;
    }

    *//**
     * 模拟查询到的报警配置
     *//*
    private static List<AppAlarmConfigInfo> getAppAlarmConfigInfos(){
        List<AppAlarmConfigInfo> appAlarmConfigInfos = new ArrayList<>();
        AppAlarmConfigInfo appAlarmConfigInfo1 = new AppAlarmConfigInfo();
        appAlarmConfigInfo1.setType("1");
        appAlarmConfigInfo1.setName("紧急报警");
        appAlarmConfigInfo1.setCategory("驾驶员引起报警");
        appAlarmConfigInfo1.setGroupName("测试企业（test）");
        AppAlarmConfigInfo appAlarmConfigInfo2 = new AppAlarmConfigInfo();
        appAlarmConfigInfo2.setType("2");
        appAlarmConfigInfo2.setName("超速报警");
        appAlarmConfigInfo2.setCategory("驾驶员引起报警");
        appAlarmConfigInfo2.setGroupName("测试企业（test）");
        appAlarmConfigInfos.add(appAlarmConfigInfo1);
        appAlarmConfigInfos.add(appAlarmConfigInfo2);
        return appAlarmConfigInfos;
    }

    *//**
     * 模拟修改配置
     *//*
    private  static JSONArray updateAlarmConfig(){
        JSONArray jsonArray = new JSONArray();
        List<AppAlarmConfigInfo> appAlarmConfigInfos = new ArrayList<>();
        AppAlarmConfigInfo appAlarmConfigInfo1 = new AppAlarmConfigInfo();
        appAlarmConfigInfo1.setType("1");
        appAlarmConfigInfo1.setName("紧急报警");
        appAlarmConfigInfo1.setCategory("驾驶员引起报警");
        appAlarmConfigInfo1.setGroupName("测试企业（test）");
        AppAlarmConfigInfo appAlarmConfigInfo2 = new AppAlarmConfigInfo();
        appAlarmConfigInfo2.setType("2");
        appAlarmConfigInfo2.setName("超速报警");
        appAlarmConfigInfo2.setCategory("驾驶员引起报警");
        appAlarmConfigInfo2.setGroupName("测试企业（test）");
        appAlarmConfigInfos.add(appAlarmConfigInfo1);
        appAlarmConfigInfos.add(appAlarmConfigInfo2);
        return jsonArray.fluentAdd(appAlarmConfigInfos);
    }
    @Test
    public void getAlarmType() {
        try{
            System.out.println("获取报警参数");
            System.out.println(webMasterAlarmTypeService.getAlarmType("1cc669e4-2972-1038-8414-657df3054443"));
        }catch (Exception e){
            System.out.println("获取报警参数方法有错误！");
        }
    }

    @Test
    public void updateAlarmType() {
        try{
            System.out.println("修改报警参数配置");
            System.out.println(webMasterAlarmTypeService.updateAlarmType(updateAlarmConfig(),"1cc669e4-2972-1038-8414-657df3054443"));
        }catch (Exception e){
            System.out.println("修改报警参数配置方法有错误！");
        }
    }

    @Test
    public void resetAlarmType() {
        try{
            System.out.println("恢复报警参数配置为默认");
            System.out.println(webMasterAlarmTypeService.resetAlarmType());
        }catch (Exception e){
            System.out.println("恢复报警参数配置为默认方法有错误！");
        }
    }

    @Test
    public void defaultAlarmType() {
        try{
            System.out.println("设置报警参数配置为组织默认");
            System.out.println(webMasterAlarmTypeService.defaultAlarmType());
        }catch (Exception e){
            System.out.println("设置报警参数配置为组织默认方法有错误！");
        }
    }

    @Test
    public void referenceGroup() {
        try{
            System.out.println("获取参考组织信息");
            System.out.println(webMasterAlarmTypeService.referenceGroup());
        }catch (Exception e){
            System.out.println("获取参考组织信息方法有错误！");
        }
    }*/
}
