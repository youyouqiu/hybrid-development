package com.zw.platform.commons;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.core.Group;
import com.zw.platform.exception.AccessDeniedAppException;
import com.zw.platform.exception.UserInformationException;
import com.zw.platform.repository.core.ResourceDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.AccountLocker;
import com.zw.platform.util.LocalizedUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * 用户登录校验
 * @author hujun
 */
public class UserPermissionStatusCheck extends LdapAuthenticationProvider {
    private static final Logger log = LogManager.getLogger(UserPermissionStatusCheck.class);
    private static final String DATA_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private AccountLocker accountLocker;

    public UserPermissionStatusCheck(LdapAuthenticator authenticator, LdapAuthoritiesPopulator authoritiesPopulator) {
        super(authenticator, authoritiesPopulator);
    }

    public UserPermissionStatusCheck(LdapAuthenticator authenticator) {
        super(authenticator);
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken authentication) {
        String username = authentication.getName();
        /* 获取用户信息 */
        DirContextOperations userData;
        try {
            userData = super.doAuthentication(authentication);
        } catch (BadCredentialsException e) {
            long currentTime = System.currentTimeMillis();
            int retryCountLeft = accountLocker.retryCountLeft(username, currentTime);
            if (retryCountLeft > 0) {
                throw new BadCredentialsException(
                    LocalizedUtils.message(messageSource, "login.fail.error", retryCountLeft));
            }
            throw new BadCredentialsException(LocalizedUtils.message(messageSource, "login.fail.password"));
        }
        /* 获取账户具体信息 */
        Attributes detailDatas = userData.getAttributes();
        try {
            /* 如果用户为admin则不进行停启用校验 */
            if ("admin".equals(username)) {
                return userData;
            }
            checkUserStatus(detailDatas);

            checkAppPermission(authentication, userData);

            return userData;
        } catch (NamingException | NullPointerException | ParseException e) {
            log.error("用户信息校验异常", e);
            throw new UserInformationException(LocalizedUtils.message(messageSource, "login.stop.exception"));
        }
    }

    private void checkAppPermission(UsernamePasswordAuthenticationToken authentication, DirContextOperations userData)
        throws NamingException {
        /* 若为APP用户登录则需要判断该用户是否有登录APP的权限 */
        Object details = authentication.getDetails();
        JSONObject jsonData = JSONObject.parseObject(JSONArray.toJSONString(details));
        if (jsonData.get("client_id") != null) {
            // 判断是否有登录权限的数量，大于0则有权限
            int appRegisterNumber = 0;
            try {
                // 根据用户id获取该用户的所有角色
                String uid = userData.getDn().toString();
                Name name = LdapUtils.newLdapName(uid + "," + userService.getBaseLdapPath().toString());
                List<Group> userGroup = (List<Group>) userService.findByMember(name);
                // 获取所有角色的id
                List<String> roleIds = new ArrayList<>();
                for (Group group : userGroup) {
                    roleIds.add(group.getId().toString());
                }
                // 根据角色id判断该用户这些角色下是否有APP登录权限
                if (roleIds.size() > 0) {
                    appRegisterNumber = resourceDao.checkAppRegister(roleIds);
                }
            } catch (Exception e) {
                throw new NamingException();
            }
            if (appRegisterNumber <= 0) {
                throw new AccessDeniedAppException("UNAUTHORIZED");
            }
        }
    }

    //
    private void checkUserStatus(Attributes attributes) throws NamingException, ParseException {
        /* 启停状态为1则为启用状态，否则抛出账号失效异常 */
        Attribute stateData = attributes.get("st");
        Object stateDataValue = stateData.get();
        if (Objects.isNull(stateDataValue) || "0".equals(stateDataValue.toString())) {
            throw new DisabledException(LocalizedUtils.message(messageSource, "login.user.stop"));
        }
        /* 判断授权日期是否到期，若到期则抛出账号过期异常 */
        Attribute expirationTime = attributes.get("carLicense");
        if (expirationTime != null) {
            String date = expirationTime.get().toString();
            if (StringUtils.isNotBlank(date) && !"null".equals(date)) {
                long nowTime = Calendar.getInstance().getTimeInMillis();
                long setTime = DateUtils.parseDate(date + " 23:59:59", DATA_FORMAT).getTime();
                if (nowTime > setTime) {
                    throw new AccountExpiredException(LocalizedUtils.message(messageSource, "login.stop.error"));
                }
            }
        }
    }
}
