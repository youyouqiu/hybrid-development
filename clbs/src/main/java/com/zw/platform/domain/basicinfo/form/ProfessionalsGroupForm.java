/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>Title: addProfessionsGroup.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年7月28日上午10:35:32
 * @version 1.0
 * 
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalsGroupForm extends BaseFormBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private String professionalsId;
	private String groupId;

}
