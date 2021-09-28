package com.zw.platform.domain.netty;


import lombok.Data;


/**
 * 音视频丢包率 @author  Tdz
 * @create 2018-01-18 11:28
 **/
@Data
public class LossRateInfo {

    private Integer receiveSubSize;// 接收数据包总数

    private Integer deviceSubSize;// 设备上报数据包序号

    private Long upTime;// 上传时间

}
