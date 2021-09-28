package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.CardReaderInfo;
import com.zw.platform.domain.basicinfo.form.CardReaderInfoForm;
import com.zw.platform.domain.basicinfo.query.CardReaderInfoQuery;
import com.zw.platform.repository.modules.CardReaderInfoDao;
import com.zw.platform.service.basicinfo.CardReaderInfoService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读卡器管理service实现类 <p>Title: CardReaderInfoServiceImpl.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年7月21日下午5:46:09
 */
@Service
public class CardReaderInfoServiceImpl implements CardReaderInfoService {
    private static Logger log = LogManager.getLogger(CardReaderInfoServiceImpl.class);

    @Autowired
    private CardReaderInfoDao cardReaderInfoDao;

    @Override
    public Page<CardReaderInfo> findByPage(CardReaderInfoQuery query) {

        try {
            return PageHelperUtil.doSelect(query, () -> cardReaderInfoDao.find(query));
        } catch (Exception e) {
            log.error("查询读卡器出错" + e);
        }
        return null;
    }

    @Override
    public CardReaderInfo get(String id) {
        try {
            return cardReaderInfoDao.get(id);
        } catch (Exception e) {
            log.error("查询读卡器信息出错" + e);
        }
        return null;
    }

    @Override
    public void add(CardReaderInfoForm form) throws BusinessException {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        if (Converter.toBlank(form.getFactoryDate()).equals("")) {
            form.setFactoryDate(null);
        }
        try {
            cardReaderInfoDao.add(form);
        } catch (Exception e) {
            log.error("新增读卡器出错" + e);
        }
    }

    @Override
    public int delete(String id) throws BusinessException {
        try {
            return cardReaderInfoDao.delete(id);
        } catch (Exception e) {
            log.error("删除读卡器出错" + e);
        }

        return 0;
    }

    @Override
    public int update(CardReaderInfoForm form) throws BusinessException {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        if (Converter.toBlank(form.getFactoryDate()).equals("")) {
            form.setFactoryDate(null);
        }
        try {
            return cardReaderInfoDao.update(form);
        } catch (Exception e) {
            log.error("更新读卡器出错" + e);
        }
        return 0;
    }

    @Override
    public boolean exportCardReaderInfo(String title, int type, HttpServletResponse response) {
        ExportExcel export = new ExportExcel(title, CardReaderInfoForm.class, 1, null);
        List<CardReaderInfoForm> exportList = new ArrayList<CardReaderInfoForm>();
        List<CardReaderInfo> list = cardReaderInfoDao.find(null);
        for (CardReaderInfo info : list) {
            CardReaderInfoForm form = new CardReaderInfoForm();
            // 启停状态重新赋值
            if (Converter.toBlank(info.getIsStart()).equals("2")) {
                form.setIsStart("停用");
            } else {
                form.setIsStart("启用"); // 默认启用
            }
            BeanUtils.copyProperties(info, form);
            // 读卡器类型重新赋值
            /*
             * if (Converter.toBlank(info.getCardReaderType()).equals("1")) { form.setCardReaderType("RFID读卡器"); } else
             * if (Converter.toBlank(info.getCardReaderType()).equals("2")) { form.setCardReaderType("IC读卡器"); } else {
             * // ID读卡器 form.setCardReaderType("ID读卡器"); }
             */
            form.setFactoryDate(Converter.toString(info.getFactoryDate(), "yyyy/MM/dd"));
            exportList.add(form);
        }
        export.setDataList(exportList);
        OutputStream out;
        try {
            out = response.getOutputStream();
            export.write(out);// 将文档对象写入文件输出流
            out.close();
        } catch (IOException e) {
            log.error("error", e);
            return false;
        }
        return true;
    }

    @MethodLog(name = "生成导入模板", description = "生成导入模板")
    public boolean generateTemplate(HttpServletResponse response) {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("读卡器编号");
        headList.add("读卡器类型");
        headList.add("启停状态");
        headList.add("读卡器厂商");
        headList.add("出厂时间");
        headList.add("描述");
        // 必填字段
        requiredList.add("读卡器编号");
        /* requiredList.add("读卡器类型"); */
        /* requiredList.add("启停状态"); */
        // 默认设置一条数据
        exportList.add("0725000");
        exportList.add("ID读卡器");
        exportList.add("启用");
        exportList.add("XX工厂");
        exportList.add(new Date());
        exportList.add("XXXXX");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 启停状态
        String[] startStop = { "启动", "停止" };
        selectMap.put("启停状态", startStop);
        // 读卡器类型
        String[] readType = { "RFID读卡器", "IC读卡器", "ID读卡器" };
        selectMap.put("读卡器类型", readType);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        try {
            out = response.getOutputStream();
            export.write(out);// 将文档对象写入文件输出流
            out.close();
        } catch (IOException e) {
            log.error("error", e);
            return false;
        }

        return true;
    }

