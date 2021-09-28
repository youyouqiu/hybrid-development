package com.zw.platform.domain.forwardplatform;

import lombok.Data;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/7/26.
 */
@Data
public class ThirdPlatFormSubscribe {
    private String platId;
    private List<String> deviceNumbers;
    private List<String> identifications ;
}
