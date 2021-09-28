package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;


/**
 * @author XK
 */
@Data
public class ProfessionalsPhotoUpdateAck implements Serializable, T808MsgBody {
    private static final long serialVersionUID = 1L;


    /**
     * 卡号
     */
    private String certificationId;

    /**
     * 人证照片版本
     */
    private String version;

    /**
     * 人证照片下载地址
     */
    private String downUrl;


    public static ProfessionalsPhotoUpdateAck getInstance(String certificationId, String version, String downUrl) {
        ProfessionalsPhotoUpdateAck data = new ProfessionalsPhotoUpdateAck();
        String cardNumber = "00000000000000000000";
        if (StringUtils.isEmpty(certificationId)) {
            certificationId = cardNumber;
        } else {
            int len = 20 - certificationId.length();
            certificationId = cardNumber.substring(0, len) + certificationId;
        }
        data.certificationId = certificationId;
        data.version = version;
        data.downUrl = downUrl;
        return data;
    }

}
