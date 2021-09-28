package com.zw.protocol.util;

import com.zw.platform.domain.enmu.ProtocolEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProtocolTypeUtil {

    /**
     * 部标808-2011
     */
    public static final String PROTOCOL_808_2011 = "0";

    /**
     * 黑标
     */
    public static final String ZHONG_WEI_PROTOCOL_808_2013 = "1";

    /**
     * 809冀标
     */
    public static final String T809_JI_PROTOCOL_809_2013 = "3";

    /**
     * 艾赛欧
     */
    public static final String ASO_PROTOCOL_808_2013 = "9";

    /**
     * F3-超长待机
     */
    public static final String F3_LONG_PROTOCOL_808_2013 = "10";

    /**
     * 部标2019
     */
    public static final String PROTOCOL_808_2019 = "11";

    /**
     * 川标
     */
    public static final String SI_CHUAN_PROTOCOL_808_2013 = "12";

    /**
     * 冀标
     */
    public static final String JI_PROTOCOL_808_2013 = "13";

    /**
     * 桂标
     */
    public static final String GUANG_XI_PROTOCOL_808_2013 = "14";

    /**
     * 苏标
     */
    public static final String JIANG_SU_PROTOCOL_808_2013 = "15";

    /**
     * 浙标
     */
    public static final String ZHE_JIANG_PROTOCOL_808_2013 = "16";

    /**
     * 吉标
     */
    public static final String JI_LIN_PROTOCOL_808_2013 = "17";

    /**
     * 陕标
     */
    public static final String SHAN_XI_PROTOCOL_808_2013 = "18";

    /**
     * 赣标
     */
    public static final String JIANG_XI_PROTOCOL_808_2013 = "19";

    /**
     * 沪标
     */
    public static final String SHANG_HAI_PROTOCOL_808_2019 = "20";
    /**
     * 中位标准808
     */
    public static final String ZW_PROTOCOL_808_2019 = "21";

    /**
     * 北京标准
     */
    public static final String JING_PROTOCOL_808_2019 = "24";

    /**
     * 黑龙江标准
     */
    public static final String HEI_PROTOCOL_808_2019 = "25";

    /**
     * 鲁标 808-2019
     */
    public static final String LU_PROTOCOL_808_2019 = "26";
    /**
     * 湘标 808-2019
     */
    public static final String XIANG_PROTOCOL_808_2013 = "27";
    /**
     * 粤标 808-2019
     */
    public static final String YUE_PROTOCOL_808_2019 = "28";

    /**
     * 1078报批稿
     */
    public static final String T808_2011_1078 = "23";

    /**
     * 京标
     */
    public static final String BEI_JING_PROTOCOL_808_2019 = "24";

    /**
     * 809川标
     */
    public static final String T809_SI_CHUAN_PROTOCOL_809_2013 = "2301";

    /**
     * 809桂标（广西）
     */
    public static final String T809_GUI_PROTOCOL_809_2013 = "2001";

    /**
     * 809沪标（上海）
     */
    public static final String T809_HU_PROTOCOL_809_2019 = "1091";

    /**
     * 809苏标（江苏）
     */
    public static final String T809_SU_PROTOCOL_809_2013 = "1001";
    /**
     * 809中位标准（2019)
     */
    public static final String T809_ZW_PROTOCOL_809_2019 = "1011";

    /**
     * 809北京标准
     */
    public static final String T809_JING_PROTOCOL_809_2019 = "1012";
    /**
     * 809黑龙江标准
     */
    public static final String T809_HEIPROTOCOL_809_2019 = "1013";

    /**
     * 809吉林
     */
    public static final String T809_JI_LIN_PROTOCOL_809_2013 = "7";

    /**
     * 809豫标
     */
    public static final String T809_YU_PROTOCOL_809_2013 = "16";

    /**
     * 是否通过809转发主动安全的报警，目前已经集成川，冀，桂（苏，沪标20200401）
     * @param connectProtocolType
     * @return
     */
    public static boolean isActiveSecurityStandard(String connectProtocolType) {
        return T809_SI_CHUAN_PROTOCOL_809_2013.equals(connectProtocolType) || T809_JI_PROTOCOL_809_2013
            .equals(connectProtocolType) || T809_GUI_PROTOCOL_809_2013.equals(connectProtocolType)
            || T809_HU_PROTOCOL_809_2019.equals(connectProtocolType) || T809_SU_PROTOCOL_809_2013
            .equals(connectProtocolType) || T809_ZW_PROTOCOL_809_2019.equals(connectProtocolType);
    }

    /**
     * 是否需要alarmId的标准，目前包括川桂（沪20200401新增）
     * @param protocolType
     * @return
     */
    public static boolean isNeedAlarmIdStandard(String protocolType) {
        boolean result =
            T809_SI_CHUAN_PROTOCOL_809_2013.equals(protocolType) || T809_GUI_PROTOCOL_809_2013.equals(protocolType)
                || T809_SU_PROTOCOL_809_2013.equals(protocolType);
        return result;
    }

    /**
     * 是否是桂标
     * @param protocolType
     * @return
     */
    public static boolean isGStandard(String protocolType) {
        return T809_GUI_PROTOCOL_809_2013.equals(protocolType);
    }

    /**
     * 判断协议类型放开功能，808-2011，808-2013版本协议都支持的
     * @param deviceType
     * @return
     */
    public static boolean checkAllDeviceType(String deviceType) {
        return ProtocolEnum.getSignByDeviceType(deviceType) == ProtocolEnum.ONE;
        // return PROTOCOL_808_2011.equals(deviceType) || HEI_PROTOCOL_808_2013.equals(deviceType)
        //     || ASO_PROTOCOL_808_2013.equals(deviceType)
        //     || F3_LONG_PROTOCOL_808_2013.equals(deviceType) || PROTOCOL_808_2019.equals(deviceType)
        //     || SI_CHUAN_PROTOCOL_808_2013.equals(deviceType)
        //     || JI_PROTOCOL_808_2013.equals(deviceType)
        //     || GUANG_XI_PROTOCOL_808_2013.equals(deviceType)
        //     || JIANG_SU_PROTOCOL_808_2013.equals(deviceType)
        //     || ZHE_JIANG_PROTOCOL_808_2013.equals(deviceType)
        //     || JI_LIN_PROTOCOL_808_2013.equals(deviceType)
        //     || SHAN_XI_PROTOCOL_808_2013.equals(deviceType);
    }

    /**
     * 判断协议类型放开功能，808-2013版本协议都支持的，不包含808-2011
     * @param deviceType
     * @return
     */
    public static boolean checkDeviceType2013And2019(String deviceType) {
        return ZHONG_WEI_PROTOCOL_808_2013.equals(deviceType) || ASO_PROTOCOL_808_2013.equals(deviceType)
            || F3_LONG_PROTOCOL_808_2013.equals(deviceType) || PROTOCOL_808_2019.equals(deviceType)
            || SI_CHUAN_PROTOCOL_808_2013.equals(deviceType) || JI_PROTOCOL_808_2013.equals(deviceType)
            || GUANG_XI_PROTOCOL_808_2013.equals(deviceType) || JIANG_SU_PROTOCOL_808_2013.equals(deviceType)
            || ZHE_JIANG_PROTOCOL_808_2013.equals(deviceType) || JI_LIN_PROTOCOL_808_2013.equals(deviceType)
            || SHAN_XI_PROTOCOL_808_2013.equals(deviceType) || JIANG_XI_PROTOCOL_808_2013.equals(deviceType)
            || SHANG_HAI_PROTOCOL_808_2019.equals(deviceType) || T808_2011_1078.equals(deviceType)
            || ZW_PROTOCOL_808_2019.equals(deviceType) || BEI_JING_PROTOCOL_808_2019.equals(deviceType)
            || HEI_PROTOCOL_808_2019.equals(deviceType) || YUE_PROTOCOL_808_2019.equals(deviceType)
            || LU_PROTOCOL_808_2019.equals(deviceType) || XIANG_PROTOCOL_808_2013.equals(deviceType);
    }

    /**
     * 判断协议类型放开功能，808-2013版本协议都支持的，不包含808-2011,808-2019
     * @param deviceType
     * @return
     */
    public static boolean checkDeviceType2013(String deviceType) {
        return ZHONG_WEI_PROTOCOL_808_2013.equals(deviceType) || ASO_PROTOCOL_808_2013.equals(deviceType)
            || F3_LONG_PROTOCOL_808_2013.equals(deviceType) || SI_CHUAN_PROTOCOL_808_2013.equals(deviceType)
            || JI_PROTOCOL_808_2013.equals(deviceType) || GUANG_XI_PROTOCOL_808_2013.equals(deviceType)
            || JIANG_SU_PROTOCOL_808_2013.equals(deviceType) || ZHE_JIANG_PROTOCOL_808_2013.equals(deviceType)
            || JI_LIN_PROTOCOL_808_2013.equals(deviceType) || SHAN_XI_PROTOCOL_808_2013.equals(deviceType)
            || JIANG_XI_PROTOCOL_808_2013.equals(deviceType) || XIANG_PROTOCOL_808_2013.equals(deviceType);
    }

    /**
     * 获取所有808-2013的协议的集合方法 目前只用于视频参数设置
     * @return
     */
    public static List<String> getDeviceType2013() {
        List<String> protocolTypes = new ArrayList<>();
        protocolTypes.add(ZHONG_WEI_PROTOCOL_808_2013);
        protocolTypes.add(SI_CHUAN_PROTOCOL_808_2013);
        protocolTypes.add(JI_PROTOCOL_808_2013);
        protocolTypes.add(GUANG_XI_PROTOCOL_808_2013);
        protocolTypes.add(JIANG_SU_PROTOCOL_808_2013);
        protocolTypes.add(ZHE_JIANG_PROTOCOL_808_2013);
        protocolTypes.add(JI_LIN_PROTOCOL_808_2013);
        protocolTypes.add(SHAN_XI_PROTOCOL_808_2013);
        protocolTypes.add(JIANG_XI_PROTOCOL_808_2013);
        protocolTypes.add(XIANG_PROTOCOL_808_2013);
        return protocolTypes;
    }

    /**
     * 获取需要初始化参数的协议集合
     * @return
     */
    public static List<String> getAllProtocol() {
        List<String> protocolTypes = new ArrayList<>();
        protocolTypes.add(SI_CHUAN_PROTOCOL_808_2013);
        protocolTypes.add(JI_PROTOCOL_808_2013);
        protocolTypes.add(GUANG_XI_PROTOCOL_808_2013);
        protocolTypes.add(JIANG_SU_PROTOCOL_808_2013);
        protocolTypes.add(ZHE_JIANG_PROTOCOL_808_2013);
        protocolTypes.add(JI_LIN_PROTOCOL_808_2013);
        protocolTypes.add(SHAN_XI_PROTOCOL_808_2013);
        protocolTypes.add(JIANG_XI_PROTOCOL_808_2013);
        protocolTypes.add(SHANG_HAI_PROTOCOL_808_2019);
        protocolTypes.add(ZW_PROTOCOL_808_2019);
        protocolTypes.add(BEI_JING_PROTOCOL_808_2019);
        protocolTypes.add(HEI_PROTOCOL_808_2019);
        protocolTypes.add(LU_PROTOCOL_808_2019);
        protocolTypes.add(YUE_PROTOCOL_808_2019);
        protocolTypes.add(XIANG_PROTOCOL_808_2013);
        return protocolTypes;
    }

    public static List<String> getAll2019Protocol() {
        List<String> protocolTypes = new ArrayList<>();
        protocolTypes.add(PROTOCOL_808_2019);
        protocolTypes.add(SHANG_HAI_PROTOCOL_808_2019);
        protocolTypes.add(ZW_PROTOCOL_808_2019);
        protocolTypes.add(BEI_JING_PROTOCOL_808_2019);
        protocolTypes.add(HEI_PROTOCOL_808_2019);
        protocolTypes.add(LU_PROTOCOL_808_2019);
        protocolTypes.add(YUE_PROTOCOL_808_2019);
        return protocolTypes;
    }

    public static boolean checkDeviceType2019(String deviceType) {
        return PROTOCOL_808_2019.equals(deviceType) || SHANG_HAI_PROTOCOL_808_2019.equals(deviceType)
            || ZW_PROTOCOL_808_2019.equals(deviceType) || BEI_JING_PROTOCOL_808_2019.equals(deviceType)
            || HEI_PROTOCOL_808_2019.equals(deviceType) || LU_PROTOCOL_808_2019.equals(deviceType)
            || YUE_PROTOCOL_808_2019.equals(deviceType);
    }

    /**
     * 主动安全中划分为报警和预警的协议,无等级划分协议
     * @return
     */
    public static boolean noLevelProtocol(Integer protocolTypeVal) {
        String protocolType = protocolTypeVal + "";
        return HEI_PROTOCOL_808_2019.equals(protocolType);
    }

    /**
     * @param deviceType -1、1 -> 808-2013; 11 -> 808-2019
     */
    public static List<String> getProtocolTypes(String deviceType) {
        if (StringUtils.isBlank(deviceType)) {
            return new ArrayList<>();
        }
        List<String> protocolTypes = new ArrayList<>();
        if ("-1".equals(deviceType) || "1".endsWith(deviceType)) {
            return ProtocolTypeUtil.getDeviceType2013();
        }
        if ("11".equals(deviceType)) {
            return ProtocolTypeUtil.getAll2019Protocol();
        }
        protocolTypes.add(deviceType);
        return protocolTypes;
    }
}
