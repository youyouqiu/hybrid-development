package com.zw.platform.basic.core;

import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 类别类型这类少量数据的本地缓存
 * @author zhnagjuan
 */
@Log4j2
public class TypeCacheManger {
    private static final TypeCacheManger INSTANCE = new TypeCacheManger();

    /**
     * 车辆运营类别
     */
    private final Map<String, VehiclePurposeDTO> vehiclePurposeMap;

    /**
     * 车辆类别
     */
    private final Map<String, VehicleCategoryDTO> vehicleCategoryMap;

    /**
     * 车类类型
     */
    private final Map<String, VehicleTypeDTO> vehicleTypeMap;

    /**
     * 车类子类型
     */
    private final Map<String, VehicleSubTypeDTO> vehicleSubTypeMap;

    /**
     * 数据字典
     */
    private final Map<String, DictionaryDO> dictionaryMap;

    private TypeCacheManger() {
        vehiclePurposeMap = new ConcurrentHashMap<>();
        vehicleCategoryMap = new ConcurrentHashMap<>();
        vehicleTypeMap = new ConcurrentHashMap<>();
        vehicleSubTypeMap = new ConcurrentHashMap<>();
        dictionaryMap = new ConcurrentHashMap<>();
    }

    public static TypeCacheManger getInstance() {
        return INSTANCE;
    }

    public void saveVehiclePurpose(VehiclePurposeDTO purpose) {
        vehiclePurposeMap.put(purpose.getId(), purpose);
    }

    public void removeVehiclePurpose(String id) {
        vehiclePurposeMap.remove(id);
    }

    public void clearVehiclePurpose() {
        vehiclePurposeMap.clear();
    }

    public VehiclePurposeDTO getVehiclePurpose(String id) {
        return vehiclePurposeMap.get(id);
    }

    public Collection<VehiclePurposeDTO> getVehiclePurposes() {
        return vehiclePurposeMap.values();
    }

    public void saveVehicleCategory(VehicleCategoryDTO vehicleCategoryDTO) {
        vehicleCategoryMap.put(vehicleCategoryDTO.getId(), vehicleCategoryDTO);
    }

    public void removeVehicleCategory(String id) {
        vehicleCategoryMap.remove(id);
    }

    public void clearVehicleCategory() {
        vehicleCategoryMap.clear();
    }

    public VehicleCategoryDTO getVehicleCategory(String id) {
        return vehicleCategoryMap.get(id);
    }

    public List<VehicleCategoryDTO> getVehicleCategories(Integer standard) {
        Collection<VehicleCategoryDTO> categories = vehicleCategoryMap.values();
        if (Objects.isNull(standard)) {
            return new ArrayList<>(categories);
        }
        return categories.stream().filter(o -> Objects.equals(o.getStandard(), standard)).collect(Collectors.toList());
    }

    public void saveVehicleType(VehicleTypeDTO typeDTO) {
        vehicleTypeMap.put(typeDTO.getId(), typeDTO);
    }

    public void removeVehicleType(String id) {
        vehicleTypeMap.remove(id);
    }

    public VehicleTypeDTO getVehicleType(String id) {
        VehicleTypeDTO vehicleType = vehicleTypeMap.get(id);
        if (Objects.nonNull(vehicleType) && Objects.nonNull(getVehicleCategory(vehicleType.getCategoryId()))) {
            vehicleType.setCategory(getVehicleCategory(vehicleType.getCategoryId()).getCategory());
        }
        return vehicleTypeMap.get(id);
    }

    public List<VehicleTypeDTO> getVehicleTypes(Set<String> categoryIds) {
        Collection<VehicleTypeDTO> types = vehicleTypeMap.values();
        List<VehicleTypeDTO> result = new ArrayList<>();
        for (VehicleTypeDTO vehicleType : types) {
            String categoryId = vehicleType.getCategoryId();
            if (categoryIds != null && !categoryIds.contains(categoryId)) {
                continue;
            }
            //类别有可能已经修改，所以从类别缓存中获取类别名称更准确
            VehicleCategoryDTO category = getVehicleCategory(categoryId);
            if (Objects.nonNull(category)) {
                vehicleType.setCategory(category.getCategory());
            }
            result.add(vehicleType);
        }
        return result;
    }

    public void clearVehicleType() {
        vehicleTypeMap.clear();
    }

    public void saveVehicleSubType(VehicleSubTypeDTO vehicleSubType) {
        vehicleSubTypeMap.put(vehicleSubType.getId(), vehicleSubType);
    }

    public void removeVehicleSubType(String id) {
        vehicleSubTypeMap.remove(id);
    }

