package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by LiaoYuecai on 2017/4/11.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CommunicationParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String vid;
    private String mainServerAPN;
    private String mainServerCallUserName;
    private String mainServerCallUserPwd;
    private String mainServerAddress;
    private String slaveServerAPN;
    private String slaveServerCallUserName;
    private String slaveServerCallUserPwd;
    private String slaveServerAddress;
    private String backupServerAPN;
    private String backupServerCallUserName;
    private String backupServerCallUserPwd;
    private String backupServerAddress;
    private Integer serverTCPPort;
    private Integer serverUDPPort;
}
