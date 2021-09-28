package com.zw.lkyw.repository.mysql.historicalSnapshot;

import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotInfo;
import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotMapData;
import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author lijie
 * @date 2020/1/6
 */
public interface HistoricalSnapshotDao {

    List<HistoricalSnapshotInfo> getHistoricalSnapshot(@Param("query")HistoricalSnapshotQuery query);

    List<HistoricalSnapshotInfo> getAllHistoricalSnapshot(@Param("query")HistoricalSnapshotQuery query);

    Set<HistoricalSnapshotMapData> getHistoricalSnapshotOfMap(@Param("query")HistoricalSnapshotQuery query);

    List<HistoricalSnapshotInfo> getHistoricalSnapshotMapData(@Param("query")HistoricalSnapshotQuery query);

}
