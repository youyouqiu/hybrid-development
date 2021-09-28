package com.zw.platform.domain.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 升级数据命令
 * @author zhouzongbo on 2019/1/30 16:11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UpgradeDataCommand extends SensorRemoteUpgrade implements Serializable {
    private static final long serialVersionUID = -2280663682575924970L;

    /**
     * 总包数
     */
    private Integer allPage;

    /**
     * 序号
     */
    private Integer sum;

    /**
     * 数据长度
     */
    private Integer dataLen;

    /**
     * 数据区: 最长 990 个字节，最后不足算作一包
     */
    private byte[] data;
}
