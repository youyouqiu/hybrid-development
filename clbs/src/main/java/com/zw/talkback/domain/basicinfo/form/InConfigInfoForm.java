package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.basic.dto.IntercomDTO;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

/**
 * 对讲信息录入表单对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class InConfigInfoForm extends Config1Form {

    /**
     * 原始机型(index)
     */
    private Long originalModelId;

    /**
     * 原始机型编号
     */
    private String modelId;

    /**
     * 终端号 原始机型+终端号
     */
    private String intercomDeviceId;

    @NotNull(message = "【设备密码】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String devicePassword;

    /**
     * 是否支持文本信息 1:支持 ;0:不支持
     */
    private Integer textEnable = 0;

    /**
     * 是否支持图片消息 1:支持 ;0:不支持
     */
    private Integer imageEnable = 0;

    /**
     * 是否支持离线语音消息 1:支持 ;0:不支持
     */
    private Integer audioEnable = 0;

    /**
     * 对讲对象ID
     */
    private String intercomInfoID;

    private OriginalModelInfo originalModelInfo;

    public IntercomDTO convert() {
        IntercomDTO intercomDTO = new IntercomDTO();
        intercomDTO.setOrgId(this.getGroupid());
        intercomDTO.setOrgName(this.getGroupName());
        intercomDTO.setMonitorType(this.getMonitorType());
        intercomDTO.setSimCardNumber(this.getSims());
        intercomDTO.setSimCardId(this.getSimID());
        intercomDTO.setId(this.getBrandID());
        intercomDTO.setName(this.getBrands());
        if (StringUtils.isNotBlank(this.getCitySelID())) {
            intercomDTO.setGroupId(this.getCitySelID().replace(";", ","));
        }
        intercomDTO.setGroupName(this.getAssignmentName());
        intercomDTO.setOriginalModelId(this.originalModelId);
        intercomDTO.setOriginalModel(this.modelId);
        intercomDTO.setDevicePassword(this.devicePassword);
        intercomDTO.setDeviceNumber(this.getDevices());
        intercomDTO.setPriority(this.getPriority());
        intercomDTO.setTextEnable(this.textEnable);
        intercomDTO.setImageEnable(this.imageEnable);
        intercomDTO.setAudioEnable(this.audioEnable);
        intercomDTO.setDeviceType(ProtocolEnum.T808_2013.getDeviceType());
        intercomDTO.setConfigId(this.getConfigId());
        return intercomDTO;
    }

}
