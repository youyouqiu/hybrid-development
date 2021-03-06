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
        accessPlatform.setType(1); //?????????????????????
        accessPlatform.setCreateDataTime(new Date());
        accessPlatform.setCreateDataUsername(SystemHelper.getCurrentUsername());
        Integer result = accessPlatformDao.add(accessPlatform);
        if (result > 0) {
            if (1 == accessPlatform.getStatus()) {
                WebSubscribeManager.getInstance().sendMsgToAll(accessPlatform, ConstantUtil.T808_ACCESS_PLATFORM_OPEN);
            }
            String message = "??????????????????IP : " + accessPlatform.getIp();
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
            // 2?????????????????????????????????
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    WebSubscribeManager.getInstance().sendMsgToAll(accessPlatform,
                        ConstantUtil.T808_ACCESS_PLATFORM_OPEN);
                }
            }, 2000);
            String berofIp = platform.getIp(); // ????????????IP??????
            String nowIp = accessPlatform.getIp(); // ???????????????IP??????
            String message = "";
            if (!berofIp.equals(nowIp)) {
                message = "??????????????????Ip : " + berofIp + " ??? : " + nowIp;
            } else {
                message = "??????????????????Ip??? : " + nowIp + " ?????????";
            }
            logSearchService.addLog(ipAddress, message, "3", "", "-", "");
        }
        return result;
    }

    @Override
    public boolean deleteById(String id, String ipAddress) throws Exception {
        if (!StringUtils.isEmpty(id)) {
            // ??????id
            List<String> ids = Arrays.asList(id.split(","));
            List<AccessPlatform> accessPlatforms = accessPlatformDao.getByIDs(ids);
            // ??????????????????
            boolean flag = accessPlatformDao.deleteByIds(ids);
            StringBuffer message = new StringBuffer("??????????????????IP : ");
            if (flag) {
                for (AccessPlatform ap : accessPlatforms) {
                    if (1 == ap.getStatus()) {
                        WebSubscribeManager.getInstance().sendMsgToAll(ap, ConstantUtil.T808_ACCESS_PLATFORM_CLOSE);
                    }
                    message.append(ap.getIp()).append(",");
                }
                // ????????????
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


    @MethodLog(name = "??????????????????", description = "??????????????????")
    public void downLoadFileTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        headList.add("????????????");
        headList.add("??????");
        headList.add("IP??????");

        requiredList.add("????????????");
        requiredList.add("??????");
        requiredList.add("IP??????");

        exportList.add("??????????????????");
        exportList.add("??????");
        exportList.add("192.168.24.11");

        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // ??????
        String[] status = {"??????", "??????"};
        selectMap.put("??????", status);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
        out.close();
    }

    @MethodLog(name = "????????????", description = "????????????")
    public Map importJoinUpPlateformIp(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        if (multipartFile.getSize() == 0) {
            resultMap.put("errorMsg", "??????????????????????????????????????????????????????");
            resultMap.put("resultInfo", "???????????????");
            return resultMap;
        }
        // ???????????????
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        Row row = importExcel.getRow(1);
        if (row == null) {
            resultMap.put("errorMsg", "?????????????????????????????????????????????</br>???????????????????????????");
            resultMap.put("resultInfo", "???????????????");
            return resultMap;
        }
        int cellNum = importExcel.getLastCellNum();
        if (cellNum != 3) {
            resultMap.put("errorMsg", "??????????????????????????????????????????????????????");
            resultMap.put("resultInfo", "???????????????");
            return resultMap;
        }
        StringBuffer errorMsg = new StringBuffer();
        StringBuffer message = new StringBuffer();
        // excel ????????? list
        List<AccessPlatformForm> accessPlatforms = importExcel.getDataList(AccessPlatformForm.class, null);
        List<AccessPlatform> importList = new ArrayList<>();
        String resultInfo = "";
        AccessPlatform resultData;
        // ?????????????????????Device
        for (int i = 0; i < accessPlatforms.size(); i++) {
            AccessPlatformForm thingInfo = accessPlatforms.get(i);
            if ("REPEAT".equals(thingInfo.getPlatformName())) {
                continue;
            }
            // ?????????????????????
            for (int j = accessPlatforms.size() - 1; j > i; j--) {
                if (accessPlatforms.get(j).getPlatformName().equals(thingInfo.getPlatformName())) {
                    String plateName = thingInfo.getPlatformName();
                    errorMsg.append("???").append(i + 1).append("?????????????????????").append(j + 1).append(
                        "?????????????????????").append(plateName).append("<br/>");
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
            // ??????????????????
            if (StringUtils.isBlank(accessPlatform.getPlatformName())
                || accessPlatform.getStatus() == null || StringUtils.isBlank(accessPlatform.getIp())) {
                resultMap.put("flag", 0);
                errorMsg.append("???").append(i + 1).append("???????????????????????????<br/>");
                continue;
            }

            // ????????????????????????
            if ("".equals(accessPlatform.getPlatformName())) {
                resultMap.put("flag", 0);
                errorMsg.append("???").append(i + 1).append("????????????????????????????????????");
                continue;
            } else {
                if (accessPlatformDao.findPlateformNameByName(accessPlatform.getPlatformName()).size() != 0) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???????????????").append(accessPlatform.getPlatformName()).append("????????????").append("<br/>");
                    continue;
                }
            }
            if (!"??????".equals(accessPlatform.getStatus()) && !"??????".equals(accessPlatform.getStatus())) {
                resultMap.put("flag", 0);
                errorMsg.append("???").append(i + 1).append("?????????????????????");
                continue;
            } else {
                if ("??????".equals(accessPlatform.getStatus())) {
                    resultData.setStatus(1);
                } else {
                    resultData.setStatus(0);
                }
            }
            // ?????????????????????
            String str = accessPlatform.getIp();
            // ??????????????????
            String regEx = "(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.)"
                + "(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.){2}"
                + "([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))";
            // ?????????????????????
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(str);
            // ??????????????????????????????????????????
            boolean result = matcher.matches();
            if (!result) {
                resultMap.put("flag", 0);
                errorMsg.append("???").append(i + 1).append("?????????IP????????????");
                continue;
            }
            resultData.setType(1); //?????????????????????
            resultData.setPlatformName(accessPlatform.getPlatformName());
            resultData.setIp(accessPlatform.getIp());
            resultData.setCreateDataTime(new Date());
            resultData.setCreateDataUsername(SystemHelper.getCurrentUsername());
            importList.add(resultData);
            message.append("??????????????????,????????? :").append(accessPlatform.getPlatformName()).append("</br>");
        }
        // ??????????????????
        if (importList.size() > 0) {
            // ???????????????????????????????????????????????????????????????????????????
            boolean flag = accessPlatformDao.addPlateformBatch(importList);
            if (flag) {
                resultInfo += "????????????" + importList.size() + "?????????,????????????"
                    + (accessPlatforms.size() - importList.size()) + "????????????";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "??????????????????IP");
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "???????????????");
            }

        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "????????????0????????????");
        }
        return resultMap;
    }

    @Override
    public boolean exportFile(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcelUtil.setResponseHead(response, "808????????????IP??????");
        List<AccessPlatformForm> data = accessPlatformDao.findAllIp();
        for (AccessPlatformForm form : data) {
            if ("0".equals(form.getStatus())) {
                form.setStatus("???");
            } else {
                form.setStatus("???");
            }
        }
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, data, AccessPlatformForm.class, null, response.getOutputStream()));
    }
}
