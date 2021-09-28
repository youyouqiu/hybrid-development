package com.zw.adas.push.RedisKeyExpire.handler;

import com.alibaba.fastjson.JSON;
import com.zw.adas.domain.define.setting.AdasPlatformRemind;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.push.RedisKeyExpire.handler.abstracts.KeyExpireHandlerAbstract;
import com.zw.adas.push.common.AdasSimpMessagingTemplateUtil;
import com.zw.adas.push.common.RiskSessionManager;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.platform.util.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class KeyExpireHandlerImpl extends KeyExpireHandlerAbstract {
    @Autowired
    private AdasSimpMessagingTemplateUtil messageTemplate;

    @Autowired
    private AdasElasticSearchService esService;

    @Override
    public void executePlatformRemind(String expireKey) {
        if (!expireKey.endsWith("_platformRemind")) {
            return;
        }
        String[] data = expireKey.split("_");
        if (data.length != 3) {
            return;
        }
        AdasRiskEventEsBean riskEventEsBean = esService.esGetRiskEventById(data[1]);
        if (riskEventEsBean == null) {
            return;
        }
        AdasRiskEsBean adasRiskEsBean = esService.esGetRiskById(riskEventEsBean.getRiskId());
        if (adasRiskEsBean == null) {
            return;
        }
        if (adasRiskEsBean.getStatus() != 6) {
            final Set<String> sessionIds = RiskSessionManager.INSTANCE.getReminders(riskEventEsBean.getVehicleId());
            if (!sessionIds.isEmpty()) {
                AdasPlatformRemind remind = new AdasPlatformRemind();
                remind.setVehicleId(riskEventEsBean.getVehicleId());
                remind.setBrand(riskEventEsBean.getBrand());
                remind.setWarmTime(riskEventEsBean.getEventTime().getTime());
                remind.setPopupType(0);
                final String msg = JSON.toJSONString(remind);
                for (String sessionId : sessionIds) {
                    messageTemplate.sendToSession(ConstantUtil.WEB_SOCKET_TOPIC_PLATFORM_REMIND, sessionId, msg);
                }
            }
        }
    }

}
