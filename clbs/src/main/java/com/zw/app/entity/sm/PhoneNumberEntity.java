package com.zw.app.entity.sm;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/***
 @Author gfw
 @Date 2018/12/10 16:58
 @Description 短信接口接收实体
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class PhoneNumberEntity extends BaseEntity {
    @Pattern(regexp = "^1(3|4|5|7|8)\\d{9}$", message = "电话号码格式有误")
    @NotNull(message = "电话号码不能为空")
    String phoneNumber;
}
