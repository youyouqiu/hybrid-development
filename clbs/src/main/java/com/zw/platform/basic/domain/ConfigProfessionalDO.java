package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * zw_m_config_professionals 绑定的监控对象与从业人员之间的绑定关系中间表
 *
 * @author create by zhangjuan on 2020-10-30.
 */
@Data
@NoArgsConstructor
public class ConfigProfessionalDO {
    private String id;

    /**
     * 从业人员ID
     */
    private String professionalsId;

    /**
     * 信息配置ID
     */
    private String configId;

    private Integer flag;

    private LocalDateTime createDataTime;

    private String createDataUsername;

    private LocalDateTime updateDataTime;

    private String updateDataUsername;

    public ConfigProfessionalDO(String configId, String professionalsId) {
        this.id = UUID.randomUUID().toString();
        this.configId = configId;
        this.professionalsId = professionalsId;
        this.flag = 1;
    }

}
