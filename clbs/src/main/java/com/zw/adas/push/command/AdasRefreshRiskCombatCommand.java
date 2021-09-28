package com.zw.adas.push.command;

import com.zw.adas.push.common.AdasSimpMessagingTemplateUtil;
import com.zw.platform.util.common.JsonResultBean;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.LongAdder;

@Data
public class AdasRefreshRiskCombatCommand {

    private static final Logger logger = LogManager.getLogger(AdasRefreshRiskCombatCommand.class);

    private LongAdder riskNumber = new LongAdder();

    private AdasSimpMessagingTemplateUtil adasSimpMessagingTemplateUtil;

    private long refreshTime;

    /**
     * 刷新风控作战页面,重新查询一次数据库
     */
    public void executeRefresh() {
        if (riskNumber.longValue() > 0) {
            riskNumber.reset();
            long l = System.currentTimeMillis();
            logger.info("本次风险距上一次风险刷新时间间隔秒: " + (System.currentTimeMillis() - refreshTime) / 1000);
            refreshTime = l;
            //推送前端的数据为空，前端收到消息后通过调用接口进行刷新
            adasSimpMessagingTemplateUtil.sendAdasRisk(new JsonResultBean(""));
        }
    }
}