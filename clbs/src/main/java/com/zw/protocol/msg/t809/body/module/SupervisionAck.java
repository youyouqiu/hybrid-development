package com.zw.protocol.msg.t809.body.module;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by LiaoYuecai on 2017/2/13.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupervisionAck extends MainModule {
    private Integer supervisionId;
    private Integer result;
}
