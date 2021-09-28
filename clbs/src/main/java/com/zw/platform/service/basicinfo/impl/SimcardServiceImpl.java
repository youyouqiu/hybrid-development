package com.zw.platform.service.basicinfo.impl;

import com.zw.platform.basic.dto.F3SimCardDTO;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.query.SimcardQuery;
import com.zw.platform.service.basicinfo.SimcardService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sim卡server实现
 * @author wangying
 */
@Service
public class SimcardServiceImpl implements SimcardService {

    @Autowired
    private SimCardNewDao simCardNewDao;

    @Autowired
    private UserService userService;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 根据id查询sim卡
     * @param id sim卡Id
     * @return sim卡信息
     */
    @Override
    @MethodLog(name = "根据id查询终端手机号", description = " 根据id查询终端手机号")
    public SimcardInfo findSimcardById(String id) {
        return simCardNewDao.findSimcardById(id);
    }

    /**
     * 根据编号查询sim卡
     * @param simcardNumber SIM卡号
     * @return SIM卡信息
     */
    @Override
    @MethodLog(name = "根据编号查询终端手机号", description = " 根据编号查询终端手机号")
    public SimcardInfo findVehicleBySimcardNumber(String simcardNumber) {
        return simCardNewDao.findSimcardBySimcardNumber(simcardNumber);
    }

    /**
     * 生成sim卡模板
     * @param response response
     * @return boolean
     * @throws Exception e
     */
    @Override
    @MethodLog(name = "生成sim卡模板", description = " 生成sim卡模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        // 获得当前用户所属企业及其下级企业名称
        List<String> groupNames = userService.getOrgNamesByUser();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("ICCID");
        headList.add("IMEI");
        headList.add("IMSI");
        headList.add("终端手机号");
        headList.add("所属企业");
        headList.add("启停状态");
        headList.add("运营商");
        headList.add("发放地市");
        headList.add("套餐流量");
        headList.add("修正系数");
        headList.add("预警系数");
        headList.add("小时流量阈值");
        headList.add("日流量阈值");
        headList.add("月流量阈值");
        headList.add("激活日期");
        headList.add("到期时间");
        headList.add("真实SIM卡号");
        headList.add("备注");
        // 必填字段
        requiredList.add("终端手机号");
        requiredList.add("所属企业");
        String dateString = DateFormatUtils.format(new Date(), DATE_FORMAT);
        // 默认设置一条数据
        exportList.add("5798663004753");
        exportList.add("启用");
        exportList.add("中国移动");
        exportList.add(dateString);
        exportList.add("1024");
        exportList.add(dateString);
        //所属企业
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("5798663004753");
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        //所属企业
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属企业", groupNameArr);
        }
        // 启停状态
        String[] startStatus = { "启用", "停用" };
        selectMap.put("启停状态", startStatus);
        // 运营商
        String[] operator = { "中国移动", "中国联通", "中国电信" };
        selectMap.put("运营商", operator);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        export.addCell(row, 3, exportList.get(0));
        export.addCell(row, 4, exportList.get(6));
        export.addCell(row, 5, exportList.get(1));
        export.addCell(row, 6, exportList.get(2));
        export.addCell(row, 8, exportList.get(4));
        export.addCell(row, 14, exportList.get(3));
        export.addCell(row, 15, exportList.get(5));
        export.addCell(row, 16, exportList.get(7));
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);
        out.close();

        return true;
    }

    @Override
    public List<Map<String, Object>> findSimCardByUser(SimcardQuery query) {
        List<String> userOrgListId = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return PageHelperUtil.doSelect(query, () -> simCardNewDao.findSimcardByUser(userOrgListId, query));
    }

    @Override
    public Map<String, Object> findSimcardGroupById(String id) {
        return simCardNewDao.findSimcardGroupById(id);
    }

    @Override
    public SimcardInfo findBySIMCard(String simcardNumber) {
        return simCardNewDao.findBySIMCard(simcardNumber);
    }


    @Override
    public int getIsBand(String id) {
        return simCardNewDao.getIsBand(id);
    }

    @Override
    public SimcardInfo isExist(String id, String simcardNumber) {
        return simCardNewDao.isExist(id, simcardNumber);
    }

    @Override
    public F3SimCardDTO getF3SimInfo(String id) {
        return simCardNewDao.getF3SimInfo(id);
    }
}
