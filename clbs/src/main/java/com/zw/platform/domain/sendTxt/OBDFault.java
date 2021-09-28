package com.zw.platform.domain.sendTxt;

import java.io.Serializable;

/**
 * 查询OBD故障码
 * 透传类型0xF1
 */
public class OBDFault implements Serializable {

    private Integer sensorID = 0xE5;

    private Integer dataLen = 1;

    private Integer data = 0;
}
