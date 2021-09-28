package com.zw.platform.domain.oilsubsidy.line;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 上下行实体
 * @date 2020/10/920:33
 */
@Data
public class DirectionDTO extends DirectionDO {

    @NotNull(message = "站点数据不能为空")
    private List<String> stationIds;

    private List<DirectionStationDTO> stationDTOList;
}
