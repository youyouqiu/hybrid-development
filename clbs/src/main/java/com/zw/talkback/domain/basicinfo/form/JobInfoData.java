package com.zw.talkback.domain.basicinfo.form;

import lombok.Data;


@Data
public class JobInfoData {
    private String id;

    private String jobName;//'职位类别名称
    private String jobIconName;//图标

    private String remark;//备注

}
