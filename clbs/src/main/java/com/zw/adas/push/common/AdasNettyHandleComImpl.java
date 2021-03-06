package com.zw.adas.push.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.zw.adas.domain.riskManagement.AdasPicPostprocessInfo;
import com.zw.adas.domain.riskManagement.AdasRiskEventInfo;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.form.AdasMediaFlagForm;
import com.zw.adas.domain.riskManagement.form.AdasMediaForm;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.reportManagement.AdasMediaService;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.platformInspection.PlatformInspectionResultDO;
import com.zw.platform.domain.reportManagement.T809AlarmFileListAck;
import com.zw.platform.domain.reportManagement.WarnMsgFileInfo;
import com.zw.platform.domain.reportManagement.form.DriverDiscernReportDo;
import com.zw.platform.push.common.AdasNettyHandleCom;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.repository.modules.DriverDiscernStatisticsDao;
import com.zw.platform.repository.modules.PlatformInspectionResultDao;
import com.zw.platform.service.connectionparamsset_809.impl.ConnectionParamsSetServiceImpl;
import com.zw.platform.service.platformInspection.impl.PlatformInspectionServiceImpl;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdasNettyHandleComImpl implements AdasNettyHandleCom {

    private static final Logger log = LogManager.getLogger(AdasNettyHandleComImpl.class);

    @Value("${unknown.location}")
    private String unknownLocation;

    @Value("${no.location}")
    private String noLocation;

    @Autowired
    private AdasMediaService adasMediaService;

    @Autowired
    AdasElasticSearchService adasElasticSearchService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    AdasSubcibeTable adasSubcibeTable;

    @Autowired
    ConnectionParamsSetServiceImpl connectionParamsSetService;

    @Autowired
    ServerParamList serverParamList;

    @Autowired
    DriverDiscernStatisticsDao driverDiscernStatisticsDao;

    @Autowired
    PlatformInspectionResultDao platformInspectionResultDao;


    @Value("${zmqConfig.adas.ringBufferSize}")
    private Integer bufSize;

    private Disruptor<NettyMessage> disruptor;

    @Value("${zmqConfig.adas.threadSum}")
    private Integer threadSum;

    @Value("${netty.1208.batch.number}")
    private Integer batchNumber;

    @Value("${netty.1208.batch.time}")
    private Integer timeOut;

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.path}")
    private String ftpPath;

    private static final String[] validResourceExtensions = {
        "mp4", "MP4", "h264", "H264", "avi", "AVI", //video
        "jpg", "jpeg", "png", "JPG", "PNG", //picture
        "wav", "mp3", "WAV", "MP3", //audio
        "bin", "BIN" //raw
    };

    /**
     * ??????1208??????
     */
    @Override
    public void deal1208Message(Message message) {
        if (disruptor != null) {
            disruptor.publishEvent(new Netty1208EventProducer(message, true));
        }
    }

    /**
     * ??????????????????9212??????
     */
    @Override
    public void deal9212Message(Message message) {
        if (disruptor != null) {
            disruptor.publishEvent(new Netty1208EventProducer(message, false));
        }
    }

    @PostConstruct
    public void init() {
        int ringBufferSize = 2;
        while (ringBufferSize < bufSize) {
            ringBufferSize <<= 1;
        }
        disruptor = new Disruptor<>(() -> new NettyMessage(new Message(), true), ringBufferSize,
            new AdasNettyMessageThreadFactory(), ProducerType.SINGLE, new BlockingWaitStrategy());
        AdasEventHandler[] adasEventHandlers = new AdasEventHandler[threadSum];
        for (int i = 0; i < threadSum; i++) {
            adasEventHandlers[i] = new AdasEventHandler();
        }
        disruptor.handleEventsWithWorkerPool(adasEventHandlers);
        disruptor.setDefaultExceptionHandler(new DisruptorExceptionHandler());
        disruptor.start();
        log.info("1208disruptor is start!");
    }

    @PreDestroy
    public void close() {
        this.disruptor.shutdown();
    }

    private static class AdasNettyMessageThreadFactory implements ThreadFactory {
        private final AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "AdasNettyMessageHandler-" + this.cnt.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }

    //disruptor???????????????????????????
    private class AdasEventHandler implements WorkHandler<NettyMessage>, EventHandler<NettyMessage> {

        //????????????????????????media??????
        private Set<AdasMediaForm> mediaForms = new HashSet<>();

        //?????????????????????????????????riskId
        private Set<AdasMediaFlagForm> adasRiskFlagForm = new HashSet<>();

        //?????????????????????????????????eventId
        private Set<AdasMediaFlagForm> adasEventFlagForm = new HashSet<>();

        private long time = System.currentTimeMillis();

        @Override
        public void onEvent(NettyMessage event, long sequence, boolean endOfBatch) {
            onEvent(event);
        }

        @Override
        public void onEvent(NettyMessage event) {
            if (event.isFtp) {
                dealFtp1208(event);
            } else {
                dealFile9212(event);
            }
            //?????????
            batchDeal();
        }

        private void dealFtp1208(NettyMessage event) {
            Message m = event.message;
            JSONObject msgBody = JSON.parseObject(m.getData().toString()).getJSONObject("msgBody");
            MsgDesc desc = m.getDesc();
            String vehicleId = desc.getMonitorId(); // ????????????id
            String brand = desc.getMonitorName();
            String deviceId = desc.getDeviceId(); // ??????id
            try {
                // ?????????
                String msgSNACK = msgBody.getString("msgSNACK");
                Pattern pattern = Pattern.compile("(^[0-9a-zA-Z.]+)$");
                // ????????????
                SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSNACK, deviceId);
                if (StringUtils.isBlank(msgSNACK) || StringUtils.isBlank(vehicleId) || !msgBody.containsKey("result")
                    || !"0".equals(msgBody.getInteger("result").toString()) || !msgBody.containsKey("fileName")
                    || !pattern.matcher(msgBody.getString("fileName")).matches()) {
                    log.error("?????????{}??????????????????????????????", brand);
                    return;
                }
                // ???????????????????????????
                String fileName = msgBody.getString("fileName"); // ??????????????????
                long fileSize = msgBody.getLong("fileSize"); // ??????????????????
                String riskEvents = info == null ? null : info.getAdasRiskInfos();
                if (riskEvents == null || riskEvents.equals("")) {
                    log.error("?????????????????????{} ????????????{}??????????????????", brand, fileName);
                    return;
                }
                JSONArray jsonArray = JSON.parseArray(riskEvents);
                for (Object object : jsonArray) {
                    AdasRiskEventInfo riskEventInfo =
                        JSON.parseObject(JSON.toJSONString(object), AdasRiskEventInfo.class);
                    AdasMediaForm mediaForm = setMediaForm(riskEventInfo, fileName, vehicleId);
                    mediaForm.setFileSize(fileSize);
                    if (StringUtils.isEmpty(mediaForm.getRiskNumber())) {
                        //???????????????
                        return;
                    }
                    mediaForms.add(mediaForm);
                    final byte[] riskId = UuidUtils.getBytesFromStr(riskEventInfo.getRiskId());
                    final byte[] riskEventId = UuidUtils.getBytesFromStr(riskEventInfo.getRiskEventId());
                    if (riskEventInfo.getMediaType() == 0) {
                        adasRiskFlagForm.add(new AdasMediaFlagForm(riskId, 1, null));
                        adasEventFlagForm.add(new AdasMediaFlagForm(riskEventId, 1, null));
                    } else {
                        adasRiskFlagForm.add(new AdasMediaFlagForm(riskId, null, 1));
                        adasEventFlagForm.add(new AdasMediaFlagForm(riskEventId, null, 1));
                    }

                    log.info(">========??????0x1208=========<?????????{} ????????????{} ???????????????{} ???????????????{}", brand,
                        fileName, riskEventInfo.getEventNumber(), new Date(riskEventInfo.getWarmTime()));
                }
                SubscibeInfoCache.getInstance().delTable(info);
            } catch (Exception e) {
                log.error("??????0x1208????????????", e);
            }
        }

        private void dealFile9212(NettyMessage event) {
            Message m = event.message;
            JSONObject msgBody = JSON.parseObject(m.getData().toString()).getJSONObject("msgBody");
            try {
                if (!msgBody.containsKey("result") || !"0".equals(msgBody.getInteger("result").toString())
                    || !msgBody.containsKey("fileName") || !checkFileName(msgBody.getString("resourceUrl"))) {
                    log.info("???????????????????????????");
                    return;
                }
                String alarmNumber = msgBody.getString("alarmNumber");
                Integer mediaType = msgBody.getInteger("fileType");
                String fileName = msgBody.getString("fileName");
                String filePath = msgBody.getString("resourceUrl");
                Long fileSize = msgBody.getLong("fileSize");
                AdasRiskEventInfo adasRiskEventInfo = (AdasRiskEventInfo) adasSubcibeTable.get(alarmNumber);
                if (adasRiskEventInfo == null) {
                    adasRiskEventInfo = (AdasRiskEventInfo) adasSubcibeTable.get(alarmNumber + "_manual");
                }

                if (adasRiskEventInfo == null) {
                    log.info("???????????????????????????????????????");
                    return;
                }

                //??????????????????????????????????????????
                if (adasRiskEventInfo.isInspection()) {
                    dealInspection(adasRiskEventInfo, filePath, mediaType);
                    log.info(">========????????????????????????0x9212=========" + "???????????????"
                        + adasRiskEventInfo.getInspectionType() + "????????????id" + adasRiskEventInfo.getInspectionResultId());
                    return;
                }

                //???????????????1407???
                setGui1407(adasRiskEventInfo, fileName, filePath, fileSize, mediaType);

                //??????adas??????????????????
                dealAdasMedia9212(adasRiskEventInfo, alarmNumber, fileName, filePath, fileSize, mediaType);

            } catch (Exception e) {
                log.error("??????0x9212????????????", e);
            }
        }

        private void setGui1407(AdasRiskEventInfo adasRiskEventInfo, String fileName, String filePath, Long fileSize,
            Integer mediaType) {
            if (adasRiskEventInfo.getProtocolType() == Integer.parseInt(ProtocolEnum.ADAS_GL_2013.getDeviceType())) {
                WarnMsgFileInfo warnMsgFileInfo = new WarnMsgFileInfo();
                warnMsgFileInfo.setFileName(fileName);
                warnMsgFileInfo.setFileNameLength(fileName.getBytes(Charset.forName("GBK")).length);
                warnMsgFileInfo.setFileUrl(webServerUrl + filePath + "?token=" + adasRiskEventInfo.getToken());
                warnMsgFileInfo.setFileUrlLengh(warnMsgFileInfo.getFileUrl().getBytes(Charset.forName("GBK")).length);
                warnMsgFileInfo.setFileType(mediaType);
                warnMsgFileInfo.setFileSize(fileSize);
                adasRiskEventInfo.getFileList().add(warnMsgFileInfo);
                T809AlarmFileListAck fileListAck = new T809AlarmFileListAck();
                fileListAck.setServer(ftpHost);
                fileListAck.setServerLength(ftpHost.length());
                fileListAck.setPort(ftpPort);
                fileListAck.setUserName(ftpUserName);
                fileListAck.setUserNameLength(ftpUserName.length());
                fileListAck.setPassword(ftpPassword);
                fileListAck.setPasswordLength(ftpPassword.length());
                adasRiskEventInfo.setFileListAck(fileListAck);
            }
        }

        private void dealAdasMedia9212(AdasRiskEventInfo adasRiskEventInfo, String alarmNumber, String fileName,
            String filePath, Long fileSize, Integer mediaType) {
            AdasMediaForm mediaForm = setMediaForm(adasRiskEventInfo, fileName, filePath, mediaType);
            mediaForm.setFileSize(fileSize);
            if (StringUtils.isEmpty(mediaForm.getRiskNumber())) {
                //???????????????
                return;
            }
            mediaForms.add(mediaForm);
            final byte[] riskId = UuidUtils.getBytesFromStr(adasRiskEventInfo.getRiskId());
            final byte[] riskEventId = UuidUtils.getBytesFromStr(adasRiskEventInfo.getRiskEventId());
            if (mediaType == 0) {
                adasRiskFlagForm.add(new AdasMediaFlagForm(riskId, 1, null));
                adasEventFlagForm.add(new AdasMediaFlagForm(riskEventId, 1, null));
            } else if (mediaType == 2) {
                adasRiskFlagForm.add(new AdasMediaFlagForm(riskId, null, 1));
                adasEventFlagForm.add(new AdasMediaFlagForm(riskEventId, null, 1));
            }

            //????????????????????????????????????????????????????????????????????????????????????
            Integer mediaCount = adasRiskEventInfo.getMediaCount();
            if (mediaCount > 1) {
                adasRiskEventInfo.setMediaCount(mediaCount - 1);
            } else {
                adasSubcibeTable.remove(alarmNumber);
                adasSubcibeTable.remove(alarmNumber + "_manual");
            }
            log.info(">========??????0x9212=========<?????????{} ????????????{} ???????????????{} ???????????????{}",
                adasRiskEventInfo.getBrand(), fileName, adasRiskEventInfo.getEventNumber(),
                new Date(adasRiskEventInfo.getWarmTime()));
        }

        /**
         * ???????????????????????????????????????
         * @param adasRiskEventInfo
         * @param filePath
         * @param mediaType
         */
        private void dealInspection(AdasRiskEventInfo adasRiskEventInfo, String filePath, Integer mediaType) {
            String inspectionResultId = adasRiskEventInfo.getInspectionResultId();
            if (adasRiskEventInfo.getInspectionType().equals(PlatformInspectionServiceImpl.IDENTIFY_INSPECTION)) {
                DriverDiscernReportDo driverDiscernReportDo =
                    driverDiscernStatisticsDao.getById(inspectionResultId);
                if (mediaType == 0) {
                    String oldImageUrl = driverDiscernReportDo.getImageUrl() == null
                        ? "" : driverDiscernReportDo.getImageUrl();
                    String imageUrl = oldImageUrl + filePath + ",";
                    driverDiscernStatisticsDao.setImageUrl(inspectionResultId, imageUrl);
                } else if (mediaType == 2) {
                    String oldVideoUrl = driverDiscernReportDo.getVideoUrl() == null
                        ? "" : driverDiscernReportDo.getVideoUrl();
                    String videoUrl = oldVideoUrl + filePath + ",";
                    driverDiscernStatisticsDao.setVideoUrl(inspectionResultId, videoUrl);
                }
            } else {
                PlatformInspectionResultDO platformInspectionResultDO
                    = platformInspectionResultDao.getById(inspectionResultId);
                if (mediaType == 0) {
                    String oldImageUrl = platformInspectionResultDO.getImageUrl() == null
                        ? "" : platformInspectionResultDO.getImageUrl();
                    String imageUrl = oldImageUrl + filePath + ",";
                    platformInspectionResultDao.setImageUrl(inspectionResultId, imageUrl);
                } else if (mediaType == 2) {
                    String oldVideoUrl = platformInspectionResultDO.getVideoUrl() == null
                        ? "" : platformInspectionResultDO.getVideoUrl();
                    String videoUrl = oldVideoUrl + filePath + ",";
                    platformInspectionResultDao.setVideoUrl(inspectionResultId, videoUrl);
                }
            }
        }

        private boolean checkFileName(String resourceUrl) {
            return resourceUrl != null && StringUtils.endsWithAny(resourceUrl, validResourceExtensions);
        }

        private void batchDeal() {
            long now = System.currentTimeMillis();
            if ((mediaForms.size() >= batchNumber || (now - time) >= timeOut) && !mediaForms.isEmpty()) {
                BatchDealNetty batchDealNetty = new BatchDealNetty(mediaForms, adasRiskFlagForm, adasEventFlagForm);
                mediaForms = new HashSet<>();
                adasRiskFlagForm = new HashSet<>();
                adasEventFlagForm = new HashSet<>();
                taskExecutor.execute(() -> {
                    Future<?> future = taskExecutor.submit(batchDealNetty);
                    try {
                        future.get(5000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        //????????????????????????????????????cancel??????????????????
                        log.info("??????????????????adas?????????????????????????????????", e);
                        future.cancel(true);
                    } catch (Exception e) {
                        log.error("??????????????????adas???????????????????????????????????????", e);
                    }
                });
            }
            time = now;
        }

        /**
         * ????????????????????????????????????
         */
        private AdasMediaForm setMediaForm(AdasRiskEventInfo riskEventInfo, String fileName, String vehicleId) {
            AdasMediaForm mediaForm = new AdasMediaForm();
            mediaForm.setProtocolType(riskEventInfo.getProtocolType());//????????????
            mediaForm.setType(riskEventInfo.getMediaType()); // ?????????????????????
            mediaForm.setMediaName(fileName);
            mediaForm.setMediaUrl(riskEventInfo.getResultPath() + mediaForm.getMediaName());
            mediaForm.setFlag(1);
            mediaForm.setRiskEventId(riskEventInfo.getRiskEventId());
            mediaForm.setVehicleId(vehicleId);
            mediaForm.setRiskId(riskEventInfo.getRiskId());
            mediaForm.setEventCode(0);
            mediaForm.setSource((short) 0);
            mediaForm.setCreateDataTime(new Date());
            mediaForm.setIdStr(UuidUtils.getBytesFromStr(mediaForm.getId()));
            mediaForm.setRiskTime(riskEventInfo.getWarmTime());
            mediaForm.setRiskType(riskEventInfo.getRiskType());
            mediaForm.setRiskLevel(riskEventInfo.getRiskLevel());
            mediaForm.setDriverName(riskEventInfo.getDriver());
            mediaForm.setBrand(riskEventInfo.getBrand());
            mediaForm.setRiskNumber(riskEventInfo.getRiskNumber());
            mediaForm.setEventId(riskEventInfo.getEventId());
            mediaForm.setEventNumber(riskEventInfo.getEventNumber());
            //???es???????????????
            AdasRiskEsBean riskEsBean = adasElasticSearchService.esGetRiskById(riskEventInfo.getRiskId());
            if (riskEsBean != null) {
                mediaForm.setRiskResult(riskEsBean.getRiskResult());
            }
            final String address = riskEventInfo.getAddress();
            mediaForm.setAddress(StringUtils.isEmpty(address)
                    || noLocation.equals(address) || unknownLocation.equals(address)
                    ? riskEventInfo.getLongitude() + ", " + riskEventInfo.getLatitude()
                    : address);
            mediaForm.setDirection(riskEventInfo.getDirection());
            return mediaForm;
        }

        /**
         * ????????????????????????????????????
         */
        private AdasMediaForm setMediaForm(AdasRiskEventInfo riskEventInfo, String fileName, String filePath,
            Integer mediaType) {
            AdasMediaForm mediaForm = new AdasMediaForm();
            mediaForm.setProtocolType(riskEventInfo.getProtocolType());//????????????
            mediaForm.setType(mediaType); // ?????????????????????
            mediaForm.setMediaName(fileName);
            mediaForm.setMediaUrl(filePath);
            mediaForm.setFlag(1);
            mediaForm.setRiskEventId(riskEventInfo.getRiskEventId());
            mediaForm.setVehicleId(riskEventInfo.getVehicleId());
            mediaForm.setRiskId(riskEventInfo.getRiskId());
            mediaForm.setEventCode(0);
            mediaForm.setSource((short) 0);
            mediaForm.setCreateDataTime(new Date());
            mediaForm.setIdStr(UuidUtils.getBytesFromStr(mediaForm.getId()));
            mediaForm.setRiskTime(riskEventInfo.getWarmTime());
            mediaForm.setRiskType(riskEventInfo.getRiskType());
            mediaForm.setRiskLevel(riskEventInfo.getRiskLevel());
            mediaForm.setDriverName(riskEventInfo.getDriver());
            mediaForm.setBrand(riskEventInfo.getBrand());
            mediaForm.setRiskNumber(riskEventInfo.getRiskNumber());
            mediaForm.setEventId(riskEventInfo.getEventId());
            mediaForm.setEventNumber(riskEventInfo.getEventNumber());
            //???es???????????????
            AdasRiskEventEsBean adasRiskEventEsBean =
                adasElasticSearchService.esGetRiskEventById(riskEventInfo.getRiskEventId());
            if (adasRiskEventEsBean != null) {
                mediaForm.setRiskResult(adasRiskEventEsBean.getRiskResult());
                mediaForm.setStatus(adasRiskEventEsBean.getStatus());
            }
            final String address = riskEventInfo.getAddress();
            mediaForm.setAddress(StringUtils.isEmpty(address)
                    || noLocation.equals(address) || unknownLocation.equals(address)
                    ? riskEventInfo.getLongitude() + ", " + riskEventInfo.getLatitude()
                    : address);
            mediaForm.setDirection(riskEventInfo.getDirection());
            return mediaForm;
        }
    }


    private static class NettyMessage {
        private Message message;

        private boolean isFtp;

        NettyMessage(Message message, boolean isFtp) {
            this.message = message;
            this.isFtp = isFtp;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public void setFtp(boolean ftp) {
            this.isFtp = ftp;
        }
    }


    //??????
    @Data
    private static class Netty1208EventProducer implements EventTranslator<NettyMessage> {
        private Message message;

        private boolean isFtp;

        Netty1208EventProducer(Message message, boolean isFtp) {
            this.message = message;
            this.isFtp = isFtp;
        }

        @Override
        public void translateTo(NettyMessage event, long sequence) {
            event.setMessage(message);
            event.setFtp(isFtp);
        }
    }


    //?????????1208?????????Future????????????
    private class BatchDealNetty implements Runnable {

        private final Set<AdasMediaForm> mediaForms;

        private final Set<AdasMediaFlagForm> adasRiskFlagForm;

        private final Set<AdasMediaFlagForm> adasEventFlagForm;

        BatchDealNetty(Set<AdasMediaForm> mediaForms, Set<AdasMediaFlagForm> adasRiskFlagForm,
            Set<AdasMediaFlagForm> adasEventFlagForm) {
            this.mediaForms = mediaForms;
            this.adasEventFlagForm = adasEventFlagForm;
            this.adasRiskFlagForm = adasRiskFlagForm;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         */
        @Override
        public void run() {
            try {
                long time = System.currentTimeMillis();
                adasMediaService.addMediaHbaseAndEsBatch(mediaForms);
                adasMediaService.updateRiskMediaFlagBatch(adasRiskFlagForm);
                adasMediaService.updateEventMediaFlagBatch(adasEventFlagForm);
                log.info("??????????????????adas?????????????????????????????????{}????????????!  ?????????{}",  mediaForms.size(),
                    (System.currentTimeMillis() - time));
                this.putToPicPostprocessQueue();
            } catch (Exception e) {
                log.error("??????????????????adas?????????????????????????????????hbase??????!", e);
            }
        }

        private void putToPicPostprocessQueue() {
            final Set<String> formVehicleIds = mediaForms.stream()
                    .filter(o -> o.getType() == 0)
                    .map(AdasMediaForm::getVehicleId)
                    .collect(Collectors.toSet());
            final Set<String> monitorIds =
                    RedisHelper.filterSetMembers(RedisKeyEnum.ADAS_PIC_POSTPROCESS_ID.of(), formVehicleIds);
            if (CollectionUtils.isNotEmpty(monitorIds)) {
                final Map<String, BindDTO> dtoMap =
                        MonitorUtils.getBindDTOMap(monitorIds, "id", "terminalType", "deviceNumber");
                final List<String> queueElements = mediaForms.stream()
                        .filter(o -> monitorIds.contains(o.getVehicleId()) && dtoMap.containsKey(o.getVehicleId()))
                        .map(o -> {
                            final BindDTO bindDTO = dtoMap.get(o.getVehicleId());
                            return new AdasPicPostprocessInfo(o.getMediaUrl(), o.getAddress(), o.getBrand(),
                                    o.getDirection(), bindDTO.getTerminalType(), bindDTO.getDeviceNumber());
                        }).map(JSON::toJSONString)
                        .collect(Collectors.toList());
                RedisHelper.addToListTail(RedisKeyEnum.ADAS_PIC_POSTPROCESS_LIST.of(), queueElements);
            }
        }
    }
}
