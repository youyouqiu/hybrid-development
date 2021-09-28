package com.zw.platform.service.regionmanagement;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.domain.functionconfig.form.AdministrationForm;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.regionmanagement.FenceTypeFrom;
import com.zw.platform.domain.regionmanagement.FenceTypeInfo;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 10:51
 */
public interface FenceManagementService {

    /**
     * 获得围栏种类列表
     * @return JsonResultBean
     */
    JsonResultBean getFenceTypeList();

    /**
     * 获得围栏种类信息
     * @param fenceTypeId 围栏种类id
     * @return FenceTypeInfo
     */
    FenceTypeInfo getFenceTypeInfoById(String fenceTypeId);

    /**
     * 新增围栏种类
     * @param fenceTypeFrom 围栏信息
     * @param ipAddress     ip地址
     * @return JsonResultBean
     */
    JsonResultBean addFenceType(FenceTypeFrom fenceTypeFrom, String ipAddress);

    /**
     * 删除围栏种类
     * @param fenceTypeId 围栏种类id
     * @param ipAddress   ip地址
     * @return JsonResultBean
     */
    JsonResultBean deleteFenceType(String fenceTypeId, String ipAddress);

    /**
     * 修改围栏种类
     * @param fenceTypeFrom 围栏信息
     * @param ipAddress     ip地址
     * @return JsonResultBean
     */
    JsonResultBean updateFenceType(FenceTypeFrom fenceTypeFrom, String ipAddress);

    /**
     * 判断围栏种类名称是否能使用(判断是否已存在)
     * @param fenceTypeName 围栏种类名称
     * @param fenceTypeId   围栏种类id
     * @return true:可以使用; false: 不可以使用
     */
    boolean judgeFenceTypeNameIsCanBeUsed(String fenceTypeName, String fenceTypeId);

    /**
     * 判断围栏名称是否能使用(判断是否已存在)
     * @param fenceName   围栏名称
     * @param fenceId     围栏id
     * @param fenceTypeId 围栏种类id
     * @return true:可以使用; false: 不可以使用
     */
    boolean judgeFenceNameIsCanBeUsed(String fenceName, String fenceId, String fenceTypeId);

    /**
     * 获得围栏种类下的绘制方式
     * @param fenceTypeId 围栏种类id
     * @return JsonResultBean
     */
    JsonResultBean getFenceTypeDrawType(String fenceTypeId);

    /**
     * 获得围栏种类下的围栏信息集合
     * @param fenceTypeId 围栏种类id
     * @return JsonResultBean
     */
    JsonResultBean getFenceInfoListByFenceTypeId(String fenceTypeId);

    /**
     * 新增或修改 线
     * @param lineForm  线 信息
     * @param ipAddress ip地址
     * @return JsonResultBean
     */
    JsonResultBean addOrUpdateLine(LineForm lineForm, String ipAddress);

    /**
     * 新增或修改 标注
     * @param markForm  标注 信息
     * @param ipAddress ip地址
     * @return JsonResultBean
     */
    JsonResultBean addOrUpdateMarker(MarkForm markForm, String ipAddress);

    /**
     * 新增或修改 圆
     * @param circleForm 圆 信息
     * @param ipAddress  ip地址
     * @return JsonResultBean
     */
    JsonResultBean addOrUpdateCircle(CircleForm circleForm, String ipAddress);

    /**
     * 新增或修改 多边形
     * @param polygonForm 多边形 信息
     * @param ipAddress   ip地址
     * @return JsonResultBean
     */
    JsonResultBean addOrUpdatePolygon(PolygonForm polygonForm, String ipAddress);

    /**
     * 新增或修改 行政区域
     * @param administrationForm 行政区域信息
     * @param ipAddress          ip地址
     * @return JsonResultBean
     */
    JsonResultBean addOrUpdateAdministration(AdministrationForm administrationForm, String ipAddress);

    /**
     * 获得围栏详情
     * @param fenceId 围栏id
     * @param type    围栏类型
     * @return JsonResultBean
     */
    JsonResultBean getFenceDetail(String fenceId, String type);

    /**
     * 判断围栏是否可以删除(判断是否关联了排班或者任务)
     * @param fenceId 围栏id
     * @return true:可以删除; false: 不可以删除
     */
    boolean judgeFenceCanBeDelete(String fenceId);

    /**
     * 判断围栏是否可以修改(判断是否关联了正在执行的排班或者任务)
     * @param fenceId 围栏id
     * @return true:可以修改; false: 不可以修改
     */
    boolean judgeFenceCanBeUpdate(String fenceId);

    /**
     * 删除围栏
     * @param fenceId   围栏id
     * @param type      围栏类型
     * @param ipAddress ip地址
     * @return JsonResultBean
     */
    JsonResultBean deleteFence(String fenceId, String type, String ipAddress);

    /**
     * 获得用户围栏显示设置
     * @return List<String>
     */
    List<String> getUserFenceDisplaySetting();

    /**
     * 获得围栏tree
     * @return JsonResultBean
     */
    JsonResultBean getFenceTree();

    /**
     * 获得围栏tree JSONArray
     * @return JSONArray
     */
    JSONArray getFenceTreeJsonArray();

    /**
     * 保存用户围栏显示设置
     * @param fenceIds  围栏id
     * @param ipAddress ip地址
     * @return JsonResultBean
     */
    JsonResultBean saveUserFenceDisplaySet(String fenceIds, String ipAddress);
}
