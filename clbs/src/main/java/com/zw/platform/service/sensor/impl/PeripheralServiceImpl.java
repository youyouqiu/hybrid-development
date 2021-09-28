package com.zw.platform.service.sensor.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.vas.f3.Peripheral;
import com.zw.platform.repository.vas.PeripheralDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sensor.PeripheralService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportNewExcel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 车辆外设管理
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月08日 17:50
 */
@Service
public class PeripheralServiceImpl implements PeripheralService, IpAddressService {
    private static final Logger log = LogManager.getLogger(PeripheralServiceImpl.class);

    @Autowired
    private PeripheralDao peripheralDao;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${add.fail}")
    private String addFail;

    @Value("${edit.success}")
    private String editSuccess;

    @Value("${peripheral.id.number}")
    private String peripheralIdNumber;

    @Value("${peripheral.id.length.exist}")
    private String peripheralIdAndLengthExist;

    @Value("${peripheral.no.exist}")
    private String peripheralNoExist;

    @Value("${peripheral.name.exist}")
    private String peripheralNameExist;

    private static final String REGEX = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}$";

    @Override
    public Page<Peripheral> findByPage(AssignmentQuery query) throws Exception {
        query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        return PageHelperUtil.doSelect(query, () -> peripheralDao.findByPage(query));
    }

    @Override
    public List<Peripheral> findAllow() {
        return this.peripheralDao.findAllow();
    }

    @Override
    public Peripheral findById(String id) {
        return this.peripheralDao.get(id);
    }

    @Override
    public List<Peripheral> findByIdentId(String identId) {
        return this.peripheralDao.getByIdentId(identId);
    }

    @Override
    public List<Peripheral> getByIdentName(String identName) {
        return this.peripheralDao.getByIdentName(identName);
    }

    @Override
    public Integer getConfigCountById(String id) {
        return this.peripheralDao.getConfigCountById(id);
    }

    @Override
    public JsonResultBean deleteById(String id) {
        Integer count = peripheralDao.getConfigCountById(id);
        Peripheral p = peripheralDao.get(id);
        if (count > 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "外设参数[" + p.getName() + "]正在使用中，不能删除");
        }
        boolean flag = peripheralDao.delete(id);
        if (flag) {
            String msg = "删除外设设置：" + p.getName();
            logSearchService.addLog(getIpAddress(), msg, "3", "车辆外设管理", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean addPeripheral(Peripheral peripheral) {
        peripheral.setCreateDataUsername(SystemHelper.getCurrentUsername());
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(peripheral.getIdentId());
        boolean regFlag = m.find();
        // 包含特殊字符
        if (regFlag) {
            return new JsonResultBean(JsonResultBean.FAULT, peripheralIdNumber);
        } else {
            int num = Integer.parseInt(peripheral.getIdentId().toLowerCase().replace("0x", ""), 16);
            if (0x00 > num || num > 0xFF) {
                return new JsonResultBean(JsonResultBean.FAULT, peripheralIdNumber);
            }
        }
        boolean flag = peripheralDao.add(peripheral);
        if (flag) {
            String msg = "新增外设设置：" + peripheral.getName();
            logSearchService.addLog(getIpAddress(), msg, "3", "车辆外设管理", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS, addSuccess);
        }
        return new JsonResultBean(JsonResultBean.FAULT, addFail);
    }

    @Override
    public JsonResultBean updatePeripheral(Peripheral peripheral) {
        String peripheralId = peripheral.getId();
        String identId = peripheral.getIdentId();
        String identIdUpperCase = identId.toUpperCase();
        List<Peripheral> peripheralList = findByIdentId(identId);
        if (!"0XE3".equals(identIdUpperCase)) {
            if (CollectionUtils.isNotEmpty(peripheralList)) {
                for (Peripheral p : peripheralList) {
                    if (!peripheralId.equals(p.getId())) {
                        return new JsonResultBean(JsonResultBean.FAULT, peripheralNoExist);
                    }
                }
            }
        } else {
            String ml = peripheral.getMsgLength() == null ? "" : peripheral.getMsgLength().toString();
            String idAndLength = identIdUpperCase + ml;
            for (Peripheral p : peripheralList) {
                String msgLength = p.getMsgLength() == null ? "" : p.getMsgLength().toString();
                String newIdAndLength = p.getIdentId().toUpperCase() + msgLength;
                if (idAndLength.equals(newIdAndLength) && !p.getId().equals(peripheralId)) {
                    return new JsonResultBean(JsonResultBean.FAULT, peripheralIdAndLengthExist);
                }
            }
        }
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(identId);
        // 包含特殊字符
        if (matcher.find()) {
            return new JsonResultBean(JsonResultBean.FAULT, peripheralIdNumber);
        } else {
            int num = Integer.parseInt(identId.toLowerCase().replace("0x", ""), 16);
            // 超出范围(0-255)
            if (0x00 > num || num > 0xFF) {
                return new JsonResultBean(JsonResultBean.FAULT, peripheralIdNumber);
            }
        }
        List<Peripheral> tp1 = this.getByIdentName(peripheral.getName());
        if (tp1 != null && tp1.size() > 0) {
            for (Peripheral p : tp1) {
                if (!peripheralId.equals(p.getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, peripheralNameExist);
                }
            }
        }
        peripheral.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = peripheralDao.update(peripheral);
        if (flag) {
            String msg = "修改外设设置：" + peripheral.getName();
            logSearchService.addLog(getIpAddress(), msg, "3", "车辆外设管理", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS, editSuccess);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteByBatch(String[] item) {
        List<String> ids = Arrays.stream(item).collect(Collectors.toList());
        StringBuilder message = new StringBuilder();
        StringBuilder errMessage = new StringBuilder();
        String hintMessage = "部分外设已和车辆绑定,到【外设轮询】中解除绑定才可以删除呦!<br/> 已绑定外设如下: <br>";
        for (String id : ids) {
            Peripheral p = peripheralDao.get(id);
            Integer count = peripheralDao.getConfigCountById(id);
            if (count > 0) {
                errMessage.append(p.getName()).append("<br/>");
            } else {
                message.append("删除外设 : ").append(p.getName()).append(" <br/>");
            }
        }
        if (errMessage.length() > 0) {
            hintMessage = hintMessage + errMessage;
            return new JsonResultBean(JsonResultBean.FAULT, hintMessage);
        }
        boolean flag = this.peripheralDao.unbindFenceByBatch(ids);
        if (flag) {
            logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "批量删除外设设置");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Map<String, Object> addImportPeripheral(MultipartFile multipartFile) throws Exception {
        Map<String, Object> resultMap = new HashMap<>(16);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // 导入的文件
        ImportNewExcel importExcel = new ImportNewExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<Peripheral> peripheralList = importExcel.getDataList(Peripheral.class);
        if (CollectionUtils.isEmpty(peripheralList)) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", "导入数据为空");
            resultMap.put("resultInfo", "成功导入0条数据!");
            return resultMap;
        }
        List<Peripheral> importList = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        Date nowDate = new Date();
        String currentUsername = SystemHelper.getCurrentUsername();
        List<Peripheral> allPeripheral = peripheralDao.findAllow();
        Set<String> alreadyExistPeripheralIds =
            allPeripheral.stream().map(Peripheral::getIdentId).collect(Collectors.toSet());
        Set<String> alreadyExistPeripheralNames =
            allPeripheral.stream().map(Peripheral::getName).collect(Collectors.toSet());
        // 外设id和下标map
        Map<String, Integer> peripheralIdAndIndexMap = new HashMap<>(16);
        // 外设名称和小标map
        Map<String, Integer> peripheralNameAndIndexMap = new HashMap<>(16);
        for (int i = 0; i < peripheralList.size(); i++) {
            Peripheral peripheral = peripheralList.get(i);
            // 外设id
            String peripheralId = peripheral.getIdentId();
            boolean peripheralIdIsNotBlank = StringUtils.isNotBlank(peripheralId);
            // 外设名称
            String peripheralName = peripheral.getName();
            boolean peripheralNameIsNotBlank = StringUtils.isNotBlank(peripheralName);
            // 外设id或者名称是重复的
            boolean peripheralIdOrNameIsRepeat = false;
            if (peripheralIdIsNotBlank) {
                Integer peripheralIdIndex = peripheralIdAndIndexMap.get(peripheralId);
                if (peripheralIdIndex == null) {
                    peripheralIdAndIndexMap.put(peripheralId, i);
                } else {
                    peripheralIdOrNameIsRepeat = true;
                    errorMsg.append("第").append(i + 1).append("条【外设ID】跟第").append(peripheralIdIndex + 1)
                        .append("条重复，值是：").append(peripheralId).append("<br/>");
                }
            }
            if (peripheralNameIsNotBlank) {
                Integer peripheralNameIndex = peripheralNameAndIndexMap.get(peripheralName);
                if (peripheralNameIndex == null) {
                    peripheralNameAndIndexMap.put(peripheralName, i);
                } else {
                    peripheralIdOrNameIsRepeat = true;
                    errorMsg.append("第").append(i + 1).append("条【外设名称】跟第").append(peripheralNameIndex + 1)
                        .append("条重复，值是：").append(peripheralName).append("<br/>");
                }
            }
            if (peripheralIdOrNameIsRepeat) {
                continue;
            }
            if (!peripheralIdIsNotBlank) {
                errorMsg.append("第").append(i + 1).append("条数据【外设ID】异常，外设ID不能为空 <br/>");
                continue;
            }
            if (!peripheralNameIsNotBlank) {
                errorMsg.append("第").append(i + 1).append("条数据【外设名称】异常，外设名称不能为空 <br/>");
                continue;
            }
            if (!Pattern.matches(REGEX, peripheralName)) {
                errorMsg.append("第").append(i + 1).append("条数据【外设名称】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位 <br/>");
                continue;
            }
            try {
                if (peripheralId.length() != 4) {
                    errorMsg.append("第").append(i + 1).append("条数据【外设ID】异常，外设ID不是正确的数字格式(0x00~0xFF) <br/>");
                    continue;
                }
                if (!peripheralId.toLowerCase().startsWith("0x")) {
                    errorMsg.append("第").append(i + 1).append("条数据【外设ID】异常，外设ID不是正确的数字格式(0x00~0xFF)<br/>");
                    continue;
                }
                int num = Integer.parseInt(peripheralId.toLowerCase().replace("0x", ""), 16);
                if (0x00 > num || num > 0xFF) {
                    errorMsg.append("第").append(i + 1).append("条数据【外设ID】异常，外设ID不是正确的数字格式(0x00~0xFF)<br/>");
                    continue;
                }
            } catch (Exception ex) {
                errorMsg.append("第").append(i + 1).append("条数据【外设ID】异常，外设ID不是正确的数字格式(0x00~0xFF)<br/>");
                continue;
            }
            Integer msgLength = peripheral.getMsgLength();
            if (msgLength != null && (msgLength > 255 || msgLength < 0)) {
                errorMsg.append("第").append(i + 1).append("条数据【外设消息】异常，外设消息长度值应在255以内<br/>");
                continue;
            }

            if (alreadyExistPeripheralIds.contains(peripheralId)) {
                errorMsg.append("第").append(i + 1).append("条数据【外设ID】异常，外设ID已存在<br/>");
                continue;
            }
            if (alreadyExistPeripheralNames.contains(peripheralName)) {
                errorMsg.append("第").append(i + 1).append("条数据【外设名称】异常，外设名称已存在<br/>");
                continue;
            }
            peripheral.setCreateDataTime(nowDate);
            peripheral.setCreateDataUsername(currentUsername);
            importList.add(peripheral);
            message.append("导入外设 : ").append(peripheralName).append(" <br/>");
        }
        int importSize = importList.size();
        if (importSize > 0) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = peripheralDao.addBatch(importList);
            if (flag) {
                resultInfo += "导入成功" + importSize + "条数据,导入失败" + (peripheralList.size() - importSize) + "条数据。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "导入外设");
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }
        } else {
            resultInfo += "导入成功" + importSize + "条数据,导入失败" + (peripheralList.size() - importSize) + "条数据。";
            resultMap.put("flag", 1);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", resultInfo);
        }
        return resultMap;
    }

    /**
     * 导出
     */
    @Override
    public boolean export(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, Peripheral.class, 1);
        Page<Peripheral> list = peripheralDao.findByPage(null);
        export.setDataList(list);
        // 输出导文件
        OutputStream out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
        return true;
    }

    /**
     * 生成模板
     */
    @Override
    @MethodLog(name = "生成模板", description = "生成模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("外设名称");
        headList.add("外设ID");
        headList.add("外设消息长度");
        // 必填字段
        requiredList.add("外设名称");
        requiredList.add("外设ID");
        // 默认设置一条数据
        exportList.add("08基站定位");
        exportList.add("0x08");
        exportList.add("2");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(16);

        ExportExcel export;
        export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int i = 0; i < exportList.size(); i++) {
            export.addCell(row, i, exportList.get(i));
        }
        // 输出导文件
        OutputStream out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
        return true;
    }
}
