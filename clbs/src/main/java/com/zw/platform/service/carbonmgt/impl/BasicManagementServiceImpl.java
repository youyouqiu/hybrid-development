package com.zw.platform.service.carbonmgt.impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.vas.carbonmgt.FuelType;
import com.zw.platform.domain.vas.carbonmgt.TimingStored;
import com.zw.platform.domain.vas.carbonmgt.form.BasicManagementForm;
import com.zw.platform.domain.vas.carbonmgt.form.FuelTypeForm;
import com.zw.platform.domain.vas.carbonmgt.query.BasicManagementQuery;
import com.zw.platform.domain.vas.carbonmgt.query.FuelTypeQuery;
import com.zw.platform.repository.vas.BasicManagementDao;
import com.zw.platform.service.carbonmgt.BasicManagementService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by 王健宇 on 2017/2/17.
 */
@Service
public class BasicManagementServiceImpl implements BasicManagementService {
    private static Logger logger = LogManager.getLogger(BasicManagementServiceImpl.class);

    @Value("${fuel.type.exist}")
    private String fuelTypeExist;

    @Autowired
    private BasicManagementDao basicManagementDao;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Override
    public List<TimingStored> oilPricesQuery(String timeStart, String timeEnd, String district, String oiltype)
        throws Exception {
        return basicManagementDao.oilPricesQuery(timeStart, timeEnd, district, oiltype);
    }

    @Override
    public Page<BasicManagementForm> find(BasicManagementQuery query) throws Exception {
        Page<BasicManagementForm> list = new Page<>();
        // 当前登录用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getOrgUuidsByUser(userId);
        if (StringUtils.isNotEmpty(userId) && CollectionUtils.isNotEmpty(orgList)) {
            list = PageHelperUtil.doSelect(query, () -> basicManagementDao.find(query, userId, orgList));
        }
        if (null != list && list.size() > 0) {
            // 处理list，将groupId对应的groupName给result相应的值赋上
            List<OrganizationLdap> orgLdapList = userService.getAllOrganization();
            for (BasicManagementForm form : list) {
                form.setSavingProductsInstallTime(
                    Converter.toString(Converter.toDate(form.getSavingProductsInstallTime()), "yyyy-MM-dd HH:mm:ss"));
                // 将groupId对应的groupName
                String groupId = form.getGroupId();
                if (StringUtils.isNotBlank(groupId)) {
                    String groupName = "";
                    for (OrganizationLdap orgLdap : orgLdapList) {
                        if (Converter.toBlank(orgLdap.getId()).equals(groupId)) {
                            groupName = Converter.toBlank(orgLdap.getName());
                            break;
                        }
                    }
                    form.setGroupName(groupName);
                }
            }
            return list;
        }
        return list;
    }

    @Override
    public boolean addMobileSourceBaseInfo(BasicManagementForm form) throws Exception {
        String userName = SystemHelper.getCurrentUsername();
        form.setCreateDataUsername(userName);
        return basicManagementDao.addMobileSourceBaseInfo(form);
    }

    @Override
    public BasicManagementForm getMobileSourceBaseinfoByVid(String vehicleId) throws BusinessException {
        BasicManagementForm form = basicManagementDao.getBaseInfoByVehicleId(vehicleId);
        if (null != form) {
            form.setSavingProductsInstallTime(
                Converter.toString(Converter.toDate(form.getSavingProductsInstallTime()), "yyyy-MM-dd HH:mm:ss"));
        }
        return form;
    }

    @Override
    public boolean editMobileSourceBaseinfo(BasicManagementForm form) throws BusinessException {
        String userName = SystemHelper.getCurrentUsername();
        form.setUpdateDataUsername(userName);
        form.setSavingProductsInstallTime(
            Converter.toString(Converter.toDate(form.getSavingProductsInstallTime()), "yyyy-MM-dd HH:mm:ss"));
        return basicManagementDao.editMobileSourceBaseinfo(form);

    }

    @Override
    public String getPlateColorByPlateColorId(String plateColor) throws BusinessException {
        String result = "";
        if (Converter.toBlank(plateColor).equals("0")) {
            result = "蓝";
        } else if (Converter.toBlank(plateColor).equals("1")) {
            result = "黄";
        } else if (Converter.toBlank(plateColor).equals("2")) {
            result = "白";
        } else if (Converter.toBlank(plateColor).equals("3")) {
            result = "黑";
        }
        return result;
    }

    @Override
    public boolean deleteMobileSourceBaseinfo(String vehicleId) throws BusinessException {
        return basicManagementDao.deleteMobileSourceBaseinfo(vehicleId);
    }

    @Override
    public Page<FuelTypeQuery> findFuelTypeByPage(FuelTypeQuery query) {
        return PageHelperUtil.doSelect(query, () -> basicManagementDao.findFuelTypeList(query));

    }

    @Override
    public boolean addFuelType(FuelTypeForm fuelType, String ipAddress) throws Exception {
        boolean flag = false;
        if (fuelType != null && ipAddress != null && !"".equals(ipAddress)) {
            fuelType.setCreateDataUsername(SystemHelper.getCurrentUsername());
            fuelType.setCreateDataTime(new Date());
            flag = basicManagementDao.addFuelType(fuelType);
        }
        if (flag) { // 新增成功则记录日志
            String msg = "新增燃料类型：" + fuelType.getFuelType();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<FuelType> findFuelType(String fuelType) {
        return basicManagementDao.findFuelType(fuelType);

    }

    @Override
    public FuelType get(String id) {
        return basicManagementDao.get(id);

    }

    @Override
    public JsonResultBean updateFuelType(FuelTypeForm form, String ipAddress) throws Exception {
        List<FuelType> list = findFuelType(form.getFuelType());
        FuelType fuelType = get(form.getId());
        String beforeFuelType = fuelType.getFuelType(); // 修改前的燃料类型
        String nowFuelType = form.getFuelType(); // 将要修改的燃料类型
        if (list.size() != 0 && !beforeFuelType.equals(nowFuelType)) {
            return new JsonResultBean(JsonResultBean.FAULT, fuelTypeExist);
        }
        form.setUpdateDataTime(new Date());// 修改时间
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // 修改人名字
        basicManagementDao.updateFuelType(form);
        String message = "";
        // 分为两种情况的修改，第一种 修改备注 第二种修改类型和备注
        if (beforeFuelType.equals(nowFuelType)) { // 修改备注
            message = "修改燃料类型：" + form.getFuelType();
        } else if (!beforeFuelType.equals(nowFuelType)) { // 修改类型
            message = "修改燃料类型：" + fuelType.getFuelType() + " 修改为：" + form.getFuelType();
        }
        logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 删除燃料类型
     * @param id
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteFuelType(String id, String ipAddress) throws Exception {
        FuelType fuelType = get(id);
        boolean flag = basicManagementDao.deleteFuel(id);
        if (flag && fuelType != null) { // 删除成功则记录日志
            logSearchServiceImpl.addLog(ipAddress, "删除燃料类型：" + fuelType.getFuelType(), "3", "", "-", "");
            return true;
        }
        return false;
    }

    /**
     * 批量删除燃料类型
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteFuelTypeMuch(List<String> ids, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        for (String id : ids) {
            FuelType fuelType = get(id);
            if (fuelType != null) {
                message.append("燃料类型 : ").append(fuelType.getFuelType()).append(" <br/>");
            }
        }
        boolean flag = basicManagementDao.deleteFuelTypeMuch(ids);
        if (flag) {
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量删除燃料类型");
            return true;
        }
        return false;
    }
}
