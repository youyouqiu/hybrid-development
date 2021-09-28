package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: LocationAttachOilExpand.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年9月21日下午2:32:15
 */
@Data
public class LocationAttachOilExpand implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer len;

    /**
     * 累计油耗
     */
    private String allExpend;

    /**
     * 油箱温度
     */
    private String oilTem;

    /**
     * 瞬时油耗
     */
    private String momentExpend;

    /**
     * 累计时间
     */
    private String deltaTime;

    private Integer unusual;

    private Integer important;
}
