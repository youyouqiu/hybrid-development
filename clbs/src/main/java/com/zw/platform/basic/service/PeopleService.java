package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.domain.PeopleDO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.query.PeopleQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

/**
 * 人员模块服务接口
 * @author zhangjuan
 */
public interface PeopleService extends MonitorBaseService<PeopleDTO> {

    /**
     * 人员是否存在绑定的
     * @param ids 人员ID
     * @return true 存在 false 不存在
     */
    boolean isExistBind(Collection<String> ids);


    Page<PeopleDTO> getPeopleList(PeopleQuery query);

    /**
     * 仅按关键字分页查询
     * @param query query
     * @return 人员详情列表
     */
    Page<PeopleDTO> getListByKeyWord(PeopleQuery query);

    /**
     * 按企业分页查询
     * @param orgId 企业ID
     * @param query query
     * @return 人员详情列表
     */
    Page<PeopleDTO> getListByOrg(String orgId, PeopleQuery query);

    /**
     * 按分组获取人员信
     * @param groupIds 分组ID集合
     * @param query    query
     * @return 人员详情列表
     */
    Page<PeopleDTO> getListByGroup(List<String> groupIds, PeopleQuery query);

    /**
     * 生成人员通用使用导入模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 生成对讲人员使用导入模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateIntercomTemplate(HttpServletResponse response) throws Exception;

    /**
     * 通用-人员信息导出
     * @param response 响应
     * @return 是否导出成功
     * @throws Exception Exception
     */
    boolean export(HttpServletResponse response) throws Exception;

    /**
     * 对象人员信息导出
     * @param response 响应
     * @return 是否导出成功
     * @throws Exception Exception
     */
    boolean exportIntercomPeople(HttpServletResponse response) throws Exception;

    /**
     * 通用-人员导入
     * @param file file
     * @return 导入结果
     * @throws Exception Exception
     */
    JsonResultBean importExcel(MultipartFile file) throws Exception;

    /**
     * 对讲人员导入
     * @param file file
     * @return 导入结果
     * @throws Exception Exception
     */
    JsonResultBean importIntercomExcel(MultipartFile file) throws Exception;

    /**
     * 人员信息批量入库
     * @param peopleList 人员信息
     * @return 操作结果
     */
    boolean addBatch(List<PeopleDO> peopleList);

    /**
     * 更新人员在职状态
     * @param id           人员ID
     * @param isIncumbency 是否在职 0:离职； 2:在职  1:显示空白
     * @return 是否更新成功
     */
    boolean updateIncumbency(String id, Integer isIncumbency);

    /**
     * 更新人员在职状态
     * @param ids          人员ID 集合
     * @param isIncumbency 是否在职 0:离职； 2:在职  1:显示空白
     * @return 是否更新成功
     */
    boolean updateIncumbency(Collection<String> ids, Integer isIncumbency);

    /**
     * 批量删除
     * @param ids ids
     * @return 删除条数
     */
    JSONObject batchDel(Collection<String> ids);

    /**
     * 根据身份证号查询人员
     * @param identity
     * @return
     */
    PeopleDTO getPeopleByIdentity(String identity);

    /**
     * 判断是否有相同的身份证号的人员
     * @param id
     * @param identity
     * @return
     */
    boolean isExistIdentity(String id, String identity);

    /**
     * 根据终端编号获取人员信
     * @param deviceNum 终端编号
     * @return 人员信
     */
    PeopleDTO getByDeviceNum(String deviceNum);

    /**
     * 进行标记，用于区分人车物
     * @return
     */
    default MonitorTypeEnum getMonitorEnum() {
        return MonitorTypeEnum.PEOPLE;
    }
}
