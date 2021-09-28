package com.zw.platform.dto.protocol;

import com.zw.protocol.msg.t809.body.module.MainModule;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/25 14:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpInfo extends MainModule {
    private static final long serialVersionUID = 5960673081429892857L;
    private byte[] bytes;
}
