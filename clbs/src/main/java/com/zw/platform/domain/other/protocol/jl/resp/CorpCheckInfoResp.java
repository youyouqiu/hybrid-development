package com.zw.platform.domain.other.protocol.jl.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求企业考核信息
 * @Author denghuabing
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class CorpCheckInfoResp {
    /**
     * 企业数据标识
     */
    @XmlAttribute(name = "corpid")
    private String corpId;
    /**
     * 企业名称
     */
    @XmlAttribute(name = "corpname")
    private String corpName;
    /**
     * 企业经营许可证号
     */
    @XmlAttribute(name = "licencecard")
    private String licenceCard;
    /**
     * 安装率
     */
    @XmlAttribute(name = "installrate")
    private String installRate;
    /**
     * 上线率
     */
    @XmlAttribute(name = "onlinerate")
    private String onlineRate;
    /**
     * 违规率
     */
    @XmlAttribute(name = "alarmrate")
    private String alarmRate;
    /**
     * 报警处置率
     */
    @XmlAttribute(name = "alarmdisposerate")
    private String alarmDisposeRate;
    /**
     * 规定工作完成率
     */
    @XmlAttribute(name = "taskfinishrate")
    private String taskFinishRate;
    /**
     * 搅拌事项完成率
     */
    @XmlAttribute(name = "assignfinishrate")
    private String assignFinishRate;
    /**
     * 呼叫应答率
     */
    @XmlAttribute(name = "callansweringrate")
    private String callAnsweringRate;

    private  String sendTime;
}
