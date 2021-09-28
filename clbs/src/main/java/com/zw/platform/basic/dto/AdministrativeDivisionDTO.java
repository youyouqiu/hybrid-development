package com.zw.platform.basic.dto;

import lombok.Data;

@Data
public class AdministrativeDivisionDTO {
    private String id;
    /**
     * 省、直辖市
     */
    private String provinceName;
    /**
     * 市、区
     */
    private String cityName;
    /**
     * 县
     */
    private String countyName;
    /**
     * 行政区划代码
     */
    private String divisionsCode;
    /**
     * 查询类型: 1: 省、直辖市， 2：市、区， 3： 县
     */
    private Integer type;

    /**
     * 省域id
     */
    private String provinceId;
    /**
     * 市域id
     */
    private String cityId;

}
