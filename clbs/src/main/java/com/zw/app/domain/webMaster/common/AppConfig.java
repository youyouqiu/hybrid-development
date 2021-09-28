package com.zw.app.domain.webMaster.common;

import lombok.Data;

import java.io.Serializable;

/**
 * app配置的返回实体
 * @author lijei
 * @date 2019/9/27 18:49
 */
@Data
public class AppConfig implements Serializable {
    private  String type;
    private  String category;
    private  String name;
}
