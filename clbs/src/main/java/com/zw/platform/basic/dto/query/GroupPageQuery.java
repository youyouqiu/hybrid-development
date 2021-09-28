package com.zw.platform.basic.dto.query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.util.common.BaseQueryBean;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wanxing
 * @Title: 分组分页实体
 * @date 2020/10/2614:32
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GroupPageQuery extends BaseQueryBean implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 分组表
     */
    @ApiParam(value = "分组id")
    private String id;

    /**
     * 分组名称
     */
    @ApiParam(value = "分组名称")
    private String name;

    /**
     * 监控对象类型
     */
    @ApiParam(value = "监控对象类型")
    private String type;

    @ApiParam(value = "备注")
    private String description;

    /**
     *  联系人
     */
    @ApiParam(value = "联系人")
    private String contacts;

    /**
     *  电话号码
     */
    @ApiParam(value = "电话号码")
    private String telephone;

    @JsonIgnoreProperties
    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private String orgId;

    private List<String> orgIds;

    public static GroupPageQuery transform(AssignmentQuery query) {
        GroupPageQuery groupPageQuery = new GroupPageQuery();
        groupPageQuery.setLimit(query.getLimit());
        groupPageQuery.setLength(query.getLength());
        groupPageQuery.setDraw(query.getDraw());
        groupPageQuery.setStart(query.getStart());

        groupPageQuery.setSimpleQueryParam(query.getSimpleQueryParam());
        groupPageQuery.setOrgId(query.getGroupId());
        groupPageQuery.setOrgIds(query.getGroupList());
        return groupPageQuery;
    }
}
