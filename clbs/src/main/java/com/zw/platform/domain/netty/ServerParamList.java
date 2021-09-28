package com.zw.platform.domain.netty;

import com.zw.protocol.netty.common.ApplicationEntity;
import lombok.Data;

import java.util.List;
import java.util.UUID;


/**
 * Created by LiaoYuecai on 2017/7/5.
 */
@Data
public class ServerParamList {
    private List<ApplicationEntity> list;

    /**
     * 809服务器ID（过检时单播不广播）
     */
    private String serverId809;

    private String accessServerAddress;

    /**
     * 客户端唯一标识
     */
    private String clientId = UUID.randomUUID().toString();
}
