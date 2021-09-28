package com.zw.platform.domain.sendTxt;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2018/9/4 17:40
 */
@Data
public class RecordCollectionDataB implements Serializable {

    private static final long serialVersionUID = -7890852822510737181L;

    /**
     * 开始时间 yyMMddHHmmss
     */
    private String startTime;

    /**
     * 结束时间 yyMMddHHmmss
     */
    private String endTime;

    /**
     * 最大单位数据块个数N(高字节0~255)
     */
    private int maxSum = 1;
}
