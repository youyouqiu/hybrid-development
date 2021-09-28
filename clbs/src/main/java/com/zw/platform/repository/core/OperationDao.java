package com.zw.platform.repository.core;


import com.zw.platform.domain.core.OperationForm;
import com.zw.platform.domain.core.Operations;

import java.util.List;


/**
 * 运营资质类别dao
 * @author cjy
 */
public interface OperationDao {

    /**
     * 新增运营资质类别
     */
    boolean addOperation(OperationForm operationFrom);

    /**
     * 查询全部运营资质类别
     */

    List<Operations> findAllOperation(String type);

    /**
     * 根据运营资质类别删除运营资质
     */
    boolean deleteOperation(String id);

    /**
     * 根据id查询运营资质类别
     */

    Operations findOperationById(String id);

    /**
     * 修改运营资质类别
     */
    boolean updateOperations(OperationForm operation);

    /**
     * 根据运营资质类别查询运营资质类别
     */
    Operations findOperationByOperation(String type);

    /**
     * 查询所有行业列表
     * @return list
     */
    List<Operations> findOperationList();
}
