package com.zw.platform.service.monitoring;

import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.multimedia.HandleMultiAlarms;
import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

public interface RealTimeRiskService {

    List<ProfessionalsForm> getRiskProfessionalsInfo(String vehicleId)  throws Exception;

    void sendHandleAlarmsAndPhoto(@ModelAttribute("form") OrderForm form) throws Exception;

    void saveCommonHandleAlarms(HandleAlarms handleAlarms) throws Exception;

    JsonResultBean setTreeShow(int aliasesFlag, int showTreeCountFlag);

    /**
     * 批量处理多条报警
     *
     * @param handleMultiAlarms 报警标识、处理方式等
     */
    void batchHandleAlarm(HandleMultiAlarms handleMultiAlarms) throws Exception;
}
