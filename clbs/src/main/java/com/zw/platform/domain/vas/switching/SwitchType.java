package com.zw.platform.domain.vas.switching;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * <p>
 * Title:开关类传感器类型
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 13:48
 */
@Data
public class SwitchType extends BaseFormBean {

    @ExcelField(title = "功能ID")
    private  String identify;//功能ID

    @ExcelField(title = "检测功能类型")
    private  String name;//名称


    @ExcelField(title = "状态1")
    private String stateOne;

    @ExcelField(title = "状态2")
    private String stateTwo;

    @ExcelField(title = "备注")
    private  String description;//说明

    /**
     * 是否重复
     */
    private Boolean isRepeat = false;

}
