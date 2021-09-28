package com.zw.platform.domain.sendTxt;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2018/9/4 16:53
 */
@Data
public class RecordCollectionDataA implements Serializable {

    private static final long serialVersionUID = 4196917374534182880L;

    /**
     * 数据帧标识位 0xAA75
     */
    private Integer tag;

    /**
     * 命令字
     */
    @JSONField(name = "CW")
    private Integer CW;

    /**
     * 数据块长度
     * 命令字(0x00~0x9) 长度为0x00
     * 命令字(0x09~0x15) 长度为0x0E
     */
    private Integer len;

    /**
     * 保留字 默认值0x00
     */
    private Integer keep;

    /**
     * 数据块B:
     */
    private RecordCollectionDataB data;
}
