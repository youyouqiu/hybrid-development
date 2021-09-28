package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.IconDO;
import com.zw.platform.basic.dto.IconDTO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.repository.IconDao;
import com.zw.platform.basic.repository.NewVehicleCategoryDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.util.FileUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 监控对象图标服务接口
 * @author zhangjuan
 */
@Service
public class MonitorIconServiceImpl implements CacheService, MonitorIconService {
    private static final Logger log = LogManager.getLogger(MonitorIconServiceImpl.class);
    private static final String DEFAULT_PEOPLE_ICON = "123.png";
    private static final String DEFAULT_THING_ICON = "thing.png";
    private static final String ICON_PATH = "resources/img/vico/";
    private static final String SUPPORT_UPLOAD_SUFFIX = "png";
    private static final int IMG_MAX_WITH = 67;
    private static final int IMG_MAX_HEIGHT = 37;
    @Autowired
    private MonitorFactory monitorFactory;

    @Autowired
    private IconDao iconDao;

    @Autowired
    private NewVehicleCategoryDao vehicleCategoryDao;

    @Autowired
    private ConfigService configService;

    @Override
    public void initCache() {
        log.info("开始进行监控对象个性化图标的redis初始化.");
        //清除图标缓存
        RedisHelper.delete(RedisKeyEnum.MONITOR_ICON.of());
        //初始化监控对象个性化图标
        for (MonitorTypeEnum typeEnum : MonitorTypeEnum.values()) {
            monitorFactory.create(typeEnum.getType()).initIconCache();
        }
        log.info("结束终监控对象个性化图标的redis初始化.");
    }

    @Override
    public boolean update(String iconId, List<Map<String, String>> monitorList) {
        //获取图标名称
        IconDO icon = iconDao.getById(iconId);
        if (Objects.isNull(icon) || StringUtils.isBlank(icon.getIcoName())) {
            return false;
        }
        String icoName = icon.getIcoName();
        //根据监控对象类型分组的Map：监控对象类型-对应类型下监控对象Id的集合
        Map<String, Set<String>> monitorTypeIdMap = groupByMoType(monitorList);
        //更新不同类型监控对象的个性化图标
        Set<String> monitorIdSet = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : monitorTypeIdMap.entrySet()) {
            String moType = MonitorTypeEnum.getTypeByEnName(entry.getKey());
            monitorFactory.create(moType).updateIcon(entry.getValue(), iconId, icoName);
            monitorIdSet.addAll(entry.getValue());
        }

