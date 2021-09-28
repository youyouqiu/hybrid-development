package com.zw.lkyw.service.messageTemplate.impl;

import com.github.pagehelper.Page;
import com.zw.lkyw.domain.messageTemplate.*;
import com.zw.lkyw.repository.mysql.messageTemplate.MessageTemplateDao;
import com.zw.lkyw.service.messageTemplate.MessageTemplateService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.*;

/**
 * 下发消息模板业务实现层
 * @author XK on 2019/12/26
 */
@Service
public class MessageTemplateServiceImpl implements MessageTemplateService {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private LogSearchService logSearchService;

    private static final int NUMBER_ZERO = 0;

    private static final int NUMBER_ONE = 1;

    private static final int REMARK_LENGTH = 50;

    private static final int CONTENT_LENGTH = 79;

    @Autowired
    TemplateExportExcel templateExportExcel;

    @Override
    public Page<MessageTemplateInfo> findMessageTemplate(MessageTemplateQuery query) {
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            // 特殊字符转译
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        //当参数status不为空  并且不为零是 设置参数status为1  1：启用 0 停用
        if (query.getStatus() != null && query.getStatus() != 0) {
            query.setStatus(1);
        }
        return PageHelperUtil
                .doSelect(query, () -> messageTemplateDao.findMessageTemplate(
                        query.getSimpleQueryParam(), query.getStatus()));
    }

    /**
     * 新增消息模板
     * @param info      新增数据
     * @param ipAddress ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean addTemplate(MessageTemplateForm info, String ipAddress) {
        info.setCreateDataUsername(SystemHelper.getCurrentUsername());
        info.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        // 新增模板信息
        List<MessageTemplateInfo> infos = messageTemplateDao.accurateFindMessageTemplate(info.getContent());
        if (infos.size() > 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "此下发模板消息内容已存在");
        }
        if (info.getContent().length() > CONTENT_LENGTH) {
            return new JsonResultBean(JsonResultBean.FAULT, "下发消息模板消息内容不服规范，长度超过79个字符");
        }
        messageTemplateDao.addMessageTemplate(info);
        String message = "新增下发消息模板 (" + "新增人 :" + info.getCreateDataUsername() + ", 模板状态 :" + info.getStatus() + ")";
        logSearchService.addLog(ipAddress, message, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 批量或者单个删除下发消息模板
     * @param templateIds 模板Id
     * @param ipAddress   ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean deleteTemplate(List<String> templateIds, String ipAddress) {
        try {
            if (templateIds.size() > 0) {
                //查询出消息模板数据用于存储日志
                List<MessageTemplateInfo> messageTemplateInfos = messageTemplateDao.findTemplatesById(templateIds);
                addDeleteTemplateLog(messageTemplateInfos, ipAddress, messageTemplateInfos.size());
                return new JsonResultBean(messageTemplateDao.deleteTemplate(templateIds));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 修改下发消息模板 信息
     * @param form      模板实体
     * @param ipAddress ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean updateTemplate(MessageTemplateBean form, String ipAddress) {
        //通过消息内容查找是否存在此消息模板
        List<MessageTemplateInfo> infos = messageTemplateDao.accurateFindMessageTemplate(form.getContent());
        if (infos.size() > 0) {
            if (!form.getId().equals(infos.get(0).getId())) {
                return new JsonResultBean(JsonResultBean.FAULT, "此下发模板消息内容已存在");
            }
        }
        form.setUpdateDataTime(new Date());
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        if (StringUtils.isBlank(form.getContent())) {
            return new JsonResultBean(JsonResultBean.FAULT, "消息内容为必填项");
        }
        //消息内容不为空且大于79时 失败
        if (form.getContent().length() > CONTENT_LENGTH) {
            return new JsonResultBean(JsonResultBean.FAULT, "修改下发模板信息不符合规范，消息内容长度不能超过79字符");
        }
        //备注信息不为空且 大于50时 失败
        if (StringUtils.isNotBlank(form.getRemark()) && form.getRemark().length() > REMARK_LENGTH) {
            return new JsonResultBean(JsonResultBean.FAULT, "修改下发消息模板不符合规范，备注长度不能超过50字符");
        }
        String message = "修改下发消息模板 (" + "消息内容: " + form.getContent() + ", 备注: " + form.getRemark() + ")";
        logSearchService.addLog(ipAddress, message, "3", "");
        boolean result = messageTemplateDao.updateTemplate(form);
        return new JsonResultBean(result);
    }

    @Override
    public List<MessageTemplateInfo> findTemplateById(String id) {
        List<String> list = Collections.singletonList(id);
        return messageTemplateDao.findTemplatesById(list);
    }

    /**
     * 记录删除模板的日志
     */
    private void addDeleteTemplateLog(List<MessageTemplateInfo> messageTemplateInfos, String ipAddress,
        int templateSize) {
        // 记录删除日志
        StringBuilder message = new StringBuilder();
        messageTemplateInfos.forEach(info -> message.append("删除下发消息模板( 内容:").append(info.getContent()).append(",状态: ")
            .append(info.getStatus() == 0 ? "停用" : "启用").append(") <br/>"));
        // 单个删除
        if (templateSize == 1) {
            logSearchService.addLog(ipAddress, message.toString(), "3", "");
        } else { // 批量删除
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量下发消息模板");
        }
    }

    /**
     * 生成终端型号导入模板
     * @param response response
     * @throws Exception e
     */
    @Override
    public void generateMessageTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("消息内容");
        headList.add("状态");
        headList.add("备注");

