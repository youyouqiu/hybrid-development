package com.zw.protocol.msg.t809.body;


import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/2/10.
 */
@Data
public class MainLogout implements T809MsgBody {
    private Integer userID;
    private String password;
}
