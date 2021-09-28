package com.zw.platform.domain.other.protocol.jl.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/11 11:02
 */
@Data
@NoArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ResponseRootElement<T> {

    @XmlAttribute
    private String server;

    @XmlElement(name = "data")
    private DataElement<T> data;
}
