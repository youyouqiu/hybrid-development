package com.zw.platform.domain.core;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.naming.Name;
import java.io.Serializable;
import java.util.Collection;

@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" }, base = "ou=zwlbs")
public final class UserLdap extends User implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private Name id;
    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 3)
    private String fullName;
    @Attribute(name = "mail")
    private String mail;
    @Attribute(name = "mobile")
    private String mobile;

    @Attribute(name = "uid")
    private String uid;
    @Attribute(name = "ou")
    private String ou;
    @Attribute(name = "st")
    private String state;

    public Name getId() {
        return id;
    }

    public void setId(Name id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UserLdap(String username, String password, boolean enabled, boolean accountNonExpired,
        boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
        Name id, String fullName, String mail, String mobile, String uid, String ou, String state) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.fullName = fullName;
        this.mail = mail;
        this.mobile = mobile;
        this.uid = uid;
        this.ou = ou;
        this.id = id;
        this.state = state;

    }
}
