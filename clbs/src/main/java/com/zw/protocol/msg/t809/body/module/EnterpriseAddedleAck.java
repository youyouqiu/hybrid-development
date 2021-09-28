package com.zw.protocol.msg.t809.body.module;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description: @Author:nixiangqian @Date:Create in 2018/10/17 13:57
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseAddedleAck extends MainModule {

    /**
     * 企业静态信息数据体
     */
    private EnterpriseInfo enterpriseInfo;

}
