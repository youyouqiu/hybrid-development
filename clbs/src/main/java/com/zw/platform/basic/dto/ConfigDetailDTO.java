package com.zw.platform.basic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 信息配置详情
 *
 * @author zhangjuan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDetailDTO<D extends BindDTO> {
    /**
     * 监控对象信息
     */
    private D monitor;

    /**
     * 群组信息
     */
    private List<GroupDTO> groupList;

    /**
     * 从业人员信息
     */
    private List<ProfessionalDTO> professionalList;


    /**
     * 终端信息
     */
    private DeviceDTO device;

    /**
     * SIM卡详情
     */
    private SimCardDTO simCard;
}
