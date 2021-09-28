package com.zw.platform.domain.accessPlatform;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * @author LiaoYuecai
 * @create 2018-01-05 9:56
 * @desc  T808接入平台
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccessPlatform extends BaseFormBean implements Serializable {

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 状态（1开，0关）
     */
    private Integer status;

    /**
     * ip地址
     */
    private String ip;

    /**
     *  类型（0上级平台，1同级平台）
     */
    private Integer type;

}
