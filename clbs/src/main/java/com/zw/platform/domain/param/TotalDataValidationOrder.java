package com.zw.platform.domain.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 总数据效验命令
 * @author zhouzongbo on 2019/1/30 16:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TotalDataValidationOrder extends SensorRemoteUpgrade implements Serializable {
    private static final long serialVersionUID = -5166030926052657432L;

    /**
     * 总字节数
     */
    private Long allByte;

    /**
     * 累加校验码
     */
    private Long upCheckCode;

    /**
     * 异或校验码
     */
    private Integer xorCheckCode;
}
