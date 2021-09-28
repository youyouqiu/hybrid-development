package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.reportManagement.form.DriverDiscernReportDo;
import com.zw.platform.domain.reportManagement.query.DriverDiscernStatisticsQuery;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDetailDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsExport;
import com.zw.platform.repository.modules.DriverDiscernStatisticsDao;
import com.zw.platform.service.reportManagement.DriverDiscernStatisticsService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.Translator;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.msg.t808.T808Message;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.FACE_ID_PATTERN;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.UUID_SERIALIZED_LENGTH;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/9/25
 **/
@Service
public class DriverDiscernStatisticsServiceImpl implements DriverDiscernStatisticsService {

    private static final Logger logger = LogManager.getLogger(DriverDiscernStatisticsServiceImpl.class);

    public static final Translator<String, Integer> IDENTIFICATION_RESULT = Translator
        .of("匹配成功", 0, "匹配失败", 1, "超时", 2, "没有启用该功能", 3, "连接异常", 4,
            "无指定人脸图片", 5, Translator.Pair.of("无人脸库", 6),
            Translator.Pair.of("匹配失败，人证不符", 7), Translator.Pair.of("匹配失败，比对超时", 8),
            Translator.Pair.of("匹配失败，无指定人脸信息", 9), Translator.Pair.of("无驾驶员图片", 10),
            Translator.Pair.of("终端人脸库为空", 11));

    public static final Translator<String, Integer> IDENTIFICATION_TYPE =
        Translator.of("插卡比对", 0, "巡检比对", 1, "点火比对", 2, "离开返回比对", 3, "动态对比", 4);

    //驾驶员身份识别的结果与平台类型映射
    public static final Translator<Integer, Integer> IDENTIFICATION_RESULT_SD = Translator
        .of(0, 0, 1, 1, 2, 2, 3, 3, 4, 4,
            10, 5);

    public static final Translator<Integer, Integer> IDENTIFICATION_RESULT_HN = Translator
        .of(0, 0, 1, 1, 2, 2, 3, 3, 4, 4,
            10, 5, Translator.Pair.of(11, 6));

    @Autowired
    private DriverDiscernStatisticsDao driverDiscernStatisticsDao;

    @Value("${driver.distinguish.photo.keep.days}")
    private String keepDays;

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Override
    public Page<DriverDiscernStatisticsDto> pageQuery(DriverDiscernStatisticsQuery query) throws Exception {
        if (StringUtils.isBlank(query.getMonitorIds())) {
            return new Page<>();
        }
        List<String> monitorIds = Arrays.asList(query.getMonitorIds().split(","));
        query.setIdentificationStartDate(query.getIdentificationStartDate() + " 00:00:00");
        query.setIdentificationEndDate(query.getIdentificationEndDate() + " 23:59:59");
        query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        Page<DriverDiscernStatisticsDto> result =
            PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> driverDiscernStatisticsDao.pageQuery(monitorIds, query));
        if (CollectionUtils.isEmpty(result)) {
            return new Page<>();
        }
        Set<String> driverIds = result.stream().filter(obj -> StringUtils.isNotBlank(obj.getDriverId()))
            .map(DriverDiscernStatisticsDto::getDriverId).collect(Collectors.toSet());
        Map<String, ProfessionalDO> professionalsInfoMap = new HashMap<>(driverIds.size());
        if (CollectionUtils.isNotEmpty(driverIds)) {
            List<ProfessionalDO> professionalsInfos = newProfessionalsDao.getByIds(driverIds);
            professionalsInfoMap =
                professionalsInfos.stream().collect(Collectors.toMap(ProfessionalDO::getId, Function.identity()));
        }
        Map<String, BindDTO> configMap = VehicleUtil.batchGetBindInfosByRedis(
            monitorIds, Lists.newArrayList("name", "orgName"));
        BindDTO bindDTO;
        for (DriverDiscernStatisticsDto o : result) {
            o.setIdentificationTimeStr(DateUtil.formatDate(o.getIdentificationTime(), DateUtil.DATE_FORMAT_SHORT));
            if (professionalsInfoMap.containsKey(o.getDriverId())) {
                ProfessionalDO professionalsInfo = professionalsInfoMap.get(o.getDriverId());
                o.setDriverName(professionalsInfo.getName());
                if (o.getCardNumber() == null) {
                    o.setCardNumber(professionalsInfo.getCardNumber());
                }
            }

            //faceId只有黑标的车才有值
            if (o.getFaceId() == null) {
                o.setFaceId(o.getDriverId());
            }
            //标记是否有附件
            if (o.getPhotoFlag() == 1) {
                o.setPhotoFlag((o.getImageUrl() == null  && o.getVideoUrl() == null) ? 0 : 1);
            }
            bindDTO = configMap.get(o.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            o.setMonitorName(bindDTO.getName());
            o.setOrgName(bindDTO.getOrgName());
        }
        return result;
    }

