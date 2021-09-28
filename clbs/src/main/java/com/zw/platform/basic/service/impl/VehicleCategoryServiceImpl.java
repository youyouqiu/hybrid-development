package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewVehicleCategoryDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.VehicleCategoryService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 车辆类别模块实现类
 * @author zhangjuan
 */
@Service
public class VehicleCategoryServiceImpl implements VehicleCategoryService, CacheService {
    private static final Logger log = LogManager.getLogger(VehicleCategoryServiceImpl.class);
    @Autowired
    private NewVehicleCategoryDao vehicleCategoryDao;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private HttpServletRequest request;

    @Override
    @PostConstruct
    public void initCache() {
        log.info("开始进行车辆类别的本地缓存初始化.");
        List<VehicleCategoryDTO> categoryList = vehicleCategoryDao.getByKeyword(null);
        TypeCacheManger.getInstance().clearVehicleCategory();
        if (categoryList.isEmpty()) {
            return;
        }
        for (VehicleCategoryDTO category : categoryList) {
            TypeCacheManger.getInstance().saveVehicleCategory(category);
        }
        log.info("结束车辆类别的本地缓存初始化.");
    }

    @Override
    public boolean add(VehicleCategoryDTO vehicleCategoryDTO) throws BusinessException {
        if (isExistCategory(null, vehicleCategoryDTO.getCategory())) {
            throw new BusinessException("", "该车辆类别已经存在！");
        }

        //车辆类别入库
        VehicleCategoryDO categoryDO = new VehicleCategoryDO(vehicleCategoryDTO);
        boolean isSuccess = vehicleCategoryDao.insert(categoryDO);
        if (!isSuccess) {
            return false;
        }
        //维护本地缓存
        vehicleCategoryDTO.setId(categoryDO.getId());
        TypeCacheManger.getInstance().saveVehicleCategory(vehicleCategoryDTO);

        logService.addLog(getIp(), "新增车辆类别：" + categoryDO.getVehicleCategory(), "3", "", "-", "");
        return true;
    }

    @Override
    public boolean isExistCategory(String id, String name) {
        VehicleCategoryDO vehicleCategoryDO = vehicleCategoryDao.getByName(name);
        return Objects.nonNull(vehicleCategoryDO) && !Objects.equals(vehicleCategoryDO.getId(), id);
    }

    @Override
    public boolean update(VehicleCategoryDTO categoryDTO) throws BusinessException {
        VehicleCategoryDTO oldCategory = vehicleCategoryDao.getById(categoryDTO.getId());
        if (Objects.isNull(oldCategory)) {
            throw new BusinessException("", "对象不存在");
        }

        if (isExistCategory(categoryDTO.getId(), categoryDTO.getCategory())) {
            throw new BusinessException("", "该车辆类别已经存在！");
        }

        VehicleCategoryDO categoryDO = new VehicleCategoryDO(categoryDTO);
        boolean isSuccess = vehicleCategoryDao.update(categoryDO);
        if (!isSuccess) {
            return false;
        }

        TypeCacheManger.getInstance().saveVehicleCategory(vehicleCategoryDao.getById(categoryDTO.getId()));

        String msg = "修改类别：" + oldCategory.getCategory();
        if (!Objects.equals(oldCategory.getCategory(), categoryDTO.getCategory())) {
            msg = msg + " 修改为：" + categoryDTO.getCategory();
        }
        logService.addLog(getIp(), msg, "3", "", "-", "");

        return true;
    }

    @Override
    public boolean delete(String id) throws BusinessException {
        VehicleCategoryDTO category = vehicleCategoryDao.getById(id);
        if (Objects.isNull(category)) {
            throw new BusinessException("", "对象不存在");
        }

        if (isBindType(Collections.singletonList(id))) {
            throw new BusinessException("", "该车辆类别下存在车辆类型绑定关系，无法删除");
        }
        VehicleCategoryDO vehicleCategoryDO = new VehicleCategoryDO();
        vehicleCategoryDO.setFlag(0);
        vehicleCategoryDO.setId(id);
        vehicleCategoryDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());

        vehicleCategoryDao.update(vehicleCategoryDO);
        TypeCacheManger.getInstance().removeVehicleCategory(id);

        logService.addLog(getIp(), "删除车辆类别：" + category.getCategory(), "3", "", "-", "");
        return true;
    }

    @Override
    public String deleteBatch(Collection<String> ids) {
        List<VehicleCategoryDTO> categoryList = vehicleCategoryDao.getByIds(ids);
        if (categoryList.isEmpty()) {
            return null;
        }

        List<VehicleTypeDTO> typeList = TypeCacheManger.getInstance().getVehicleTypes(new HashSet<>(ids));
        Set<String> categorySet =
            typeList.stream().map(VehicleTypeDTO::getCategoryId).distinct().collect(Collectors.toSet());

        List<String> bindTypeCategoryList = new ArrayList<>();
        List<String> delIds = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        for (VehicleCategoryDTO category : categoryList) {
            if (categorySet.contains(category.getId())) {
                bindTypeCategoryList.add(category.getCategory());
                continue;
            }
            delIds.add(category.getId());
            TypeCacheManger.getInstance().removeVehicleCategory(category.getId());
            msg.append("删除车辆类别 : ").append(category.getCategory()).append(" <br/>");
        }

        if (!delIds.isEmpty()) {
            vehicleCategoryDao.deleteBatch(delIds);
            logService.addLog(getIp(), msg.toString(), "3", "batch", "批量删除车辆类别");
        }
        return StringUtils.join(bindTypeCategoryList, ",");
    }

    @Override
    public VehicleCategoryDTO getById(String id) {
        return vehicleCategoryDao.getById(id);
    }

    @Override
    public List<VehicleCategoryDTO> getAllByKeyword(String keyword) {
        return vehicleCategoryDao.getByKeyword(StringUtil.mysqlLikeWildcardTranslation(keyword));
    }

    @Override
    public boolean isBindType(Collection<String> ids) {
        List<VehicleTypeDTO> typeList = TypeCacheManger.getInstance().getVehicleTypes(new HashSet<>(ids));
        return !typeList.isEmpty();
    }

    @Override
    public VehicleCategoryDTO getByName(String categoryName) {
        VehicleCategoryDO category = vehicleCategoryDao.getByName(categoryName);
        if (Objects.isNull(category)) {
            return null;
        }
        return new VehicleCategoryDTO(category);
    }

    private String getIp() {
        return new GetIpAddr().getIpAddr(request);
    }
}
