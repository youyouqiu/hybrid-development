package com.zw.adas.service.report;

import com.zw.adas.domain.report.paas.VehicleDeviceRunStatusDTO;
import com.zw.adas.domain.report.query.SingleVehicleStateQuery;
import com.zw.adas.domain.report.query.VehicleDeviceStateQuery;
import com.zw.platform.util.common.PageGridBean;

import java.util.Map;
import java.util.Set;

/**
 * 车辆与设备运行状态
 *
 * @author zhangjuan
 */
public interface VehicleDeviceStateService {
    /**
     * 分页获取车辆与设备运行状态列表
     *
     * @param query 分页查询条件
     * @return 车辆与设备运行状态列表
     * @throws Exception 异常
     */
    PageGridBean getList(VehicleDeviceStateQuery query) throws Exception;


    /**
     * 根据车牌号模糊搜索指定企业（车辆所属企业）下绑定的车辆ID
     *
     * @param orgId   车辆所属企业
     * @param keyword 车牌关键字
     * @return 查询出的结果
     */
    Set<String> fuzzyVehicleByBrand(String orgId, String keyword);


    /**
     * 构造pass端查询条件
     *
     * @param query  web端查询条件
     * @param isPage 是否分页
     * @return pass端查询条件
     */
    Map<String, String> getQueryCondition(VehicleDeviceStateQuery query, boolean isPage);


    /**
     * 单条车辆与终端
     *
     * @param query 单条查询条件
     * @return 车辆状态详情
     * @throws Exception 异常
     */
    VehicleDeviceRunStatusDTO getSingleState(SingleVehicleStateQuery query) throws Exception;


}
