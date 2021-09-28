package com.zw.adas.service.reportManagement;


import com.zw.adas.domain.riskManagement.form.AdasMediaFlagForm;
import com.zw.adas.domain.riskManagement.form.AdasMediaForm;

import java.util.Set;


/**
 * @author wangying
 */
public interface AdasMediaService {

    /**
     * 批量往 habse 和 es中插入数据
     *
     * @param mediaForms
     * @return
     */
    boolean addMediaHbaseAndEsBatch(Set<AdasMediaForm> mediaForms);

    /**
     * 批量更新risk表的证据标记
     */
    void updateRiskMediaFlagBatch(Set<AdasMediaFlagForm> adasMediaFlagForms);

    /**
     * 批量更新risk_event表的证据标记
     */
    void updateEventMediaFlagBatch(Set<AdasMediaFlagForm> adasMediaFlagForms);

}
