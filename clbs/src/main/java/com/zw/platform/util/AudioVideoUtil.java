package com.zw.platform.util;

/**
 * 音视频工具类
 *
 * @author hujun
 * @version 创建时间：2018年1月9日 下午2:26:28
 */
public class AudioVideoUtil {

    private static final String[] ALL_CHANNALS = {
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
        "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
        "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33",
        "36", "37", "64", "65", "100", "101"};

    /**
     * 获取音频采样率标识对应的名称
     *
     * @param samplingrate
     * @return
     * @author hujun
     * @version 创建时间：2018年1月9日 下午2:27:09
     */
    public static String getSamplingrateName(Integer samplingrate) {
        StringBuffer sb = new StringBuffer();
        if (samplingrate != null) {
            switch (samplingrate) {
                case 0:
                    sb.append("8kHz");
                    break;
                case 1:
                    sb.append("22.05kHz");
                    break;
                case 2:
                    sb.append("44.1kHz");
                    break;
                case 3:
                    sb.append("48kHz");
                    break;

                default:
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 获取音频采样位数标识对应的名称
     *
     * @param samplingBit
     * @return
     * @author hujun
     * @version 创建时间：2018年1月9日 下午2:31:08
     */
    public static String getSamplingBitName(Integer samplingBit) {
        StringBuffer sb = new StringBuffer();
        if (samplingBit != null) {
            switch (samplingBit) {
                case 0:
                    sb.append("8位");
                    break;
                case 1:
                    sb.append("16位");
                    break;
                case 2:
                    sb.append("32位");
                    break;

                default:
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 是否支持音频输出
     *
     * @param isContinuousOutput
     * @return
     * @author hujun
     * @version 创建时间：2018年1月9日 下午2:35:55
     */
    public static String isContinuousOutput(Integer isContinuousOutput) {
        StringBuffer sb = new StringBuffer();
        if (isContinuousOutput != null) {
            switch (isContinuousOutput) {
                case 0:
                    sb.append("不支持");
                    break;
                case 1:
                    sb.append("支持");
                    break;

                default:
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 获取音视频编码方式名称
     *
     * @param audioVideoCodeType
     * @return
     * @author hujun
     * @version 创建时间：2018年1月9日 下午2:42:57
     */
    public static String getAudioVideoCodeName(Integer audioVideoCodeType) {
        StringBuffer sb = new StringBuffer();
        if (audioVideoCodeType != null) {
            switch (audioVideoCodeType) {
                case 1:
                    sb.append("G.721");
                    break;
                case 2:
                    sb.append("G.722");
                    break;
                case 3:
                    sb.append("G.723");
                    break;
                case 4:
                    sb.append("G.728");
                    break;
                case 5:
                    sb.append("G.729");
                    break;
                case 6:
                    sb.append("G.711A");
                    break;
                case 7:
                    sb.append("G.711U");
                    break;
                case 8:
                    sb.append("G.726");
                    break;
                case 9:
                    sb.append("G.729A");
                    break;
                case 10:
                    sb.append("DVI4_3");
                    break;
                case 11:
                    sb.append("DVI4_4");
                    break;
                case 12:
                    sb.append("DVI4_8K");
                    break;
                case 13:
                    sb.append("DVI4_16K");
                    break;
                case 14:
                    sb.append("LPC");
                    break;
                case 15:
                    sb.append("S168E_STEREO");
                    break;
                case 16:
                    sb.append("S16BE_MONO");
                    break;
                case 17:
                    sb.append("MPEGAUDIO");
                    break;
                case 18:
                    sb.append("LPCM");
                    break;
                case 19:
                    sb.append("AAC");
                    break;
                case 20:
                    sb.append("WMA9STD");
                    break;
                case 21:
                    sb.append("HEAAC");
                    break;
                case 22:
                    sb.append("PCM_VOICE");
                    break;
                case 23:
                    sb.append("PCM_AUDIO");
                    break;
                case 24:
                    sb.append("AACLC");
                    break;
                case 25:
                    sb.append("MP3");
                    break;
                case 26:
                    sb.append("ADPCMA");
                    break;
                case 27:
                    sb.append("MP4AUDIO");
                    break;
                case 28:
                    sb.append("AMR");
                    break;
                case 91:
                    sb.append("透传");
                    break;
                case 98:
                    sb.append("H.264");
                    break;
                case 99:
                    sb.append("H.265");
                    break;
                case 100:
                    sb.append("AVS");
                    break;
                case 101:
                    sb.append("SVAC");
                    break;
                default:
                    sb.append("未知编码");
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 获取协议定义的逻辑通道号及名称
     *
     * @return
     */
    public static String[] getLogicChannels() {
        return ALL_CHANNALS;
    }

    /**
     * 获取逻辑通道号对应协议定义名称
     *
     * @param channelNumber
     * @return
     */
    public static String getLogicChannelName(Integer channelNumber) {
        String logicChannelName = null;
        if (channelNumber == null) {
            return logicChannelName;
        }
        switch (channelNumber) {
            case 1:
                logicChannelName = "驾驶员";
                break;
            case 2:
                logicChannelName = "车辆正前方";
                break;
            case 3:
                logicChannelName = "车前门";
                break;
            case 4:
                logicChannelName = "车厢前部";
                break;
            case 5:
                logicChannelName = "ADAS";
                break;
            case 6:
                logicChannelName = "DSM";
                break;
            case 7:
                logicChannelName = "行李舱";
                break;
            case 8:
                logicChannelName = "车辆左侧";
                break;
            case 9:
                logicChannelName = "车辆右侧";
                break;
            case 10:
                logicChannelName = "车辆正后方";
                break;
            case 11:
                logicChannelName = "车厢中部";
                break;
            case 12:
                logicChannelName = "车中门";
                break;
            case 13:
                logicChannelName = "驾驶席车门";
                break;
            case 14:
            case 15:
            case 16:
                logicChannelName = "预留";
                break;
            case 33:
                logicChannelName = "驾驶员";
                break;
            case 36:
                logicChannelName = "车厢前部";
                break;
            case 37:
                logicChannelName = "车厢后部";
                break;
            default:
                break;
        }
        return logicChannelName;
    }
}
