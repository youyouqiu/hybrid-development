package com.zw.ws.entity.common;

import lombok.Data;

import java.io.Serializable;


@Data
public class MileageSensor implements Serializable {
    /**
     * 长度
     */
    private String len;

    /**
     * id
     */
    private String id;

    /**
     * 速度
     */
    private String speed;

    /**
     * 里程
     */
    private String mileage;

    /**
     * 状态 0：正常 1：异常
     */
    private Integer unusual;
}
