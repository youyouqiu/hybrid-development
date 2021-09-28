package com.zw.platform.service.core;

import com.zw.platform.domain.core.SendParam;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.DirectiveStatusEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by FanLu on 2017/6/2
 */
@Service
public class MultiThreadProcessService {
    public static final Logger logger = LogManager.getLogger(MultiThreadProcessService.class);

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private DelayedEventTrigger trigger;

    /**
     * 更新参数下发表状态，如果3分钟设备没有回应，将状态设为1，即指令未生效
     */
    public void updateSendParam(SendParam sendParam) {
        logger.debug("MultiThreadProcessService-processSomething" + Thread.currentThread() + "......start");
        trigger.addEvent(3, TimeUnit.MINUTES, () -> parameterDao
            .updateStatusByMsgSN(sendParam.getMsgSNACK(), sendParam.getVehicleId(),
                DirectiveStatusEnum.IS_NOT_EFFECTED.getNum()));
        logger.debug("MultiThreadProcessService-processSomething" + Thread.currentThread() + "......end");
    }
}
