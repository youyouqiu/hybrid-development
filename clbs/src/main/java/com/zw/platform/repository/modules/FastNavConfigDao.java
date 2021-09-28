package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.form.FastNavConfigForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FastNavConfigDao {

    List<FastNavConfigForm> getList(String userId);

    List<FastNavConfigForm> getOutSideNav(String userId);

    void delete(FastNavConfigForm fastNavConfigForm);

    void add(FastNavConfigForm fastNavConfigForm);

    FastNavConfigForm findBySort(@Param("userId") String userId, @Param("order") String order);

    /**
     * 根据用户id和导航Id查询导航信息
     * @param id Id
     * @return FastNavConfigForm
     */
    FastNavConfigForm findNavById(String id);

    /**
     * 批量新增 Nav
     * @param fastNavConfigForm 导航信息表单
     * @return boolean
     */
    boolean addNavs(List<FastNavConfigForm> fastNavConfigForm);

    /**
     * 批量删除 Nav
     * @param fastNavConfigForm 导航信息表单
     * @return boolean
     */
    boolean deleteNavs(List<FastNavConfigForm> fastNavConfigForm);

    /**
     * 根据Id  删除nav
     * @param id id
     */
    void deleteNavById(@Param("id") String id);

    /**
     * 根据表单修改 order
     * @param fastNavConfigForm form
     * @return bool
     */
    boolean updateNavOrderByForm(FastNavConfigForm fastNavConfigForm);
}
