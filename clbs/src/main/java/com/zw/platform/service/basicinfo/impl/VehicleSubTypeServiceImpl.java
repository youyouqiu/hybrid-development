package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewVehicleSubTypeDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleSubTypeInfo;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import com.zw.platform.service.basicinfo.VehicleSubTypeService;
import com.zw.platform.service.basicinfo.VehicleTypeService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 子类型(ServiceImpl)
 *
 * @author zhouzongbo on 2018/4/17 9:32
 */
@Deprecated
@Service("oldVehicleSubTypeService")
public class VehicleSubTypeServiceImpl implements VehicleSubTypeService {

    private static Logger log = LogManager.getLogger(VehicleTypeServiceImpl.class);

    @Autowired
    private NewVehicleSubTypeDao newVehicleSubTypeDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private VehicleTypeService vehicleTypeService;

    @Override
    public boolean addSubType(VehicleSubTypeForm form, String ip) throws Exception {
        form.setCreateDataTime(new Date());
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        String vehicleTypeId = form.getPid();
        VehicleTypeDTO vehicleType = vehicleTypeService.get(vehicleTypeId);
        if (vehicleType == null) {
            return false;
        }
        form.setVehicleCategory(vehicleType.getCategoryId());
        form.setVehicleType(vehicleType.getType());
        boolean flag = newVehicleSubTypeDao.insert(new VehicleTypeDO(form.convert()));
        if (flag) {
            String msg = "新增子类型：" + form.getVehicleSubtypes();
            logSearchService.addLog(ip, msg, "3", "", "-", "");
        }
        return flag;
    }

    @Override
    public VehicleSubTypeDTO getSubTypeBy(String vehicleType, String vehicleSubType) {
        return newVehicleSubTypeDao.getByVehicleTypeAndSubType(vehicleType, vehicleSubType);
    }

    @Override
    public VehicleSubTypeInfo getVehicleSubTypeById(String id) {
        VehicleSubTypeDTO vehicleSubTypeDTO = newVehicleSubTypeDao.getById(id);
        if (vehicleSubTypeDTO == null) {
            return null;
        }
        return new VehicleSubTypeInfo(vehicleSubTypeDTO);
    }

    @Override
    public Page<VehicleSubTypeDTO> findVehicleSubTypePage(VehicleSubTypeQuery query) {
        if (query != null) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        return PageHelperUtil.doSelect(query, () -> newVehicleSubTypeDao.getByPage(query));
    }

    @Override
    public boolean updateSubType(VehicleSubTypeForm form, String ip) throws Exception {
        form.setUpdateDataTime(new Date());
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        String vehicleTypeId = form.getPid();
        VehicleTypeDTO vehicleType = vehicleTypeService.get(vehicleTypeId);
        if (vehicleType == null) {
            return false;
        }
        form.setVehicleCategory(vehicleType.getCategoryId());
        form.setVehicleType(vehicleType.getType());

        VehicleSubTypeDTO oldVehicleSubTypeInfo = newVehicleSubTypeDao.getById(form.getId());
        String message = "";
        if (oldVehicleSubTypeInfo != null) {
            if (oldVehicleSubTypeInfo.getSubType().equals(form.getVehicleSubtypes())) {
                message = "修改车辆子类型: " + form.getVehicleSubtypes();
            } else {
                message =
                        "修改车辆子类型: " + oldVehicleSubTypeInfo.getSubType() + " 修改为: " + form.getVehicleSubtypes();
            }
        }
        boolean flag = newVehicleSubTypeDao.update(new VehicleTypeDO(form.convert()));
        if (flag) {
            logSearchService.addLog(ip, message, "3", "", "", "");
        }
        return flag;
    }

    @Override
    public boolean checkVehicleSubTypeIsBinding(String id) {
        return newVehicleSubTypeDao.checkVehicleSubTypeIsBinding(id);
    }

