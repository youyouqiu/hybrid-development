package com.zw.adas.service.riskEvidence;

import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.query.PicProcessPageQuery;
import com.zw.adas.domain.riskManagement.show.AdasPicPostprocessResult;

import java.util.Set;

public interface AdasPicPostprocessService {
    int batchRemove(Set<String> monitorIds);

    int add(Set<String> monitorIds);

    Page<AdasPicPostprocessResult> page(PicProcessPageQuery query);
}
