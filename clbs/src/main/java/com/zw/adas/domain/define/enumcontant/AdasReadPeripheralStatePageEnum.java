package com.zw.adas.domain.define.enumcontant;

import java.util.HashMap;
import java.util.Map;

import static com.zw.protocol.util.ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.JI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.LU_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.YUE_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.ZW_PROTOCOL_808_2019;

/**
 * 读取外设状态信息页面
 * @author zhangjuan
 */
public enum AdasReadPeripheralStatePageEnum {
    /**
     * 川标
     */

    CHUAN(SI_CHUAN_PROTOCOL_808_2013, "chuanPerState"),

    /**
     * 冀标
     */
    JI(JI_PROTOCOL_808_2013, "jiPerState"),

    /**
     * 苏标
     */
    SU(JIANG_SU_PROTOCOL_808_2013, "suPerState"),

    /**
     * 桂标
     */
    GUI(GUANG_XI_PROTOCOL_808_2013, "guiPerState"),

    /**
     * 浙标
     */
    ZHE(ZHE_JIANG_PROTOCOL_808_2013, "zhePerState"),

    /**
     * 吉标
     */
    JI_LIN(JI_LIN_PROTOCOL_808_2013, "jiLinPerState"),

    /**
     * 陕标
     */
    SHAN(SHAN_XI_PROTOCOL_808_2013, "shanPerState"),

    /**
     * 赣标
     */
    GAN(JIANG_XI_PROTOCOL_808_2013, "ganPerState"),

    /**
     * 沪标
     */
    HU(SHANG_HAI_PROTOCOL_808_2019, "huPerState"),

    /**
     * 中位标
     */
    ZHONG_WEI(ZW_PROTOCOL_808_2019, "zhongWeiPerState"),

    /**
     * 鲁标
     */
    LU(LU_PROTOCOL_808_2019, "luPerState"),

    /**
     * 粤标
     */
    YUE(YUE_PROTOCOL_808_2019, "yuePerState"),

    ;

    private String protocolType;

    private String page;

    AdasReadPeripheralStatePageEnum(String protocolType, String page) {
        this.protocolType = protocolType;
        this.page = "risk/riskManagement/DefineSettings/" + page;
    }

    private static Map<String, AdasReadPeripheralStatePageEnum> readPageMap = new HashMap<>();

    static {
        for (AdasReadPeripheralStatePageEnum pageEnum : AdasReadPeripheralStatePageEnum.values()) {
            readPageMap.put(pageEnum.protocolType, pageEnum);
        }
    }

    public static String getPage(String protocolType) {
        AdasReadPeripheralStatePageEnum pageEnum = readPageMap.get(protocolType);
        //为空返回错误页面
        if (pageEnum == null) {
            return null;
        }
        return pageEnum.page;
    }
}
