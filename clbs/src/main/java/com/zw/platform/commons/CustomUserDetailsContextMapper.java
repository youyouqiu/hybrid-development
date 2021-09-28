package com.zw.platform.commons;

import com.zw.platform.domain.core.UserLdap;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import javax.naming.Name;
import java.util.Collection;

public class CustomUserDetailsContextMapper implements UserDetailsContextMapper {

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        Name id=ctx.getDn();
        String fullName = ctx.getStringAttribute("cn");
        String mail = ctx.getStringAttribute("mail");
        String mobile = ctx.getStringAttribute("mobile");
        String uid = ctx.getStringAttribute("uid");
        String ou = ctx.getStringAttribute("ou");
        String state = ctx.getStringAttribute("st");
        UserLdap userDetails = new UserLdap(username, "", true, true, true, true, authorities, id, fullName, mail,
                mobile, uid, ou,state);

        return userDetails;
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new IllegalStateException("Only retrieving data from LDAP is currently supported");
    }

}
