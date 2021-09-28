package com.zw.platform.domain.infoconfig;

import lombok.Data;

/**
 * @author  Tdz
 * @create 2017-10-24 9:15
 **/
@Data
public class TreeList {
    private String deviceType;

    private String simcardNumber;

    private String iconSkin;

    private String assignName;

    private String name;

    private Integer isVideo;

    private String pId;

    private String id;

    private String type;

    private String deviceNumber;

    private Integer plateColor;

    private Boolean isParent;

    /**
     * 别名
     */
    private String aliases;

    private String physicsChannel;

    private String logicChannel;

    private String channelType;

    private String sort;

    private String connectionFlag;

    private String streamType;

    private String mobile;

    private Integer status;

    private Integer acc;
}
