package com.zw.platform.domain.other.protocol.jl.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求企业信息
 * @Author denghuabing
 * @Date 2020/6/12
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class QueryAloneCorpInfo {
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
}
