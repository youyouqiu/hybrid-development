package com.zw.talkback.repository.mysql;

import com.zw.talkback.domain.intercom.form.IntercomModelForm;
import com.zw.talkback.domain.intercom.info.IntercomModelInfo;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.domain.intercom.query.IntercomModelQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  对讲机型dao
 */
public interface IntercomModelDao {

    /**
     * 新增对讲机型
     * @param form
     */
    void addIntercomModel(@Param("form") IntercomModelForm form);

    /**
     * 更新对讲机型
     * @param form
     */
    void updateIntercomModel(@Param("form") IntercomModelForm form);

    /**
     * 批量更新对讲机型
     * @param intercomModels
     */
    void addIntercomModels(@Param("intercomModels") List<IntercomModelForm> intercomModels);

    /**
     * 列表对讲机型
     * @param query
     * @return
     */
    List<OriginalModelInfo> getIntercomModels(@Param("query") IntercomModelQuery query);

    /**
     * 删除对讲机型
     * @param id
     */
    void deleteIntercomModelById(@Param("id") String id);

    /**
     * 删除对讲机型
     * @param ids
     */
    void deleteIntercomModelByIds(@Param("ids") String ids);

    /**
     * 根据对讲机型id获取绑定的信息
     * @param ids
     * @return
     */
    List<String> getBindIntercomModelByIds(@Param("ids") String ids);

    /**
     * 根据名称和id查询对讲机型名称，来判断是否平台是否已经存在
     * @param id
     * @return
     */
    String getModelNameByIdAndName(@Param("name") String name, @Param("id") String id);

    /**
     * 根据id获取对讲机型
     * @param id
     * @return
     */
    IntercomModelInfo getIntercomModelById(@Param("id") String id);

    /**
     * 获得对讲机型列表
     * @return List<IntercomModelInfoNew>
     */
    List<IntercomModelInfo> getAllIntercomModeList();

    /**
     * 在新增对讲机型的时候，判断选择的原始机型是否被绑定了
     * @param originalModelId
     * @return
     */
    List<String> getNameByOriginalModelId(@Param("originalModelId") long originalModelId, @Param("id") String id);
}
