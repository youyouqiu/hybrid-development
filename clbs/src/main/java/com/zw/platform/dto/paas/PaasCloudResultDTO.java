package com.zw.platform.dto.paas;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/10/21 14:03
 */
@Data
public class PaasCloudResultDTO<T> implements Serializable {
    private static final long serialVersionUID = 6128529049974557816L;
    /**
     * 状态码: 正常为10000
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据体
     */
    private T data;
}
