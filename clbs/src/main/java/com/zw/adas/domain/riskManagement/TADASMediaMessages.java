package com.zw.adas.domain.riskManagement;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.domain.realTimeVideo.TalkBackRecord;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


/**
 * Created by PengFeng on 2017/10/18  17:33
 */
@Data
public class TADASMediaMessages implements T808MsgBody {
    /**
     * ftp地址
     */
    private String ftpUrl;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * ftp用户名
     */
    private String userName;

    /**
     * ftp密码
     */
    private String pwd;

    /**
     * 消息存在ftp的路径
     */
    private String filePath;

    /**
     * 上传类型
     * 默认值为 0x00，代表通过多媒体 ID 方式上传文件；
     * 值非 0，则按照 1078 协议定义方式上传
     */
    private Integer type = 0x00;

    /**
     * 多媒体Id、对应报警上来的多媒体Id
     */
    private Long mediaId;

    /**
     * 外设ID
     * 外设 ID 定义见表 4-7，文件上传模式字段值为 0 时有效
     */
    private Integer peripheralId;

    /**
     * 保留字段
     */
    private Byte[] keep = {00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00};

    /**
     * 报警标识，
     * 见 JT/T 1078-2016 表 26 的定义；文件上传模式字段值为 0，该字段无效，可填 0
     */
    private Long alarm = 0L;

    /**
     * 音视频资源类型
     * 0：音视频，1：音频，2：视频，3：视频或音视频 ，4：图片
     */
    private Integer mediaType = 2;

    /**
     * 码流类型
     * 0：主码流或子码流，1：主码流，2：子码流
     */
    private Integer bitstream = 0;

    /**
     * 存储位置
     * 0：主存储器或灾备存储器，1：主存储器，2：灾备存储器
     */
    private Integer storageAddress = 0;

    /**
     * 任务执行条件
     * Bit0：WIFI，为1 时表示WIFI下可下载；
     * Bit1：LAN，为 1 时表示 LAN 连接时可下载；
     * Bit2：3G/4G，为 1 时表 示3G/4G 连接时可下载；
     */
    private Integer duty = 7;

    /**
     * 多媒体列表
     */
    private JSONArray mediaInfos;

    /**
     * 报警附加多媒体信息列表总数
     */
    private Integer mediaCount;

    /**
     * 对讲录音
     */
    private TalkBackRecord talkBackRecord;

    //协议类型(1黑标，12川标，13冀标)
    private Integer protocolType;

    //文件服务器TCP端口
    private Integer tcpPort;

    //文件服务器UDP端口
    private Integer udpPort;

    //报警编号（uuid）
    private String alarmNumer;

    //media信息
    private AlarmSign alarmSign;

    //鉴权信息token
    private String token;

    //鉴权信息长度
    private Integer tokenLength;

    //服务器ip地址
    private String serviceIp;

}
