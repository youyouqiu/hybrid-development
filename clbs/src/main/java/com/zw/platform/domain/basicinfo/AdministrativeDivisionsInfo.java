package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 行政区划字典表实体
 * @author hujun
 * @date 2019/2/28 9:29
 */
@Data
public class AdministrativeDivisionsInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String provinceName; //省、直辖市
    private String cityName; //市、区
    private String countyName; //县
    private String divisionsCode; //行政区划代码
    private Integer type; //查询类型: 1: 省、直辖市， 2：市、区， 3： 县

    private String provinceId; //省域id
    private String cityId; //市域id
}
