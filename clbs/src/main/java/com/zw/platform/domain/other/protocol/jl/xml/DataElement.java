package com.zw.platform.domain.other.protocol.jl.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/11 11:11
 */
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
public class DataElement<T> {
    @XmlAttribute
    private String type;
    @XmlAttribute
    private String typename;

    @XmlAnyElement(lax = true)
    private List<T> content;

    /**
     * 如果这个参数不为空, 调用三方平台异常， 从里面获取
     */
    @XmlElement(name = "ETBase")
    private EtBaseElement etBase;
}
