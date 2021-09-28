package com.zw.app.service.alarm.impl;

/**
 * APP报警模块测试类
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:*.xml","classpath:*.properties","classpath:spring-config/*.xml"})
public class AppAlarmSearchServiceImplTest {
   /* @InjectMocks
    private static AppAlarmSearchServiceImpl appAlarmSearchService;

    @InjectMocks
    private static AppAlarmSearchDao appAlarmSearchDao;

    @InjectMocks
    private static AppAlarmTypeDao appAlarmTypeDao;

    @InjectMocks
    private static WebMasterAlarmTypeServiceImpl webMasterAlarmTypeServiceImpl;

    @BeforeClass
    public static void getAlarm() {
        appAlarmSearchService = new AppAlarmSearchServiceImpl();
        webMasterAlarmTypeServiceImpl = new WebMasterAlarmTypeServiceImpl();
        appAlarmTypeDao = Mockito.mock(AppAlarmTypeDao.class);
        appAlarmSearchDao = Mockito.mock(AppAlarmSearchDao.class);
        appAlarmSearchService.setAppAlarmSearchDao(appAlarmSearchDao);
        appAlarmSearchService.setAppAlarmTypeDao(appAlarmTypeDao);
//        Mockito.when(appAlarmSearchDao.getAlarmDate(Mockito.)).thenReturn(getActionDay());
        Mockito.when(appAlarmTypeDao.getAlarmMaxDateByGroupId(Mockito.anyString())).thenReturn(getAlarMaxDate());
    }

    public static List<AppAlarmAction> getActionDay() {
        List<AppAlarmAction> actionDay = new LinkedList<>();
        AppAlarmAction appAlarmAction = new AppAlarmAction();
//        appAlarmAction.setActionDay(9);
        appAlarmAction.setAlarmCount(2000);
        actionDay.add(appAlarmAction);
        return actionDay;
    }

    public static int getAlarMaxDate(){
        return 30;
    }
    private static int getAlarmNumber() {
            return 6975;
    }

    @Test
    public void getAlarmInfo() {

    }

    @Test
    public void getMonitorAlarmAction() {
        try {
            System.out.println("查询监控对报警活跃时间");
            AppAlarmQuery appAlarmQuery = new AppAlarmQuery();
            appAlarmQuery.setStartTime("2018-07-29 00:00:00");
            appAlarmQuery.setEndTime("2018-08-29 23:00:00");
            appAlarmQuery.setPageSize(10);
            appAlarmQuery.setPage(1);
            appAlarmQuery.setAlarmType("71,72,73,74,75,76,77,79,6811,6812");
            List<?> result = appAlarmSearchService.getMonitorAlarmAction(
                "e458f2ae-92b0-409d-8cf3-8f556b857c76",appAlarmQuery);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出错了");
        }
    }

    @Test
    public void getMonitorAlarmDetail() {
    }

    @Test
    public void getAlarMonitorNumber() {
    }

    *//**
     * 获取用户的报警参数设置
     *//*
    @Test
    public void getUserAlarmSetting() {
    }*/
}