package com.zw.platform.domain.basicinfo.form;


import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 *  <p> Title: 品牌机型管理Form </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @author: penghujie
 * @date 2018年4月17日下午4:00:00
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BrandModelsForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotEmpty(message = "【品牌】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String brandId;

    /**
     * 品牌名称
     */
    @ExcelField(title = "品牌")
    private String brandName;

    /**
     * 机型
     */
    @NotEmpty(message = "【机型】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 32, message = "【机型】长度不能超过32！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "机型")
    private String modelName;

    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String describtion;

}
