package com.zw.platform.util.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/9/21 15:00
 */
@Component
public class EsPrefixConfig {
    @Value("${supervise.es.prefix:#{\"\"}}")
    private String supervisePrefix;

    private String prefix;

    @PostConstruct
    private void initPrefix() {
        if (Objects.equals(supervisePrefix, "default")) {
            prefix = "";
            return;
        }
        if (!StringUtils.isEmpty(supervisePrefix) && !supervisePrefix.contains("_")) {
            prefix = supervisePrefix + "_";
            return;
        }
        prefix = supervisePrefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
