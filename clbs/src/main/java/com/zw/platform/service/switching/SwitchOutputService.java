package com.zw.platform.service.switching;

import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.alram.OutputControl;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

public interface SwitchOutputService {

    JsonResultBean send8500(OutputControl outputControl);

    JsonResultBean sendCloseOutputControl(String vehicleId, String protocolType);

    /**
     * 输出控制列
     * @param query query
     * @return PageGridBean
     * @throws Exception Exception
     */
    PageGridBean pageList(SensorConfigQuery query) throws Exception;
}
