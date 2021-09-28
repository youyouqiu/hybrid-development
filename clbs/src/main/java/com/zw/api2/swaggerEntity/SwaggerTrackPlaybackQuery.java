package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class SwaggerTrackPlaybackQuery {
    @ApiParam(value = "东北角经度",required = true)
    private String leftTopLongitude;//东北角经度

    @ApiParam(value = "东北角纬度",required = true)
    private String leftTopLatitude;//东北角纬度

    @ApiParam(value = "西南角经度",required = true)
    private String rightFloorLongitude;//西南角经度

    @ApiParam(value = "西南角纬度",required = true)
    private String rightFloorLatitude;//西南角纬度

    @ApiParam(value = "开始时间(秒数如：1548311492)",required = true)
    private long startTime;//开始时间

    @ApiParam(value = "结束时间(秒数如：1548311492)",required = true)
    private long endTime;//结束时间
}