        // 必填字段
        requiredList.add("消息内容");
        requiredList.add("状态");
        // 默认设置一条数据
        exportList.add("您已超速，请减速行驶！");
        exportList.add("启用");
        exportList.add("");
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(10);
        // 状态
        String[] statusFlag = { "启用", "停用" };
        selectMap.put("状态", statusFlag);
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));


        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
    }

    /**
     * 导入终端型号信息
     */
    @Override
    public Map<String, Object> importTemplate(MultipartFile multipartFile, String ipAddress) throws Exception {
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, NUMBER_ONE, NUMBER_ZERO);
        // excel 转换成 list
        List<MessageTemplateImportBean> importExcelDataList =
            importExcel.getDataList(MessageTemplateImportBean.class, (int[]) null);
        int total = (importExcelDataList != null && importExcelDataList.size() > 0 ? importExcelDataList.size() : 0);
        List<MessageTemplateForm> resultImportList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>(16);
        resultMap.put("flag", NUMBER_ZERO);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";


        //  当前用户
        String userName = SystemHelper.getCurrentUsername();
        StringBuilder message = new StringBuilder();
        if (!CollectionUtils.isEmpty(importExcelDataList)) {
            List<MessageTemplateInfo> infos = messageTemplateDao.accurateFindMessageTemplate(null);
            Map<String, Integer> contentMap = new HashMap<>(infos.size() * 3 / 2);
            infos.forEach(info -> contentMap.put(info.getContent(), infos.indexOf(info)));
            Map<String, Integer> importContentMap = new HashMap<>(importExcelDataList.size() * 3 / 2);
            //组装map集合用于校验表中数据是否有重复~
            for (int i = 0; i < importExcelDataList.size(); i++) {
                if (importContentMap.get(importExcelDataList.get(i).getContent()) == null) {
                    importContentMap.put(importExcelDataList.get(i).getContent(), 1);
                } else {
                    importContentMap.put(importExcelDataList.get(i).getContent(), 2);
                }
            }
            //for循环遍历导入数据校验数据格式是否正确
            for (int index = 0; index < importExcelDataList.size(); index++) {
                MessageTemplateImportBean bean = importExcelDataList.get(index);
                MessageTemplateBean bean1 = new MessageTemplateBean();
                BeanUtils.copyProperties(bean, bean1);
                MessageTemplateForm form = new MessageTemplateForm();
                // 验证数据的正确性
                // 消息内容
                String content = bean.getContent();
                if (StringUtils.isBlank(content)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,消息内容未填<br/>");
                    continue;
                }
                if (importContentMap.get(content) == 2) {
                    importContentMap.put(content, 3);
                } else if (importContentMap.get(content) == 3) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,消息内容已存在表格中，请勿重复导入<br/>");
                    continue;
                }
                if (contentMap.get(content) != null) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,消息内容已存在<br/>");
                    continue;
                }
                if (StringUtils.isEmpty(content)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,消息内容为必填项<br/>");
                    continue;
                }
                if (content.length() > 79) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,消息内容长度超过限定长度79！<br/>");
                    continue;
                }
                form.setContent(content);
                //验证状态信息
                if ("启用".equals(bean.getStatusStr())) {
                    form.setStatus(NUMBER_ONE);
                } else if ("停用".equals(bean.getStatusStr())) {
                    form.setStatus(NUMBER_ZERO);
                } else {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,状态填写不规范！<br/>");
                    continue;
                }
                //验证备注信息
                if (StringUtils.isNotBlank(bean.getRemark())) {
                    if (bean.getRemark().length() > 50) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,备注信息超过限定长度50！<br/>");
                        continue;
                    }
                }
                form.setCreateDataUsername(userName);
                form.setUpdateDataUsername(userName);
                form.setCreateDataTime(new Date());
                form.setRemark(bean.getRemark());
                message.append("导入下发消息模板 ( 消息内容: ").append(bean.getContent()).append(" ) <br/>");
                resultImportList.add(form);
            }
            if (resultImportList.size() > NUMBER_ZERO) {
                // 批量新增终消息模板
                messageTemplateDao.addTemplateList(resultImportList);
                resultInfo +=
                    "导入成功" + resultImportList.size() + "条数据,导入失败" + (total - resultImportList.size()) + "条数据。";
                resultMap.put("flag", NUMBER_ONE);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                // 记录日志
                if (!message.toString().isEmpty()) {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入下发消息模板");
                }
            } else {
                resultMap.put("flag", NUMBER_ZERO);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", "成功导入0条数据。");
            }
        } else {
            resultMap.put("errorMsg", "请检查文件内容是否填写");
            resultMap.put("resultInfo", "无效文件。");
        }
        return resultMap;
    }

    @Override
    public void exportTemplate(String fuzzyParam, HttpServletResponse response)
            throws Exception {
        List<MessageTemplateInfo> queryDate = messageTemplateDao.findMessageTemplate(fuzzyParam, null);
        List<MessageTemplateBean> beans = new ArrayList<>();
        for (int i = 0; i < queryDate.size(); i++) {
            MessageTemplateBean bean = new MessageTemplateBean();
            bean.setSerialNumber(i + 1);
            bean.setContent(queryDate.get(i).getContent());
            bean.setStatusStr(queryDate.get(i).getStatus() == 1 ? "启用" : "停用");
            bean.setRemark(queryDate.get(i).getRemark());
            bean.setCreateDataTime(queryDate.get(i).getCreateDataTime());
            bean.setUpdateDataTime(queryDate.get(i).getUpdateDataTime());
            bean.setUpdateDataUsername(
                    queryDate.get(i).getUpdateDataUsername() == null
                            ? queryDate.get(i).getCreateDataUsername() : queryDate.get(i).getUpdateDataUsername());
            beans.add(bean);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("beans", beans);
        String fileName = "下发消息模板列表";
        templateExportExcel
                .templateExportExcel("/file/lkyw/sendMessageTemplate.xls", response, data, fileName);
    }

}
