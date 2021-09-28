package com.zw.platform.service.core;

import java.util.List;

import com.zw.platform.domain.core.OperationForm;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.util.common.JsonResultBean;


/**
 * @author cjy
 */
public interface OperationService {

    /**
	 * 增加运营资质类别
	 * @return
	 */
    JsonResultBean addOperation(String type, String explains, String ipAddress) throws Exception;

    /**
     * 删除运营资质类别
     */
    JsonResultBean deleteOperation(List<String> operationIds, String ipAddress) throws Exception;

    /**
     * 查询全部运营资质类别
     */
    List<Operations> findOperation(String type) throws Exception;

    List<Operations> findAll() throws Exception;

    /**
     * 根据id查询运营资质类别
     */
    public Operations findOperationById(String id) throws Exception;

    /**
     * 修改运营资质类别
     */
    public JsonResultBean updateOperation(OperationForm from, String ipAddress) throws Exception;

    /**
     * 根据运营资质类别查询运营资质类别
     */
    public Operations findOperationByOperation(String type) throws Exception;

}
