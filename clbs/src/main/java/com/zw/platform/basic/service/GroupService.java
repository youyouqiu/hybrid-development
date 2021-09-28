package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.OrganizationGroupDO;
import com.zw.platform.basic.dto.UserGroupDTO;
import com.zw.platform.basic.dto.query.GroupPageQuery;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 分组service
 * @date 2020/10/2614:21
 */
public interface GroupService {

    /**
     * 添加
     * @param groupDO
     * @param organizationGroupDO
     * @return
     */
    boolean add(GroupDO groupDO, OrganizationGroupDO organizationGroupDO);

    /**
     * 修改
     * @param groupDTO
     * @return
     * @throws BusinessException
     */
    boolean update(GroupDTO groupDTO) throws BusinessException;

    /**
     * 删除
     * @param id
     * @return
     */
    String delete(String id);

    /**
     * 模糊查询分页
     * @param query
     * @return
     */
    Page<GroupDTO> getPageByKeyword(GroupPageQuery query);

    /**
     * 通过企业Id进行获取
     * @param orgId
     * @return
     */
    List<GroupDTO> getGroupsByOrgId(String orgId);

    /**
     * 通过分组名称模糊搜索分组
     * @param fuzzyName 模糊的分组名称
     * @return List<GroupDO>
     */
    List<GroupDTO> getListByFuzzyName(String fuzzyName);

    /**
     * 通过企业Id进行获取
     * @param orgIds
     * @return
     */
    List<GroupDTO> getGroupsByOrgIds(Collection<String> orgIds);

    /**
     * 导入
     * @param multiFile
     * @return
     * @throws Exception
     */
    JsonResultBean importGroup(MultipartFile multiFile) throws Exception;

    /**
     * 导出
     * @param title
     * @param type
     * @param response
     * @param assignmentQuery
     * @return
     * @throws IOException
     */
    boolean export(String title, int type, HttpServletResponse response,
                   AssignmentQuery assignmentQuery) throws IOException;

    /**
     * 通过分组ids获取
     * @param ids 分组集合
     * @return
     */
    List<GroupDTO> getGroupsById(Collection<String> ids);

    /**
     * 通过id进行获取
     * @param id
     * @return
     * @throws BusinessException
     */
    GroupDTO getById(String id) throws BusinessException;

    /**
     * 通过id删除分组
     * @param ids
     * @return
     */
    String deleteBatch(Collection<String> ids);

    /**
     * 通过id获取名称
     * @param groupIdList
     * @return
     */
    List<String> getNamesByIds(Collection<String> groupIdList);

    /**
     * 分组批量入库
     * @param groupList 分组列表
     * @return 是否新增分组
     */
    boolean addByBatch(List<GroupDO> groupList);

    /**
     * 分组批量存入Redis
     * @param groupList 分组列表
     */
    void addToRedis(List<GroupDO> groupList);

    /**
     * 获取新增分组默认拥有权限的用户
     * @param newGroupList 新增分组
     * @return 用户和分组列表
     */
    List<UserGroupDTO> getNewGroupOwnUser(List<GroupDO> newGroupList);

    /**
     * 通过企业Id获取分组的id集合
     * @param orgId
     * @return
     */
    List<String> getGroupIdsByOrgId(String orgId);

    /**
     * 判断名称是否重复
     * @param name    分组名称
     * @param orgId   组织Id
     * @param groupId 分组Id
     * @return
     */
    boolean checkNameExist(String name, String orgId, String groupId);

    /**
     * 导出模板
     * @param response
     * @throws IOException
     */
    void exportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 获取当前用户监控对象下数量
     * @param id
     * @return
     */
    int getCurrentUserMonitorCount(String id);

    /**
     * 通过分组Id获取监控对象数量以及分组信息
     * @param id
     * @return
     */
    GroupDTO getMonitorCountById(String id);

    /**
     * 获取企业及下级企业监控对象数量以及分组信息
     * @param orgDn 企业ID
     * @return 企业下的分组和分组的监控数量
     */
    List<GroupDTO> getMonitorCountOrgId(String orgDn);

    /**
     * 组装分组树节点
     * @param groupList        分组列表
     * @param orgList          组织列表
     * @param type             根节点是否可选 single：nocheck=true multiple 不组装 nocheck
     * @param isBigData        分组专用 是否有子节点 true：isParent = true
     * @param needMonitorCount 是否需要分组数量
     * @return 分组数节点
     */
    JSONArray buildTreeNodes(List<GroupDTO> groupList, List<OrganizationLdap> orgList, String type, boolean isBigData,
        boolean needMonitorCount);

    /**
     * 构建分组下监控对象的数量
     * @param groupList        分组详情
     * @param groupMonitorMap  分组ID-分组下监控对象的集合
     * @param onLineMonitorIds 所有在线的监控对象Id  若等于null的话，就是不需要封装在线数
     */
    void getGroupMonitorCount(List<GroupDTO> groupList, Map<String, Set<String>> groupMonitorMap,
        Set<String> onLineMonitorIds);

    /**
     * 根据orgDn获取用户权限下的分组列表
     * @param orgDn 组织ID
     * @return 分组列表
     */
    List<GroupDTO> getUserGroupByOrgDn(String orgDn);
}
