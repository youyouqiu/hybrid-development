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
 * 读取外设信基本信息页面
 * @author zhangjuan
 */
public enum AdasReadPeripheralMsgPageEnum {
    /**
     * 川标
     */
    CHUAN(SI_CHUAN_PROTOCOL_808_2013, "chuanPerInfo"),

    /**
     * 冀标
     */
    JI(JI_PROTOCOL_808_2013, "jiPerInfo"),

    /**
     * 苏标
     */
    SU(JIANG_SU_PROTOCOL_808_2013, "suPerInfo"),

    /**
     * 桂标
     */
    GUI(GUANG_XI_PROTOCOL_808_2013, "guiPerInfo"),

    /**
     * 浙标
     */
    ZHE(ZHE_JIANG_PROTOCOL_808_2013, "zhePerInfo"),

    /**
     * 陕标
     */
    SHAN(SHAN_XI_PROTOCOL_808_2013, "shanPerInfo"),

    /**
     * 赣标
     */
    GAN(JIANG_XI_PROTOCOL_808_2013, "ganPerInfo"),

    /**
     * 沪标
     */
    HU(SHANG_HAI_PROTOCOL_808_2019, "huPerInfo"),

    /**
     * 中位标
     */
    ZHONG_WEI(ZW_PROTOCOL_808_2019, "zhongWeiPerInfo"),

    /**
     * 吉标
     */
    JI_LIN(JI_LIN_PROTOCOL_808_2013, "jiLinPerInfo"),

    /**
     * 鲁标
     */
    LU(LU_PROTOCOL_808_2019, "luPerInfo"),

    /**
     * 粤标
     */
    YUE(YUE_PROTOCOL_808_2019, "yuePerInfo"),

    ;

    private String protocolType;

    private String page;

    AdasReadPeripheralMsgPageEnum(String protocolType, String page) {
        this.protocolType = protocolType;
        this.page = "risk/riskManagement/DefineSettings/" + page;
    }

    private static Map<String, AdasReadPeripheralMsgPageEnum> readPageMap = new HashMap<>();

    static {
        for (AdasReadPeripheralMsgPageEnum pageEnum : AdasReadPeripheralMsgPageEnum.values()) {
            readPageMap.put(pageEnum.protocolType, pageEnum);
        }
    }

    public static String getPage(String protocolType) {
        AdasReadPeripheralMsgPageEnum pageEnum = readPageMap.get(protocolType);
        //为空返回错误页面
        if (pageEnum == null) {
            return null;
        }
        return pageEnum.page;
    }
}
