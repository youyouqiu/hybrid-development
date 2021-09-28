package com.zw.platform.domain.regionmanagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户围栏显示设置
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 10:52
 */
@Data
public class UserFenceDisplaySet extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 3976205851263496553L;

    /**
     * 关联的围栏id
     */
    private String relationId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 标识
     */
    private String mark = "1";

}
