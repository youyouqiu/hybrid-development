package com.zw.platform.domain.sendTxt;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/5/9.
 */
@Data
public class SensorParam {
    private Integer peripheralID;//外设ID
    private Integer peripheralMsgLen;//外设消息长度
    private Integer pollingTime;//轮询间隔
    private Integer dataMsgLen;//外设测量数据的消息长度
}
