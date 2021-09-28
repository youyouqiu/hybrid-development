package com.zw.platform.service.oilsubsidy;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;

import com.github.pagehelper.Page;
import com.zw.platform.util.common.BusinessException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.zw.platform.util.IPAddrUtil;

/**
 * @author wanxing
 * @Title: 油补增删改查公共service
 * @date 2020/9/3017:32
 */
public interface CrudService<T, Q> {

    /**
     * 添加
     *
     * @param t 操作对象
     * @return
     * @throws BusinessException
     */
    boolean add(T t) throws BusinessException;

    /**
     * 修改
     *
     * @param t
     * @return
     * @throws BusinessException
     */
    boolean update(T t) throws BusinessException;

    /**
     * 删除
     *
     * @param id
     * @return
     * @throws BusinessException
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 批量删除
     *
     * @param ids 删除对象的id集合
     * @return 删除数量
     * @throws BusinessException 异常信息
     */
    default int delBatch(Collection<String> ids) throws BusinessException {
        return 0;
    }

    /**
     * 分页模糊查询
     *
     * @param query
     * @return
     * @throws BusinessException
     */
    Page<T> getListByKeyword(Q query) throws BusinessException;

    /**
     * 详情,通过ID获取接口
     *
     * @param id
     * @return
     * @throws BusinessException
     */
    T getById(String id) throws BusinessException;

    /**
     * 获取request请求
     *
     * @return
     */
    default HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    default String getIpAddress() {
        HttpServletRequest request = getHttpServletRequest();
        return IPAddrUtil.getClientIp(request);
    }
}
