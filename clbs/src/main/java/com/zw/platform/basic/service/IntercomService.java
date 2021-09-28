package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.FriendDTO;
import com.zw.platform.basic.dto.IntercomDTO;
import com.zw.platform.basic.dto.query.IntercomQuery;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 对讲信配置
 * @author zhangjuan
 */
public interface IntercomService {
    /**
     * 添加
     * @param intercom 对讲信息绑定信息
     * @return 是否添加成功
     * @throws Exception 异常
     */
    boolean add(IntercomDTO intercom) throws Exception;

    /**
     * 将定位对象转换成对讲对象
     * @param bindDTO 相关信息
     * @return 是否转换成功
     * @throws Exception 异常
     */
    boolean convertToIntercom(IntercomDTO bindDTO) throws Exception;

    /**
     * 校验对讲对象是否进行了绑定
     * @param bindDTO 绑定信息
     * @return 是否绑定的详情
     */
    Map<String, Object> checkIsBind(IntercomDTO bindDTO);

    /**
     * 校验是否绑定
     * @param inputId     sims 检查SIM卡，devices 检查设备，brands 检查监控对象
     * @param inputValue  卡号/设备号/监控对象
     * @param monitorType 监控对象类型
     * @return 绑定详情
     */
    Map<String, Object> checkIsBind(String inputId, String inputValue, String monitorType);

    /**
     * 根据id获取对讲对象
     * @param configId 信息配置的id
     * @return 对讲信息
     */
    IntercomDTO getDetailByConfigId(String configId);

    /**
     * 根据id获取对讲对象
     * @param configIds 信息配置的集合
     * @return 对讲信息列表
     */
    List<IntercomDTO> getDetailByConfigIds(Collection<String> configIds);

    /**
     * 更新对象绑定信息
     * @param intercom 对讲对象信息
     * @return 是否更新成功
     * @throws Exception 更新异常
     */
    boolean update(IntercomDTO intercom) throws Exception;

    /**
     * 按信息配置ID解绑对讲对象（解绑完后变成定位对象，只解除与对讲相关的绑定）
     * @param configIds 信息配置的id
     * @return 成功解绑条数
     * @throws Exception 异常
     */
    int unbindByConfigId(Collection<String> configIds) throws Exception;

    /**
     * 按监控对象ID解绑对讲对象（解绑完后变成定位对象，只解除与对讲相关的绑定）
     * @param monitorIds 信息配置的id
     * @return 成功解绑条数
     * @throws Exception 异常
     */
    int unbindByMonitorIds(Collection<String> monitorIds) throws Exception;

    /**
     * 获取用户权限下的对讲对象
     * @param keyword 关键字
     * @return 监控对象ID集合
     */
    List<String> getUserOwnIds(String keyword);

    /**
     * 根据关键字分页查询对讲对象
     * @param query 分页查询条件
     * @return 对讲对象列表
     */
    Page<IntercomDTO> getByKeyword(IntercomQuery query);

    /**
     * 对讲信息列表导出
     * @param response 响应
     * @return 是否导出成功
     * @throws Exception Exception
     */
    boolean export(HttpServletResponse response) throws Exception;

    /**
     * 获取信息配置页面录入前端初始化数据
     * @return 初始化信息
     */
    JSONObject getAddPageInitData();

    /**
     * 数据导入模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 对讲信息列表导入
     * @param file 文件
     * @return 导入结果
     * @throws Exception Exception
     */
    JsonResultBean importFile(MultipartFile file) throws Exception;

    /**
     * 批量生成对讲对象
     * @param configIds 信息配置ID集合
     * @return 生成结果
     */
    JsonResultBean addToIntercomPlatform(Collection<String> configIds);

    /**
     * 修改对讲对象的录音状态
     * @param configId     信息配置ID
     * @param recordEnable 是否录音: 0: 不录音; 1: 录音
     * @return 是否修改成功
     * @throws BusinessException BusinessException
     */
    boolean updateRecordStatus(String configId, Integer recordEnable) throws BusinessException;

    /**
     * 获取对讲对象树形结构 包含 组织+群组+对讲对象
     * @param queryType       查询类型 monitor 按监控对象名称 group 按群组名称 org 按组织名称
     * @param type            根节点是否可选 single 可选 multiple或空 不可选（针对组织）
     * @param keyword         模糊搜索关键字 不为空返回全部
     * @param status          生成状态 -1或空 全部(返回全部的监控对象) 1、已经生成的对讲对象 返回第一组的监控对象 0 未生成 第一组监控对象
     * @param isFilterNullOrg 是否过滤没有分组的组织 true 过滤 false 不过滤
     * @return 对讲树形结构
     */
    JSONArray getIntercomBaseTree(String queryType, String type, String keyword, Integer status,
        boolean isFilterNullOrg);

    /**
     * 获取分组下的对讲对象
     * @param groupId 分组ID
     * @return 监控对象数节点
     */
    JSONArray getTreeNodeByGroupId(String groupId);

    /**
     * 获取组织+调度员树形结构
     * @param keyword 用户名关键字
     * @return 调度员树形结构
     */
    JSONArray getDispatcherTree(String keyword);

    /**
     * 获取对讲对象的好友列表
     * @param userId 对讲平台对讲对象的id
     * @return 对讲对象当前的好友列表
     */
    List<FriendDTO> getFriends(Long userId);

    /**
     * 好友添加
     * @param friendJsonArr 好友列表字符串
     * @param monitorName   监控对象名称
     * @param userId        用户ID
     * @return 是否添加成功
     * @throws BusinessException BusinessException
     */
    boolean addFriend(String friendJsonArr, String monitorName, Long userId) throws BusinessException;
}
