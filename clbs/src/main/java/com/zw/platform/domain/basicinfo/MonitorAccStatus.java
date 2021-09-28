package com.zw.platform.domain.basicinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/11/4
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorAccStatus {

    /**
     * 在线状态
     */
    private Integer status;
    private Integer acc;
}
