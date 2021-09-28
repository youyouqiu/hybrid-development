package com.zw.platform.domain.oilsubsidy.line;

import com.zw.platform.domain.oilsubsidy.station.StationDTO;
import lombok.Data;

/**
 * @author wanxing
 * @Title: 方向站点station
 * @date 2020/10/1010:26
 */
@Data
public class DirectionStationDTO extends StationDTO {

    private Short stationOrder;

    private Byte directionType;

    private String directionInfoId;
}
