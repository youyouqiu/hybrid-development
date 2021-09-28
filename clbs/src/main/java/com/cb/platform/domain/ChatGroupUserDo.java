package com.cb.platform.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:聊天分组用户关联
 * @Author:nixiangqian
 * @Date:Create in 2018/5/14 14:34:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ChatGroupUserDo implements Serializable {

    private static final long serialVersionUID = -7888502553700166063L;

    /**
     * 用户与分组关联ID
     */
    private String id;
    /**
     * 分组编号
     */
    private String groupId;


    /**
     * 用户id
     */
    private String userId;

    /**
     * 创建时间
     */
    private Date createDataTime;


    /**
     * 创建人
     */
    private String createDataUsername;


    /**
     * 修改时间
     */
    private Date updateDataTime;


    /**
     * 修改人
     */
    private String updateDataUsername;
}
