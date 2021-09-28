package com.zw.lkyw.domain.messageTemplate;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 下发消息模板导入实体
 * @author XK on 2019/12/30
 */

@Data
public class MessageTemplateImportBean implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 数据id
     */
    private String id;
    /**
     * 消息内容
     */
    @Size(max = 79, message = "【消息内容】长度不超过79！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "消息内容                                                ")
    private String content;
    /**
     * 数据状态1：启用 0：停用
     */
    private Integer status;

    @ExcelField(title = "状态")
    private String statusStr;

    /**
     * 备注
     */
    @Size(max = 50, message = "【备注】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "备注")
    private String remark;
}
