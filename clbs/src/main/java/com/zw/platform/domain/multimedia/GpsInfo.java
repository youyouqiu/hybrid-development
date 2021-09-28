package com.zw.platform.domain.multimedia;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/4/1.
 */
@Data
public class GpsInfo {
    private Long alarm = 0l;
    private Long status = 0l;
    private Long latitude = 0l;
    private Long longitude = 0l;
    private Integer altitude = 0;
    private Integer speed = 0;
    private Integer direction = 0;
    private String time = "";
}
