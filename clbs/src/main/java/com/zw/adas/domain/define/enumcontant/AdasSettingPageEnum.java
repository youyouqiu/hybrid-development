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
 * @Author: zjc
 * @Description:主动安全参数设置页面（ 新增/修改页面弹出）和协议关系实体
 * @Date: create in 2020/12/7 9:26
 */
public enum AdasSettingPageEnum {
    /**
     * 川标
     */
    CHUAN(SI_CHUAN_PROTOCOL_808_2013, "newSetting"),
    /**
     * 冀标
     */
    JI(JI_PROTOCOL_808_2013, "jiSetting"),
    /**
     * 苏标
     */
    SU(JIANG_SU_PROTOCOL_808_2013, "suSetting"),
    /**
     * 桂标
     */
    GUI(GUANG_XI_PROTOCOL_808_2013, "guiSetting"),
    /**
     * 浙标
     */
    ZHE(ZHE_JIANG_PROTOCOL_808_2013, "zheSetting"),
    /**
     * 吉标
     */
    JILIN(JI_LIN_PROTOCOL_808_2013, "jiLinSetting"),
    /**
     * 陕标
     */
    SHAN(SHAN_XI_PROTOCOL_808_2013, "shanSetting"),
    /**
     * 赣标
     */
    GAN(JIANG_XI_PROTOCOL_808_2013, "ganSetting"),
    /**
     * 沪
     */
    HU(SHANG_HAI_PROTOCOL_808_2019, "huSetting"),
    /**
     * 中位2019
     */
    ZW2019(ZW_PROTOCOL_808_2019, "zhongWeiSetting"),
    /**
     * 京标
     */
    JING(JING_PROTOCOL_808_2019, "jingSetting"),
    /**
     * 黑标
     */
    HEI(HEI_PROTOCOL_808_2019, "heiSetting"),
    /**
     * 鲁标
     */
    LU(LU_PROTOCOL_808_2019, "luSetting"),
    /**
     * 湘标
     */
    XIANG(XIANG_PROTOCOL_808_2013, "xiangSetting"),
    /**
     * 粤标
     */
    YUE(YUE_PROTOCOL_808_2019, "yueSetting"),

    ;

    private String protocolType;

    private String page;

    AdasSettingPageEnum(String protocolType, String page) {
        this.protocolType = protocolType;
        this.page = "risk/riskManagement/DefineSettings/" + page;
    }

    private static Map<String, AdasSettingPageEnum> editPageMap = new HashMap<>();

    static {
        for (AdasSettingPageEnum pageEnum : AdasSettingPageEnum.values()) {
            editPageMap.put(pageEnum.protocolType, pageEnum);
        }
    }

    /**
     * 通过协议类型获取对应的页面地址
     * @param protocolType
     * @return
     */

    public static String getPage(String protocolType) {
        AdasSettingPageEnum pageEnum = editPageMap.get(protocolType);
        //为空返回错误页面
        if (pageEnum == null) {
            return "html/errors/error_exception";
        }
        return pageEnum.page;
    }

}
