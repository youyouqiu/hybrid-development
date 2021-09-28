package com.zw.platform.commons;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 防止跨站提交攻击, 但是rest服务会失效，需要自定义一个Matcher
 */
public class SecurityCsrfRequestMatcher implements RequestMatcher {
    private List<String> execludeUrls; // 需要排除的url列表
    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|POST|TRACE|OPTIONS)$");

    @Override
    public boolean matches(final HttpServletRequest request) {
        if (execludeUrls != null && execludeUrls.size() > 0) {
            String servletPath = request.getServletPath();
            for (String url : execludeUrls) {
                if (servletPath.contains(url)) {
                    return false;
                }
            }
        }
        return !allowedMethods.matcher(request.getMethod()).matches();
    }

    public List<String> getExecludeUrls() {
        return execludeUrls;
    }

    public void setExecludeUrls(final List<String> execludeUrlsParm) {
        this.execludeUrls = execludeUrlsParm;
    }
}