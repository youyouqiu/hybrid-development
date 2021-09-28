package com.zw.platform.basic.dto.query;

import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

/**
 * @Author: zjc
 * @Description:sim卡查询条件类
 * @Date: create in 2020/11/6 15:06
 */
@Data
public class SimCardQuery extends BaseQueryBean {
    /**
     * 接收前端参数企业id
     */
    private String groupId;
    /**
     * 后端真正使用的参数企业id
     */
    private String orgId;

    public void paramInit() {
        orgId = groupId;
    }

    /**
     * 判断是否包含高级查询提交
     * @return
     */
    public boolean containsFuzzyQuery() {
        return StrUtil.isNotBlank(getSimpleQueryParam());
    }
}
