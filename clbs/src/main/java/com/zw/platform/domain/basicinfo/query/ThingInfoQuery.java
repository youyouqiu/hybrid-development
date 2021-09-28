package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class ThingInfoQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;//名称
    private String thingNumber;//数量
    private String weight;//重量
    private String volume;//体积
    private String thingNum;//数量
    private String remark;//备注
    private Short flag;//
    private Date createDataTime;//
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

    /**
     * 组织查询参数（分组或者企业）
     */
    private String groupName;

    /**
     * 组织类型
     */
    private String groupType;


}