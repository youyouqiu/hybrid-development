package com.zw.platform.commons.filter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.zw.platform.util.DecryptionUtil;

/**
 * @author Chen Feng
 * @version 1.0 2018/11/7
 */
public class WebAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    @Value("${module.decryption}")
    private boolean enableDecryption;

    public WebAuthenticationProcessingFilter() {
        this("/j_spring_security_check");
    }

    protected WebAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {
        if (Objects.equals(request.getMethod(), "GET")) {
            return new AnonymousAuthenticationToken("key", "anonymous",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String key = (String) request.getSession().getAttribute("loginKey");
        password = decryptPassword(password, key);
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private String decryptPassword(String password, String key) {
        if (!enableDecryption) {
            return password;
        }
        try {
            password = DecryptionUtil.aesDecrypt(password, key);
        } catch (Exception e) {
            password = "";
        }
        return password;
    }
}
