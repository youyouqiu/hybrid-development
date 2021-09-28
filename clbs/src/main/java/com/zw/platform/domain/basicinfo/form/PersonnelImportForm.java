package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;


/**
 * @Author: tianzhangxu
 * @Date: 2020.9.10
 * @Description: 人员导入入参实体类
 */
@Data
public class PersonnelImportForm extends ImportErrorData {
    private static final long serialVersionUID = 3595737554576990149L;

    /**
     * 人员编号
     */
    @ExcelField(title = "编号", required = true, repeatable = false)
    private String peopleNumber;


    /**
     * 性别
     */
    private String gender;

    @ExcelField(title = "性别")
    private String genderStr;

    /**
     * 姓名
     */
    @ExcelField(title = "姓名")
    private String name;
    
    /**
     * 身份证号
     */
    @ExcelField(title = "身份证号", repeatable = false)
    private String identity;

    /**
     * 电话
     */
    @ExcelField(title = "电话")
    private String phone;

    /**
     * 邮箱
     */
    @ExcelField(title = "邮箱")
    private String email;
    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}