    public VehicleSubTypeDTO getVehicleSubType(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        VehicleSubTypeDTO vehicleSubType = vehicleSubTypeMap.get(id);
        if (Objects.isNull(vehicleSubType)) {
            return null;
        }

        VehicleTypeDTO vehicleTypeDTO = getVehicleType(vehicleSubType.getTypeId());
        if (Objects.nonNull(vehicleTypeDTO)) {
            vehicleSubType.setType(vehicleTypeDTO.getType());
            vehicleSubType.setCategoryId(vehicleTypeDTO.getCategoryId());
            vehicleSubType.setCategory(vehicleTypeDTO.getCategory());
        }

        return vehicleSubType;
    }

    /**
     * 获取车辆子类型
     * @param typeId 车辆类型 为空查询全部
     * @return 车辆子类型
     */
    public List<VehicleSubTypeDTO> getVehicleSubTypes(String typeId) {
        Collection<VehicleSubTypeDTO> collection = vehicleSubTypeMap.values();
        List<VehicleSubTypeDTO> vehicleSubTypes = new ArrayList<>();
        for (VehicleSubTypeDTO vehicleSubType : collection) {
            VehicleTypeDTO vehicleTypeDTO = getVehicleType(vehicleSubType.getTypeId());
            if (Objects.nonNull(vehicleTypeDTO)) {
                vehicleSubType.setType(vehicleTypeDTO.getType());
                vehicleSubType.setCategoryId(vehicleTypeDTO.getCategoryId());
                vehicleSubType.setCategory(vehicleTypeDTO.getCategory());
            }
            if (StringUtils.isBlank(typeId) || typeId.equals(vehicleSubType.getTypeId())) {
                vehicleSubTypes.add(vehicleSubType);
            }

        }
        return vehicleSubTypes;
    }

    public void clearVehicleSubType() {
        vehicleSubTypeMap.clear();
    }

    public void clearDictionary() {
        dictionaryMap.clear();
    }

    public void saveDictionary(DictionaryDO dictionaryDO) {
        dictionaryMap.put(dictionaryDO.getId(), dictionaryDO);
    }

    public String getDictionaryValue(String id) {
        DictionaryDO dictionaryDO = dictionaryMap.get(id);
        return Objects.isNull(dictionaryDO) ? "" : dictionaryDO.getValue();
    }

    public DictionaryDO getDictionary(String id) {
        return dictionaryMap.get(id);
    }

    public List<DictionaryDO> getDictionaryList(String type) {
        Collection<DictionaryDO> dictionarySet = dictionaryMap.values();
        List<DictionaryDO> result = new ArrayList<>();
        for (DictionaryDO dictionary : dictionarySet) {
            if (Objects.equals(type, dictionary.getType())) {
                result.add(dictionary);
            }
        }
        return result;
    }

    public List<Map<String, String>> getDictionaryMapList(String type) {
        Collection<DictionaryDO> dictionarySet = dictionaryMap.values();
        List<Map<String, String>> result = new ArrayList<>();
        Map<String, String> map;
        for (DictionaryDO dictionary : dictionarySet) {
            if (Objects.equals(type, dictionary.getType())) {
                map = new HashMap<>(6);
                map.put("code", dictionary.getCode());
                map.put("value", dictionary.getValue());
                map.put("type", dictionary.getType());
                result.add(map);
            }
        }
        return result;
    }

    public Map<String, String> getDictValueIdMap(String type) {
        Map<String, String> valueIdMap = new HashMap<>(16);
        for (Map.Entry<String, DictionaryDO> entry : dictionaryMap.entrySet()) {
            if (StringUtils.isNotBlank(type) && !Objects.equals(type, entry.getValue().getType())) {
                continue;
            }
            valueIdMap.put(entry.getValue().getValue(), entry.getKey());
        }
        return valueIdMap;
    }

    public Map<String, String> getDictValueCodeMap(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        Map<String, String> valueIdMap = new HashMap<>(16);
        for (Map.Entry<String, DictionaryDO> entry : dictionaryMap.entrySet()) {
            if (!Objects.equals(type, entry.getValue().getType())) {
                continue;
            }
            valueIdMap.put(entry.getValue().getValue(), entry.getValue().getCode());
        }
        return valueIdMap;
    }

    public Map<String, String> getDictCodeValueMap(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        Map<String, String> valueIdMap = new HashMap<>(16);
        for (Map.Entry<String, DictionaryDO> entry : dictionaryMap.entrySet()) {
            if (!Objects.equals(type, entry.getValue().getType())) {
                continue;
            }
            valueIdMap.put(entry.getValue().getCode(), entry.getValue().getValue());
        }
        return valueIdMap;
    }
}
