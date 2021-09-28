package com.zw.platform.domain.param;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


@Data
public class SingleMediaSearchUp implements T808MsgBody {
    private Integer id; // 多媒体id

    private Integer deleteSign; // 删除标识(0:保留 1:删除)
}
