package com.zw.platform.controller.monitoring;


import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.multimedia.form.LongOrderForm;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.service.monitoring.RealTimeLongService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Customer;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.ws.entity.aso.ASOFixedPoint;
import com.zw.ws.entity.aso.ASOFrequency;
import com.zw.ws.entity.aso.ASOTransparent;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/v/monitoringLong")
public class RealTimeLongController {

    private static Logger log = LogManager.getLogger(RealTimeLongController.class);

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "HH:mm:ss";

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    RealTimeLongService realTimeLongService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    // 超长待机下发参数
    @RequestMapping(value = {"/sendParam"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParam(@ModelAttribute("form") LongOrderForm form, HttpServletRequest request) {
        boolean re = false;
        Customer c = new Customer();
        form.setSerialNumber(Integer.valueOf(c.getCustomerID()));
        Map<String, Object> relt = new HashMap<String, Object>();
        try {
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            final Map<String, String> vehicleInfo =
                    RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(form.getVid(), "name", "deviceType"));
            if (vehicleInfo != null) {
                String brand = vehicleInfo.get("name");
                String deviceType = vehicleInfo.get("deviceType");
                String vid = form.getVid();
                String[] vehicleStr = logSearchService.findCarMsg(vid);
                switch (form.getOrderType()) {
                    case 17:// 上报频率设置
                        if ("9".equals(deviceType)) { // 艾赛欧超长待机
                            ASOFrequency frequency = new ASOFrequency();
                            frequency.setFrequencyTime(form.getLocationNumber());
                            frequency.setLocationType(form.getLocationPattern());
                            re = realTimeLongService.sendASOReportSet(vid, frequency);
                        } else { // F3超长待机
                            StationParam stationParam = new StationParam();
                            String vehicleNumber = brand;// 获取车牌号
                            stationParam.setVehicleNumber(vehicleNumber);
                            stationParam.setLocationTimeNum(0);// 上报时间节点数
                            stationParam.setRequiteTime("");// 上报时间节点
                            stationParam.setLocationNumber(form.getLocationNumber());// 上报间隔时间
                            stationParam.setLocationPattern(form.getLocationPattern());// 定位模式
                            stationParam.setRequiteTime(form.getRequiteTime());// 上报起始时间
                            re = realTimeLongService.sendReportSet(vid, stationParam);
                        }
                        logSearchService.addLog(ip, "监控对象：" + vehicleStr[0] + " 上报频率设置", "3", "MONITORING",
                                vehicleStr[0],
                                vehicleStr[1]);
                        relt.put("type", re);
                        relt.put("serialNumber", form.getSerialNumber());
                        return new JsonResultBean(relt);
                    case 18:// 定点
                        if ("9".equals(deviceType)) { // 艾赛欧超长待机
                            ASOFixedPoint fixedPoint = new ASOFixedPoint();
                            fixedPoint.setLocationTime(form.getLocationTimes()[0].replace(":", ""));// 上报时间节点
                            re = realTimeLongService.sendASOFixedPoint(vid, fixedPoint);
                        } else { // F3超长待机
                            String[] checkTimes = form.getLocationTimes();
                            List<Long> tl = new ArrayList<Long>();
                            for (String ct : checkTimes) {
                                if (StringUtil.isNullOrBlank(ct)) {
                                    continue;
                                }
                                tl.add(DateUtils.parseDate(ct, DATE_FORMAT).getTime());
                            }
                            if (tl.size() > 1) {
                                Collections.sort(tl);// 排序
                                for (int i = 0; i < tl.size() - 1; i++) { // 校验相邻时间点是否大于等于300秒
                                    if (tl.get(i + 1) - tl.get(i) < 300000) {
                                        relt.put("type", re);
                                        relt.put("serialNumber", form.getSerialNumber());
                                        return new JsonResultBean(relt);
                                    }
                                }
                            }
                            StationParam stationParam = new StationParam();
                            stationParam.setLocationTimes(form.getLocationTimes());
                            stationParam.setLocationNumber(0);// 上报间隔时间
                            stationParam.setRequiteTime("000001");// 上报起始时间
                            stationParam.setLocationPattern(0xFF);// 定位模式
                            String[] times = stationParam.getLocationTimes();
                            int count = 0;
                            StringBuffer timestr = new StringBuffer();
                            for (String time : times) {
                                if (StringUtil.isNullOrBlank(time)) {
                                    continue;
                                }
                                timestr.append(time);
                                count++;
                            }
                            stationParam.setLocationTime(timestr.toString().replace(":", ""));// 上报时间节点
                            stationParam.setLocationTimeNum(count);// 上报时间节点数
                            re = realTimeLongService.sendReportSet(vid, stationParam);
                        }
                        logSearchService.addLog(ip, "监控对象：" + vehicleStr[0] + " 定点和校时", "3", "MONITORING",
                                vehicleStr[0],
                                vehicleStr[1]);
                        relt.put("type", re);
                        relt.put("serialNumber", form.getSerialNumber());
                        return new JsonResultBean(relt);
                    case 19:// F3位置跟踪
                        re = realTimeLongService.sendLocationTracking(form);
                        logSearchService.addLog(ip, "监控对象：" + vehicleStr[0] + " 位置跟踪", "3", "MONITORING", vehicleStr[0],
                                vehicleStr[1]);
                        relt.put("type", re);
                        relt.put("serialNumber", form.getSerialNumber());
                        return new JsonResultBean(relt);
                    case 20:// 艾赛欧透传指令
                        ASOTransparent asoTransparent = new ASOTransparent();
                        asoTransparent.setContent(form.getLongData());
                        re = realTimeLongService.sendPassthroughInstruction(vid, asoTransparent);
                        logSearchService.addLog(ip, "监控对象：" + vehicleStr[0] + " 透传指令", "3", "MONITORING", vehicleStr[0],
                                vehicleStr[1]);
                        relt.put("type", re);
                        relt.put("serialNumber", form.getSerialNumber());
                        return new JsonResultBean(relt);
                    case 21:// 艾赛欧远程复位
                        re = realTimeLongService.sendRestart(vid);
                        logSearchService.addLog(ip, "监控对象：" + vehicleStr[0] + " 远程复位", "3", "MONITORING", vehicleStr[0],
                                vehicleStr[1]);
                        relt.put("type", re);
                        relt.put("serialNumber", form.getSerialNumber());
                        return new JsonResultBean(relt);
                    default:
                        return new JsonResultBean(relt);

                }
            }
            return new JsonResultBean(relt);
        } catch (Exception e) {
            log.error("超长待机指令下发异常:" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
