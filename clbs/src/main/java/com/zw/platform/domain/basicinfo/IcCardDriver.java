package com.zw.platform.domain.basicinfo;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/7/11 15:22
 @Description ic卡司机对象
 @version 1.0
 **/
@Data
public class IcCardDriver {
    /**
     * 从业人员id
     */
    private String id;
    /**
     * 从业人员名称
     */
    private String name;
    /**
     * 从业人员ic卡
     */
    private String cardNumber;
    /**
     * 从业人员的所属企业id
     */
    private String groupId;
}
