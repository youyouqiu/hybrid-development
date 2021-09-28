package com.zw.platform.service.reportManagement;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.domain.reportManagement.form.VehGeneralInfo;
import com.zw.platform.domain.reportManagement.query.VehGeneralInfoQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/***
 @Author zhengjc
 @Date 2019/4/30 16:58
 @Description 车辆综合信息报表
 @version 1.0
 **/
public interface VehGeneralInfoService {

    /**
     * 获取车辆综合信息报表数据
     *
     * @param query 查询的实体
     * @return
     */
    Page<VehGeneralInfo> listVehGeneralInfo(final VehGeneralInfoQuery query);

    /**
     * 获取车辆综合信息报表数据
     * @return
     */
    List<VehicleTypeDTO> getVehTypes();

    void exportVehicleGeneralInfo(HttpServletResponse response) throws Exception;
}
