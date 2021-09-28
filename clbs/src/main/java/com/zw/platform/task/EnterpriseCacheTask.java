package com.zw.platform.task;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.domain.core.OrganizationLdap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 每天维护企业层级缓存关系，避免企业被删除后丢失历史数据
 * @Author zhangqiang
 * @Date 2020/5/14 16:55
 */
@Component
public class EnterpriseCacheTask {

    public static String DATE_TIME_FORMAT = "yyyy-MM";
    private Logger logger = LogManager.getLogger(EnterpriseCacheTask.class);
    @Autowired
    private OrganizationService organizationService;

    /**
     * 每日23:55执行维护
     */
    @Scheduled(cron = "0 55 23 * * ? ")
    public void executeOrgChildToRedis() {
        logger.info("定时维护企业层级关系到redis中开始");
        try {
            List<OrganizationLdap> orgs = organizationService.getOrgChildList("ou=organization");
            Map<String, String> orgChildMap = orgs.stream().collect(Collectors.toMap(OrganizationLdap::getUuid, o -> {
                List<String> childOrgIds = organizationService.getOrgChildUUidList(o.getEntryDN());
                if (CollectionUtils.isNotEmpty(childOrgIds)) {
                    return String.join(",", childOrgIds);
                }
                return o.getUuid();
            }, (key1, key2) -> key1));
            RedisHelper.addToHash(HistoryRedisKeyEnum.ORG_CHILD_UUID_LIST.of(getDatePrefix()), orgChildMap);
            logger.info("定时维护企业层级关系到redis中结束");
        } catch (Exception e) {
            logger.error("定时维护企业层级关系到redis中异常", e);
        }
    }

    public String getDatePrefix() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return LocalDateTime.now().format(dateFormat);
    }

}
