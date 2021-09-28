package com.zw.adas.domain.define.enumcontant;

import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zjc
 * @Description:主动安全协议长度对应关系
 * @Date: create in 2020/12/14 15:40
 */
public enum AdasSettingLengthEnum {

    /******************************************前向*******************************/
    /**
     * 川标
     */
    SI_CHUAN_13_F364(0xF364, ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013, 56),
    /**
     * 冀标
     */
    JI_13_F364(0xF364, ProtocolTypeUtil.JI_PROTOCOL_808_2013, 56),
    /**
     * 桂标
     */
    GUANG_XI_13_F364(0xF364, ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013, 56),
    /**
     * 苏标
     */
    JIANG_SU_13_F364(0xF364, ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013, 56),
    /**
     * 浙标
     */
    ZHE_JIANG_13_F364(0xF364, ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013, 56),
    /**
     * 陕标
     */
    SHAN_XI_13_F364(0xF364, ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013, 56),
    /**
     * 赣标
     */
    JIANG_XI_13_F364(0xF364, ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013, 56),
    /**
     * 沪标
     */
    SHANG_HAI_19_F364(0xF364, ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019, 56),
    /**
     * 鲁标
     */
    LU_19_F364(0xF364, ProtocolTypeUtil.LU_PROTOCOL_808_2019, 56),
    /**
     * 粤标
     */
    YUE_13_F364(0xF364, ProtocolTypeUtil.YUE_PROTOCOL_808_2019, 64),
    /**
     * 吉标
     */
    JI_LIN_13_F364(0xF364, ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013, 112),
    /**
     * 中位标准
     */
    ZW_19_F0E1(0xF0E1, ProtocolTypeUtil.ZW_PROTOCOL_808_2019, 67),
    /**
     * 湘标
     */
    XIANG_11_F364(0xF364, ProtocolTypeUtil.XIANG_PROTOCOL_808_2013, 95),

    /******************************************驾驶员行为*******************************/
    /**
     * 川标
     */
    SI_CHUAN_13_F365(0xF365, ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013, 49),
    /**
     * 陕标
     */
    SHAN_XI_13_F365(0xF365, ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013, 49),
    /**
     * 冀标
     */
    JI_13_F365(0xF365, ProtocolTypeUtil.JI_PROTOCOL_808_2013, 49),
    /**
     * 桂标
     */
    GUANG_XI_13_F365(0xF365, ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013, 49),
    /**
     * 苏标
     */
    JIANG_SU_13_F365(0xF365, ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013, 49),
    /**
     * 沪标
     */
    SHANG_HAI_19_F365(0xF365, ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019, 49),
    /**
     * 鲁标
     */
    LU_19_F365(0xF365, ProtocolTypeUtil.LU_PROTOCOL_808_2019, 49),
    /**
     * 粤标
     */
    YUE_13_F365(0xF365, ProtocolTypeUtil.YUE_PROTOCOL_808_2019, 66),

    /**
     * 浙标
     */
    ZHE_JIANG_13_F365(0xF365, ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013, 62),
    /**
     * 吉标
     */
    JI_LIN_13_F365(0xF365, ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013, 126),
    /**
     * 赣标
     */
    JIANG_XI_13_F365(0xF365, ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013, 56),
    /**
     * 中位标准
     */
    ZW_19_F0E2(0XF0E2, ProtocolTypeUtil.ZW_PROTOCOL_808_2019, 66),
    /**
     * 湘标
     */
    XIANG_11_F365(0xF365, ProtocolTypeUtil.XIANG_PROTOCOL_808_2013, 109),

    /******************************************胎压*******************************/
    /**
     * 川标
     */
    SI_CHUAN_13_F366(0xF366, ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013, 109),
    /**
     * 苏标
     */
    JIANG_SU_13_F366(0xF366, ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013, 36),
    /**
     * 桂标
     */
    GUANG_XI_13_F366(0xF366, ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013, 36),
    /**
     * 赣标
     */
    JIANG_XI_13_F366(0xF366, ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013, 36),
    /**
     * 鲁标
     */
    LU_19_F366(0xF366, ProtocolTypeUtil.LU_PROTOCOL_808_2019, 36),
    /**
     * 粤标
     */
    YUE_13_F366(0xF366, ProtocolTypeUtil.YUE_PROTOCOL_808_2019, 36),
    /**
     * 中位标准
     */
    ZW_19_F0E3(0XF0E3, ProtocolTypeUtil.YUE_PROTOCOL_808_2019, 56),

    /******************************************盲区*******************************/

    /**
     * 川标
     */
    SI_CHUAN_13_F367(0xF367, ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013, 2),
    /**
     * 桂标
     */
    GUANG_XI_13_F367(0xF367, ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013, 2),
    /**
     * 苏标
     */
    JIANG_SU_13_F367(0xF367, ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013, 2),
    /**
     * 浙标
     */
    ZHE_JIANG_13_F366(0xF366, ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013, 2),
    /**
     * 吉标
     */
    JI_LIN_13_F367(0xF367, ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013, 2),
    /**
     * 陕标
     */
    SHAN_XI_13_F367(0xF367, ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013, 2),
    /**
     * 赣标
     */
    JIANG_XI_13_F367(0xF367, ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013, 2),
    /**
     * 沪标
     */
    SHANG_HAI_19_F367(0xF367, ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019, 2),
    /**
     * 中位标准
     */
    ZW_19_F0E4(0XF0E4, ProtocolTypeUtil.ZW_PROTOCOL_808_2019, 2),
    /**
     * 鲁标
     */
    LU_19_F367(0xF367, ProtocolTypeUtil.LU_PROTOCOL_808_2019, 2),
    /**
     * 粤标
     */
    YUE_13_F367(0xF367, ProtocolTypeUtil.YUE_PROTOCOL_808_2019, 2),

