package com.zw.api.config;

import javax.servlet.http.HttpServletResponse;


public class ResponseUntil {
    /**
     * 解决跨域问题
     *
     * @param response
     */
    public static void setResponseHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Max-Age", "10000");
    }

}
