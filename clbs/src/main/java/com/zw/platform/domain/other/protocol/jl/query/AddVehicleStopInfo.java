package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 上传停运车辆
 * @Author denghuabing
 * @Date 2020/6/12
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class AddVehicleStopInfo {

    private String vehicleId;

    /**
     * 车牌号码.
     */
    @XmlAttribute(name = "vehicleno")
    private String vehicleNo;
    /**
     * 车牌颜色编码(1:蓝色,2:黄色,3:黑色,4:白色,5:农黄,6:农蓝,7:农绿,9:其他)
     */
    @XmlAttribute(name = "platecolorid")
    private Integer plateColorId;
    /**
     * 报停原因(1:天气原因,2:车辆故障,3:路阻,4:终端报修,9:其他)
     */
    @XmlAttribute(name = "stopcausecode")
    private Integer stopCauseCode;
    /**
     * 报停开始日期(如20160102)
     */
    @XmlAttribute(name = "startdate")
    private String startDate;
    /**
     * 报停结束日期(如20160103)
     */
    @XmlAttribute(name = "enddate")
    private String endDate;
}
