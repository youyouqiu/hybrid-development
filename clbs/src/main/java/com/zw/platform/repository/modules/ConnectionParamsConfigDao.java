package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.PlantParamConfigInfo;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfigQuery;
import com.zw.platform.domain.connectionparamsset_809.T809PlatFormSubscribe;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 809监控对象转发管理Dao
 *
 * @author hujun
 * @Date 创建时间：2018年3月2日 下午5:14:00
 */
public interface ConnectionParamsConfigDao {
    /**
     * 查询809转发绑定关系数据
     *
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月2日 下午5:37:19
     */
    List<T809ForwardConfig> findConfig(T809ForwardConfigQuery query);

    /**
     * 通过809设置id查询转发车辆信息
     *
     * @param settingId 809设置id
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> findForwardVehiclesBySettingId(String settingId);

    /**
     * 通过协议类型查询转发车辆信息
     *
     * @param protocolType protocolType
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> findForwardVehiclesByProtocolType(Integer protocolType);

    /**
     * 根据绑定关系id查询绑定关系信息
     *
     * @param cids
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月8日 下午3:27:23
     */
    List<T809PlatFormSubscribe> findConfigByConfigUuid(List<String> cids);

    List<T809PlatFormSubscribe> findConfigByConfigId(@Param("configIds") List<String> configIds,
                                                     @Param("protocolType") String protocolType);

    List<T809PlatFormSubscribe> findConfigBySettingId(List<String> cids);

    List<T809PlatFormSubscribe> findConfigBySimNumber(String simNumber);

    /**
     * 保存809转发绑定关系数据
     *
     * @param param
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月5日 上午9:25:52
     */
    boolean addConfig(List<T809ForwardConfig> param);

    /**
     * 删除809转发绑定关系数据
     *
     * @param ids
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月5日 上午9:34:34
     */
    boolean deleteConfig(List<String> ids);

    boolean deleteConfigBySettingId(List<String> ids);

    /**
     * 根据信息配置绑定id批量删除车辆809转发信息
     *
     * @param configIds
     * @param protocolType 这里固定写死，固定位油补协议类型
     * @return
     */
    boolean deleteOilSubsidyConfigByConfigIds(@Param("configIds") Collection<String> configIds,
                                              @Param("protocolType") String protocolType);

    /**
     * 查询指定平台下绑定的所有车辆id
     *
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月5日 下午6:09:32
     */
    Set<String> findVehiclesOfPlatform(String platFormId);

    /**
     * 根据指定平台id查询809转发绑定关系id
     *
     * @param pids
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月8日 下午2:40:47
     */
    String findConfigUuidByPids(List<String> pids);

    List<String> findBind809ConfigMonitorIds();

    /**
     * 根据车辆绑定关系id查找车辆转发绑定关系id
     *
     * @param
     * @return
     * @throws
     * @author hujun
     * @date 创建日期： 2019/1/11 16:56
     */
    List<String> findConfigIdByVConfigIds(List<String> vcids);

    /**
     * 根据车id查询监控对象绑定809平台消息
     */
    List<PlantParam> getConnectionInfoByVehicleId(String vehicleId);

    /**
     * 根据车id查询监控对象绑定809平台消息
     *
     * @param vehicleIds vehicleIds
     * @return List<PlantParam>
     */
    List<PlantParam> getConnectionInfoByVehicleIds(@Param("vehicleIds") Collection<String> vehicleIds);

    /**
     * 查询车辆的绑定的809平台消息
     *
     * @param vehicleIds vehicleIds
     * @return List<PlantParam>
     */
    List<PlantParam> getVehicleConnectionInfoByVehicleIds(@Param("vehicleIds") Collection<String> vehicleIds);

    /**
     * 查询企业下的车辆绑定809平台消息
     *
     * @param groupIds 企业uuid
     * @return List<PlantParam>
     */
    List<PlantParam> getConnectionInfoByGroupId(@Param("groupIds") Collection<String> groupIds);

    /**
     * 查询全部绑定809平台的车辆id
     */
    List<PlantParamConfigInfo> getAllBind809Info();

    /**
     * 根据车辆id获取809转发平台的id
     */
    String getTransPlatIdByVehicleId(@Param("vehicleId") String vehicleId);


    /**
     * 根据监控对象ID和协议类型获取上级平台信息
     *
     * @param monitorIds 监控对象ID
     * @param protocolTypes  协议类型
     * @return 监控对象及上级平台相关信息
     */
    List<T809ForwardConfig> getByMonitorIdAndProtocol(@Param("monitorIds") Collection<String> monitorIds,
                                                      @Param("protocolTypes") Collection<String> protocolTypes);

}
