package com.zw.talkback.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;

@Data
public class AttendanceReportQuery extends BaseQueryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String monitorId;
}
