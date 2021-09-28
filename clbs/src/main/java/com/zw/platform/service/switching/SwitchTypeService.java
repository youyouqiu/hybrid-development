package com.zw.platform.service.switching;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title:开关类传感器类型Service
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 14:08
 */
public interface SwitchTypeService {
    /**
     * 查询分页数据
     *
     * @param query
     * @return
     */
    Page<SwitchType> findByPage(BaseQueryBean query) throws Exception;

    /**
     * 查询所有可用
     *
     * @return
     */
    List<SwitchType> findAllow() throws Exception;


    /**
     * 根据主键ID查询外设信息
     *
     * @param id
     * @return
     */
    SwitchType findByid(String id) throws Exception;

    /**
     * 新增
     *
     * @param switchType
     * @return
     */
    JsonResultBean addSwitchType(SwitchType switchType,String ipAddress) throws Exception;


    /**
     * 通过id检查是否已被绑定
     *
     * @param id
     * @return
     */
    Boolean checkBind(String id) throws Exception;

    /**
     * 通过name获取信息
     *
     * @param name
     * @return
     */
    SwitchType findByName(String name) throws Exception;

    /**
     * 通过identify获取信息
     *
     * @param identify
     * @return
     */
    SwitchType findByIdentify(String identify) throws Exception;

    /**
     * 根据ID删除外设信息
     *
     * @param id
     * @param ipAddress
     */
    JsonResultBean deleteById(String id,String ipAddress) throws Exception;

    /**
     * 修改
     *
     * @param switchType
     * @param ipAddress
     */
    JsonResultBean updateSwitchType(SwitchType switchType,String ipAddress) throws Exception;


    /**
     * 批量删除
     *
     * @param ids
     * @param ipAddress
     */
    JsonResultBean deleteBatchSwitchType(List<String> ids,String ipAddress) throws Exception;

    /**
     * 导入
     *
     * @param multipartFilem
     * @param ipAddress
     * @return
     */
    Map addImportSwitchType(MultipartFile multipartFilem,String ipAddress) throws Exception;

    /**
     * 导出
     *
     * @param title    excel名称
     * @param type     导出类型（1:导出数据；2：导出模板）
     * @param response 文件
     * @return
     */
    boolean export(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 生成模板
     *
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    SwitchType findByStateRepetition(String id, String state, Integer flag);

    /**
     * 获得io的功能参数类型
     * @return
     */
    List<SwitchType> getIoSwitchType()throws Exception;
}
