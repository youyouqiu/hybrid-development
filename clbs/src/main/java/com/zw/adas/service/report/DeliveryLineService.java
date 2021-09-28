package com.zw.adas.service.report;

import com.github.pagehelper.Page;
import com.zw.adas.domain.report.deliveryLine.LineRecordDto;
import com.zw.adas.domain.report.query.DeliveryLineQuery;
import com.zw.protocol.msg.Message;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface DeliveryLineService {

    Page<LineRecordDto> pageList(DeliveryLineQuery query);

    boolean export(DeliveryLineQuery query, HttpServletResponse response) throws IOException;

    void addDrvLineInfo(Message message);

}
