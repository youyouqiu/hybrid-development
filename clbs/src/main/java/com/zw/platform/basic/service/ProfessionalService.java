package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.domain.ProfessionalShowDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.ProfessionalPageDTO;
import com.zw.platform.basic.dto.query.NewProfessionalsQuery;
import com.zw.platform.domain.basicinfo.query.IcCardDriverQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 从业人员
 */
public interface ProfessionalService {

    /**
         * @param professionalDTO
     * @return boolean
     * @throws @author wangying
     * @Title: 新增从业人员及关联表
     */
    boolean add(ProfessionalDTO professionalDTO) throws Exception;

    /**
     * 上传图片到ftp
     * @param fileUrl
     * @return
     */
    boolean editImg(String fileUrl);

    /**
     * 判断从业人员是否绑定监控对象
     * @param id
     * @return
     */
    boolean isBind(String id);

    /**
     * 删除单个未绑定的从业人员
     * @param id
     * @return
     * @throws Exception
     */
    boolean deleteProfessionalsById(String id) throws Exception;

    /**
     * 删除绑定了的从业人员
     * @param id
     * @return
     * @throws Exception
     */
    boolean deleteBindProfessional(String id) throws Exception;

    /**
     * 批量删除从业人员
     * @param ids
     * @return
     * @throws Exception
     */
    JsonResultBean deleteProfessionalsByBatch(String ids) throws Exception;

    /**
     * 批量删除绑定的车业人员
     * @param ids
     * @return
     * @throws Exception
     */
    JsonResultBean deleteMoreBindProfessional(String ids) throws Exception;

    /**
     * 获取修改页面的从业人员信息
     * @param id
     * @return
     */
    ProfessionalDTO editPageData(String id);

    /**
     * 修改从业人员
     * @param professionalDTO
     * @return
     */
    boolean checkEditProfessional(ProfessionalDTO professionalDTO) throws Exception;

    /**
     * 修改从业人员表及关联表
     * @param newProfessionalDTO
     * @param oldProfessionalDTO
     * @return
     * @throws Exception
     */
    boolean updateProGroupByProId(ProfessionalDTO newProfessionalDTO, ProfessionalDTO oldProfessionalDTO)
        throws Exception;

    /**
     * 查询从业人员分页
     * @param query
     * @return
     * @throws Exception
     */
    Page<ProfessionalPageDTO> getListPage(final NewProfessionalsQuery query) throws Exception;

    /**
     * 生成导入模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 批量导入从业人员
     * @param multipartFile
     * @return
     * @throws Exception
     */
    JsonResultBean importProfessionals(MultipartFile multipartFile) throws Exception;

    /**
     * 导出从业人员
     * @param title
     * @return
     * @throws Exception
     */
    boolean exportProfessionals(String title) throws Exception;

    /**
     * 根据企业id查询从业人员
     * @param orgId
     * @return
     */
    List<ProfessionalDO> getProfessionalsByOrgId(String orgId);

    /**
     * 根据id获取从业人员信息
     * @param ids 从业人员id
     * @return 从业人员
     */
    List<ProfessionalDTO> getProfessionalByIds(Collection<String> ids);

    /**
     * 获取从业人员树
     * @param isOrg
     * @return
     */
    String getTree(String isOrg);

    /**
     * 根据名字校验从业人员存在异常
     * @param name
     * @param identity
     * @return
     */
    boolean repetition(String name, String identity);

    boolean repetitions(String id, String identity);

    /**
     * 根据姓名和身份证号查询从业人员
     * @param name
     * @param identity
     * @return
     */
    ProfessionalDO findByNameExistIdentity(String name, String identity);

    /**
     * 通过身份证查找从业人员
     * @param identity 身份证号
     * @return list
     * @throws Exception Exception
     */
    List<ProfessionalDO> getProfessionalsByIdentity(String identity);

    /**
     * @param name 从业人员姓名
     * @return 从业人员list
     * @throws Exception 异常
     */
    List<ProfessionalDO> getProfessionalsByName(String name);

    /**
     * 根据id获取从业人员
     * @param id
     * @return
     */
    ProfessionalDTO getProfessionalsById(String id);

    /**
     * 驾驶员统计模块的树
     * @param type
     * @param name
     * @return
     */
    JSONObject getIcCardTree(String type, String name);

    /**
     * 驾驶员识别管理的树
     * @param type
     * @param name
     * @return
     */
    JSONObject getProTree(String type, String name);

    /**
     * 根据企业id查询从业人员
     * @param orgIds
     * @return
     */
    Set<String> getRedisOrgProfessionalId(Collection<String> orgIds);

    int getProfessionalCountByPid(String parentId);

    /**
     * 实时监控
     * @param vehicleId
     * @return
     */
    List<ProfessionalDTO> getRiskProfessionalsInfo(String vehicleId);

    /**
     * 用户权限下的从业人员下拉选项
     * @param keyword 从业人员名称关键字
     * @return id - name
     */
    List<Map<String, String>> getSelectList(String keyword);

    /**
     * 通过ic卡号和从业人员名称查询从业人员信息
     * @param icCardDriverQueryList
     * @return
     */
    Map<String, ProfessionalShowDTO> getProfessionalShowMaps(Collection<IcCardDriverQuery> icCardDriverQueryList);

    Map<String, ProfessionalShowDTO> getProfessionalShowMap(Collection<String> ids);
}
