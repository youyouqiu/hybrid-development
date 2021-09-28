package com.zw.platform.repository.vas;

import com.zw.platform.domain.generalCargoReport.CargoGroupVehOperateForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 功能描述:普货车辆操作日志
 * @author zhengjc
 * @date 2019/8/29
 * @time 15:37
 */
public interface CargoGroupVehOperateDao {

    void addBatch(@Param("operates") List<CargoGroupVehOperateForm> operates);

    void add(@Param("operate") CargoGroupVehOperateForm operate);

}
