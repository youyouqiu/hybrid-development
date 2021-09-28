package com.zw.app.controller.appOCR;

import com.zw.app.entity.appOCR.ImageUploadEntity;
import com.zw.app.entity.appOCR.PersonnelIdentityInfoQueryEntity;
import com.zw.app.entity.appOCR.PersonnelIdentityInfoUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseDuplicateUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseFrontUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseQueryEntity;
import com.zw.app.entity.appOCR.VehiclePhotoQueryEntity;
import com.zw.app.entity.appOCR.VehiclePhotoUpLoadEntity;
import com.zw.app.entity.appOCR.VehicleTransportInfoQueryEntity;
import com.zw.app.entity.appOCR.VehicleTransportInfoUploadEntity;
import com.zw.app.service.appOCR.AppOcrMonitorService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/app/appOcr/monitorInfo")
public class AppOcrMonitorController {

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private AppOcrMonitorService appOcrMonitorService;

    /**
     * 获取监控对象-人的身份证照片和身份证基本信息
     */
    @RequestMapping(value = "/getIdentityCardInfo", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getPersonIdentityCardPhotoInfo(HttpServletRequest request,
        @Validated PersonnelIdentityInfoQueryEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR上传监控对象-人的身份证照片和身份证基本信息
     */
    @RequestMapping(value = "/uploadIdentityCardInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean uploadPersonIdentityCardPhotoInfo(HttpServletRequest request,
        @Validated PersonnelIdentityInfoUploadEntity upLoadEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, upLoadEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR获取监控对象-车的运输证信息
     */
    @RequestMapping(value = "/getTransportNumberInfo", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getVehicleTransportNumberInfo(HttpServletRequest request,
        @Validated VehicleTransportInfoQueryEntity vehicleQueryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, vehicleQueryEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR上传监控对象-车的运输证信息
     */
    @RequestMapping(value = "/uploadTransportNumberInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean uploadVehicleTransportNumberInfo(HttpServletRequest request,
        @Validated VehicleTransportInfoUploadEntity loadEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, loadEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR获取监控对象-车的驾驶证信息
     */
    @RequestMapping(value = "/getVehicleDriveLicenseInfo", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getVehicleDriveLicenseInfo(HttpServletRequest request,
        @Validated VehicleDrivingLicenseQueryEntity vehicleQueryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, vehicleQueryEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR上传监控对象-车的驾驶证正本信息
     */
    @RequestMapping(value = "/uploadVehicleDriveLicenseFrontInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean uploadVehicleDriveLicenseFrontInfo(HttpServletRequest request,
        @Validated VehicleDrivingLicenseFrontUploadEntity upLoadEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, upLoadEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR上传监控对象-车的驾驶证副本信息
     * @return
     */
    @RequestMapping(value = "/uploadVehicleDriveLicenseDuplicateInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean uploadVehicleDriveLicenseDuplicateInfo(HttpServletRequest request,
        @Validated VehicleDrivingLicenseDuplicateUploadEntity upLoadEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, upLoadEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR获取监控对象-车的照片信息
     */
    @RequestMapping(value = "/getVehiclePhotoInfo", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getVehiclePhoto(HttpServletRequest request,
        @Validated VehiclePhotoQueryEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appOcrMonitorService);
    }

    /**
     * APP-OCR上传监控对象-车的照片信息
     */
    @RequestMapping(value = "/uploadVehiclePhoto", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean uploadVehiclePhotoInfo(HttpServletRequest request,
        @Validated VehiclePhotoUpLoadEntity uploadEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, uploadEntity, result, appOcrMonitorService);
    }

    /**
     * OCR 图片上传
     * 注: tomcat 通过post传递文件, 如果不设置, 则最大支持2M, 可通过修改Tomcat conf下的server.xml放开限制
     * <Connector port="8088" protocol="HTTP/1.1"
     *                connectionTimeout="20000"
     *                redirectPort="8443" maxPostSize="4096000"/>
     * @return AppResultBean
     */
    @RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean uploadImage(HttpServletRequest request, @Validated ImageUploadEntity entity,
        BindingResult bindingResult) {
        return AppVersionUtil.getResultData(request, entity, bindingResult, appOcrMonitorService);
    }

}
