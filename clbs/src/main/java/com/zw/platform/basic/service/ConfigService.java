package com.zw.platform.basic.service;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.ConfigDetailDTO;
import com.zw.platform.basic.dto.ConfigUpdateDTO;
import com.zw.platform.basic.dto.query.BasePageQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.imports.ProgressDetails;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息配置绑定
 * @author zhangjuan
 */
public interface ConfigService {
    /**
     * 监控对象绑定 绑定信息
     * @param configDTO 绑定信息
     * @return 是否绑定成功
     * @throws BusinessException 异常
     */
    boolean add(ConfigDTO configDTO) throws BusinessException;

    /**
     * 监控对象绑定关系更新
     * @param configDTO    绑定信息 信息为最新修改的信息
     * @param oldMonitorId 历史的监控对象名称
     * @return 是否更新成功
     * @throws BusinessException 异常
     */
    boolean update(ConfigDTO configDTO, String oldMonitorId) throws BusinessException;

    /**
     * 根据信息配置解绑信息配置
     * @param configIds 信息配置ID
     * @return 是否解绑成功
     * @throws BusinessException 异常
     */
    Map<String, Object> unbind(Collection<String> configIds) throws BusinessException;

    /**
     * 根据监控对象解绑信息配置
     * @param monitorIds 监控对象ID
     * @return 解绑过程中的中间变量
     * @throws BusinessException 异常
     */

    ConfigUpdateDTO unbindByMonitor(Collection<String> monitorIds) throws BusinessException;

    /**
     * 删除信息配置，包括同步删除监控对象、终端和SIM卡
     * @param configId 信息配置ID
     * @return 是否删除成功
     * @throws BusinessException 异常
     */
    boolean delete(String configId) throws BusinessException;

    /**
     * 获取信息配置详情
     * @param configId 信息配置ID
     * @return 信息配置详情
     * @throws BusinessException 异常
     */
    ConfigDetailDTO getDetailById(String configId) throws BusinessException;

    /**
     * 根据监控对象ID集合获取监控对象绑定信息（从缓存中获取）
     * @param monitorIds  监控对象集合
     * @param filterGroup 分组是否按权限进行过滤
     * @return 监控对象绑定信息
     */
    List<BindDTO> getByMonitorIds(Collection<String> monitorIds, boolean filterGroup);

    /**
     * 获取监控对象绑定信息 （未绑定不返回）
     * @param monitorId 监控对象Id
     * @return 监控对象绑定详情
     */
    BindDTO getByMonitorId(String monitorId);

    /**
     * 获取监控对象绑定信息 （未绑定不返回）
     * @param configId 信息配置ID
     * @return 监控对象绑定详情
     */
    BindDTO getByConfigId(String configId);

    /**
     * 根据关键字（监控对象名称、终端号和sim卡号）查询信息配置
     * @param configQuery 查询条件
     * @return 分页的信息配置
     */
    Page<BindDTO> getByKeyword(BasePageQuery configQuery);

    /**
     * 根据关键字查找用户权限下绑定的监控对象ID的监控Id
     * @param keyword 关键字（监控对象名称） 可为空
     * @param type    monitor 根据监控对象名字查找， all 根据监控对象、sim卡终端查找
     * @return 关键字对应的监控对象ID
     */
    Set<String> getByKeyWord(String keyword, String type);

    /**
     * 根据关键字查找用户权限下绑定的顺序监控对象ID集合
     * @param keyword 关键字（监控对象名称） 可为空
     * @param type    monitor 根据监控对象名字查找， all 根据监控对象、sim卡终端查找
     * @return 关键字对应的监控对象ID
     */
    List<String> getSortListByKeyWord(String keyword, String type);

    /**
     * 根据组织（组织及下级组织下分组下的监控对象）进行分页获取
     * @param orgId       组织ID
     * @param configQuery 分页查询条件
     * @return 分页的信息配置
     */
    Page<BindDTO> getByOrg(String orgId, BasePageQuery configQuery);

    /**
     * 根据分组查询绑定的监控对象
     * @param groupId     分组ID
     * @param configQuery 分页查询条件
     * @return 分页的信息配置
     */
    Page<BindDTO> getByGroup(String groupId, BasePageQuery configQuery);

    /**
     * 分页查询
     * @param query 分页查询条件
     * @return 信息配置列表
     */
    Page<BindDTO> getByPage(ConfigQuery query);

    /**
     * 信息配置导出
     * @param response 响应
     * @param query 查询条件
     * @return 是否导出成功
     * @throws Exception Exception
     */
    boolean export(HttpServletResponse response, ConfigQuery query) throws Exception;

    /**
     * 生成信息配置导入模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 信息配置导入
     * @param file 文件名
     * @return 导入结果
     * @throws Exception Exception
     */
    boolean importExcel(MultipartFile file) throws Exception;

    /**
     * 货运导入
     * @param file     导入文件数据
     * @param progress 导入进度
     * @param request  导入http请求
     * @return 导入结果
     * @throws Exception Exception;
     */
    JsonResultBean importTransport(MultipartFile file, ProgressDetails progress, HttpServletRequest request)
        throws Exception;

    /**
     * 维护信息配置redis缓存
     * @param bindList 信息配置列表
     */
    void addOrUpdateRedis(List<BindDTO> bindList);

    /**
     * 随机生成监控对象的名称
     * @param simNum      终端手机号
     * @param monitorType 监控对象类型
     * @return 随机生成的监控对象名称
     */
    String getRandomMonitorName(String simNum, String monitorType);

    /**
     * 检查输入对象是否已经绑定 brands 监控对象
     * @param inputId     校验对象类型： brands：监控对象 devices：终端号 sims：终端手机号
     * @param inputValue  监控对象名称/终端号/终端手机号
     * @param monitorType 监控对象类型
     * @return false:未绑定 true 已经绑定
     */
    boolean checkIsBound(String inputId, String inputValue, Integer monitorType);

    /**
     * 通过车辆id，获取配置信息
     * @param vid 车辆id
     * @return list l
     */
    List<Map<String, String>> getConfigByVehicle(String vid);

    /**
     * 根据监控对象ID获取信息配置ID
     * @param monitorId 监控对象ID
     * @return configId;
     */
    String getConfigId(String monitorId);

    /**
     * 获取信息配置ID集合
     * @param monitorIds 监控对象Id集合
     * @return 信息配置对应的监控对象Id
     */
    List<String> getConfigIds(Collection<String> monitorIds);

    /**
     * 根据分组/企业id查询监控对象少于100的分组id
     * @param id   节点id
     * @param type 节点类型 1：分组 2：企业
     * @return 校验结果
     */
    Map<String, Object> checkGroupMonitorCount(String id, int type);

    /**
     * 获取从业人员下拉框
     * @param configId 信息配置ID
     * @param keyword  关键字
     */
    List<Map<String, String>> getProfessionalSelect(String configId, String keyword);
}
