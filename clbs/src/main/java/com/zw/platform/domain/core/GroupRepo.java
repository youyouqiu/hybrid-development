package com.zw.platform.domain.core;

import org.springframework.ldap.repository.LdapRepository;
import org.springframework.ldap.repository.Query;

import javax.naming.Name;
import java.util.Collection;

public interface GroupRepo extends LdapRepository<Group>, GroupRepoExtension {
    public final static String USER_GROUP = "ROLE_USER";

    Group findByName(String groupName);

    @Query("(member={0})")
    Collection<Group> findByMember(Name member);
    
    
}
