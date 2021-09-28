package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.elasticsearch.common.recycler.Recycler;

import java.io.Serializable;
import java.util.Date;
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalsTypeQuery  extends BaseQueryBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private String id;
	private String professionalstype;//车辆类型
	private String description;//类型描述
	private Short flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
}
