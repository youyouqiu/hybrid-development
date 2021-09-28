package com.zw.platform.basic.dto.query;

import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/10/22 9:19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceQuery extends BaseQueryBean {
    /**
     * 终端型号
     */
    private String terminalType;
    /**
     * 终端厂商
     */
    private String terminalManufacturer;

    /**
     * 所属企业id:前端，在使用之前需要转换
     */
    private String groupId;

    private String orgId;
    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 启停状态0:停用 1:启用
     */
    private Integer isStart;

    /**
     * 判断是否包含高级查询提交
     */
    public boolean containsAdvanceQuery() {
        return StrUtil.moreOneNotBlank(terminalManufacturer, terminalType, deviceType, groupId) || null != isStart;
    }

    public void paramInit() {
        orgId = groupId;
    }

}
