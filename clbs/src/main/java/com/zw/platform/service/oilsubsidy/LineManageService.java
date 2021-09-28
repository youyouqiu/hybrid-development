package com.zw.platform.service.oilsubsidy;

import com.zw.platform.domain.oilsubsidy.line.LineDTO;
import com.zw.platform.domain.oilsubsidy.line.LineQuery;
import com.zw.platform.util.common.BusinessException;

import java.util.Collection;
import java.util.List;

/**
 * @author wanxing
 * @Title: 线路service
 * @date 2020/10/913:47
 */
public interface LineManageService extends CrudService<LineDTO, LineQuery> {

    /**
     * 批量删除，范围string
     * @param ids
     * @return
     * @throws BusinessException
     */
    String delBatchReturnStr(Collection<String> ids) throws BusinessException;

    /**
     * 通过对接码组织Id获取
     * @param  orgId
     * @return
     */
    List<LineDTO> getLineByOrgId(String orgId);

    /**
     * 上报1301
     */
    void upData1301Command();

    /**
     * 上报1302
     */
    void upData1302Command();
}
