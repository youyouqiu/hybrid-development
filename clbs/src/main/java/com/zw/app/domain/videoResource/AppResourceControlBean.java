package com.zw.app.domain.videoResource;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppResourceControlBean extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /* 车辆Id */
    private String vehicleId;

    /* 通道号 */
    private String channlNumer;

    /**
     * 回放方式
     */
    private Integer remote;

    /**
     * 快进快退倍数
     */
    private Integer forwardOrRewind;

    /**
     * 拖动回放时间点（YYMMDDHHmmss）回放控制为5时有效，否则全为0
     */
    private String dragPlaybackTime;
}
