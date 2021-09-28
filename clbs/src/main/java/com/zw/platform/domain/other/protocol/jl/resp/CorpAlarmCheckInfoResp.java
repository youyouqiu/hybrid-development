package com.zw.platform.domain.other.protocol.jl.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求企业车辆违规情况考核
 * @Author denghuabing
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class CorpAlarmCheckInfoResp {
    /**
     * 企业id
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
     * 企业在线率
     */
    @XmlAttribute(name = "onlinerate")
    private String onlineRate;
    /**
     * 停运车次数量
     */
    @XmlAttribute(name = "stopno")
    private String stopNo;
    /**
     * 保修车次数量
     */
    @XmlAttribute(name = "repairno")
    private String repairNo;
    /**
     * 车辆超员次数
     */
    @XmlAttribute(name = "overloadno")
    private String overLoadNo;
    /**
     * 车辆超员扣分数
     */
    @XmlAttribute(name = "overloadpoints")
    private String overLoadPoints;
    /**
     * 车辆脱线运行次数
     */
    @XmlAttribute(name = "offlineno")
    private String offLineNo;
    /**
     * 车辆脱险运行扣分数
     */
    @XmlAttribute(name = "offlinepoints")
    private String offLinePoints;
    /**
     * 扭动镜头次数
     */
    @XmlAttribute(name = "turnlensno")
    private String turnLensNo;
    /**
     * 扭动镜头扣分数
     */
    @XmlAttribute(name = "turnlenspoints")
    private String turnLensPoints;
    /**
     * 遮挡镜头次数
     */
    @XmlAttribute(name = "shelterlensno")
    private String shelterLensNo;
    /**
     * 遮挡镜头扣分数
     */
    @XmlAttribute(name = "shelterlenspoints")
    private String shelterLensPoints;
    /**
     * 无照片次数
     */
    @XmlAttribute(name = "nophotono")
    private String noPhotoNo;
    /**
     * 无照片扣分数
     */
    @XmlAttribute(name = "nophotopoints")
    private String noPhotoPoints;
    /**
     * 无定位数据次数
     */
    @XmlAttribute(name = "nopositionno")
    private String noPositionNo;
    /**
     * 无定位数据扣分数
     */
    @XmlAttribute(name = "nopositionnopoints")
    private String noPositionNoPoints;
    /**
     * 轨迹异常次数
     */
    @XmlAttribute(name = "contrailexceptionno")
    private String contrailExceptionNo;
    /**
     * 轨迹异常扣分数
     */
    @XmlAttribute(name = "contrailexceptionnopoints")
    private String contrailExceptionNoPoints;

    private String sendTime;
}
