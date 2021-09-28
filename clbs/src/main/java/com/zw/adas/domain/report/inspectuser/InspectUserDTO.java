package com.zw.adas.domain.report.inspectuser;

import com.zw.platform.domain.reportManagement.Zw809MessageDO;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author wanxing
 * @Title: 巡检监控人员DTO
 * @date 2020/12/3016:25
 */
@Data
public class InspectUserDTO {

    private String id;
    /**
     * 巡检对象类型
     */
    private Integer objectType;

    @ExcelField(title = "巡检对象类型")
    private String objectTypeStr;
    /**
     * 巡检对象id
     */
    @ExcelField(title = "巡检对象ID")
    private String objectId;

    /**
     * 巡检时间
     */
    @ExcelField(title = "巡检时间")
    private Date inspectTime;

    /**
     * 应答时限
     */
    @ExcelField(title = "应答时限")
    private String answerTime;

    private Date ackTime;

    @ExcelField(title = "应答时间")
    private String ackTimeStr;

    /**
     *应答状态 0：未应答，1：正常应答，2：已过期
     */
    private Integer answerStatus;

    /**
     * 应答状态 未应答, 正常应答，已过期
     */
    @ExcelField(title = "应答状态")
    private String answerStatusStr;

    /**
     * 应答人
     */
    @ExcelField(title = "应答人")
    @NotNull(message = "应答人")
    private String answerUser;

    /**
     * 应答人电话
     */
    @ExcelField(title = "联系电话")
    @NotNull(message = "联系电话不能为空，请到用户管理补全信息")
    private String answerUserTel;

    /**
     * 应答人身份证号码
     */
    @ExcelField(title = "身份证号码")
    @NotNull(message = "联系电话不能为空，请到用户管理补全信息")
    private String answerUserIdentityNumber;

    @ExcelField(title = "社会保险号")
    @NotNull(message = "联系电话不能为空，请到用户管理补全信息")
    private String socialSecurityNumber;
    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 企业Id
     */
    private String orgId;

    /**
     * 平台Id
     */
    private String platformId;

    /**
     * 图片路径
     */
    private String mediaUrl;

    /**
     * 上级平台巡检请求消息源报文序列号
     */
    private Integer sourceMsgSn;

    /**
     * 服务器时间，前端需要
     */
    private String serverTime;


    public static InspectUserDTO copy2DTO(Zw809MessageDO data) {
        InspectUserDTO inspectUserDTO = new InspectUserDTO();
        inspectUserDTO.setId(data.getId());
        inspectUserDTO.setPlatformId(data.getPlatformId());
        inspectUserDTO.setObjectId(data.getObjectId());
        inspectUserDTO.setObjectType(data.getObjectType());
        inspectUserDTO.setOrgId(data.getGroupId());
        inspectUserDTO.setInspectTime(data.getTime());
        inspectUserDTO.setAnswerStatus(data.getResult());
        inspectUserDTO.setExpireTime(data.getExpireTime());
        inspectUserDTO.setMediaUrl(data.getMediaUrl());
        inspectUserDTO.setSourceMsgSn(data.getSourceMsgSn());
        inspectUserDTO.setPlatformId(data.getPlatformId());
        inspectUserDTO.setServerTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (1 == data.getResult()) {
            //正常应答
            inspectUserDTO.setAnswerTime(data.getAnswerTime() != null ? String.valueOf(data.getAnswerTime()) : null);
            inspectUserDTO.setAckTime(data.getAckTime());
            inspectUserDTO.setSocialSecurityNumber(data.getSocialSecurityNumber());
            inspectUserDTO.setAnswerUserIdentityNumber(data.getIdentityNumber());
            inspectUserDTO.setAnswerUser(data.getDealer());
            inspectUserDTO.setAnswerUserTel(data.getDealerTelephone());
        }
        return inspectUserDTO;
    }

    public Zw809MessageDO copy2DO() {
        Zw809MessageDO zw809MessageDO = new Zw809MessageDO();
        zw809MessageDO.setId(this.getId());
        zw809MessageDO.setPlatformId(this.getPlatformId());
        zw809MessageDO.setGroupId(this.getOrgId());
        zw809MessageDO.setType(4);
        zw809MessageDO.setSourceDataType(ConstantUtil.UP_PLATFORM_MSG_INSPECTION_USER_ACK);
        zw809MessageDO.setSourceMsgSn(this.getSourceMsgSn());
        zw809MessageDO.setObjectId(this.getObjectId());
        zw809MessageDO.setObjectType(this.getObjectType());
        zw809MessageDO.setTime(this.getInspectTime());
        zw809MessageDO.setAnswerTime(getAnswerTime() != null ? Long.parseLong(getAnswerTime()) : null);
        zw809MessageDO.setAckTime(new Date());
        zw809MessageDO.setResult(this.getAnswerStatus());
        zw809MessageDO.setDealer(this.getAnswerUser());
        zw809MessageDO.setDealerTelephone(this.getAnswerUserTel());
        zw809MessageDO.setIdentityNumber(this.getAnswerUserIdentityNumber());
        zw809MessageDO.setSocialSecurityNumber(this.getSocialSecurityNumber());
        zw809MessageDO.setMediaUrl(this.getMediaUrl());
        return zw809MessageDO;
    }

    /**
     * 转换属性，不能删除
     * @return
     */
    public String getObjectTypeStr() {
        return switchType(this.objectType);
    }

    public String getAnswerStatusStr() {
        String str = "";
        switch (answerStatus) {
            case 0:
                str = "未应答";
                break;
            case 1:
                str = "正常应答";
                break;
            case 2:
                str = "已过期";
                break;
            default:
                break;
        }
        return str;
    }

    private String switchType(Integer objectType) {
        String str = "";
        if (objectType == null) {
            return str;
        }
        switch (objectType) {
            case 0x00:
                str = "下级平台所属单一平台";
                break;
            case 0x01:
                str = "当前连接的下级平台";
                break;
            case 0x02:
                str = "下级平台所属单一业户";
                break;
            case 0x03:
                str = "下级平台所属所有用户";
                break;
            case 0x04:
                str = "下级平台所属所有平台";
                break;
            case 0x05:
                str = "下级平台所属所有平台和业户";
                break;
            case 0x06:
                str = "下级平台所属所有政府监控平台（含监控端）";
                break;
            case 0x07:
                str = "下级平台所属所有企业监控平台";
                break;
            case 0x08:
                str = "下级平台所属所有经营企业监控平台";
                break;
            case 0x09:
                str = "下级平台所属所有非经营性企业监控平台";
                break;
            default:
                break;
        }
        return str;
    }

    /**
     * 转换属性，不能删除
     * @return
     */
    public String getAckTimeStr() {
        if (ackTime == null) {
            return null;
        }
        return DateUtil.getDateToString(getAckTime(), null);
    }
}
