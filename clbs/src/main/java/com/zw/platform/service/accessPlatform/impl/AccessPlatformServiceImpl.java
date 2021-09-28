package com.zw.platform.service.accessPlatform.impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.accessPlatform.AccessPlatform;
import com.zw.platform.domain.accessPlatform.AccessPlatformForm;
import com.zw.platform.domain.accessPlatform.AccessPlatformQuery;
import com.zw.platform.repository.modules.AccessPlatformDao;
import com.zw.platform.service.accessPlatform.AccessPlatformService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author LiaoYuecai
 * @create 2018-01-05 10:25
 * @desc
 */
@Service
public class AccessPlatformServiceImpl implements AccessPlatformService {

    @Autowired
    private AccessPlatformDao accessPlatformDao;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public AccessPlatform getByID(String id) throws Exception {
        return accessPlatformDao.getByID(id);
    }

    @Override
    public Page<AccessPlatform> find(AccessPlatformQuery query) {
        return PageHelperUtil.doSelect(query, () -> accessPlatformDao.find(query));
    }

    @Override
    public int add(AccessPlatform accessPlatform, String ipAddress) throws Exception {
        accessPlatform.setType(1); //默认为同级平台
        accessPlatform.setCreateDataTime(new Date());
        accessPlatform.setCreateDataUsername(SystemHelper.getCurrentUsername());
        Integer result = accessPlatformDao.add(accessPlatform);
        if (result > 0) {
            if (1 == accessPlatform.getStatus()) {
                WebSubscribeManager.getInstance().sendMsgToAll(accessPlatform, ConstantUtil.T808_ACCESS_PLATFORM_OPEN);
            }
            String message = "新增接入平台IP : " + accessPlatform.getIp();
            logSearchService.addLog(ipAddress, message, "3", "", "-", "");
        }
        return result;
    }

