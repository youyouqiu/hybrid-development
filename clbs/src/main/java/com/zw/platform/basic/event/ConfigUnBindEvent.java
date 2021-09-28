package com.zw.platform.basic.event;

import com.zw.platform.basic.dto.BindDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

/**
 * 信息配置解绑事件
 * @author zhangjuan
 */
@Getter
public class ConfigUnBindEvent extends ApplicationEvent {
    /**
     * 解绑中间信息
     */
    private Collection<BindDTO> unbindList;

    private String operation;


    /**
     * ******日志添加相关信息*******
     * Id地址
     */
    private String ipAddress;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户所述企业UUID
     */
    private String orgId;

    public ConfigUnBindEvent(Object source, Collection<BindDTO> unbindList, String operation, String ipAddress,
                             String userName, String orgId) {
        super(source);
        this.unbindList = unbindList;
        this.operation = operation;
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.orgId = orgId;
    }

}