        //批量获取监控对象的终端号，并删除对应的监控对象缓存 --保留的原有逻辑，原因目前不了解
        deleteDeviceCache(monitorIdSet);
        return true;
    }

    private void deleteDeviceCache(Set<String> monitorIdSet) {
        List<RedisKey> redisKeys =
            monitorIdSet.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        List<String> deviceNumList = RedisHelper.batchGetHashMap(redisKeys, "deviceNumber");
        if (deviceNumList.isEmpty()) {
            return;
        }
        List<RedisKey> deviceRedisKeys =
            deviceNumList.stream().map(HistoryRedisKeyEnum.DEVICE_VEHICLE_INFO::of).collect(Collectors.toList());
        RedisHelper.delete(deviceRedisKeys);
    }

    private Map<String, Set<String>> groupByMoType(List<Map<String, String>> monitorList) {
        Map<String, Set<String>> monitorTypeIdMap = new HashMap<>(16);
        for (Map<String, String> monitorMap : monitorList) {
            String type = monitorMap.get("type");
            Set<String> monitorIds = monitorTypeIdMap.getOrDefault(type, new HashSet<>());
            monitorIds.add(monitorMap.get("id"));
            monitorTypeIdMap.put(type, monitorIds);
        }
        return monitorTypeIdMap;
    }

    @Override
    public boolean delete(List<Map<String, String>> monitorList) {
        //根据监控对象类型分组的Map：监控对象类型-对应类型下监控对象Id的集合
        Map<String, Set<String>> monitorTypeIdMap = groupByMoType(monitorList);
        Set<String> monitorIdSet = new HashSet<>();

        //删除监控对象的个性化图标
        for (Map.Entry<String, Set<String>> entry : monitorTypeIdMap.entrySet()) {
            String moType = MonitorTypeEnum.getTypeByEnName(entry.getKey());
            monitorFactory.create(moType).deleteIcon(entry.getValue());
            monitorIdSet.addAll(entry.getValue());
        }

        //批量获取监控对象的终端号，并删除对应的监控对象缓存 --保留的原有逻辑，原因目前不了解
        deleteDeviceCache(monitorIdSet);
        return true;
    }

    @Override
    public Map<String, String> getByMonitorId(Collection<String> monitorIdSet) {
        Set<String> monitorIds = new HashSet<>(monitorIdSet);
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new HashMap<>(16);
        }
        Map<String, String> monitorIconMap = new HashMap<>(CommonUtil.ofMapCapacity(monitorIds.size()));
        //优先获取监控对象设置的个性化图标
        monitorIconMap.putAll(RedisHelper.getHashMapReturnNonNull(RedisKeyEnum.MONITOR_ICON.of(), monitorIds));
        Set<String> hasIconMonitorIds = monitorIconMap.keySet();
        if (Objects.equals(monitorIds.size(), hasIconMonitorIds.size())) {
            return monitorIconMap;
        }
        //剩下的监控对象根据车辆类别或车辆子类型获取监控对象图标
        monitorIds.removeAll(hasIconMonitorIds);
        List<RedisKey> redisKeys = monitorIds.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        List<String> fields = Arrays.asList("id", "vehicleCategoryId", "monitorType", "vehicleSubTypeId");
        List<Map<String, String>> monitorList = RedisHelper.batchGetHashMap(redisKeys, fields);
        TypeCacheManger cacheManger = TypeCacheManger.getInstance();
        for (Map<String, String> monitor : monitorList) {
            String monitorId = monitor.get("id");
            if (StringUtils.isBlank(monitorId)) {
                continue;
            }
            String moType = monitor.get("monitorType");
            //若监控对象是人，设置人员默认图标
            if (Objects.equals(moType, MonitorTypeEnum.PEOPLE.getType())) {
                monitorIconMap.put(monitorId, DEFAULT_PEOPLE_ICON);
                continue;
            }
            //若监控对象是物，设置物品图标
            if (Objects.equals(moType, MonitorTypeEnum.THING.getType())) {
                monitorIconMap.put(monitorId, DEFAULT_THING_ICON);
                continue;
            }
            //获取车辆类别
            String vehicleCategoryId = Converter.toBlank(monitor.get("vehicleCategoryId"));
            VehicleCategoryDTO categoryDTO = cacheManger.getVehicleCategory(vehicleCategoryId);
            if (Objects.isNull(categoryDTO)) {
                log.error("车辆【{}】的类别【{}】不存在", monitorId, vehicleCategoryId);
                continue;
            }

            //非工程机械类型直接用车辆类别图标
            String iconName = categoryDTO.getIconName();
            if (!Objects.equals(categoryDTO.getStandard(), Vehicle.Standard.ENGINEERING)) {
                monitorIconMap.put(monitorId, iconName);
                continue;
            }

            //工程机械类型车辆优先从车辆子类别中获取
            VehicleSubTypeDTO subTypeDTO = null;
            if (StringUtils.isNotBlank(monitor.get("vehicleSubTypeId"))) {
                subTypeDTO = cacheManger.getVehicleSubType(monitor.get("vehicleSubTypeId"));
            }
            if (Objects.nonNull(subTypeDTO) && StringUtils.isNotBlank(subTypeDTO.getIconName())) {
                monitorIconMap.put(monitorId, subTypeDTO.getIconName());
                continue;
            }
            //工程机械类型的车辆子类型不存在或车辆子类型没有图标，则从车辆类别中获取车辆的图标
            if (StringUtils.isNotBlank(iconName)) {
                monitorIconMap.put(monitorId, iconName);
            }
        }
        return monitorIconMap;
    }

    @Override
    public String getMonitorIcon(String monitorId) {
        Map<String, String> monitorIconMap = getByMonitorId(Collections.singletonList(monitorId));
        return monitorIconMap.get(monitorId);
    }

    @Override
    public Map<String, String> getUserOwnMonitorIcon() {
        Set<String> monitorIds = configService.getByKeyWord(null, null);
        return getByMonitorId(monitorIds);
    }

    @Override
    public String getIconPath(HttpServletRequest request) {
        return request.getSession().getServletContext().getRealPath("/") + ICON_PATH;
    }

    @Override
    public List<IconDTO> getIconList() {
        return iconDao.getAll();
    }

    @Override
    public boolean deleteIcon(String iconId, HttpServletRequest request) {
        IconDO icon = iconDao.getById(iconId);
        if (Objects.isNull(icon)) {
            return true;
        }
        //检查监控车辆类别是否有使用该图标
        List<VehicleCategoryDTO> categoryList = vehicleCategoryDao.getByIcon(iconId);
        if (!categoryList.isEmpty()) {
            return false;
        }
        iconDao.delete(iconId);
        return FileUtil.deleteFile(getIconPath(request), icon.getIcoName());
    }

    @Override
    public Map<String, Object> uploadImg(HttpServletRequest request, MultipartFile file) {
        String filePath = getIconPath(request);
        Map<String, Object> result =
            FileUtil.uploadImg(file, filePath, IMG_MAX_WITH, IMG_MAX_HEIGHT, SUPPORT_UPLOAD_SUFFIX);
        if (!Objects.equals("0", result.get("state"))) {
            return result;
        }
        //进行图标插入
        IconDO iconDO = new IconDO(String.valueOf(result.get("imgName")));
        iconDao.insert(iconDO);
        result.put("id", iconDO.getId());
        return result;
    }

    @Override
    public boolean updateIconDirection(String flag) {
        RedisHelper.setString(HistoryRedisKeyEnum.ICON_DIRECTION.of(), flag);
        return true;
    }

}
