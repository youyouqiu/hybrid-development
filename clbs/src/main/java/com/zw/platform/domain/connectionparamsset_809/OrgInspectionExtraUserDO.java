package com.zw.platform.domain.connectionparamsset_809;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 企业查岗消息额外接收用户实体
 *
 * @author Zhang Yanhui
 * @since 2021/3/4 13:52
 */

@Data
@NoArgsConstructor
public class OrgInspectionExtraUserDO {

    private String username;

    private String orgId;

    private LocalDateTime createDataTime;

    private String createDataUsername;

    public OrgInspectionExtraUserDO(String username, String orgId, String createDataUsername) {
        this.username = username;
        this.orgId = orgId;
        this.createDataUsername = createDataUsername;
    }
}
