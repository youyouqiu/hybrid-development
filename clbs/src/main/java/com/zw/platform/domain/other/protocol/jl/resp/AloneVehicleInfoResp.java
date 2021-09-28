package com.zw.platform.domain.other.protocol.jl.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求车辆信息
 * @Author denghuabing
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class AloneVehicleInfoResp {

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

    private String plateColorStr;

    /**
     * 业户名称
     */
    @XmlAttribute(name = "corpname")
    private String corpName;

    /**
     * 业户经营许可证号
     */
    @XmlAttribute(name = "corpnum")
    private String corpNum;

    /**
     * 运营状态编码(10:运营、21:停运、22:挂失、31:迁出(过户)、32:迁出(转籍)、33:报废、34:歇业、80:注销、90:其 他)
     */
    @XmlAttribute(name = "runstatus")
    private String runStatus;

    private String runStatusStr;

    /**
     * 所属行业(011:班车客运、 012:包车可与、030:危货运输、900:其他)
     */
    @XmlAttribute(name = "vcltrade")
    private String vclTrade;

    private String vclTradeStr;

    /**
     * 发证机构
     */
    @XmlAttribute(name = "managedep")
    private String manageDep;

    /**
     * 道路运输许可证号
     */
    @XmlAttribute(name = "vehiclenum")
    private String vehicleNum;

    /**
     * 道路运输许可证发证日期
     */
    @XmlAttribute(name = "vclnumredy")
    private String vclNumReDy;

    /**
     * 道路运输许可证有效起始日期
     */
    @XmlAttribute(name = "vclnumstart")
    private String vclNumStart;

    /**
     * 道路运输许可证有效截止日期
     */
    @XmlAttribute(name = "vclnumend")
    private String vclNumEnd;

    /**
     * 厂牌
     */
    @XmlAttribute(name = "factoryid")
    private String factoryId;

    /**
     * 车身颜色
     */
    @XmlAttribute(name = "vclcolor")
    private String vclColor;

    /**
     * 型号
     */
    @XmlAttribute(name = "factorytype")
    private String factoryType;

    /**
     * 车架号
     */
    @XmlAttribute(name = "vclbodynum")
    private String vclBodyNum;

    /**
     * 发动机号
     */
    @XmlAttribute(name = "vclenginenum")
    private String vclEngineNum;

    /**
     * 核定载质量(吨)
     */
    @XmlAttribute(name = "tons")
    private String tons;

    /**
     * 核定载客
     */
    @XmlAttribute(name = "seats")
    private String seats;

    private String sendTime;

}
