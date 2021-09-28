package com.zw.platform.commons.filter;

import com.alibaba.fastjson.JSON;
import com.zw.api.config.ResponseUntil;
import com.zw.app.exception.MyOAuthException;
import com.zw.app.exception.MyOauthExceptionHandle;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.BadClientCredentialsException;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A filter and authentication endpoint for the OAuth2 Token Endpoint. Allows clients to authenticate using request
 * parameters if included as a security filter, as permitted by the specification (but not recommended). It is
 * recommended by the specification that you permit HTTP basic authentication for clients, and not use this filter at
 * all.
 * @author Dave Syer
 */
public class MyToken extends AbstractAuthenticationProcessingFilter {
    private AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
    private MyOauthExceptionHandle authExceptionHandle = new MyOauthExceptionHandle();
    private boolean allowOnlyPost = false;

    public MyToken() {
        this("/oauth/token");
    }

    private Integer count = 0;

    public MyToken(String path) {
        super(path);
        setRequiresAuthenticationRequestMatcher(new ClientCredentialsRequestMatcher(path));
        // If authentication fails the type is "Form"
        ((OAuth2AuthenticationEntryPoint) authenticationEntryPoint).setTypeName("Form");
    }

    public void setAllowOnlyPost(boolean allowOnlyPost) {
        this.allowOnlyPost = allowOnlyPost;
    }

    /**
     * @param authenticationEntryPoint the authentication entry point to set
     */
    public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        setAuthenticationFailureHandler((request, response, exception) -> {
            if (exception instanceof MyOAuthException) {
                authExceptionHandle.commence(request, response, exception);
                return;
            }
            if (exception instanceof BadCredentialsException) {
                exception = new BadCredentialsException(exception.getMessage(), new BadClientCredentialsException());
            }
            authenticationEntryPoint.commence(request, response, exception);
        });
        setAuthenticationSuccessHandler((request, response, authentication) -> {
            // no-op - just allow filter chain to continue to token endpoint
        });
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {

        if (allowOnlyPost && !"POST".equalsIgnoreCase(request.getMethod())) {
            throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] { "POST" });
        }

        String clientId = request.getParameter("client_id");
        String clientSecret = request.getParameter("client_secret");

        // If the request is already authenticated we can assume that this
        // filter is not needed
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication;
        }

        if (clientId == null) {
            throw new BadCredentialsException("No client credentials presented");
        }

        checkSmCode(request);
        if (clientSecret == null) {
            clientSecret = "";
        }

        clientId = clientId.trim();
        UsernamePasswordAuthenticationToken authRequest =
            new UsernamePasswordAuthenticationToken(clientId, clientSecret);
        ResponseUntil.setResponseHeader(response);
        return this.getAuthenticationManager().authenticate(authRequest);

    }

    private void checkSmCode(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keys = parameterMap.keySet();
        if (keys.contains("verificationCode")) {
            String[] verificationCodes = parameterMap.get("verificationCode");
            String[] usernames = parameterMap.get("username");
            String verificationCode = verificationCodes[0];
            String regex = "^\\d{6}$";
            Pattern compile = Pattern.compile(regex);
            boolean matches = compile.matcher(verificationCode).matches();
            String code = RedisHelper.getString(HistoryRedisKeyEnum.USERNAME_CODE.of(usernames[0]));
            String codeCount = RedisHelper.getString(HistoryRedisKeyEnum.USERNAME_CODE_COUNT.of(usernames[0]));
            if (codeCount == null) {
                count = 0;
            } else {
                count = Integer.parseInt(codeCount);
            }
            //1.验证码格式不符
            //2.验证码过期/失效
            //3.验证码错误次数超限 3
            //4.验证码不匹配
            if (!matches) {
                AppResultBean appBean = new AppResultBean(101, "验证码格式不符");
                throw new MyOAuthException(JSON.toJSONString(appBean));
            }
            if (code == null) {
                AppResultBean appBean = new AppResultBean(102, "验证码过期/失效");
                throw new MyOAuthException(JSON.toJSONString(appBean));
            }
            if (count >= 3) {
                AppResultBean appBean = new AppResultBean(103, "验证码次数超限,请重新获取");
                RedisHelper.delete(HistoryRedisKeyEnum.USERNAME_CODE.of(usernames[0]));
                RedisHelper.delete(HistoryRedisKeyEnum.USERNAME_CODE_COUNT.of(usernames[0]));
                throw new MyOAuthException(JSON.toJSONString(appBean));
            }
            if (!code.equals(verificationCode)) {
                count++;
                RedisHelper.setString(HistoryRedisKeyEnum.USERNAME_CODE_COUNT.of(usernames[0]), count + "", 60);
                AppResultBean appBean = new AppResultBean(104, "验证码不匹配");
                throw new MyOAuthException(JSON.toJSONString(appBean));
            }
            count = 0;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
        Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }

    protected static class ClientCredentialsRequestMatcher implements RequestMatcher {

        private String path;

        public ClientCredentialsRequestMatcher(String path) {
            this.path = path;

        }

        @Override
        public boolean matches(HttpServletRequest request) {
            String uri = request.getRequestURI();
            int pathParamIndex = uri.indexOf(';');

            if (pathParamIndex > 0) {
                // strip everything after the first semi-colon
                uri = uri.substring(0, pathParamIndex);
            }

            String clientId = request.getParameter("client_id");

            if (clientId == null) {
                // Give basic auth a chance to work instead (it's preferred anyway)
                return false;
            }

            if ("".equals(request.getContextPath())) {
                return uri.endsWith(path);
            }

            return uri.endsWith(request.getContextPath() + path);
        }

    }

}

