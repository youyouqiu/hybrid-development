package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 请求企业考核信息
 * @Author denghuabing
 * @Date 2020/6/12
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "content")
public class QueryCorpCheckInfo {
    /**
     * 企业名称
     */
    @XmlAttribute(name = "corpname")
    private String corpName;
    /**
     * 企业经营许可证号
     */
    @XmlAttribute(name = "licencecard")
    private String licenceCard;
    /**
     * 开始时间(格式: yyyyMMdd，如: 20160301)
     */
    @XmlAttribute(name = "starttime")
    private String startTime;
    /**
     * 结束时间(格式: yyyyMMdd，如: 20160301 )
     */
    @XmlAttribute(name = "endtime")
    private String endTime;
    /**
     * 时间类型(1:年 2:季3:月4:周 5:日 )
     */
    @XmlAttribute(name = "timetype")
    private String timeType;
}
