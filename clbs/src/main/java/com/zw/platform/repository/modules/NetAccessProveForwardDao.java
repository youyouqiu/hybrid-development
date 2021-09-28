package com.zw.platform.repository.modules;

import com.zw.platform.domain.netaccessproveforward.NetAccessProveForwardVehicleDo;
import com.zw.platform.dto.netaccessproveforward.NetAccessProveForwardVehicleDto;
import com.zw.platform.dto.netaccessproveforward.NetAccessProveForwardVehicleQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/7/20 9:35
 */
public interface NetAccessProveForwardDao {

    /**
     * 新增入网证明转发
     * @param list list
     * @return boolean
     */
    boolean addNetAccessProveForward(List<NetAccessProveForwardVehicleDo> list);

    /**
     * 删除入网证明转发
     * @param vehicleIds vehicleIds
     */
    void deleteNetAccessProveForward(List<String> vehicleIds);

    /**
     * 获得所有已经添加的
     * @return List<String>
     */
    List<String> getAllVehicleIds();

    /**
     * 获得所有已经添加的
     * @param query query
     * @return List<NetAccessProveForwardVehicleDto>
     */
    List<NetAccessProveForwardVehicleDto> listByPage(@Param("query") NetAccessProveForwardVehicleQuery query);
}
