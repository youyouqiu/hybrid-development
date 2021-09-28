package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求车辆信息
 * @Author denghuabing
 * @Date 2020/6/12
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class QueryAloneVehicleInfo {

    private String vehicleId;
    /**
     * 车牌号码
     */
    @XmlAttribute(name = "vehicleno")
    private String vehicleNo;
    /**
     * 车牌颜色编码(1:蓝色,2:黄色，3:黑色,4:白色,5:农黄,6:农蓝,7:农绿,9:其他)
     */
    @XmlAttribute(name = "platecolorid")
    private Integer plateColorId;
    /**
     * 设备厂家编号
     */
    @XmlAttribute(name = "devfactorynum")
    private String devFactoryNum;
    /**
     * 设备类型
     */
    @XmlAttribute(name = "devtype")
    private String devType;
    /**
     * 设备编号
     */
    @XmlAttribute(name = "devnum")
    private String devNum;
    /**
     * Sim卡号
     */
    @XmlAttribute(name = "devsimnum")
    private String devSimNum;
    /**
     * 通讯编码(平台唯一通讯标识)
     */
    @XmlAttribute(name = "commid")
    private String commId;
    /**
     * 视频服务器IP
     */
    @XmlAttribute(name = "videohost")
    private String videoHost;
    /**
     * 视频服务器端口
     */
    @XmlAttribute(name = "videoport")
    private String videoPort;
    /**
     * 视频通讯ID
     */
    @XmlAttribute(name = "videocomid")
    private String videoComId;
}
