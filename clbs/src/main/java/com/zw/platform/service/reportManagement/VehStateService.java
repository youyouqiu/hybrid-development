package com.zw.platform.service.reportManagement;

import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.domain.reportManagement.VehPrintDTO;
import com.zw.platform.domain.reportManagement.VehStateListDTO;
import com.zw.platform.domain.reportManagement.query.VehStateQuery;

/**
 * @Author: zjc
 * @Description:车辆状态报表servcie
 * @Date: create in 2020/11/12 17:52
 */
public interface VehStateService extends IpAddressService {

    /**
     * 查询一个企业下的车辆状态列表信息
     * @param query
     * @return
     */
    VehStateListDTO getData(VehStateQuery query);

    /**
     * 通过列表的id查询前端打印的需要的超速和疲劳车牌号
     * @param id
     * @return
     */
    VehPrintDTO getPrintInfo(String id);

    /**
     * 导出企业下的全部报表信息
     * @param query
     */
    void export(VehStateQuery query);

    /**
     * 定期删除数据（保留三个月）
     */
    void deleteData();
}
