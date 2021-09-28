package com.zw.platform.domain.other.protocol.jl.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求监控平台考核信息
 * @Author denghuabing
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class PlatformCheckInfoResp {

    /**
     * 平台编号
     */
    @XmlAttribute(name = "platformid")
    private String platformId;
    /**
     * 累计连接时间
     */
    @XmlAttribute(name = "totalconntime")
    private String totalConnTime;
    /**
     * 中断连接次数
     */
    @XmlAttribute(name = "disconnnumber")
    private String disConnNumber;
    /**
     * 累计断开时间
     */
    @XmlAttribute(name = "totaldisconntime")
    private String totalDisConnTime;
    /**
     * 接入车辆数
     */
    @XmlAttribute(name = "onlinevehicleno")
    private String onlineVehicleNo;
    /**
     * 接入车辆占总接入车辆的比例
     */
    @XmlAttribute(name = "onlinevehiclerate")
    private String onlineVehicleRate;
    /**
     * 维修响应时间
     */
    @XmlAttribute(name = "repairrespondtime")
    private String repairRespondTime;
    /**
     * 报修车次数量
     */
    @XmlAttribute(name = "repairvehicleno")
    private String repairVehicleNo;
    /**
     * 报修车次数量占接入车辆的比例
     */
    @XmlAttribute(name = "repairvehiclerate")
    private String repairVehicleRate;
    /**
     * 软件完善响应
     */
    @XmlAttribute(name = "softwarerespondrate")
    private String softwareRespondRate;
    /**
     * 上传数据错误率
     */
    @XmlAttribute(name = "uploaderrorrate")
    private String uploadErrorRate;
    /**
     * 呼叫应答率
     */
    @XmlAttribute(name = "callansweringrate")
    private String callAnsweringRate;
    /**
     * 企业满意度  (这个字段明细接口文档拼错了   满意度： Satisfaction)
     */
    @XmlAttribute(name = "companySatifactiondegree")
    private String companySatisfactionDegree;
    /**
     * 投诉举报
     */
    @XmlAttribute(name = "complaintno")
    private String complaintNo;
    /**
     * 事故倒查时的信息完整性
     */
    @XmlAttribute(name = "accidentssolverate")
    private String accidentsSolveRate;

    private String sendTime;

}
