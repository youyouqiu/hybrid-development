package com.zw.platform.manager.url;

import com.zw.platform.commons.UrlConvert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 报警联动项目相关url
 * @author create by zhouzongbo on 2020/10/9.
 */
public enum AlarmLinkageUrlEnum implements UrlConvert {
    /**
     * 报警联动参数设置
     */
    ADD_ALARM_LINKAGE("/business/alarm/linkage/param", HttpMethod.PUT),
    ;

    private final String path;
    private final HttpMethod httpMethod;

    AlarmLinkageUrlEnum(String path, HttpMethod httpMethod) {
        this.path = path;
        this.httpMethod = httpMethod;
    }

    private static final Map<String, String> API_URL = new HashMap<>(values().length);

    /**
     * 聚合address + path
     * @param address address
     */
    public static void assembleUrl(String address) {
        final AlarmLinkageUrlEnum[] values = values();
        for (AlarmLinkageUrlEnum value : values) {
            API_URL.put(value.name(), address + value.getPath());
        }
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getUrl() {
        return API_URL.getOrDefault(this.name(), StringUtils.EMPTY);
    }

    @Override
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }
}
