package com.zw.platform.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class GetIpAddr {
    public String getIpAddr(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String ipAddress = " ";
        for (int i = 0; i < cookies.length; i++) {
            if ("ip".equals(cookies[i].getName())) {
                ipAddress = cookies[i].getValue();
            }
        }
        return ipAddress;
    }
}
