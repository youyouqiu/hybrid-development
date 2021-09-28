package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DefaultConfiguration extends BaseEntity {

    private String type;//0车，1人，2物

    private Boolean isFilter;//是否过滤出有参数设置的监控对象

    private Integer defaultSize;//默认配置的最大显示数
}
