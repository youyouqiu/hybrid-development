package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.UserGroupDTO;
import com.zw.platform.commons.SystemHelper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * @author wanxing
 * @Title: 用户分组实体
 * @date 2020/10/3014:55
 */
@Data
@NoArgsConstructor
public class UserGroupDO {

    private String id = UUID.randomUUID().toString();
    /**
     * 用户UUID
     */
    private String userId;
    /**
     * 分组ID
     */
    private String groupId;
    private Byte flag;
    private Date createDataTime;
    private String createDataUsername;

    public UserGroupDO(String groupId, String userId, String createDataUsername) {
        this.groupId = groupId;
        this.userId = userId;
        this.createDataUsername = createDataUsername;
        this.flag = 1;
        this.createDataTime = new Date();
    }

    public UserGroupDO(UserGroupDTO userGroupDTO) {
        this.groupId = userGroupDTO.getGroupId();
        this.userId = userGroupDTO.getUserId();
        this.createDataUsername = SystemHelper.getCurrentUsername();
        this.flag = 1;
        this.createDataTime = new Date();
    }
}
