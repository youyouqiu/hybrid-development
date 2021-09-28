package com.zw.platform.domain.other.protocol.jl.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/11 11:14
 */
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class SuccessContentElement {
    @XmlAttribute
    private Integer result;
    @XmlAttribute
    private String msg;
}