    @MethodLog(name = "批量导入", description = "批量导入")
    public Map importCardReaderInfo(MultipartFile multipartFile) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        try {
            // 导入的文件
            ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
            // excel 转换成 list
            List<CardReaderInfoForm> list = importExcel.getDataList(CardReaderInfoForm.class, null);
            String temp;
            int num = 0;
            for (int i = 0; i < list.size() - 1; i++) {
                // for(int j=i+1;j<list.size();j++){
                for (int j = list.size() - 1; j > i; j--) {
                    if (list.get(j).getCardReaderNumber().equals(list.get(i).getCardReaderNumber())) {
                        temp = list.get(i).getCardReaderNumber();
                        errorMsg.append("第").append((i + 1 + num)).append("行跟第").append((j + 1 + num)).append("行重复，值是：")
                            .append(temp).append("<br/>");
                        num++;
                        list.remove(j);
                    }
                }
            }
            List<CardReaderInfoForm> importList = new ArrayList<CardReaderInfoForm>();
            int i = 0;
            // 校验需要导入的CardReaderInfo
            /* for (int k = 1; k<list.size(); k++) { */
            for (CardReaderInfoForm cardReaderInfoForm : list) {
                /* CardReaderInfoForm cardReaderInfoForm = list.get(k); */
                i++;
                // 校验必填字段
                if (cardReaderInfoForm.getCardReaderNumber() == null || cardReaderInfoForm.getCardReaderNumber() == ""
                    || cardReaderInfoForm.getCardReaderType() == null || cardReaderInfoForm.getCardReaderType() == ""
                    || cardReaderInfoForm.getIsStart() == null) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据必填字段未填\n");
                    continue;
                }
                // 校验读卡器编号是否重复
                if (checkCardReaderNumber(cardReaderInfoForm.getCardReaderNumber())) {
                    resultMap.put("flag", 0);
                    errorMsg.append("读卡器编号为“").append(cardReaderInfoForm.getCardReaderNumber()).append("”已存在<br/>");
                    continue;
                }
                // 启停状态重新赋值
                if (Converter.toBlank(cardReaderInfoForm.getIsStart()).equals("停用")) {
                    cardReaderInfoForm.setIsStart("2");
                } else {
                    cardReaderInfoForm.setIsStart("1"); // 默认启用
                }
                // 读卡器类型重新赋值
                /*
                 * if (Converter.toBlank(cardReaderInfoForm.getCardReaderType()).equals("RFID读卡器")) {
                 * cardReaderInfoForm.setCardReaderType("1"); } else if
                 * (Converter.toBlank(cardReaderInfoForm.getCardReaderType()).equals("IC读卡器")) {
                 * cardReaderInfoForm.setCardReaderType("2"); } else { // ID读卡器
                 * cardReaderInfoForm.setCardReaderType("3"); }
                 */
                importList.add(cardReaderInfoForm);
            }
            // 组装导入结果
            if (importList.size() > 0) {
                // 处理出厂日期 ，不然导入会出错
                for (CardReaderInfoForm cif : importList) {
                    if (Converter.toBlank(cif.getFactoryDate()).equals("")) {
                        cif.setFactoryDate(null);
                    }
                }
                // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
                boolean flag = cardReaderInfoDao.addByBatch(importList);
                if (flag) {
                    resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                    resultMap.put("flag", 1);
                    resultMap.put("errorMsg", errorMsg);
                    resultMap.put("resultInfo", resultInfo);
                } else {
                    resultMap.put("flag", 0);
                    resultMap.put("resultInfo", "导入失败！");
                    return resultMap;
                }

            } else {
                resultMap.put("flag", 0);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", "成功导入0条数据。");
                return resultMap;
            }

        } catch (IOException e) {
            log.error("error", e);
            return resultMap;
        } catch (InstantiationException e) {
            log.error("error", e);
            return resultMap;
        } catch (IllegalAccessException e) {
            log.error("error", e);
            return resultMap;
        } catch (BusinessException e) {

            return resultMap;
        }

        return resultMap;
    }

    /**
     * 校验读卡器编号是否重复
     * @param cardReaderNumber
     * @return boolean
     * @throws BusinessException
     * @Title: checkCardReaderNumber
     * @author Liubangquan
     */
    public boolean checkCardReaderNumber(String cardReaderNumber) throws BusinessException {
        CardReaderInfo cardReaderInfo = null;
        // 校验读卡器编号是否重复
        try {
            cardReaderInfo = cardReaderInfoDao.findByCardReaderNumber(cardReaderNumber);
        } catch (Exception e) {
            log.error("校验读卡器编号是否重复", e);
        }
        if (cardReaderInfo != null && !Converter.toBlank(cardReaderInfo.getId()).equals("")) {
            return true;
        }
        return false;
    }

    @Override
    public CardReaderInfo findByCardReaderInfo(String cardReaderNumber) {
        // TODO Auto-generated method stub
        return cardReaderInfoDao.findByCardReaderInfo(cardReaderNumber);
    }

}
