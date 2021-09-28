package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.monitoring.form.T808_0x8202;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.monitoring.RealTimeService;
import com.zw.platform.service.oilmassmgt.impl.OilCalibrationServiceImpl;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.ws.impl.WsOilSensorCommandService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by LiaoYuecai on 2016/10/14.
 */
@Service
public class RealTimeServiceImpl implements RealTimeService {
    private static Logger log = LogManager.getLogger(OilCalibrationServiceImpl.class);

    @Autowired
    GroupDao groupDao;

    @Autowired
    private WsOilSensorCommandService wsOilSensorCommandService;

    @Autowired
    private UserService userService;

    @Override
    public String getGroups(String vehicleId) throws Exception {
        // 得到当前用户的id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidByDn(userId);
        // 返回当前用户下的车辆分组
        return groupDao.getUserVehicleGroups(vehicleId, uuid);
    }

    @Override
    public String getParametersTrace(String vehicleId, T808_0x8202 ptf) throws Exception {
        Integer msgSN = null;
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (bindDTO != null) {
            String deviceNumber = bindDTO.getDeviceNumber();
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setSimcardNumber(bindDTO.getSimCardNumber());
            vehicle.setDeviceId(bindDTO.getDeviceId());
            vehicle.setDeviceType(bindDTO.getDeviceType());
            // 序列号
            msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
            if (msgSN != null) { // 设备已经注册
                // 下发参数
                wsOilSensorCommandService.parametersTrace(ptf, msgSN, vehicle);
            } else { // 设备未注册
            }
        }
        return Converter.toBlank(msgSN);
    }

    @Override
    public void exportKML(String[] lineArr, HttpServletResponse response) throws Exception {
        Element root = DocumentHelper.createElement("kml"); // 根节点是kml
        Document document = DocumentHelper.createDocument(root);
        // 给根节点kml添加属性
        root.addAttribute("xmlns", "http://www.opengis.net/kml/2.2")
            .addAttribute("xmlns:gx", "http://www.google.com/kml/ext/2.2")
            .addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .addAttribute("xsi:schemaLocation",
                "http://www.opengis.net/kml/2.2 http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd"
                + " http://www.google.com/kml/ext/2.2 http://code.google.com/apis/kml/schema/kml22gx.xsd");

        // 给根节点kml添加子节点 Document
        Element documentElement = root.addElement("Document");
        // =====================================节点分割线================================================================//
        documentElement.addElement("Name").addText("6196445_gjhf");
        Element styleElement = documentElement.addElement("Style");// Style节点
        styleElement.addAttribute("id", "sn_ylw-pushpin0");
        // IconStyle
        Element iconStyleElement = styleElement.addElement("IconStyle");
        iconStyleElement.addElement("scale").addText("1.1");
        // Icon
        Element iconElement = iconStyleElement.addElement("Icon");
        iconElement.addElement("href").addText("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
        // hotSpot
        Element hotSpotElement = iconStyleElement.addElement("hotSpot");
        hotSpotElement.addAttribute("x", "20");
        hotSpotElement.addAttribute("xunits", "pixels");
        hotSpotElement.addAttribute("y", "2");
        hotSpotElement.addAttribute("yunits", "pixels");
        // LineStyle
        Element lineStyleElement = styleElement.addElement("LineStyle");
        lineStyleElement.addElement("color").addText("ff0000ff");
        lineStyleElement.addElement("width").addText("2");
        // ===================================节点分割线=================================================================//
        Element styleElement2 = documentElement.addElement("Style");// Style节点
        styleElement2.addAttribute("id", "sn_ylw-pushpin1");
        // IconStyle
        Element iconStyleElement2 = styleElement2.addElement("IconStyle");
        iconStyleElement2.addElement("scale").addText("1.1");
        // Icon
        Element iconElement2 = iconStyleElement2.addElement("Icon");
        iconElement2.addElement("href").addText("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
        // hotSpot
        Element hotSpotElement2 = iconStyleElement.addElement("hotSpot");
        hotSpotElement2.addAttribute("x", "20");
        hotSpotElement2.addAttribute("xunits", "pixels");
        hotSpotElement2.addAttribute("y", "2");
        hotSpotElement2.addAttribute("yunits", "pixels");
        // LineStyle
        Element lineStyleElement2 = styleElement2.addElement("LineStyle");
        lineStyleElement2.addElement("color").addText("ff0000ff");
        lineStyleElement2.addElement("width").addText("2");
        // ===================================节点分割线=================================================================//
        Element styleMapElement = documentElement.addElement("StyleMap");
        styleMapElement.addAttribute("id", "msn_ylw-pushpin");
        Element pairElement = styleMapElement.addElement("Pair");
        pairElement.addElement("key").addText("normal");
        pairElement.addElement("styleUrl").addText("#sn_ylw-pushpin0");
        Element pair2Element = styleMapElement.addElement("Pair");
        pair2Element.addElement("key").addText("highlight");
        pair2Element.addElement("styleUrl").addText("#sh_ylw-pushpin1");
        // ==================================节点分割线==================================================================//
        Element placemarkElment = documentElement.addElement("Placemark");
        placemarkElment.addElement("NAME").addText("6196445_gjhf");
        placemarkElment.addElement("styleUrl").addText("#msn_ylw-pushpin");
        Element lineStringElment = placemarkElment.addElement("LineString");
        lineStringElment.addElement("tessellate").addText("1");
        String lines = "";
        for (int i = 0; i < lineArr.length; i++) {
            String line = lineArr[i];
            lines = lines + line + ", 0 ";
        }
        lineStringElment.addElement("coordinates").addText(lines

        );

        response.getOutputStream().write(document.asXML().getBytes());
        response.getOutputStream().close();
    }

    /**
     * 从缓存中取电子围栏信息
     * @param sendId
     * @param vehicleId
     * @return
     */
    @Override
    public JSONObject getFenceInfoBySendId(Integer sendId, String vehicleId) throws Exception {
        // 组装key
        RedisKey redisKey = HistoryRedisKeyEnum.FENCE_SEND.of(vehicleId, sendId);
        String redisMessage = RedisHelper.getString(redisKey);
        if (StringUtils.isBlank(redisMessage)) {
            return new JSONObject();
        }
        // 将String转为json对象
        return JSONObject.parseObject(redisMessage);
    }
}
