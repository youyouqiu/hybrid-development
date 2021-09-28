package com.zw.platform.domain.sendTxt;

import com.zw.platform.util.DateUtil;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.protocol.util.ProtocolTypeUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 文本下发消息体格式
 *
 * @author Created by LiaoYuecai on 2017/3/31.
 */
@Data
public class SendTxt implements T808MsgBody {

    private static final long serialVersionUID = 288580613454334318L;

    /**
     * 标志
     */
    private Integer sign;

    /**
     * 文本信息
     */
    private String txt;

    /**
     * 1:通知; 2: 服务
     */
    private Integer textType;

    /**
     * 1:通知; 2: 服务
     */
    private Integer type;

    /**
     * 协议类型
     */
    private String deviceType;

    public static SendTxt getSendTxt2019(SendTextParam sendTextParam) {
        SendTxt sendTxt = new SendTxt();
        Integer signData = sendTextParam.getSignData();
        sendTxt.setSign(signData);
        sendTxt.setTextType(sendTextParam.getTextType());
        sendTxt.setType(sendTextParam.getTextType());
        sendTxt.setTxt(sendTextParam.getSendTextContent());
        return sendTxt;
    }

    public static SendTxt getSendTxt2013(String sendTextContent, String marks) {
        SendTxt sendTxt = new SendTxt();
        Integer signData = getSignData(marks);
        sendTxt.setSign(signData);
        sendTxt.setTxt(sendTextContent);
        return sendTxt;
    }

    private static Integer getSignData(String marks) {
        List<String> markList = new ArrayList<>();
        if (StringUtils.isNotBlank(marks)) {
            markList = Arrays.asList(marks.split(","));
        }
        Integer emergency = markList.contains("1") ? 1 : 0;
        Integer displayTerminalDisplay = markList.contains("3") ? 1 : 0;
        Integer tts = markList.contains("4") ? 1 : 0;
        Integer advertisingDisplay = markList.contains("5") ? 1 : 0;
        Integer ttsAndDeal = markList.contains("6") ? 1 : 0;
        return emergency + (displayTerminalDisplay << 2) + (tts << 3) + (advertisingDisplay << 4) + (ttsAndDeal << 5);
    }

    /**
     * 转换为日志中的格式
     *
     * @see com.zw.platform.domain.sendTxt.SendTxt
     * @see com.zw.platform.domain.sendTxt.SendTextParam
     */
    public static String convertTxtToLogMsg(String deviceType, SendTxt sendTxt) {
        final int sign = sendTxt.getSign();
        final Boolean isEmergency;
        final Boolean isService;
        final Boolean isNotification;
        final Boolean isNavigation;
        final Boolean isCanError;
        final Boolean isTerminalDisplay;
        final Boolean isTts;
        final Boolean isAdDisplay;
        if (ProtocolTypeUtil.checkDeviceType2013(deviceType)) {
            isEmergency = (sign & 1) == 1;
            isService = false;
            isNotification = false;
            isNavigation = false;
            isCanError = false;
            isTerminalDisplay = (sign >> 2 & 1) == 1;
            isTts = (sign >> 3 & 1) == 1;
            isAdDisplay = (sign >> 4 & 1) == 1;
        } else if (ProtocolTypeUtil.checkDeviceType2019(deviceType)) {
            isEmergency = (sign & 3) == 2;
            isService = (sign & 3) == 1;
            isNotification = (sign & 3) == 3;
            isNavigation = (sign >> 5 & 1) == 0;
            isCanError = (sign >> 5 & 1) == 1;
            isTerminalDisplay = (sign >> 2 & 1) == 1;
            isTts = (sign >> 3 & 1) == 1;
            isAdDisplay = false;
        } else {
            isEmergency = null;
            isService = null;
            isNotification = null;
            isNavigation = null;
            isCanError = null;
            isTerminalDisplay = null;
            isTts = null;
            isAdDisplay = null;
        }
        return "下发时间：" + DateUtil.YMD_HMS.format(LocalDateTime.now()).orElse("")
                + " <br/>文本内容：" + sendTxt.getTxt()
                + " <br/>紧急：" + convert(isEmergency)
                + " <br/>服务：" + convert(isService)
                + " <br/>通知：" + convert(isNotification)
                + " <br/>中心导航信息：" + convert(isNavigation)
                + " <br/>CAN故障码信息：" + convert(isCanError)
                + " <br/>终端显示器显示：" + convert(isTerminalDisplay)
                + " <br/>终端TTS语音：" + convert(isTts)
                + " <br/>广告屏显示：" + convert(isAdDisplay);
    }

    /**
     * Boolean -> String
     *
     * @param value input
     * @return “” or "是" or "否"
     */
    private static String convert(Boolean value) {
        return null == value ? "" : value ? "是" : "否";
    }

}
