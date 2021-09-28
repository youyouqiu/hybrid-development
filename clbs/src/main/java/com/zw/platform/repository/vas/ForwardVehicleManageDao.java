package com.zw.platform.repository.vas;

import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlForm;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlInfo;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleForm;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleInfo;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleQuery;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilVehicleInfo;
import com.zw.platform.domain.oilsubsidy.subsidyManage.SubsidyManageResp;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 油补下载地址管理
 */
public interface ForwardVehicleManageDao {
    /**
     * 新增
     * @param oilDownloadUrl
     * @return
     */
    int add(OilDownloadUrlForm oilDownloadUrl);

    /**
     * 修改
     * @param oilDownloadUrl
     * @return
     */
    int update(OilDownloadUrlForm oilDownloadUrl);

    /**
     * 更新下载状态
     * @param oilDownloadUrl
     * @return
     */
    int updateDownloadStatus(OilDownloadUrlForm oilDownloadUrl);

    /**
     * 删除
     * @param id
     * @return
     */
    int delete(String id);

    /**
     * 根据对接码组织查询转发地址列表新
     * @return
     */
    List<OilDownloadUrlInfo> queryInfos(@Param("dockingCodeOrgIds") Collection<String> dockingCodeOrgIds);

    /**
     * 根据id查询信息
     * @param id
     * @return
     */
    OilDownloadUrlInfo findById(String id);

    /**
     * 根据id和对接码查询验证是否平台存在该对接码
     * @param id
     * @param dockingCode
     * @return
     */
    String getIdByDockingCode(@Param("id") String id, @Param("dockingCode") String dockingCode);

    /**
     * 根据对接码组织id查询是否有相关的记录
     * @param dockingCode
     * @return
     */
    String getForwardVehicleByDockingCode(String dockingCode);

    /**
     * 根据对接码查询油补转发表的车辆信息
     * @param dockingCode
     * @return
     */
    List<OilForwardVehicleForm> getForwardVehiclesByDockingCode(String dockingCode);

    /**
     * 按照对接码查询下面匹配的车辆id
     * @param dockingCode
     * @return
     */
    List<String> getForwardVehicleIdByDockingCode(String dockingCode);

    /**
     * 按照对接吗删除转发表中的转发车辆信息
     * @param dockingCode
     * @return
     */
    int deleteForwardVehicleByDockingCode(String dockingCode);

    /**
     * 根据对接码组织查询转发地址列表新
     * @return
     */
    List<OilForwardVehicleInfo> queryVehicleInfos(@Param("query") OilForwardVehicleQuery query);

    /**
     * 根据id获取转发车辆信息
     * @param id
     * @return
     */
    OilForwardVehicleForm getOilForwardVehicleById(@Param("id") String id);

    /**
     * 根据id获取转发车辆信息
     * @param ids
     * @return
     */
    List<OilForwardVehicleForm> getOilForwardVehicleByIds(@Param("ids") Collection<String> ids);

    /**
     * 根据车辆id获取转发车辆信息
     * @param vehicleIds 车辆id
     * @return List<OilForwardVehicleForm>
     */
    List<OilForwardVehicleForm> getByVehicleIds(@Param("vehicleIds") Collection<String> vehicleIds);

    /**
     * 验证线路是否被绑定
     * @param lineIds
     * @return
     */
    Set<String> checkBindLineIds(@Param("lineIds") Collection<String> lineIds);

    /**
     * 获取有绑定线路的车的平台的车id
     * @param lineId
     * @return
     */
    List<OilForwardVehicleForm> getBindLineVehicleId(String lineId);

    void bindLine(@Param("id") String id, @Param("lineId") String lineId);

    /**
     * 检车平台是否有对应的车
     * @param orgIds
     * @param brand
     * @param plateColor
     * @return
     */
    List<String> checkVehicle(@Param("orgIds") Collection<String> orgIds, @Param("brand") String brand,
        @Param("plateColor") Integer plateColor);

    /**
     * 匹配成功修改状态
     * @param oilForwardVehicleForms
     */
    void changeSuccessBindStatus(@Param("oilForwardVehicleForms") List<OilForwardVehicleForm> oilForwardVehicleForms,
        @Param("time") Date time, @Param("update_data_username") String updateDataUsername);

    /**
     * 匹配失败修改状态
     * @param oilForwardVehicleForms
     */
    void changeFailBindStatus(@Param("oilForwardVehicleForms") List<OilForwardVehicleForm> oilForwardVehicleForms,
        @Param("time") Date time, @Param("update_data_username") String updateDataUsername);

    /**
     * 查询油补车辆表
     * @param ids
     * @return
     */
    List<OilVehicleInfo> getOilVehicles(@Param("ids") Collection<String> ids);

    /**
     * 查询油补需要的终端信息
     * @param ids
     * @return
     */
    List<OilVehicleInfo> getDevices(@Param("ids") Collection<String> ids);

    /**
     * 查询油补需要的车辆信息
     * @param ids
     * @return
     */
    List<OilVehicleInfo> getVehicles(@Param("ids") Collection<String> ids);

    /**
     * 获取线路信息
     * @param ids
     * @return
     */
    List<OilVehicleInfo> getLines(@Param("ids") Collection<String> ids);

    /**
     * 获取所有绑定"河南油补809-2011"协议的车辆
     * @return 协议配置信息
     */
    List<T809ForwardConfig> getAllBindVehicle();

    /**
     * 删除车辆
     * @param ids
     */
    Boolean deleteVehicleByIds(@Param("ids") Collection<String> ids);

    Set<String> getBindVehicleId(@Param("ids") Collection<String> ids);

    /**
     * 批量新增油补转发表信息
     * @param forwardVehicles
     * @return
     */
    int addForwardVehicles(@Param("forwardVehicles") Collection<OilForwardVehicleForm> forwardVehicles);

    /**
     * 批量更新转发表数据
     * @param forwardVehicles
     * @return
     */
    int updateForwardVehicles(@Param("forwardVehicles") Collection<OilForwardVehicleForm> forwardVehicles);

    /**
     * 查询企业下所有车辆信息
     * @param orgIds
     * @return
     */
    List<OilForwardVehicleForm> getVehicleByOrgIds(@Param("orgIds") Collection<String> orgIds);

    /**
     * 查询企业下转发车辆信息
     * @param orgIds orgIds
     * @return
     */
    List<SubsidyManageResp> getForwardVehicleByOrgIds(@Param("orgIds") Collection<String> orgIds);

    /**
     * 模拟油补平台数据，通过对接码
     */
    List<OilForwardVehicleForm> getOilPlatInfoData(@Param("dockingCode") String dockingCode);

}
