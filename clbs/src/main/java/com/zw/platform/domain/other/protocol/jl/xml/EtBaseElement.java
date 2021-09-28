package com.zw.platform.domain.other.protocol.jl.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/11 15:31
 */
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
public class EtBaseElement {

    /**
     * 0: 上传失败; 1: 上传失败
     */
    @XmlAttribute
    private String result;
    @XmlAttribute
    private String msg;
}