    @Override
    public int update(AccessPlatform accessPlatform, String ipAddress) throws Exception {
        AccessPlatform platform = accessPlatformDao.getByID(accessPlatform.getId());
        accessPlatform.setUpdateDataTime(new Date());
        accessPlatform.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        Integer result = accessPlatformDao.update(accessPlatform);
        if (result > 0 && platform != null) {
            WebSubscribeManager.getInstance().sendMsgToAll(accessPlatform, ConstantUtil.T808_ACCESS_PLATFORM_CLOSE);
            // 2秒后再发送新增平台指令
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    WebSubscribeManager.getInstance().sendMsgToAll(accessPlatform,
                        ConstantUtil.T808_ACCESS_PLATFORM_OPEN);
                }
            }, 2000);
            String berofIp = platform.getIp(); // 修改前的IP地址
            String nowIp = accessPlatform.getIp(); // 现在提交的IP地址
            String message = "";
            if (!berofIp.equals(nowIp)) {
                message = "修改接入平台Ip : " + berofIp + " 为 : " + nowIp;
            } else {
                message = "修改接入平台Ip为 : " + nowIp + " 的信息";
            }
            logSearchService.addLog(ipAddress, message, "3", "", "-", "");
        }
        return result;
    }

    @Override
    public boolean deleteById(String id, String ipAddress) throws Exception {
        if (!StringUtils.isEmpty(id)) {
            // 转换id
            List<String> ids = Arrays.asList(id.split(","));
            List<AccessPlatform> accessPlatforms = accessPlatformDao.getByIDs(ids);
            // 日志记录代码
            boolean flag = accessPlatformDao.deleteByIds(ids);
            StringBuffer message = new StringBuffer("删除接入平台IP : ");
            if (flag) {
                for (AccessPlatform ap : accessPlatforms) {
                    if (1 == ap.getStatus()) {
                        WebSubscribeManager.getInstance().sendMsgToAll(ap, ConstantUtil.T808_ACCESS_PLATFORM_CLOSE);
                    }
                    message.append(ap.getIp()).append(",");
                }
                // 日志记录
                logSearchService.addLog(ipAddress, message.toString().substring(0, message.length() - 1), "3", "", "-",
                    "");
            }
            return flag;
        }
        return false;
    }

    @Override
    public boolean check808InputPlatFormSole(String platFormName, String pid) throws Exception {
        String id = accessPlatformDao.check808InputPlatFormSole(platFormName);
        if (StringUtils.isEmpty(id) || pid.equals(id)) {
            return true;
        } else {
            return false;
        }
    }


    @MethodLog(name = "生成导入模板", description = "生成导入模板")
    public void downLoadFileTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        headList.add("平台名称");
        headList.add("状态");
        headList.add("IP地址");

        requiredList.add("平台名称");
        requiredList.add("状态");
        requiredList.add("IP地址");

        exportList.add("导入接入平台");
        exportList.add("关闭");
        exportList.add("192.168.24.11");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 性别
        String[] status = {"开启", "关闭"};
        selectMap.put("状态", status);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
    }

    @MethodLog(name = "批量导入", description = "批量导入")
    public Map importJoinUpPlateformIp(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        if (multipartFile.getSize() == 0) {
            resultMap.put("errorMsg", "请将导入文件按照模板格式整理后再导入");
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        Row row = importExcel.getRow(1);
        if (row == null) {
            resultMap.put("errorMsg", "请将导入文件按照模板格式整理后</br>填入合理数据再导入");
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        int cellNum = importExcel.getLastCellNum();
        if (cellNum != 3) {
            resultMap.put("errorMsg", "请将导入文件按照模板格式整理后再导入");
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        StringBuffer errorMsg = new StringBuffer();
        StringBuffer message = new StringBuffer();
        // excel 转换成 list
        List<AccessPlatformForm> accessPlatforms = importExcel.getDataList(AccessPlatformForm.class, null);
        List<AccessPlatform> importList = new ArrayList<>();
        String resultInfo = "";
        AccessPlatform resultData;
        // 校验需要导入的Device
        for (int i = 0; i < accessPlatforms.size(); i++) {
            AccessPlatformForm thingInfo = accessPlatforms.get(i);
            if ("REPEAT".equals(thingInfo.getPlatformName())) {
                continue;
            }
            // 列表中重复数据
            for (int j = accessPlatforms.size() - 1; j > i; j--) {
                if (accessPlatforms.get(j).getPlatformName().equals(thingInfo.getPlatformName())) {
                    String plateName = thingInfo.getPlatformName();
                    errorMsg.append("第").append(i + 1).append("行平台名称跟第").append(j + 1).append(
                        "行重复，值是：").append(plateName).append("<br/>");
                    accessPlatforms.get(j).setPlatformName("REPEAT");
                }
            }
        }
        for (int i = 0; i < accessPlatforms.size();i++) {
            AccessPlatformForm accessPlatform = accessPlatforms.get(i);
            resultData = new AccessPlatform();
            if ("REPEAT".equals(accessPlatform.getPlatformName())) {
                continue;
            }
            // 校验必填字段
            if (StringUtils.isBlank(accessPlatform.getPlatformName())
                || accessPlatform.getStatus() == null || StringUtils.isBlank(accessPlatform.getIp())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                continue;
            }

            // 校验数据的合法性
            if ("".equals(accessPlatform.getPlatformName())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据平台名称是非法数据");
                continue;
            } else {
                if (accessPlatformDao.findPlateformNameByName(accessPlatform.getPlatformName()).size() != 0) {
                    resultMap.put("flag", 0);
                    errorMsg.append("平台名称“").append(accessPlatform.getPlatformName()).append("”已存在").append("<br/>");
                    continue;
                }
            }
            if (!"开启".equals(accessPlatform.getStatus()) && !"关闭".equals(accessPlatform.getStatus())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据状态错误");
                continue;
            } else {
                if ("开启".equals(accessPlatform.getStatus())) {
                    resultData.setStatus(1);
                } else {
                    resultData.setStatus(0);
                }
            }
            // 要验证的字符串
            String str = accessPlatform.getIp();
            // 邮箱验证规则
            String regEx = "(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.)"
                + "(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.){2}"
                + "([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))";
            // 编译正则表达式
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(str);
            // 字符串是否与正则表达式相匹配
            boolean result = matcher.matches();
            if (!result) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据IP格式错误");
                continue;
            }
            resultData.setType(1); //默认为同级平台
            resultData.setPlatformName(accessPlatform.getPlatformName());
            resultData.setIp(accessPlatform.getIp());
            resultData.setCreateDataTime(new Date());
            resultData.setCreateDataUsername(SystemHelper.getCurrentUsername());
            importList.add(resultData);
            message.append("导入接入平台,名称为 :").append(accessPlatform.getPlatformName()).append("</br>");
        }
        // 组装导入结果
        if (importList.size() > 0) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = accessPlatformDao.addPlateformBatch(importList);
            if (flag) {
                resultInfo += "导入成功" + importList.size() + "条数据,导入失败"
                    + (accessPlatforms.size() - importList.size()) + "条数据。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入接入平台IP");
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
            }

        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "成功导入0条数据。");
        }
        return resultMap;
    }

    @Override
    public boolean exportFile(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcelUtil.setResponseHead(response, "808接入平台IP列表");
        List<AccessPlatformForm> data = accessPlatformDao.findAllIp();
        for (AccessPlatformForm form : data) {
            if ("0".equals(form.getStatus())) {
                form.setStatus("关");
            } else {
                form.setStatus("开");
            }
        }
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, data, AccessPlatformForm.class, null, response.getOutputStream()));
    }
}
