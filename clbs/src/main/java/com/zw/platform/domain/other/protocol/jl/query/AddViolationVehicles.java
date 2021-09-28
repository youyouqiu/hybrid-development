package com.zw.platform.domain.other.protocol.jl.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 上传违规车辆
 * @author denghuabing
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class AddViolationVehicles {
    /**
     * 车牌号码
     */
    @XmlAttribute(name = "vehicleno")
    private String vehicleNo;
    /**
     * 车牌颜色编码(1:蓝 色,2:黄色,3:黑色,4:白色,5:农黄,6:农蓝,7:农绿,9:其他)
     */
    @XmlAttribute(name = "platecolorid")
    private Integer plateColorId;
    /**
     * 违规类型(1:扭动镜头,2:遮挡镜头,3:无照片,4:无定位数据,5:轨迹.异常，6:超员,7:超速,8:脱线运行)
     */
    @XmlAttribute(name = "violationtype")
    private Integer violationType;
    /**
     * 违规时间格式: yyyyMMddHHmmss (如20160209132421 )
     */
    @XmlAttribute(name = "violattime")
    private String violationTime;

}
