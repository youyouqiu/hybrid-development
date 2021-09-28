package com.zw.platform.domain.reportManagement;

import com.zw.adas.domain.riskManagement.AlarmSign;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class T809AlarmFileListAck implements Serializable {

    /**
     * 报警信息 ID
     */
    private String infoId;

    /**
     * 附件服务器地址长度
     */
    private Integer serverLength;

    /**
     * 附件服务器地址
     */
    private String server;

    /**
     * 附件访问协议类型 0x01:HTTP 0x02:FTP
     */
    private int serverType;

    /**
     * 附件服务器 FTP 协议端口号
     */
    private Integer port;

    /**
     * 附件服务器用户名长度(川冀标附加服务器无密码和账号默认传"")
     */
    private Integer userNameLength = 0;

    /**
     * 附件服务器用户名(川冀标附加服务器无密码和账号默认传"")
     */
    private String userName = "";

    /**
     * 密码长度(川冀标附加服务器无密码和账号默认传"")
     */
    private Integer passwordLength = 0;

    /**
     * 附件服务器密码(川冀标附加服务器无密码和账号默认传"")
     */
    private String password = "";

    /**
     * 附件数量
     */
    private Integer fileCount;

    /**
     * 附件信息列表
     */
    private List<WarnMsgFileInfo> fileInfos;

    private AlarmSign mediaInfo;
}
