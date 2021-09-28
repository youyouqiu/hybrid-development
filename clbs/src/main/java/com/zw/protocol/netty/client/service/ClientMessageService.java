package com.zw.protocol.netty.client.service;


import com.zw.protocol.msg.Message;

/**
 * 处理设备上传数据
 */
public interface ClientMessageService {

    /**
     * 通用应答
     */
    void currencyAnswer(Message message) throws Exception;
    
    /**
     * 保存数据透传上报日志
     */
    void saveDataPermeanceLog(Message message) throws Exception;

    /**
     * 处理远程升级透传应答数据
     */
    void handleRemoteUpgradePermeanceData(Message message);

    /**
     * 查询终端参数应答0x0104
     */
    void saveDevieParamAckLog(Message message) throws Exception;
    
    /**
     * 查询音视频属性应答0x1003
     * @author hujun
     */
    void saveVideoParamAckLog(Message message);
    
    /**
     * 终端上传乘客流量0x1005
     * @author hujun
     */
    void saveRiderShipAckLog(Message message) throws Exception;
}
