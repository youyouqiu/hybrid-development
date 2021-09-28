package com.zw.platform.domain.sendTxt;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/5/18.
 */
@Data
public class F3CommunicationParam {
    private Integer sensorID;//外设ID
    private Integer dataLen = 8;//数据长度
    private Integer peripheralAddress;//外设地址
    private Integer baudRate;//波特率
    private Integer evenOddCheck;//奇偶校验
    private Integer keep = 0;//保留项
}
