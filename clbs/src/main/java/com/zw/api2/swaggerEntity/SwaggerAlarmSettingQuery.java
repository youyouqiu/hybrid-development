package com.zw.api2.swaggerEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 @Author gfw
 @Date 2019/1/30 14:28
 @Description 分页查询报警参数列表
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class SwaggerAlarmSettingQuery {

    /**
     * 页数
     */
    private Long page;
    /**
     * 每页显示条数
     */
    private Long limit;
    /**
     * 查询条件
     */
    private String simpleQueryParam;
    /**
     * 组织id
     */
    private String groupId;
    /**
     * 所属分组
     */
    private String assignmentId;
    /**
     * 设备类型
     */
    private String deviceType;
}
