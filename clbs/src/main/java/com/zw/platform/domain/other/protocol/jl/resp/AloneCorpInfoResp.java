package com.zw.platform.domain.other.protocol.jl.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求企业信息
 * @Author denghuabing
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class AloneCorpInfoResp {

    /**
     * 企业名称
     */
    @XmlAttribute(name = "corpname")
    private String corpName;
    /**
     * 企业经营许可证号
     */
    @XmlAttribute(name = "corpnum")
    private String corpNum;
    /**
     * 企业地址
     */
    @XmlAttribute(name = "address")
    private String address;
    /**
     * 法人代表名称
     */
    @XmlAttribute(name = "corporatename")
    private String corporateName;
    /**
     * 法人身份证号
     */
    @XmlAttribute(name = "identification")
    private String identification;
    /**
     * 电话
     */
    @XmlAttribute(name = "phoneno")
    private String phoneNo;
    /**
     * 所属机构
     */
    @XmlAttribute(name = "managedep")
    private String manageDep;

    /**
     * 下发时间
     */
    private String sendTime;
}
