package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:聊天分组
 * @Author:nixiangqian
 * @Date:Create in 2018/5/14 14:16:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ChatGroupDo implements Serializable {


    private static final long serialVersionUID = -6288347024590422745L;
    /**
     * 分组编号
     */
    private String groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组备注
     */
    private String groupRemark;

    /**
     * 分组包含用户名UUID
     */
    private String userIds;

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
