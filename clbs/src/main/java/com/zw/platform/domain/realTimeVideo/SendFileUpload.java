package com.zw.platform.domain.realTimeVideo;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


@Data
public class SendFileUpload implements T808MsgBody {

    private String ftpUrl; // FTP 服务器IP地址

    private Integer port; // FTP 服务器服务器端口号

    private String userName; // 用户名

    private String pwd; // 密码

    private String filePath; // 文件上传路径

    private Integer type; // 上传类型(传当前通道号)

    private Integer mediaId = 0; // 默认传0

    private Integer peripheralId = 0; // 外设ID 默认传0

    private byte[] keep; // 保留字段 默认传0

    private long alarm = 0; // 报警标识,默认为0(全0表示不指定是否有报警)

    private Integer mediaType; // 音视频资源类型 0：音视频，1：音频，2：视频，3：视频或音视频 ，4：图片

    private Integer bitstream; // 码流类型0：主码流或子码流 1：主码流 2：子码流

    private Integer storageAddress; // 存储位置 0：主存储器或灾备存储器 1：主存储器 2：灾备存储器

    // 任务执行条件Bit0：WIFI，为1 时表示WIFI下可下载
    // Bit1：LAN，为 1 时表示 LAN 连接时可下载
    // Bit2：3G/4G，为 1 时表 示3G/4G 连接时可下载；
    private Integer duty;

    private Integer channelNumber; // 逻辑通道号

    private String startTime; // 开始时间(yyMMddHHmmss)

    private String endTime; // 结束时间(yyMMddHHmmss)





}
