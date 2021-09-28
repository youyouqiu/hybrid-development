package com.zw.platform.dto.paas;

import lombok.Data;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/10/21 14:02
 */
@Data
public class PaasCloudPageDataDTO<T> {
    /**
     * 查询的分页数据
     */
    private List<T> items;

    /**
     * 分页信息 比如total page 等等等等....
     */
    private PaasCloudPageDTO pageInfo;
}
