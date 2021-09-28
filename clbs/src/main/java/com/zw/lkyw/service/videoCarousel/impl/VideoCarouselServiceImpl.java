package com.zw.lkyw.service.videoCarousel.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.zw.adas.utils.FastDFSClient;
import com.zw.lkyw.service.videoCarousel.VideoCarouselService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.repository.modules.MediaDao;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.impl.VehicleServiceImpl;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.TreeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author denghuabing on 2019/12/26 10:11
 */
@Service
public class VideoCarouselServiceImpl implements VideoCarouselService {

    private Logger log = LogManager.getLogger(VideoCarouselServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    private VehicleServiceImpl vehicleService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    FastDFSClient fastDFSClient;

    @Autowired
    MediaDao mediaDao;

    @Override
    public Map<String, JSONArray> getMonitor(String id, String type, Boolean isChecked) {
        Map<String, JSONArray> map = new HashMap<>();
        if (StringUtils.isNotBlank(id)) {
            List<String> assignIds = new ArrayList<>();
            // 遍历得到当前用户组织及下级组织id的list

            if ("group".equals(type)) {
                List<OrganizationLdap> userOrg = organizationService.getOrgChildList(id);
                List<String> userOrgListId = new ArrayList<>();
                if (userOrg != null && !userOrg.isEmpty()) {
                    for (OrganizationLdap anUserOrg : userOrg) {
                        userOrgListId.add(anUserOrg.getUuid());
                    }
                }
                // 查询当前用户权限分组
                String userUuid = userService.getCurrentUserUuid();
                // 获取权限下的分组
                List<Assignment> assignmentList = assignmentService.findUserAssignment(userUuid, userOrgListId);
                // 分组id
                if (assignmentList != null && !assignmentList.isEmpty()) {
                    for (Assignment anAssignmentList : assignmentList) {
                        assignIds.add(anAssignmentList.getId());
                    }
                }
            } else {
                assignIds.add(id);
            }
            List<VehicleInfo> vehicleList = groupDao.findMonitorByAssignmentIds(assignIds, null);
            generateTree(map, vehicleList, isChecked);
        }
        return map;
    }

    @Override
    public JSONArray getTree(String queryType, String queryParam, Boolean isChecked) {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String uuid = userService.getCurrentUserUuid();
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> orgs = userService.getCurrentUseOrgList();
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = orgs.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        if ("group".equals(queryType) && StringUtils.isNotBlank(queryParam)) {
            final String finalQueryParams = queryParam;
            List<OrganizationLdap> filterList =
                orgs.stream().filter(org -> org.getName().contains(finalQueryParams)).collect(Collectors.toList());
            orgs = TreeUtils.getFilterWholeOrgList(orgs, filterList);
            userOrgListId = vehicleService.getOrgUuids(filterList);
        }

        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, userOrgListId);
        // 组装分组树结构
        List<String> assignIdList = assignmentService.putAssignmentTree(assignmentList, result, "", false);
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, ""));
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)

        if (assignIdList != null && assignIdList.size() > 0) {
            queryParam = StringUtil.mysqlLikeWildcardTranslation(queryParam);

            // 实时监控，组装所有数据；实时视频，过滤除808以外的车辆
            List<String> deviceTypes = Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE);
            List<VehicleInfo> vehicleList =
                groupDao.findMonitorByAssignmentIdsFuzzy(assignIdList, queryParam, queryType, deviceTypes);

            // 组装车辆树
            if (vehicleList != null && vehicleList.size() > 0) {
                List<String> vids = vehicleList.stream().map(VehicleInfo::getId).collect(Collectors.toList());
                List<VideoChannelSetting> vcs = videoChannelSettingDao.getVideoChannelByVehicleIds(vids);
                Map<String, List<VideoChannelSetting>> channelSettingMap = new HashMap<>();
                if (CollectionUtils.isNotEmpty(vcs)) {
                    channelSettingMap = vcs.stream().collect(Collectors.groupingBy(VideoChannelSetting::getVehicleId));
                }
                for (VehicleInfo vehicle : vehicleList) {
                    JSONObject vehicleObj = new JSONObject();
                    // 树组装
                    putMonitorTree(vehicle, vehicleObj, channelSettingMap, isChecked);
                    result.add(vehicleObj);
                }
            }
        }
        return result;
    }

    @Override
    public String getVideoSetting() {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_VIDEO_SETTING.of(userId);
        if (RedisHelper.isContainsKey(redisKey)) {
            return RedisHelper.getString(redisKey);
        }
        return "";
    }

    @Override
    public boolean videoSet(String setting) {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_VIDEO_SETTING.of(userId);
        RedisHelper.setString(redisKey, setting);
        return true;
    }

    private void generateTree(Map<String, JSONArray> map, List<VehicleInfo> vehicleList, Boolean isChecked) {
        if (!vehicleList.isEmpty()) {
            List<String> vids = vehicleList.stream().map(VehicleInfo::getId).collect(Collectors.toList());
            List<VideoChannelSetting> vcs = videoChannelSettingDao.getVideoChannelByVehicleIds(vids);
            Map<String, List<VideoChannelSetting>> channelSettingMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(vcs)) {
                channelSettingMap = vcs.stream().collect(Collectors.groupingBy(VideoChannelSetting::getVehicleId));
            }
            for (VehicleInfo vehicle : vehicleList) {
                JSONObject vehicleObj = new JSONObject();
                // 组装将空对象数据
                putMonitorTree(vehicle, vehicleObj, channelSettingMap, isChecked);
                JSONArray array = new JSONArray();
                if (map.containsKey(vehicle.getAssignmentId())) { // 包含
                    array = map.get(vehicle.getAssignmentId());
                }
                array.add(vehicleObj);
                map.put(vehicle.getAssignmentId(), array);
            }
        }
    }

    /**
     * 组装监控对象
     */
    private void putMonitorTree(VehicleInfo vehicle, JSONObject vehicleObj,
        Map<String, List<VideoChannelSetting>> channelSettingMap, Boolean isChecked) {
        vehicleObj.put("id", vehicle.getId());
        String monitorType = vehicle.getMonitorType();
        if ("0".equals(monitorType)) {
            vehicleObj.put("type", "vehicle");
            vehicleObj.put("iconSkin", "vehicleSkin");
        } else if ("1".equals(monitorType)) {
            vehicleObj.put("type", "people");
            vehicleObj.put("iconSkin", "peopleSkin");
        } else if ("2".equals(monitorType)) {
            vehicleObj.put("type", "thing");
            vehicleObj.put("iconSkin", "thingSkin");
        }
        vehicleObj.put("pId", vehicle.getAssignmentId());
        vehicleObj.put("name", vehicle.getBrand());
        vehicleObj.put("deviceNumber", vehicle.getDeviceNumber());
        vehicleObj.put("isVideo", vehicle.getIsVideo());
        vehicleObj.put("deviceType", vehicle.getDeviceType());
        vehicleObj.put("plateColor", vehicle.getPlateColor());
        vehicleObj.put("simcardNumber", vehicle.getSimcardNumber());
        vehicleObj.put("professional", vehicle.getProfessionalsName());
        vehicleObj.put("assignName", vehicle.getGroupName());
        vehicleObj.put("aliases", vehicle.getAliases());
        if (Objects.nonNull(isChecked) && isChecked) {
            vehicleObj.put("checked", true);
        }
        vehicleObj.put("isParent", true); // 若为实时视频，则组装为有子节点

        // 通道属性
        if (channelSettingMap.containsKey(vehicle.getId())) {
            putChannelProperties(vehicle.getId(), vehicleObj, channelSettingMap);
        }
    }

    public void putChannelProperties(String monitorId, JSONObject vehicleObj,
        Map<String, List<VideoChannelSetting>> channelSettingMap) {
        List<VideoChannelSetting> channelSetting = channelSettingMap.get(monitorId);

        List<Integer> panoramics = new ArrayList<>();
        List<Integer> physicsChannels = new ArrayList<>();
        List<Integer> logicChannels = new ArrayList<>();
        List<Integer> channelTypes = new ArrayList<>();
        List<Integer> sorts = new ArrayList<>();
        List<Integer> connectionFlags = new ArrayList<>();
        List<Integer> streamTypes = new ArrayList<>();
        List<String> mobiles = new ArrayList<>();
        // 去掉通道类型为音频的
        if (channelSetting != null) {
            for (VideoChannelSetting setting : channelSetting) {
                if (setting.getChannelType() == 1) {
                    continue;
                }
                panoramics.add(Boolean.TRUE.equals(setting.getPanoramic()) ? 1 : 0);
                physicsChannels.add(setting.getPhysicsChannel());
                logicChannels.add(setting.getLogicChannel());
                channelTypes.add(setting.getChannelType());
                sorts.add(setting.getSort());
                connectionFlags.add(setting.getConnectionFlag());
                streamTypes.add(setting.getStreamType());
                mobiles.add(setting.getMobile());
            }
        }
        vehicleObj.put("panoramic", Joiner.on(",").join(panoramics));
        vehicleObj.put("physicsChannel", Joiner.on(",").join(physicsChannels));
        vehicleObj.put("logicChannel", Joiner.on(",").join(logicChannels));
        vehicleObj.put("channelType", Joiner.on(",").join(channelTypes));
        vehicleObj.put("sort", Joiner.on(",").join(sorts));
        vehicleObj.put("connectionFlag", Joiner.on(",").join(connectionFlags));
        vehicleObj.put("streamType", Joiner.on(",").join(streamTypes));
        vehicleObj.put("mobile", Joiner.on(",").join(mobiles));
    }

    @Override
    public boolean saveMedia(MultipartFile fileData, String vehicleId, String channelNum) {
        String imageFilename = null;
        try {
            int imageLength = (int) fileData.getSize();
            InputStream inputStream = fileData.getInputStream();
            // 生成文件名称
            String originalFilename = UUID.randomUUID().toString() + ".jpg";
            imageFilename = fastDFSClient.uploadFile(inputStream, imageLength, originalFilename);
        } catch (Exception e) {
            log.error("视频轮播截图保存上传fastDFS异常！", e);
        }
        final String plateNumber = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "name");
        MediaForm mediaForm = new MediaForm();
        mediaForm.setBrand(plateNumber);
        mediaForm.setVehicleId(vehicleId);
        mediaForm
            .setMediaName(imageFilename != null ? imageFilename.substring((imageFilename.lastIndexOf("/") + 1)) : null);
        mediaForm.setMediaUrlNew(imageFilename);
        mediaForm.setCreateDataTime(new Date());
        mediaForm.setFormatCode(0);
        mediaForm.setType(0);
        mediaForm.setEventCode(-1);
        mediaForm.setFlag(1);
        mediaForm.setWayId(Integer.parseInt(channelNum));
        return mediaDao.addMedia(mediaForm);
    }
}
