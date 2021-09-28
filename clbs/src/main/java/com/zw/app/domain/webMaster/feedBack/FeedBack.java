package com.zw.app.domain.webMaster.feedBack;


import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FeedBack implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;

    @ExcelField(title = "APP用户")
    private String userName;

    @ExcelField(title = "意见内容")
    private String feedBack;

    @ExcelField(title = "发送时间")
    private Date submitDate;
}
