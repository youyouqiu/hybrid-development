package com.zw.platform.basic.service;

import com.zw.platform.util.IPAddrUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wanxing
 * @Title: 公共基础操作类
 * @date 2020/9/2511:14
 */
public interface IpAddressService {

    /**
     * 获取request请求
     * @return
     */
    default HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获取response请求
     * @return
     */
    default HttpServletResponse getHttpServletResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    /**
     * 获取ip地址
     * @return
     */
    default String getIpAddress() {
        HttpServletRequest request = getHttpServletRequest();
        return IPAddrUtil.getClientIp(request);
    }

    default HttpServletResponse getResponse() {
        return getHttpServletResponse();
    }
}
