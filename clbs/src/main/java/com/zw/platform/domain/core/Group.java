package com.zw.platform.domain.core;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * 
 */
@Entry(objectClasses = { "groupOfNames", "top" }, base = "ou=Groups")
public final class Group {
	@Id
	private Name id;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 1)
	private String name;

	@Attribute(name = "uid")
	private String uid;

	@Attribute(name = "description")
	private String description;

	@Attribute(name = "o")
	private String roleName;

	@Attribute(name = "member")
	private Set<Name> members = new HashSet<Name>();
	
	@Attribute(name = "createTimestamp")
	private String createTimestamp;

	private Integer cid;

	private Integer pid;
	
	private Boolean delFlag;//是否可以删除标识,默认为null

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Integer getCid() {
		return cid;
	}

	public void setCid(Integer cid) {
		this.cid = cid;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Name> getMembers() {
		return members;
	}

	public void addMember(Name newMember) {
		members.add(newMember);
	}

	public void removeMember(Name member) {
		members.remove(member);
	}

	public Name getId() {
		return id;
	}

	public void setId(Name id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public String getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(String createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

    public Boolean getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Boolean delFlag) {
        this.delFlag = delFlag;
    }
}
