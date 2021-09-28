package com.zw.platform.service.oilsubsidy;

import com.github.pagehelper.Page;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlForm;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlInfo;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlQuery;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleInfo;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleQuery;
import com.zw.platform.util.common.BusinessException;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 车辆转发service
 */
public interface ForwardVehicleManageService {

    /**
     * 新增
     * @param oilDownloadUrl
     * @return
     */
    boolean add(OilDownloadUrlForm oilDownloadUrl, HttpServletRequest request) throws BusinessException;

    /**
     * 修改
     * @param oilDownloadUrl
     * @return
     */
    boolean update(OilDownloadUrlForm oilDownloadUrl, HttpServletRequest request) throws BusinessException;

    /**
     * 删除
     * @param id
     * @return
     */
    boolean delete(String id, HttpServletRequest request);

    /**
     * 根据对接码组织查询转发地址列表新
     * @return
     */
    List<OilDownloadUrlInfo> queryInfos(OilDownloadUrlQuery query);

    /**
     * 根据id查询信息
     * @param id
     * @return
     */
    OilDownloadUrlInfo findById(String id);

    /**
     * @return
     */
    List<Map<String, String>> findOilSubsidyPlat(String orgId);

    /**
     * 是否能够打开转发下载地址页面
     * @param id
     * @return
     */
    boolean canEdit(String id);

    /**
     * 分页查询转发车辆列表
     * @return
     */
    Page<OilForwardVehicleInfo> queryVehicleInfos(OilForwardVehicleQuery query);

    /**
     * 根据id查询转发车辆信息
     * @param id
     * @return
     */
    OilForwardVehicleInfo findVehicleById(String id);

    /**
     * 关联线路
     * @param id
     * @param lineId
     * @return
     */
    Boolean saveBindLine(String id, String lineId, String ipAddress);

    /**
     * 关联车辆
     * @param ids
     * @return
     */
    Boolean saveCheckVehicle(String ids, String ipAddress);

    /**
     * 初始化油补绑定下发需要的参数
     * @param bindInfos
     */
    void initOilBindInfos(List<BindInfo> bindInfos);

    /**
     * 修改了线路信息时候，需要调用该方法进行监控对象信息同步
     * @param vehicleIds
     */
    void send809Message(Collection<String> vehicleIds);

    boolean updateDownloadVehicles(String id, HttpServletRequest request) throws BusinessException;

    /**
     * 删除车辆
     * @param ids
     * @return
     */
    boolean deleteVehicle(String ids, String ipAddress);
}
