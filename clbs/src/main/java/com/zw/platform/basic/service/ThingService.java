package com.zw.platform.basic.service;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.domain.ThingDO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.result.DeleteThingDTO;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.domain.basicinfo.query.ThingInfoQuery;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

public interface ThingService extends MonitorBaseService<ThingDTO>, IpAddressService {
    /**
     * 分页获取物品信息
     * @param query 分页基本查询条件
     * @return 监控对象列表
     */
    Page<ThingInfoForm> getListByKeyWord(ThingInfoQuery query);

    /**
     * 根据组织分页查询监控对象
     * @param orgId 组织ID (uuid)
     * @param query 基本查询条件
     * @return 监控对象列表
     */
    Page<ThingInfoForm> getListByOrg(String orgId, BaseQueryBean query);

    /**
     * 根据分组分页查询监控对象
     * @param groupIds 分组ID
     * @param query    分页查询
     * @return 监控对象列表
     */
    Page<ThingInfoForm> getListByGroup(Collection<String> groupIds, BaseQueryBean query);

    boolean export(HttpServletResponse response) throws Exception;

    boolean thingTemplate(HttpServletResponse response) throws Exception;

    JsonResultBean importThingInfo(MultipartFile multipartFile) throws Exception;

    /**
     * 批量入库物品
     * @param thingList 物品信息
     * @return 操作是否成功
     */
    boolean addByBatch(List<ThingDO> thingList);

    /**
     * 批量删除
     * @param ids ids
     * @return 删除条数
     */
    int batchDel(Collection<String> ids);

    /**
     * 批量删除物品信息
     * @param thingIds
     * @return
     */
    DeleteThingDTO deleteThingInfoByBatch(String thingIds);

    /**
     * 获取基础的物品信息
     * @param id 物品id
     * @return 物品信息
     */
    ThingDO getBaseById(String id);

    /**
     * 校验物品编号是否唯一
     */
    boolean checkThingNumberSole(String thingNumber, String id);

    /**
     * 进行标记，用于区分人车物
     * @return
     */
    default MonitorTypeEnum getMonitorEnum() {
        return MonitorTypeEnum.THING;
    }

}
