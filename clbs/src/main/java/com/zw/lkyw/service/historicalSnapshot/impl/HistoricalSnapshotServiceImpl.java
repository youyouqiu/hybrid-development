package com.zw.lkyw.service.historicalSnapshot.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotInfo;
import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotMapData;
import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotQuery;
import com.zw.lkyw.repository.mysql.historicalSnapshot.HistoricalSnapshotDao;
import com.zw.lkyw.service.historicalSnapshot.HistoricalSnapshotService;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.service.multimedia.MultimediaService;
import com.zw.platform.service.oil.impl.PositionalServiceImpl;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/***
 * 两客一危历史抓拍
 * @author lijie
 * @since 2020/1/6 11:57
 * @version 1.0
 **/
@Service
public class HistoricalSnapshotServiceImpl implements HistoricalSnapshotService {

    @Autowired
    private HistoricalSnapshotDao historicalSnapshotDao;

    @Autowired
    private PositionalServiceImpl positionalService;

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    private MultimediaService multimediaService;

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    private static final String mediaUrl = "/clbs/resources/img/media";

    @Override
    public JsonResultBean getHistoricalSnapshot(HistoricalSnapshotQuery historicalSnapshotQuery) {
        historicalSnapshotQuery.setPage(historicalSnapshotQuery.getPageNum().longValue());
        historicalSnapshotQuery.setLimit(historicalSnapshotQuery.getPageSize().longValue());
        String[] vehicleIds = historicalSnapshotQuery.getVehicleIds().split(",");
        List<String> vids = Arrays.asList(vehicleIds);
        historicalSnapshotQuery.setVids(vids);
        Page<HistoricalSnapshotInfo> list = PageHelperUtil.doSelect(historicalSnapshotQuery,
            () -> historicalSnapshotDao.getHistoricalSnapshot(historicalSnapshotQuery));
        String path;
        for (HistoricalSnapshotInfo historicalSnapshotInfo : list) {
            historicalSnapshotInfo.setAddress(positionalService
                .getAddress(historicalSnapshotInfo.getLongitude(), historicalSnapshotInfo.getLatitude()));
            path = historicalSnapshotInfo.getMediaUrlNew();
            if (path != null && !path.equals("")) {
                if (sslEnabled) {
                    webServerUrl = "/";
                }
                historicalSnapshotInfo.setMediaUrlNew(path.startsWith("/") ? mediaUrl + path : webServerUrl + path);
            }
        }

        PageGridBean result = new PageGridBean(historicalSnapshotQuery, list, true);
        return new JsonResultBean(result);
    }

    @Override
    public JsonResultBean getMediaMapData(HistoricalSnapshotQuery historicalSnapshotQuery) {
        JSONObject re = new JSONObject();
        String[] vehicleIds = historicalSnapshotQuery.getVehicleIds().split(",");
        List<String> vids = Arrays.asList(vehicleIds);
        historicalSnapshotQuery.setVids(vids);
        Set<HistoricalSnapshotMapData> historicalSnapshotMapData =
            historicalSnapshotDao.getHistoricalSnapshotOfMap(historicalSnapshotQuery);
        re.put("data", historicalSnapshotMapData);
        return new JsonResultBean(re);
    }

    @Override
    public JsonResultBean getMediaMapDataDetail(HistoricalSnapshotQuery historicalSnapshotQuery) {
        JSONObject re = new JSONObject();
        List<HistoricalSnapshotInfo> historicalSnapshotInfos =
            historicalSnapshotDao.getHistoricalSnapshotMapData(historicalSnapshotQuery);
        String address =
            positionalService.getAddress(historicalSnapshotQuery.getLongitude(), historicalSnapshotQuery.getLatitude());
        HistoricalSnapshotInfo firstHistoricalSnapshotInfo = null;
        for (HistoricalSnapshotInfo historicalSnapshotInfo : historicalSnapshotInfos) {
            historicalSnapshotInfo.setAddress(address);
            if (historicalSnapshotQuery.getSnapshotTime() != null && !historicalSnapshotQuery.getSnapshotTime()
                .equals("") && historicalSnapshotInfo.getCreateDataTime().getTime() == DateUtil
                .getStringToLong(historicalSnapshotQuery.getSnapshotTime(), null)) {
                firstHistoricalSnapshotInfo = historicalSnapshotInfo;
            }
            String path = historicalSnapshotInfo.getMediaUrlNew();
            if (sslEnabled) {
                webServerUrl = "/";
            }
            if (path != null && !path.equals("")) {
                historicalSnapshotInfo.setMediaUrlNew(path.startsWith("/") ? mediaUrl + path : webServerUrl + path);
            }
        }
        if (firstHistoricalSnapshotInfo != null) {
            historicalSnapshotInfos.remove(firstHistoricalSnapshotInfo);
            historicalSnapshotInfos.add(0, firstHistoricalSnapshotInfo);
        }
        re.put("mediaMapDataDetail", historicalSnapshotInfos);
        return new JsonResultBean(re);
    }

    @Override
    public JsonResultBean send8801(String vehicleId) {
        BindDTO vehicleInfo = MonitorUtils.getBindDTO(vehicleId);
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.getDeviceId();
            String mobile = vehicleInfo.getSimCardNumber();
            List<VideoChannelSetting> channelSettings = videoChannelSettingDao.getAppVideoChannel(vehicleId);
            for (VideoChannelSetting videoChannelSetting : channelSettings) {
                Photograph photograph = new Photograph();
                photograph.setChroma(125);
                photograph.setCommand(1);
                photograph.setContrast(60);
                photograph.setDistinguishability(1);
                photograph.setLuminance(125);
                photograph.setQuality(5);
                photograph.setSaturability(60);
                photograph.setWayID(videoChannelSetting.getPhysicsChannel());
                photograph.setSaveSign(0);
                photograph.setTime(0);
                int msgSN = DeviceHelper.serialNumber(vehicleId);
                multimediaService.photograph(deviceId, photograph, mobile, msgSN, vehicleInfo);
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }
}
