package com.zw.adas.domain.define.enumcontant;

import java.util.HashMap;
import java.util.Map;

import static com.zw.protocol.util.ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.HEI_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.JING_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.JI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.LU_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.XIANG_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.YUE_PROTOCOL_808_2019;
import static com.zw.protocol.util.ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013;
import static com.zw.protocol.util.ProtocolTypeUtil.ZW_PROTOCOL_808_2019;

/**
 * 读取外设参数设置页面
 * @author zhangjuan
 */
public enum AdasReadParamPageEnum {
    /**
     * 川标
     */
    CHUAN(SI_CHUAN_PROTOCOL_808_2013, "paramInfo"),

    /**
     * 冀标
     */
    JI(JI_PROTOCOL_808_2013, "jiParamInfo"),

    /**
     * 桂标
     */
    GUI(GUANG_XI_PROTOCOL_808_2013, "guiParamInfo"),

    /**
     * 苏标
     */
    SU(JIANG_SU_PROTOCOL_808_2013, "suParamInfo"),

    /**
     * 浙标
     */
    ZHE(ZHE_JIANG_PROTOCOL_808_2013, "zheParamInfo"),

    /**
     * 吉标
     */
    JI_LIN(JI_LIN_PROTOCOL_808_2013, "jiLinParamInfo"),

    /**
     * 陕西
     */
    SHAN(SHAN_XI_PROTOCOL_808_2013, "shanParamInfo"),

    /**
     * 赣标
     */
    GAN(JIANG_XI_PROTOCOL_808_2013, "ganParamInfo"),

    /**
     * 沪标
     */
    HU(SHANG_HAI_PROTOCOL_808_2019, "huParamInfo"),

    /**
     * 中位标
     */
    ZHONG_WEI(ZW_PROTOCOL_808_2019, "zhongWeiParamInfo"),

    /**
     * 黑标
     */
    HEI(HEI_PROTOCOL_808_2019, "heiParamInfo"),

    /**
     * 京标
     */
    JING(JING_PROTOCOL_808_2019, "jingParamInfo"),

    /**
     * 鲁标
     */
    LU(LU_PROTOCOL_808_2019, "luParamInfo"),

    /**
     * 湘标
     */
    XIANG(XIANG_PROTOCOL_808_2013, "xiangParamInfo"),

    /**
     * 粤标
     */
    YUE(YUE_PROTOCOL_808_2019, "yueParamInfo"),;
    private String protocolType;

    private String page;

    AdasReadParamPageEnum(String protocolType, String page) {
        this.protocolType = protocolType;
        this.page = "risk/riskManagement/DefineSettings/" + page;
    }

    private static Map<String, AdasReadParamPageEnum> readPageMap = new HashMap<>();

    static {
        for (AdasReadParamPageEnum pageEnum : AdasReadParamPageEnum.values()) {
            readPageMap.put(pageEnum.protocolType, pageEnum);
        }
    }

    public static String getPage(String protocolType) {
        AdasReadParamPageEnum pageEnum = readPageMap.get(protocolType);
        //为空返回错误页面
        if (pageEnum == null) {
            return null;
        }
        return pageEnum.page;
    }
}