    @Override
    public JSONObject getMediaInfo(String id) {
        JSONObject re = new JSONObject();
        DriverDiscernReportDo driverDiscernReportDo = driverDiscernStatisticsDao.getById(id);
        JSONObject imageInfo = new JSONObject();
        List<String> imageList = new ArrayList<>();
        if (driverDiscernReportDo.getImageUrl() != null) {
            imageList = Arrays.asList(driverDiscernReportDo.getImageUrl().split(","));
            imageList = imageList.stream().map(this::getFilePath).collect(Collectors.toList());
        }
        imageInfo.put("imageList", imageList);
        imageInfo.put("count", imageList.size());


        JSONObject videoInfo = new JSONObject();
        List<String> videoList = new ArrayList<>();
        if (driverDiscernReportDo.getVideoUrl() != null) {
            videoList = Arrays.asList(driverDiscernReportDo.getVideoUrl().split(","));
            videoList = videoList.stream().map(this::getFilePath).collect(Collectors.toList());
        }
        videoInfo.put("videoList", videoList);
        videoInfo.put("count", videoList.size());

        re.put("imageInfo", imageInfo);
        re.put("videoInfo", videoInfo);

        return re;
    }

    @Override
    public List<DriverDiscernStatisticsDetailDto> detail(String monitorId, String time) {
        List<DriverDiscernStatisticsDetailDto> detail = driverDiscernStatisticsDao.detail(monitorId, time);
        if (CollectionUtils.isEmpty(detail)) {
            return detail;
        }

        Set<String> driverIds =
            detail.stream().map(DriverDiscernStatisticsDetailDto::getDriverId).collect(Collectors.toSet());
        Map<String, ProfessionalDO> professionalsInfoMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(driverIds)) {
            List<ProfessionalDO> professionalsInfos = newProfessionalsDao.getByIds(driverIds);
            professionalsInfoMap =
                professionalsInfos.stream().collect(Collectors.toMap(ProfessionalDO::getId, Function.identity()));
        }
        Map<String, BindDTO> configMap = VehicleUtil.batchGetBindInfosByRedis(
            Lists.newArrayList(monitorId), Lists.newArrayList("name", "orgName"));
        BindDTO bindDTO = configMap.get(monitorId);
        for (DriverDiscernStatisticsDetailDto o : detail) {
            if (o.getPhotoFlag() == 0) {
                // 照片已删除
                o.setImageUrl(null);
            }
            if (StringUtils.isNotBlank(o.getImageUrl())) {
                String url = o.getImageUrl().split(",")[0];
                o.setImageUrl(getFilePath(url));
            }
            o.setIdentificationTimeStr(DateUtil.formatDate(o.getIdentificationTime(), DateUtil.DATE_FORMAT_SHORT));
            if (professionalsInfoMap.containsKey(o.getDriverId())) {
                ProfessionalDO professionalsInfo = professionalsInfoMap.get(o.getDriverId());
                o.setDriverName(professionalsInfo.getName());
                if (o.getCardNumber() == null) {
                    o.setCardNumber(professionalsInfo.getCardNumber());
                }

                if (StringUtils.isNotBlank(professionalsInfo.getPhotograph())) {
                    if (sslEnabled) {
                        mediaServer = "/mediaserver";
                    }
                    o.setDriverPhotoUrl(mediaServer + professionalFtpPath + professionalsInfo.getPhotograph());
                }
            }
            //faceId只有黑标的车才有值
            if (o.getFaceId() == null) {
                o.setFaceId(o.getDriverId());
            }
            if (bindDTO == null) {
                continue;
            }
            o.setMonitorName(bindDTO.getName());
            o.setOrgName(bindDTO.getOrgName());
        }
        return detail;
    }

    private String getFilePath(String oldPath) {
        String path;
        if (sslEnabled) {
            path = "/" + oldPath;
        } else {
            path = fastDFSClient.getWebAccessUrl(oldPath);
        }
        return path;
    }

    @Override
    public void export(HttpServletResponse response, DriverDiscernStatisticsQuery query) throws IOException {
        List<DriverDiscernStatisticsExport> exportData = new ArrayList<>();
        List<String> monitorIds = Arrays.asList(query.getMonitorIds().split(","));
        query.setIdentificationStartDate(query.getIdentificationStartDate() + " 00:00:00");
        query.setIdentificationEndDate(query.getIdentificationEndDate() + " 23:59:59");
        query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        List<DriverDiscernStatisticsDto> result = driverDiscernStatisticsDao.pageQuery(monitorIds, query);
        if (CollectionUtils.isNotEmpty(result)) {
            Set<String> driverIds = result.stream().filter(obj -> StringUtils.isNotBlank(obj.getDriverId()))
                .map(DriverDiscernStatisticsDto::getDriverId).collect(Collectors.toSet());
            Map<String, ProfessionalDO> professionalsInfoMap = new HashMap<>(driverIds.size());
            if (CollectionUtils.isNotEmpty(driverIds)) {
                List<ProfessionalDO> professionalsInfos = newProfessionalsDao.getByIds(driverIds);
                professionalsInfoMap = professionalsInfos.stream()
                    .collect(Collectors.toMap(ProfessionalDO::getId, Function.identity()));
            }
            Map<String, BindDTO> configMap = VehicleUtil.batchGetBindInfosByRedis(
                monitorIds, Lists.newArrayList("name", "orgName"));
            BindDTO bindDTO;
            for (DriverDiscernStatisticsDto dto : result) {
                DriverDiscernStatisticsExport export = new DriverDiscernStatisticsExport();
                BeanUtils.copyProperties(dto, export);
                export.setIdentificationTimeStr(DateUtil.formatDate(dto.getIdentificationTime(),
                    DateUtil.DATE_FORMAT_SHORT));
                export.setIdentificationResultStr(IDENTIFICATION_RESULT.p2b(dto.getIdentificationResult()));
                export.setMatchRateStr(StringUtils.isNotBlank(dto.getMatchRate()) ? dto.getMatchRate() + "%" : "");
                export.setMatchThresholdStr(
                    Objects.nonNull(dto.getMatchThreshold()) ? dto.getMatchThreshold() + "%" : "");
                export.setIdentificationTypeStr(IDENTIFICATION_TYPE.p2b(dto.getIdentificationType()));
                if (professionalsInfoMap.containsKey(dto.getDriverId())) {
                    ProfessionalDO professionalsInfo = professionalsInfoMap.get(dto.getDriverId());
                    export.setDriverName(professionalsInfo.getName());
                    if (export.getCardNumber() == null) {
                        export.setCardNumber(professionalsInfo.getCardNumber());
                    }
                }
                //faceId只有黑标的车才有值
                if (export.getFaceId() == null) {
                    export.setFaceId(export.getDriverId());
                }
                exportData.add(export);
                bindDTO = configMap.get(dto.getMonitorId());
                if (bindDTO == null) {
                    continue;
                }
                export.setMonitorName(bindDTO.getName());
                export.setOrgName(bindDTO.getOrgName());
            }
        }
        ExportExcelUtil.setResponseHead(response, "终端驾驶员识别统计");
        ExportExcelUtil.export(new ExportExcelParam(null, 1, exportData, DriverDiscernStatisticsExport.class, null,
            response.getOutputStream()));
    }

    @Override
    public void saveReportHandle(Message message) throws IOException {
        MsgDesc desc = message.getDesc();
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        JSONObject body = (JSONObject) t808Message.getMsgBody();
        DriverDiscernReportDo reportDo = new DriverDiscernReportDo();
        reportDo.setMonitorId(desc.getMonitorId());
        //终端人脸ID转回CLBS人脸ID格式(UUID)
        String faceId = body.getString("faceId");
        Integer result = body.getInteger("result");
        String protocol = t808Message.getMsgHead().getDeviceType();
        if (Objects.equals(protocol, ProtocolEnum.T808_2019_SD.getDeviceType()) || Objects
            .equals(protocol, ProtocolEnum.T808_2013_HN.getDeviceType())) {
            //将结果与平台的终端驾驶员识别报表对应
            result = protocol.equals(ProtocolEnum.T808_2019_SD.getDeviceType())
                ? IDENTIFICATION_RESULT_SD.p2b(result) : IDENTIFICATION_RESULT_HN.p2b(result);
            try {
                String [] driverInfos = faceId.split("_");
                String driverId = newProfessionalsDao
                    .getProByCardNumberAndCreateTime(driverInfos[0],
                        DateUtil.getStringToDate(driverInfos[1], DateUtil.DATE_YYMMDDHHMMSS));
                reportDo.setDriverId(driverId);
            } catch (Exception e) {
                logger.error("0E10根据人脸id获取平台从业人员id异常", e);
            }
        } else {
            if (faceId.length() == UUID_SERIALIZED_LENGTH && FACE_ID_PATTERN.matcher(faceId).matches()) {
                faceId = faceId.substring(0, 8) + "-" + faceId.substring(8, 12) + "-" + faceId.substring(12, 16)
                    + "-" + faceId.substring(16, 20) + "-" + faceId.substring(20);
                reportDo.setDriverId(faceId);
            }
        }
        reportDo.setFaceId(faceId);
        reportDo.setIdentificationResult(result);
        reportDo.setMatchThreshold(body.getInteger("similarityValue"));
        reportDo.setMatchRate(body.getString("similarity"));
        reportDo.setIdentificationType(body.getInteger("type"));

        JSONObject gpsInfo = body.getJSONObject("gpsInfo");
        reportDo.setLatitude(gpsInfo.getString("latitude"));
        reportDo.setLongitude(gpsInfo.getString("longitude"));
        if (StringUtils.isNotBlank(gpsInfo.getString("time"))) {
            reportDo
                .setIdentificationTime(DateUtil.getStringToDate(gpsInfo.getString("time"), DateUtil.DATE_YYMMDDHHMMSS));
        }

        byte[] photoData = body.getBytes("photoData");
        if (Objects.nonNull(photoData) && photoData.length > 0) {
            ByteArrayInputStream inputStream = null;
            try {
                inputStream = new ByteArrayInputStream(photoData);
                String name = UUID.randomUUID().toString() + ".jpeg";
                String url = fastDFSClient.uploadFile(inputStream, photoData.length, name);
                reportDo.setImageUrl(url);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        driverDiscernStatisticsDao.save(reportDo);
    }

    @Override
    public void deletePhoto() {
        long minusDay = 7;
        if (StringUtils.isNotBlank(keepDays)) {
            minusDay = Long.parseLong(keepDays);
        }
        LocalDateTime dateTime = LocalDateTime.now().minusDays(minusDay);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        // 查找这个时间之前为被删掉的数据  photo_flag = 1
        List<DriverDiscernReportDo> deleteData = driverDiscernStatisticsDao.findDeleteData(date);
        if (CollectionUtils.isNotEmpty(deleteData)) {
            Set<String> ids = deleteData.stream().map(DriverDiscernReportDo::getId).collect(Collectors.toSet());
            Set<String> urls = new HashSet<>();
            for (DriverDiscernReportDo driverDiscernReportDo : deleteData) {
                if (driverDiscernReportDo.getImageUrl() != null) {
                    urls.addAll(Arrays.asList(driverDiscernReportDo.getImageUrl().split(",")));
                }
                if (driverDiscernReportDo.getVideoUrl() != null) {
                    urls.addAll(Arrays.asList(driverDiscernReportDo.getVideoUrl().split(",")));
                }
            }

            if (CollectionUtils.isNotEmpty(urls)) {
                urls.forEach(o -> fastDFSClient.deleteFile(o));
            }
            driverDiscernStatisticsDao.delete(ids);
        }
    }
}
