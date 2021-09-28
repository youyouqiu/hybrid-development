package com.zw.platform.repository.modules;

import com.zw.platform.domain.connectionparamsset_809.AlarmSettingBean;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.PlantParamQuery;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmMapping;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmSetting;
import com.zw.platform.domain.connectionparamsset_809.T809PlantFormCheck;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 809连接参数设置Dao
 *
 * @author hujun
 * @Date 创建时间：2018年2月26日 下午4:51:35
 */
public interface ConnectionParamsSetDao {
    /**
     * 保存809连接参数
     *
     * @param param
     * @return
     * @author hujun
     * @Date 创建时间：2018年2月26日 下午4:55:50
     */
    boolean save809ConnectionParamsSet(PlantParam param);

    /**
     * 修改809连接参数
     *
     * @param param
     * @return
     * @author hujun
     * @Date 创建时间：2018年2月27日 上午9:07:37
     */
    boolean update809ConnectionParamsSet(PlantParam param);

    /**
     * 删除809连接参数
     *
     * @param ids
     * @return
     * @author hujun
     * @Date 创建时间：2018年2月27日 上午9:37:52
     */
    boolean delete809ConnectionParamsSet(List<String> ids);

    /**
     * 查询809连接参数
     *
     * @param plantParamQuery
     * @return
     * @author hujun
     * @Date 创建时间：2018年2月27日 上午11:10:20
     */
    List<PlantParam> get809ConnectionParamsSet(PlantParamQuery plantParamQuery);

    /**
     * 查询809连接参数
     *
     * @param id 809设置ID
     * @return
     */
    List<PlantParam> get809ParamSet(@Param("id") String id);

    /**
     * 根据协议类型获取809链接参数设置
     *
     * @param protocolType 协议类型
     * @return List<PlantParam>
     */
    List<PlantParam> get809ConnectionParamSetsByProtocolType(Integer protocolType);

    /**
     * 根据平台id查询809连接参数
     *
     * @param ids
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月8日 下午2:22:29
     */
    List<PlantParam> get809ConnectionParamsByIds(@Param("ids") Collection<String> ids);

    /**
     * 校验平台名称是否唯一
     *
     * @param platFormName
     * @return
     * @throws Exception
     * @author hujun
     * @Date 创建时间：2018年3月16日 上午11:11:25
     */
    String check809PlatFormSole(String platFormName);

    /**
     * 校验协议类型下是否已经存在平台
     *
     * @param protocolType
     * @return
     * @throws Exception
     * @author hujun
     * @Date 创建时间：2018年3月16日 上午11:11:29
     */
    String check809ProtocolType(String protocolType);

    /**
     * 检验不同协议主链路ip是否重复
     *
     * @param param
     * @return
     */
    String check809ProtocolTypeIp(T809PlantFormCheck param);

    /**
     * 获取809所有录入主从链路ip
     *
     * @param param
     * @return
     * @author hujun
     * @Date 创建时间：2018年4月10日 下午3:47:37
     */
    List<String> get809Ip(T809PlantFormCheck param);

    /**
     * 获取809转发平台唯一id
     *
     * @param param
     * @return
     */
    List<String> getSolePlantParamId(T809PlantFormCheck param);

    /**
     * 获取809转发平台唯一数据
     *
     * @param param
     * @return
     */
    PlantParam getSolePlantParamData(T809PlantFormCheck param);

    List<String> getGroupId(@Param("centerId") String centerId, @Param("ip") String ip);

    /**
     * 根据809转发平台IP、接入码、协议类型查询唯一id
     */
    List<PlantParam> getPlatformFlag(@Param("centerId") String centerId, @Param("ip") String ip);

    PlantParam getPlatformGroupId(@Param("id") String id, @Param("serviceIp") String service,
                                  @Param("centerId") String centerId);

    /**
     * 根据监控对象绑定id查询转发平台IP
     */
    List<PlantParam> getPlatformIpByConfigId(String configId);

    /**
     * 设置809报警映射
     *
     * @param t809AlarmMapping
     * @return
     */
    boolean add809AlarmMapping(List<T809AlarmMapping> t809AlarmMapping);

    /**
     * 根据设置809设置id和协议号删除映射
     *
     * @param t809AlarmSetting
     * @return
     */
    boolean delete809AlarmMapping(T809AlarmSetting t809AlarmSetting);

    /**
     * 根据809settingid和协议号查询是否设置了报警映射
     *
     * @param id
     * @param protocol
     * @return
     */
    Integer get809Mapping(@Param("id") String id, @Param("protocol") Integer protocol);

    /**
     * 设置报警映射后设置表的flag改为1
     *
     * @param t809AlarmSetting
     * @return
     */
    boolean update809AlarmMapping(T809AlarmSetting t809AlarmSetting);

    /**
     * 查询809报警映射
     *
     * @param t809AlarmSetting
     * @return
     */
    List<AlarmSettingBean> get809AlarmMapping(T809AlarmSetting t809AlarmSetting);

    Map<String, Object> getPlatformInfoByConfigId(String id);

    List<Map<String, Object>> getPlatformInfoBySettingId(String id);

    Map<String, String> getT809ConnectionStatusAndGroupIdById(@Param("id") String id);

    String getT809ConnectionStatusByGroupId(@Param("groupId") String groupId);

    /**
     * 根据809连接id查询809连接协议类型
     */
    Integer getConnectionProtocolTypeById(String plateFormId);

    /**
     * 根据809连接id查询所属企业id
     */
    String getConnectionGroupIdById(String plateFormId);

    /**
     * 根据监控对象绑定id查询监控对象绑定的809连接参数
     */
    List<PlantParam> getPlatformInfoByMonitorConfigId(String configId);

    /**
     * 根据809连接id查询809连接完整信息
     */
    PlantParam getConnectionInfoById(String id);

    /**
     * 通过协议类型 得到对应的808_pos、809_pos映射关系
     *
     * @return list
     */
    // List<T809AlarmMapping> get808PosBy809PosAndProtocolType();

    List<T809AlarmMapping> get808PosAnd809PosByProtocolType(@Param("protocolType") Integer protocolType);

    /**
     * 通过808pos得到809pos
     *
     * @return 809POS
     */
    String get809PosBy808Pos(@Param("808Pos") String pos);

    /**
     * @param centerId 接入码
     * @param id       809设置id
     * @return
     */
    List<String> get809IdsByCenterId(@Param("centerId") Integer centerId, @Param("id") String id);

    /**
     * @param platformName 平台名称
     * @param id           809设置id
     * @return
     */
    List<String> get809IdsByPlatformName(@Param("platformName") String platformName, @Param("id") String id);

    /**
     * 根据协议类型查找平台
     *
     * @param type
     * @return
     */
    List<PlantParam> get809ByProtocolType(Integer type);

    List<PlantParam> get809ByProtocolTypeAndOrgId(@Param("protocolType") Integer protocolType,
                                                  @Param("orgIds") List<String> orgIds);

    /**
     * 根据id查询平台的id
     *
     * @param ids
     * @return
     */
    List<PlantParam> get809PlatByIds(@Param("ids") List<String> ids);

    /**
     * 根据id查询name
     */
    List<PlantParam> listPlatformNameByIdIn(Collection<String> ids);
}

