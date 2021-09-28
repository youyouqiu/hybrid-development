package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.basicinfo.query.DeviceQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.repository.modules.TerminalTypeDao;
import com.zw.platform.service.basicinfo.DeviceService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.response.ResponseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangying
 * @deprecated since 4.4.0 replace com.zw.platform.basic.service.impl.DeviceServiceImpl
 */
@Service("deviceService")
public class DeviceServiceImpl implements DeviceService {
    private static final Logger log = LogManager.getLogger(DeviceServiceImpl.class);

    @Autowired
    private DeviceNewDao deviceNewDao;

    @Autowired
    private TerminalTypeDao terminalTypeDao;

    @Autowired
    private UserService userService;


    /**
     * 根据id查询终端
     */
    @Override
    @MethodLog(name = "根据id查询终端", description = "根据id查询终端")
    public DeviceInfo findDeviceById(String id) {
        return deviceNewDao.findById(id);
    }

    /**
     * 根据终端编号查询终端信息
     */
    @Override
    @MethodLog(name = "根据终端号查询终端", description = "根据终端号查询终端")
    public DeviceInfo findDeviceByDeviceNumber(String deviceNumber) {
        DeviceInfo di = deviceNewDao.findDeviceByDeviceNumber(deviceNumber);
        if (di != null) {
            //获取所属企业名
            OrganizationLdap ol = userService.getOrgByUuid(di.getGroupId());
            di.setGroupName(ol != null ? ol.getName() : "");
        }
        return di;
    }


    /**
     * 生成导入模板
     */
    @Override
    @MethodLog(name = "生成导入模板", description = "生成导入模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        // 获得当前用户所属企业及其下级企业名称
        List<String> groupNames = userService.getOrgNamesByUser();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("终端号");
        headList.add("所属企业");
        headList.add("通讯类型");
        headList.add("终端厂商");
        headList.add("终端型号");
        headList.add("功能类型");
        headList.add("终端名称");
        headList.add("制造商ID");
        headList.add("终端型号（注册）");
        headList.add("MAC地址");
        headList.add("制造商");
        headList.add("条码");
        headList.add("启停状态");
        // headList.add("监控对象");
        headList.add("安装时间");
        headList.add("采购时间");
        // headList.add("创建日期");
        // headList.add("修改日期");
        headList.add("安装单位");
        headList.add("联系人");
        headList.add("联系电话");
        headList.add("是否符合要求");
        headList.add("备注");
        // 必填字段
        requiredList.add("终端号");
        requiredList.add("所属企业");
        requiredList.add("通讯类型");
        requiredList.add("终端厂商");
        requiredList.add("终端型号");
        requiredList.add("功能类型");
        // 默认设置一条数据
        exportList.add("TH00300");
        //所属企业
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("交通部JT/T808-2013");
        exportList.add("[f]F3");
        exportList.add("F3-default");
        exportList.add("简易型车机");
        exportList.add("测试11");
        exportList.add("中位");
        exportList.add("型号741");
        exportList.add("");
        exportList.add("中位科技");
        exportList.add("1123572044597");
        exportList.add("启用");
        // exportList.add("");
        exportList.add("2016-12-01");
        exportList.add("2016-12-03");
        // exportList.add("2016-12-01");
        // exportList.add("2016-12-03");
        exportList.add("中位科技");
        exportList.add("");
        exportList.add("");
        exportList.add("是");
        exportList.add("终端信息");
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        //所属企业
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属企业", groupNameArr);
        }
        // 启停状态
        String[] startStop = { "启用", "停用" };
        selectMap.put("启停状态", startStop);
        // 是否符合需求
        String[] isRequirements = { "是", "否" };
        selectMap.put("是否符合要求", isRequirements);
        // 通讯类型
        Set<String> deviceTypeNameList = ProtocolEnum.DEVICE_TYPE_NAMES;
        String[] deviceType = new String[deviceTypeNameList.size()];
        deviceTypeNameList.toArray(deviceType);
        selectMap.put("通讯类型", deviceType);
        // 功能类型
        String[] functionalType = { "简易型车机", "行车记录仪", "对讲设备", "手咪设备", "超长待机设备", "定位终端" };
        selectMap.put("功能类型", functionalType);
        //终端厂商
        List<String> terminalManufacturer = terminalTypeDao.getTerminalManufacturer();
        String[] manuFacturer = new String[terminalManufacturer.size()];
        terminalManufacturer.toArray(manuFacturer);
        selectMap.put("终端厂商", manuFacturer);
        //终端型号
        List<TerminalTypeInfo> allTerminalType = terminalTypeDao.getAllTerminalType();
        String[] terminalType = new String[allTerminalType.size()];
        for (int i = 0; i < allTerminalType.size(); i++) {
            terminalType[i] = allTerminalType.get(i).getTerminalType();
        }
        selectMap.put("终端型号", terminalType);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        ResponseUtil.writeFile(response, export);

        return true;
    }

    @Override
    public Page<Map<String, Object>> findDeviceByUser(DeviceQuery query) {
        List<String> userOrgListId = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());

        return PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> deviceNewDao.findDeviceByUser(userOrgListId, query));
    }


    /**
     * 根据id查询终端
     */
    @Override
    @MethodLog(name = "根据id查询终端", description = "根据id查询终端")
    public Map<String, Object> findDeviceGroupById(String id) {
        return deviceNewDao.findDeviceGroupById(id);
    }

    @Override
    public DeviceInfo findByDevice(String deviceNumber) {
        return deviceNewDao.findbyDevice(deviceNumber);
    }

    @Override
    public String findGroupIdByNumber(String deviceNumber) {
        return deviceNewDao.fingGroupIdByDeviceNumber(deviceNumber);
    }

}
