package com.zw.platform.repository.oilsubsidy;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.oilsubsidy.line.DirectionStationDTO;
import com.zw.platform.domain.oilsubsidy.line.DirectionStationMiddleDO;

/**
 * @author wanxing
 * @Title: 方向，站点中间表
 * @date 2020/10/915:43
 */
public interface DirectionStationMiddleDao extends CrudDao<DirectionStationMiddleDO>  {

    /**
     * 通过方向ID获取站点信息
     * @param directionIds
     * @return
     */
    List<DirectionStationDTO> getStationInfoByDirectionId(@Param("directionIds") List<String> directionIds);
}
