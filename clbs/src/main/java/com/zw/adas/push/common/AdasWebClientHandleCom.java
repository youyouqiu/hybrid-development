package com.zw.adas.push.common;

import com.zw.platform.util.ConstantUtil;
import com.zw.protocol.msg.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by lijie on 2019/6/11
 */
@Component
public class AdasWebClientHandleCom {
    private static final Logger log = LogManager.getLogger(AdasWebClientHandleCom.class);

    @Autowired
    AdasNettyHandleComImpl adasNettyHandleCom;

    public void handle(Message message) {
        try {
            if (message.getDesc() == null) {
                return;
            }
            Integer msgID = message.getDesc().getMsgID();
            switch (msgID) {
                case ConstantUtil.T808_RSP_MEDIA_STORAGE_FILE_9212:
                    adasNettyHandleCom.deal9212Message(message);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("adas媒体上传协议解析数据出错", e);
        }
    }
}
