package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 信息配置-绑定后的监控对象视频参数信息
 *
 * @author create by zhangjuan on 2020/11/12.
 */
@Slf4j
public class ConfigVideoChannelHandler extends BaseImportHandler {
    private final VideoChannelSettingDao videoChannelSettingDao;
    private final DeviceNewDao deviceNewDao;
    private final ConfigImportHolder holder;

    public ConfigVideoChannelHandler(ConfigImportHolder holder, DeviceNewDao deviceNewDao,
                                     VideoChannelSettingDao videoChannelSettingDao) {
        this.holder = holder;
        this.videoChannelSettingDao = videoChannelSettingDao;
        this.deviceNewDao = deviceNewDao;
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public int stage() {
        return 4;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[]{ImportTable.ZW_M_VIDEO_CHANNEL_SETTING};
    }

    @Override
    public boolean addMysql() {
        List<BindDTO> bindList = holder.getImportList();
        Map<String, String> monitorChannelMap = new HashMap<>(Math.max((int) (bindList.size() / .75f) + 1, 16));
        bindList.forEach(bindDTO -> monitorChannelMap.put(bindDTO.getId(), bindDTO.getTerminalTypeId()));

        //1.获取所有的监控对象集合和终端型号集合
        Set<String> monitorIds = new HashSet<>(monitorChannelMap.keySet());
        Set<String> terminalTypeIds = new HashSet<>(monitorChannelMap.values());

        //获取符合条件的终端型号数据
        List<TerminalTypeInfo> terminalTypes =
                holder.getTerminalTypeInfoList().stream().filter(o -> StringUtils.isNotBlank(o.getDeviceChannelId())
                        && terminalTypeIds.contains(o.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(terminalTypes)) {
            //保留原有逻辑
            return false;
        }

        Map<String, VideoChannelSetting> deviceChannelMap =
                AssembleUtil.collectionToMap(deviceNewDao.getDeviceChannelSetting(), VideoChannelSetting::getId);

        // 3.根据终端型号id进行分组
        Map<String, String> terminalTypeInfoMap = AssembleUtil
                .collectionToMap(terminalTypes, TerminalTypeInfo::getId, TerminalTypeInfo::getDeviceChannelId);

        // 4.批量删除历史设置的音视频通道
        videoChannelSettingDao.deleteMoreByMonitorIds(monitorIds);

        List<VideoChannelSetting> videoChannelSettingList = new ArrayList<>();
        for (Map.Entry<String, String> map : monitorChannelMap.entrySet()) {
            String monitorId = map.getKey();
            String terminalId = map.getValue();
            String deviceChannelId = terminalTypeInfoMap.get(terminalId);
            if (StringUtils.isEmpty(deviceChannelId)) {
                continue;
            }
            // 7.添加修改音视频通道
            for (String deviceChannel : deviceChannelId.split(",")) {
                VideoChannelSetting video = deviceChannelMap.get(deviceChannel);
                VideoChannelSetting newVideoChannelSetting = new VideoChannelSetting();
                newVideoChannelSetting.setId(UUID.randomUUID().toString());
                newVideoChannelSetting.setLogicChannel(video.getLogicChannel());
                newVideoChannelSetting.setPhysicsChannel(video.getPhysicsChannel());
                newVideoChannelSetting.setChannelType(video.getChannelType());
                newVideoChannelSetting.setConnectionFlag(video.getConnectionFlag());
                newVideoChannelSetting.setSort(video.getSort());
                newVideoChannelSetting.setStreamType(video.getStreamType());
                newVideoChannelSetting.setVehicleId(monitorId);
                newVideoChannelSetting.setPanoramic(Boolean.FALSE);
                videoChannelSettingList.add(newVideoChannelSetting);
            }
        }
        if (CollectionUtils.isNotEmpty(videoChannelSettingList)) {
            partition(videoChannelSettingList, videoChannelSettingDao::addVideoChannels);
            ZMQFencePub.pubChangeFence("20");
        }
        return true;
    }
}
