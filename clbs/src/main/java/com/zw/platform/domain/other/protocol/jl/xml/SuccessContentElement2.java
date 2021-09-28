package com.zw.platform.domain.other.protocol.jl.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/11/3
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "Content")
public class SuccessContentElement2 {
    @XmlAttribute
    private Integer result;
    @XmlAttribute
    private String msg;
}
