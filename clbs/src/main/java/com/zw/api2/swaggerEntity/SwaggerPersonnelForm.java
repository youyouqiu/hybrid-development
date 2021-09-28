package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
public class SwaggerPersonnelForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiParam(value = "人员ID", required = true)
    private String id;
    /**
     * 人员编号
     */
    @NotEmpty(message = "【人员编号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, message = "【人员编号】最大20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "人员编号,最大20个字符", required = true)
    private String peopleNumber; // 人员编号

    /**
     * 出生年月
     */
    private Date birthday;

    /**
     * 性别
     */
    @Pattern(message = "【性别】输入错误，只能输入1,2,其中1:男,2:女！", regexp = "^[1-2]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ApiParam(value = "性别")
    private String gender;

    /**
     * 姓名
     */
    @Size(max = 15, message = "【姓名】最大15个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "姓名")
    private String name;

    /**
     * 身份证号
     */
    @Pattern(message = "【身份证号】输入错误，请输入正确的身份证号！", regexp = "^\\s*$|[1-9]\\d{13,16}[a-zA-Z0-9]{1}", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "身份证号")
    private String identity;

    /**
     * 电话
     */
    @Pattern(message = "【电话输入错误】", regexp = "^[1][3,4,5,7,8][0-9]{9}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ApiParam(value = "电话号码")
    private String phone;

    /**
     * 邮箱
     */
    @ApiParam(value = "邮箱")
    private String email;
    /**
     * 备注
     */
    @ApiParam(value = "描述")
    private String remark;

    @NotEmpty(message = "【企业id】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "企业id", required = true)
    private String groupId; // 所属企业

    private String groupName; // 所属企业名称

    private String deviceNumber; // 终端编号

    private String simcardNumber; // sim卡号

    private String createDataTimeStr; // 创建时间 yyyy-MM-dd

    private String assignmentId; // 分组id

    private String assignmentName; // 分组名称

    private String assignmentGroupId; // 分组所属企业id

}
