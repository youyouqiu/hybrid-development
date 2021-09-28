package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.IntercomDTO;
import com.zw.platform.basic.dto.imports.IntercomImportDTO;
import com.zw.platform.commons.SystemHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 对讲对象 zw_m_intercom_info
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class IntercomDO extends BaseDO {

    /**
     * 对讲终端号(设备号：由原始机型+7位设备标识组成)
     */
    private String intercomDeviceId;

    /**
     * 组织ID 监控对象所属组织Id，感觉目前没有用，后续使用的时候统一用监控对象的所属组织
     */
    private String orgId;

    /**
     * sim卡ID
     */
    private String simcardId;

    /**
     * 调度平台的USER_ID
     */
    private Long userId;

    /**
     * 对讲终端密码  固定长度 8位
     */
    private String devicePassword;

    /**
     * 优先级 1-5
     */
    private Integer priority;

    /**
     * 客户代码: 默认为1
     */
    private Long customerCode;

    /**
     * 个呼号码，5位号码，开头不为0
     */
    private String number;

    /**
     * 原始机型ID
     */
    private Long originalModelId;

    /**
     * 文本信息: 1:支持 ;0:不支持
     */
    private Integer textEnable;

    /**
     * 是否支持图片消息 1:支持 0:不支持
     */
    private Integer imageEnable;

    /**
     * 是否支持离线语音消息 1:支持 0:不支持
     */
    private Integer audioEnable;

    /**
     * 生成状态: 0: 未生成; 1:生成成功; 2:生成失败
     */
    private Integer status;

    /**
     * 是否录音: 0: 不录音; 1: 录音
     */
    private Integer recordEnable;

    public IntercomDO(IntercomDTO intercomDTO) {
        if (StringUtils.isNotBlank(intercomDTO.getIntercomInfoId())) {
            this.setId(intercomDTO.getIntercomInfoId());
            this.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        } else {
            intercomDTO.setIntercomInfoId(this.getId());
            this.setCreateDataUsername(SystemHelper.getCurrentUsername());
        }
        if (Objects.isNull(intercomDTO.getStatus())) {
            this.status = 0;
        }
        this.intercomDeviceId = intercomDTO.getIntercomDeviceNumber();
        this.orgId = intercomDTO.getOrgId();
        this.simcardId = intercomDTO.getSimCardId();
        this.userId = intercomDTO.getUserId();
        this.devicePassword = intercomDTO.getDevicePassword();
        this.priority = intercomDTO.getPriority();
        this.customerCode = intercomDTO.getCustomerCode();
        this.number = intercomDTO.getCallNumber();
        this.originalModelId = intercomDTO.getOriginalModelId();
        this.textEnable = intercomDTO.getTextEnable();
        this.imageEnable = intercomDTO.getImageEnable();
        this.audioEnable = intercomDTO.getAudioEnable();
        this.recordEnable = intercomDTO.getRecordEnable();
    }

    public IntercomDO(BindDTO bindDTO, IntercomImportDTO importDTO) {
        bindDTO.setIntercomInfoId(this.getId());
        this.setCreateDataUsername(SystemHelper.getCurrentUsername());
        this.status = 0;
        this.intercomDeviceId = bindDTO.getIntercomDeviceNumber();
        this.orgId = bindDTO.getOrgId();
        this.simcardId = bindDTO.getSimCardId();
        this.userId = bindDTO.getUserId();
        this.devicePassword = importDTO.getDevicePassword();
        this.priority = importDTO.getPriority();
        priority = priority == null || priority > 5 || priority < 1 ? 1 : priority;
        this.number = bindDTO.getCallNumber();
        this.textEnable = 1;
        this.imageEnable = 1;
        this.audioEnable = 1;
        this.recordEnable = 0;
    }

}
