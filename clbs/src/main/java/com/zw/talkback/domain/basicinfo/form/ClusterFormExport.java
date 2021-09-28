package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ClusterFormExport extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群组名称
     */
    @NotEmpty(message = "【群组名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 30, message = "【群组组名称】不能超过30个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "群组名称")
    private String name;

    /**
     * 监控对象类型
     */
    private String type;

    @ExcelField(title = "所属组织")
    private String groupName;


    /**
     * 组呼号码
     */
    @ExcelField(title = "组呼号码")
    private String groupCallNumber;

    /**
     * 是否录音  1:录音  0：不  默认0
     */

    private Integer soundRecording = 0;


    /**
     * 联系人
     */
    @Size(max = 20, message = "【联系人】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "联系人")
    private String contacts;

    /**
     * 电话号码
     */
    @ExcelField(title = "电话号码")
    private String telephone;

   /* *//**
     * 是否录音
     *//*
    @ExcelField(title = "是否录音")
    private String state;*/

    /**
     * 描述
     */
    @Size(max = 150, message = "【备注】不能超过150个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "备注")
    private String description;

    private Integer orderNum = 0;
    /**
     * 分组下的监控对象数量
     */
    private Integer assignmentNumber;
    private String groupId;


    /**
     * 对讲群组id
     */
    private Long intercomGroupId;



    private  short types;

    // private String test;
}