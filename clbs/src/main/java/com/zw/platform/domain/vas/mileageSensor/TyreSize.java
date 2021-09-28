package com.zw.platform.domain.vas.mileageSensor;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * <p>
 * Title:轮胎规格表
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 10:54
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TyreSize extends BaseFormBean {

    @NotEmpty(message = "【种类】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【种类】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【种类】输入错误，请输入合法字符(中文、-、_、字母、数字、（）、*)！",regexp = "^[A-Za-z0-9_.\\(\\)\\（\\）\\*\\u4e00-\\u9fa5\\-]+$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "轮胎种类")
    private String tireType;//轮胎种类

    @NotEmpty(message = "【规格】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 25, message = "【规格】长度不超过25！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【规格】输入错误，请输入合法字符(中文、-、_、字母、数字、（）、*)！",regexp = "^[A-Za-z0-9_.\\(\\)\\（\\）\\*\\u4e00-\\u9fa5\\-/]+$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "轮胎规格")
    private String sizeName;//轮胎规格名称

    @NotEmpty(message = "【滚动半径】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 10, message = "【滚动半径】长度不超过10！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【滚动半径】输入错误，请输入合法字符(数字、.)！",regexp = "^[0-9_.]+$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer rollingRadius;//滚动半径

    @ExcelField(title = "轮胎滚动半径(mm)")
    private String rollingRadiusStr;//滚动半径

    private String remark;//备注




}
