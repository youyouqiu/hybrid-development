package com.zw.platform.service.monitoring;

import com.zw.lkyw.domain.SendMsgDetail;
import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.platform.domain.vas.alram.OutputControlSend;
import com.zw.platform.domain.vas.alram.OutputControlSendInfo;
import com.zw.platform.util.common.JsonResultBean;

/**
 * 指令service @author  Tdz
 *
 * @create 2017-04-21 9:09
 **/
public interface OrderService {
    /**
     * 拍照指令
     */
    boolean takePhoto(OrderForm form);

    /**
     * 录像指令
     */

    boolean getVideo(OrderForm form);

    /**
     * 输出控制
     */
    boolean outputControlBy13(OutputControlSend outputControlSend, boolean isAuto);

    /**
     * 输出控制
     */
    boolean outputControlBy19(OutputControlSendInfo outputControlSendInfo, String vehicleId, boolean isAuto);

    /**
     * 位置汇报
     */
    boolean regularReports(OrderForm form);

    /**
     * 电话回拨
     */
    boolean telBack(OrderForm form);

    /**
     * 文本信息
     */
    boolean sendTxt(OrderForm form)
        throws Exception;

    /**
     * 只发送文本信息，不订阅
     */
    boolean sendTxtOnly(OrderForm form) throws Exception;

    /**
     * 提问下发
     */
    boolean sendQuestion(OrderForm form)
        throws Exception;

    /**
     * 终端控制
     */
    boolean terminalControl(OrderForm form)
        throws Exception;

    /**
     * 车辆控制
     *
     * @param form
     * @return
     */
    boolean vehicleControl(OrderForm form)
        throws Exception;

    /**
     * 设置超速
     *
     * @param form
     * @return
     */
    boolean updateSpeedMax(OrderForm form)
        throws Exception;

    boolean recordCollection(OrderForm form)
        throws Exception;

    /**
     * 行驶记录参数下传
     *
     * @param form
     * @return
     */
    boolean recordSend(OrderForm form)
        throws Exception;

    boolean multimediaRetrieval(OrderForm form)
        throws Exception;

    boolean multimediaUpload(OrderForm form)
        throws Exception;

    boolean record(OrderForm form);

    boolean originalOrder(OrderForm form)
        throws Exception;

    boolean terminalParameters(OrderForm form)
        throws Exception;

    boolean informationService(OrderForm form)
        throws Exception;

    boolean oilElectric(OrderForm form)
        throws Exception;

    boolean updateTerminalPlate(OrderForm orderForm, String ip)
        throws Exception;

    JsonResultBean findOBD();

    boolean sendOBDParam(OrderForm orderForm, String ip)
        throws Exception;

    /**
     * 下发链路监测
     *
     * @param vehicleId
     * @return
     * @throws Exception
     */
    boolean sendLindCheck(String vehicleId);

    boolean sendFenceQuery(OrderForm form, String ip);

    /**
     * 驾驶员主动上报
     * @param form form
     * @param ip ip
     * @return
     */
    boolean sendDriverActiveReport(OrderForm form, String ip);

    SendMsgDetail getSendMsgDetail(OrderForm form);
}
