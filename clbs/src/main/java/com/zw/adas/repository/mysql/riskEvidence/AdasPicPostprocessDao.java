package com.zw.adas.repository.mysql.riskEvidence;

import java.util.Collection;

public interface AdasPicPostprocessDao {

    int unmarkPicPostprocess(Collection<String> monitorIds);

    int markPicPostprocess(Collection<String> monitorIds);
}
