package com.zw.platform.domain.other.protocol.jl.xml;

import com.zw.platform.domain.other.protocol.jl.query.AddVehicleAlarmRelieveInfo;
import com.zw.platform.domain.other.protocol.jl.query.AddVehicleStopInfo;
import com.zw.platform.domain.other.protocol.jl.query.AddViolationVehicles;
import com.zw.platform.domain.other.protocol.jl.query.QueryAloneCorpInfo;
import com.zw.platform.domain.other.protocol.jl.query.QueryAloneVehicleInfo;
import com.zw.platform.domain.other.protocol.jl.query.QueryCorpAlarmCheckInfo;
import com.zw.platform.domain.other.protocol.jl.query.QueryCorpCheckInfo;
import com.zw.platform.domain.other.protocol.jl.query.QueryPlatformCheckInfo;
import com.zw.platform.domain.other.protocol.jl.query.QueryVehicleServiceInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/11 11:02
 */
@Data
@NoArgsConstructor
@XmlRootElement(name = "request")
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlSeeAlso({ AddVehicleAlarmRelieveInfo.class, AddVehicleStopInfo.class, AddViolationVehicles.class,
    QueryAloneCorpInfo.class, QueryAloneVehicleInfo.class, QueryCorpAlarmCheckInfo.class, QueryCorpCheckInfo.class,
    QueryPlatformCheckInfo.class, QueryVehicleServiceInfo.class })
public class RequestRootElement<T> {

    @XmlAttribute
    private String server;

    @XmlElement(name = "data")
    private DataElement<T> data;
}

