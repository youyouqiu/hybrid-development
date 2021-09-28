package com.zw.adas.domain.driverStatistics;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * Created by lijie on 2020/5/8  17:33
 */
@Data
public class IcDriverMessages implements T808MsgBody {

    //private String driverName;

    //private String certificationID;

    //private String cAName;

    //private String expiryDate;

    private String time;

    private Integer status;

    //private String iCResult;

}
