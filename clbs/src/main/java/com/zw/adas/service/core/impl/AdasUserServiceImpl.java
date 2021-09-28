package com.zw.adas.service.core.impl;

import com.zw.adas.domain.core.OrganizationLdapAdas;
import com.zw.adas.domain.core.OrganizationRepoAdas;
import com.zw.adas.service.core.AdasUserService;
import com.zw.platform.commons.DirectoryType;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.GroupRepo;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.UserRepo;
import com.zw.platform.service.redis.RedisAssignService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/***
 @Author gfw
 @Date 2019/5/30 14:34
 @Description AdasUserService实现
 @version 1.0
 **/
public class AdasUserServiceImpl implements AdasUserService, BaseLdapNameAware {

    private LdapName baseLdapPath;
    private final UserRepo userRepo;
    private final OrganizationRepoAdas organizationRepoAdas;
    private final LdapTemplate ldapTemplateAdas;
    private final GroupRepo groupRepo;
    private RedisAssignService redisAssignService;
    private DirectoryType directoryType;

    /**
     * 根据entryDn 查询用户组织
     * @param id
     * @return
     */
    @Override
    public OrganizationLdapAdas getOrgByEntryDN(String id) {
        return null;
    }

    /**
     * Set the base LDAP path specified in the current
     * <code>ApplicationContext</code>.
     * @param baseLdapPath the base path used in the <code>ContextSource</code>
     */
    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    /**
     * 构造方法
     * @param userRepo         userRepo
     * @param groupRepo        groupRepo
     * @param organizationRepo organizationRepo
     * @param ldapTemplate     ldapTemplate
     */
    @Autowired
    public AdasUserServiceImpl(UserRepo userRepo, GroupRepo groupRepo, OrganizationRepoAdas organizationRepo,
        LdapTemplate ldapTemplate) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.organizationRepoAdas = organizationRepo;
        this.ldapTemplateAdas = ldapTemplate;
    }

    @Autowired
    public void setRedisAssignService(RedisAssignService redisAssignService) {
        this.redisAssignService = redisAssignService;
    }

    @Override
    public LdapName getBaseLdapPath() {
        return this.baseLdapPath;
    }

    /**
     * 根据用户id查询其所有的角色列表
     * @param member member
     * @return Collection<Group>
     * @author fanlu
     */
    @Override
    public Collection<Group> findByMember(Name member) {
        return groupRepo.findByMember(member);
    }

    /**
     * 根据用户id查询其所有的角色
     */
    public Collection<Group> findByMember(String userId) {
        LdapName name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        return groupRepo.findByMember(name);
    }

    public void setDirectoryType(DirectoryType directoryType) {
        this.directoryType = directoryType;
    }

    /**
     * 根据uuid查询组织
     * @param uuid uuid
     * @return result
     * @author lijie
     */
    @Override
    public OrganizationLdapAdas getOrgByUuid(String uuid) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "telephoneNumber",
                "upOrganizationCode" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        filter.and(new EqualsFilter("entryUUID", uuid));
        List<OrganizationLdapAdas> orgs =
            ldapTemplateAdas.search(searchBase, filter.encode(), searchCtls, new OrganizationContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * OrganizationContextMapper
     * @author lijie
     * @version 1.0
     */
    static class OrganizationContextMapper implements ContextMapper<OrganizationLdapAdas> {
        @Override
        public OrganizationLdapAdas mapFromContext(Object ctx) throws NamingException {
            DirContextAdapter context = (DirContextAdapter) ctx;
            OrganizationLdapAdas org = new OrganizationLdapAdas();
            org.setOu(context.getStringAttribute("ou"));
            org.setDescription(context.getStringAttribute("description"));
            org.setOrganizationCode(context.getStringAttribute("postalCode"));
            org.setLicense(context.getStringAttribute("p0"));
            org.setRegisterDate(context.getStringAttribute("physicalDeliveryOfficeName"));
            org.setOperation(context.getStringAttribute("businessCategory"));
            org.setAddress(context.getStringAttribute("registeredAddress"));
            org.setPrincipal(context.getStringAttribute("st"));
            org.setPhone(context.getStringAttribute("telephoneNumber"));
            org.setName(context.getStringAttribute("l"));
            org.setUuid(context.getStringAttribute("entryuuid"));
            org.setOperatingState(context.getStringAttribute("operatingState"));
            org.setScopeOfOperation(context.getStringAttribute("scopeOfOperation"));
            org.setProvinceName(context.getStringAttribute("provinceName"));
            org.setIssuingOrgan(context.getStringAttribute("issuingOrgan"));
            org.setCityName(context.getStringAttribute("street"));
            org.setCountyName(context.getStringAttribute("countyName"));
            org.setAreaNumber(context.getStringAttribute("areaNumber"));
            org.setId(context.getDn());
            org.setEntryDN(context.getStringAttribute("entryDN"));
            return org;
        }
    }

    /**
     * 获取用户当前及下级组织列表
     * @param userId userId
     * @return result
     * @author lijie
     */
    @Override
    public List<String> getOrgUuidsByUser(String userId) {
        List<String> userOrgListId = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            // 获取组织id(根据用户id得到用户所在部门)
            int beginIndex = userId.indexOf(",");
            String orgId = userId.substring(beginIndex + 1);
            List<OrganizationLdapAdas> orgs = getOrgChild(orgId);
            // 遍历得到当前用户组织及下级组织id的list
            if (orgs != null && orgs.size() > 0) {
                for (OrganizationLdapAdas org : orgs) {
                    userOrgListId.add(org.getUuid());
                }
            }
        }
        return userOrgListId;
    }

    /**
     * getOrgChild
     * 根据组织id(不是uuid,是entryDN)查询企业与其所有下级企业的信息
     * @param orgId orgId
     * @return List<OrganizationLdap>
     */
    public List<OrganizationLdapAdas> getOrgChild(String orgId) {
        SearchControls searchCtls = new SearchControls();
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8", "p9",
                "upOrganizationCode" };
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnedAtts);
        String searchFilter = "objectClass=organizationalUnit";
        List<OrganizationLdapAdas> list;
        list = ldapTemplateAdas.search(orgId, searchFilter, searchCtls, (ContextMapper<OrganizationLdapAdas>) ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            OrganizationLdapAdas org = new OrganizationLdapAdas();
            String cid = context.getDn().toString();
            org.setId(context.getDn());
            // 有父节点
            setcpId(org, cid);
            org.setOu(context.getStringAttribute("ou"));
            org.setAddress(context.getStringAttribute("description"));
            org.setName(context.getStringAttribute("l"));
            org.setUuid(context.getStringAttribute("entryuuid"));
            org.setOperatingState(context.getStringAttribute("operatingState"));
            org.setScopeOfOperation(context.getStringAttribute("scopeOfOperation"));
            org.setProvinceName(context.getStringAttribute("provinceName"));
            org.setIssuingOrgan(context.getStringAttribute("issuingOrgan"));
            org.setCityName(context.getStringAttribute("street"));
            org.setCountyName(context.getStringAttribute("countyName"));
            org.setAreaNumber(context.getStringAttribute("areaNumber"));
            org.setLicense(context.getStringAttribute("p0")); // 经营许可证
            return org;
        });
        if (list != null && !list.isEmpty()) {
            return list;
        }
        return null;
    }

    private void setcpId(OrganizationLdapAdas obj, String cid) {
        // 有父节点
        if (cid.indexOf(",") > 0) {
            int beginIndex = cid.indexOf(",");
            obj.setCid(cid);
            obj.setPid(cid.substring(beginIndex + 1));
        } else {
            // 根节点
            obj.setCid(cid);
            obj.setPid("");
        }
    }

    /**
     * 获取用户的uuid
     * @param userId userId
     * @return result
     */
    @Override
    public String getUserUuidById(String userId) {
        String uuid = "";
        if (StringUtils.isNotBlank(userId)) {
            UserBean user = getUserByEntryDn(userId);
            if (user != null) {
                uuid = user.getUuid();
            }
        }
        return uuid;
    }

    /**
     * 根据uuid查询用户
     * @param id id
     * @return user
     * @author wangying
     */
    @Override
    public UserBean getUserByEntryDn(String id) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        filter.and(new EqualsFilter("entryDN", id + ",dc=zwlbs,dc=com"));
        List<UserBean> users =
            ldapTemplateAdas.search(searchBase, filter.encode(), searchCtls, new UserContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    /**
     * UserContextMapper
     * @author wangying
     * @version 1.0
     */
    static class UserContextMapper implements ContextMapper<UserBean> {
        @Override
        public UserBean mapFromContext(Object ctx) throws NamingException {
            DirContextAdapter context = (DirContextAdapter) ctx;
            UserBean p = new UserBean();
            p.setFullName(context.getStringAttribute("givenName"));
            p.setUsername(context.getStringAttribute("uid"));
            p.setMail(context.getStringAttribute("mail"));
            p.setMobile(context.getStringAttribute("mobile"));
            p.setId(context.getDn());
            p.setMember(LdapUtils.newLdapName(context.getStringAttribute("entryDN")));
            p.setCreateTimestamp(context.getStringAttribute("createTimestamp"));
            p.setGender(context.getStringAttribute("employeeType"));
            p.setState(context.getStringAttribute("st"));
            p.setAuthorizationDate(context.getStringAttribute("carLicense"));
            p.setUuid(context.getStringAttribute("entryuuid"));
            p.setIdentity(context.getStringAttribute("employeeNumber"));
            p.setIndustry(context.getStringAttribute("businessCategory"));
            p.setDuty(context.getStringAttribute("departmentNumber"));
            p.setAdministrativeOffice(context.getStringAttribute("displayName"));
            return p;
        }
    }

    /**
     * 根据当前用户获取其所属组织uuid
     * @return String result
     * @author lijie
     */
    @Override
    public String getOrgUuidByUser() {
        String uuid = "";
        UserLdap user = SystemHelper.getCurrentUser();
        String userId = user == null ? null : user.getId().toString();
        if (user == null) {
            return null;
        }
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(",");
        String orgId = userId.substring(beginIndex + 1);
        OrganizationLdapAdas org = getOrgByEntryDN(orgId);
        if (org != null) {
            uuid = org.getUuid();
        }
        return uuid;
    }

    /**
     * 查询所有的组织架构
     * @return List<OrganizationLdap>
     * @author wangying
     */
    @Override
    public List<OrganizationLdapAdas> getAllOrganization() {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = "objectClass=organizationalUnit";
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "telephoneNumber", "p7", "p8",
                "p9", "upOrganizationCode" };
        searchCtls.setReturningAttributes(returnedAtts);
        List<OrganizationLdapAdas> list =
            ldapTemplateAdas.search(searchBase, searchFilter, searchCtls, new OrganizationContextMapper());
        List<OrganizationLdapAdas> orgList = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (OrganizationLdapAdas obj : list) {
                // Goups是用户权限
                if ("Groups".equals(obj.getOu())) {
                    orgList.remove(obj);
                    continue;
                }
                Name id = obj.getId();
                String cid = id.toString();
                setcpId(obj, cid);
                orgList.add(obj);
            }
        }
        return orgList;
    }

}
