package com.zw.app.domain.activeSecurity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zw.adas.domain.common.AdasRiskType;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.utils.AdasCommonHelper;

import lombok.Data;

@Data
public class RiskEvent implements Serializable {

    private String address;

    private Date eventTime;

    private String riskEvent;

    private String riskType;

    public static List<RiskEvent> getRiskEventResult(List<AdasRiskEventEsBean> riskEventEsBeans,
        AdasCommonHelper adasCommonHelper) {
        List<RiskEvent> result = new ArrayList<>();
        riskEventEsBeans.forEach(riskEventEsBean -> result.add(getRiskEvent(riskEventEsBean, adasCommonHelper)));
        return result;
    }

    private static RiskEvent getRiskEvent(AdasRiskEventEsBean riskEventEsBean, AdasCommonHelper adasCommonHelper) {
        RiskEvent event = new RiskEvent();
        String evenName = adasCommonHelper.geEventName(riskEventEsBean.getEventType() + "");
        event.setRiskEvent(evenName);
        event.setAddress(riskEventEsBean.getAddress());
        event.setEventTime(riskEventEsBean.getEventTime());
        event.setRiskType(AdasRiskType.getAppRiskEventType(riskEventEsBean.getEventType()));
        return event;
    }

}
