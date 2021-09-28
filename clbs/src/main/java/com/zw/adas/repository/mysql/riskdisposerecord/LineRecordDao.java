package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.report.deliveryLine.LineRecordDo;
import com.zw.adas.domain.report.deliveryLine.LineRecordDto;
import com.zw.adas.domain.report.query.DeliveryLineQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineRecordDao {

    /**
     * 新增
     * @param  lineRecordDo
     * @return boolean
     */
    boolean insert(LineRecordDo lineRecordDo);

    /**
     * 刪除
     * @param id id
     * @return boolean
     */
    boolean delete(Long id);

    /**
     * 更新
     * @param  lineRecordDo
     * @return boolean
     */
    boolean update(LineRecordDo lineRecordDo);

    /**
     * 查询 根据主键 id 查询
     * @param id id
     * @return 809LineRecordDo
     */
    LineRecordDo getById(Long id);

    /**
     * 分页查询
     * @param query
     * @return
     */
    List<LineRecordDto> pageList(@Param("query") DeliveryLineQuery query);

}
