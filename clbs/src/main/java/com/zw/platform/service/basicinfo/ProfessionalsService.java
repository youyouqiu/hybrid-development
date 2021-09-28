package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.domain.ProfessionalsTypeDO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsGroupForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsTypeForm;
import com.zw.platform.domain.basicinfo.query.ProfessionalsQuery;
import com.zw.platform.domain.basicinfo.query.ProfessionalsTypeQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Title: 从业人员管理Service
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月26日下午5:09:25
 */
public interface ProfessionalsService {

    /**
     * 查询所有的从业人员
     * @return
     */
    List<ProfessionalDO> selectLicense(List<String> list);

    /**
     * 修改从业人员信息
     * @param professionalsForm
     * @return true:修改成功 false: 修改失败
     */
    boolean updateProfessionals(ProfessionalsForm professionalsForm) throws Exception;

    /**
     * 导出
     * @param title    excel名称
     * @param type     导出类型（1:导出数据；2：导出模板）
     * @param response 文件
     * @return
     */
    boolean exportProfessionals(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 生成导入模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
         * @param id
     * @return ProfessionalsInfo
     * @Title: 根据id查询从业人员
     * @author wangying
     */
    ProfessionalDTO findProfessionalsById(String id);

    /**
         * @param query
     * @return Page<Map < String, Object>>
     * @throws @author wangying
     * @Title: 查询从业人员，关联group
     */
    Page<Map<String, Object>> findProfessionalsWithGroup(ProfessionalsQuery query) throws Exception;

    /**
         * @param id
     * @return Map<String, Object>
     * @throws @author wangying
     * @Title: findProGroupById
     */
    Map<String, Object> findProGroupById(String id);

    /**
         * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: updateProGroupByProId
     */
    boolean updateProGroupByProId(ProfessionalsForm proForm, ProfessionalsGroupForm form) throws Exception;

    /**
         * @return List<Map < String, Object>>
     * @throws
     * @Title: 查询从业人员以及车
     * @author wangying
     */
    List<Map<String, Object>> findProfessionalAndVehicle() throws Exception;

    ProfessionalDO findByProfessionalsInfo(String identity) throws Exception;

    /**
     * 查询从业人员是否已经绑定
     * @param id
     * @return 是否绑定组织
     * @author Fan Lu
     */

    int getIsBandGroup(String id) throws Exception;


    /**
     * 批量查询从业人员是否已经绑定
     * @param ids
     * @return
     * @author Wang Ying
     */
    int getIsBandGroupByBatch(List<String> ids) throws Exception;

    /**
     * 根据名称查询
     * @param name
     * @return
     */
    ProfessionalDO findProfessionalsByName(String name) throws Exception;

    /**
     * 修改时，查询非当前从业人员以外，名称是否重复
     * @param id
     * @param name
     * @return
     */
    ProfessionalDTO findProfessionalsForNameRep(String id, String name) throws Exception;


    /**
     * 新增类型
     */
    void add(ProfessionalsTypeDO professionalsTypeDO, String ipAddress) throws Exception;

    /**
     * 删除类型
     * @param id
     * @return
     */

    boolean deletePostType(String id, String ipAddress) throws Exception;

    /**
     * @param ids
     * @return
     */

    boolean deleteMore(List<String> ids, String ipAddress) throws Exception;

    /**
     * 通过id获取类型
     * @param id
     * @return
     */

    ProfessionalsTypeDO get(final String id) throws Exception;

    /**
     * 修改类型
     * @param form
     * @return
     */
    JsonResultBean update(final ProfessionalsTypeForm form, String ipAddress) throws Exception;

    /**
     * 导出
     */

    boolean exportType(String title, int type, HttpServletResponse response) throws Exception;
    /*
     * 导入
     */

    Map importType(MultipartFile file, String ipAddress) throws Exception;

    /**
     * 生成岗位模版
     * @param response
     * @return
     */

    boolean generateTemplateType(HttpServletResponse response) throws Exception;

    /**
     * 分页查询 User
     * @return
     */
    Page<ProfessionalsTypeDO> findByPage(ProfessionalsTypeQuery query) throws Exception;

    Set<String> getRedisGroupProfessionalId(String groupId);

    List<String> getGroupProfessional();

    /**
     * 根据岗位类型查询数据库是否有相同岗位类型
     */
    ProfessionalsTypeDO findTypeByType(String postType) throws Exception;


    String getIcCardDriverIdByIdentityAndName(String cardNumberAndName);

}
