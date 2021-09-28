package com.zw.app.service.webMaster.personalized.impl;

import com.zw.app.domain.webMaster.personalized.AppPersonalized;
import com.zw.app.repository.mysql.webMaster.personalized.AppPersonalizedDao;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.service.core.UserService;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @author lijie
 * @date 2018/8/30 10:09
 */

public class AppPersonalizedServiceImplTest {
    /*@InjectMocks
    private static UserService userService;
    @InjectMocks
    private static AppPersonalizedDao appPersonalizedDao;
    @InjectMocks
    private static MockHttpServletRequest mockHttpServletRequest;

    private static AppPersonalizedServiceImpl appPersonalizedService;
    @BeforeClass
    public static void setUp() {
        //模拟写入用户信息到session
        HttpSession session = new MockHttpSession();
        mockHttpServletRequest = new MockHttpServletRequest();
        session.setAttribute("admin",getCurrentUser());
        mockHttpServletRequest.setSession( session);
        ServletRequestAttributes servletRequestAttributes =   new ServletRequestAttributes((HttpServletRequest)mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        appPersonalizedDao = Mockito.mock(AppPersonalizedDao.class);
        userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getSuperiorGroupIds()).thenReturn(getSuperiorGroupIds());
        Mockito.when(userService.getOrgUuidByUser()).thenReturn("1cc669e4-2972-1038-8414-657df3054443");
        Mockito.when(appPersonalizedDao.find("f09cef2c-2a71-1038-8430-657df3054443",1)).thenReturn(null);
        Mockito.when(appPersonalizedDao.find("1cc669e4-2972-1038-8414-657df3054443",1)).thenReturn(getDefaultAppPersonalized());
        Mockito.when(appPersonalizedDao.find("1cc669e4-2972-1038-8414-657df3054443",0)).thenReturn(getAppPersonalized());
        Mockito.when(appPersonalizedDao.addGroupData(Mockito.any())).thenReturn(true);
        Mockito.when(appPersonalizedDao.updateAppPersonalized(Mockito.any())).thenReturn(true);
        Mockito.when(appPersonalizedDao.getSameLoginLogo("logo.png","0d21afba-49d1-46e5-a117-99610c116fb8")).thenReturn(0);
        Mockito.when(appPersonalizedDao.getSameLoginLogo("appLogo.png","e6ca7098-e86b-4621-b57d-61a2ab3f144b")).thenReturn(1);
        Mockito.when(appPersonalizedDao.getSameGroupAvatar("favicon.ico","0d21afba-49d1-46e5-a117-99610c116fb8")).thenReturn(0);
        Mockito.when(appPersonalizedDao.getSameGroupAvatar("appFavicon.ico","e6ca7098-e86b-4621-b57d-61a2ab3f144b")).thenReturn(1);
        appPersonalizedService =new AppPersonalizedServiceImpl();
        appPersonalizedService.setUserService(userService);
        appPersonalizedService.setAppPersonalizedDao(appPersonalizedDao);
    }
    *//**
     * 模拟获取用户的上级组织
     *//*
    private static List<String> getSuperiorGroupIds() {
        List<String> groupIds = new ArrayList<>();
        groupIds.add("f09cef2c-2a71-1038-8430-657df3054443");
        groupIds.add("1cc669e4-2972-1038-8414-657df3054443");
        return groupIds;
    }
    *//**
     * 模拟用户的信息
     *//*
    private static UserLdap getCurrentUser() {
        Collection collection = new ArrayList();
        UserLdap userLdap = new UserLdap("lijie1", "1", true, true,
                true, true, collection, null, "1", "1",
                "1", "1", "1", "1");
        return userLdap;
    }
    *//**
     * 模拟组织默认配置数据
     *//*
    private static AppPersonalized getDefaultAppPersonalized(){
        AppPersonalized appPersonalized = new AppPersonalized();
        appPersonalized.setId("0d21afba-49d1-46e5-a117-99610c116fb8");
        appPersonalized.setGroupAvatar("favicon.ico");
        appPersonalized.setLoginLogo("logo.png");
        appPersonalized.setGroupDefault(0);
        appPersonalized.setHistoryTimeLimit(30);
        appPersonalized.setAboutPlatform("服务QQ：3516756375\\n服务电话：023-63516572-807\\n©2015-2017 中位（北京）科技有限公司");
        appPersonalized.setAlarmTimeLimit(7);
        appPersonalized.setMaxObjectnumber(100);
        appPersonalized.setPasswordPrompt("请联系平台运营找回密码\\n电话：400-1515-400");
        appPersonalized.setAggregationNumber(50);
        appPersonalized.setWebsiteName("www.zoomwell.cn/clbs");
        appPersonalized.setGroupId("1cc669e4-2972-1038-8414-657df3054443");
        appPersonalized.setLoginTitle("中位F3物联网监控平台");
        appPersonalized.setLoginPrompt("登录账号和密码和F3物联网\\n监控平台一致");
        appPersonalized.setUpdateDataUsername("lijie");
        appPersonalized.setUpdateDataTime(new Date());
        appPersonalized.setCreateDataUsername("lijie");
        appPersonalized.setCreateDataTime(new Date());
        appPersonalized.setFlag(1);
        return appPersonalized;
    }
    *//**
     * 模拟当前组织配置
     *//*
    private static AppPersonalized getAppPersonalized(){
        AppPersonalized appPersonalized = new AppPersonalized();
        appPersonalized.setId("e6ca7098-e86b-4621-b57d-61a2ab3f144b");
        appPersonalized.setGroupAvatar("appFavicon.ico");
        appPersonalized.setLoginLogo("AppLogo.png");
        appPersonalized.setGroupDefault(1);
        appPersonalized.setHistoryTimeLimit(30);
        appPersonalized.setAboutPlatform("服务QQ：3516756375\\n服务电话：023-63516572-807\\n©2015-2017 中位（北京）科技有限公司");
        appPersonalized.setAlarmTimeLimit(7);
        appPersonalized.setMaxObjectnumber(100);
        appPersonalized.setPasswordPrompt("请联系平台");
        appPersonalized.setAggregationNumber(50);
        appPersonalized.setWebsiteName("www.zoomwell.cn/clbs");
        appPersonalized.setGroupId("1cc669e4-2972-1038-8414-657df3054443");
        appPersonalized.setLoginTitle("中位F3物联网监控平台");
        appPersonalized.setLoginPrompt("登录账号和密码和F3物联网\\n监控平台一致");
        appPersonalized.setUpdateDataUsername("lijie");
        appPersonalized.setUpdateDataTime(new Date());
        appPersonalized.setCreateDataUsername("lijie");
        appPersonalized.setCreateDataTime(new Date());
        appPersonalized.setFlag(1);
        return appPersonalized;
    }
    *//**
     * 模拟要修改的配置数据
     *//*
    private static AppPersonalized getUpdateAppPersonalized(){
        AppPersonalized appPersonalized = new AppPersonalized();
        appPersonalized.setGroupDefault(1);
        appPersonalized.setAboutPlatform("服务QQ：3516756375\\n服务电话：023-63516572-807\\n©2015-2017 中位（北京）科技有限公司");
        appPersonalized.setUpdateDataUsername("lijie");
        appPersonalized.setUpdateDataTime(new Date());
        appPersonalized.setFlag(1);
        return appPersonalized;
    }

    @Test
    public  void find(){
        try{
            System.out.println("获取的组织app配置信息数据");
            System.out.println(appPersonalizedService.find());
        }catch (Exception e){
            System.out.println("获取的组织配置信息方法有错误！");
        }
    }

    @Test
    public void updateAppPersonalized() {
        try{
            System.out.println("修改组织的app配置");
            System.out.println(appPersonalizedService.updateAppPersonalized(getUpdateAppPersonalized()));
        }catch (Exception e){
            System.out.println("修改组织的app配置方法有错误！");
        }
    }

    @Test
    public void resetLoginTitle() {
        try{
            System.out.println("恢复登录标题为默认值");
            System.out.println(appPersonalizedService.resetLoginTitle());
        }catch (Exception e){
            System.out.println("恢复登录标题为默认值方法有错误！");
        }
    }

    @Test
    public void defaultLoginTitle() {
        try{
            System.out.println("设置当前登录页标题为组织默认值");
            System.out.println(appPersonalizedService.defaultLoginTitle("中位F3物联网监控平台"));
        }catch (Exception e){
            System.out.println("设置当前登录页标题为组织默认值方法有错误！");
        }
    }

    @Test
    public void resetLoginUrl() {
        try{
            System.out.println("恢复平台网址为组织默认值");
            System.out.println(appPersonalizedService.resetLoginUrl());
        }catch (Exception e){
            System.out.println("恢复平台网址为组织默认值方法有错误！");
        }
    }

    @Test
    public void defaultLoginUrl() {
        try{
            System.out.println("设置平台网址为组织默认值");
            System.out.println(appPersonalizedService.defaultLoginUrl("www.zoomwell.cn/clbs"));
        }catch (Exception e){
            System.out.println("设置平台网址为组织默认值方法有错误！");
        }
    }

    @Test
    public void resetAboutLogin() {
        try{
            System.out.println("恢复关于登录提示为组织默认");
            System.out.println(appPersonalizedService.resetAboutLogin());
        }catch (Exception e){
            System.out.println("恢复关于登录提示为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultAboutLogin() {
        try{
            System.out.println("设置关于登录提示为组织默认");
            System.out.println(appPersonalizedService.defaultAboutLogin("登录账号和密码和F3物联网\\n监控平台一致"));
        }catch (Exception e){
            System.out.println("设置关于登录提示为组织默认方法有错误！");
        }
    }

    @Test
    public void resetPwdComment() {
        try{
            System.out.println("恢复忘记密码提示为组织默认");
            System.out.println(appPersonalizedService.resetPwdComment());
        }catch (Exception e){
            System.out.println("恢复忘记密码提示为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultPwdComment() {
        try{
            System.out.println("设置当前忘记密码提示为组织默认");
            System.out.println(appPersonalizedService.defaultPwdComment("请联系平台"));
        }catch (Exception e){
            System.out.println("设置当前忘记密码提示为组织默认方法有错误！");
        }
    }

    @Test
    public void resetAboutUs() {
        try{
            System.out.println("恢复关于我们提示为组织默认");
            System.out.println(appPersonalizedService.resetAboutUs());
        }catch (Exception e){
            System.out.println("恢复关于我们提示为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultAboutUs() {
        try{
            System.out.println("设置当前关于我们为组织默认");
            System.out.println(appPersonalizedService.defaultAboutUs("服务QQ：3516756375\\n服务电话：023-63516572-807\\n©2015-2017 中位（北京）科技有限公司"));
        }catch (Exception e){
            System.out.println("设置当前关于我们为组织默认方法有错误！");
        }
    }

    @Test
    public void resetAggrNum() {
        try{
            System.out.println("恢复开始聚合对象数量为组织默认");
            System.out.println(appPersonalizedService.resetAggrNum());
        }catch (Exception e){
            System.out.println("恢复开始聚合对象数量为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultAggrNum() {
        try{
            System.out.println("设置开始聚合对象数量为组织默认");
            System.out.println(appPersonalizedService.defaultAggrNum(30));
        }catch (Exception e){
            System.out.println("设置开始聚合对象数量为组织默认方法有错误！");
        }
    }

    @Test
    public void resetHistoryPeriod() {
        try{
            System.out.println("恢复开始聚合对象数量为组织默认");
            System.out.println(appPersonalizedService.resetHistoryPeriod());
        }catch (Exception e){
            System.out.println("恢复开始聚合对象数量为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultHistoryPeriod() {
        try{
            System.out.println("设置历史数据最大查询时间范围为组织默认");
            System.out.println(appPersonalizedService.defaultHistoryPeriod(7));
        }catch (Exception e){
            System.out.println("设置历史数据最大查询时间范围为组织默认方法有错误！");
        }
    }

    @Test
    public void resetAlarmPeriod() {
        try{
            System.out.println("恢复报警最大查询时间范围为组织默认");
            System.out.println(appPersonalizedService.resetAlarmPeriod());
        }catch (Exception e){
            System.out.println("恢复报警最大查询时间范围为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultAlarmPeriod() {
        try{
            System.out.println("设置报警最大查询时间范围为组织默认");
            System.out.println(appPersonalizedService.defaultAlarmPeriod(30));
        }catch (Exception e){
            System.out.println("设置报警最大查询时间范围为组织默认方法有错误！");
        }
    }

    @Test
    public void resetMaxStatObjNum() {
        try{
            System.out.println("恢复统计最多选择对象数量为组织默认");
            System.out.println(appPersonalizedService.resetMaxStatObjNum());
        }catch (Exception e){
            System.out.println("恢复统计最多选择对象数量为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultMaxStatObjNum() {
        try{
            System.out.println("设置统计最多选择对象数量为组织默认");
            System.out.println(appPersonalizedService.defaultAlarmPeriod(50));
        }catch (Exception e){
            System.out.println("设置统计最多选择对象数量为组织默认方法有错误！");
        }
    }

    @Test
    public void updateLoginLogo() {
        try{
            //模拟上传的图片
            File file = new File("D:\\Desktop\\logo.png");
            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "png/plain", IOUtils.toByteArray(input));
            System.out.println("修改登录页logo，返回文件名");
            System.out.println(appPersonalizedService.updateLoginLogo(multipartFile,mockHttpServletRequest));
        }catch (Exception e){
            System.out.println("修改登录页logo失败");
        }
    }

    @Test
    public void resetLoginLogo() {
        try{
            System.out.println("恢复登录页logo为组织默认");
            System.out.println(appPersonalizedService.resetLoginLogo(mockHttpServletRequest));
        }catch (Exception e){
            System.out.println("恢复登录页logo为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultLoginLogo() {
        try{
            System.out.println("设置当前登录页logo为组织默认");
            System.out.println(appPersonalizedService.defaultLoginLogo("153570235790044.png",mockHttpServletRequest));
        }catch (Exception e){
            System.out.println("设置当前登录页logo为组织默认方法有错误！");
        }
    }

    @Test
    public void updateGroupAvatar() {
        try{
            //模拟上传的图片
            File file = new File("D:\\Desktop\\avatar.ico");
            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "ico/plain", IOUtils.toByteArray(input));
            System.out.println("修改组织头像，返回文件名");
            System.out.println(appPersonalizedService.updateGroupAvatar(multipartFile,mockHttpServletRequest));
        }catch (Exception e){
            System.out.println("修改组织头像失败");
        }
    }

    @Test
    public void resetGroupAvatar() {
        try{
            System.out.println("恢复组织头像为组织默认");
            System.out.println(appPersonalizedService.resetGroupAvatar(mockHttpServletRequest));
        }catch (Exception e){
            System.out.println("恢复组织头像为组织默认方法有错误！");
        }
    }

    @Test
    public void defaultGroupAvatar() {
        try{
            System.out.println("设置当前组织头像为组织默认");
            System.out.println(appPersonalizedService.defaultLoginLogo("153570235790044.png",mockHttpServletRequest));
        }catch (Exception e){
            System.out.println("设置当前组织头像为组织默认方法有错误！");
        }
    }*/
}
