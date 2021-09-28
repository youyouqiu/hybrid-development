package com.zw.platform.basic.dto.export;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.IntercomDTO;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IntercomExportDTO {

    @ExcelField(title = "监控对象")
    private String monitorName;

    @ExcelField(title = "监控对象类型")
    private String monitorType;

    @ExcelField(title = "生成状态")
    private String statusName;

    @ExcelField(title = "所属企业")
    private String orgName;

    @ExcelField(title = "群组")
    private String groupName;

    @ExcelField(title = "终端手机号")
    private String simCardNum;

    @ExcelField(title = "对讲设备标识")
    private String intercomDeviceNum;

    @ExcelField(title = "对讲机型")
    private String intercomModelName;

    @ExcelField(title = "优先级")
    private Integer priority;

    @ExcelField(title = "客户代码")
    private Long customerCode;

    @ExcelField(title = "个呼号码")
    private String number;

    @ExcelField(title = "组数")
    private String groupNum;

    public IntercomExportDTO(IntercomDTO intercomDTO) {
        this.monitorName = intercomDTO.getName();
        this.monitorType = MonitorTypeEnum.getNameByType(intercomDTO.getMonitorType());
        this.statusName = 0 == intercomDTO.getStatus() ? "未生成" : 1 == intercomDTO.getStatus() ? "已生成" : "生成失败";
        this.orgName = intercomDTO.getOrgName();
        this.groupName = intercomDTO.getGroupName();
        this.simCardNum = intercomDTO.getSimCardNumber();
        this.intercomDeviceNum = intercomDTO.getIntercomDeviceNumber();
        this.intercomModelName = intercomDTO.getIntercomModelName();
        this.priority = intercomDTO.getPriority();
        this.customerCode = intercomDTO.getCustomerCode();
        this.number = intercomDTO.getCallNumber();
        this.groupNum = intercomDTO.getCurrentGroupNum() + "/" + intercomDTO.getMaxGroupNum();
    }
}
