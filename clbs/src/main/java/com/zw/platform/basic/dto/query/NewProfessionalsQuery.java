package com.zw.platform.basic.dto.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * Title: 从业人员Query
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: penghujie
 * @date 2018年4月16日下午4:15:13
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NewProfessionalsQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 查询企业id
     */
    private String orgId;


}
