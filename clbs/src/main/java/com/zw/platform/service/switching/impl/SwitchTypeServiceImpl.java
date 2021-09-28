package com.zw.platform.service.switching.impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.vas.SwitchTypeDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.switching.SwitchTypeService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportNewExcel;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p> Title: <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 14:12
 */
@Service
public class SwitchTypeServiceImpl implements SwitchTypeService {
    private static Logger log = LogManager.getLogger(SwitchTypeServiceImpl.class);

    @Autowired
    private SwitchTypeDao switchTypeDao;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${switch.typeid.null}")
    private String switchTypeidNull;

    @Value("${switch.typeid.error}")
    private String switchTypeidError;

    @Value("${switch.typeid.exist}")
    private String switchTypeidExist;

    @Value("${switch.typename.exist}")
    private String switchTypeNameExist;

    @Value("${switch.type.exist}")
    private String switchTypeExist;

    private static final String REGEX = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}$";

    @Override
    public Page<SwitchType> findByPage(BaseQueryBean query) throws Exception {
        if (query != null) {
            String simpleQueryParam = query.getSimpleQueryParam();
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        }
        return PageHelperUtil.doSelect(query, () -> switchTypeDao.findByPage(query));
    }

    @Override
    public List<SwitchType> findAllow() throws Exception {
        return switchTypeDao.findAllow();
    }

    @Override
    public SwitchType findByid(String id) throws Exception {
        return switchTypeDao.findByid(id);
    }

    @Override
    public JsonResultBean addSwitchType(SwitchType switchType, String ipAddress) throws Exception {
        if (StringUtil.isNull(switchType.getIdentify())) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidNull);
        }
        if (switchType.getIdentify().length() > 6) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidError);
        }
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(switchType.getIdentify());
        boolean regFlag = matcher.find();
        if (regFlag) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidError);
        } else {
            int num = Integer.parseInt(switchType.getIdentify().substring(2, switchType.getIdentify().length()), 16);
            if (switchType.getIdentify().length() == 1) {
                switchType.setIdentify("0" + switchType.getIdentify());
            }
            if (0x0 > num || num > 0xFFFF) {
                return new JsonResultBean(JsonResultBean.FAULT, switchTypeidError);
            }
        }
        SwitchType tp = findByIdentify(switchType.getIdentify());
        if (tp != null) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidExist);
        }
        tp = findByName(switchType.getName());
        if (tp != null) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeNameExist);
        }
        switchType.setCreateDataTime(new Date());
        switchType.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = switchTypeDao.add(switchType);
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            String msg = "新增检测功能ID : " + switchType.getIdentify() + "和功能类型 : " + switchType.getName();
            logSearchService.addLog(ipAddress, msg, "3", "检测功能类型", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Boolean checkBind(String id) throws Exception {
        Integer num = this.switchTypeDao.checkBind(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public SwitchType findByName(String name) throws Exception {
        return this.switchTypeDao.findByName(name);
    }

    @Override
    public SwitchType findByIdentify(String identify) throws Exception {
        return this.switchTypeDao.findByIdentify(identify);
    }

    @Override
    public JsonResultBean deleteById(String id, String ipAddress) throws Exception {
        boolean isBind = checkBind(id); // 根据外设ID判断是否绑定
        if (isBind) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeExist);
        }
        SwitchType st = findByid(id);
        boolean flag = this.switchTypeDao.deleteById(id);
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            String msg = "删除检测功能ID : " + st.getIdentify() + "和功能类型 : " + st.getName();
            logSearchService.addLog(ipAddress, msg, "3", "检测功能类型", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean updateSwitchType(SwitchType switchType, String ipAddress) throws Exception {
        if (StringUtil.isNull(switchType.getIdentify())) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidNull);
        }
        if (switchType.getIdentify().length() > 6) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidError);
        }
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(switchType.getIdentify());
        boolean regFlag = matcher.find();
        if (!regFlag) { // 判断是否包含特殊字符
          /*  int num = Integer.parseInt(switchType.getIdentify(), 16);
            if (switchType.getIdentify().length() == 1) {
                switchType.setIdentify("0" + switchType.getIdentify());
            }
            if (0x0 > num || num > 0xFF) {
                return new JsonResultBean(JsonResultBean.FAULT, switchTypeidError);
            }*/
        } else {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidError);
        }

        SwitchType tp = findByIdentify(switchType.getIdentify());
        if (tp != null && !tp.getId().equals(switchType.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeidExist);
        }
        tp = findByName(switchType.getName());
        if (tp != null && !tp.getId().equals(switchType.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, switchTypeNameExist);
        }
        SwitchType beforSwitchType = switchTypeDao.findByid(switchType.getId());
        switchType.setUpdateDataTime(new Date());
        switchType.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = this.switchTypeDao.updateSwitchType(switchType);
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            String beforType = beforSwitchType.getName();
            String msg = "";
            if (!beforType.equals(switchType.getName())) {
                msg = "修改检测功能类型 ： " + beforType + " 为 : " + switchType.getName();
            } else {
                msg = "修改检测功能类型：" + switchType.getName();
            }
            logSearchService.addLog(ipAddress, msg, "3", "检测功能类型", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteBatchSwitchType(List<String> ids, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        for (String id : ids) {
            boolean count = checkBind(id);
            SwitchType p = findByid(id);
            if (count) {
                return new JsonResultBean(JsonResultBean.FAULT, "检测功能类型[" + p.getName() + "]正在使用中，不能删除");
            }
            message.append("删除检测功能ID : ").append(p.getIdentify()).append(" 和 检测功能类型 : ").append(p.getName())
                .append(" <br/>");
        }
        boolean flag = switchTypeDao.deleteBatchSwitchType(ids);
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除检测功能类型");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public Map addImportSwitchType(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        StringBuilder message = new StringBuilder();
        resultMap.put("errorMsg", errorMsg);
        resultMap.put("resultInfo", "导入失败,未检测到导入数据！");
        // 导入的文件
        ImportNewExcel importExcel = new ImportNewExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<SwitchType> list = importExcel.getDataList(SwitchType.class, null);
        String temp = "";
        List<SwitchType> importList = new ArrayList<SwitchType>();
        // 校验需要导入的油箱
        if (list.size() == 0) {
            return resultMap;
        }
        for (int i = 0; i < list.size(); i++) {
            // 校验必填字段
            if (StringUtils.isBlank(list.get(i).getIdentify())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【功能ID】异常，功能ID不能为空<br/>");
                continue;
            }
            // 校验必填字段
            if (StringUtils.isBlank(list.get(i).getName())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【检测功能类型】异常，检测功能类型不能为空<br/>");
                continue;
            }
            if (StringUtils.isBlank(list.get(i).getStateOne())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【状态1】异常，检测功能类型不能为空<br/>");
                continue;
            }
            if (StringUtils.isBlank(list.get(i).getStateTwo())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【状态2】异常，检测功能类型不能为空<br/>");
                continue;
            }
            if ("REPEAT".equals(list.get(i).getName())) {
                continue;
            }
            for (int j = i + 1; j < list.size(); j++) {
                if ((list.get(i).getName()).equals(list.get(j).getName())) {
                    temp = list.get(j).getName();
                    errorMsg.append("第").append(i + 1).append("条【检测功能类型】跟第").append(j + 1).append("条重复,值是 : ")
                        .append(temp + "<br/>");
                    list.get(j).setIsRepeat(true);
                }
            }
            for (int j = i + 1; j < list.size(); j++) {
                if ((list.get(i).getIdentify()).equals(list.get(j).getIdentify())) {
                    temp = list.get(i).getIdentify();
                    errorMsg.append("第").append(i + 1).append("条【功能ID】跟第").append(j + 1).append("条重复,值是 : ")
                        .append(temp + "<br/>");
                    list.get(j).setIsRepeat(true);
                }
            }
            for (int j = i + 1; j < list.size(); j++) {
                if ((list.get(i).getStateOne()).equals(list.get(j).getStateOne())) {
                    temp = list.get(i).getStateOne();
                    errorMsg.append("第").append(i + 1).append("条【状态1】跟第").append(j + 1).append("条重复,值是 : ")
                        .append(temp + "<br/>");
                    list.get(j).setIsRepeat(true);
                }
            }
            for (int j = i + 1; j < list.size(); j++) {
                if ((list.get(i).getStateTwo()).equals(list.get(j).getStateTwo())) {
                    temp = list.get(i).getStateTwo();
                    errorMsg.append("第").append(i + 1).append("条【状态2】跟第").append(j + 1).append("条重复,值是 : ")
                        .append(temp + "<br/>");
                    list.get(j).setIsRepeat(true);
                }
            }
        }
        for (int i = 0; i < list.size(); i++) {
            SwitchType sensor = list.get(i);
            if ("REPEAT".equals(sensor.getName()) || "REPEAT".equals(sensor.getIdentify()) || "REPEAT"
                .equals(sensor.getStateOne()) || "REPEAT".equals(sensor.getStateTwo()) || sensor.getIsRepeat()) {
                continue;
            }
            try {
                if (sensor.getIdentify().length() != 6) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据【功能ID】异常，功能ID格式和范围:0x0000~0xFFFF<br/>");
                    continue;
                }
                int num = Integer.parseInt(sensor.getIdentify().substring(2, sensor.getIdentify().length()), 16);
                if (sensor.getIdentify().length() == 1) {
                    sensor.setIdentify("0" + sensor.getIdentify());
                }
                if (0x0 > num || num > 0xFFFF) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据【功能ID】异常，功能ID格式和范围:0x0000~0xFFFF<br/>");
                    continue;
                }
            } catch (NumberFormatException ex) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【功能ID】异常，功能ID格式和范围:0x0000~0xFFFF<br/>");
                continue;
            }
            if (!Pattern.matches(REGEX, sensor.getName())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【检测功能类型】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                continue;
            }
            if (sensor.getStateOne().length() > 20) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【状态1】异常，状态1长度不能大于20位<br/>");
                continue;
            }
            if (sensor.getStateTwo().length() > 20) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【状态2】异常，状态2长度不能大于20位<br/>");
                continue;
            }
            if (StringUtils.isNotBlank(sensor.getDescription()) && sensor.getDescription().length() > 40) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【说明】异常，说明长度不能超过40位<br/>");
                continue;
            }
            // 与数据库是否有重复数据
            SwitchType p = this.switchTypeDao.findByIdentify(sensor.getIdentify());
            if (p != null) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【功能ID】异常，功能ID已存在<br/>");
                continue;
            }
            p = this.switchTypeDao.findByName(sensor.getName());
            if (p != null) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【检测功能类型】异常，检测功能类型已存在<br/>");
                continue;
            }
            p = this.switchTypeDao.findByStateRepetition(null, sensor.getStateOne(), 1);
            if (p != null) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【状态1】异常，状态1已存在<br/>");
                continue;
            }
            p = this.switchTypeDao.findByStateRepetition(null, sensor.getStateTwo(), 2);
            if (p != null) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【状态2】异常，状态2已存在<br/>");
                continue;
            }
            sensor.setCreateDataTime(new Date());
            sensor.setCreateDataUsername(SystemHelper.getCurrentUsername());
            importList.add(sensor);
            message.append("导入功能ID : ").append(sensor.getIdentify()).append(" 和 功能类型 : ").append(sensor.getName())
                .append(" <br/>");
        }
        // 组装导入结果
        if (importList.size() == 0) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }

        // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
        boolean flag = switchTypeDao.addBatch(importList);
        if (flag) {
            ZMQFencePub.pubChangeFence("18");
            resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
            resultMap.put("flag", 1);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", resultInfo);
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入检测功能类型");
        } else {
            resultMap.put("flag", 0);
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        return resultMap;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, SwitchType.class, 1, null);
        Page<SwitchType> list = switchTypeDao.findByPage(null);
        export.setDataList(list);
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @Override
    @MethodLog(name = "生成模板", description = "生成模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("功能ID");
        headList.add("检测功能类型");
        headList.add("状态1");
        headList.add("状态2");
        headList.add("备注");
        // 必填字段
        requiredList.add("功能ID");
        requiredList.add("检测功能类型");
        requiredList.add("状态1");
        requiredList.add("状态2");
        // 默认设置一条数据
        exportList.add("0x0001");
        exportList.add("制冷开关");
        exportList.add("状态1");
        exportList.add("状态2");
        exportList.add("制冷空调线");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @Override
    public SwitchType findByStateRepetition(String id, String state, Integer flag) {
        return switchTypeDao.findByStateRepetition(id, state, flag);
    }

    @Override
    public List<SwitchType> getIoSwitchType() throws Exception {
        return switchTypeDao.getIoSwitchType();
    }
}
