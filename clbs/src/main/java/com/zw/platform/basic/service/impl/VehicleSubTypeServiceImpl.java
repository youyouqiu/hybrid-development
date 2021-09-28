package com.zw.platform.basic.service.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewVehicleSubTypeDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.VehicleSubTypeService;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.PageHelperUtil;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 车辆子类型逻辑实现类
 * @author zhangjuan
 */
@Service
public class VehicleSubTypeServiceImpl implements VehicleSubTypeService, CacheService {
    private static final Logger log = LogManager.getLogger(VehicleSubTypeServiceImpl.class);
    @Autowired
    private NewVehicleSubTypeDao vehicleSubTypeDao;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private HttpServletRequest request;

    @PostConstruct
    @Override
    public void initCache() {
        log.info("开始进行车辆子类型的本地缓存初始化.");
        List<VehicleSubTypeDTO> subTypes = vehicleSubTypeDao.getByKeyword(null);
        TypeCacheManger.getInstance().clearVehicleSubType();
        if (subTypes.isEmpty()) {
            return;
        }

        for (VehicleSubTypeDTO subType : subTypes) {
            TypeCacheManger.getInstance().saveVehicleSubType(subType);
        }
        log.info("结束车辆子类型的本地缓存初始化.");
    }

    @Override
    public boolean add(VehicleSubTypeDTO subTypeDTO) throws BusinessException {
        VehicleTypeDTO vehicleTypeDTO = TypeCacheManger.getInstance().getVehicleType(subTypeDTO.getTypeId());
        if (Objects.isNull(vehicleTypeDTO)) {
            throw new BusinessException("", "类型" + subTypeDTO.getType() + "不存在！");
        }
        subTypeDTO.setType(vehicleTypeDTO.getType());

        if (isExistSubType(null, subTypeDTO.getType(), subTypeDTO.getSubType())) {
            throw new BusinessException("", "该类型已经存在该子类型了.");
        }

        subTypeDTO.setCategory(vehicleTypeDTO.getCategory());
        subTypeDTO.setCategoryId(vehicleTypeDTO.getCategoryId());
        VehicleTypeDO vehicleTypeDO = new VehicleTypeDO(subTypeDTO);
        vehicleSubTypeDao.insert(vehicleTypeDO);

        TypeCacheManger.getInstance().saveVehicleSubType(subTypeDTO);
        return true;
    }

    @Override
    public boolean isExistSubType(String id, String vehicleType, String name) {
        VehicleSubTypeDTO subTypeDTO = vehicleSubTypeDao.getByName(vehicleType, name);
        return Objects.nonNull(subTypeDTO) && !Objects.equals(id, subTypeDTO.getId());
    }

    @Override
    public boolean update(VehicleSubTypeDTO subTypeDTO) throws BusinessException {
        VehicleSubTypeDTO oldSubType = vehicleSubTypeDao.getById(subTypeDTO.getId());
        if (Objects.isNull(oldSubType)) {
            throw new BusinessException("", "对象不存在！");
        }

        VehicleTypeDTO vehicleTypeDTO = TypeCacheManger.getInstance().getVehicleType(subTypeDTO.getTypeId());
        if (Objects.isNull(vehicleTypeDTO)) {
            throw new BusinessException("", "类型" + subTypeDTO.getType() + "不存在！");
        }
        subTypeDTO.setType(vehicleTypeDTO.getType());

        if (isExistSubType(subTypeDTO.getId(), subTypeDTO.getType(), subTypeDTO.getSubType())) {
            throw new BusinessException("", "该类型已经存在该子类型了。");
        }

        subTypeDTO.setCategory(vehicleTypeDTO.getCategory());
        subTypeDTO.setCategoryId(vehicleTypeDTO.getCategoryId());
        VehicleTypeDO vehicleTypeDO = new VehicleTypeDO(subTypeDTO);

        vehicleSubTypeDao.update(vehicleTypeDO);
        TypeCacheManger.getInstance().saveVehicleSubType(vehicleSubTypeDao.getById(subTypeDTO.getId()));
        return true;
    }

    @Override
    public boolean delete(String id) throws BusinessException {
        VehicleSubTypeDTO subType = vehicleSubTypeDao.getById(id);
        if (Objects.isNull(subType)) {
            throw new BusinessException("", "对象不存在！");
        }

        //校验子类型是否被车辆绑定
        if (checkVehicleSubTypeIsBinding(id)) {
            throw new BusinessException("", "车辆子类型" + subType.getSubType() + "已绑定车辆,请先解除绑定再删除!");
        }

        vehicleSubTypeDao.deleteByBatch(Collections.singletonList(id));
        TypeCacheManger.getInstance().removeVehicleSubType(id);
        return true;
    }

    @Override
    public String deleteBatch(Collection<String> ids) {
        List<VehicleSubTypeDTO> vehicleSubTypeList = vehicleSubTypeDao.getByIds(ids);
        if (vehicleSubTypeList.isEmpty()) {
            return null;
        }

        //获取被车辆绑定的车辆子类型
        Set<String> bindIds = vehicleSubTypeDao.getVehicleBindTypeList(ids);
        List<String> deleteIds = new ArrayList<>();
        List<String> bindSubTypes = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        for (VehicleSubTypeDTO vehicleSubType : vehicleSubTypeList) {
            if (bindIds.contains(vehicleSubType.getId())) {
                bindSubTypes.add(vehicleSubType.getSubType());
                continue;
            }
            deleteIds.add(vehicleSubType.getId());
            msg.append("删除车辆子类型: ").append(vehicleSubType.getSubType()).append(" <br/>");
        }

        if (!deleteIds.isEmpty()) {
            vehicleSubTypeDao.deleteByBatch(deleteIds);
            deleteIds.forEach(o -> TypeCacheManger.getInstance().removeVehicleSubType(o));
        }
        String result = "";
        if (!bindSubTypes.isEmpty()) {
            result = "车辆子类型" + StringUtils.join(bindSubTypes, ",") + "已绑定车辆，请先解除绑定再删除!";
        }
        if (StringUtils.isNotBlank(msg.toString())) {
            logService.addLog(getIp(), msg.toString(), "3", "batch", "批量删除车型子类型");
        }
        return result;
    }

    @Override
    public VehicleSubTypeDTO getById(String id) {
        return vehicleSubTypeDao.getById(id);
    }

    @Override
    public List<VehicleSubTypeDTO> getAllByKeyword(String keyword) {
        return vehicleSubTypeDao.getByKeyword(StringUtil.mysqlLikeWildcardTranslation(keyword));
    }

    @Override
    public boolean checkVehicleSubTypeIsBinding(String id) {
        return !vehicleSubTypeDao.getVehicleBindTypeList(Collections.singletonList(id)).isEmpty();
    }

    @Override
    public Page<VehicleSubTypeDTO> getByPage(VehicleSubTypeQuery subTypeQuery) {
        String keyword = StringUtil.mysqlLikeWildcardTranslation(subTypeQuery.getSimpleQueryParam());
        subTypeQuery.setSimpleQueryParam(keyword);
        return PageHelperUtil.doSelect(subTypeQuery, () -> vehicleSubTypeDao.getByPage(subTypeQuery));
    }

    @Override
    public List<VehicleSubTypeDTO> getByType(String vehicleTypeId) {
        return vehicleSubTypeDao.getByType(vehicleTypeId);
    }

    private String getIp() {
        return new GetIpAddr().getIpAddr(request);
    }
}
