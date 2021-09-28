package com.zw.adas.domain.core;

import com.zw.platform.domain.core.UserBean;
import org.springframework.ldap.repository.LdapRepository;

public interface UserRepoAdas extends LdapRepository<UserBean> {
//    List<UserBean> findByFullNameContains(String name);
//
//    List<UserBean> findByUsernameContains(String username);
//
//    /***
//     * 查询用户是否为监管用户
//     * @param username
//     * @param area0
//     * @return
//     */
//    UserBean findUserBeanByUsernameAndDisplayName(String username, String area);
}