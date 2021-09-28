package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarkDao {

    /**
     * 查询标注
     */
    Mark findMarkById(@Param("id") String id);

    /**
     * 查询标注
     */
    List<Mark> findMarkByIds(@Param("ids") List<String> ids);

    /**
     * 新增标注
     */
    boolean marker(final MarkForm form);

    boolean fenceInfo(final ManageFenceFrom fenceForm);

    /**
     * 修改标注
     * @author Liubangquan
     */
    boolean updateMarker(final MarkForm form);

    /**
     * 删除标注
     * @param fenceId 标注id
     * @return boolean
     */
    boolean deleteMarker(String fenceId);
}
