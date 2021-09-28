package com.zw.platform.commons;

import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.util.common.ConstantSystemValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;


/**
 * 系统帮助工具类
 */
public final class SystemHelper {
    private static final Logger log = LogManager.getLogger(SystemHelper.class);

    private static final String INFO_INPUT = "/m/infoconfig/infoFastInput/add";// 快速录入

    private static final String INFO_CONFIG = "/m/infoconfig/infoinput/list";// 信息录入

    private SystemHelper() {
        throw new Error("工具类不能实例化！");
    }

    /**
     * 退出系统并清空session
     */
    public static void logout() {
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * 得到request
     */
    public static HttpServletRequest getRequest() {
        HttpServletRequest request = null;
        if (RequestContextHolder.getRequestAttributes() != null) {
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }
        return request;
    }

    /**
     * 得到session
     */
    public static HttpSession getSession() {
        HttpSession session = null;
        HttpServletRequest request = getRequest();
        if (request != null) {
            session = request.getSession(true);
        }
        return session;
    }

    /**
     * 保存信息到session
     */
    private static void setSessionAttribute(final String key, final Object value) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(key, value);
        }
    }

    /**
     * 从session获取属性
     */
    private static Object getSessionAttribute(final String key) {
        Object resutlt = null;
        HttpSession session = getSession();
        if (session != null) {
            resutlt = session.getAttribute(key);
        }
        return resutlt;
    }

    /**
     * 获取getCreationTime
     */
    public static long getCreationTime() {
        long resutlt = 0L;
        HttpSession session = getSession();
        if (session != null) {
            resutlt = session.getCreationTime();
        }
        return resutlt;
    }

    /**
     * 获取getLastAccessedTime
     */
    public static long getLastAccessedTime() {
        long resutlt = 0L;
        HttpSession session = getSession();
        if (session != null) {
            resutlt = session.getLastAccessedTime();
        }
        return resutlt;
    }

    /**
     * 检查用户是已认证
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        } else {
            return false;
        }
    }

    /**
     * 得到当前用户
     */
    public static UserLdap getCurrentUser() {
        UserLdap user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            user = (UserLdap) auth.getPrincipal();
        }
        return user;
    }

    /**
     * 保存当前用户
     */
    public static void setCurrentUser(final UserLdap user) {
        setSessionAttribute(ConstantSystemValues.CURRENT_USER, user);
    }

    /**
     * 得到当前用户id
     */
    public static String getCurrentUserId() {
        String userId = null;
        UserLdap user = getCurrentUser();
        if (user != null) {
            userId = user.getUsername();
        }
        return userId;
    }

    /**
     * 得到当前用户id
     */
    public static String getCurrentUId() {
        String userId = null;
        UserLdap user = getCurrentUser();
        if (user != null) {
            userId = user.getId().toString();
        }
        return userId;
    }

    /**
     * 得到当前用户id
     */
    public static String getCurrentUserDn() {
        String userId = null;
        UserLdap user = getCurrentUser();
        if (user != null) {
            userId = user.getId().toString();
        }
        return userId;
    }

    /**
     * 获得认证信息
     */
    public static Authentication getAuthentication() {
        Authentication authentication = null;
        SecurityContextImpl securityContextImpl = (SecurityContextImpl) getSessionAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContextImpl != null) {
            authentication = securityContextImpl.getAuthentication();
        }
        return authentication;
    }

    /**
     * 检查Permission
     */
    static boolean checkPermission(final String permission) {
        List<String> userPermissions = new ArrayList<>();
        userPermissions.add("user");
        userPermissions.add("role");
        log.debug("Permissions: {}", permission);
        return userPermissions.contains(permission);
    }

    /**
     * 是否超级管理员
     */
    public static boolean isAdmin() {
        return ConstantSystemValues.ADMIN_USER.equals(getCurrentUsername());
    }

    /**
     * 得到当前用户用户名
     */
    public static String getCurrentUsername() {
        String username = null;
        UserLdap user = getCurrentUser();
        if (user != null) {
            username = user.getUsername();
        }
        return username;
    }

    /**
     * 得到当前用户真实姓名
     */
    public static String getCurrentUserRealname() {
        String userRealName = null;
        UserLdap user = getCurrentUser();
        if (user != null) {
            userRealName = user.getFullName();
        }
        return userRealName;
    }

    /**
     * 用户是否具有编辑资源的权限,从session里面获取资源并校验
     *
     * @return boolean
     * @author FanLu
     */
    public static boolean checkPermissionEditable() {
        HttpServletRequest request = getRequest();
        if (request == null || request.getRequestURI() == null) {
            return false;
        }
        String url = request.getRequestURI();
        return checkPermissionEditable(url);
    }

    @SuppressWarnings({"unchecked"})
    public static boolean checkPermissionEditable(String url) {
        HttpSession session = getSession();
        if (session == null || session.getAttribute("permissions") == null) {
            return false;
        }
        List<String> menuUrls = (List<String>) (session.getAttribute("permissions"));
        url = url.substring(url.indexOf('/', url.indexOf('/') + 1));
        if (INFO_INPUT.equals(url)) {
            url = INFO_CONFIG;
        }
        return menuUrls.contains(url);
    }
}
