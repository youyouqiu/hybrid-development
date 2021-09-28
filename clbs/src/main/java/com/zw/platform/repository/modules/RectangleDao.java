package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.form.RectangleForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Tdz on 2016/8/8.
 */
public interface RectangleDao {
    Rectangle getRectangleByID(@Param("id") final String id);

    /**
     * 新增矩形
     */
    boolean rectangles(final RectangleForm form);

    /**
     * 更新矩形区域
     * @author Liubangquan
     */
    boolean updateRectangle(final RectangleForm form);

    boolean fenceInfo(final ManageFenceFrom fenceForm);

    /**
     * 根据id list 查询矩形
     * @author wangying
     */
    List<Rectangle> getRectangleByIds(@Param("ids") final List<String> ids);
}
