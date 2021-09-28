package com.zw.app.domain.update;

import lombok.Data;

/***
 @Author gfw
 @Date 2018/12/11 14:03
 @Description APP版本数据库实体
 @version 1.0
 **/
@Data
public class UpdateInfo {
    private String id;
    private String updateMessage;
    private String platform;
    private String appUrl;
    private Integer version;
}
