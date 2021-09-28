package com.zw.platform.commons;

import org.springframework.http.HttpMethod;

/**
 * @author create by zhouzongbo on 2020/6/18.
 */
public interface UrlConvert {
    /**
     * path
     * @return path
     */
    String getPath();

    /**
     * url
     * @return url
     */
    String getUrl();

    /**
     * HttpMethod
     * @return HttpMethod
     */
    default HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
