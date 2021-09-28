package com.zw.app.service.appOCR.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.utils.FastDFSClient;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.entity.appOCR.PersonnelIdentityInfoUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseDuplicateUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseFrontUploadEntity;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseUploadEntity;
import com.zw.app.entity.appOCR.VehiclePhotoUpLoadEntity;
import com.zw.app.entity.appOCR.VehicleTransportInfoUploadEntity;
import com.zw.app.service.appOCR.AppOcrMonitorService;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.domain.PeopleDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.repository.PeopleDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.common.MapUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@AppServerVersion
public class AppOcrMonitorServiceImpl implements AppOcrMonitorService {

    @Autowired
    private PeopleDao peopleDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/getIdentityCardInfo")
    public JSONObject getPersonIdentityCardInfo(String personId) throws Exception {
        PeopleDO peopleDO = peopleDao.getById(personId);
        JSONObject msg = new JSONObject();
        if (peopleDO == null) {
            return msg;
        }
        msg.put("name", peopleDO.getName());
        msg.put("gender", peopleDO.getGender());
        msg.put("identity", peopleDO.getIdentity());
        msg.put("identityCardPhoto", peopleDO.getIdentityCardPhoto());
        // 获取人员的身份证信息
        return msg;
    }

    /**
     * APP上传人员身份证信息
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/uploadIdentityCardInfo")
    public boolean uploadPersonIdentityCardInfo(PersonnelIdentityInfoUploadEntity entity) throws Exception {
        if (entity == null) {
            return false;
        }
        String monitorId = entity.getMonitorId();
        PeopleDO peopleDO = peopleDao.getById(monitorId);
        if (peopleDO == null) {
            return false;
        }
        String oldIdentityPhotoSavePath = entity.getOldIdentityCardPhoto();
        if (StringUtils.isNotBlank(oldIdentityPhotoSavePath)) {
            fastDFSClient.deleteFile(oldIdentityPhotoSavePath);
        }
        String birthDay = entity.getBirthday();
        // 判断生日是否是合法的格式
        if (StringUtils.isNotBlank(birthDay)) {
            entity.setBirthday(ocrPhotoDateFormat(birthDay));
        } else {
            entity.setBirthday(null);
        }
        String userName = SystemHelper.getCurrentUsername();
        entity.setUpdateDataUsername(userName);
        // 更新人员信息
        boolean result = peopleDao.updatePersonIdentityCardInfo(entity);
        if (result) {
            // 记录日志
            String message = "用户:上传身份证信息";
            logSearchService.log(message, "4", "", userName, peopleDO.getPeopleNumber(), "");
            // 将人员身份证图片路径更新至缓存
            // String redisKey = monitorId + "_people_list";
            // Map<String, String> peopleInfo =
            //     RedisUtil.getMapObjFromRedisByKey(redisKey, PublicVariable.REDIS_TEN_DATABASE);
            // peopleInfo.put("identityCardPhoto", entity.getIdentityCardPhoto());
            // RedisHelper.setString(redisKey, JSONObject.toJSONString(peopleInfo), PublicVariable.REDIS_TEN_DATABASE);
        }
        return result;
    }

    /**
     * 获取车辆运输证信息
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/getTransportNumberInfo")
    public JSONObject getVehicleTransportNumberInfo(String vehicleId) throws Exception {
        JSONObject msg = new JSONObject();
        if (StringUtils.isBlank(vehicleId)) {
            return msg;
        }
        VehicleTransportInfoUploadEntity info = newVehicleDao.getVehicleTransportNumber(vehicleId);
        msg.put("transportNumber", info != null ? info.getTransportNumber() : "");
        msg.put("transportNumberPhoto", info != null ? info.getTransportNumberPhoto() : "");
        return msg;
    }

    /**
     * 上传车辆运输证
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/uploadTransportNumberInfo")
    public boolean uploadVehicleTransportNumberInfo(VehicleTransportInfoUploadEntity entity) throws Exception {
        if (entity == null) {
            return false;
        }
        VehicleDTO vehicleDTO = getVehicleInfo(entity.getMonitorId());
        if (vehicleDTO == null) {
            return false;
        }
        String oldTransportNumberPhoto = entity.getOldTransportNumberPhoto();
        if (StringUtils.isNotBlank(oldTransportNumberPhoto)) {
            fastDFSClient.deleteFile(oldTransportNumberPhoto);
        }
        // 当前操作用户
        String userName = SystemHelper.getCurrentUsername();
        entity.setUpdateDataUsername(userName);
        boolean result = newVehicleDao.updateVehicleTransportNumberInfo(entity);
        if (result) {
            // 记录日志
            String message = "用户:上传运输证信息";
            logSearchService.log(message, "4", "", userName, vehicleDTO.getName(),
                String.valueOf(vehicleDTO.getPlateColor()));
            // 将人员身份证图片路径更新至缓存
            // String redisKey = entity.getMonitorId() + "_vehicle_list";
            // Map<String, String> vehicleCacheInfo =
            //     RedisUtil.getMapObjFromRedisByKey(redisKey, PublicVariable.REDIS_TEN_DATABASE);
            // vehicleCacheInfo.put("transportNumberPhoto", entity.getTransportNumberPhoto());
            // RedisHelper.setString(redisKey, JSONObject.toJSONString(vehicleCacheInfo),
            //     PublicVariable.REDIS_TEN_DATABASE);
        }
        return result;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/getVehicleDriveLicenseInfo")
    public JSONObject getVehicleDrivingLicenseInfo(String vehicleId) throws Exception {
        JSONObject msg = new JSONObject();
        if (StringUtils.isBlank(vehicleId)) {
            return msg;
        }
        VehicleDrivingLicenseUploadEntity drivingInfo = newVehicleDao.getVehicleDrivingLicense(vehicleId);
        if (drivingInfo == null) {
            return msg;
        }
        msg.put("chassisNumber", drivingInfo.getChassisNumber());
        msg.put("engineNumber", drivingInfo.getEngineNumber());
        msg.put("usingNature", drivingInfo.getUsingNature());
        msg.put("brandModel", drivingInfo.getBrandModel());
        String registrationDate = drivingInfo.getRegistrationDate();
        if (StringUtils.isNotBlank(registrationDate) && registrationDate.contains(".0")) {
            registrationDate = registrationDate.substring(0, registrationDate.indexOf("."));
        }
        msg.put("registrationDate", registrationDate);
        String licenseIssuanceDate = drivingInfo.getLicenseIssuanceDate();
        if (StringUtils.isNotBlank(licenseIssuanceDate) && licenseIssuanceDate.contains(".0")) {
            licenseIssuanceDate = licenseIssuanceDate.substring(0, licenseIssuanceDate.indexOf("."));
        }
        msg.put("licenseIssuanceDate", licenseIssuanceDate);
        msg.put("drivingLicenseFrontPhoto", drivingInfo.getDrivingLicenseFrontPhoto());
        msg.put("validEndDate", drivingInfo.getValidEndDate());
        msg.put("totalQuality", drivingInfo.getTotalQuality());
        msg.put("profileSizeLong", drivingInfo.getProfileSizeLong());
        msg.put("profileSizeWide", drivingInfo.getProfileSizeWide());
        msg.put("profileSizeHigh", drivingInfo.getProfileSizeHigh());
        msg.put("drivingLicenseDuplicatePhoto", drivingInfo.getDrivingLicenseDuplicatePhoto());
        msg.put("standard", drivingInfo.getPageSign());
        return msg;
    }

    /**
     * 保存APP上传的行驶证正本信息
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/uploadVehicleDriveLicenseFrontInfo")
    public boolean uploadVehicleDrivingLicenseFrontInfo(VehicleDrivingLicenseFrontUploadEntity entity)
        throws Exception {
        if (entity == null) {
            return false;
        }
        VehicleDrivingLicenseUploadEntity uploadEntity = new VehicleDrivingLicenseUploadEntity();
        BeanUtils.copyProperties(entity, uploadEntity);
        uploadEntity.setPageSign("1");
        return uploadVehicleDrivingLicenseInfo(uploadEntity);
    }

    /**
     * 保存APP上传的行驶证副本信息
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/uploadVehicleDriveLicenseDuplicateInfo")
    public boolean uploadVehicleDrivingLicenseDuplicateInfo(VehicleDrivingLicenseDuplicateUploadEntity entity)
        throws Exception {
        if (entity == null) {
            return false;
        }
        VehicleDrivingLicenseUploadEntity uploadEntity = new VehicleDrivingLicenseUploadEntity();
        BeanUtils.copyProperties(entity, uploadEntity);
        uploadEntity.setPageSign("2");
        return uploadVehicleDrivingLicenseInfo(uploadEntity);
    }

    private boolean uploadVehicleDrivingLicenseInfo(VehicleDrivingLicenseUploadEntity entity) throws Exception {
        if (entity == null) {
            return false;
        }
        String pageSign = entity.getPageSign();
        if (StringUtils.isBlank(pageSign)) {
            return false;
        }
        VehicleDTO vehicleDTO = getVehicleInfo(entity.getMonitorId());
        if (vehicleDTO == null) {
            return false;
        }
        String oldDrivingLicensePhoto;
        String message;
        if ("1".equals(pageSign)) { // 行驶证正面数据
            oldDrivingLicensePhoto = entity.getOldDrivingLicenseFrontPhoto();
            message = "用户:上传行驶证正本信息";
            entity.setRegistrationDate(ocrPhotoDateFormat(entity.getRegistrationDate()));
            entity.setLicenseIssuanceDate(ocrPhotoDateFormat(entity.getLicenseIssuanceDate()));
        } else {
            oldDrivingLicensePhoto = entity.getOldDrivingLicenseDuplicatePhoto();
            message = "用户:上传行驶证副本信息";
            entity.setValidEndDate(ocrPhotoDateFormat(entity.getValidEndDate()));
        }
        if (StringUtils.isNotBlank(oldDrivingLicensePhoto)) {
            fastDFSClient.deleteFile(oldDrivingLicensePhoto);
        }
        String userName = SystemHelper.getCurrentUsername();
        entity.setUpdateDataUsername(userName);
        boolean result = newVehicleDao.updateVehicleDrivingLicenseInfo(entity);
        if (result) {
            // 记录日志
            logSearchService.log(message, "4", "", userName, vehicleDTO.getName(),
                String.valueOf(vehicleDTO.getPlateColor()));
            // 将人员身份证图片路径更新至缓存
            // String redisKey = entity.getMonitorId() + "_vehicle_list";
            // Map<String, String> vehicleCacheInfo =
            //     RedisUtil.getMapObjFromRedisByKey(redisKey, PublicVariable.REDIS_TEN_DATABASE);
            // if ("1".equals(pageSign)) {
            //     vehicleCacheInfo.put("drivingLicenseFrontPhoto", entity.getDrivingLicenseFrontPhoto());
            // } else {
            //     vehicleCacheInfo.put("drivingLicenseDuplicatePhoto", entity.getDrivingLicenseDuplicatePhoto());
            // }
            // RedisHelper.setString(redisKey, JSONObject.toJSONString(vehicleCacheInfo),
            //     PublicVariable.REDIS_TEN_DATABASE);
        }
        return result;
    }

    /**
     * 时间转换
     */
    private String ocrPhotoDateFormat(String dateStr) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            }
            Date birthDayDate = DateUtils.parseDate(dateStr, "yyyyMMdd");
            dateStr = DateFormatUtils.format(birthDayDate, "yyyy-MM-dd");
            return dateStr;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从缓存获取车辆信息
     */
    private VehicleDTO getVehicleInfo(String monitorId) throws Exception {
        if (StringUtils.isBlank(monitorId)) {
            return null;
        }
        Map<String, String> monitorMap =
            RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(monitorId), Arrays.asList("plateColor", "name"));

        return MapUtil.mapToObj(monitorMap, VehicleDTO.class);
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/getVehiclePhotoInfo")
    public JSONObject getVehiclePhotoInfo(String vehicleId) throws Exception {
        JSONObject msg = new JSONObject();
        if (StringUtils.isBlank(vehicleId)) {
            return msg;
        }
        VehicleDO vehicleDO = newVehicleDao.getById(vehicleId);
        msg.put("vehiclePhoto", vehicleDO == null ? null : vehicleDO.getVehiclePhoto());
        return msg;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/uploadVehiclePhoto")
    public boolean uploadVehiclePhotoInfo(VehiclePhotoUpLoadEntity entity)
        throws Exception {
        if (entity == null) {
            return false;
        }
        VehicleDTO vehicleDTO = getVehicleInfo(entity.getMonitorId());
        if (vehicleDTO == null) {
            return false;
        }
        String oldVehiclePhoto = entity.getOldVehiclePhoto();
        if (StringUtils.isNotBlank(oldVehiclePhoto)) {
            fastDFSClient.deleteFile(oldVehiclePhoto);
        }
        String userName = SystemHelper.getCurrentUsername();
        entity.setUpdateDataUsername(userName);
        boolean result = newVehicleDao.updateVehiclePhoto(entity);
        if (result) {
            String message = "用户: 上传车辆照片";
            logSearchService.log(message, "4", "", userName, vehicleDTO.getName(),
                String.valueOf(vehicleDTO.getPlateColor()));
        }
        return result;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE,
        url = "/clbs/app/appOcr/monitorInfo/uploadImage")
    public JSONObject uploadImage(String decodeImage) {
        JSONObject result = new JSONObject();
        Base64.Decoder encoder = Base64.getDecoder();
        final byte[] decode = encoder.decode(decodeImage);
        int imageLength = decode.length;
        InputStream inputStream = new ByteArrayInputStream(decode);
        // 生成文件名称
        String originalFilename = UUID.randomUUID().toString() + ".jpg";
        // 此处先不压缩, 压缩后的图片太小了, 展示效果不好
        String imageFilename = fastDFSClient.uploadFile(inputStream, imageLength, originalFilename);
        if (StringUtils.isEmpty(imageFilename)) {
            result.put("result", false);
        } else {
            result.put("result", true);
            result.put("imageFilename", imageFilename);
        }

        return result;
    }
}
