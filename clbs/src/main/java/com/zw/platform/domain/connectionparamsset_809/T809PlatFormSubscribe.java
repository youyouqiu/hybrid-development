package com.zw.platform.domain.connectionparamsset_809;

import java.util.List;

import com.zw.protocol.msg.t809.T809MsgBody;

import lombok.Data;

@Data
public class T809PlatFormSubscribe implements T809MsgBody {
    private List<String> settingIds;
    private String deviceNumber;
    private String identification;
    private String protocolType;
}