    @Override
    public JsonResultBean deleteSubType(String id, String ip, int mark) throws Exception {
        // 单个删除
        if (mark == 0) {
            VehicleSubTypeDTO vehicleSubType = newVehicleSubTypeDao.getById(id);
            boolean flag = newVehicleSubTypeDao.delete(id);
            // 修改成功
            if (flag) {
                logSearchService.addLog(ip, "删除车辆子类型: " + vehicleSubType.getSubType(), "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } else {
            //批量删除
            String[] ids = id.split(",");
            StringBuilder sb = new StringBuilder();
            StringBuilder result = new StringBuilder();
            for (String nowId : ids) {
                // 验证是否绑定(true:绑定; false: 未绑定)
                boolean isBinding = newVehicleSubTypeDao.checkVehicleSubTypeIsBinding(nowId);
                VehicleSubTypeDTO vehicleSubType = newVehicleSubTypeDao.getById(nowId);
                if (!isBinding) {
                    boolean flag = newVehicleSubTypeDao.delete(nowId);
                    // 修改成功
                    if (flag) {
                        sb.append("删除车辆子类型: ").append(vehicleSubType.getSubType()).append(" <br/>");
                    }
                } else {
                    result.append(vehicleSubType.getSubType()).append(",");
                }
            }
            if (StringUtils.isNotBlank(sb.toString())) {
                logSearchService.addLog(ip, sb.toString(), "3", "batch", "批量删除车型子类型");
                if (StringUtils.isNotBlank(result.toString())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "车辆子类型" + result.toString() + "已绑定车辆，请先解除绑定再删除!");
                }
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else if (StringUtils.isNotBlank(result.toString())) {
                return new JsonResultBean(JsonResultBean.FAULT, "车辆子类型" + result.toString() + "已绑定车辆，请先解除绑定再删除!");
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public void exportSubType(String title, int type, HttpServletResponse response) {
        ExportExcel exportExcel = new ExportExcel(title, VehicleSubTypeForm.class, type, null);
        List<VehicleSubTypeForm> list = new ArrayList<>();
        List<VehicleSubTypeDTO> vehicleSubTypeList = newVehicleSubTypeDao.getByKeyword(null);
        for (VehicleSubTypeDTO vehicleSubTypeInfo : vehicleSubTypeList) {
            Integer drivingWay = vehicleSubTypeInfo.getDrivingWay();
            String drivingWayStr = null;
            switch (drivingWay) {
                case 0:
                    drivingWayStr = "自行";
                    break;
                case 1:
                    drivingWayStr = "运输";
                    break;
                default:
                    break;
            }
            VehicleSubTypeForm vehicleSubTypeForm = new VehicleSubTypeForm(vehicleSubTypeInfo);
            vehicleSubTypeForm.setDrivingWay(drivingWayStr);
            list.add(vehicleSubTypeForm);
        }
        exportExcel.setDataList(list);
        try (OutputStream outputStream = response.getOutputStream()) {
            exportExcel.write(outputStream);
        } catch (Exception e) {
            log.error("导出车辆子类型列表异常", e);
        }
    }

    @Override
    public VehicleSubTypeInfo getSubTypeBySubName(String vehicleSubType) {
        VehicleSubTypeDTO vehicleSubTypeDTO = newVehicleSubTypeDao.findByVehicleSubType(vehicleSubType);
        if (vehicleSubTypeDTO == null) {
            return null;
        }
        return new VehicleSubTypeInfo(vehicleSubTypeDTO);
    }

    @Override
    public VehicleSubTypeInfo getSubTypeByVehicleId(String vehicleId) {
        VehicleSubTypeDTO vehicleSubTypeDTO = newVehicleSubTypeDao.findByVehicleId(vehicleId);
        if (vehicleSubTypeDTO == null) {
            return null;
        }
        return new VehicleSubTypeInfo(vehicleSubTypeDTO);
    }
}
