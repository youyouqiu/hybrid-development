package com.zw.lkyw.service.historicalSnapshot;

import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotQuery;
import com.zw.platform.util.common.JsonResultBean;

public interface HistoricalSnapshotService {

    JsonResultBean getHistoricalSnapshot(HistoricalSnapshotQuery historicalSnapshotQuery);

    JsonResultBean getMediaMapData(HistoricalSnapshotQuery historicalSnapshotQuery);

    JsonResultBean getMediaMapDataDetail(HistoricalSnapshotQuery historicalSnapshotQuery);

    JsonResultBean send8801(String vehicleId);
}
