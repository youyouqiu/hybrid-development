package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:聊天记录
 * @Author:nixiangqian
 * @Date:Create in 2018/5/14 14:16:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ChatDo  implements Serializable {

    private static final long serialVersionUID = 5690983623746767423L;
    /**
     * 消息ID
     */
    private String chatId;

    /**
     * 创建时间
     */
    @ExcelField(title = "聊天时间")
    private Date createDataTime;


    /**
     * 发送者用户id
     */
    private String fromUserId;

    /**
     * 发送者用户名
     */
    private String fromUserName;
    /**
     * 创建人
     */
    @ExcelField(title = "用户名")
    private String createDataUsername;

    /**
     * 1：用户，2：组
     */
    private Integer toType;

    /**
     * 接受者id，根据类型不同接受者是用户或者组
     */
    private String toTypeId;

    /**
     * 接受者id，根据类型不同接受者是用户或者组
     */
    @ExcelField(title = "讨论组")
    private String toTypeName;


    /**
     * 聊天内容
     */
    @ExcelField(title = "聊天内容")
    private String chatContent;

    /**
     * 1：已发送，2：已收到 相对于个人对个人
     */
    private Integer chatType;






    /**
     * 修改时间
     */
    private Date updateDataTime;


    /**
     * 修改人
     */
    private String updateDataUsername;

}
