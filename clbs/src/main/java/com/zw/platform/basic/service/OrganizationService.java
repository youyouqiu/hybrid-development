package com.zw.platform.basic.service;

import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 组织service类
 * @date 2020/9/2511:02
 */
public interface OrganizationService {

    /**
     * 根据用户名和组织部门构建dn
     * @param userName userName
     * @param groupId  groupId
     * @return Name
     * @author fanlu
     */
    Name bindDn(String userName, String groupId);

    /**
     * 获取当前用户的组织树
     * @return
     */
    String getCurrentUserOrgTree();

    /**
     * 根据entryDN查询组织
     * @param id
     * @param useCache
     * @return
     */
    OrganizationLdap getOrgByEntryDn(String id, boolean useCache);

    boolean add(OrganizationLdap organization);

    boolean update(OrganizationLdap newOrg) throws BusinessException;

    boolean insert(OrganizationLdap organization);

    /**
     * 删除
     * @param orgDn orgDn
     * @return 被删除的org
     */
    OrganizationLdap delete(String orgDn);

    /**
     * 模糊查询组织
     * @param keyword
     * @return
     */
    List<OrganizationLdap> fuzzyOrgList(String keyword);

    OrganizationLdap getOrgInfoByName(String name);

    /**
     * 通过组织名称模糊查询，以及传入组织id进行排除
     * @param name
     * @param orgIdSet
     * @return
     */
    List<String> getOrgIdsByOrgName(String name, Set<String> orgIdSet);

    /**
     * 通过uuid 获取用户下的所有组织
     * @param uuid
     * @return
     */
    List<OrganizationLdap> getOrgListByUuid(String uuid);

    /**
     * 查询摸个企业下自子组织的方法
     */
    List<String> getChildOrgIdByUuid(String parentUuid);

    /**
     * 通过组织的dn获取组织下的child的uuid
     * @param orgDn
     * @return
     */
    List<String> getOrgChildUUidList(String orgDn);

    /**
     * 根据关键字模糊查询用户
     * @param searchParam   查询参数
     * @param orgId         orgId
     * @param searchSubTree searchSubTree false当前一级，true递归子级
     * @return List<UserBean>
     */
    List<UserDTO> fuzzyUsersByOrgDn(String searchParam, String orgId, boolean searchSubTree);

    /**
     * 根据entryDN查询组织
     * @param dn dn
     * @return result
     * @author wangying
     */
    OrganizationLdap getOrgByEntryDn(String dn);

    /**
     * 根据当前用户获取其所属组织uuid
     * @return String result
     * @author wangying
     */
    public String getCurrentUserOrgUuid();

    /**
     * 根据当前用户获取其所属组织uuid
     * @param userId
     * @return
     */
    String getOrgUuidByUserId(String userId);

    /**
     * 通过组织ID获取组织下所有子企业
     * @param orgDn 分干啥的
     * @return 大法师
     */

    List<OrganizationLdap> getOrgChildList(String orgDn);

    /**
     * 获取所有的企业
     * @return
     */
    List<OrganizationLdap> getAllOrganization();

    /**
     * 递归获取指定组织的上级组织
     * @param allList    所有组织
     * @param orgDn      指定组织id
     * @param returnList 上级list
     * @author wangying
     */
    void getParentOrgList(List<OrganizationLdap> allList, String orgDn, Collection<OrganizationLdap> returnList);

    /**
     * 根据企业的uuid获取企业的详情信息
     * @param uuid uuid
     * @return ldap实体
     */
    OrganizationLdap getOrganizationByUuid(String uuid);

    /**
     * 通过企业的uuid查询企业名称
     * @param uuid
     * @return
     */
    String getOrgNameByUuid(String uuid);

    /**
     * 通过uuid集合查询
     * @param uuidSet set集合
     * @return map集合
     */
    Map<String, OrganizationLdap> getOrgByUuids(Set<String> uuidSet);

    /**
     * 通过用户DN获取组织的uuid集合
     * @param userDn userDn
     * @return List<String>
     */
    List<String> getOrgUuidsByUser(String userDn);

    /**
     * 通过ou进行搜索
     * @param ou
     * @return
     * @throws BusinessException
     */
    OrganizationLdap getByOu(String ou) throws BusinessException;

    /**
     * 导入企业
     * @param file
     * @param parentDn
     * @return
     * @throws Exception
     */
    Map<String, Object> importOrg(MultipartFile file, String parentDn) throws Exception;

    /**
     * 下载模板
     * @param response
     * @throws Exception
     */
    void generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 通过用户dn获取用户组织及其下属组织
     * @param toString
     * @return
     */
    List<OrganizationLdap> getOrgListByUserDn(String toString);

    /**
     * 获取当前登录用户所属企业及上级的组织集合
     * @return
     */
    List<OrganizationLdap> getSuperiorOrg();

    /**
     * 根据orgDn获取组织的直接上级组织与，直接下级组织
     * @param orgDn orgDn
     * @return 组织列表
     */
    List<OrganizationLdap> getOrgParentAndChild(String orgDn);
}
