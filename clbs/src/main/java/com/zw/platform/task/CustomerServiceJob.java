package com.zw.platform.task;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.leaderboard.CustomerService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.PrecisionUtils;
import com.zw.platform.util.common.PropsUtil;
import com.zw.ws.common.PublicVariable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CustomerServiceJob implements Job {

    private static final Logger log = LogManager.getLogger(SaveExpireLicense.class);

    private static String riskPeopleName;

    /**
     * 定时任务记录每小时整点时刻客服总数
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx;
        LocalDateTime dateTime = LocalDateTime.now();

        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (appCtx != null) {
                riskPeopleName = Optional.ofNullable(riskPeopleName)
                    .orElse(PropsUtil.getValue("adas.risk.people.name", "application.properties"));
                UserService userService = appCtx.getBean(UserService.class);
                if (userService != null) {
                    int customerServiceTotal = userService.queryRoleUserSize(riskPeopleName);
                    Set<String> onlineCustomerService =
                        new HashSet<>(RedisHelper.scanKeys(HistoryRedisKeyEnum.ADAS_REPORT_CUSTOMER_SERVICE.of("*")));
                    RedisKey onlineCustomerServiceKey =
                        HistoryRedisKeyEnum.ADAS_REPORT_ONLINE_CUSTOMER_SERVICE.of(Date8Utils.getValToDay(dateTime));
                    long time = Date8Utils.getValToHour(dateTime);
                    String value = getCuntomerService(onlineCustomerService.size(), customerServiceTotal, time);
                    storeDataToRedis(onlineCustomerServiceKey, time + "", value);
                }
            }
            RedisKey key = HistoryRedisKeyEnum.CUSTOMER_SERVICE_JOB.of(Date8Utils.getValToHour(dateTime));
            RedisHelper.setString(key, "done", PublicVariable.REDIS_CACHE_TIMEOUT_HOUR);
        } catch (Exception e) {
            log.error("记录每小时整点时刻客服总数失败.");
        }

    }

    private String getCuntomerService(int onlineCustomerService, int customerServiceTotal, long time) {
        CustomerService customerService = new CustomerService();
        customerService.setOnline(onlineCustomerService);
        customerService.setTime(time);
        customerService.setTotal(customerServiceTotal);
        double rate = customerServiceTotal == 0 ? 0 : (1.0 * onlineCustomerService / customerServiceTotal * 100);
        customerService.setRate(PrecisionUtils.roundByScale(rate, 2));
        return JSONObject.toJSONString(customerService);
    }

    private void storeDataToRedis(RedisKey onlineCustomerServiceKey, String field, String value) {
        RedisHelper.addToHash(onlineCustomerServiceKey, field, value);
        RedisHelper.expireKey(onlineCustomerServiceKey, PublicVariable.REDIS_CACHE_TIMEOUT_DAY * 3);
    }
}
