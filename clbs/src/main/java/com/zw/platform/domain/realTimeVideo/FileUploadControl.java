package com.zw.platform.domain.realTimeVideo;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


/**
 * 文件上传控制指令下发参数
 */
@Data
public class FileUploadControl implements T808MsgBody {

    private Integer msgSn; // 应答流水号，对应文件上传消息9206的流水号

    private Integer control; // 上传控制 0暂停 1继续 2取消
}
