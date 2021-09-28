package com.zw.platform.basic.dto.query;

import com.zw.platform.domain.core.query.UserQuery;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wanxing
 * @Title: 用户查询类
 * @date 2020/9/259:38
 */
@Data
public class UserPageQuery extends BaseQueryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织树Dn
     */
    private String orgDn;
    /**
     * 搜索范围的标记
     */
    private boolean searchSubFlag;

    private String orgName;

    public static UserPageQuery transform(UserQuery query) {

        UserPageQuery userPageQuery = new UserPageQuery();
        userPageQuery.setLimit(query.getLimit());
        userPageQuery.setLength(query.getLength());
        userPageQuery.setDraw(query.getDraw());
        userPageQuery.setStart(query.getStart());
        userPageQuery.setSimpleQueryParam(query.getSimpleQueryParam());
        userPageQuery.setOrgDn(query.getGroupName());
        return userPageQuery;
    }
}
