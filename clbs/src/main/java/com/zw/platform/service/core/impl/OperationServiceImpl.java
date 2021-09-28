package com.zw.platform.service.core.impl;


import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OperationForm;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.repository.core.OperationDao;
import com.zw.platform.service.core.OperationService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private OperationDao operationdao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Value("${operational.type.exist}")
    private String operationalTypeExist;

    @Override
    public JsonResultBean addOperation(String type, String explains, String ipAddress) throws Exception {
        Operations operation = findOperationByOperation(type);
        if (operation == null) {
            OperationForm operationForm = new OperationForm();
            String state = explains.trim();
            operationForm.setOperationType(type);
            operationForm.setExplains(state);
            operationForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag = operationdao.addOperation(operationForm);
            if (flag) {
                // 获取ip地址
                String message = "新增运营资质类别:" + type;
                // 记录日志
                logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT, "添加" + operationalTypeExist);
    }

    @Override
    public JsonResultBean deleteOperation(List<String> operationIds, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        for (String id : operationIds) {
            Operations operations = operationdao.findOperationById(id);
            if (operations != null) {
                boolean flag = operationdao.deleteOperation(id);
                if (flag) {
                    // 监控对象操作(被删除的运营资质类别)
                    message.append("删除运营资质类别 : ").append(operations.getOperationType()).append(" <br/>");
                }
            }
        }
        if (!message.toString().isEmpty()) {
            if (operationIds.size() == 1) {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                // 批量删除日志记录
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量删除运营资质类别");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);

    }

    @Override
    public List<Operations> findOperation(String type) throws Exception {
        return operationdao.findAllOperation(type.trim());
    }

    /**
     * 所有
     * @return
     * @throws Exception
     */
    @Override
    public List<Operations> findAll() throws Exception {
        return operationdao.findOperationList();
    }

    @Override
    public Operations findOperationById(String id) throws Exception {
        Operations operations = operationdao.findOperationById(id);
        return operations;

    }

    @Override
    public JsonResultBean updateOperation(OperationForm form, String ipAddress) throws Exception {
        Operations beforeOperation = findOperationById(form.getId());
        if (beforeOperation != null) {
            String beforType = beforeOperation.getOperationType();
            String nowType = form.getOperationType();
            if (!beforType.equals(nowType)) {
                Operations operations = findOperationByOperation(nowType);// 根据类别查询运营资质类别
                if (operations != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "修改" + operationalTypeExist);
                }
            }
            form.setUpdateDataUsername(SystemHelper.getCurrentUsername());// 当前操作用户
            boolean flag = operationdao.updateOperations(form);
            if (flag) {
                String message = "";
                if (!nowType.equals(beforType)) {
                    message = "修改运营资质类别 : " + beforType + " 为 : " + nowType;
                } else {
                    message = "修改运营资质类别:" + nowType;
                }
                // 日志记录
                logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Operations findOperationByOperation(String type) throws Exception {
        Operations operation = operationdao.findOperationByOperation(type);
        return operation;
    }

}
