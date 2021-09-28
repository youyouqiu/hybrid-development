package com.zw.platform.domain.other.protocol.jl.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @Description: 请求车辆营运状态
 * @Author denghuabing
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class VehicleServiceInfoResp {

    /**
     * 车牌号码
     */
    @XmlAttribute(name = "vehicleno")
    private String vehicleNo;
    /**
     * 车牌颜色编码(1:蓝色,2:黄色,3:黑色 ,4:白色,5:农黄,6:农蓝,7:农绿,9:其他)
     */
    @XmlAttribute(name = "platecolorid")
    private Integer plateColorId;
    @XmlTransient
    private String plateColorStr;
    /**
     * 营运状态编码( 10:营运,21:停运,22:挂失,31:迁出(过户),32:迁出(转籍),33:报废,34:歇业,80:注销,90:其他)
     */
    @XmlAttribute(name = "runstatus")
    private Integer runStatus;
    @XmlTransient
    private String runStatusStr;
    /**
     * 返回时间
     */
    @XmlTransient
    private String returnTimeStr;
}
