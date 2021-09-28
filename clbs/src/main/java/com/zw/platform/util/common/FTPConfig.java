package com.zw.platform.util.common;


import lombok.Data;


/**
 * ftp配置 @author  Tdz
 * @create 2017-10-20 12:47
 **/
@Data
public class FTPConfig {
    private String userName;

    private String passWord;

    private Integer port;

    private String host;

    private String path;
}
