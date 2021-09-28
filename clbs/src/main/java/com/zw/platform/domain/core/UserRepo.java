package com.zw.platform.domain.core;

import org.springframework.ldap.repository.LdapRepository;

import java.util.List;

public interface UserRepo extends LdapRepository<UserBean> {
    List<UserBean> findByFullNameContains(String name);
    List<UserBean> findByUsernameContains(String username);
}