package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: . 上传报警车辆
 * @Author denghuabing
 * @Date 2020/6/12
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class AddVehicleAlarmRelieveInfo {
    /**
     * 车牌号码
     */
    @XmlAttribute(name = "vehicleno")
    private String vehicleNo;
    /**
     * 车牌颜色编码(1:蓝色,2:黄色,3:黑色,4:白色,5:农黄,6:农蓝,7:农绿,9:其他)
     */
    @XmlAttribute(name = "platecolorid")
    private Integer plateColorId;
    /**
     * 报警类型编码:0:紧急报警 10:疲劳报警 200:禁入报警 201:禁出报警 210:偏航报警 41:超速报警 53:夜间行驶报警
     */
    @XmlAttribute(name = "alarmtype")
    private Integer alarmType;
    /**
     * 报警时间 格式: yyyyMMddHHmmss (如20160209132421 )
     */
    @XmlAttribute(name = "alarmtime")
    private Long alarmTime;
    /**
     * 解除时间.  格式: yyyyMMddHHmmss (如20160209132421 )
     */
    @XmlAttribute(name = "relievetime")
    private Long relieveTime;
    /**
     * 报警处理状态编码(1:处理中,2:已处理完毕,3:不作处理,4:将来处理)
     */
    @XmlAttribute(name = "alarmstatus")
    private Integer alarmStatus;

    public AddVehicleAlarmRelieveInfo(String vehicleNo, Integer plateColorId, Integer alarmType, Long alarmTime,
        Long relieveTime, Integer alarmStatus) {
        this.vehicleNo = vehicleNo;
        this.plateColorId = plateColorId;
        this.alarmType = alarmType;
        this.alarmTime = alarmTime;
        this.relieveTime = relieveTime;
        this.alarmStatus = alarmStatus;
    }
}