    /******************************************不按规定上下客或超员报警*******************************/

    /**
     * 沪标
     */
    SHANG_HAI_19_F368(0xF368, ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019, 38),
    /**
     * 湘标
     */
    XIANG_11_F368(0xF368, ProtocolTypeUtil.XIANG_PROTOCOL_808_2013, 52),

    /******************************************激烈驾驶*******************************/

    /**
     * 川标
     */
    SI_CHUAN_13_F370(0xF370, ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013, 54),
    /**
     * 湘标
     */
    GUANG_XI_13_F370(0xF370, ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013, 54),

    /******************************************驾驶员身份识别*******************************/
    /**
     * 黑标
     */
    HEI_2019_E138(0xE138, ProtocolTypeUtil.HEI_PROTOCOL_808_2019, 22),

    /******************************************车辆运行监测*******************************/
    /**
     * 黑标
     */
    HEI_19_E139(0xE139, ProtocolTypeUtil.HEI_PROTOCOL_808_2019, 33),

    /******************************************驾驶员驾驶行为*******************************/
    /**
     * 黑标
     */
    HEI_19_E140(0xE140, ProtocolTypeUtil.HEI_PROTOCOL_808_2019, 33),
    /******************************************设备失效监测系统*******************************/
    /**
     * 黑标
     */
    HEI_19_E141(0xE141, ProtocolTypeUtil.HEI_PROTOCOL_808_2019, 33),

    /******************************************驾驶员对比*******************************/
    /**
     * 鲁标
     */
    LU_19_F0E9(0xF0E9, ProtocolTypeUtil.LU_PROTOCOL_808_2019, 3),
    /**
     * 湘标
     */
    XIANG_11_F0E9(0xF0E9, ProtocolTypeUtil.XIANG_PROTOCOL_808_2013, 4),
    ;
    private static Logger log = LogManager.getLogger(AdasSettingLengthEnum.class);

    /**
     * 协议类型
     */
    private String protocolType;
    /**
     * 参数id
     */
    private Integer paramId;
    /**
     * 参数设置长度
     */
    private Integer length;

    private static Map<String, AdasSettingLengthEnum> paramIdLengthMap = new HashMap<>();

    static {
        for (AdasSettingLengthEnum data : AdasSettingLengthEnum.values()) {
            paramIdLengthMap.put(getKey(data.paramId, data.protocolType), data);
        }
    }

    private static String getKey(Integer parameterId, String protocolType) {
        return protocolType + "_" + parameterId;
    }

    AdasSettingLengthEnum(Integer paramId, String protocolType, Integer length) {
        this.protocolType = protocolType;
        this.paramId = paramId;
        this.length = length;
    }

    public static Integer getParamIdLength(Integer parameterId, String protocolType) {
        AdasSettingLengthEnum data = paramIdLengthMap.get(getKey(parameterId, protocolType));
        if (data == null) {
            return null;
        }
        return data.length;
    }

    /**
     * 该方法是原来的方法，暂时留在在这里，运行一段时间之后，如果新方法没有问题，会删掉这个方法
     * @param parameterId
     * @param protocolType
     * @return
     */
    @Deprecated
    public static Integer getParameterLength(Integer parameterId, String protocolType) {
        Integer length = null;
        //前向
        if (parameterId == 0xF364 || parameterId == 0XF0E1) {
            switch (protocolType) {
                case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                    length = 56;
                    break;
                case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                    length = 112;
                    break;
                case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                    length = 67;
                    break;

                case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                    length = 95;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //驾驶员行为
        if (parameterId == 0xF365 || parameterId == 0XF0E2) {
            switch (protocolType) {
                case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                    length = 49;
                    break;
                case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                    length = 62;
                    break;
                case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                    length = 126;
                    break;
                case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                    length = 56;
                    break;
                case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                    length = 66;
                    break;
                case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                    length = 109;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //胎压
        if (parameterId == 0xF366 || parameterId == 0XF0E3) {
            switch (protocolType) {
                case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                    length = 36;
                    break;
                case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                    length = 56;
                    break;
                case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:

                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //盲区
        if (parameterId == 0xF367 || parameterId == 0XF0E4) {
            switch (protocolType) {
                case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                    length = 2;
                    break;
                case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }

        }
        //激烈驾驶
        if (parameterId == 0xF370) {
            switch (protocolType) {
                case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                    length = 54;
                    break;
                case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //不按规定上下客或超员报警
        if (parameterId == 0xF368) {
            switch (protocolType) {
                case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                    length = 38;
                    break;
                case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                    break;
                case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                    length = 28;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //驾驶员身份识别
        if (parameterId == 0xE138) {
            switch (protocolType) {
                case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
                    length = 22;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //车辆运行监测
        if (parameterId == 0xE139) {
            switch (protocolType) {
                case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
                    length = 33;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //驾驶员驾驶行为
        if (parameterId == 0xE140) {
            switch (protocolType) {
                case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
                    length = 33;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        } //设备失效监测系统
        if (parameterId == 0xE141) {
            switch (protocolType) {
                case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
                    length = 45;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        //驾驶员对比
        if (parameterId == 0xF0E9) {
            switch (protocolType) {
                case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                    length = 3;
                    break;
                case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                    length = 4;
                    break;
                default:
                    logGetParamIdLengthError(protocolType);
                    break;
            }
        }
        return length;
    }

    private static void logGetParamIdLengthError(String protocolType) {
        log.info("设置数据长度,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
    }

}
