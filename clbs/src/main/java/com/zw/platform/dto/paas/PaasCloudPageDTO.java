package com.zw.platform.dto.paas;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/10/21 14:02
 */
@Data
public class PaasCloudPageDTO implements Serializable {
    private static final long serialVersionUID = 7631048505744726127L;
    /**
     * 总数量
     */
    private Integer total;
    private Integer totalPage;
    private Integer pageSize;
    private Integer page;
}
