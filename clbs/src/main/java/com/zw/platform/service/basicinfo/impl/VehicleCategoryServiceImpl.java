package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.repository.NewVehicleCategoryDao;
import com.zw.platform.basic.repository.NewVehicleTypeDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import com.zw.platform.service.basicinfo.VehicleCategoryService;
import com.zw.platform.service.basicinfo.VehicleTypeService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Deprecated
@Service("oldVehicleCategoryService")
public class VehicleCategoryServiceImpl implements VehicleCategoryService {
    private static Logger log = LogManager.getLogger(VehicleCategoryServiceImpl.class);

    @Autowired
    private NewVehicleCategoryDao newVehicleCategoryDao;

    @Autowired
    private NewVehicleTypeDao newVehicleTypeDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private VehicleTypeService vehicleTypeService;

    /**
     * 新增类别
     *
     * @param form      实体
     * @param ipAddress ip
     * @return true or false
     * @throws Exception exception
     * @authro zhouzongbo
     */
    @Override
    public boolean add(VehicleTypeForm form, String ipAddress) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        boolean flag = newVehicleCategoryDao.insert(new VehicleCategoryDO(form.convertCategory()));
        if (flag) { // 新增成功则记录日志
            String msg = "新增车辆类别：" + form.getVehicleCategory();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            return true;
        } else {
            return false;
        }
    }

    @Override
    @MethodLog(name = "分页查询 User", description = "分页查询 User")
    public Page<VehicleType> findByPage(VehicleTypeQuery query) throws Exception {
        List<VehicleCategoryDTO> categoryList =
            newVehicleCategoryDao.getByKeyword(StringUtil.mysqlLikeWildcardTranslation(query.getVehicleCategory()));
        List<VehicleType> result = new ArrayList<>();
        categoryList.forEach(category -> result.add(new VehicleType(category)));
        return RedisQueryUtil.getListToPage(result, query, result.size());
    }

    @Override
    public boolean delete(String id, String ipAddress) throws Exception {
        VehicleType vehicleType = get(id); // 根据id获取车辆类别信息
        boolean flag = newVehicleCategoryDao.delete(id); // 逻辑删车辆类别
        if (flag) { // 删除成功则记录日志
            String msg = "删除车辆类别：" + vehicleType.getVehicleCategory();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String deleteBatch(String ids, String ipAddress) throws Exception {
        String[] items = ids.split(",");
        StringBuilder result = new StringBuilder();
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            List<VehicleTypeDO> vehicleList = vehicleTypeService.findVehicleType(items[i]);
            VehicleCategoryDTO vehicleCategoryDTO = newVehicleCategoryDao.getById(items[i]);
            if (vehicleList.size() == 0) {
                boolean flag = newVehicleCategoryDao.delete(items[i]); // 逻辑删车辆类别
                if (flag) {
                    msg.append("删除车辆类别 : ").append(vehicleCategoryDTO.getCategory()).append(" <br/>");
                }
            } else {
                result.append(vehicleCategoryDTO.getCategory()).append(",");
            }
        }
        if (!"".equals(msg.toString())) {
            logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "批量删除车辆类别");
        }
        return result.toString();
    }

    @Override
    public VehicleType get(String id) throws Exception {
        VehicleCategoryDTO vehicleCategoryDTO = newVehicleCategoryDao.getById(id);
        if (vehicleCategoryDTO == null) {
            return null;
        }
        return new VehicleType(vehicleCategoryDTO);
    }

    @Override
    public VehicleCategoryDTO getByStandard(String id) throws Exception {
        return newVehicleCategoryDao.getById(id);
    }

    @Override
    public boolean update(VehicleTypeForm form, String ipAddress) throws Exception {
        boolean flag = false;
        String msg = "";
        VehicleType vehicleCategory = null;
        if (form != null && ipAddress != null && !"".equals(ipAddress)) {
            form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            form.setUpdateDataTime(new Date());
            vehicleCategory = get(form.getId());
            flag = newVehicleCategoryDao.update(new VehicleCategoryDO(form.convertCategory()));
        }
        if (flag && vehicleCategory != null) { // 修改成功则记录日志
            if (vehicleCategory.getVehicleCategory().equals(form.getVehicleCategory())) {
                msg = "修改类别：" + form.getVehicleCategory();
            } else {
                msg = "修改类别：" + vehicleCategory.getVehicleCategory() + " 修改为：" + form.getVehicleCategory();
            }
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            RedisHelper.delByPattern(HistoryRedisKeyEnum.DEVICE_VEHICLE_INFO_PATTERN.of());
            return true;
        } else {
            return false;
        }

    }

    @Override
    public VehicleCategoryDO findByVehicleType(String vehicleCategory) throws Exception {
        return newVehicleCategoryDao.findByCategory(vehicleCategory);
    }

    @Override
    public List<VehicleTypeDO> findVehicleTypeByCategoryId(String id) {
        return newVehicleTypeDao.findByCategoryIds(Collections.singletonList(id));
    }

    @Override
    public List<VehicleCategoryDO> findVehicleCategoryList() {
        return newVehicleCategoryDao.getAll();
    }
}
