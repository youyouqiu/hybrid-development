package com.zw.platform.domain.bsj;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * <p>
 * Title: 分组管理Query
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
 *
 * @author: wangying
 * @date 2016年10月9日下午6:15:22
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CommandQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
}
