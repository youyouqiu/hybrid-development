package com.zw.platform.domain.functionconfig.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>Title: </p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年8月4日下午1:54:07
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FenceQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
     * 电子围栏信息
     */
    private String id;

    /**
     * 类型
     */
    private String type;

    /**
     * 形状（每个形状的ID）
     */
    private String shape;

    /**
     * 预览
     */
    private String preview;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
}
