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
public class AppExpireRemindQueryEntity extends BaseEntity {
    String userName;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(userName);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = (String.class);
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP查询到期数据异常";
    }
}
