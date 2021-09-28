package com.zw.platform.domain.sendTxt;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆控制类型/ID集合
 * @author zhouzongbo on 2019/5/30 16:03
 */
@Data
public class VehicleControllerInfo implements Serializable {
    private static final long serialVersionUID = -100336267176436557L;
    private Integer type;

    private Integer sign;
    /**
     * 控制ID
     */
    private Integer id;

    /**
     * 控制参数
     */
    private Object info;
}
