package com.zw.platform.domain.basicinfo;

import lombok.Data;

/***
 @Author tianzhangxu
 @Date 2020/10/9 15:22
 @Description 从业人员下拉树实体
 **/
@Data
public class ProWithGroupInfo {
    /**
     * 从业人员id
     */
    private String id;

    /**
     * 从业人员名称
     */
    private String name;

    /**
     * 从业资格证号
     */
    private String cardNumber;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 从业人员的所属企业id
     */
    private String groupId;
}
