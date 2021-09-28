package com.zw.platform.domain.param;

import lombok.Data;

import java.io.Serializable;

/**
 * 远程升级处理参数
 * @author hujun
 * @date 2019/2/13 10:12
 */
@Data
public class HandleRemoteUpgradeParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer answerType;// 应答类型（0:0x0001；1:0x0900）
    private Integer status;// 标识状态（0x0001 0:成功 1:失败；0x0900 0:成功 1:进行中 2:失败）
    private Integer serialNumber;// 消息流水号
    private Integer allPage;// 总包数（仅当0x0900 时有效）
    private Integer downPage;// 完成下载包数（仅当0x0900 时有效）
}
