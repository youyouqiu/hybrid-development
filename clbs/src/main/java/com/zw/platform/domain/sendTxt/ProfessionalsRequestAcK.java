package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author XK
 */
@Data
public class ProfessionalsRequestAcK implements Serializable, T808MsgBody {

    /**
     * 从业资格证号
     */
    private String certificationId;

    /**
     * 比对结果
     */
    private Integer result;

    /**
     * 版本号
     */
    private String version;

    /**
     * 下载路径
     */
    private String downUrl;

    public static ProfessionalsRequestAcK getInstance(String certificationId, String version,
                                                      String downUrl, Integer result) {
        ProfessionalsRequestAcK data = new ProfessionalsRequestAcK();
        String cardNumber = "00000000000000000000";
        if (StringUtils.isEmpty(certificationId)) {
            certificationId = cardNumber;
        } else {
            int len = 20 - certificationId.length();
            certificationId = cardNumber.substring(0, len) + certificationId;
        }
        data.certificationId = certificationId;
        if (result == 0X00) {
            data.result = result;
            data.downUrl = null;
            data.version = null;
        } else {
            data.result = 0X01;
            data.version = version;
            data.downUrl = downUrl;
        }
        return data;
    }

}
