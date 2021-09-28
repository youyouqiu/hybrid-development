package com.zw.platform.domain.basicinfo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * <p> Title: 分组管理实体 </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team: ZhongWeiTeam </p>
 * @version 1.0
 */
@Data
public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分组表
     */
    private String id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 监控对象类型
     */
    private String type;

    private String description;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 电话号码
     */
    private String telephone;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private String groupId;

    private String groupName;

    private Integer mNum;

    private List<String> monitorIds; // 监控对象id集合（该分组下的监控对象）

    private String vehicleId; // 监控对象ID 不区分人和车

    private String assignmentId;

    /**
     * 当前分组包含的监控对象数量
     */
    private Integer assignmentNumber;

    private Integer orderNum = 0;

    /**
     * 是否达到分组最大存储个数100
     */
    private Boolean isMaxSize = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Assignment that = (Assignment) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
