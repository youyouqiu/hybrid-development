package com.zw.platform.basic.service;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.F3SimCardDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.SimCardListDTO;
import com.zw.platform.basic.dto.query.SimCardQuery;
import com.zw.platform.domain.basicinfo.SendSimCard;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zjc
 * @Description:simcard service
 * @Date: create in 2020/10/22 9:10
 */
public interface SimCardService {

    /**
     * 新增sim卡
     * @param simCardDTO
     * @return
     */
    boolean add(SimCardDTO simCardDTO) throws BusinessException;

    /**
     * 修改sim卡
     * @param simCardDTO
     * @return
     */
    boolean updateNumber(SimCardDTO simCardDTO) throws BusinessException;

    /**
     * 删除sim卡信息
     * @param id
     * @return
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 查询列表信息
     * @param simCardQuery
     * @return
     */
    Page<SimCardListDTO> getListByKeyWord(SimCardQuery simCardQuery);

    /**
     * 获取sim卡的绑定默认信息
     * @param bindDTO
     * @return
     */
    SimCardDTO getDefaultInfo(ConfigDTO bindDTO);

    /**
     * 根据sim卡编号获取sim卡
     * @param number
     * @return
     */
    SimCardDTO getByNumber(String number);

    /**
     * 更新sim卡编号
     * @param id     sim卡Id
     * @param number sim卡编号
     * @return realNum 真实的sim卡编号
     */
    boolean updateNumber(String id, String number, String realNum);

    /**
     * 检验是否重复:true代表已经存在，false代表不存在
     * @param number
     * @param id
     * @return
     */
    boolean checkIsExist(String number, String id);

    /**
     * 根据sim卡查询，sim卡的相关信息
     * @param id
     * @return
     */
    SimCardDTO getById(String id);

    /**
     * 导入
     * @param multipartFile
     * @return
     * @throws BusinessException
     */
    JsonResultBean importData(MultipartFile multipartFile) throws Exception;

    /**
     * 验证sim卡是否绑定
     * @param number
     * @return
     */
    boolean checkIsBind(String number);

    /**
     * 导出sim卡
     * @throws Exception
     */
    void exportSimCard() throws Exception;

    /**
     * 批量删除
     * @param simCardIds
     * @return
     */
    boolean deleteBatch(List<String> simCardIds) throws BusinessException;

    /**
     * 获取指定企业下的sim卡id
     * @param orgId
     * @return
     */
    Set<String> getOrgSimCardIds(String orgId);

    /**
     * 获取未绑定sim卡
     * @param keyword 关键词
     * @return 有序的sim卡
     */
    List<Map<String, String>> getUbBindSelectList(String keyword);

    /**
     * 根据终端Id获取SIM卡信息
     * @param deviceIds 终端Id集合
     * @return SIM卡信息
     */
    List<SimCardDTO> getByDeviceIds(Collection<String> deviceIds);

    /**
     * 根据sim卡id获取f3sim卡信息
     * @param id
     * @return
     */
    F3SimCardDTO getF3SimInfo(String id);

    /**
     * 生成导入模板
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 实时监控sim卡指令下发
     * @param sendSimCard
     * @return
     * @throws Exception
     */
    JsonResultBean sendSimCard(SendSimCard sendSimCard);
}
