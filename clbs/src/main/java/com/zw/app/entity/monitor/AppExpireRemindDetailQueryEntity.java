package com.zw.app.entity.monitor;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/***
 @Author zhengjc
 @Date 2019/11/21 18:00
 @Description 到期提醒查询实体
 @version 1.0
 **/
@Data
public class AppExpireRemindDetailQueryEntity extends BaseEntity {
    /**
     * 用户名
     */
    String userName;
    /**
     * 0：服务即将到期
     * 1：服务已经到期
     * 2：驾驶证即将到期
     * 3：驾驶证已经到期
     * 4：道路运输证即将到期
     * 5.道路运输证已经到期
     * 6.保养即将到期
     * 7.保险即将到期
     */
    int type;
    /**
     * 当前页
     */
    long page;
    /**
     * 每页显示的数量
     */
    long limit;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(this);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = (AppExpireRemindDetailQueryEntity.class);
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP查询到期数据详情异常";
    }

}
