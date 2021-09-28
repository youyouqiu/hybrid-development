package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;

/***
 @Author gfw
 @Date 2019/2/15 20:22
 @Description 新增分组
 @version 1.0
 **/
@Data
public class SwaggerAssignmentForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分组名称
     */
    @NotEmpty(message = "【分组名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class  })
    @Size(max = 30, message = "【分组名称】不能超过30个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "分组名称")
    private String name;

    /**
     *  联系人
     */
    @Size(max = 20, message = "【联系人】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "联系人")
    private String contacts;

    /**
     *  电话号码
     */
    @ExcelField(title = "电话号码")
    private String telephone;


    /**
     *  描述
     */
    @Size(max = 50, message = "【描述】不能超过50个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "描述")
    private String description;

    private String groupId;
}
