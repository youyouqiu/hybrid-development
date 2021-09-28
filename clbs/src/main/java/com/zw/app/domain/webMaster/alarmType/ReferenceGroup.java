package com.zw.app.domain.webMaster.alarmType;

import lombok.Data;

import java.io.Serializable;

/**
 * 参考组织
 * @author lijie
 * @date 2018/8/28 18:35
 */
@Data
public class ReferenceGroup implements Serializable {
    private String id;//组织id
    private String name;//组织名字
}
