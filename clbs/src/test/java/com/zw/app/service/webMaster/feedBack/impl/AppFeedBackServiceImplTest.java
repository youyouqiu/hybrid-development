package com.zw.app.service.webMaster.feedBack.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.webMaster.feedBack.FeedBack;
import com.zw.app.domain.webMaster.feedBack.FeedBackQuery;
import com.zw.app.repository.mysql.webMaster.feedBack.AppFeedBackDao;
import com.zw.platform.domain.core.UserLdap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

public class AppFeedBackServiceImplTest {
   /* private  static AppFeedBackServiceImpl appFeedBackService ;
    @InjectMocks
    private static AppFeedBackDao appFeedBackDao;
    @InjectMocks
    private static MockHttpServletRequest mockHttpServletRequest;
    @BeforeClass
    public static void setUp() {
        HttpSession session = new MockHttpSession();
        mockHttpServletRequest = new MockHttpServletRequest();
        session.setAttribute("admin",getCurrentUser());
        mockHttpServletRequest.setSession( session);
        ServletRequestAttributes servletRequestAttributes =   new ServletRequestAttributes((HttpServletRequest)mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        appFeedBackService = new AppFeedBackServiceImpl();
        appFeedBackDao = Mockito.mock(AppFeedBackDao.class);
        Mockito.when(appFeedBackDao.getFeedBackTotal(getFeedBackQuery())).thenReturn(20);
        Mockito.when(appFeedBackDao.searchFeedBack(getFeedBackQuery())).thenReturn(getFeedBack());
        Mockito.when(appFeedBackDao.addFeedBack(Mockito.any())).thenReturn(true);
        appFeedBackService.setAppFeedBackDao(appFeedBackDao);
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

    private static FeedBackQuery getFeedBackQuery(){
        FeedBackQuery feedBackQuery = new FeedBackQuery();
        feedBackQuery.setUserName("lijie");
        feedBackQuery.setStartTime("2018-08-08 00:00:00");
        feedBackQuery.setEndTime("2018-08-31 00:00:00");
        return  feedBackQuery;
    }

    private static List<FeedBack> getFeedBack(){
        List<FeedBack> feedBacks= new ArrayList<>();
        FeedBack feedBack1 = new FeedBack();
        feedBack1.setFeedBack("app快开发完了！");
        feedBack1.setSubmitDate(new Date());
        feedBack1.setUserName("lijie");
        feedBacks.add(feedBack1);
        FeedBack feedBack2 = new FeedBack();
        feedBack2.setFeedBack("app开始测试了！");
        feedBack2.setSubmitDate(new Date());
        feedBack2.setUserName("lijie1");
        feedBacks.add(feedBack2);
        return feedBacks;
    }

    @Test
    public void searchFeedBack() {
        try{
            System.out.println("分页查询app意见反馈");
            System.out.println(appFeedBackService.searchFeedBack(getFeedBackQuery()));
        }catch (Exception e){
            System.out.println("分页查询app意见反馈方法有错误！");
        }
    }

    @Test
    public void sendFeedBack() {
        try{
            System.out.println("发送app意见反馈");
            System.out.println(appFeedBackService.sendFeedBack("app"));
        }catch (Exception e){
            System.out.println("发送app意见反馈方法有错误！");
        }
    }*/
}
