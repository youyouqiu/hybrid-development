package com.zw.platform.domain.scheduledmanagement;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 15:34
 */
@Data
public class SchedulingFrom extends BaseFormBean {
    private static final long serialVersionUID = -2918123066711925988L;

    /**
     * 排班名字
     */
    @Pattern(message = "【排班名称】填值错误！可支持汉字、字母、数字或短横杠", regexp = "^[A-Za-z0-9\\u4e00-\\u9fa5\\-]+$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, min = 1, message = "【排班名称】不能少于1个字符或超过20个字符！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @NotNull(message = "【排班名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String scheduledName;

    /**
     * 排班开始日期
     */
    @NotNull(message = "【排班开始时间】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 排班结束日期
     */
    @NotNull(message = "【排班结束时间】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 日期重复类型（1星期一,2星期二,3星期三,4星期四,5星期五,6星期六,7星期天,8每天,）
     */
    @NotNull(message = "【日期重复类型】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String dateDuplicateType;

    /**
     * 人员id
     */
    @NotNull(message = "【至少选择一个人员】！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String monitorIds;

    /**
     * 排班项
     */
    @NotNull(message = "【排班项不能为空】！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String schedulingItemInfos;

    /**
     * 备注
     */
    @Size(max = 100, message = "【备注】不能超过100个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String remark;

    private String groupId;

    /**
     * 检查排班冲突
     * 是否是修改; true:修改; false:新增
     */
    private Boolean isUpdate = false;
}
