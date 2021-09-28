package com.zw.platform.domain.reportManagement.form;

import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/4/30 15:51
 @Description 车辆综合信息
 @version 1.0
 **/
@Data
public class VehGeneralInfo {

    @ExcelField(title = "车牌号")
    private String brand;

    @ExcelField(title = "分组")
    private String assignmentName;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "所属地域")
    private String groupArea;

    @ExcelField(title = "经营范围")
    private String businessScope;

    @ExcelField(title = "经营许可证号")
    private String license;

    @ExcelField(title = "发证机关")
    private String issuingOrgan;

    @ExcelField(title = "企业法人")
    private String principal;

    @ExcelField(title = "电话号码")
    private String phone;

    @ExcelField(title = "地址")
    private String orgAddress;

    @ExcelField(title = "终端号")
    private String deviceNumber;

    @ExcelField(title = "终端手机号")
    private String simCard;

    @ExcelField(title = "从业人员")
    private String professionalNames;

    /**
     * 从业人员id
     */
    private transient String professionalIds;

    /**
     * 车辆所属企业id
     */
    private transient String groupId;

    public VehGeneralInfo init(Map<String, String> bindInfo) {
        this.assignmentName = bindInfo.get("groupName");
        this.brand = bindInfo.get("name");
        this.simCard = bindInfo.get("simCardNumber");
        this.deviceNumber = bindInfo.get("deviceNumber");
        this.professionalIds = bindInfo.get("professionalIds");
        this.professionalNames = bindInfo.get("professionalNames");
        this.groupId = bindInfo.get("orgId");
        this.groupName = bindInfo.get("orgName");
        return this;

    }

    /**
     * 设置监控对象权限内可显示分组
     * @param cfg
     */
    private String getAssignName(ConfigList cfg, Map<String, String> map) {
        String[] assignIds = cfg.getAssignmentId().split(",");
        StringBuilder assignNames = new StringBuilder();
        for (int i = 0; i < assignIds.length; i++) {
            if (map.get(assignIds[i]) != null) {
                assignNames.append(map.get(assignIds[i])).append(",");
            }
        }
        return assignNames.toString().substring(0, assignNames.toString().length() - 1);
    }

}