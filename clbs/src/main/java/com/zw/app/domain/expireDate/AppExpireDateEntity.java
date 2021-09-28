package com.zw.app.domain.expireDate;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/11/25 9:28
 @Description 到期工具类
 @version 1.0
 **/
@Data
public class AppExpireDateEntity {
    /**
     * 对应记录id
     */
    private String id;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 监控对象名称
     */
    private String monitorName;
}
