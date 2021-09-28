package com.zw.platform.util;

import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.service.systems.ParameterService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 下发工具类 @author  Tdz
 *
 * @create 2017-02-20 9:21
 **/
@Component
public class SendHelper {
    @Autowired
    private ParameterService parameterService;

    /**
     * 根据不同下发类型及绑定的id删除
     */
    public void deleteByVehicleIdParameterName(String vehicleId, String parameterName, String type) {
        this.parameterService.deleteByVechicleidParameterName(vehicleId, parameterName, type);
    }

    /**
     * 修改参数下发表数据
     *
     * @param paramId       参数下发id
     * @param vehicleId     车辆id
     * @param paramType     参数下发类型
     * @param msgSN         流水号
     * @param parameterName 绑定id
     */
    public String updateParameterStatus(String paramId, int msgSN, int status, String vehicleId, String paramType,
        String parameterName) {
        if (paramId != null && !"".equals(paramId) && !paramId.equals("undefined")) {
            List<String> paramIds = new ArrayList<>();
            paramIds.add(paramId);
            // 重新下发 ，修改流水号
            // 1 : 下发未回应
            parameterService.updateMsgSNAndNameById(paramIds, msgSN, status, parameterName, 1);
        } else {
            // 新增下发参数数据
            DirectiveForm form = generateDirective(vehicleId, status, paramType, msgSN, parameterName, 1, null);
            if (form != null) {
                // 批量新增
                paramId = form.getId();
                parameterService.addDirective(form);
            }
        }
        return paramId;
    }

    /**
     * 修改参数下发表数据
     *
     * @param paramId       参数下发id
     * @param vehicleId     车辆id
     * @param paramType     参数下发类型
     * @param msgSN         流水号
     * @param parameterName 绑定id
     * @param remark        描述,如果remark为ADAS,那么就是ADAS的下发信息,主要用于区分音视频的1026和ADAS的1206
     */
    public String updateParameterStatus(String paramId, int msgSN, int status, String vehicleId, String paramType,
        String parameterName, String remark) {
        // int status = 4; // 已下发
        if (paramId != null && !"".equals(paramId) && !paramId.equals("undefined")) {
            List<String> paramIds = new ArrayList<>();
            paramIds.add(paramId);
            // 重新下发 ，修改流水号
            parameterService.updateMsgSNAndNameById(paramIds, msgSN, status, parameterName, 1); // 1 : 下发未回应
        } else {
            // 新增下发参数数据
            DirectiveForm form = new DirectiveForm("", vehicleId, paramType, parameterName, status, new Date(), msgSN,
                1, remark);
            // 批量新增
            paramId = form.getId();
            parameterService.addDirective(form);
        }
        return paramId;
    }

    /**
     * 生成参数下发数据
     */
    public DirectiveForm generateDirective(String vehicleId, Integer status, String parameterType, int msgSN,
        String parameterName, int replyCode, String remark) {
        DirectiveForm form = new DirectiveForm();
        form.setDownTime(new Date());
        form.setMonitorObjectId(vehicleId);
        form.setParameterName(parameterName);
        form.setStatus(status);
        form.setParameterType(parameterType);
        form.setSwiftNumber(msgSN);
        form.setReplyCode(replyCode);
        form.setRemark(remark);
        form.setUpdateOrAdd(2);
        return form;
    }

    /**
     * 根据车辆、下发参数编号、下发类型获取最后一次下发的编号
     *  @param vehicleId 车辆编号
     * @param paramId   下发参数编号
     * @param type      下发类型获
     */
    public String getLastSendParamID(String vehicleId, String paramId, String type) {
        // 6:报警
        List<Directive> directiveList = parameterService.findParameterByType(vehicleId, paramId, type);
        Directive param;
        if (CollectionUtils.isNotEmpty(directiveList)) {
            param = directiveList.get(0);
            return param.getId();
        }
        return "";
    }

    public List<Directive> findParameterByVehicleIds(List<String> vehicleIds, List<String> parameterNames,
        String type) {
        return parameterService.findParameterByVehicleIds(vehicleIds, parameterNames, type);
    }

    /**
     * 清除轮询
     */
    public String updateParameterStatusAndRemark(String paramId, int msgSno, int status, String vehicleId,
        String paramType, String parameterName, String remark) {
        // int status = 4; // 已下发
        if (paramId != null && !"".equals(paramId) && !paramId.equals("undefined")) {
            List<String> paramIds = new ArrayList<String>();
            paramIds.add(paramId);
            // 重新下发 ，修改流水号
            // 1 : 下发未回应
            parameterService.updateMsgSNAndNameAndRemarkById(paramIds, msgSno, status, parameterName, 1, remark);
        } else {
            // 新增下发参数数据
            DirectiveForm form = generateDirective(vehicleId, status, paramType, msgSno, parameterName, 1, remark);
            if (form != null) {
                // 批量新增
                paramId = form.getId();
                parameterService.addDirective(form);
            }
        }
        return paramId;
    }
}
