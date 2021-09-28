package com.zw.platform.domain.core.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/9/2
 **/
@Data
public class OrganizationImportForm {

    @ExcelField(title = "企业名称")
    private String name;

    @ExcelField(title = "组织机构代码")
    private String organizationCode;

    @ExcelField(title = "上级组织机构代码")
    private String upOrganizationCode;

    @ExcelField(title = "管理者组织机构代码")
    private String managerOrganizationCode;

    @ExcelField(title = "行业类别")
    private String operation;

    @ExcelField(title = "经营范围")
    private String scopeOfOperation;

    private String scopeOfOperationIds;
    private String scopeOfOperationCodes;

    @ExcelField(title = "发证机关")
    private String issuingOrgan;

    @ExcelField(title = "经营许可证号")
    private String license;

    @ExcelField(title = "经营许可证字别")
    private String businessLicenseType;

    @ExcelField(title = "许可证有效期起")
    private String licenseValidityStartDate;

    @ExcelField(title = "许可证有效期止")
    private String licenseValidityEndDate;

    @ExcelField(title = "经营状态")
    private String operatingStateStr;

    private String operatingState;

    @ExcelField(title = "行政区域代码")
    private String areaNumber;

    @ExcelField(title = "注册日期")
    private String registerDate;

    @ExcelField(title = "企业法人")
    private String principal;

    @ExcelField(title = "法人电话")
    private String principalPhone;

    @ExcelField(title = "联系人")
    private String contactName;

    @ExcelField(title = "联系人电话")
    private String phone;

    @ExcelField(title = "地址")
    private String address;

    @ExcelField(title = "备注")
    private String description;
}
