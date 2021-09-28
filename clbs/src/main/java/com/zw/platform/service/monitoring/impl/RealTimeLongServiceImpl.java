package com.zw.platform.service.monitoring.impl;

import com.zw.platform.domain.multimedia.form.LongOrderForm;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.domain.vas.monitoring.form.T808_0x8202;
import com.zw.platform.service.monitoring.RealTimeLongService;
import com.zw.platform.service.sendTxt.AsoSendTxtService;
import com.zw.platform.service.sendTxt.F3SendTxtService;
import com.zw.ws.entity.aso.ASOFixedPoint;
import com.zw.ws.entity.aso.ASOFrequency;
import com.zw.ws.entity.aso.ASOTransparent;
import com.zw.ws.entity.t808.parameter.ParamItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RealTimeLongServiceImpl implements RealTimeLongService {

    @Autowired
    F3SendTxtService f3SendTxtService;

    @Autowired
    RealTimeServiceImpl realTime;
    @Autowired
    AsoSendTxtService asoSendTxtService;

    @Override
    public boolean sendReportSet(String vehicleId, StationParam stationParam) throws Exception {
        //F3超待设备基站参数设置
        Integer paramLength = 15;
        Integer count = stationParam.getLocationTimeNum();
        if (count != null) {
            paramLength = paramLength + (count * 3);
        }
        ParamItem t808Param = new ParamItem();
        t808Param.setParamId(0xF308);
        t808Param.setParamLength(paramLength);
        t808Param.setParamValue(stationParam);
        List<ParamItem> params = new ArrayList<>();
        params.add(t808Param);
        f3SendTxtService.setF3SetParam(vehicleId, "", params, "8103-F3-08", false);
        return true;

    }

    @Override
    public boolean sendLocationTracking(LongOrderForm form) throws Exception {
        T808_0x8202 ptf = new T808_0x8202();
        ptf.setInterval(Integer.valueOf(form.getLongInterval()));
        ptf.setValidity(Integer.valueOf(form.getLongValidity()));
        f3SendTxtService.setParametersTrace(form.getVid(), "", ptf, "8202-F3-08", false);
        return true;
    }

    @Override
    public boolean sendPassthroughInstruction(String vehicleId, ASOTransparent asoTransparent) throws Exception {
        asoSendTxtService.sendTransparent(vehicleId, asoTransparent);
        return true;
    }

    @Override
    public boolean sendASOReportSet(String vehicleId, ASOFrequency frequency) throws Exception {
        asoSendTxtService.sendFrequency(vehicleId, frequency);
        return true;
    }

    @Override
    public boolean sendASOFixedPoint(String vehicleId, ASOFixedPoint fixedPoint) throws Exception {
        asoSendTxtService.sendFixedPoint(vehicleId, fixedPoint);
        return true;
    }

    @Override
    public boolean sendRestart(String vehicleId) throws Exception {
        asoSendTxtService.sendRestart(vehicleId);
        return true;
    }

}
