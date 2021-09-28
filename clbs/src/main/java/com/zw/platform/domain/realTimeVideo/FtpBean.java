package com.zw.platform.domain.realTimeVideo;


import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class FtpBean {

    /* ftp服务器名称 */
    private String ftpName;

    /* ftp主机(IP地址) */
    private String host;

    /* ftp密码 */
    private String password;

    /* ftp端口 */
    private int port;

    /* ftp登录用户名 */
    private String username;

    /* ftp挑战路径 */
    private String path;

}
