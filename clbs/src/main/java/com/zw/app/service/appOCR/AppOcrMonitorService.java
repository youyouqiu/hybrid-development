package com.zw.app.service.appOCR;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.entity.appOCR.PersonnelIdentityInfoUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseDuplicateUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseFrontUploadEntity;
import com.zw.app.entity.appOCR.VehiclePhotoUpLoadEntity;
import com.zw.app.entity.appOCR.VehicleTransportInfoUploadEntity;


/**
 * OCR-人员身份证信息service
 */
public interface AppOcrMonitorService {
    /**
     * 获取监控对象-人的身份证信息
     */
    JSONObject getPersonIdentityCardInfo(String personId) throws Exception;

    /**
     * 存储监控对象-人的身份证信息
     */
    boolean uploadPersonIdentityCardInfo(PersonnelIdentityInfoUploadEntity entity) throws Exception;

    /**
     * 获取监控对象-车的运输证信息
     */
    JSONObject getVehicleTransportNumberInfo(String vehicleId) throws Exception;

    /**
     * 存储监控对象-车的运输证信息
     */
    boolean uploadVehicleTransportNumberInfo(VehicleTransportInfoUploadEntity entity) throws Exception;

    /**
     * 获取监控对象-车的行驶证信息
     */
    JSONObject getVehicleDrivingLicenseInfo(String vehicleId) throws Exception;

    /**
     * 存储监控对象-车的行驶证正本信息
     */
    boolean uploadVehicleDrivingLicenseFrontInfo(VehicleDrivingLicenseFrontUploadEntity entity) throws Exception;

    /**
     * 存储监控对象-车的行驶证副本信息
     */
    boolean uploadVehicleDrivingLicenseDuplicateInfo(VehicleDrivingLicenseDuplicateUploadEntity entity)
        throws Exception;

    /**
     * 获取监控对象-车的照片信息
     */
    JSONObject getVehiclePhotoInfo(String vehicleId) throws Exception;

    /**
     * 存储监控对象-车的照片信息
     */
    boolean uploadVehiclePhotoInfo(VehiclePhotoUpLoadEntity entity) throws Exception;

    /**
     * 图片上传
     * @return JSONObject
     */
    JSONObject uploadImage(String decodeImage);
}
