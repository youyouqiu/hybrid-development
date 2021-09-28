package com.zw.platform.domain.vas.alram;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/29 9:45
 */
@Data
@NoArgsConstructor
public class AlarmSettingReferentDTO {
    /**
     * 监控对象id
     */
    private String moId;
    /**
     * 名称
     */
    private String name;

    public AlarmSettingReferentDTO(String moId, String name) {
        this.moId = moId;
        this.name = name;
    }
}
