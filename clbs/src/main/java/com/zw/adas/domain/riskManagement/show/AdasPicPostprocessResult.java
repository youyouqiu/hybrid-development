package com.zw.adas.domain.riskManagement.show;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdasPicPostprocessResult implements Serializable {
    private static final long serialVersionUID = -8297424070864940677L;

    /**
     * no doc
     */
    private String monitorId;
    private String monitorName;
    private String orgName;
    private String groupName;
    private String vehiclePurposeCategory;
    private String deviceNumber;
    private String simcardNumber;
    private String deviceModelNumber;

}
