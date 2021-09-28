package com.zw.platform.domain.core;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
public class SendParam implements Serializable {
    private static final long serialVersionUID = 1L;
    private int msgSNACK;
    private String vehicleId;
    private String paramId;
    private String msgId;
    private String sessionId;
}
